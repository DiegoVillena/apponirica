# Plan fase 2 — Motor de transcripción offline (100% local) + audio adjunto coherente

Fecha: 2026-09-30 · Basado en la información verificada de `docs/investigacion.md` y en los hallazgos del móvil real.

## Por qué esta fase

1. **MIUI no comparte el micrófono**: hoy la nota m4a no se guarda en móviles con mic exclusivo cuando transcribimos (era el fallback previsto, pero le quita el audio adjunto al diario).
2. **El "beep"** del servicio de Google suena en cada arranque de segmento y no hay API pública para quitarlo.
3. **Privacidad**: los sueños son íntimos; con motor 100% local, la voz **nunca sale del móvil**.
4. **Testeo determinista**: el motor offline funciona igual en el emulador (CPU) — acabamos el test del "no hay voz real aquí".

## Diseño (una única fuente de audio, ya sin conflictos)

```
AudioRecord propio (PCM 16 kHz mono 16-bit)
   ├─→ streaming al reconocedor offline → texto parcial/final EN VIVO
   └─→ escritor WAV (header + PCM)     → adjunto del sueño que SIEMPRE coincide
```

- Se reemplaza MediaRecorder/m4a por WAV nativo — las libs offline consumen PCM directo: sin conversión de formatos y sin `ffmpeg-kit`.
- Nueva clase `TranscriptorVosk : Transcriptor` (misma interfaz que ya existe — el contrato híbrido se mantiene).
- El `TranscriptorSistema` actual NO se borra: queda como opción en Ajustes (calidad Google a cambio de que el audio salga del móvil).

## Componentes verificados a usar

| Pieza | Dato verificado | Fuente |
|---|---|---|
| **Vosk** | `com.alphacephei:vosk-android:0.3.47` en Maven Central; API streaming con resultados parciales; Apache 2.0 | Maven Central + alphacephei.com |
| Modelo ES pequeño | `vosk-model-small-es-0.42`, **39 MB**, WER ~11–16 (cv/mtedx) | alphacephei.com/vosk/models |
| Modelo ES grande (upgrade) | `vosk-model-es-0.42`, 1,4 GB, WER ~5,8–7,5 | alphacephei.com/vosk/models |
| Upgrade de calidad (fase 2.5) | sherpa-onnx: Moonshine ES quantized **48,4 MB** (feb 2026); zipformer streaming ES 119 MB; Whisper (base 198 MB / small 610 MB) | GitHub releases `k2-fsa/sherpa-onnx` |

## Pasos de implementación (rama `feat/motor-offline`)

1. **Audio pipeline**: `core/audio/FuenteAudio` — AudioRecord 16k/mono/PCM16 + `EscribeWav` (escribe header a posteriori con el tamaño real). Lectura en loop de corrutina IO, distribución de buffers por Flow.
2. **`TranscriptorVosk`**: Model (carga del dir del modelo), Recognizer `setWords(true)`, feed de muestras en streaming, callbacks → `textoParcial`/`textoFinal`/estado (mismo contracto UI: pulso por RMS queda con el nivel real del AudioRecord).
3. **Modelo**: el modelo 39 MB **empaquetado en `assets`** (offline inmediato sin cuentas/CDN; el APK sube ~40 MB — aceptable en MVP). Alternativa futura: descarga on-demand.
4. **Ajustes (DataStore)**: selector de motor `Offline (privado)` / `Google (calidad)`; default **offline**.
5. **Detalle**: reproducción del WAV adjunto; (borrado también del fichero al eliminar el sueño — hoy el m4a queda huérfano: pequeño fix de limpieza en la misma fase).
6. **Pruebas**: emulador → flujo completo offline; móvil real → grabar sueño COMPLETO con audio adjunto, sin beeps y sin conflictos.
7. *(Opcional, fase 2.5)* mejora de calidad con sherpa-onnx (Moonshine ES 48 MB o Whisper int8) bajo la MISMA interfaz `Transcriptor`.

## Riesgos

- **Calidad**: WER de vosk-small-es (~11–16%) peor que Google (~5%): el campoeditable lo mitiga; el upgrade de fase 2.5 lo mejora.
- **Batería**: escucha continua con AudioRecord consume algo más: añadir autostop a los 2 min + stop manual (ya existente).
- **Tamaño**: +39 MB de APK si el modelo va en assets.

## Fuera de esta fase (roadmap ya en `docs/investigacion.md`)

Widget de captura rápida al despertar · diccionario de símbolos · detección de sueños recurrentes · export/import · PIN · recordatorios.