package yc.bluetooth.androidble.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.BLEMessageSender;
import yc.bluetooth.androidble.ble.GlobalConfigs;
import yc.bluetooth.androidble.common.CallbackValue;
import yc.bluetooth.androidble.util.MathUtils;

public class DeviceInfoActivity extends AppCompatActivity {

    private EditText deviceIdText;
    private EditText groupIdText;
    private TextView softwareVersionText;
    private TextView hardwareVersionText;
    private Button applyButton;

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
        groupIdText = findViewById(R.id.group_id_text);
        softwareVersionText = findViewById(R.id.software_id_text);
        hardwareVersionText = findViewById(R.id.hardware_id_text);
        applyButton = findViewById(R.id.submit_btn);
    }

    private void refreshViews() {
        deviceIdText.setText(GlobalConfigs.deviceId.getValue());
        softwareVersionText.setText(GlobalConfigs.softwareVersion.getValue());
        hardwareVersionText.setText(GlobalConfigs.hardwareVersion.getValue());
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

        GlobalConfigs.deviceId.addOnValueChangeListener(onDeviceIdChangedListener);
        GlobalConfigs.softwareVersion.addOnValueChangeListener(onSoftwareVersionChangedListener);
        GlobalConfigs.hardwareVersion.addOnValueChangeListener(onHardwareVersionChangedListener);

        deviceIdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0)
                    return;

                deviceIdText.removeTextChangedListener(this);
                String input = s.toString();

                int cursorIndex = deviceIdText.getSelectionStart();

                if (input.length() > 2) {
                    deviceIdText.setText("99");
                } else if (input.charAt(0) != '0') {
                    int value = Integer.parseInt(input);
                    int validValue = MathUtils.clamp(value, 0, 99);
                    deviceIdText.setText(String.valueOf(validValue));
                }

                cursorIndex = MathUtils.clamp(cursorIndex, 0, deviceIdText.getText().length());
                deviceIdText.setSelection(cursorIndex);

                deviceIdText.addTextChangedListener(this);
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = deviceIdText.getText().toString();

                if (id == null || id.length() == 0) {
                    Toast.makeText(DeviceInfoActivity.this, "Target ID is invalid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int idToSet = Integer.parseInt(deviceIdText.getText().toString());
                BLEManager.getInstance().getBleMessageSender().sendSetTime(idToSet);
                new AlertDialog.Builder(DeviceInfoActivity.this)
                        .setMessage("Apply successfully, please reconnect device.")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        })
                        .setCancelable(false)
                        .show();


                try {

                } catch (Exception e) {

                }
            }
        });
    }

    private void unregisterListeners() {
        GlobalConfigs.deviceId.removeOnValueChangeListener(onDeviceIdChangedListener);
        GlobalConfigs.softwareVersion.removeOnValueChangeListener(onSoftwareVersionChangedListener);
        GlobalConfigs.hardwareVersion.removeOnValueChangeListener(onHardwareVersionChangedListener);
    }
}