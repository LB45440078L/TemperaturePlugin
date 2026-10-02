package top.cmarco.temperature.plugin.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.environment.ActiveEffect;
import top.cmarco.temperature.core.physics.ConsumptionModel;

/**
 * Tracks the timed additive effects in force per player: the lingering chill of a drink of water and
 * any heat or cold a plugin has attached to a consumable.
 *
 * <p>A drink is stored as the instant it happened, not as a fixed amount, because the cooling curve
 * ramps up and back down — the same drink is worth different amounts at different times. Storing the
 * timestamp means the curve is evaluated at read time and a shortened server tick cannot distort it.
 *
 * <p>Not thread-safe; the adapter calls it from the server thread.
 */
public final class EffectRegistry {

    private final Map<UUID, Long> waterDrinks = new HashMap<>();
    private final Map<UUID, List<ActiveEffect>> timed = new HashMap<>();

    /**
     * Records that a player drank water now.
     *
     * @param playerId       the player
     * @param nowEpochMillis the instant of the drink
     */
    public void recordWater(UUID playerId, long nowEpochMillis) {
        waterDrinks.put(playerId, nowEpochMillis);
    }

    /**
     * Records a timed effect.
     *
     * @param playerId  the player
     * @param effect    the effect to attach
     */
    public void addTimed(UUID playerId, @NotNull ActiveEffect effect) {
        timed.computeIfAbsent(playerId, key -> new ArrayList<>()).add(effect);
    }

    /**
     * Returns the effects currently in force, pruning anything expired.
     *
     * @param playerId        the player
     * @param nowEpochMillis  the current instant
     * @param consumptionModel the water cooling curve
     * @return the active effects, water included
     */
    @NotNull
    public List<ActiveEffect> activeEffects(UUID playerId, long nowEpochMillis,
                                            @NotNull ConsumptionModel consumptionModel) {
        List<ActiveEffect> active = new ArrayList<>();

        Long drunkAt = waterDrinks.get(playerId);
        if (drunkAt != null) {
            double minutes = (nowEpochMillis - drunkAt) / 60_000.0;
            if (minutes > consumptionModel.durationMinutes()) {
                waterDrinks.remove(playerId);
            } else {
                double cooling = consumptionModel.waterCoolingCelsius(minutes);
                if (cooling > 0.0) {
                    active.add(new ActiveEffect(-cooling, drunkAt + consumptionModel.durationMinutes() * 60_000L,
                            "water"));
                }
            }
        }

        List<ActiveEffect> playerEffects = timed.get(playerId);
        if (playerEffects != null) {
            Iterator<ActiveEffect> iterator = playerEffects.iterator();
            while (iterator.hasNext()) {
                ActiveEffect effect = iterator.next();
                if (effect.isActiveAt(nowEpochMillis)) {
                    active.add(effect);
                } else {
                    iterator.remove();
                }
            }
            if (playerEffects.isEmpty()) {
                timed.remove(playerId);
            }
        }
        return active;
    }

    /**
     * Forgets everything about a player.
     *
     * @param playerId the player
     */
    public void forget(UUID playerId) {
        waterDrinks.remove(playerId);
        timed.remove(playerId);
    }

    /** Forgets every player. */
    public void clear() {
        waterDrinks.clear();
        timed.clear();
    }
}
