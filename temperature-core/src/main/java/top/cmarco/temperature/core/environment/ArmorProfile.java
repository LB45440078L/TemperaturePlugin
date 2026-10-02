package top.cmarco.temperature.core.environment;

/**
 * What the player is wearing, reduced to the two facts the model uses.
 *
 * <p>Leather insulates: each piece nudges the perceived temperature up. Fire protection (the
 * enchantment, or the potion effect) does not change the reading at all — it grants immunity to the
 * heat penalty, which is a threshold concern handled by the adapter, so it is carried here only so
 * the adapter does not have to rescan the inventory.
 *
 * @param leatherPieces           how many of the four armour slots hold leather armour
 * @param fireProtectionPieces    how many worn pieces carry fire protection
 */
public record ArmorProfile(int leatherPieces, int fireProtectionPieces) {

    /** A player wearing nothing relevant. */
    public static final ArmorProfile NONE = new ArmorProfile(0, 0);

    public ArmorProfile {
        if (leatherPieces < 0 || fireProtectionPieces < 0) {
            throw new IllegalArgumentException("armour counts must not be negative");
        }
    }

    /** @return {@code true} if the player is wearing enough leather to be insulated. */
    public boolean hasLeather() {
        return leatherPieces > 0;
    }
}
