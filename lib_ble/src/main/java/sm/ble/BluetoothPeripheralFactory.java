package sm.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

public interface BluetoothPeripheralFactory {

    BluetoothPeripheral create(Context context, BluetoothDevice bluetoothDevice);
}
