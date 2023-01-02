package sm.ble;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

import sm.peripheral.Peripheral;
import sm.peripheral.PeripheralManager;
import sm.peripheral.PeripheralType;

public class BluetoothPeripheralFragment extends Fragment implements PeripheralManager.Callback {
    private final BluetoothStateReceiver bluetoothStateReceiver = new BluetoothStateReceiver();
    private BluetoothDeviceExplorer bluetoothDeviceExplorer;
    private BluetoothPermissionsContext bluetoothPermissionsContext = new BluetoothPermissionsContext();
    private final List<PeripheralType> targetPeripherals = new ArrayList<>();
    private PeripheralManager.Callback peripheralManagerCallback;
    private Peripheral connectedPeripheral;
    private boolean disableAutoDisconnect;
    private boolean activated;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothDeviceExplorer = new BluetoothDeviceExplorer();
        bluetoothPermissionsContext.request(this, () -> {
            if (activated) startExploreDevice();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        activated = true;

        bluetoothStateReceiver.registerTo(getActivity(), enabled -> {
            if (enabled) {
                bluetoothDeviceExplorer.retryExploreDevice();
            } else {
                connectedPeripheral = null;
            }
        });

        startExploreDevice();
    }

    @Override
    public void onPause() {
        super.onPause();
        activated = false;
        stopExploreDevice();
        bluetoothStateReceiver.unregisterFrom(getActivity());
    }

    @Override
    public void onDestroy() {
        peripheralManagerCallback = null;
        if (connectedPeripheral != null && !disableAutoDisconnect) {
            connectedPeripheral.disconnect();
            connectedPeripheral = null;
        }
        super.onDestroy();
    }

    @Override
    public void onDiscovered(@NonNull Peripheral peripheral) {
        if (peripheralManagerCallback != null && connectedPeripheral == null && isTargetPeripheral(peripheral)) {
            peripheralManagerCallback.onDiscovered(peripheral);
        }
    }

    @Override
    public void onConnected(@NonNull Peripheral peripheral) {
        if (connectedPeripheral == null && isTargetPeripheral(peripheral)) {
            connectedPeripheral = peripheral;
            if (peripheralManagerCallback != null) {
                peripheralManagerCallback.onConnected(peripheral);
            }
        }
    }

    @Override
    public void onDisconnected(@NonNull Peripheral peripheral) {
        if (peripheral.equals(connectedPeripheral)) {
            connectedPeripheral = null;
            if (peripheralManagerCallback != null) {
                peripheralManagerCallback.onDisconnected(peripheral);
            }
        }
    }

    private boolean isTargetPeripheral(Peripheral peripheral) {
        boolean result = false;
        for (PeripheralType targetPeripheral : targetPeripherals) {
            if (targetPeripheral.getAddress() != null) {
                result = targetPeripheral.include(peripheral);
                break;
            } else {
                result = targetPeripheral.include(peripheral);
                if (result) break;
            }
        }
        return result;
    }

    private void startExploreDevice() {
        if (bluetoothPermissionsContext.checkAllPermissions() && connectedPeripheral == null) {
            bluetoothDeviceExplorer.startExploreDevice(targetPeripherals, BluetoothPeripheralManager.getInstance());
            BluetoothPeripheralManager.getInstance().addCallback(this);
        }
    }

    private void stopExploreDevice() {
        bluetoothDeviceExplorer.stopExploreDevice();
        BluetoothPeripheralManager.getInstance().removeCallback(this);
    }

    public BluetoothPeripheralFragment setCallback(PeripheralManager.Callback callback) {
        this.peripheralManagerCallback = callback;
        return this;
    }

    public BluetoothPeripheralFragment setExplorePeripheralType(List<PeripheralType> peripheralTypes) {
        this.targetPeripherals.clear();
        this.targetPeripherals.addAll(peripheralTypes);
        return this;
    }

    public BluetoothPeripheralFragment setPermissionsContext(BluetoothPermissionsContext permissionsContext) {
        this.bluetoothPermissionsContext = permissionsContext;
        return this;
    }

    public BluetoothPeripheralFragment disableAutoDisconnect() {
        this.disableAutoDisconnect = true;
        return this;
    }

    public BluetoothPeripheralFragment attachTo(FragmentActivity fragmentActivity) {
        fragmentActivity
                .getSupportFragmentManager()
                .beginTransaction()
                .add(this, this.getClass().getSimpleName())
                .commitNowAllowingStateLoss();
        return this;
    }

    public void reconnect() {
        stopExploreDevice();
        startExploreDevice();
    }
}
