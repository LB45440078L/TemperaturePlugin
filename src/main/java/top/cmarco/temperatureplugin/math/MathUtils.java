package top.cmarco.temperatureplugin.math;

public class MathUtils {
   public static final double G_FACTOR_1 = 116000.0D / (4700.0D * Math.sqrt(6.283185307179586D));
   public static final double G_FACTOR_2 = 4.418E7D;
   public static final double NOON_TICKS = 6500.0D;
   public static final double[] CACHED_HUMIDITY_SCALE = new double[24001];
   public static final double[] CACHED_HEIGHT_MAP_SCALE = new double[79864];
   public static final double[] CACHED_HEIGHT_POLYNOMIAL_COEFFS = new double[]{3.981796231733E-6D, -0.00322799D, 0.695913D};

   public static double getScaleAt(long ticks) {
      return (G_FACTOR_1 * Math.exp(-(((double)ticks - 6500.0D) * ((double)ticks - 6500.0D) / 4.418E7D)) + 0.15D) / 10.0D;
   }

   static {
      int j;
      double i;
      for(j = 0; j <= 24000; ++j) {
         i = getScaleAt((long)j);
         CACHED_HUMIDITY_SCALE[j] = i;
      }

      j = 0;

      for(i = -64.0D; i < 330.0D; ++j) {
         double ax2 = CACHED_HEIGHT_POLYNOMIAL_COEFFS[0] * i * i;
         double bx = CACHED_HEIGHT_POLYNOMIAL_COEFFS[1] * i;
         double c = CACHED_HEIGHT_POLYNOMIAL_COEFFS[2];
         double f = ax2 + bx + c;
         CACHED_HEIGHT_MAP_SCALE[j] = f;
         i += 0.005D;
      }

   }
}
