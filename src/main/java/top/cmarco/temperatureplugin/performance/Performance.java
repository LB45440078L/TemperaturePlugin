package top.cmarco.temperatureplugin.performance;

public enum Performance {
   LOW(20L),
   MEDIUM(10L),
   HIGH(8L),
   EXTREME(1L);

   private final long updateValue;

   private Performance(final long param3) {
      assert param3 >= 1L;

      this.updateValue = param3;
   }

   public long getUpdateValue() {
      return this.updateValue;
   }

   // $FF: synthetic method
   private static Performance[] $values() {
      return new Performance[]{LOW, MEDIUM, HIGH, EXTREME};
   }
}
