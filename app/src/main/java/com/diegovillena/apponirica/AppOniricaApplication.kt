package com.diegovillena.apponirica

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.diegovillena.apponirica.core.keywords.ExtractoraPalabrasClave
import com.diegovillena.apponirica.data.db.AppDatabase
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import com.diegovillena.apponirica.transcription.MotorTranscripcion
import com.diegovillena.apponirica.transcription.Transcriptor
import com.diegovillena.apponirica.transcription.TranscriptorSistema
import com.diegovillena.apponirica.transcription.TranscriptorVosk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** DataStore único de ajustes de la app. */
val Context.almacenAjustes by preferencesDataStore(name = "ajustes")

class AppOniricaApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

/** DI manual de toda la app: una sola instancia por dependencia, sin frameworks. */
class AppContainer(contexto: Context) {
    private val appContext = contexto.applicationContext

    private val alcanceEdicion = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val db: AppDatabase by lazy { AppDatabase.crear(appContext) }

    val extractor = ExtractoraPalabrasClave()

    val repositorio: RepositorioSuenos by lazy { RepositorioSuenos(db, extractor) }

    val transcriptorVosk: Transcriptor by lazy { TranscriptorVosk(appContext) }

    val transcriptorSistema: Transcriptor by lazy { TranscriptorSistema(appContext) }

    /** Motor activo EN MEMORIA: la captura decide al crear su ViewModel, sin esperar al primer
     *  valor del DataStore por composición (llegaría tarde y cogería el default equivocado). */
    @Volatile
    var motorActual: MotorTranscripcion = MotorTranscripcion.OFFLINE
        private set

    /** Motor elegido por el usuario: por defecto local (privacidad total y sin beeps). */
    val motor: Flow<MotorTranscripcion> = appContext.almacenAjustes.data
        .map { ajustes ->
            ajustes[claveMotor]?.let { nombre ->
                runCatching { MotorTranscripcion.valueOf(nombre) }
                    .getOrDefault(MotorTranscripcion.OFFLINE)
            } ?: MotorTranscripcion.OFFLINE
        }

    init {
        alcanceEdicion.launch {
            motor.collect { elegido -> motorActual = elegido }
        }
    }

    fun fijarMotor(nuevo: MotorTranscripcion) {
        alcanceEdicion.launch {
            appContext.almacenAjustes.edit { ajustes ->
                ajustes[claveMotor] = nuevo.name
            }
        }
    }

    fun transcriptorPara(): Transcriptor = when (motorActual) {
        MotorTranscripcion.OFFLINE -> transcriptorVosk
        MotorTranscripcion.GOOGLE -> transcriptorSistema
    }

    private companion object {
        val claveMotor = stringPreferencesKey("motor_transcripcion")
    }
}