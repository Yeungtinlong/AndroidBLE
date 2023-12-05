package yc.bluetooth.androidble.ble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yc.bluetooth.androidble.common.CallbackValue;

public class GlobalConfigs {

    public static final String SERVICE_UUID_JDY = "0000ffe0-0000-1000-8000-00805f9b34fb";  //蓝牙通讯服务
    public static final String READ_UUID_JDY = "0000ffe1-0000-1000-8000-00805f9b34fb";  //读特征
    public static final String WRITE_UUID_JDY = "0000ffe2-0000-1000-8000-00805f9b34fb";  //写特征

    public static final String SERVICE_UUID_OS = "0000fff0-0000-1000-8000-00805f9b34fb";  //蓝牙通讯服务
    public static final String READ_UUID_OS = "0000fff1-0000-1000-8000-00805f9b34fb";  //写特征
    public static final String WRITE_UUID_OS = "0000fff2-0000-1000-8000-00805f9b34fb";  //写特征

    public static final Map<String, List<String>> serviceCharacteristicsMap = new HashMap<String, List<String>>() {{
        this.put(SERVICE_UUID_JDY, new ArrayList<String>() {{
            this.add(READ_UUID_JDY);
            this.add(WRITE_UUID_JDY);
        }});
        this.put(SERVICE_UUID_OS, new ArrayList<String>() {{
            this.add(READ_UUID_OS);
            this.add(WRITE_UUID_OS);
        }});
    }};

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
