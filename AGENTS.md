# AGENTS.md — AppOnírica

## Qué es
App Android de diario de sueños con foco en **captura por voz**: grabar una nota de audio nada más despertar → transcribirla a texto (offline cuando sea posible) → guardar cada sueño como registro independiente → extraer **palabras clave automáticamente** y acumularlas en una BD para mostrar las más usadas (dream signs).

## Stack (decidido el 2026-09-30)
- **Android nativo**: Kotlin + Jetpack Compose + Room (con FTS4 para búsqueda) + DataStore.
- **Transcripción: híbrido** con interfaz `Transcriber` intercambiable:
  - MVP: recognizer del sistema (SpeechRecognizer) — gratis, calidad top en español.
  - Fase 2: modelo offline descargable (sherpa-onnx con Moonshine ES ~48 MB, o Vosk small-es 39 MB) para privacidad total y dispositivos sin servicios de Google.
- Cloud de pago (OpenAI/Deepgram) descartada por ahora: solo como opción futura opt-in.

## Estructura
- `docs/investigacion.md` — investigación de apps existentes y opciones de voz→texto (fuentes 2026).
- `(por crear) app/` — módulo Android cuando se monte el proyecto.

## Cómo se compila/ejecuta
- Gradle Android (por definir). Se prueban los builds con el emulador Android disponible en este entorno (plugin android-emulator).

## Estado actual
- 2026-09-30: investigación inicial completada (apps existentes + opciones de transcripción, ver `docs/investigacion.md`). Decisiones tomadas: plataforma Android nativo Kotlin, transcripción híbrida (sistema→offline). Repo GitHub público creado. Pendiente: montar esqueleto del proyecto.