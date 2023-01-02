package sm.peripheral;

import androidx.annotation.NonNull;

public interface PeripheralFirmwareVersion {

    /**
     * @return 版本。不支持时，返回1
     */
    @NonNull
    String getVersion();
}
