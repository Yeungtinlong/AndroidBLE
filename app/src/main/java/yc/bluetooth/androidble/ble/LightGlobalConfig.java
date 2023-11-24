package yc.bluetooth.androidble.ble;

public class LightGlobalConfig {

    public static CallbackValue<Integer> globalTimerSet = new CallbackValue<>(0);
    public static CallbackValue<Boolean> isCountingDown = new CallbackValue<>(false);
    public final static byte[] globalFrequencies = new byte[6];
    public final static byte[] globalFrequencySwitches = new byte[6];
    public final static byte[] globalLights = new byte[6];

}
