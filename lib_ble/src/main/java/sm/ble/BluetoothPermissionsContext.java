package sm.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * 蓝牙权限处理
 */
public class BluetoothPermissionsContext extends Fragment {
    @SuppressLint("AnnotateVersionCheck")
    private static final boolean BUILD_VERSION_SDK_INT_GT_30 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

    private static final String[] BLUETOOTH_PERMISSIONS =
            BUILD_VERSION_SDK_INT_GT_30 ?
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT
                    } :
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    };
    public static final int FINISH_TYPE_NO_PERMISSION = 0;
    public static final int FINISH_TYPE_LOCATION_SERVICE_DISABLE = 1;
    public static final int FINISH_TYPE_BLUETOOTH_DISABLE = 2;
    private static final int REQUEST_CODE_PERMISSION = 1023;
    private static final int REQUEST_CODE_ENABLE_LOCATION_SERVICE = 1024;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1025;
    private OnPermissionsGranted onPermissionsGranted;
    private final BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private boolean isPermanentlyDenied;

    /**
     * 请求权限
     *
     * @param activity             当前activity
     * @param onPermissionsGranted 权限通过监听
     */
    public void request(FragmentActivity activity, OnPermissionsGranted onPermissionsGranted) {
        this.onPermissionsGranted = onPermissionsGranted;
        activity.getSupportFragmentManager().beginTransaction().add(this, getClass().getSimpleName()).commitAllowingStateLoss();
    }

    /**
     * 请求权限
     *
     * @param fragment             当前fragment
     * @param onPermissionsGranted 权限通过监听
     */
    public void request(Fragment fragment, OnPermissionsGranted onPermissionsGranted) {
        this.onPermissionsGranted = onPermissionsGranted;
        fragment.getChildFragmentManager().beginTransaction().add(this, getClass().getSimpleName()).commitAllowingStateLoss();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        request();
    }

    private void request() {
        if (checkSelfPermission()) {
            onBluetoothPermissionGranted();
        } else {
            requestEnableBluetoothPermission();
        }
    }

    private boolean checkSelfPermission() {
        return ActivityCompat.checkSelfPermission(getContext(), BLUETOOTH_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkAllPermissions() {
        return checkSelfPermission() && (BUILD_VERSION_SDK_INT_GT_30 || isLocationServiceEnabled()) && bluetooth.isEnabled();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.length == BLUETOOTH_PERMISSIONS.length) {
            final boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                onBluetoothPermissionGranted();
            } else if (!shouldShowRequestPermissionRationale(permissions[0])) {
                this.isPermanentlyDenied = true;
                onBluetoothPermissionPermanentlyDenied(permissions);
            } else {
                finish(FINISH_TYPE_NO_PERMISSION);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isPermanentlyDenied) {
            if (checkSelfPermission()) {
                isPermanentlyDenied = false;
                onBluetoothPermissionGranted();
            } else {
                finish(FINISH_TYPE_NO_PERMISSION);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_LOCATION_SERVICE) {
            if (isLocationServiceEnabled()) {
                enableBluetooth();
            } else {
                finish(FINISH_TYPE_LOCATION_SERVICE_DISABLE);
            }
        } else if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (bluetooth.isEnabled()) {
                onPermissionsGranted.onPermissionsGranted();
            } else {
                finish(FINISH_TYPE_BLUETOOTH_DISABLE);
            }
        }
    }

    /**
     * 请求开启蓝牙权限
     */
    protected void requestEnableBluetoothPermission() {
        requestPermissions(BLUETOOTH_PERMISSIONS, REQUEST_CODE_PERMISSION);
    }

    /**
     * 请求开启定位服务
     */
    protected void requestEnabledLocationService() {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, REQUEST_CODE_ENABLE_LOCATION_SERVICE);
    }

    /**
     * 请求开启蓝牙
     *
     * @param action intent action.
     *               default use {@link BluetoothAdapter#ACTION_REQUEST_ENABLE},
     *               you can use {@link android.provider.Settings#ACTION_BLUETOOTH_SETTINGS}
     */
    protected void requestEnableBluetooth(String action) {
        Intent intent = new Intent(action);
        startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    /**
     * 蓝牙权限被永久拒绝
     */
    protected void onBluetoothPermissionPermanentlyDenied(String[] permissions) {
        finish(FINISH_TYPE_NO_PERMISSION);
    }

    /**
     * 权限流被中断，默认会销毁当前页面
     *
     * @param finishType 中断原因
     * @see #FINISH_TYPE_NO_PERMISSION
     * @see #FINISH_TYPE_LOCATION_SERVICE_DISABLE
     * @see #FINISH_TYPE_BLUETOOTH_DISABLE
     */
    protected void finish(int finishType) {
        getActivity().finish();
    }

    private void onBluetoothPermissionGranted() {
        if (BUILD_VERSION_SDK_INT_GT_30 || isLocationServiceEnabled()) {
            enableBluetooth();
        } else {
            requestEnabledLocationService();
        }
    }

    private void enableBluetooth() {
        if (bluetooth.isEnabled()) {
            onPermissionsGranted.onPermissionsGranted();
        } else {
            requestEnableBluetooth(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
    }

    private boolean isLocationServiceEnabled() {
        boolean gpsLocationEnabled = false;
        boolean networkLocationEnabled = false;
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            gpsLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return gpsLocationEnabled || networkLocationEnabled;
    }

    public interface OnPermissionsGranted {
        /**
         * 权限申请通过
         */
        void onPermissionsGranted();
    }
}
