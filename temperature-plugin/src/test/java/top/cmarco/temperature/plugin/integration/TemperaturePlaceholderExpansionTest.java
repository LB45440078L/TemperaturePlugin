package top.cmarco.temperature.plugin.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.cmarco.temperature.core.climate.Season;
import top.cmarco.temperature.core.config.TemperatureSettings;
import top.cmarco.temperature.core.physics.AmbientState;
import top.cmarco.temperature.core.service.TemperatureReading;
import top.cmarco.temperature.core.unit.TemperatureUnit;
import top.cmarco.temperature.plugin.TemperaturePlugin;
import top.cmarco.temperature.plugin.config.DisplaySettings;
import top.cmarco.temperature.plugin.config.LoadedConfiguration;
import top.cmarco.temperature.plugin.preference.PlayerPreferences;
import top.cmarco.temperature.plugin.service.SeasonTracker;
import top.cmarco.temperature.plugin.service.TemperatureService;
import top.cmarco.temperature.plugin.service.WorldRegistry;
import top.cmarco.temperature.plugin.ui.ActionBarRenderer;

/**
 * Pins the PlaceholderAPI contract: which identifiers resolve, what they resolve to, and — just as
 * important — that an unknown identifier returns {@code null} rather than an empty string, because
 * {@code null} is how a typo becomes visible instead of silently rendering as nothing.
 *
 * <p>The plugin and its services are mocked; the values come from a hand-built reading, so every
 * assertion below is about the mapping and the formatting rather than about the physics (which has
 * its own tests in {@code temperature-core}).
 */
class TemperaturePlaceholderExpansionTest {

    private static final double BODY_CELSIUS = 21.5;
    private static final double AIR_CELSIUS = 24.0;
    private static final double APPARENT_CELSIUS = 26.5;
    private static final double HUMIDITY = 62.4;
    private static final double WIND = 15.5;

    private TemperaturePlugin plugin;
    private Player player;
    private World world;
    private TemperaturePlaceholderExpansion expansion;

    @BeforeEach
    void setUp() {
        TemperatureSettings settings = TemperatureSettings.defaults();
        DisplaySettings display = displaySettings();

        world = mock(World.class);
        lenient().when(world.getName()).thenReturn("world");

        // `Biome` is deliberately not mocked: it is an interface whose constants are populated from
        // the server's registry, and Mockito cannot create one outside a running server. The biome
        // placeholder is a one-line delegation to BiomeKeys.keyOf(Biome#getKey()), so it is left out
        // of this test rather than faked.
        player = mock(Player.class);
        lenient().when(player.getPlayer()).thenReturn(player);
        lenient().when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(player.getWorld()).thenReturn(world);

        TemperatureReading reading = new TemperatureReading(
                player.getUniqueId(), BODY_CELSIUS,
                new AmbientState(AIR_CELSIUS, APPARENT_CELSIUS, HUMIDITY, WIND, Season.SUMMER), 0L);

        TemperatureService service = mock(TemperatureService.class);
        lenient().when(service.lastReading(player)).thenReturn(Optional.of(reading));
        lenient().when(service.unitOf(player)).thenReturn(TemperatureUnit.CELSIUS);
        lenient().when(service.display()).thenReturn(display);
        PlayerPreferences preferences = mock(PlayerPreferences.class);
        lenient().when(preferences.displayEnabled(player)).thenReturn(true);
        lenient().when(service.preferences()).thenReturn(preferences);

        SeasonTracker seasons = mock(SeasonTracker.class);
        lenient().when(seasons.season(world)).thenReturn(Season.SUMMER);
        WorldRegistry worlds = mock(WorldRegistry.class);
        lenient().when(worlds.isSimulated(world)).thenReturn(true);

        LoadedConfiguration configuration = mock(LoadedConfiguration.class);
        lenient().when(configuration.simulation()).thenReturn(settings);
        lenient().when(configuration.display()).thenReturn(display);

        PluginDescriptionFile description = mock(PluginDescriptionFile.class);
        lenient().when(description.getVersion()).thenReturn("3.0.1");
        lenient().when(description.getAuthors()).thenReturn(List.of("CMarco"));

        plugin = mock(TemperaturePlugin.class);
        lenient().when(plugin.getDescription()).thenReturn(description);
        lenient().when(plugin.service()).thenReturn(service);
        lenient().when(plugin.seasons()).thenReturn(seasons);
        lenient().when(plugin.worlds()).thenReturn(worlds);
        lenient().when(plugin.configuration()).thenReturn(configuration);
        lenient().when(plugin.renderer()).thenReturn(new ActionBarRenderer());

        expansion = new TemperaturePlaceholderExpansion(plugin);
    }

    private static DisplaySettings displaySettings() {
        Map<Season, String> labels = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            labels.put(season, season.displayName());
        }
        return new DisplaySettings(true, TemperatureUnit.CELSIUS, "&7[{BAR}&7] {TEMP} {SEASON}",
                "%.1f", 10, "|", ".", "&b", "&a", "&c", 5.0, 29.5, labels);
    }

    private String request(String identifier) {
        return expansion.onRequest(player, identifier);
    }

    @Test
    void declaresTheExpectedIdentity() {
        assertThat(expansion.getIdentifier()).isEqualTo("temperature");
        assertThat(expansion.persist()).isTrue();
        assertThat(expansion.getVersion()).isEqualTo("3.0.1");
        assertThat(expansion.getAuthor()).isEqualTo("CMarco");
    }

    @Test
    void resolvesTheReadingInThePlayersUnitAndInEveryScale() {
        assertThat(request("temperature")).isEqualTo("21.5");
        assertThat(request("temp")).isEqualTo("21.5");
        assertThat(request("temperature_celsius")).isEqualTo("21.5");
        assertThat(request("temperature_fahrenheit")).isEqualTo("70.7");
        assertThat(request("temperature_kelvin")).isEqualTo("294.7");
        assertThat(request("temperature_rankine")).isEqualTo("530.4");
        assertThat(request("temperature_reaumur")).isEqualTo("17.2");
        assertThat(request("temperature_newton")).isEqualTo("7.1");
        assertThat(request("temperature_delisle")).isEqualTo("117.8");
        assertThat(request("temperature_romer")).isEqualTo("18.8");
    }

    @Test
    void resolvesTheEnvironment() {
        assertThat(request("air")).isEqualTo("24.0");
        assertThat(request("apparent")).isEqualTo("26.5");
        assertThat(request("feels_like")).isEqualTo("26.5");
        assertThat(request("humidity")).isEqualTo("62.4");
        assertThat(request("wind")).isEqualTo("15.5");
    }

    @Test
    void integerVariantsDropTheDecimal() {
        assertThat(request("temperature_int")).isEqualTo("22");
        assertThat(request("humidity_int")).isEqualTo("62");
        assertThat(request("wind_int")).isEqualTo("16");
    }

    @Test
    void resolvesMetadataThatNeedsNoReading() {
        assertThat(request("unit")).isEqualTo("CELSIUS");
        assertThat(request("unit_symbol")).isEqualTo("\u00b0C");
        assertThat(request("world")).isEqualTo("world");
        assertThat(request("season")).isEqualTo("SUMMER");
        assertThat(request("simulated")).isEqualTo("true");
        assertThat(request("display_enabled")).isEqualTo("true");
        assertThat(request("heat_threshold")).isEqualTo("40.5");
        assertThat(request("cold_threshold")).isEqualTo("-0.5");
    }

    @Test
    void derivesStateFromTheConfiguredThresholds() {
        assertThat(request("state")).isEqualTo("comfortable");
    }

    @Test
    void resolvesTheRenderedInterface() {
        assertThat(request("bar")).isNotEmpty().contains(String.valueOf(org.bukkit.ChatColor.COLOR_CHAR));
        assertThat(request("action_bar")).isNotEmpty()
                .contains("21.5")
                .contains("Summer")
                .doesNotContain("{TEMP}");
    }

    @Test
    void identifiersAreCaseInsensitive() {
        assertThat(request("TEMPERATURE")).isEqualTo("21.5");
        assertThat(request("Humidity")).isEqualTo("62.4");
    }

    @Test
    void unknownIdentifierReturnsNullSoTyposAreVisible() {
        assertThat(request("does_not_exist")).isNull();
        assertThat(request("")).isNull();
    }

    @Test
    void aNullPlayerYieldsAnEmptyString() {
        assertThat(expansion.onRequest(null, "temperature")).isEmpty();
    }

    @Test
    void anOfflinePlayerStillResolvesConfigurationButNotReadings() {
        Player offline = mock(Player.class);
        when(offline.getPlayer()).thenReturn(null);

        assertThat(expansion.onRequest(offline, "unit")).isEqualTo("CELSIUS");
        assertThat(expansion.onRequest(offline, "temperature")).isEmpty();
        assertThat(expansion.onRequest(offline, "unknown_thing")).isNull();
    }
}
