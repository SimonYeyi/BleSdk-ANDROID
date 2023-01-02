package sm.peripheral;

public class PeripheralInitResult {
    private PeripheralFirmwareVersion version;
    private PeripheralPower power;

    public PeripheralInitResult() {
    }

    public PeripheralInitResult(PeripheralFirmwareVersion version, PeripheralPower power) {
        this.version = version;
        this.power = power;
    }

    public PeripheralFirmwareVersion getVersion() {
        return version;
    }

    public void setVersion(PeripheralFirmwareVersion version) {
        this.version = version;
    }

    public PeripheralPower getPower() {
        return power;
    }

    public void setPower(PeripheralPower power) {
        this.power = power;
    }

    @Override
    public String toString() {
        return "PeripheralInitResult{" +
                "version=" + (version == null ? null : version.getVersion()) +
                ", power=" + (power == null ? null : power.getPower()) +
                '}';
    }
}
