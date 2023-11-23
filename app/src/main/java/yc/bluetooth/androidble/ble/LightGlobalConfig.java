package yc.bluetooth.androidble.ble;

public class LightGlobalConfig {

    public static CallbackValue<Integer> globalTimerSet = new CallbackValue<>(900);
    public final static byte[] globalFrequencies = new byte[6];
    public final static byte[] globalFrequencySwitches = new byte[6];
    public final static byte[] globalLights = new byte[6];

}
