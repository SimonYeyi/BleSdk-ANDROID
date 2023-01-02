package sm.peripheral;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface PeripheralManager {

    /**
     * 连接一类设备
     *
     * @param peripheralType 设备类型
     * @return PeripheralManager
     */
    PeripheralManager connect(PeripheralType peripheralType);

    /**
     * 连接多个设备
     *
     * @param peripheralTypes 设备类型
     * @return PeripheralManager
     */
    PeripheralManager connect(List<PeripheralType> peripheralTypes);

    /**
     * 连接其中一个设备
     *
     * @param peripheralTypes 设备类型
     * @return PeripheralManager
     */
    PeripheralManager connectOnlyOne(List<PeripheralType> peripheralTypes);

    /**
     * 停止一类设备连接
     *
     * @param peripheralType 设备类型
     */
    void stopConnect(PeripheralType peripheralType);

    /**
     * 停止多个设备连接
     *
     * @param peripheralTypes 设备类型
     */
    void stopConnect(List<PeripheralType> peripheralTypes);

    /**
     * 停止所有设备连接
     */
    void stopConnectAll();

    /**
     * 断开一类设备连接
     *
     * @param peripheralType 设备类型
     */
    void disconnect(PeripheralType peripheralType);

    /**
     * 断开多个设备连接
     *
     * @param peripheralTypes 设备类型
     */
    void disconnect(List<PeripheralType> peripheralTypes);

    /**
     * 断开所有设备连接
     */
    void disconnectAll();

    /**
     * 添加连接回调
     */
    void addCallback(Callback callback);

    /**
     * 移除连接回调
     */
    void removeCallback(Callback callback);

    /**
     * 获取其中一个连接的设备
     *
     * @param peripheralTypes 设备类型
     * @return 连接的设备。不存在时返回null
     */
    @Nullable
    Peripheral getCurrentPeripheral(List<PeripheralType> peripheralTypes);

    /**
     * 获取最后一个连接的设备
     *
     * @return 连接的设备。不存在时返回null
     */
    @Nullable
    Peripheral getCurrentPeripheral();

    /**
     * 获取多个连接的设备
     *
     * @param peripheralTypes 设备类型
     * @return 连接的设备列表
     */
    List<Peripheral> getConnectedPeripherals(List<PeripheralType> peripheralTypes);

    /**
     * 获取所有连接的设备
     *
     * @return 连接的设备列表
     */
    List<Peripheral> getConnectedPeripherals();

    /**
     * 连接回调
     */
    interface Callback {

        /**
         * 设备被发现
         *
         * @param peripheral 发现的设备
         */
        void onDiscovered(@NonNull Peripheral peripheral);

        /**
         * 设备已连接
         *
         * @param peripheral 连接的设备
         */
        void onConnected(@NonNull Peripheral peripheral);

        /**
         * 设备已断开
         *
         * @param peripheral 断开的设备
         */
        void onDisconnected(@NonNull Peripheral peripheral);
    }
}
