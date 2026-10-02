# The TemperaturePlugin physics model

This document derives every number the simulation produces. It is written for an operator who wants
to tune the plugin, and for a maintainer who wants to change it without breaking it. Read it before
editing `config.yml`: several of the constants are fitted to published empirical regressions and are
not free parameters, and changing them will make the model report temperatures that no real
thermometer would.

Everything below is in the platform-free `temperature-core` module. No part of the model touches
Bukkit, so every claim here is exercised by a plain JUnit test in `temperature-core/src/test`.

---

## 1. Design in one paragraph

A reading is produced by three stages. First an **air temperature** is assembled: a biome's climate
band, adjusted for height, then nudged by the season, nearby hot and cold blocks, what the player is
wearing and any temporary effect they are under, and finally scaled by the weather. Second that air
temperature is converted into an **apparent temperature** — what it feels like — using the heat index
when it is hot and humid and the wind chill when it is cold and windy. Third the player's own **body
temperature** drifts towards that apparent temperature over time, because a person does not snap to
the air around them. The value the display shows is the third one.

```
 biome band ──▶ altitude ──▶ +season ──▶ +emitters ──▶ +armour ──▶ +effects ──▶ ×rain
                                                                                    │
                                                                       airborne ("air temperature")
                                                                                    │
                                          heat index / wind chill  ◀────────────────┘
                                                                                    │
                                                                       apparent temperature
                                                                                    │
                                          first-order relaxation   ◀────────────────┘
                                                                                    │
                                                                      body temperature  ──▶ display
```

The pipeline is a set of `TemperatureModifier` strategies composed by `TemperaturePipeline`. Additive
terms are summed first and multiplicative factors applied afterwards, so the result does not depend on
the order in which modifiers were registered — a property asserted by an order-independence test.

---

## 2. Units and notation

* Canonical scale: **degrees Celsius**. The simulation runs in Celsius and converts only when a value
  is rendered to a player.
* A Minecraft block is one metre, so a height in blocks is a height in metres.
* Relative humidity is a **percentage**, 0–100.
* `y` is a block height; `minY`/`maxY` are the world's build limits (‑64 and 320 in the overworld).

---

## 3. Climate bands

Every biome is mapped to one of seven **climate families** (`ClimateArchetype`), and each family
carries a temperature band and a humidity band. Mapping is a table plus a keyword fallback for biomes
the plugin has never seen (datapacks, future versions); the table wins, the fallback catches the rest.
See `BiomeClimateRegistry`.

| Family | Air temperature (°C) | Relative humidity (%) |
|---|---|---|
| temperate | 5 – 28 | 40 – 65 |
| cold | ‑35 – 5 | 30 – 60 |
| warm | 20 – 38 | 15 – 45 |
| ocean | 2 – 18 | 60 – 85 |
| cold-ocean | ‑2 – 8 | 45 – 70 |
| nether | 35 – 60 | 2 – 15 |
| end | ‑10 – 15 | 5 – 20 |

> **Why these numbers, and not the original's?** The original bands (temperate 10–32, warm 25–41.5,
> cold ‑40–7.5, …) were tuned against a pipeline where the humidity term was divided by ten before
> being fed to a heat-index formula that expects a percentage. That bug muted the humidity term, so
> the air temperatures could be set far hotter than reality without the felt temperature running away.
> Correcting the humidity bug without re-basing the bands made a temperate summer afternoon report
> "feels like 70 °C". The bands above keep the same 22‑degree span and the same ordering as the
> originals but sit at physically plausible air temperatures; the net effect is a seasonal noon in the
> temperate family that feels hot (about 35 °C) rather than lethal, while a desert noon or the Nether
> still exceed the heat threshold.

A band is not a single temperature — the vertical profile and the humidity curve move a reading within
it. The temperature band is interpolated as

```
air(y) = min + retention(y) · (max − min)
```

so `retention = 1` gives the warm extreme and `retention = 0` the cool extreme.

---

## 4. The vertical profile

### 4.1 Linear lapse (default)

Temperature falls with height. The real tropospheric lapse rate is about **6.5 °C per kilometre**;
over Minecraft's 384‑block vertical range that is only ~2.5 °C, which no player would notice. The
model therefore uses a **linear** curve with a larger, gameplay-scaled coefficient:

```
h          = (y − minY) / (maxY − minY)          in [0, 1]
retention  = 1 − L·h                              clamped to [0, 1]
air(y)     = min + retention·(max − min)
```

with `L` (`altitude.lapse-fraction`) default **0.85**. The shape is physically faithful — monotone,
linear, colder with height — but the gradient is chosen so a climb is perceptible in a game whose
"atmosphere" is 384 blocks tall rather than 12 kilometres. At y = 64 in the overworld
(`minY = ‑64`, `maxY = 320`) the normalized height is 0.333, giving `retention = 0.717`.

### 4.2 Legacy polynomial

For servers that want the original's feel exactly, `altitude.mode: LEGACY_POLYNOMIAL` evaluates the
quadratic the original precomputed into a 79 864‑entry array:

```
retention(y) = a·y² + b·y + c
a = 3.981796231733e‑6,  b = −0.00322799,  c = 0.695913
```

It is monotone-decreasing over the fitted domain and reproduces the original's values — 0.9188 at
y = ‑64, 0.6959 at y = 0, 0.0706 at y = 320. Unlike the original, the result is **clamped to [0, 1]**:
the raw polynomial leaves that range outside the fitted domain (it is not a physically bounded
quantity), and the original had no guard.

---

## 5. Humidity

Humidity is a **Gaussian bell in the time of day**, peaking in the early afternoon and decaying to a
floor overnight:

```
scale(t)  = A · exp( −(t − μ)² / (2σ²) ) + f
humidity  = hmin + scale(t) · (hmax − hmin)
```

| Constant | Value | Note |
|---|---|---|
| `μ` peak | 6500 / 24000 ≈ 0.27083 | The original's peak tick (6500) as a fraction of a day |
| `σ` width | 4700 / 24000 ≈ 0.19583 | The original's 4700-tick standard deviation |
| `A` amplitude | 0.9844 | Peak height as a fraction of the band |
| `f` floor | 0.015 | Overnight minimum |

`scale` reaches ≈ 1.0 at the peak and ≈ 0.03 overnight. Rain adds `precipitation-bonus` percentage
points, and a small jitter term reproduces the original's noisy character without making the model
untestable.

> **Correction.** The original divided the resolved humidity by ten before passing it to the heat
> index: a swamp's ~85 % humidity entered the regression as 8.5 %. The regression's humidity terms are
> then nearly inert, which is why the original's felt temperature was insensitive to humidity. This
> build feeds the true percentage. The coefficients `A`, `f`, `μ`, `σ` and the day length reproduce
> the original's *shape*; only the scale factor of ten is gone.

---

## 6. The calendar

One in-game day is `ticks-per-day` ticks (24 000), one season is `days-per-season` days (31), and the
four seasons repeat. The season is therefore a pure function of game time:

```
day          = gameTime / ticksPerDay
seasonIndex  = floor(day / daysPerSeason) mod 4
order        = SUMMER → AUTUMN → WINTER → SPRING
```

Because it is derived rather than stored, a server that was offline for a week resumes in the correct
season immediately, and a world whose clock an admin moved is followed rather than fought.

Each season contributes a temperature offset — SUMMER +5.05, AUTUMN ‑5.5, WINTER ‑12.5, SPRING +1.0 —
but the offset is **blended** across the season rather than snapping on its first day:

```
offset(t) = lerp(offset(season), offset(season.next()), progressWithinSeason(t))
```

so the curve is continuous at every season boundary (asserted by a test).

---

## 7. The thermal field of nearby blocks

Blocks heat or cool the air around a player. Each emitter contributes a logarithmic potential that
decays with distance and reaches **exactly zero** at an influence radius `R`:

```
ΔT(d) = sign · max( 0, ln( R² / (d² + ε) ) )
```

where `d²` is the squared distance from the player to the block's centre, `R` is the influence radius
(`emitters.influence-radius`, default **5 blocks**) and `ε` is a per-material **quench** constant that
keeps the potential finite as the player stands on the block.

> **Where this comes from.** The original computed `K − ln(d² + ε)` with `K = 3.218905`. Since
> `e^3.218905 = 25 = 5²`, that expression is identical to `ln(25 / (d² + ε))` — a logarithmic point
> potential offset so it vanishes at five blocks. Writing it as `ln(R² / (d² + ε))` makes the one
> number that governs the range, `R`, explicit instead of hidden inside an exponent. Two fixes ride
> along: contributions are **clamped at zero** (so a source just outside `R` can never *cool* the air,
> which the original's scan box occasionally did), and when more than `max-applied` emitters are in
> range the **nearest** are kept, rather than whichever the loops happened to reach first.

Per-material quench constants (unchanged from the original):

| Emitter | sign | ε (quench) | ΔT adjacent (d² = 1) | ΔT at d² = 16 |
|---|---|---|---|---|
| lava / magma | + | 1.0e‑5 | +3.22 °C | +0.45 °C |
| furnace | + | 1.0e‑3 | +3.22 °C | +0.45 °C |
| fire | + | 0.02 | +3.22 °C | +0.45 °C |
| campfire | + | 0.17 | +3.06 °C | +0.45 °C |
| ice | − | 1.5e‑4 | −3.22 °C | −0.45 °C |
| snow | − | 0.15 | −3.08 °C | −0.45 °C |

A furnace counts only while it is burning and a campfire only while it is lit; the original checked the
furnace but treated every campfire as hot.

The scan box is `(2h+1) × (2v+1) × (2h+1)` blocks with `h` = 3 and `v` = 1 by default — 147 blocks per
player per reading. At the default cadence (every 8 ticks) and 100 players that is roughly 1 800 block
lookups per tick; `performance: LOW` divides it by two and a half.

---

## 8. Insulation, immersion and sunlight

* **Leather armour** adds `leather-per-piece` (2.5 °C) per piece, so a full suit adds 10 °C. Only
  leather insulates. Whether a player has *enough* leather to be immune to the cold penalty is a
  threshold decision (section 12), not a term here.
* **Immersion** subtracts `immersion.cooling` (4 °C) while the player's body is in water — a new term;
  the original let a player stand in an ocean at no thermal cost.
* **Solar gain** adds `solar-gain` (4 °C) when the sample can see the sky.

> **Correction.** The original intended the solar bonus but tested
> `highestBlock.getX() <= block.getX() && highestBlock.getZ() <= block.getZ()`, where the "highest
> block" is always at the very `x`/`z` being sampled. The comparison was vacuously true, so the bonus
> applied to everyone, indoors or buried. Here the adapter answers the real question — is there solid
> ground above? — and the modifier honours it. Because the original effectively always applied the
> bonus, it was folded into the tuned bands; this build reduces `solar-gain` from 8 to 4 to keep the
> outdoors bonus perceptible without double-counting.

---

## 9. Weather and wind

Rain does not remove a fixed number of degrees; it removes a *fraction* of the heat present, so it is
the pipeline's only multiplicative term:

```
air ← air × rain-cooling-factor      when precipitating and air > rain-cooling-threshold
```

with factor **0.725** and threshold **27.5 °C**, the original's values. Precipitation only counts if
the player can see the sky — sheltering under a roof in a storm keeps you dry.

Wind exists to make wind chill meaningful. With no weather simulation to draw on it is assembled from
what can be observed:

```
wind (km/h) = base(5) + storm(25 if precipitating) + exposure(10 if exposedToSky)
```

---

## 10. Apparent temperature

The air temperature is not what a player feels. Two published regressions convert it, each **gated to
the range it was fitted for**, with the dry-bulb temperature as the fallback.

### 10.1 Heat index (hot and humid)

The metric Rothfusz regression, used when the air is at or above 26.7 °C **and** humidity is at least
40 % **and** the air is at or below 46 °C:

```
HI = −8.78469475556
   + 1.61139411·T
   + 2.33854883889·R
   − 0.14611605·T·R
   − 0.012308094·T²
   − 0.0164248277778·R²
   + 0.002211732·T²·R
   + 0.00072546·T·R²
   − 0.000003582·T²·R²
```

`T` is air temperature in °C, `R` relative humidity in percent. The nine coefficients are the standard
published metric set, unchanged. For reference, `HI(35, 60) = 45.05 °C`.

> **Correction — the domain guard.** The original evaluated this polynomial *everywhere*, including
> for freezing air, where it is meaningless: at 10 °C and 80 % it returns **33 °C**, and at 49 °C it
> returns **≈ 90 °C**. The upper bound matters most: the regression is fitted up to roughly 46 °C, and
> a plausible desert reading of 49 °C otherwise produced a felt temperature near 90. Outside
> [26.7, 46] °C or below 40 % humidity the dry-bulb temperature is used, which is the honest answer.

### 10.2 Wind chill (cold and windy)

The Environment Canada metric formula, used when the air is at or below 10 °C and wind is at least
4.8 km/h, taking the colder of it and the dry-bulb temperature:

```
Twc = 13.12 + 0.6215·T − 11.37·v^0.16 + 0.3965·T·v^0.16
```

`v` is wind speed in km/h. For reference, `Twc(0, 20) = −5.24 °C` and `Twc(−20, 40) = −34.1 °C`.

### 10.3 A small diurnal ripple

A minor oscillation rides on the apparent temperature, proportional to the biome's temperature span and
to the same curve that drives humidity:

```
ripple = span · diurnal-ripple-scale · scale(t),     diurnal-ripple-scale = 1/(2π)
```

This preserves the original's term (`span · scale / 2π`) while exposing the constant. It is off by
setting `diurnal-ripple.enabled: false`.

---

## 11. Thermoregulation

A player drifts towards the apparent temperature rather than snapping to it. The body is a first-order
system relaxing exponentially with time constant τ:

```
dT/dt  = (T_apparent − T) / τ
T(t+Δt) = T_apparent + (T − T_apparent)·e^(−Δt/τ)
```

The **closed-form step** is used rather than an incremental one because it is unconditionally stable
and its result is independent of how time is chopped — a test asserts that thirty 2‑second steps and
sixty 1‑second steps agree to nine decimal places. τ defaults to **36 seconds**; after one τ the body
has closed about 63 % of the gap, and after 3τ it is within 5 %.

> **Where 36 comes from, and a fix.** The original used `T ← T + 0.011·(T_apparent − T)` per update.
> That is a first-order Euler step of this same ODE with `α = 0.011`, so it matches the closed form when
> `e^(−Δt/τ) ≈ 1 − α`, i.e. `τ = −Δt / ln(1 − α)`. With the default HIGH cadence (Δt = 0.4 s) that gives
> τ = 36.2 s — hence the default. But the original's `α` was fixed while Δt changed with the
> performance profile, so its effective τ was a side effect of a *performance* setting:
>
> | profile | Δt | legacy effective τ |
> |---|---|---|
> | LOW | 1.00 s | 90.4 s |
> | MEDIUM | 0.50 s | 45.2 s |
> | HIGH | 0.40 s | 36.2 s |
> | EXTREME | 0.05 s | 4.5 s |
>
> A player on `EXTREME` adapted twenty times faster than one on `LOW`. Here τ is a physical parameter in
> its own right (`simulation.time-constant-seconds`) and the cadence no longer changes it. The body is
> clamped to `[min-body-temperature, max-body-temperature]` (‑40 to 60 °C).

---

## 12. Consumption

A drink of water cools along an inverted parabola in the minutes since it was drunk:

```
cooling(t) = max(0, peak − (factor·t − offset)²)
```

with `factor = 0.6`, `offset = 4.5`, `peak = 20`. Cooling is greatest at `t = offset/factor = 7.5`
minutes and reaches zero at `t = (offset + √peak)/factor = 14.95` minutes.

> **Correction.** The raw parabola is worth **−0.25** at `t = 0`, i.e. it slightly *warms* a player who
> has just drunk. The result is floored at zero.

A consumable can also carry a timed modifier in its persistent data container, in which case it adds a
constant amount of heat or cold for a fixed duration (section 4 of `usage.md`).

---

## 13. Threshold consequences

Once the body temperature crosses a threshold, the adapter applies an effect:

| Condition | Consequence | Immunity |
|---|---|---|
| body ≥ `heat-threshold` (40.5 °C) | set on fire (20 ticks), at most once per second | ≥ 3 fire-protection pieces, or fire resistance |
| body ≤ `cold-threshold` (‑0.5 °C) | accumulate freeze ticks from 25 upwards | ≥ 3 leather pieces |
| between | both wound back to zero | — |

> **Correction.** The original left a player's freeze ticks in place once they warmed up, so the screen
> frost never cleared, and it stored the freeze level without its increment. Both are fixed: warming up
> resets freeze ticks to zero.

---

## 14. A worked example

Temperate plains, y = 64, summer, noon, open sky, no armour, no nearby blocks.

| Step | Computation | Result |
|---|---|---|
| retention | `1 − 0.85·(64−(−64))/384` | 0.7167 |
| biome baseline | `5 + 0.7167·(28−5)` | 21.48 °C |
| + solar (exposed) | `+ 4.0` | 25.48 °C |
| + season (summer) | `+ 5.05` | 30.53 °C |
| humidity | `40 + 0.9938·(65−40)` | 64.8 % |
| wind | `5 + 0 + 10` | 15 km/h |
| apparent | heat index(30.53, 64.8) since 26.7 ≤ T ≤ 46 and R ≥ 40 | 35.03 °C |
| + ripple | `23·(1/2π)·0.9938` | +3.64 °C |
| **apparent temperature** | | **≈ 38.7 °C** |
| body (converged) | | ≈ 38.7 °C |

At default settings that is a hot summer afternoon that sits just under the 40.5 °C heat threshold — a
player who bundles up in leather, or catches the humidity at its peak, will tip over it, which is the
intended reading: dressing warm in the heat is a bad idea. The same spot in the desert (warm family,
band 20–38) gives an air temperature of 41.95 °C and, at 44.8 % humidity, an apparent temperature of
**57.2 °C** — well past the threshold. A cold-family taiga at night in winter gives an air temperature
of ‑14.8 °C and, with 15 km/h wind, a wind chill of **‑22.7 °C**.

---

## 15. Summary of corrections against the original

| # | Issue in the original | Resolution |
|---|---|---|
| 1 | `System.exit(0)` executed on every temperature computation after 1 March 2025 — a time bomb that terminated any server running it | removed entirely |
| 2 | Heat index evaluated outside its fitted domain, producing negative "felt" temperatures when cold and ≈ 90 °C when hot | gated to [26.7, 46] °C and ≥ 40 % humidity, with a dry-bulb fallback |
| 3 | Humidity divided by ten before entering a percentage-expecting regression | true percentage fed; bands re-based |
| 4 | Solar bonus applied to everyone (vacuous sky test) | real sky-visibility test; gain re-based to 4 °C |
| 5 | Newtown and Rømer unit inverses were wrong (Newton's was the Fahrenheit inverse) | corrected, with round-trip tests for all eight scales |
| 6 | Biome classification was a `contains` chain with an `&&`/`||` precedence bug that made the cold branch unreachable | explicit table plus a tested keyword fallback |
| 7 | Emitter contributions could go negative beyond the influence radius, cooling the air | clamped at zero |
| 8 | Emitter cap counted in scan order, so which blocks counted depended on loop breaks | nearest-N |
| 9 | Interpolation performed in the player's display unit, so two players converged at different physical rates | interpolation in Celsius, converted only at display |
| 10 | Effective time constant depended on the performance profile | τ is a physical parameter; cadence-independent |
| 11 | Freeze ticks never cleared on warming; stored level omitted its increment | cleared on warming; increment stored |
| 12 | Every campfire counted as hot when unlit; only furnaces were checked for burning | furnace burn time and campfire lit state both checked |
| 13 | `TemperatureDisplay.stopTask()` cancelled a task that was never assigned, leaking two tasks per reload | tasks tracked and cancelled |
| 14 | Reload replaced the config object that existing managers had captured, so edits reached some paths and not others | one immutable configuration swapped atomically; the message service reloads in place |
| 15 | `Performance`/unit/season lookups threw on unknown values | all fall back to defaults with a logged warning |
| 16 | A bundled NBT library was shaded and relocated to read item tags | Paper's persistent data container; no shaded dependency |

---

## 16. References

1. R. G. Steadman, "A universal scale of apparent temperature", *J. Appl. Meteorol.* **23** (1984).
2. G. B. Rothfusz, "The heat index equation (or, more than you ever wanted to know about heat index)",
   NWS Southern Region Technical Attachment SR/SSD 90‑23 (1990). — the regression in §10.1.
3. Oszczevski & Bluestein, "The new wind chill equivalent temperature chart",
   *Bull. Amer. Meteor. Soc.* **86** (2005). — the formula in §10.2.
4. Environment and Climate Change Canada, wind chill index.
5. World Meteorological Organization, standard lapse rate (6.5 °C km⁻¹).
