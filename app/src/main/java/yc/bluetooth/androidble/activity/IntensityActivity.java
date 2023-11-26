package yc.bluetooth.androidble.activity;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.LightGlobalConfig;
import yc.bluetooth.androidble.common.CallbackValue;
import yc.bluetooth.androidble.ui.SuperSeekbar;

public class IntensityActivity extends AppCompatActivity {

    private SuperSeekbar controllerCh0;
    private SuperSeekbar controllerCh1;
    private SuperSeekbar controllerCh2;
    private SuperSeekbar controllerCh3;
    private SuperSeekbar controllerCh4;
    private SuperSeekbar controllerCh5;
    private Button submitBtn;

    private CallbackValue.Action<byte[]> onLightsChangedListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransparentStatusBar.setTransparent(this);

        setContentView(R.layout.activity_intensity_settings);

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
            sendIntensitySettings();
            finish();
        });

        onLightsChangedListener = lights -> {
            setLights(lights);
        };

        LightGlobalConfig.globalLights.addOnValueChangeListener(onLightsChangedListener);
    }

    private void unregisterListeners() {
        LightGlobalConfig.globalLights.removeOnValueChangeListener(onLightsChangedListener);
    }

    private void sendIntensitySettings() {
        byte[] lights = new byte[6];
        lights[0] = (byte) controllerCh0.getValue();
        lights[1] = (byte) controllerCh1.getValue();
        lights[2] = (byte) controllerCh2.getValue();
        lights[3] = (byte) controllerCh3.getValue();
        lights[4] = (byte) controllerCh4.getValue();
        lights[5] = (byte) controllerCh5.getValue();

        BLEManager.getInstance().getBleMessageSender().sendSetLights(lights);
    }

    private void refreshViews() {
        byte[] lights = LightGlobalConfig.globalLights.getValue();
        controllerCh0.setValue(lights[0], false);
        controllerCh1.setValue(lights[1], false);
        controllerCh2.setValue(lights[2], false);
        controllerCh3.setValue(lights[3], false);
        controllerCh4.setValue(lights[4], false);
        controllerCh5.setValue(lights[5], false);
    }

    private void setLights(byte[] lights) {
        controllerCh0.setValue(lights[0], false);
        controllerCh1.setValue(lights[1], false);
        controllerCh2.setValue(lights[2], false);
        controllerCh3.setValue(lights[3], false);
        controllerCh4.setValue(lights[4], false);
        controllerCh5.setValue(lights[5], false);
    }
}
