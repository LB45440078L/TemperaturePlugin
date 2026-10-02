/**
 * Platform-free simulation core for the TemperaturePlugin.
 *
 * <p>Nothing in this package tree may reference {@code org.bukkit} or {@code io.papermc}. The
 * Paper adapter ({@code temperature-plugin}) is responsible for translating the live world into
 * the immutable {@link top.cmarco.temperature.core.physics.ThermalEnvironment} this module
 * consumes, and for translating results back into player-visible effects.
 */
package top.cmarco.temperature.core;
