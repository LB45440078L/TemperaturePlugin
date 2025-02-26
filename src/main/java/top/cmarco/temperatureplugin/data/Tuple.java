package top.cmarco.temperatureplugin.data;

import org.jetbrains.annotations.NotNull;

public class Tuple<K, T> {
   private final K a;
   private final T b;

   public Tuple(@NotNull K a, @NotNull T b) {
      this.a = a;
      this.b = b;
   }

   @NotNull
   public K getA() {
      return this.a;
   }

   @NotNull
   public T getB() {
      return this.b;
   }

   public static final class Builder<K, T> {
      private K k = null;
      private T t = null;

      public Tuple.Builder<K, T> setFirst(@NotNull K k) {
         this.k = k;
         return this;
      }

      public Tuple.Builder<K, T> setSecond(@NotNull T t) {
         this.t = t;
         return this;
      }

      public Tuple<K, T> build() {
         return this.k != null && this.t != null ? new Tuple(this.k, this.t) : null;
      }
   }
}
