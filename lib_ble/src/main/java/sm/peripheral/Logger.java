package sm.peripheral;

import android.util.Log;

public class Logger {
    private static final String TAG = "blesdk";

    public static void d(String message) {
        Log.d(TAG, message);
    }

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }
}
