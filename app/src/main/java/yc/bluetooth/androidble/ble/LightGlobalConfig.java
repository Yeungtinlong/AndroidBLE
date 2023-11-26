package yc.bluetooth.androidble.ble;

import yc.bluetooth.androidble.common.CallbackValue;

public class LightGlobalConfig {

    // 倒计时设定
    public static CallbackValue<Integer> globalTimerSet = new CallbackValue<>(0);
    // 是否正在倒计时
    public static CallbackValue<Boolean> isCountingDown = new CallbackValue<>(false);

    // 设备Id
    public static CallbackValue<String> deviceId = new CallbackValue<>("");
    // 软件Id
    public static CallbackValue<String> softwareVersion = new CallbackValue<>("");
    // 硬件Id
    public static CallbackValue<String> hardwareVersion = new CallbackValue<>("");

    // 各频率灯周期设置
    public final static CallbackValue<int[]> globalFrequencies = new CallbackValue<>(new int[6]);
    // 各频率灯开关
    public final static CallbackValue<byte[]> globalFrequencySwitches = new CallbackValue<>(new byte[6]);
    // 各频率灯强度设置
    public final static CallbackValue<byte[]> globalLights = new CallbackValue<>(new byte[6]);

}
