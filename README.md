# 🌙 AppOnírica

> Diario de sueños para Android con captura por voz y **transcripción 100% offline**: tu voz, tu texto y tu audio nunca salen del móvil.

![Plataforma](https://img.shields.io/badge/plataforma-Android-3DDC84) ![Transcripción](https://img.shields.io/badge/transcripci%C3%B3n-100%25_offline-8B5CF6) ![Privacidad](https://img.shields.io/badge/sin_telemetr%C3%ADa_ni_analytics-A78BFA) ![Estado](https://img.shields.io/badge/estado-app_personal-orange) ![Licencia](https://img.shields.io/badge/licencia_todos_los_derechos_reservados-lightgrey)

**Español** · [English](README.en.md)

App Android de diario de sueños que **yo mismo uso a diario en mi móvil**: nada más despertar grabo una nota con la voz, un motor de transcripción local la convierte a texto sin internet, y el sueño queda guardado con título, marcadores (lúcido/pesadilla), ánimo y claridad. Las palabras clave del sueño se extraen automáticamente con un stemmer propio y se acumulan para mostrar mis temas recurrentes. Construida de principio a fin por una sola persona (desarrollo asistido por IA), sin Hilt y sin Firebase.

## Por qué existe

Es la app que quise para mí: un diario de sueños sin fricción (la voz es la forma natural de capturar algo nada más despertar), 100% privado y sin dependencias de nube. El código queda aquí como pieza de portfolio: puedes leerlo, compilarlo y usarlo como referencia.

## 🔒 Privacidad: la voz nunca sale del móvil

Por defecto la app transcribe con un **motor local** ([Vosk](https://alphacephei.com/vosk/), modelo small-es empaquetado en el APK): ni el audio ni el texto se envían a ningún servidor; no hay telemetría, analytics, cuentas ni peticiones de red en el flujo normal de la app.

| | Vosk local (por defecto) | Google del sistema (opcional) |
|---|---|---|
| Internet | No hace falta | Necesario |
| Privacidad | Nada sale del móvil | La voz va a los servidores de Google |
| Audio adjunto | Sí (WAV del sueño) | No (algunas ROMs no comparten el mic) |
| Beep de fondo | No | Sí |

El motor de Google es una alternativa **opcional** que el usuario elige en Ajustes; nunca es motor por defecto ni vía de datos de la app.

## ✨ Capturas

<table>
  <tr>
    <td><img src="docs/capturas/diario.png" width="200" alt="Diario"></td>
    <td><img src="docs/capturas/captura.png" width="200" alt="Captura por voz"></td>
    <td><img src="docs/capturas/detalle.png" width="200" alt="Detalle del sueño"></td>
  </tr>
  <tr>
    <td><i>Diario con palabras clave</i></td>
    <td><i>Captura por voz</i></td>
    <td><i>Detalle con nota de voz</i></td>
  </tr>
  <tr>
    <td><img src="docs/capturas/estadisticas.png" width="200" alt="Estadísticas"></td>
    <td><img src="docs/capturas/ajustes.png" width="200" alt="Ajustes"></td>
  </tr>
  <tr>
    <td><i>Estadísticas (1 palabra por sueño)</i></td>
    <td><i>Selector de motor de transcripción</i></td>
  </tr>
</table>

## Qué guarda cada sueño

- Texto transcrito (editable a mano) + título opcional.
- Audio de la nota (WAV) y reproductor en el detalle.
- Marcas: Lúcido / Pesadilla, ánimo y claridad (1–5).
- Fecha + origen (voz o teclado), palabras clave con stemming en español, búsqueda de texto completo (FTS4) y estadísticas (total, racha, meses y top de dream signs contando cada palabra como máximo 1 vez por sueño).

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

## Estado y roadmap

- **Ahora: app personal funcionando en mi móvil.** MVP esqueleto → fase 2 (motor offline Vosk con WAV adjunto y selector de motor) → pulido de usabilidad y de datos (estadísticas por presencia, refresco de listas, formularios scrollables).
- Pendiente/elegible después: widget de captura rápida al despertar · detección de sueños recurrentes · export/import · PIN · recordatorios · mejora de calidad del motor (sherpa-onnx / Whisper). Véase `docs/investigacion.md` y `docs/plan-fase2.md`.

## Limitaciones honestas

- El modelo small transcribe con error del orden del 11–16% (WER): el campo es editable a mano y el motor de Google es la alternativa de calidad.
- Sin export/import todavía: los sueños viven en la base de datos local del móvil.
- App para uso personal: sin licencia open-source (todos los derechos reservados, ver abajo).

## Licencia

© 2026 Diego Villena. Todos los derechos reservados. El código se publica en GitHub con fines de portfolio y de referencia: puedes leerlo, compilarlo y probarlo, pero no redistribuirlo ni reutilizarlo sin permiso. La app se ofrece «tal cual», sin garantía de ningún tipo; los datos de tus sueños son tuyos y viven localmente en tu dispositivo.