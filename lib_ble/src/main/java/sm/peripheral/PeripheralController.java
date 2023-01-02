package sm.peripheral;

import androidx.annotation.Nullable;

import java.util.Date;

public interface PeripheralController {

    /**
     * 初始化
     *
     * @param initCallback 结果
     */
    void init(PeripheralCallback<PeripheralInitResult> initCallback);

    /**
     * 设置时间
     *
     * @param setDateCallback 结果
     */
    void setDate(Date date, @Nullable PeripheralCallback<Boolean> setDateCallback);

    /**
     * 设置为当前时间
     *
     * @param setDateCallback 结果
     */
    void setCurrentDate(@Nullable PeripheralCallback<Boolean> setDateCallback);

    /**
     * 获取固件版本
     *
     * @param getFirmwareVersionCallback 结果
     */
    void getFirmwareVersion(PeripheralCallback<PeripheralFirmwareVersion> getFirmwareVersionCallback);

    /**
     * 获取电量
     *
     * @param getPowerCallback 结果
     */
    void getPower(PeripheralCallback<PeripheralPower> getPowerCallback);

    /**
     * 设置回调
     *
     * @param <T> 结果
     */
    interface PeripheralCallback<T> {
        /**
         * 设置回调
         *
         * @param result 结果
         */
        void onCall(T result);
    }
}
