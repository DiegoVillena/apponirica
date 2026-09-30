# AppOnírica 🌙

App Android de diario de sueños con foco en **captura por voz**: nada más despertar, grabas una nota con tu voz, se **transcribe en tu propio móvil** (sin internet, sin nube) y queda guardada como registro del diario, con **palabras clave extraídas automáticamente** (dream signs) y estadísticas.

Repo público: [github.com/DiegoVillena/apponirica](https://github.com/DiegoVillena/apponirica)

## Privacidad: la voz nunca sale del móvil

Por defecto la app usa un **motor de transcripción 100% offline** ([Vosk](https://alphacephei.com/vosk/), modelo small-es empaquetado en el APK): ni el audio ni el texto se envían a ningún servidor. Hay un motor alternativo opcional (el recognizer de Google del sistema, algo más exacto) que puede elegirse en **Ajustes**, dejando claro que en ese caso la voz sí va a los servidores de Google.

| | Vosk local (por defecto) | Google del sistema |
|---|---|---|
| Internet | No hace falta | Necesario |
| Privacidad | Nada sale del móvil | Audio en servidores de Google |
| Audio adjunto | Sí (WAV del sueño) | No (MIUI no comparte el mic) |
| Beep de fondo | No | Sí (servicio de Google) |

## Qué guarda cada sueño

- Texto transcritó (editable a mano) + título opcional.
- Audio de la nota (WAV) y reproductor en el detalle.
- Marcas: Lúcido / Pesadilla, ánimo y claridad (1–5).
- Fecha + origen (voz o teclado), palabras clave con stemming en español, búsqueda de texto completo (FTS4) y estadísticas (total, racha, meses, top de dream signs).

## Stack

- Android nativo: Kotlin 2.0.21 + Jetpack Compose (Material 3) + Navigation Compose.
- Room (FTS4) + DataStore, coroutines/Flow, DI manual (sin Hilt).
- Transcripción offline: `vosk-android 0.3.47` con `vosk-model-small-es-0.42` (Apache 2.0) en `assets` — el APK pesa ~96 MB por el modelo.
- Toolchain: Gradle 8.9, AGP 8.7.3, JDK 17, minSdk 26, targetSdk/compileSdk 35.

## Compilar y probar

```bash
./gradlew :app:assembleDebug        # APK en app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest    # tests del extractor de palabras clave (JVM)
```

Requisitos: JDK 17 y un SDK de Android (AGP 8.7.3). Instala el APK en un dispositivo real para voz real; en emulador funciona el motor offline igualmente (CPU) aunque sin voz real del entorno de prueba.

## Estructura

```
app/src/main/java/com/diegovillena/apponirica/
├── core/
│   ├── audio/      EscritorWav (WAV PCM 16 kHz mono)
│   └── keywords/   StemmerEspanol, ExtractoraPalabrasClave, StopwordsEspanol
├── data/
│   ├── db/         Room: Dream+FTS4, PalabraClave, vínculos, DAOs
│   └── repo/       RepositorioSuenos (guardar/borrar recalcula keywords)
├── transcription/  Transcriptor (contrato) · TranscriptorVosk · TranscriptorSistema
└── ui/             tema, captura, detalle, diario, estadísticas, ajustes
```

## Roadmap

Widget de captura rápida al despertar · detección de sueños recurrentes · export/import · PIN · recordatorios · mejora de calidad del motor (sherpa-onnx / Whisper) — ver `docs/investigacion.md` y `docs/plan-fase2.md`.