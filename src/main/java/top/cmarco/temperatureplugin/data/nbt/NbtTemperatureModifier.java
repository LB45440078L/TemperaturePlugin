package top.cmarco.temperatureplugin.data.nbt;

public record NbtTemperatureModifier(boolean increase, int amount, long expireTime) {

    @Override
    public String toString() {
        return "NbtTemperatureModifier{" +
                "increase=" + increase +
                ", amount=" + amount +
                ", expireTime=" + expireTime +
                '}';
    }
}
