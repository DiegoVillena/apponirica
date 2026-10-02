# AGENTS.md — AppOnírica

## Qué es
App Android de diario de sueños con foco en **captura por voz**: grabar una nota de audio nada más despertar → transcribirla a texto (offline cuando sea posible) → guardar cada sueño como registro independiente → extraer **palabras clave automáticamente** y acumularlas en una BD para mostrar las más usadas (dream signs).

## Stack
- **Android nativo**: Kotlin 2.0.21 + Jetpack Compose (BOM 2024.12.01) + Material 3 + Navigation Compose 2.8.5.
- **Datos**: Room 2.6.1 vía KSP (FTS4 para búsqueda de texto completo), Coroutines/Flow, DataStore 1.2.1 (ajustes: motor de transcripción).
- **Transcripción**: interfaz `Transcriptor` intercambiable con dos motores, elegibles en Ajustes: `TranscriptorVosk` (offline, **por defecto**, vosk-android 0.3.47 + modelo `vosk-model-small-es-0.42` en assets) y `TranscriptorSistema` (SpeechRecognizer de Google, es-ES, encadenamiento de segmentos). El motor elegido se resuelve en `AppContainer.motorActual` (cacheado en memoria del DataStore). Ver `docs/investigacion.md` y `docs/plan-fase2.md`.
- **Audio**: una sola fuente para el motor offline: `AudioRecord` (16 kHz mono PCM16) → alimenta a Vosk en streaming Y alimenta a `EscritorWav` (WAV adjunto del sueño en `getExternalFilesDir/notas_voz`). El reconocedor de Vosk nace y muere con cada sesión (el `Model` se caches). `GrabadoraAudio` (MediaRecorder/m4a) quedó huérfano sin referencias.
- **DI**: manual (`AppContainer` en `AppOniricaApplication`), sin Hilt.
- Toolchain: Gradle 8.9 (wrapper, bootstrap reutilizado de wikiloklocal), AGP 8.7.3, JDK 17, minSdk 26, targetSdk/compileSdk 35. **No** subir a AGP 9.x/Kotlin 2.4/BOM 2026 sin instalar build-tools 36 (sdkmanager ausente en este PC; upgrade anotado). APK ~96 MB por el modelo en assets.

## Identidad visual
- Paleta propia "Medianoche" (oscuro: fondo `#141126`, lavanda `#A78BFA`, ámbar para Lúcido, coral para Pesadilla) + variante clara "Amanecer". Sin dynamic color.
- Tipografía Nunito variable bundlada (`res/font/nunito_variable.ttf`, weights por FontVariation).
- Icono adaptive custom: luna creciente + "zZ" + estrellas (vectores: `drawable/ic_launcher_background` con gradiente índigo→violeta, `drawable/ic_launcher_foreground`, capa monochrome).

## Estructura (app/src/main/java/com/diegovillena/apponirica/)
- `data/db/` — Dream(+DreamFts FTS4), PalabraClave, SuenoPalabraClave, DreamDao, PalabraClaveDao, AppDatabase
- `data/repo/RepositorioSuenos` — guardar/actualizar/eliminar recalculando keywords en transacción; `estadisticas` = Flow combinado (total, racha, meses, top 20)
- `core/keywords/` — StemmerEspanol (reglas plurales y verbales), ExtractoraPalabrasClave (tokens→stopwords→stem→tf + `consultaFts`), StopwordsEspanol
- `core/audio/` — EscritorWav (WAV PCM 16 kHz mono 16-bit, cabecera reescrita al cerrar)
- `transcription/` — Transcriptor (contrato + MotorTranscripcion) + TranscriptorVosk (offline) + TranscriptorSistema (Google)
- `ui/` — theme (paleta + tipografía), common (PalabraClaveChip, MarcaChip, SelectorMarcado, EstadoVacio, BarraConteo, formatearFechaSueno), AppNavHost (pestañas Diario/Estadísticas/Ajustes + FAB; rutas `captura`, `detalle/{id}` y `ajustes`, con ReproductorAudio en detalle), y ViewModel por pantalla
- `assets/model-es-small/` — modelo Vosk small-es (copiado por copiador propio SIN el symlink `uuid`; ver commonerrors.md)
- `res/` — icono/gradiente/foreground/monochrome, `drawable/ilustracion_suenos.xml`, `values(-night)` con fondo/estado de barra del sistema
- **Resource-ids expuestos** (`testTagsAsResourceId` en la raíz): `campo-buscar`, `lista-suenos`, `btn-grabar`, `campo-texto`, `campo-titulo`, `btn-guardar`, `btn-volver`, `btn-borrar`, `tab-diario`, `tab-estadisticas`, `tab-ajustes`, `fab-grabar`, `ajuste-offline`, `ajuste-google`, `chip-keyword-<stem saneado>` — para tests con UI Automator sin visión.

## Cómo se compila/ejecuta
- Build: `./gradlew :app:assembleDebug` (con `JAVA_HOME='C:\Program Files\Java\jdk-17.0.5'` si falta) → APK en `app/build/outputs/apk/debug/`
- Tests: `./gradlew :app:testDebugUnitTest` (stemmer + extractor, JVM puros)
- Emulador: plugin android-emulator; AVD `Pixel_3a_API_34_extension_level_7_x86_64` (imagen con Play). Con el motor Vosk el emulador SÍ hace el flujo completo (CPU), salvo voz real del entorno de prueba; el motor Google entra en ERROR_NO_MATCH (fallback esperado). Voz real en dispositivo físico.
- Builds largos y logs: los MCP `android_*` cortan a 30 s (ver commonerrors.md).
- Capturas de referencia: `docs/capturas/`
- Repo público: `github.com/DiegoVillena/apponirica` (SSH `git@github.com:DiegoVillena/apponirica.git`).

## Estado actual
- 2026-10-02 (tarde, merges de portfolio): `main` ya es portada del portfolio público (merge commit `9edb192`): README de nivel portfolio en español + `README.en.md` espejo, capturas regeneradas del emulador con 3 sueños demo (borradas `movil_actual.png` del móvil real y `launcher.png`), delta de borrado corregido (`dreamCount` baja siempre: commit `0577837` en `feat/motor-offline`). GitHub detecta Kotlin, `main` por defecto, README visible. Los 3 sueños demo están en el emulador (Pixel_3a_API_34): si el emulador se recrea, hay que recargarlos a mano (la automatización no puede teclear tildes/ñ).
- 2026-10-02 (mañana): fixes de usabilidad y de datos **commiteados** en `feat/motor-offline`: estadísticas por PRESENCIA (ranking y número por `dreamCount`, no `totalCount`; sección "Palabras clave más recurrentes" con "N sueños"), señal de versión en `RepositorioSuenos` (la lista del diario refresca al guardar/borrar en caliente), anclaje de la lista al top, y formularios de captura/detalle scrollables con los botones fijos abajo. Verificado en emulador y en el móvil real; instalable compartible en `Descargas/AppOnirica.apk` del móvil.
- 2026-09-30: fase 2 (motor offline) **commiteada** en rama `feat/motor-offline`: `TranscriptorVosk` por defecto (sin internet, sin beep, WAV adjunto que siempre coincide con el relato), selector de motor en la nueva pestaña Ajustes (DataStore), borrado del sueño limpia su fichero de audio, reproducción del WAV en el detalle. Móvil real Xiaomi/MIUI **verificado por el usuario con voz real**: transcripción y adjunto funcionando. Fixes de la sesión: micro inerte tras la primera escucha (flag de sesión nunca reseteado → reconocedor por sesión), sueño tecleado heredaba el WAV del sueño anterior (solo adjuntar si hubo voz), motor elegido ignorado en captura (motorActual cacheado en memoria). Pendiente: borrar `GrabadoraAudio.kt` (código muerto), merge a `main`, LICENSE, y fase 2.5 (upgrade de calidad sherpa-onnx) según `docs/plan-fase2.md`.