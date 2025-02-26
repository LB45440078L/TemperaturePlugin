package top.cmarco.temperatureplugin.data.impl;

import org.jetbrains.annotations.NotNull;
import top.cmarco.temperatureplugin.data.Tuple;

public final class DoublesTuple extends Tuple<Double, Double> {
   public DoublesTuple(@NotNull Double a, @NotNull Double b) {
      super(a, b);
   }

   public static Tuple.Builder<Double, Double> getBuilder() {
      return new Tuple.Builder();
   }
}
