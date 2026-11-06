# Bubble · El personaje

Bubble es la mascota virtual de BubbleAgent y, a la vez, la voz de su agente de chat. Es una **burbuja de jabón viva** que se nutre del tiempo que pasas fuera de la pantalla.

> **Principio central:** Bubble vive mejor cuando no la miras. No mejora porque la toques; mejora cuando sales al mundo real.

## Archivos

| Archivo | Contenido |
|---|---|
| [`bubble.svg`](bubble.svg) | Bubble maestra en reposo (R = 100), fondo transparente |
| [`bubble-24.svg`](bubble-24.svg) | Versión simplificada para tamaños menores de 32 px |
| [`model-sheet.svg`](model-sheet.svg) | Hoja de modelo: construcción, paleta, vocabulario de ojos y escalas |
| [`states.json`](states.json) | Parámetros de los 6 estados y la coreografía de *pensando* |
| [`reactions.json`](reactions.json) | Tocar, aparecer, celebrar y despedirse, con tiempos y sonidos |
| [`voice-guide.md`](voice-guide.md) | Tono, frases y paleta de sonidos |
| [`prototype.html`](prototype.html) | Prototipo interactivo con todos los estados y reacciones (ábrelo en el navegador) |

## Decisiones de diseño

- **Rol:** Bubble es el mismo personaje que la voz del agente de chat.
- **Tono:** cálido intermedio, tierno sin ser infantil.
- **Comunicación:** sonidos no verbales (plops y burbujeos), sin voz hablada.
- **Siempre en modo oscuro.** Bubble OS no tiene modo claro; Bubble no se diseña para fondo claro.
- **Nunca culpa.** No muere, no revienta, no se pone triste por tu uso del teléfono.

## Anatomía

- **Cuerpo transparente.** La forma la define el borde, no el relleno.
- **Contorno rígido.** Siempre un círculo perfecto, porque una burbuja real no se dobla. Única excepción: al tocarla, el cuerpo completo oscila un 3 % durante unos 0.6 s.
- **Película iridiscente.** El borde y dos bandas interiores giran; su velocidad expresa la energía de Bubble.
- **Luz fija en el mundo.** El brillo en media luna (arriba a la izquierda) y el punto de luz no rotan con el cuerpo.
- **Reflejo interior.** Un horizonte líquido que se mece con su propio ritmo.
- **Ojos de luz.** Grandes, centrados (esquema infantil) y sin pupila. Expresan emoción cambiando de forma, como si tuvieran párpados invisibles. Inspirados en el *principio* de los ojos de EVE (WALL·E), pero propios: óvalos verticales, blancos y flotando dentro de una burbuja transparente.
- **Burbujitas.** Suben a su alrededor y dan vida a los sonidos y las celebraciones.

## Estados

| Estado | Disparador | Expresión clave |
|---|---|---|
| Flotando | Uso equilibrado | Película lenta, ojos neutros |
| Empañada | Racha larga de uso continuo | Película casi detenida, niebla interior, ojos recortados por arriba |
| Iridiscente | Actividad offline completada | Colores rápidos, ojos en media luna, muchas burbujitas |
| Curiosa | Aprende un interés nuevo | Se acerca, ojos 1.3× y cada parpadeo es doble |
| Dormida | Noche o Focus mode | Se encoge y desciende, ojos cerrados en arco |
| Pensando | Genera una respuesta en el chat | Coreografía: entrecierra, parpadea, mira a cada lado con un ojo abierto |

## Reglas de uso

**Siempre:**
- Fondo oscuro (`#121216`)
- Contorno circular perfecto
- Luz arriba a la izquierda
- Ojos de luz

**Nunca:**
- Doblar o deformar el contorno (salvo la oscilación del 3 % al tocar)
- Ponerle boca, extremidades u otros accesorios
- Rellenarla con un color opaco
- Rotar los brillos con el cuerpo
- Recortarla (Bubble siempre aparece completa)
- Mostrarla sobre fondo claro
- Usar la versión completa por debajo de 32 px
- Hacerla reventar

## Notas de implementación

- **Compose:** `Canvas` con círculos y `Path` para ojos y reflejo; cada parámetro de `states.json` se anima con `animateFloatAsState` (o un lerp exponencial con tasa 2.4/s). La rotación de la película es `rotate()` sobre el `Brush` del borde.
- **Datos:** `ScreenTimeTracker` y las actividades completadas eligen el estado destino; el chat activa *pensando* y *curiosa*.
- **Widget (Glance):** no admite Canvas animado; usar fotogramas estáticos por estado y `bubble-24.svg` para tamaños pequeños.
- **Rive (opcional):** una máquina de estados con entradas numéricas equivalentes a los parámetros de `states.json`.
