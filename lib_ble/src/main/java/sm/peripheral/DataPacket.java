package sm.peripheral;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Arrays;

public abstract class DataPacket {
    protected final byte[] data;

    protected DataPacket(byte[] data) {
        this.data = data;
    }

    public DataPacket(String[] hex) {
        data = new byte[hex.length];
        for (int i = 0; i < hex.length; i++) {
            data[i] = int2byte(Integer.parseInt(hex[i], 16));
        }
    }

    public DataPacket(String hex) {
        this.data = hex2bytes(hex);
    }

    public byte[] getData() {
        return data;
    }

    public String getDataHex() {
        return bytes2hex(data, " ");
    }

    public abstract byte getType();

    private byte[] hex2bytes(String hex) {
        hex = hex.replace(" ", "");
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytes2hex(byte[] data, String separator) {
        StringBuilder builder = new StringBuilder();
        for (byte datum : data) {
            String s = byte2hex(datum);
            builder.append(s);
            builder.append(separator);
        }
        return builder.toString();
    }

    protected static String byte2hex(byte value) {
        int newValue = value;
        if (newValue < 0) newValue += 256;
        return int2hex(newValue);
    }

    protected static String int2hex(int value) {
        return int2hex(value, (value < 256 ? 2 : 4));
    }

    protected static String int2hex(int value, int X) {
        return String.format("%0" + X + "X", value);
    }

    protected static int hex2int(String hex) {
        return new BigInteger(hex, 16).intValue();
    }

    protected static byte int2byte(int value) {
        return (byte) (value & 0xFF);
    }

    protected static int byte2int(byte value) {
        return value & 0xFF;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        DataPacket that = (DataPacket) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getDataHex();
    }
}
