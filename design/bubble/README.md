# Bubble · The Character

Bubble is the virtual companion of BubbleAgent and the voice of its chat assistant. It is a **living soap bubble** nurtured by the time you spend offline in the real world.

> **Core Principle:** Bubble thrives when you are not looking at it. It doesn't grow because you interact with it; it blossoms when you live in the physical world.

## Files

| File | Content |
|---|---|
| [`bubble.svg`](bubble.svg) | Master resting Bubble (R = 100), transparent background |
| [`bubble-24.svg`](bubble-24.svg) | Simplified version for view sizes below 32 px |
| [`model-sheet.svg`](model-sheet.svg) | Model sheet: construction, color palette, eye expressions, and scales |
| [`states.json`](states.json) | Parameters for the 6 emotional states and the *thinking* choreography |
| [`reactions.json`](reactions.json) | Touch, appear, celebrate, and dismiss reactions, with timings and sound cues |
| [`voice-guide.md`](voice-guide.md) | Tone, phrases, and sound palette |
| [`prototype.html`](prototype.html) | Interactive HTML prototype showcasing all states and reactions |

## Design Decisions

- **Role:** Bubble is both the virtual companion and the personality of the local AI assistant.
- **Tone:** Warm, gentle, and tender without being childish.
- **Communication:** Non-verbal sounds (plops, water chimes, and bubbles) without spoken synthetic voices.
- **Always Dark Mode:** Bubble OS does not have a light mode; Bubble is never displayed on light surfaces.
- **Guilt-Free:** It never bursts, dies, or expresses guilt over the user's phone usage.

## Anatomy

- **Transparent Body:** The spherical shape is defined by its refractive outer edge, not an opaque fill.
- **Rigid Contour:** Always a perfect circle. A real soap bubble does not bend. The only exception: when touched, the entire sphere oscillates by 3% for ~0.6s.
- **Iridescent Film:** The rim and two interior bands rotate; their rotational speed conveys Bubble's energy level.
- **Fixed World Lighting:** The crescent highlight (top left) and pinpoint light reflection remain stationary and do not rotate with the body.
- **Inner Reflection:** A liquid horizon that sways gently with its own independent rhythm.
- **Eyes of Light:** Large, centered, pupil-less light ovals. They convey emotion by reshaping, as if governed by invisible eyelids. Inspired by the eye philosophy of EVE (WALL·E), but unique: vertical white ovals floating within a transparent sphere.
- **Micro-bubbles:** Rise gently around it during sound emissions and milestone celebrations.

## States

| State | Trigger | Key Expression |
|---|---|---|
| Floating | Balanced phone usage | Gentle rotation, neutral expressive eyes |
| Foggy | Extended continuous screen time session | Film nearly halted, internal mist, upper eye clipping |
| Iridescent | Offline activity completed | Fast vibrant colors, crescent eyes, abundant micro-bubbles |
| Curious | New interest or goal saved to vault | Leans forward, 1.3× eye scale, double blinks |
| Asleep | Night time or Focus mode active | Gently scales down and descends, closed arc eyes |
| Thinking | Generating on-device reply in chat | Choreography: squints, blinks, scans left and right |

## Usage Rules

**Always:**
- Dark background (`#121216`)
- Perfect circular contour
- Highlight placed top-left
- Luminous expressive eyes

**Never:**
- Distort or bend the outer ring (except the 3% touch oscillation)
- Add mouths, limbs, or external accessories
- Fill with an opaque flat color
- Rotate highlights with the body
- Crop Bubble's edges (Bubble is always shown whole)
- Display over light backgrounds
- Use the detailed full SVG below 32 px
- Make Bubble pop or burst

## Implementation Guidelines

- **Compose:** Implemented via `Canvas` with circles and `Path` for eyes and liquid reflection; parameters from `states.json` animated using `animateFloatAsState` (or exponential lerp at 2.4/s). Film rotation via `rotate()` on border `Brush`.
- **Data Integration:** `ScreenTimeTracker` and completed goals select the target state; chat activity drives *thinking* and *curious*.
- **Widget (Glance):** Does not support real-time animated Canvas; use static state renders and `bubble-24.svg` for small widget tiles.
