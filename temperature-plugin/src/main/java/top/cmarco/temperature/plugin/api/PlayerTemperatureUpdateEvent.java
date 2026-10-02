package top.cmarco.temperature.plugin.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import top.cmarco.temperature.core.physics.AmbientState;
import top.cmarco.temperature.core.unit.TemperatureUnit;

/**
 * Fired every time the plugin produces a new body temperature for a player.
 *
 * <p>The event carries the reading twice: canonically, in degrees Celsius ({@link #bodyCelsius()}),
 * and in the player's own display unit ({@link #bodyInUnit()}). Both are readable and writable, so a
 * consumer can either nudge the physics in the player's preferred scale or in the scale the
 * simulation actually works in — and whichever they set, the other follows.
 *
 * <p>Cancelling the event leaves the player's stored temperature untouched for this step; the
 * ambient reading is still reported so a listener can react to it.
 *
 * <p>This replaces the legacy event, which stored only a display-unit value and therefore could not
 * describe the physical state to a listener that did not know the player's unit.
 */
public class PlayerTemperatureUpdateEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final TemperatureUnit unit;
    private final AmbientState ambient;
    private double bodyCelsius;
    private boolean cancelled;

    /**
     * @param player      the player
     * @param unit        the unit the player is displaying in
     * @param bodyCelsius the new body temperature, in degrees Celsius
     * @param ambient     the ambient state the body is drifting towards
     */
    public PlayerTemperatureUpdateEvent(@NotNull Player player, @NotNull TemperatureUnit unit,
                                        double bodyCelsius, @NotNull AmbientState ambient) {
        super(player);
        this.unit = unit;
        this.bodyCelsius = bodyCelsius;
        this.ambient = ambient;
    }

    /** @return the new body temperature, in degrees Celsius. */
    public double bodyCelsius() {
        return bodyCelsius;
    }

    /** @param celsius the new body temperature, in degrees Celsius. */
    public void setBodyCelsius(double celsius) {
        this.bodyCelsius = celsius;
    }

    /** @return the new body temperature in the player's display unit. */
    public double bodyInUnit() {
        return unit.fromCelsius(bodyCelsius);
    }

    /** @param value the new body temperature, interpreted in the player's display unit. */
    public void setBodyInUnit(double value) {
        this.bodyCelsius = unit.toCelsius(value);
    }

    /** @return the unit the player is displaying in. */
    @NotNull
    public TemperatureUnit unit() {
        return unit;
    }

    /** @return the ambient state behind this reading. */
    @NotNull
    public AmbientState ambient() {
        return ambient;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
