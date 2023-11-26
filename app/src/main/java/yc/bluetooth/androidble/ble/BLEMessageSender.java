package yc.bluetooth.androidble.ble;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import yc.bluetooth.androidble.util.TypeConversion;

public final class BLEMessageSender {

    private final static String TAG = "BLEMessageSender";

    private final static int SEND_INTERVAL_MILLIS = 200;

    private final static byte PACK_PREFIX = (byte) 0xfb;
    private final static byte PACK_POSTFIX = (byte) 0xbf;

    public final static byte GET_DEVICE_INFO_MODE_ID = 0x01;
    public final static byte GET_DEVICE_INFO_MODE_VERSION = 0x02;

    private final static int MAX_TIMER_SECONDS = 1800;

    private final static int SET_LIGHTS_CH_OFFSET = 3;
    private final static int SET_FREQUENCIES_CH_OFFSET = 9;

    private Queue<byte[]> messageQueue = new LinkedList<>();

    private final BLEManager bleManager;
    private Handler handler;

    public BLEMessageSender(BLEManager bleManager, Handler handler) {
        this.bleManager = bleManager;
        this.handler = handler;

        startMessageThread();
    }

    private void startMessageThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                while (messageQueue.size() == 0) {
                    Thread.yield();
                }

                synchronized (BLEMessageSender.this) {
                    sendMessageInternal(messageQueue.poll());
                }

                try {
                    Thread.sleep(SEND_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    private boolean sendMessage(byte[] data) {
        if (bleManager == null) {
            Log.e(TAG, "sendMessage(byte[])-->bleManager== null");
            return false;
        }

        synchronized (this) {
            messageQueue.offer(data);
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    private void sendMessageInternal(byte[] data) {
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

    public boolean sendSetLight(byte light, int ch) {
        byte[] data = new byte[3];
        data[0] = 0x53;
        data[1] = (byte) (ch + SET_LIGHTS_CH_OFFSET);
        data[2] = light;

        return sendMessage(data);
    }

    public boolean sendSetLights(byte[] lights) {
        boolean result = true;
        for (int i = 0; i < lights.length; i++) {
            result &= sendSetLight(lights[i], i);
        }
        return result;
    }

//    public boolean sendGetLight(int ch) {
//        byte[] data = new byte[2];
//        data[0] = 0x52;
//        data[1] = (byte) (ch + 2);
//
//        return sendMessage(data);
//    }
//
//    public boolean sendGetLights() {
//        boolean result = true;
//        for (int i = 0; i < 6; i++) {
//            result &= sendGetLight(i);
//        }
//        return result;
//    }

    public boolean sendSetFrequencies(int[] frequencies, byte[] frequencies_sw) {
        boolean result = true;
        for (int i = 0; i < frequencies.length; i++) {
            result &= sendSetFrequency(frequencies[i], frequencies_sw[i], i);
        }
        return result;
    }

    public boolean sendSetFrequency(int frequency, byte frequency_sw, int ch) {
        int freq = frequency;
        if (frequency_sw == 0)
            freq = 0;

        byte[] freqData = new byte[4];
        freqData[0] = 0x53;
        freqData[1] = (byte) (ch + SET_FREQUENCIES_CH_OFFSET);
        freqData[2] = (byte) ((freq >> 8) & 0xFF);
        freqData[3] = (byte) (freq & 0xFF);

        return sendMessage(freqData);
    }
}
