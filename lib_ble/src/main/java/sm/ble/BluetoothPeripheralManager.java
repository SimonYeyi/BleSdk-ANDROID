package sm.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import sm.peripheral.Logger;
import sm.peripheral.Peripheral;
import sm.peripheral.PeripheralManager;
import sm.peripheral.PeripheralType;

public class BluetoothPeripheralManager implements PeripheralManager, BluetoothStateReceiver.OnBluetoothStateListener, BluetoothDeviceExplorer.ExploreDeviceCallback, Peripheral.ConnectCallback {
    private static final BluetoothPeripheralManager instance = new BluetoothPeripheralManager();
    private Context applicationContext;
    private final List<Peripheral> connectedPeripherals = new CopyOnWriteArrayList<>();
    private final List<Peripheral> connectingPeripherals = new ArrayList<>();
    private final List<Callback> callbacks = new ArrayList<>();
    private final List<BluetoothDeviceExplorer> bluetoothDeviceExplorers = new ArrayList<>();
    private final BluetoothStateReceiver bluetoothStateReceiver = new BluetoothStateReceiver();
    private final Handler handler = new Handler();
    private BluetoothPeripheralFactory bluetoothPeripheralFactory;

    protected BluetoothPeripheralManager() {
    }

    public static BluetoothPeripheralManager getInstance() {
        return instance;
    }

    public void init(Context context, BluetoothPeripheralFactory bluetoothPeripheralFactory) {
        this.applicationContext = context.getApplicationContext();
        this.bluetoothPeripheralFactory = bluetoothPeripheralFactory;
        this.bluetoothStateReceiver.registerTo(applicationContext, this);
    }

    public BluetoothPeripheral createBluetoothPeripheral(BluetoothDevice bluetoothDevice) {
        if (bluetoothPeripheralFactory == null) {
            throw new RuntimeException("init method must be called");
        }
        return bluetoothPeripheralFactory.create(applicationContext, bluetoothDevice);
    }

    @Override
    public PeripheralManager connect(PeripheralType peripheralType) {
        return connect(Collections.singletonList(peripheralType));
    }

    @Override
    public PeripheralManager connect(List<PeripheralType> peripheralTypes) {
        for (PeripheralType peripheralType : peripheralTypes) {
            connectOnlyOne(Collections.singletonList(peripheralType));
        }
        return this;
    }

    @Override
    public PeripheralManager connectOnlyOne(List<PeripheralType> peripheralTypes) {
        for (Peripheral connectedPeripheral : connectedPeripherals) {
            for (PeripheralType peripheralType : peripheralTypes) {
                if (peripheralType.include(connectedPeripheral)) {
                    handler.post(() -> {
                        callbackManager.onDiscovered(connectedPeripheral);
                        callbackManager.onConnected(connectedPeripheral);
                    });
                    return this;
                }
            }
        }

        BluetoothDeviceExplorer idleBluetoothDeviceExplorer = null;
        for (BluetoothDeviceExplorer bluetoothDeviceExplorer : bluetoothDeviceExplorers) {
            if (bluetoothDeviceExplorer.equalsTargets(peripheralTypes)
                    || bluetoothDeviceExplorer.isCanceledExploreDevice()) {
                idleBluetoothDeviceExplorer = bluetoothDeviceExplorer;
                break;
            }
        }

        if (idleBluetoothDeviceExplorer == null) {
            idleBluetoothDeviceExplorer = new BluetoothDeviceExplorer();
            bluetoothDeviceExplorers.add(idleBluetoothDeviceExplorer);
        }

        idleBluetoothDeviceExplorer.startExploreDevice(peripheralTypes, this);
        return this;
    }

    @Override
    public void stopConnect(PeripheralType peripheralType) {
        stopConnect(Collections.singletonList(peripheralType));
    }

    @Override
    public void stopConnect(List<PeripheralType> peripheralTypes) {
        for (BluetoothDeviceExplorer bluetoothDeviceExplorer : bluetoothDeviceExplorers) {
            if (bluetoothDeviceExplorer.equalsTargets(peripheralTypes)) {
                bluetoothDeviceExplorer.stopExploreDevice();
            } else {
                for (PeripheralType peripheralType : peripheralTypes) {
                    bluetoothDeviceExplorer.removeTarget(peripheralType);
                }
            }
        }
    }

    @Override
    public void stopConnectAll() {
        for (BluetoothDeviceExplorer bluetoothDeviceExplorer : bluetoothDeviceExplorers) {
            bluetoothDeviceExplorer.stopExploreDevice();
        }
        bluetoothDeviceExplorers.clear();
    }

    @Override
    public void disconnect(PeripheralType peripheralType) {
        disconnect(Collections.singletonList(peripheralType));
    }

    @Override
    public void disconnect(List<PeripheralType> peripheralTypes) {
        stopConnect(peripheralTypes);

        for (Peripheral connectedPeripheral : connectedPeripherals) {
            for (PeripheralType peripheralType : peripheralTypes) {
                if (peripheralType.include(connectedPeripheral)) {
                    connectedPeripheral.disconnect();
                    connectedPeripherals.remove(connectedPeripheral);
                    break;
                }
            }
        }
    }

    @Override
    public void disconnectAll() {
        stopConnectAll();
        for (Peripheral connectedPeripheral : connectedPeripherals) {
            connectedPeripheral.disconnect();
        }
        connectedPeripherals.clear();
    }

    @Override
    public void addCallback(Callback callback) {
        if (callbacks.contains(callback)) return;
        callbacks.add(callback);
    }

    @Override
    public void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    @Nullable
    @Override
    public Peripheral getCurrentPeripheral(List<PeripheralType> peripheralTypes) {
        List<Peripheral> connectedPeripherals = getConnectedPeripherals(peripheralTypes);
        return getLastPeripheral(connectedPeripherals);
    }

    @Nullable
    @Override
    public Peripheral getCurrentPeripheral() {
        return getLastPeripheral(connectedPeripherals);
    }

    private Peripheral getLastPeripheral(List<Peripheral> peripherals) {
        return peripherals.isEmpty() ? null : peripherals.get(peripherals.size() - 1);
    }

    @Override
    public List<Peripheral> getConnectedPeripherals(List<PeripheralType> peripheralTypes) {
        List<Peripheral> connectedPeripheralsWithTypes = new ArrayList<>(peripheralTypes.size());
        for (Peripheral connectedPeripheral : connectedPeripherals) {
            for (PeripheralType peripheralType : peripheralTypes) {
                if (peripheralType.include(connectedPeripheral)
                        && !connectedPeripheralsWithTypes.contains(connectedPeripheral)) {
                    connectedPeripheralsWithTypes.add(connectedPeripheral);
                    break;
                }
            }
        }
        return connectedPeripheralsWithTypes;
    }

    @Override
    public List<Peripheral> getConnectedPeripherals() {
        return new ArrayList<>(connectedPeripherals);
    }

    @Override
    public void onEnabled(boolean enabled) {
        if (enabled) {
            for (BluetoothDeviceExplorer bluetoothDeviceExplorer : bluetoothDeviceExplorers) {
                bluetoothDeviceExplorer.retryExploreDevice();
            }
        } else {
            for (Peripheral connectedPeripheral : connectedPeripherals) {
                connectedPeripheral.disconnect();
            }
            connectedPeripherals.clear();
        }
    }

    @Override
    public void onDeviceExplored(BluetoothDevice bluetoothDevice) {
        Peripheral peripheral = createBluetoothPeripheral(bluetoothDevice);
        if (connectingPeripherals.contains(peripheral)) return;
        if (connectedPeripherals.contains(peripheral)) return;
        callbackManager.onDiscovered(peripheral);
        peripheral.connect(this);
        connectingPeripherals.add(peripheral);
    }

    @Override
    public void onConnected(Peripheral peripheral) {
        connectingPeripherals.remove(peripheral);
        callbackManager.onConnected(peripheral);
    }

    @Override
    public void onDisconnected(Peripheral peripheral) {
        callbackManager.onDisconnected(peripheral);
    }

    @Override
    public void onConnectFailed(Peripheral peripheral) {
        Logger.d("BluetoothPeripheralManager: onConnectFailed " + peripheral);
        connectingPeripherals.remove(peripheral);
    }

    private final Callback callbackManager = new Callback() {
        @Override
        public void onDiscovered(@NonNull Peripheral peripheral) {
            Logger.d("BluetoothPeripheralManager: onDiscovered " + peripheral);
            for (Callback callback : callbacks) {
                callback.onDiscovered(peripheral);
            }
        }

        @Override
        public void onConnected(@NonNull Peripheral peripheral) {
            Logger.d("BluetoothPeripheralManager: onConnected " + peripheral);
            if (!connectedPeripherals.contains(peripheral)) {
                connectedPeripherals.add(peripheral);
            }
            for (Callback callback : callbacks) {
                callback.onConnected(peripheral);
            }
        }

        @Override
        public void onDisconnected(@NonNull Peripheral peripheral) {
            Logger.d("BluetoothPeripheralManager: onDisconnected " + peripheral);
            connectedPeripherals.remove(peripheral);
            for (Callback callback : callbacks) {
                callback.onDisconnected(peripheral);
            }
        }
    };
}
