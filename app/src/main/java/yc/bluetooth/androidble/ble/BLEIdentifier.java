package yc.bluetooth.androidble.ble;


import android.util.Log;

import yc.bluetooth.androidble.util.LogX;

public class BLEIdentifier {

    public interface OnIdentifyResultListener {
        void onIdentifySuccess();

        void onIdentifyFail();
    }

    private static final String TAG = "BLEIdentifier";

    private String latestReceivedData;
    private BLEMessageSender bleMessageSender;
    private OnIdentifyResultListener onIdentifyResultListener;

    public BLEIdentifier(BLEMessageSender bleMessageSender) {
        this.bleMessageSender = bleMessageSender;
    }

    public void startIdentify(OnIdentifyResultListener onIdentifyResultListener) {
        this.onIdentifyResultListener = onIdentifyResultListener;

        Thread identifyThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                LogX.d(TAG, "Thread Id = " + Thread.currentThread().getId());
                bleMessageSender.sendGetTime();

//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendGetTime();
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendPing();
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendPing();
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendPing();
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendGetTime();
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendGetDevice((byte) 0);
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendGetDevice((byte) 0x01);
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
//
//                try {
//                    Thread.sleep(100);
//                    bleMessageSender.sendGetDevice((byte) 0x02);
//                } catch (Exception ex) {
//                    LogX.e("identifyThread", ex.getMessage());
//                }
            }
        });
        identifyThread.start();


        this.onIdentifyResultListener.onIdentifySuccess();
    }

    public String getLatestReceivedData() {
        return latestReceivedData;
    }

    public void setLatestReceivedData(String latestReceivedData) {
        this.latestReceivedData = latestReceivedData;
    }
}
