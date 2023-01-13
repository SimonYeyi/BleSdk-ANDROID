# lib_ble

一个为android-ble设备建立连接、通讯而设计的库

# Demo

参照项目下的app模块

# 特性

1、**内置蓝牙相关权限申请（适配Android12-API31）**

2、**内置位置服务开关、蓝牙开关设置页面导航**

3、**支持自定义权限申请提示对话框、位置服务开关提示对话框、蓝牙开关提示对话框**

4、**支持多设备同时连接、通讯**

5、**指令格式化Log打印，便于排查、发现问题**

# 如何使用

**1. 项目下build.gradle中配置仓库：**

````gradle
allprojects {
    repositories {
        //...
        mavenLocal()
    }
}
````

**2. 项目下app的build.gradle中依赖：**

````gradle
dependencies {
    //...
    implementation 'com.sm:ble:1.0.0'
}
````

**3. 在Application的onCreate()中初始化：**

````java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothPeripheralManager.getInstance().init(context, new SupportBluetoothPeripheralFactory());
    }
}
````

**4. 页面级连接、通讯：**

````java
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //...
        new BluetoothPeripheralFragment()
                //配置权限申请流程
                .setPermissionsContext(new MyPermissionsContext())
                //设置需要连接的设备类型，此处使用温度计
                .setExplorePeripheralType(SupportPeripheralTypes.thermometers())
                //页面销毁时不断开连接
                .disableAutoDisconnect()
                //应用以上配置，开始连接
                .connect(this)
                //连接回调
                .setCallback(new PeripheralManager.Callback() {
                    @Override
                    public void onDiscovered(@NonNull Peripheral peripheral) {
                        //发现设备
                    }

                    @Override
                    public void onConnected(@NonNull Peripheral peripheral) {
                        //设备已连接
                        //由于setExplorePeripheralType(SupportPeripheralTypes.thermometers())
                        //所以peripheral必然是BluetoothThermometer
                        BluetoothThermometer thermometer = ((BluetoothThermometer) peripheral);
                        ThermometerController controller = thermometer.getController();
                        //通过peripheral.controller进行通讯，先调用设备初始化方法，不可重复调用
                        controller.init(initResult -> {
                            //之后,可使用controller与设备正常通讯
                        });
                    }

                    @Override
                    public void onDisconnected(@NonNull Peripheral peripheral) {
                        //连接已断开
                    }
                });
    }
}
````

**5. 蓝牙权限处理流：**

````java
public class MyPermissionsContext extends BluetoothPermissionsContext {

    @Override
    protected void requestEnableBluetoothPermission() {
        //可插入自定义蓝牙权限申请提示框
        //BleUtils.showRequestBluetoothPermissionsTipDialog(getActivity(), v -> {
        super.requestEnableBluetoothPermission();
        //});
    }

    @Override
    protected void onBluetoothPermissionPermanentlyDenied(String[] permissions) {
        //可处理蓝牙权限被永久拒绝的情况
        //DialogUtils.showNoPermissionDialog(getContext(), Arrays.asList(permissions), true);
    }

    @Override
    protected void requestEnabledLocationService() {
        //可插入自定义打开位置服务提示框
        //BleUtils.showEnableLocationServiceTipDialog(getActivity(), v -> {
        super.requestEnabledLocationService();
        //});
    }

    @Override
    protected void requestEnableBluetooth(String action) {
        //可插入自定义打开蓝牙开关提示框
        //BleUtils.showEnableBluetoothTipDialog(getContext(), (dialog, which) -> {
        super.requestEnableBluetooth(Settings.ACTION_BLUETOOTH_SETTINGS);
        //});
    }
}
````

**6. 页面外连接、通讯（需提前自行处理蓝牙权限申请，可使用BluetoothPermissionsContext）：**

````java
public class Xxx implements PeripheralManager.Callback {

    public void init() {
        BluetoothPeripheralManager.getInstance().addCallback(this);
    }

    public void connect() {
        BluetoothPeripheralManager
                .getInstance()
                .connectOnlyOne(SupportPeripheralTypes.thermometers());
    }

    public void disconnect() {
        BluetoothPeripheralManager.getInstance().disconnectAll();
    }

    public void destroy() {
        disconnect();
        BluetoothPeripheralManager.getInstance().removeCallback(this);
    }

    @Override
    public void onDiscovered(@NonNull Peripheral peripheral) {

    }

    @Override
    public void onConnected(@NonNull Peripheral peripheral) {

    }

    @Override
    public void onDisconnected(@NonNull Peripheral peripheral) {
        
    }
}
````

**7. 蓝牙权限申请流程图（BluetoothPermissionsContext）：**

<img src="/蓝牙权限申请流程图.jpg" width="900px"/>

**8. 蓝牙设备连接时序图：**

<img src="/蓝牙设备连接时序图.jpg" width="900px"/>

**9. 蓝牙设备通讯类图：**

<img src="/蓝牙设备通讯类图.jpg" width="900px"/>

# 进一步使用，请留意api文档注释或研读源码
