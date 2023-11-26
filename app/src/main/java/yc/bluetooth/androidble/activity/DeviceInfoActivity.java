package yc.bluetooth.androidble.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.BLEMessageSender;
import yc.bluetooth.androidble.ble.LightGlobalConfig;
import yc.bluetooth.androidble.common.CallbackValue;

public class DeviceInfoActivity extends AppCompatActivity {

    private TextView deviceIdText;
    private TextView softwareVersionText;
    private TextView hardwareVersionText;

    private CallbackValue.Action<String> onDeviceIdChangedListener;
    private CallbackValue.Action<String> onSoftwareVersionChangedListener;
    private CallbackValue.Action<String> onHardwareVersionChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TransparentStatusBar.setTransparent(this);

        setContentView(R.layout.activity_device_info);

        // 初始化UI
        initViews();
        // 注册监听
        registerListeners();
        // 刷新UI
        refreshViews();

        BLEManager.getInstance().getBleMessageSender().sendGetDevice(BLEMessageSender.GET_DEVICE_INFO_MODE_ID);
        BLEManager.getInstance().getBleMessageSender().sendGetDevice(BLEMessageSender.GET_DEVICE_INFO_MODE_VERSION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销监听
        unregisterListeners();
    }

    private void initViews() {
        deviceIdText = findViewById(R.id.device_id_text);
        softwareVersionText = findViewById(R.id.software_id_text);
        hardwareVersionText = findViewById(R.id.hardware_id_text);
    }

    private void refreshViews() {
        deviceIdText.setText(LightGlobalConfig.deviceId.getValue());
        softwareVersionText.setText(LightGlobalConfig.softwareVersion.getValue());
        hardwareVersionText.setText(LightGlobalConfig.hardwareVersion.getValue());
    }

    private void registerListeners() {
        onDeviceIdChangedListener = deviceId -> {
            deviceIdText.setText(deviceId);
        };

        onSoftwareVersionChangedListener = softwareVersion -> {
            softwareVersionText.setText(softwareVersion);
        };

        onHardwareVersionChangedListener = hardwareVersion -> {
            hardwareVersionText.setText(hardwareVersion);
        };

        LightGlobalConfig.deviceId.addOnValueChangeListener(onDeviceIdChangedListener);
        LightGlobalConfig.softwareVersion.addOnValueChangeListener(onSoftwareVersionChangedListener);
        LightGlobalConfig.hardwareVersion.addOnValueChangeListener(onHardwareVersionChangedListener);
    }

    private void unregisterListeners() {
        LightGlobalConfig.deviceId.removeOnValueChangeListener(onDeviceIdChangedListener);
        LightGlobalConfig.softwareVersion.removeOnValueChangeListener(onSoftwareVersionChangedListener);
        LightGlobalConfig.hardwareVersion.removeOnValueChangeListener(onHardwareVersionChangedListener);
    }
}