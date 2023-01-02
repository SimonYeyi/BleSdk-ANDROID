package sm.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import sm.peripheral.Logger;
import sm.peripheral.PeripheralType;

public class BluetoothDeviceExplorer {
    private final BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private final List<PeripheralType> targetPeripherals = new ArrayList<>();
    private ExploreDeviceCallback exploreDeviceCallback;
    private BluetoothLeScanCallback bluetoothLeScanCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private BluetoothLeScanner bluetoothLeScanner;

    public void startExploreDevice(List<PeripheralType> targetPeripherals, ExploreDeviceCallback exploreDeviceCallback) {
        this.targetPeripherals.clear();
        this.targetPeripherals.addAll(targetPeripherals);
        this.exploreDeviceCallback = exploreDeviceCallback;
        stopExploreDevice();
        exploreDevice();
    }

    @SuppressLint("MissingPermission")
    private void exploreDevice() {
        if (!isCanceledExploreDevice()) return;
        Logger.d("BluetoothDeviceExplorer: explore start, targetPeripherals is " + targetPeripherals + ", explorer is " + this);

        bluetoothLeScanCallback = new BluetoothLeScanCallback();
        if (getBluetoothLeScanner() != null && bluetooth.isEnabled()) {
            List<ScanFilter> scanFilters = null;
/*            scanFilters = new ArrayList<>(targetPeripherals.size());
            for (PeripheralType peripheralType : targetPeripherals) {
                scanFilters.add(new ScanFilter.Builder().setDeviceName(peripheralType.getName()).setDeviceAddress(peripheralType.getAddress()).build());
            }*/
            getBluetoothLeScanner().startScan(scanFilters, new ScanSettings.Builder().build(), bluetoothLeScanCallback);
        }
        //loopExploreDevice();
    }

    public void retryExploreDevice() {
        if (isCanceledExploreDevice()) return;
        stopExploreDevice();
        exploreDevice();
    }

    @SuppressLint("MissingPermission")
    public void stopExploreDevice() {
        if (isCanceledExploreDevice()) return;
        Logger.d("BluetoothDeviceExplorer: explore stop, targetPeripherals is " + targetPeripherals + ", explorer is " + this);

        if (getBluetoothLeScanner() != null && bluetooth.isEnabled()) {
            getBluetoothLeScanner().stopScan(bluetoothLeScanCallback);
        }
        bluetoothLeScanCallback = null;
        handler.removeCallbacksAndMessages(null);
    }

    public boolean isCanceledExploreDevice() {
        return bluetoothLeScanCallback == null;
    }

    public boolean equalsTargets(List<PeripheralType> targetPeripherals) {
        return this.targetPeripherals.equals(targetPeripherals);
    }

    public void removeTarget(PeripheralType targetPeripheral) {
        targetPeripherals.remove(targetPeripheral);
        if (targetPeripherals.isEmpty()) stopExploreDevice();
    }

    private BluetoothLeScanner getBluetoothLeScanner() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetooth.getBluetoothLeScanner();
        }
        if (bluetoothLeScanner == null) {
            Logger.w("BluetoothDeviceExplorer: BluetoothLeScanner null");
        }
        return bluetoothLeScanner;
    }

    private void loopExploreDevice() {
        handler.postDelayed(this::retryExploreDevice, 10 * 1000);
    }

    private class BluetoothLeScanCallback extends ScanCallback {

        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice bluetoothDevice = result.getDevice();
            final String deviceName = bluetoothDevice.getName();
            boolean hit = false;
            for (PeripheralType targetPeripheral : targetPeripherals) {
                hit = targetPeripheral.include(bluetoothDevice);
                if (hit) break;
            }
            if (!isCanceledExploreDevice()
                    && hit
                    && deviceName != null
                    && bluetooth.isEnabled()
            ) {
                handler.post(() -> {
                    if (isCanceledExploreDevice()) return;
                    Logger.d("BluetoothDeviceExplorer: explore result, deviceName is " + deviceName + ", deviceAddress is " + bluetoothDevice.getAddress() + ", explorer is " + BluetoothDeviceExplorer.this);
                    stopExploreDevice();
                    exploreDeviceCallback.onDeviceExplored(bluetoothDevice);
                });
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Logger.d("BluetoothDeviceExplorer: explore fail, targetPeripherals is " + targetPeripherals + ", errorCode is " + errorCode + ", explorer is " + BluetoothDeviceExplorer.this);
            loopExploreDevice();
        }
    }

    public interface ExploreDeviceCallback {
        void onDeviceExplored(BluetoothDevice bluetoothDevice);
    }
}
