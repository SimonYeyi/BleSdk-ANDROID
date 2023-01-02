package sm.peripheral;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * 设备实体。包含基本信息，以及处理连接、通讯
 *
 * @see #connect(ConnectCallback)
 * @see #disconnect()
 * @see #set(DataPacket) 用于指令控制, 一般会委托给controller{@link #createController()}
 * @see #setMessenger(Messenger)  用于指令应答
 */
public abstract class Peripheral {
    protected final String name;
    protected final String address;
    protected Messenger messenger;
    private PeripheralController controller;

    public Peripheral(String name, String address) {
        this.name = name;
        this.address = address;
    }

    /**
     * 连接设备
     *
     * @param callback 连接回调
     */
    public abstract void connect(ConnectCallback callback);

    /**
     * 断开连接
     */
    public abstract void disconnect();

    /**
     * 设置指令
     *
     * @param command 指令
     */
    public abstract void set(DataPacket command);

    /**
     * 创建控制器
     *
     * @return 控制器
     */
    protected abstract PeripheralController createController();

    /**
     * 获取控制器
     *
     * @return 控制器
     */
    public PeripheralController getController() {
        if (controller == null) controller = createController();
        return controller;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public final String getName() {
        return name;
    }

    /**
     * 获取地址
     *
     * @return 地址
     */
    public final String getAddress() {
        return address;
    }

    /**
     * 设置信使，接收应答
     *
     * @param messenger 信使
     */
    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peripheral that = (Peripheral) o;
        return Objects.equals(name, that.name) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @NonNull
    @Override
    public String toString() {
        return "Peripheral{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    /**
     * 连接回调
     */
    public interface ConnectCallback {
        /**
         * 设备已连接
         *
         * @param peripheral 连接的设备
         */
        void onConnected(Peripheral peripheral);

        /**
         * 设备已断开
         *
         * @param peripheral 断开的设备
         */
        void onDisconnected(Peripheral peripheral);

        /**
         * 设备连接失败
         *
         * @param peripheral 连接失败的设备
         */
        void onConnectFailed(Peripheral peripheral);
    }

    /**
     * 信使
     */
    public interface Messenger {
        /**
         * 收到应答
         *
         * @param data 应答数据
         */
        void reply(byte[] data);
    }
}
