# AGENTS.md — AppOnírica

## Qué es
App Android de diario de sueños con foco en **captura por voz**: grabar una nota de audio nada más despertar → transcribirla a texto (offline cuando sea posible) → guardar cada sueño como registro independiente → extraer **palabras clave automáticamente** y acumularlas en una BD para mostrar las más usadas (dream signs).

## Stack
- **Android nativo**: Kotlin 2.0.21 + Jetpack Compose (BOM 2024.12.01) + Material 3 + Navigation Compose 2.8.5.
- **Datos**: Room 2.6.1 vía KSP (FTS4 para búsqueda de texto completo), Coroutines/Flow, DataStore 1.2.1 (reservado para settings).
- **Transcripción**: interfaz `Transcriptor` intercambiable; implementación actual `TranscriptorSistema` (SpeechRecognizer del sistema, es-ES, resultados parciales + RMS). Fase 2: motor offline (sherpa-onnx/Vosk) — ver `docs/investigacion.md`.
- **Audio**: `GrabadoraAudio` (MediaRecorder → m4a en `getExternalFilesDir/notas_voz`); graba en paralelo al recognizer; si el micrófono está ocupado, fallback silencioso a solo-texto.
- **DI**: manual (`AppContainer` en `AppOniricaApplication`), sin Hilt.
- Toolchain: Gradle 8.9 (wrapper, bootstrap reutilizado de wikiloklocal), AGP 8.7.3, JDK 17, minSdk 26, targetSdk/compileSdk 35. **No** subir a AGP 9.x/Kotlin 2.4/BOM 2026 sin instalar build-tools 36 (sdkmanager ausente en este PC; upgrade anotado).

## Identidad visual
- Paleta propia "Medianoche" (oscuro: fondo `#141126`, lavanda `#A78BFA`, ámbar para Lúcido, coral para Pesadilla) + variante clara "Amanecer". Sin dynamic color.
- Tipografía Nunito variable bundlada (`res/font/nunito_variable.ttf`, weights por FontVariation).
- Icono adaptive custom: luna creciente + "zZ" + estrellas (vectores: `drawable/ic_launcher_background` con gradiente índigo→violeta, `drawable/ic_launcher_foreground`, capa monochrome).

## Estructura (app/src/main/java/com/diegovillena/apponirica/)
- `data/db/` — Dream(+DreamFts FTS4), PalabraClave, SuenoPalabraClave, DreamDao, PalabraClaveDao, AppDatabase
- `data/repo/RepositorioSuenos` — guardar/actualizar/eliminar recalculando keywords en transacción; `estadisticas` = Flow combinado (total, racha, meses, top 20)
- `core/keywords/` — StemmerEspanol (reglas plurales y verbales), ExtractoraPalabrasClave (tokens→stopwords→stem→tf + `consultaFts`), StopwordsEspanol
- `core/audio/GrabadoraAudio`
- `transcription/` — Transcriptor (interfaz) + TranscriptorSistema
- `ui/` — theme (paleta + tipografía), common (PalabraClaveChip, MarcaChip, SelectorMarcado, EstadoVacio, BarraConteo, formatearFechaSueno), AppNavHost (pestañas Diario/Estadísticas + FAB; rutas `captura` y `detalle/{id}`), y ViewModel por pantalla
- `res/` — icono/gradiente/foreground/monochrome, `drawable/ilustracion_suenos.xml`, `values(-night)` con fondo/estado de barra del sistema
- **Resource-ids expuestos** (`testTagsAsResourceId` en la raíz): `campo-buscar`, `lista-suenos`, `btn-grabar`, `campo-texto`, `campo-titulo`, `btn-guardar`, `btn-volver`, `btn-borrar`, `tab-diario`, `tab-estadisticas`, `fab-grabar`, `chip-keyword-<stem saneado>` — para tests con UI Automator sin visión.

## Cómo se compila/ejecuta
- Build: `./gradlew :app:assembleDebug` (con `JAVA_HOME='C:\Program Files\Java\jdk-17.0.5'` si falta) → APK en `app/build/outputs/apk/debug/`
- Tests: `./gradlew :app:testDebugUnitTest` (stemmer + extractor, JVM puros)
- Emulador: plugin android-emulator; AVD `Pixel_3a_API_34_extension_level_7_x86_64` (imagen con Play). El recognizer del emulador no recibe voz real: el banner de error y el modo teclado son el fallback esperado; voz real en dispositivo físico.
- Capturas de referencia: `docs/capturas/`

## Estado actual
- 2026-09-30: esqueleto MVP **commiteado** en rama `feat/esqueleto-mvp` (build verde, 11 tests unitarios verdes) y **verificado en emulador + móvil real con voz real**: transcripción funcionando, transcripción dirigida al campo activo (relato/título). Fixes de la sesión: recognizer del sistema con PRIORIDAD sobre MediaRecorder (MIUI no comparte el micrófono), encadenamiento de segmentos para que las pausas no corten la escucha, supresión del aviso falso al parar manualmente. Límite documentado: el "beep" del servicio de Google en cada arranque de segmento (desaparece con el motor offline). Capturas de referencia en `docs/capturas/`. Siguiente fase: motor offline (ver `docs/plan-fase2.md`).