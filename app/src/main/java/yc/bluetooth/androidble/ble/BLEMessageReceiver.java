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

    private final static byte SET = 0x53;

    private final static byte SET_TIMER = 0x0F;

    private final static int SET_LIGHTS_CH_OFFSET = 3;
    private final static int SET_FREQUENCIES_CH_OFFSET = 9;


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
                resolveGetDeviceInfo(data);
                break;
            }

            case PING: {
                Log.d(TAG, "get ping " + PING + " from remote");
                break;
            }

            case GET_TIMER: {
//                Log.d(TAG, "Thread Id = " + Thread.currentThread().getId());
                resolveGetTimer(data);
                break;
            }

            case SET: {

                resolveSet(data);
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

        Log.d(TAG, "get remaining time " + time + "s.");
        LightGlobalConfig.globalTimerSet.setValue(time);
    }

    private void resolveSet(byte[] data) {
        int stuff = (0xFF & data[2]);
        switch (stuff) {
            case SET_TIMER: {
                resolveSetTimer(data);
                break;
            }
            default: {
                if (SET_LIGHTS_CH_OFFSET <= stuff && stuff <= SET_LIGHTS_CH_OFFSET + 5) {

                    resolveSetLights(data);

                } else if (SET_FREQUENCIES_CH_OFFSET <= stuff && stuff <= SET_FREQUENCIES_CH_OFFSET + 5) {

                    resolveSetFrequencies(data);

                }
                break;
            }
        }
    }

    private void resolveSetTimer(byte[] data) {
        int high = (0xFF & data[3]);
        int low = (0xFF & data[4]);
        byte check = data[5];
        byte postfix = data[6];

        int time = (high << 8) | low;

        Log.d(TAG, "get remaining time " + time + "s.");
        LightGlobalConfig.globalTimerSet.setValue(time);
    }

    private void resolveSetLights(byte[] data) {
        int ch = (0xFF & data[2]) - SET_LIGHTS_CH_OFFSET;
        byte value = (byte) (0xFF & data[3]);
        LightGlobalConfig.globalLights.getValue()[ch] = value;
        LightGlobalConfig.globalLights.notifyChanged();
    }

    private void resolveSetFrequencies(byte[] data) {
        int ch = (0xFF & data[2]) - SET_FREQUENCIES_CH_OFFSET;

        int value = ((0xFF & data[3]) << 8) | (0xFF & data[4]);

        LightGlobalConfig.globalFrequencies.getValue()[ch] = value;
        LightGlobalConfig.globalFrequencies.notifyChanged();
    }

    private void resolveGetDeviceInfo(byte[] data) {
        int mode = (0xFF & data[2]);

        switch (mode) {
            case GET_DEVICE_MODE_ID: {
                int id = (0xFF & data[3]);
                String deviceId = id > 10 ? String.valueOf(id) : "0" + id;
                LightGlobalConfig.deviceId.setValue(deviceId);
                break;
            }
            case GET_DEVICE_MODE_VERSION: {
                int softVer = (0xFF & data[3]);
                int hardVer = (0xFF & data[4]);
                String softwareVersion = softVer > 10 ? String.valueOf(softVer) : "0" + softVer;
                String hardwareVersion = hardVer > 10 ? String.valueOf(hardVer) : "0" + hardVer;
                LightGlobalConfig.softwareVersion.setValue(softwareVersion);
                LightGlobalConfig.hardwareVersion.setValue(hardwareVersion);
                break;
            }

        }
    }
}
