package yc.bluetooth.androidble.ble;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import yc.bluetooth.androidble.util.TypeConversion;

public final class BLEMessageSender {

    private final static String TAG = "BLEMessageSender";

    private final static byte PACK_PREFIX = (byte) 0xfb;
    private final static byte PACK_POSTFIX = (byte) 0xbf;

    private final static int MAX_TIMER_SECONDS = 1800;


    private final BLEManager bleManager;
    private Handler handler;

    public BLEMessageSender(BLEManager bleManager, Handler handler) {
        this.bleManager = bleManager;
        this.handler = handler;
    }

    @SuppressLint("MissingPermission")
    private boolean sendMessage(byte[] data) {
        if (bleManager == null) {
            Log.e(TAG, "sendMessage(byte[])-->bleManager== null");
            return false;
        }

        byte[] buffer = new byte[data.length + 3];
        buffer[0] = PACK_PREFIX;

        byte check = 0;
        for (int i = 0; i < data.length; i++) {
            buffer[i + 1] = data[i];
            check = (byte) (check ^ data[i]);
        }

        buffer[data.length + 1] = check;
        buffer[buffer.length - 1] = PACK_POSTFIX;

        Log.d(TAG, TypeConversion.bytes20xHexString(buffer) + " sent.");

        boolean b = bleManager.getWriteCharacteristic().setValue(buffer);
        Log.d(TAG, "写特征设置值结果：" + b);

        b = bleManager.getBluetoothGatt().writeCharacteristic(bleManager.getWriteCharacteristic());
        Log.d(TAG, "写入特征结果：" + b);

        return true;
    }

    public boolean sendGetDevice(byte mode) {
        byte[] data = new byte[2];
        data[0] = 0x50;
        data[1] = mode;

        return sendMessage(data);
    }

    public boolean sendPing() {
        byte[] data = new byte[2];
        data[0] = 0x51;
        data[1] = 0;

        return sendMessage(data);
    }


    public boolean sendGetTime() {
        byte[] data = new byte[2];
        data[0] = 0x52;
        data[1] = 0;

        return sendMessage(data);
    }


    public boolean sendSetOn() {
        byte[] data = new byte[2];
        data[0] = 0x53;
        data[1] = 0x10;

        return sendMessage(data);
    }

    public boolean sendSetOff() {
        byte[] data = new byte[2];
        data[0] = 0x53;
        data[1] = 0x00;

        return sendMessage(data);
    }

    public boolean sendSetTime(int seconds) {
        if (seconds > MAX_TIMER_SECONDS) {
            Log.e("sendSetTime", "不能设置时间大于1800s，或小于0s");
            return false;
        }

        byte[] data = new byte[4];
        data[0] = 0x53;
        data[1] = 0x0f;
        data[2] = (byte) (seconds >> 8);
        data[3] = (byte) (seconds & 0x00FF);

        return sendMessage(data);
    }

    public boolean sendSetLight(byte[] lights, int ch) {
        byte[] data = new byte[3];
        data[0] = 0x53;
        data[1] = (byte) (ch + 2);
        data[2] = lights[ch];

        return sendMessage(data);
    }

    public boolean sendSetFrequency(byte[] frequencies, byte[] frequencies_sw, int ch) {
        byte freq = frequencies[ch];
        if (frequencies_sw[ch] == 0)
            freq = 0;

        byte[] data = new byte[4];
        data[0] = 0x53;
        data[1] = (byte) (ch + 8);
        data[2] = (byte) (freq >> 8);
        data[3] = (byte) (freq & 0x00FF);

        return sendMessage(data);
    }
}
