package yc.bluetooth.androidble.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.LightGlobalConfig;
import yc.bluetooth.androidble.ui.SuperSeekbar;

public class TimerActivity extends AppCompatActivity {

    private SuperSeekbar timerSeekbar;
    private Button submitBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransparentStatusBar.setTransparent(this);

        setContentView(R.layout.activity_timer_settings);

        initViews();

        registerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        timerSeekbar.post(() -> refreshViews());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterListeners();
    }

    private void initViews() {
        timerSeekbar = findViewById(R.id.timer_seekbar);
        timerSeekbar.setRange(0, 30);
        timerSeekbar.setStepSize(1);

        submitBtn = findViewById(R.id.submit_btn);
    }

    private void registerListeners() {
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEManager.getInstance().getBleMessageSender().sendSetTime((int) timerSeekbar.getValue() * 60);
                finish();
            }
        });
    }

    private void unregisterListeners() {

    }

    private void refreshViews() {
        timerSeekbar.setValue(LightGlobalConfig.globalTimerSet.getValue() / 60, false);
    }
}
