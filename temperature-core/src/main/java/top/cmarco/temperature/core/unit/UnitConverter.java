package top.cmarco.temperature.core.unit;

/**
 * A single-variable scalar transform used to move a reading between two temperature scales.
 *
 * <p>Every conversion in {@link TemperatureUnit} is expressed as a pair of these: one from the
 * canonical working scale (degrees Celsius) and one back to it. Homogeneous (linear) transforms
 * are used throughout because all supported scales are affine in Celsius.
 */
@FunctionalInterface
public interface UnitConverter {

    /**
     * Applies the transform.
     *
     * @param value the source magnitude, in the scale this converter reads from
     * @return the transformed magnitude, in the scale this converter writes to
     */
    double convert(double value);
}
