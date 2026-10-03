# TemperaturePlugin

### Real heat. Real cold. Real physics.

**Stop pretending a biome name is a temperature.**

Most temperature plugins do the same thing: read the biome, pick a number, start a timer. This one
models the world around the player — the air, the height, the season, the sun, the rain, the lava three
blocks away, and what they are wearing.

---

## What makes it different

**1. A real model, documented end to end.**
Heat index and wind chill are published regressions, used only inside the range they were actually
fitted for. The full derivation — every constant, every correction — ships with the plugin. When a
player asks "why is it so hot here?", you can answer, because you can read the maths.

**2. Heat and cold come from the world, not from a list of biome names.**
Lava, fire, lit furnaces and campfires warm the air around a player. Ice and snow cool it. The effect
falls off with distance and reaches exactly zero at the edge of its reach, so standing next to a source
matters and standing ten blocks away does not. A furnace only counts while it is burning; a campfire
only while it is lit.

**3. The world around the player keeps changing.**
Climb a mountain and it gets colder. Summer and winter blend into each other instead of snapping over
on the first day of a new season. Humidity rises and falls through the day. Rain scrubs heat out of hot
air, and wind makes cold bite harder.

**4. The body adapts — at a physical rate.**
A player drifts toward what the air feels like rather than snapping to it, over one configurable time
constant. That rate is a physical setting, so changing the plugin's tick cadence can never quietly
change how fast your players adapt.

**5. It explains itself.**
`/temperature debug` prints every contribution to a player's reading: the biome baseline, the season,
each nearby heat source, their armour, the weather. No more guessing whether a config change did
anything.

---

## Features

- **Seven climate families** cover every vanilla biome, with a fallback classifier for biome packs, and
  per-biome overrides by name.
- **Heat and cold have consequences.** Cross the line and a player catches fire or starts freezing.
  Fire protection and a full set of leather armour protect them — as they should.
- **Insulation, water and drinking.** Leather insulates. Standing in water cools. A drink of water cools
  along a curve that peaks a few minutes later and fades out.
- **Per-player display.** Eight temperature units, and a toggle each player controls. Remembered per
  player, stored on the player — no database.
- **A fully documented config** with a validated `config-version`, and a reload that preserves every
  player's temperature instead of resetting the server.
- **No dependencies.** The jar bundles nothing but its own simulation core.
- **PlaceholderAPI support**, plus a developer event API with two cancellable events.
- **Spigot 26.2, Java 25** — and the same jar runs on Paper.

## The interface

A clean action bar, entirely yours to shape:

`action-bar: '&7[{BAR}&7] {TEMP} &7{SEASON}'`

`{BAR}` fills against a fixed physical range, so its fill means the same thing in every biome.
`{TEMP}` is coloured by how extreme the reading is. `{SEASON}` is the current season.

`/temperature status` gives the full picture: what the player feels, the air temperature behind it, the
humidity, the wind, the biome and the season.

## Commands

| Command | What it does |
|---|---|
| `/temperature help` | List the commands you can use. |
| `/temperature status` | Your reading and the environment behind it. |
| `/temperature toggle` | Turn your own display on or off. |
| `/temperature unit <unit>` | Choose your unit. |
| `/temperature debug` | Explain every contribution to your reading. |
| `/temperature reload` | Reload the configuration. |

Aliases: `/temp` and `/temperatureplugin`.

## Permissions

| Node | Default | Meaning |
|---|---|---|
| `temperature.*` | op | Everything below. |
| `temperature.use` | **true** | Be simulated at all. |
| `temperature.toggle` | **true** | Toggle your own display. |
| `temperature.unit` | **true** | Change your own unit. |
| `temperature.reload` | op | Reload the configuration. |
| `temperature.notify` | op | Threshold alerts. |

`temperature.use` defaults to true, so it works out of the box — and can be granted per world if you
only want temperature in one arena.

## Requirements

- **Spigot 26.2** (`api-version` 26.2) with **Java 25** — it also runs on Paper, which ships the same API.
- PlaceholderAPI is optional.
- **Folia is not supported.**
- Ships with a 123-test automated suite, so behaviour is pinned rather than hoped for.

**On performance:** no benchmark is published, because none has been measured on your hardware.
Performance profiles let you trade fidelity for cost, and only players with the permission are ever
simulated.
