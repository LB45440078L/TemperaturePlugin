package top.cmarco.temperatureplugin.humidity;

import java.util.EnumMap;
import java.util.HashMap;

import org.bukkit.block.Biome;
import top.cmarco.temperatureplugin.data.Tuple;
import top.cmarco.temperatureplugin.data.impl.DoublesTuple;

public final class BiomeHumidity {
   public static final HashMap<Biome, Tuple<Double, Double>> BIOME_HUMIDITY = new HashMap<>();
   public static final Tuple<Double, Double> STANDARD = DoublesTuple.getBuilder().setFirst(40.0D).setSecond(50.0D).build();
   public static final Tuple<Double, Double> HUMID = DoublesTuple.getBuilder().setFirst(70.0D).setSecond(90.0D).build();
   public static final Tuple<Double, Double> VERY_HUMID = DoublesTuple.getBuilder().setFirst(80.0D).setSecond(99.999D).build();
   public static final Tuple<Double, Double> DRY = DoublesTuple.getBuilder().setFirst(20.0D).setSecond(42.5D).build();
   public static final Tuple<Double, Double> VERY_DRY = DoublesTuple.getBuilder().setFirst(5.0D).setSecond(20.0D).build();
   public static final Tuple<Double, Double> SANITIZED = DoublesTuple.getBuilder().setFirst(0.0D).setSecond(0.025D).build();

   static {
      BIOME_HUMIDITY.put(Biome.OCEAN, HUMID);
      BIOME_HUMIDITY.put(Biome.PLAINS, HUMID);
      BIOME_HUMIDITY.put(Biome.DESERT, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.WINDSWEPT_HILLS, DRY);
      BIOME_HUMIDITY.put(Biome.FOREST, HUMID);
      BIOME_HUMIDITY.put(Biome.TAIGA, HUMID);
      BIOME_HUMIDITY.put(Biome.SWAMP, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.MANGROVE_SWAMP, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.RIVER, STANDARD);
      BIOME_HUMIDITY.put(Biome.NETHER_WASTES, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.THE_END, STANDARD);
      BIOME_HUMIDITY.put(Biome.FROZEN_OCEAN, HUMID);
      BIOME_HUMIDITY.put(Biome.FROZEN_RIVER, DRY);
      BIOME_HUMIDITY.put(Biome.SNOWY_PLAINS, DRY);
      BIOME_HUMIDITY.put(Biome.MUSHROOM_FIELDS, STANDARD);
      BIOME_HUMIDITY.put(Biome.BEACH, HUMID);
      BIOME_HUMIDITY.put(Biome.JUNGLE, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.SPARSE_JUNGLE, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.DEEP_OCEAN, HUMID);
      BIOME_HUMIDITY.put(Biome.STONY_SHORE, STANDARD);
      BIOME_HUMIDITY.put(Biome.SNOWY_BEACH, DRY);
      BIOME_HUMIDITY.put(Biome.BIRCH_FOREST, HUMID);
      BIOME_HUMIDITY.put(Biome.DARK_FOREST, HUMID);
      BIOME_HUMIDITY.put(Biome.SNOWY_TAIGA, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.OLD_GROWTH_PINE_TAIGA, HUMID);
      BIOME_HUMIDITY.put(Biome.WINDSWEPT_FOREST, STANDARD);
      BIOME_HUMIDITY.put(Biome.SAVANNA, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.SAVANNA_PLATEAU, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.BADLANDS, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.WOODED_BADLANDS, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.SMALL_END_ISLANDS, STANDARD);
      BIOME_HUMIDITY.put(Biome.END_MIDLANDS, STANDARD);
      BIOME_HUMIDITY.put(Biome.END_HIGHLANDS, STANDARD);
      BIOME_HUMIDITY.put(Biome.END_BARRENS, STANDARD);
      BIOME_HUMIDITY.put(Biome.WARM_OCEAN, HUMID);
      BIOME_HUMIDITY.put(Biome.LUKEWARM_OCEAN, HUMID);
      BIOME_HUMIDITY.put(Biome.COLD_OCEAN, DRY);
      BIOME_HUMIDITY.put(Biome.DEEP_LUKEWARM_OCEAN, STANDARD);
      BIOME_HUMIDITY.put(Biome.DEEP_COLD_OCEAN, DRY);
      BIOME_HUMIDITY.put(Biome.DEEP_FROZEN_OCEAN, DRY);
      BIOME_HUMIDITY.put(Biome.THE_VOID, STANDARD);
      BIOME_HUMIDITY.put(Biome.SUNFLOWER_PLAINS, STANDARD);
      BIOME_HUMIDITY.put(Biome.WINDSWEPT_GRAVELLY_HILLS, DRY);
      BIOME_HUMIDITY.put(Biome.FLOWER_FOREST, STANDARD);
      BIOME_HUMIDITY.put(Biome.ICE_SPIKES, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.OLD_GROWTH_BIRCH_FOREST, HUMID);
      BIOME_HUMIDITY.put(Biome.OLD_GROWTH_SPRUCE_TAIGA, HUMID);
      BIOME_HUMIDITY.put(Biome.WINDSWEPT_SAVANNA, HUMID);
      BIOME_HUMIDITY.put(Biome.ERODED_BADLANDS, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.BAMBOO_JUNGLE, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.SOUL_SAND_VALLEY, HUMID);
      BIOME_HUMIDITY.put(Biome.CRIMSON_FOREST, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.WARPED_FOREST, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.BASALT_DELTAS, VERY_HUMID);
      BIOME_HUMIDITY.put(Biome.DRIPSTONE_CAVES, HUMID);
      BIOME_HUMIDITY.put(Biome.LUSH_CAVES, HUMID);
      BIOME_HUMIDITY.put(Biome.DEEP_DARK, STANDARD);
      BIOME_HUMIDITY.put(Biome.MEADOW, STANDARD);
      BIOME_HUMIDITY.put(Biome.GROVE, DRY);
      BIOME_HUMIDITY.put(Biome.SNOWY_SLOPES, DRY);
      BIOME_HUMIDITY.put(Biome.FROZEN_PEAKS, VERY_DRY);
      BIOME_HUMIDITY.put(Biome.JAGGED_PEAKS, STANDARD);
      BIOME_HUMIDITY.put(Biome.STONY_PEAKS, DRY);
      BIOME_HUMIDITY.put(Biome.CHERRY_GROVE, DRY);
      BIOME_HUMIDITY.put(Biome.CUSTOM, STANDARD);
   }
}
