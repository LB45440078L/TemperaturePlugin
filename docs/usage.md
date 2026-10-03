# TemperaturePlugin — usage guide

A physics-based temperature simulation for Spigot Minecraft servers (the same jar runs on Paper).
Players feel heat and cold based
on where they are, what the sky is doing, what they are wearing and what is burning nearby.

The model behind every number is derived in [`physics_model.md`](physics_model.md).

---

## 1. Requirements

| | |
|---|---|
| Server | Spigot **26.2** (the `api-version` the plugin declares). Also runs on Paper, unchanged |
| Java | **25** or newer — this is Spigot 26.2's own minimum, and the plugin is compiled to target it |
| Optional | [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for the placeholders in §7 |

The plugin has no runtime dependencies. Nothing is shaded except the plugin's own simulation core, so
the jar is a few hundred kilobytes and there is nothing to relocate or conflict with.

---

## 2. Installation

1. Drop `TemperaturePlugin-3.0.0.jar` into your server's `plugins/` directory.
2. Start the server once. `plugins/TemperaturePlugin/config.yml` and `messages.yml` are created.
3. Edit `config.yml` (at minimum, the `worlds:` list) and run `/temperature reload`.

If no configured world is loaded at startup the console says so — that is nearly always a typo in the
`worlds:` list.

---

## 3. Commands

All commands live under `/temperature` (aliases `/temp` and `/temperatureplugin`).

| Command | Permission | What it does |
|---|---|---|
| `/temperature` or `/temperature help` | — | List the commands you may use. |
| `/temperature status` | — | Your current reading, the air temperature behind it, the humidity, the wind, your biome and the season. |
| `/temperature toggle` | `temperature.toggle` | Turn your own action bar on or off. Remembered per player. |
| `/temperature unit <unit>` | `temperature.unit` | Choose your display unit. Remembered per player. |
| `/temperature debug` | `temperature.reload` | Explain the contributions behind your reading — the biome baseline, the season, nearby blocks, armour and so on. |
| `/temperature reload` | `temperature.reload` | Re-read `config.yml` and `messages.yml`. Player temperatures are preserved across the reload. |

Units: `CELSIUS`, `FAHRENHEIT`, `KELVIN`, `RANKINE`, `REAUMUR`, `NEWTON`, `DELISLE`, `ROMER`. Tab
completion lists them.

`/temperature debug` output looks like this, and sums to the air temperature the physics used:

```
[Temperature] Contributions to your air temperature (Celsius):
- biome-altitude: +21.48
- sunlight: +4.00
- season: +5.05
- emitters: +3.22
- armor: +2.50
- active-effects: +0.00
- immersion: +0.00
- rain: +0.00
- total: +36.25
```

---

## 4. Permissions

| Node | Default | Meaning |
|---|---|---|
| `temperature.*` | op | Grants everything below. |
| `temperature.use` | **true** | Be simulated at all. Without it a player is never sampled and never sees an effect. |
| `temperature.toggle` | **true** | Toggle one's own display. |
| `temperature.unit` | **true** | Change one's own unit. |
| `temperature.reload` | op | Reload configuration. |
| `temperature.notify` | op | Reserved for threshold alerts. |

`temperature.use` defaults to *true* so the plugin works out of the box; on a network where you only
want temperature in one arena, negate it globally and grant it per-world with your permissions plugin.

---

## 5. Configuration

Every key is documented inline in `config.yml`. The sections, and what they are for:

| Section | Controls |
|---|---|
| `worlds` | Which worlds are simulated. |
| `display` | The action bar: template, bar, colours, season labels, the default unit. |
| `simulation` | The tick cadence, and the body's time constant and bounds. |
| `calendar` | Day length, season length, per-season temperature offsets. |
| `altitude` | Whether height uses the physical linear lapse or the original polynomial. |
| `humidity` | The shape of the diurnal humidity curve. |
| `climate` | The temperature and humidity band for each of the seven climate families. |
| `biome-overrides` | Force a specific biome onto a family. |
| `emitters` | The nearby-block scan: influence radius, how many sources count, scan size. |
| `solar-gain`, `insulation`, `immersion` | Sunlight, leather, water. |
| `weather` | Rain cooling and the wind model. |
| `apparent-temperature` | The heat-index and wind-chill gates. |
| `diurnal-ripple` | The small day/night oscillation. |
| `consumption` | The water-cooling curve. |
| `effects` | The temperatures at which a player catches fire or freezes. |

### Common adjustments

**Only simulate one world.**
```yaml
worlds:
  - 'world'
```

**Make the simulation cheaper.** In `simulation.performance`, use `LOW` (every 20 ticks) instead of
`HIGH` (8). Combine with a smaller `emitters.scan` if your server is very busy.

**Make heat less punishing.** Raise `effects.heat-threshold` (e.g. to `45.0`) or lower the `warm` and
`temperate` bands under `climate`.

**Turn off a feature.** `immersion.enabled: false`, `apparent-temperature.heat-index.enabled: false`,
`diurnal-ripple.enabled: false` — each removes one term without touching the rest.

**Change the cadence without changing the feel.** `simulation.time-constant-seconds` is the physical
relaxation time and is independent of the cadence, so changing `performance` does not change how fast
players adapt (unlike the original — see §11 of the physics document).

### The action bar template

`display.action-bar` supports `{BAR}`, `{TEMP}` and `{SEASON}`. Example:

```yaml
action-bar: '&7Temp &8| {BAR} &8| {TEMP} &8| {SEASON}'
```

`{BAR}` spans a fixed physical range (‑40 to 40 °C) so its fill means the same thing in every biome.

---

## 6. Tagging a consumable

A food or drink item can carry a timed temperature modifier. The plugin reads three keys from the
item's **persistent data container**:

| Key | Type | Meaning |
|---|---|---|
| `temperature_modifier_type` | string | `increase` or `decrease` |
| `temperature_modifier_amount` | double | degrees Celsius |
| `temperature_modifier_time` | long | duration, in seconds |

Tag an item from another plugin:

```java
NamespacedKey type   = new NamespacedKey(plugin, "temperature_modifier_type");
NamespacedKey amount = new NamespacedKey(plugin, "temperature_modifier_amount");
NamespacedKey time   = new NamespacedKey(plugin, "temperature_modifier_time");

ItemStack cocoa = new ItemStack(Material.COOKIE);
ItemMeta meta = cocoa.getItemMeta();
PersistentDataContainer pdc = meta.getPersistentDataContainer();
pdc.set(type,   PersistentDataType.STRING, "increase");
pdc.set(amount, PersistentDataType.DOUBLE, 8.0);
pdc.set(time,   PersistentDataType.LONG,   30L);
cocoa.setItemMeta(meta);
```

Eating it raises the player's air temperature by 8 °C for thirty seconds.

> **Migrating from 1.x.** The original read raw NBT tags through a bundled NBT library. The server's
> persistent data container is the supported equivalent and needs no shaded dependency, but items
> tagged for the old plugin must be re-tagged with the keys above. A plain drink of water needs no
> tagging at all — the plugin recognises `minecraft:potion` with a `WATER` base on its own.

---

## 7. Placeholders

Available when PlaceholderAPI is installed:

| Placeholder | Value |
|---|---|
| `%temperature_temperature%` (or `%temperature_temp%`) | The player's body temperature, in their unit. |
| `%temperature_celsius%` | In degrees Celsius. |
| `%temperature_fahrenheit%` | In degrees Fahrenheit. |
| `%temperature_kelvin%` | In kelvin. |
| `%temperature_unit%` / `%temperature_symbol%` | The player's unit name / symbol. |
| `%temperature_humidity%` | Relative humidity, percent. |
| `%temperature_season%` | The season name. |
| `%temperature_air%` | The air temperature. |
| `%temperature_apparent%` | The apparent temperature. |
| `%temperature_wind%` | Estimated wind, km/h. |

Placeholders are answered from the last simulation step, so a scoreboard shows exactly the values the
physics produced. An unsimulated player (or one without `temperature.use`) yields an empty string.

---

## 8. Developer API

Two events are fired, both cancellable.

### `PlayerTemperatureUpdateEvent`

Fired after each simulation step.

```java
@EventHandler
public void onTemperature(PlayerTemperatureUpdateEvent event) {
    double celsius = event.bodyCelsius();       // canonical
    double shown   = event.bodyInUnit();         // the player's unit
    AmbientState air = event.ambient();          // air, apparent, humidity, wind, season

    if (playerIsInASauna(event.getPlayer())) {
        event.setBodyCelsius(celsius + 5.0);     // taking the colder accessor works too
    }
}
```

Setting a value changes what is stored for the step. Cancelling the event leaves the previous
temperature in place, and the threshold effects will not fire for that step.

### `WorldSeasonChangeEvent`

Fired when a simulated world's season turns.

```java
@EventHandler
public void onSeason(WorldSeasonChangeEvent event) {
    if (event.upcomingSeason() == Season.WINTER) {
        announceWinter(event.getWorld());
    }
    // Or redirect it entirely:
    // event.setUpcomingSeason(Season.SPRING);
}
```

Obtaining the services:

```java
TemperaturePlugin plugin = (TemperaturePlugin) Bukkit.getPluginManager().getPlugin("TemperaturePlugin");
TemperatureEngine engine = plugin.service().engine();   // body temperatures
```

---

## 9. Troubleshooting

**Nobody feels anything.** Check the console for "No simulated worlds are loaded" and confirm your
`worlds:` names match the server. Confirm players hold `temperature.use` (it defaults to true, but a
permissions plugin can negate it).

**The action bar does not appear.** `display.enabled` must be true, and the player must not have run
`/temperature toggle`. Check `simulation.action-bar-interval-ticks` is sensible.

**The reading is stuck.** The body starts at `simulation.initial-body-temperature` (36 °C) and drifts;
give it a `time-constant-seconds` (36 s by default) to settle. A player with no reading yet shows
"no reading yet" in `/temperature status`.

**Everyone catches fire in summer.** That is genuinely hot, humid summer weather by the model's
reckoning. Raise `effects.heat-threshold`, lower the `warm`/`temperate` bands, or reduce
`weather.wind.exposure-bonus` (wind does not cause it, but reducing humidity via `climate.*.humidity`
does). See §10.1 and §14 of the physics document for the exact numbers.

**A config edit did nothing.** Run `/temperature reload`. If a key still seems inert, check the console
— an unknown value logs a warning and falls back to the default rather than failing silently.

**I edited `messages.yml` and it did not change.** `/temperature reload` reloads it too. The service
re-reads the file on every send, so there is no cache to clear.

---

## 10. Building from source

```bash
# Requires JDK 25+ and Maven 3.9+
mvn clean package
```

The build produces `temperature-plugin/target/TemperaturePlugin-3.0.0.jar`. Run the tests with
`mvn test`; the suite is 123 tests across the two modules and needs no server.
