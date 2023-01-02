package sm.peripheral;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import java.util.Objects;

public class PeripheralType {
    private final String name;
    private String address;

    public PeripheralType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PeripheralType withAddress(String address) {
        this.address = address;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public boolean include(String name, String address) {
        return (this.name == null || this.name.equals(name)) && (this.address == null || this.address.equals(address));
    }

    @SuppressLint("MissingPermission")
    public boolean include(BluetoothDevice bluetoothDevice) {
        return include(bluetoothDevice.getName(), bluetoothDevice.getAddress());
    }

    public boolean include(Peripheral peripheral) {
        return include(peripheral.getName(), peripheral.getAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeripheralType that = (PeripheralType) o;
        return Objects.equals(name, that.name) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @NonNull
    @Override
    public String toString() {
        return "PeripheralType{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
