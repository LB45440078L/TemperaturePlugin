package top.cmarco.temperatureplugin.temperature;

import org.jetbrains.annotations.NotNull;

public enum Temperature {
   CELSIUS((v) -> {
      return v;
   }, (v) -> {
      return v;
   }, "°C"),
   FAHRENHEIT((v) -> {
      return v * 1.8D + 32.0D;
   }, (v) -> {
      return (v - 32.0D) / 1.8D;
   }, "°F"),
   KELVIN((v) -> {
      return v + 273.15D;
   }, (v) -> {
      return v - 273.15D;
   }, "K"),
   RANKINE((v) -> {
      return (v + 273.15D) * 1.8D;
   }, (v) -> {
      return (v - 491.67D) / 1.8D;
   }, "°R"),
   REAUMUR((v) -> {
      return v * 0.8D;
   }, (v) -> {
      return v / 0.8D;
   }, "°Re"),
   NEWTON((v) -> {
      return v * 0.33D;
   }, (v) -> {
      return (v - 32.0D) * 5.0D / 9.0D;
   }, "°N"),
   DELISLE((v) -> {
      return (100.0D - v) * 1.5D;
   }, (v) -> {
      return (100.0D - v) / 1.5D;
   }, "°De"),
   ROMER((v) -> {
      return 7.5D + v * 0.525D;
   }, (v) -> {
      return (v - 7.5D) / 0.525D;
   }, "°Ro");

   private final TemperatureConverter celsiusToUnit;
   private final TemperatureConverter unitToCelsius;
   private final String name;

   private Temperature(@NotNull final TemperatureConverter param3, TemperatureConverter param4, final String param5) {
      this.celsiusToUnit = param3;
      this.unitToCelsius = param4;
      this.name = param5;
   }

   public String getName() {
      return this.name;
   }

   public double convertToUnit(double value) {
      return this.celsiusToUnit.convert(value);
   }

   public double convertUnitToCelsius(double value) {
      return this.unitToCelsius.convert(value);
   }

   // $FF: synthetic method
   private static Temperature[] $values() {
      return new Temperature[]{CELSIUS, FAHRENHEIT, KELVIN, RANKINE, REAUMUR, NEWTON, DELISLE, ROMER};
   }
}
