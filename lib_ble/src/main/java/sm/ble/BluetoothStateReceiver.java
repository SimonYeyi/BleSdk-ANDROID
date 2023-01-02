package sm.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothStateReceiver extends BroadcastReceiver {
    private OnBluetoothStateListener listener;
    private boolean isRegistered = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener == null) return;
        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
        switch (blueState) {
            case BluetoothAdapter.STATE_OFF:
                listener.onEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                listener.onEnabled(true);
                break;
            default:
                break;
        }
    }

    public void registerTo(Context context, OnBluetoothStateListener listener) {
        registerTo(context, false, listener);
    }

    public void registerTo(Context context, boolean callbackCurrentState, OnBluetoothStateListener listener) {
        this.listener = listener;
        if (!isRegistered) {
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(this, intentFilter);
            isRegistered = true;
        }
        if (callbackCurrentState) {
            listener.onEnabled(BluetoothAdapter.getDefaultAdapter().isEnabled());
        }
    }

    public void unregisterFrom(Context context) {
        if (!isRegistered) return;
        try {
            context.unregisterReceiver(this);
        } catch (Exception ignore) {
        }
        isRegistered = false;
        listener = null;
    }

    public interface OnBluetoothStateListener {
        void onEnabled(boolean enabled);
    }
}
