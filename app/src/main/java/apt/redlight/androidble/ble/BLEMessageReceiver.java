package apt.redlight.androidble.ble;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

import apt.redlight.androidble.util.LogX;
import apt.redlight.androidble.util.TypeConversion;

public final class BLEMessageReceiver {

    private final static String TAG = "BLEMessageReceiver";


    private final static byte PACK_PREFIX = (byte) 0xfb;
    private final static byte PACK_POSTFIX = (byte) 0xbf;

    private final static byte GET_DEVICE = 0x50;
    private final static byte GET_DEVICE_MODE_ID = 0x01;
    private final static byte GET_DEVICE_MODE_VERSION = 0x02;


    private final static byte PING = 0x51;

    private final static byte GET_TIMER = 0x52;

    private final static byte SET = 0x53;

    private final static byte SET_TIMER = 0x0F;

    private final static int SET_LIGHTS_CH_OFFSET = 3;
    private final static int SET_FREQUENCIES_CH_OFFSET = 9;

//    private final Queue<Byte> recevieBufferQueue = new ConcurrentLinkedDeque<>();
    private final Queue<Byte> messageQueue = new ConcurrentLinkedDeque<>();
    private final List<Byte> waitingList = new ArrayList<>();
    private boolean isReceivingMessage;
//    private boolean isTransforming;
    private boolean isUnpacking = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BLEMessageReceiver() {
    }

//    private void startTransformQueueThread() {
//        if (isTransforming) return;
//
//        isTransforming = true;
//
//        Thread thread = new Thread(() -> {
//            while (!this.recevieBufferQueue.isEmpty()) {
//                synchronized (BLEMessageReceiver.this) {
//                    this.messageQueue.add(recevieBufferQueue.poll());
//                }
//            }
//            isTransforming = false;
//            startLookMessageQueueThread();
//        });
//        thread.start();
//    }

    private void startLookMessageQueueThread() {
        if (this.isReceivingMessage) return;

        this.isReceivingMessage = true;

        Thread thread = new Thread(() -> {
            while (!this.messageQueue.isEmpty()) {
                Byte bytePtr = this.messageQueue.poll();
                if (bytePtr == null) {
                    LogX.w(TAG, "Get null ptr.");
                    continue;
                }

                byte data = bytePtr;

                /// 收到prefix，开始解密
                if (data == PACK_PREFIX && !this.isUnpacking) {
                    this.isUnpacking = true;
                    this.waitingList.add(data);
                    continue;
                }
                /// 收到重复prefix，放弃解密
                if (data == PACK_PREFIX && this.isUnpacking) {
                    this.isUnpacking = false;
                    this.waitingList.clear();
                    continue;
                }
                /// 正在解密，尝试比对协议长度
                if (this.isUnpacking) {
                    this.waitingList.add(data);

                    byte[] msg = new byte[waitingList.size()];
                    for (int i = 0; i < this.waitingList.size(); i++) {
                        msg[i] = waitingList.get(i);
                    }

                    if (tryUnpackMessage(msg)) {
                        this.waitingList.clear();
                        this.isUnpacking = false;
                    }
                    continue;
                }
            }
            if (!this.waitingList.isEmpty()) {
                byte[] msg = new byte[waitingList.size()];
                for (int i = 0; i < this.waitingList.size(); i++) {
                    msg[i] = waitingList.get(i);
                }
                LogX.w(TAG, "Wait list is not empty, " + TypeConversion.bytes2HexString(msg, msg.length));
            }

//            this.waitingList.clear();
//            this.isUnpacking = false;
            this.isReceivingMessage = false;
        });
        thread.start();
    }

    public void receiveMessage(byte[] data) {
        for (int i = 0; i < data.length; i++) {
//            recevieBufferQueue.add(data[i]);
            this.messageQueue.add(data[i]);
        }
//        startTransformQueueThread();
        startLookMessageQueueThread();
    }

    public boolean tryUnpackMessage(byte[] data) {
        if (data == null || data.length == 0) {
//            LogX.w(TAG, "get null message.");
            return false;
        }

        if (data.length < 4) {
//            LogX.w(TAG, "get invalid message with length " + data.length + " bytes, " + TypeConversion.bytes20xHexString(data));
            return false;
        }

        if (data[0] != PACK_PREFIX) {
//            LogX.w(TAG, "get invalid message = " + TypeConversion.bytes2HexString(data, data.length));
            return false;
        }

        if (data[data.length - 1] != PACK_POSTFIX) {
            return false;
        }

        if (data[1] == GET_DEVICE) {
            return resolveGetDeviceInfo(data);
        }

        if (data[1] == PING && data.length == 4 && data[3] == PACK_POSTFIX) {
            LogX.w(TAG, "PING: " + TypeConversion.bytes2HexString(data, data.length));
            return true;
        }

        if (data[1] == GET_TIMER && data.length == 6 && data[5] == PACK_POSTFIX) {
            LogX.w(TAG, "GET_TIMER: " + TypeConversion.bytes2HexString(data, data.length));
            resolveGetTimer(data);
            return true;
        }

        if (data[1] == SET) {
            return resolveSet(data);
        }

//        switch (data[1]) {
//            // 5 byte
//            case GET_DEVICE: {
//                resolveGetDeviceInfo(data);
//                break;
//            }
//            //
//            case PING: {
//                LogX.d(TAG, "get ping " + PING + " from remote");
//                break;
//            }
//            // 6 byte
//            case GET_TIMER: {
////                LogX.d(TAG, "Thread Id = " + Thread.currentThread().getId());
//                resolveGetTimer(data);
//                break;
//            }
//            //
//            case SET: {
//
//                resolveSet(data);
//                break;
//            }
//        }

        return false;
    }

    private void resolveGetTimer(byte[] data) {
        int high = (0xFF & data[2]);
        int low = (0xFF & data[3]);
        byte check = data[4];
        byte postfix = data[5];

        int time = (high << 8) | low;

        LogX.d(TAG, "get remaining time " + time + "s.");

        mainHandler.post(() -> {
            GlobalConfigs.globalTimerSet.setValue(time);
        });
    }

    private boolean resolveSet(byte[] data) {
        int stuff = (0xFF & data[2]);

        if (stuff == SET_TIMER && data.length == 7 && data[6] == PACK_POSTFIX) {
            LogX.w(TAG, "SET_TIMER: " + TypeConversion.bytes2HexString(data, data.length));
            resolveSetTimer(data);
            return true;
        }

        if (SET_LIGHTS_CH_OFFSET <= stuff && stuff <= SET_LIGHTS_CH_OFFSET + 5 && data.length == 6 && data[5] == PACK_POSTFIX) {
            LogX.w(TAG, "SET_LIGHTS_CH_OFFSET: " + TypeConversion.bytes2HexString(data, data.length));
            resolveSetLights(data);
            return true;
        }

        if (SET_FREQUENCIES_CH_OFFSET <= stuff && stuff <= SET_FREQUENCIES_CH_OFFSET + 5 && data.length == 7 && data[6] == PACK_POSTFIX) {
            LogX.w(TAG, "SET_FREQUENCIES_CH_OFFSET: " + TypeConversion.bytes2HexString(data, data.length));
            resolveSetFrequencies(data);
            return true;
        }

        return false;
    }

    private void resolveSetTimer(byte[] data) {
        int high = (0xFF & data[3]);
        int low = (0xFF & data[4]);
        byte check = data[5];
        byte postfix = data[6];

        int time = (high << 8) | low;

        LogX.d(TAG, "get remaining time " + time + "s.");

        mainHandler.post(() -> {
            GlobalConfigs.globalTimerSet.setValue(time);
        });
    }

    private void resolveSetLights(byte[] data) {
        int ch = (0xFF & data[2]) - SET_LIGHTS_CH_OFFSET;
        byte value = (byte) (0xFF & data[3]);
        mainHandler.post(() -> {
            GlobalConfigs.globalLights.getValue()[ch] = value;
            GlobalConfigs.globalLights.notifyChanged();
        });
    }

    private void resolveSetFrequencies(byte[] data) {
        int ch = (0xFF & data[2]) - SET_FREQUENCIES_CH_OFFSET;

        int value = ((0xFF & data[3]) << 8) | (0xFF & data[4]);
        mainHandler.post(() -> {
            GlobalConfigs.globalFrequencies.getValue()[ch] = value;
            GlobalConfigs.globalFrequencies.notifyChanged();
        });
    }

    private boolean resolveGetDeviceInfo(byte[] data) {
        int mode = (0xFF & data[2]);

        if (mode == GET_DEVICE_MODE_ID && data.length == 6 && data[5] == PACK_POSTFIX) {
            LogX.w(TAG, "GET_DEVICE_MODE_ID: " + TypeConversion.bytes2HexString(data, data.length));
            int id = (0xFF & data[3]);
            String deviceId = id > 10 ? String.valueOf(id) : "0" + id;
            mainHandler.post(() -> GlobalConfigs.deviceId.setValue(deviceId));
            return true;
        }
        if (mode == GET_DEVICE_MODE_VERSION && data.length == 7 && data[6] == PACK_POSTFIX) {
            LogX.w(TAG, "GET_DEVICE_MODE_VERSION: " + TypeConversion.bytes2HexString(data, data.length));
            int softVer = (0xFF & data[3]);
            int hardVer = (0xFF & data[4]);
            String softwareVersion = softVer > 10 ? String.valueOf(softVer) : "0" + softVer;
            String hardwareVersion = hardVer > 10 ? String.valueOf(hardVer) : "0" + hardVer;
            mainHandler.post(() -> {
                GlobalConfigs.softwareVersion.setValue(softwareVersion);
                GlobalConfigs.hardwareVersion.setValue(hardwareVersion);
            });
            return true;
        }

        return false;
    }
}
