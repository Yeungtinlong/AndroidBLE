package yc.bluetooth.androidble.activity;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.GlobalConfigs;
import yc.bluetooth.androidble.common.CallbackValue;
import yc.bluetooth.androidble.ui.SuperSeekbar;

public class PulseActivity extends AppCompatActivity {

    private SuperSeekbar controllerCh0;
    private SuperSeekbar controllerCh1;
    private SuperSeekbar controllerCh2;
    private SuperSeekbar controllerCh3;
    private SuperSeekbar controllerCh4;
    private SuperSeekbar controllerCh5;
    private Button submitBtn;

    private CallbackValue.Action<int[]> onFrequenciesChangedListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransparentStatusBar.setTransparent(this);

        setContentView(R.layout.activity_pulse_settings);

        initViews();

        registerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterListeners();
    }

    private void initViews() {
        controllerCh0 = findViewById(R.id.controller_ch0);
        controllerCh1 = findViewById(R.id.controller_ch1);
        controllerCh2 = findViewById(R.id.controller_ch2);
        controllerCh3 = findViewById(R.id.controller_ch3);
        controllerCh4 = findViewById(R.id.controller_ch4);
        controllerCh5 = findViewById(R.id.controller_ch5);
        submitBtn = findViewById(R.id.submit_btn);
    }

    private void registerListeners() {
        submitBtn.setOnClickListener(v -> {
            sendFrequenciesSettings();
            finish();
        });

//        onFrequenciesChangedListener = frequencies -> {
//            setFrequencies(frequencies);
//        };

//        LightGlobalConfig.globalFrequencies.addOnValueChangeListener(onFrequenciesChangedListener);
    }

    private void unregisterListeners() {
//        LightGlobalConfig.globalFrequencies.removeOnValueChangeListener(onFrequenciesChangedListener);
    }

    private void sendFrequenciesSettings() {
        int[] lights = new int[6];
        lights[0] = (int) controllerCh0.getValue();
        lights[1] = (int) controllerCh1.getValue();
        lights[2] = (int) controllerCh2.getValue();
        lights[3] = (int) controllerCh3.getValue();
        lights[4] = (int) controllerCh4.getValue();
        lights[5] = (int) controllerCh5.getValue();

        byte[] sws = new byte[6];
        sws[0] = (byte) (controllerCh0.getOn() ? 1 : 0);
        sws[1] = (byte) (controllerCh1.getOn() ? 1 : 0);
        sws[2] = (byte) (controllerCh2.getOn() ? 1 : 0);
        sws[3] = (byte) (controllerCh3.getOn() ? 1 : 0);
        sws[4] = (byte) (controllerCh4.getOn() ? 1 : 0);
        sws[5] = (byte) (controllerCh5.getOn() ? 1 : 0);

        GlobalConfigs.globalFrequencySwitches.setValue(sws);
        BLEManager.getInstance().getBleMessageSender().sendSetFrequencies(lights, sws);
    }

    private void refreshViews() {
        byte[] frequencies_sws = GlobalConfigs.globalFrequencySwitches.getValue();
        int[] frequencies = GlobalConfigs.globalFrequencies.getValue();
        setFrequencies(frequencies, frequencies_sws);
    }

    private void setFrequencies(int[] frequencies, byte[] frequencies_sws) {
        controllerCh0.setSwitch(frequencies_sws[0] != 0).setValue(frequencies[0], false);
        controllerCh1.setSwitch(frequencies_sws[1] != 0).setValue(frequencies[1], false);
        controllerCh2.setSwitch(frequencies_sws[2] != 0).setValue(frequencies[2], false);
        controllerCh3.setSwitch(frequencies_sws[3] != 0).setValue(frequencies[3], false);
        controllerCh4.setSwitch(frequencies_sws[4] != 0).setValue(frequencies[4], false);
        controllerCh5.setSwitch(frequencies_sws[5] != 0).setValue(frequencies[5], false);
    }
}
