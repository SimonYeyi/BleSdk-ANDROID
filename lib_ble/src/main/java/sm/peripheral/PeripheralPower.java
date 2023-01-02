package sm.peripheral;

public interface PeripheralPower {

    /**
     * @return 电量。不支持时，返回-1
     */
    int getPower();
}
