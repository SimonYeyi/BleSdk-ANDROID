package sm.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.UUID;

import sm.peripheral.DataPacket;
import sm.peripheral.Logger;
import sm.peripheral.Peripheral;

public abstract class BluetoothPeripheral extends Peripheral {
    private final BluetoothDevice bluetoothDevice;
    private final Context context;
    protected ConnectCallback callback;
    private BluetoothBleCallback bluetoothBleCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile int connectState = BluetoothProfile.STATE_DISCONNECTED;

    @SuppressLint("MissingPermission")
    public BluetoothPeripheral(Context context, BluetoothDevice bluetoothDevice) {
        super(bluetoothDevice.getName(), bluetoothDevice.getAddress());
        this.context = context;
        this.bluetoothDevice = bluetoothDevice;
    }

    protected BluetoothPeripheral(Context context, String name, String address) {
        super(name, address);
        this.context = context;
        this.bluetoothDevice = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void connect(ConnectCallback callback) {
        if (connectState != BluetoothProfile.STATE_DISCONNECTED) return;
        this.callback = callback;
        bluetoothBleCallback = new BluetoothBleCallback();
        bluetoothDevice.connectGatt(context, false, bluetoothBleCallback);
        connectState = BluetoothProfile.STATE_CONNECTING;
    }

    private class BluetoothBleCallback extends BluetoothGattCallback {
        private BluetoothGatt gatt;

        @SuppressLint("MissingPermission")
        @Override
        public synchronized void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Logger.d("BluetoothPeripheral: gatt is " + gatt + ", status is " + status + ", newState is " + newState);
            this.gatt = gatt;
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (gatt.discoverServices()) return;
                onConnectFailed();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                final boolean connecting = connectState == BluetoothProfile.STATE_CONNECTING;
                close();
                if (connecting) onConnectFailed();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public synchronized void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BluetoothGattService gattService = gatt.getService(UUID.fromString(getServiceUuid()));
            if (gattService == null) {
                onConnectFailed();
            } else {
                BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(UUID.fromString(BluetoothPeripheral.this.getNotifyCharacteristicUuid()));
                gatt.setCharacteristicNotification(gattCharacteristic, true);
                BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(gattDescriptor);
            }
        }

        @Override
        public synchronized void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (connectState != BluetoothProfile.STATE_DISCONNECTED) {
                connectState = BluetoothProfile.STATE_CONNECTED;
                handler.post(() -> callback.onConnected(BluetoothPeripheral.this));
            }
        }

        @Override
        public synchronized void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (connectState != BluetoothProfile.STATE_CONNECTED) return;
            byte[] value = characteristic.getValue();
            if (messenger != null) handler.post(() -> messenger.reply(value));
        }

        @SuppressLint("MissingPermission")
        private synchronized void close() {
            if (gatt == null) return;
            gatt.disconnect();
            gatt.close();
            gatt = null;
            final boolean connected = connectState == BluetoothProfile.STATE_CONNECTED;
            connectState = BluetoothProfile.STATE_DISCONNECTED;
            if (connected) handler.post(() -> callback.onDisconnected(BluetoothPeripheral.this));
        }

        private void onConnectFailed() {
            close();
            handler.post(() -> callback.onConnectFailed(BluetoothPeripheral.this));
        }
    }

    @Override
    public synchronized void disconnect() {
        if (connectState == BluetoothProfile.STATE_DISCONNECTED) return;
        bluetoothBleCallback.close();
        bluetoothBleCallback = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void set(DataPacket command) {
        if (connectState != BluetoothProfile.STATE_CONNECTED) return;
        Logger.d(getClass().getSimpleName() + ": " + command.toString());
        BluetoothGatt bluetoothGatt = bluetoothBleCallback.gatt;
        BluetoothGattService gattService = bluetoothGatt.getService(UUID.fromString(getServiceUuid()));
        if (gattService == null) return;
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(this.getWriteCharacteristicUuid()));
        if (characteristic == null) return;
        characteristic.setValue(command.getData());
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    protected abstract String getServiceUuid();

    protected abstract String getNotifyCharacteristicUuid();

    protected abstract String getWriteCharacteristicUuid();
}
