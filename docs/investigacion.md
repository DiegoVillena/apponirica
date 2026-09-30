# Investigación inicial — AppOnírica

Fecha: 2026-09-30 · Objetivo: decidir qué funcionalidades robar de las apps existentes de diario de sueños y qué método usar para transcribir la nota de voz a texto.

---

## 1. Qué es AppOnírica

App Android de diario de sueños donde el captador principal es la **voz**: el usuario graba una nota de audio nada más despertar, la app la **transcribe a texto**, guarda cada sueño como un registro independiente, y en cada inserción aplica un **algoritmo de palabras clave** que acumula y-ranking de las palabras más usadas (≈ "dream signs").

---

## 2. Panorama de apps existentes (2026)

### 2.1 Apps dedicadas

| App | Plataformas | Nota Play Store | Precio | Idea central |
|---|---|---|---|---|
| **Dream Journal Ultimate (DJU)** | Android + iOS | 4,3★ (Play) · 4,7 en reviews | Gratis / 4,99 USD-mes | **Voz con transcripción automática** + detección automática de *dream signs* que aprende de tus entradas, tags por emoción/persona/lugar/tema, calendario, estadísticas, exportar PDF/texto, comunidad |
| **Lucidity** | Android + iOS | **4,8★ (la mejor valorada en Play)** | Gratis / 5,99 USD-mes | Todo-en-uno: journal + recordatorios de *reality checks* + guías técnicas + correlación técnica↔resultados + comunidad opcional. Voz básica |
| **DreamLens** | Android + iOS | — (23/25 en test comparativo) | Gratis (10 sueños/mes) / 4,99 USD-mes | **Voz-first: la captura por voz más rápida de las testeadas**; IA personalizada; *pattern tracking* que auto-detecta temas SIN tags manuales; arte con IA; smart alarm que despierta en fase útil |
| **Hypnos** | Móvil | — (21/25) | 6,99 USD-mes | Análisis profundo multi-marco (Jung, Freud, cognitivo, emocional); transcripción por voz rápida y exacta; estudio de **símbolos recurrentes**; privacidad fuerte; sin arte IA |
| **DreamApp** | Android + iOS | 4,4 | 9,99 USD-mes | Voz→texto de alta precisión (en su nube), análisis IA de temas, insights personalizados; **procesa el material en sus servidores** y entrena con datos anonimizados |
| **Shape** | iOS | 4,6 | 6,99 USD-mes | Diseño: colores por estado de ánimo, timeline visual, backup cifrado E2E; sin voz |
| **Awoken** | Android | 4,3 | Gratis con ads / 4,99 pago único | **Todo local/offline y privacidad**; análisis de frecuencia de dream signs; totem sound; sin voz, UI anticuada |
| **Capture** | iOS | 4,2 | 3,99 USD-mes | **Lanzar→grabar en <2 s con widget**; local-first; minimalista |
| **Oniri** | Android + iOS | 3,5★ (Play) | 5,99 USD-mes | Journal clásico, hábito de escritura |

Otras apps visibles en Play Store con buena nota: Dream Advisor (4,6), Dreamlusive (4,5), Temenos Dream (4,5), Dreamseer (4,5), Dream Catcher (4,3), reDream ("Offline Dream Journal"), Somnio (3,8).

### 2.2 Dato clave de dominio: velocidad de captura

- Se pierde ~50% del contenido de un sueño en los **5 primeros minutos** tras despertar (investigación de recall, Aspy et al. 2017 citada por Oneironaut).
- Por eso todas las apps punteras apuestan por: **grabar con voz sin abrir los ojos / sin coger el teléfono a ratos**, acceso en 1 toque (widget), y transcripción instantánea.
- "Keywords first, then expand": capturar 5-10 palabras al despertar y expandir el relato despues es una técnica real que promueven.

### 2.3 Dónde están las oportunidades

1. **La transcripción de voz es EL factor diferencial** y en todas las apps punteras cuesta suscripción (4–10 USD/mes) o va en la nube. Nadie la ofrece **offline y gratuita**.
2. La **detección automática de patrones/palabras clave** es la segunda feature valorada (DJU la aprende de tus entradas; DreamLens auto-detecta temas; Awoken hace análisis de frecuencia) y también está tras paywalls.
3. La **privacidad es un argumento de venta real** (los sueños son íntimos): Hypnos y Awoken destacan por no compartir; DreamApp pierde por entrenar IA con tus sueños. Un enfoque 100% local encaja.
4. Todas tienen freemium caro → una app **gratis, offline, sin cuenta** ocupa un nicho vacío.

---

## 3. Ideas a reaprovechar

| Idea | Qué es | Fase |
|---|---|---|
| Grabación por voz con transcripción | Núcleo del MVP: botón grabar → texto editable + audio guardado | **MVP** |
| Autocompletado de palabras clave | Extraer keywords de cada entrada y acumularlas en BD con ranking de las más usadas | **MVP** (es lo que pide el usuario) |
| Captura ultrarrápida | Widget/atajo que abre directamente en "grabar", transcripción en <10 s | MVP+ |
| Tags por emoción/persona/lugar/tema | Chips editables + autoclase sugerida por el algoritmo | MVP+ |
| Marcas por sueño | ¿Lúcido? / ¿Pesadilla? / claridad 1–5 / estado de ánimo | **MVP** |
| Búsqueda de texto completo | FTS sobre los relatos + filtro por palabra clave | **MVP** |
| Estadísticas | Top-palabras (nube/lista), nº de sueños, racha, gráfica mensual, % lucidez | **MVP** (ver keywords) |
| Símbolos recurrentes / suenos repetidos | Detectar si una keyword reaparece N veces con su historial | MVP+ |
| Diccionario de símbolos | Definición local de símbolos típicos (volar, agua, caída…) al tocar un keyword | MVP+ |
| Recordatorio al despertar | Notificación/alarma para anotar al despertar; smart alarm (fase REN) | Después |
| Reality checks y técnicas lucidez | Guías MILD/WBTD, recordatorios aleatorios en el día | Después |
| Export/import y backup | PDF/texto/JSON (todas lo tienen; fidelidad y portabilidad) | MVP+ |
| Bloqueo con PIN/biometría | Privacidad | MVP+ |
| Modo oscuro, UI minimalista | Consenso en todas las reviews | **MVP** |
| Comunidad/interpretación IA | Compartir anónimamente, interpretación Jung/Freud con LLM | Después (opcional, en la nube, nunca por defecto) |

---

## 4. Transcripción voz → texto (estado 2026, fuentes primarias)

### 4.1 Opciones on-device (gratis, sin cuenta)

| Opción | Español | Tamaño del modelo | Streaming en vivo | Integración Android | Verificado |
|---|---|---|---|---|---|
| **SpeechRecognizer del sistema** (`android.speech`) | Excelente (Google) | 0 (ya está en el móvil) | Sí (resultados parciales) | API estática de Android, `RecognitionListener`, permiso `RECORD_AUDIO` | Doc oficial |
| **SpeechRecognizer on-device** (`createOnDeviceSpeechRecognizer`) | Bueno | se descarga el modelo via `triggerModelDownload` | Sí | Misma API; Android moderno | Doc oficial (métodos existen) |
| **sherpa-onnx** (K2-FSA, 15,4k★) | Multiples ES: Moonshine ES 48 MB, zipformer streaming ES (kroko) 119 MB, NVIDIA fast-conformer CTC-es int8 99 MB, canary es-en 147 MB, Whisper tiny/base/small 111/198/610 MB | Sí (kroko/moonshine) | Ejemplos oficiales Android + Flutter + Kotlin API; runtime ONNX CPU | GitHub releases `k2-fsa/sherpa-onnx` (tamaños de assets medidos) |
| **whisper.cpp** (52k★) | Whisper base/small | 75–466 MB ggml (tiny/base oficialmente recomendados para Android) | No (transcribe al terminar) | Ejemplos oficiales `examples/whisper.android` (+ Java) y JNI | README oficial |
| **Vosk** | `vosk-model-small-es-0.42` 39 MB (WER ~11–16) / `vosk-model-es-0.42` 1,4 GB (WER ~5,8–10) | streaming sí | AAR publica en Maven Central `com.alphacephei:vosk-android:0.3.47` | Página oficial de modelos + Maven Central |
| **Whisper TFLite** (`vilassn/whisper_android`, 692★) | Whisper cuantizado | variable | No | Kotlin/TFLite | GitHub |

### 4.2 APIs cloud (de pago, verificadas en 2026)

| API | Precio | Español | Notas |
|---|---|---|---|
| **OpenAI** `whisper-1` / `gpt-4o-transcribe` | 0,006 USD/min (0,36/h) | 50+ idiomas | fichero máx 25 MB; sin streaming real |
| **OpenAI** `gpt-4o-mini-transcribe` | 0,003 USD/min | 50+ | la económica |
| **AssemblyAI Universal-2** | 0,0025 USD/min | sí | la más barata encontrada |
| **Deepgram Nova-3** | 0,0043 USD/min batch; streaming 0,0048–0,0077 | 50+ | 200 USD de crédito gratis |
| **Google Cloud STT** | ~0,016 USD/min | sí | la más cara |

A ritmo de 1 nota/día de ~2 min: ≈ 0,43 USD/mes con gpt-4o-mini-transcribe. Barato, pero requiere API key + internet (y no es ideal en modelo "gratis/offline").

### 4.3 Detalles técnicos clave para Android

- Permisos: `RECORD_AUDIO` en manifest; `SpeechRecognizer` exige estar en hilo principal y llamar `destroy()`.
- Privacidad (doc oficial, verificada): la implementación estándar de `SpeechRecognizer` "**puede enviar el audio a servidores remotos** para hacer reconocimiento"; no es apta para reconocimiento continuo (batería/banda). La variante on-device (`createOnDeviceSpeechRecognizer` + `isOnDeviceRecognitionAvailable` + `triggerModelDownload`) evita ese flujo pero **requiere dispositivos y versión de Android adecuados**.
- Formato de audio: mejor grabar directio con `AudioRecord` (PCM 16 kHz mono 16-bit) y guardar como WAV: las librerías offline consumen PCM directamente y **se elimina la conversión m4a→wav** (y además `ffmpeg-kit` está EOL).
- `sherpa-onnx` tiene la mejor oferta ES off-line: modelo pequeño de 48 MB (Moonshine ES, feb 2026) hasta streaming zipformer (119 MB) o Whisper completo; ejemplos oficiales Android y API Kotlin.
- `whisper.cpp`: integración madura (JNI), calidad alta con `base` en español a bajo coste de CPU; no streaming (transcribir al pulsar parar).

### 4.4 Recomendación

- **Arquitectura intercambiable**: interfaz `Transcriber` única → implementaciones `SpeechRecognizerTranscriber` (fallback rápido) y `OfflineONNX` (`sherpa-onnx` con modelo ES descargable a primera hora, 48–120 MB).
- Si hay que elegir UNO para el MVP: **offline (sherpa-onnx)** para vender "gratis + privado + sin Google" O **SpeechRecognizer** si prioridad es tener algo funcionando esta semana. Ambos gratis.
- Cloud (OpenAI/Deepgram) solo como opción futura premium bajo opt-in.

---

## 5. Propuesta de MVP (a validar con el usuario)

### Stack (recomendado)
- **Kotlin + Jetpack Compose**, Room (SQLite con FTS4), DataStore para settings, Min SDK 26.
- Transcripción: abstracción `Transcriber` (ver 4.4).

### Pantallas
1. **Diario** (lista timeline + búsqueda FTS + filtro por palabra key)
2. **Captura** (FAB/widget: grabar → transcribir → texto editable → título/marcas → guardar)
3. **Detalle** (texto, audio playable, keywords de esa entrada, editables)
4. **Estadísticas** (top palabras con chips/nuvel, nº de sueños, racha, gráfica mensual)

### Modelo de datos (Room)
- `dream(id, titulo, texto, audio_path, origen [voz/teclado], esLúcido, mood, claridad 1–5, created_at, dream_at)`
- `keyword(id, token [stem], display, frecuencia_total, n_sueños, last_at)`
- `dream_keyword(dream_id, keyword_id, tf)` + índice FTS4 para búsqueda

### Algoritmo de palabras clave (ES)
1. Normalizar (minúsculas, quitar tildes para matching, conservar display)
2. Tokenizar por letras (regex `\p{L}+`), descartar <3 chars
3. Quitar stopwords españolas (lista curada)
4. Stemming (Snowball/Lucene o stemmer ES simple) para agrupar volar/volando/volé
5. Actualizar cuenta agregada por stem + frecuencia por sueño + fecha
6. UI: chip con recuento; tap → lista filtrada. (TF-IDF / bigramas: fase 2)

---

## 6. Notas de testeo en emulador

- `SpeechRecognizer` puede no estar disponible en el emulador (depende de si tiene servicios de Google) → **las librerías offline (sherpa-onnx/Vosk) sí funcionan en emulador** en CPU, lo que facilita el testeo automatizado.
- El micrófono del emulador puede pasar el audio del equipo host; también existe la entrada por archivo para tests.

---

## 7. Fuentes (2026)

- Article Oneironaut "Best Dream Journal Apps for Lucid Dreaming (2026)" — https://oneironauts.io/blog/best-dream-journal-apps
- Article DreamLens "Best Dream Journal Apps Compared (2026): 7 Apps Tested" — https://dreamlens.ai/blog/best-dream-journal-apps/
- Play Store búsqueda "dream journal" (valoraciones Android, consultadas 2026-09-30)
- Doc oficial SpeechRecognizer — https://developer.android.com/reference/android/speech/SpeechRecognizer
- Vosk modelos (tamaños y WER oficiales) — https://alphacephei.com/vosk/models
- `k2-fsa/sherpa-onnx` (releases `asr-models`, tamaños de assets medidos por API GitHub) — https://github.com/k2-fsa/sherpa-onnx
- `ggerganov/whisper.cpp` (ejemplo oficial Android) — https://github.com/ggerganov/whisper.cpp
- `vilassn/whisper_android` (Whisper TFLite) — https://github.com/vilassn/whisper_android
- Precios cloud (artículos 2026 con precios verificados): OpenAI 0,006/min / 0,003/min mini; Deepgram Nova-3 0,0043/min batch; AssemblyAI 0,0025/min
- Maven Central: `com.alphacephei:vosk-android:0.3.47`