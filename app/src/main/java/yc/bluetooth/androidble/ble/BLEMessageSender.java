package yc.bluetooth.androidble.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public final class BLEMessageSender {

    private final static byte PACK_PREFIX = (byte) 0xfb;
    private final static byte PACK_POSTFIX = (byte) 0xbf;

    private final static int MAX_TIMER_SECONDS = 1800;


    private final BluetoothGatt bluetoothGatt;
    private final BluetoothGattCharacteristic writeCharacteristic;

    public BLEMessageSender(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic writeCharacteristic) {
        this.bluetoothGatt = bluetoothGatt;
        this.writeCharacteristic = writeCharacteristic;
    }

    @SuppressLint("MissingPermission")
    private boolean sendMessage(byte[] data) {
        byte[] buffer = new byte[data.length + 3];
        buffer[0] = PACK_PREFIX;

        byte check = 0;
        for (int i = 0; i < data.length; i++) {
            buffer[i + 1] = data[i];
            check = (byte) (check ^ data[i]);
        }

        buffer[data.length + 1] = check;
        buffer[buffer.length] = PACK_POSTFIX;

        writeCharacteristic.setValue(buffer);
        return bluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    public void sendSetTime(int seconds) {
        if (seconds > MAX_TIMER_SECONDS) {
            Log.e("sendSetTime", "不能设置时间大于1800s，或小于0s");
            return;
        }

        byte[] data = new byte[4];
        data[0] = 0x53;
        data[1] = 0x0f;
        data[2] = (byte) (seconds >> 8);
        data[3] = (byte) (seconds & 0x00FF);

        sendMessage(data);
    }

    public void sendSetLight(byte[] lights, int ch) {
        byte[] data = new byte[3];
        data[0] = 0x53;
        data[1] = (byte) (ch + 2);
        data[2] = lights[ch];

        sendMessage(data);
    }
}
