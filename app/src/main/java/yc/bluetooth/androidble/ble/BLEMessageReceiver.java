package yc.bluetooth.androidble.ble;

import android.util.Log;

import yc.bluetooth.androidble.util.TypeConversion;

public final class BLEMessageReceiver {

    private final static String TAG = "BLEMessageReceiver";


    private final static byte DATA_PREFIX = (byte) 0xFB;

    private final static byte GET_DEVICE = 0x50;
    private final static byte GET_DEVICE_MODE_ID = 0x01;
    private final static byte GET_DEVICE_MODE_VERSION = 0x02;


    private final static byte PING = 0x51;

    private final static byte GET_TIMER = 0x52;


    public BLEMessageReceiver() {
    }

    public void receiveMessage(byte[] data) {
        if (data == null || data.length == 0) {
            Log.w(TAG, "get null message.");
            return;
        }

        if (data.length < 4) {
            Log.w(TAG, "get invalid message with length " + data.length + " bytes, " + TypeConversion.bytes20xHexString(data));
            return;
        }

        if (data[0] != DATA_PREFIX) {
            Log.w(TAG, "get invalid message = " + TypeConversion.bytes2HexString(data));
            return;
        }

        switch (data[1]) {
            case GET_DEVICE: {

                break;
            }

            case PING: {
                Log.d(TAG, "get ping " + PING + " from remote");
                break;
            }

            case GET_TIMER: {
                Log.d(TAG, "Thread Id = " + Thread.currentThread().getId());
                resolveGetTimer(data);
                break;
            }
        }
    }

    private void resolveGetTimer(byte[] data) {
        int high = (0xFF & data[2]);
        int low = (0xFF & data[3]);
        byte check = data[4];
        byte postfix = data[5];

        int time = (high << 8) | low;

//        Log.d(TAG, "get remaining time " + time + "s.");
        LightGlobalConfig.globalTimerSet.setValue(time);
    }
}
