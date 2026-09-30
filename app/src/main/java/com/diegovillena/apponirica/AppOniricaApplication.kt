package com.diegovillena.apponirica

import android.app.Application
import android.content.Context
import com.diegovillena.apponirica.core.audio.GrabadoraAudio
import com.diegovillena.apponirica.core.keywords.ExtractoraPalabrasClave
import com.diegovillena.apponirica.data.db.AppDatabase
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import com.diegovillena.apponirica.transcription.Transcriptor
import com.diegovillena.apponirica.transcription.TranscriptorSistema

class AppOniricaApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

/** DI manual de toda la app: una sola instancia por dependencia, sin frameworks. */
class AppContainer(contexto: Context) {
    private val appContext = contexto.applicationContext

    val db: AppDatabase by lazy { AppDatabase.crear(appContext) }

    val extractor = ExtractoraPalabrasClave()

    val repositorio: RepositorioSuenos by lazy { RepositorioSuenos(db, extractor) }

    val grabadora: GrabadoraAudio by lazy { GrabadoraAudio(appContext) }

    val transcriptor: Transcriptor by lazy { TranscriptorSistema(appContext) }
}