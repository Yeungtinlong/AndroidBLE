package yc.bluetooth.androidble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.common.CallbackValue;
import yc.bluetooth.androidble.ble.LightGlobalConfig;

public class CountDownActivity extends AppCompatActivity {

    private static final String TAG = "CountDownActivity";

    private static final int MAX_TIME = 1800;
    private static final String TIMER_TEXT_PREFIX = "Countdown \n";

    private Button backBtn;
    private Button deviceInfoBtn;
    private Button timerBtn;
    private Button intensityBtn;
    private Button pulseBtn;

    private ToggleButton timerSwitchBtn;
    private ProgressBar timerProgressBar;
    private TextView countdownText;
    private long latestExitTime = 0;
    private final static long EXIT_APPLICATION_BACK_BUTTON_INTERVAL_MILLIS = 2000;

    private CallbackValue.Action<Integer> onGlobalTimerChanged;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransparentStatusBar.setTransparent(this);
        setContentView(R.layout.activity_countdown);

        // 初始化UI
        initViews();

        // 注册UI时间
        registerViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 刷新UI
        refreshViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterViews();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - latestExitTime > EXIT_APPLICATION_BACK_BUTTON_INTERVAL_MILLIS) {
                Toast.makeText(this, "Press again to exit app", Toast.LENGTH_SHORT).show();
                latestExitTime = System.currentTimeMillis();
            } else {
                BLEManager.getInstance().disConnectDevice();
                finishAffinity();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        backBtn = findViewById(R.id.btn_backToMain);
        deviceInfoBtn = findViewById(R.id.btn_deviceId);
        timerBtn = findViewById(R.id.btn_timer);
        timerProgressBar = findViewById(R.id.timer).findViewById(R.id.timer_circle_progress);
        countdownText = findViewById(R.id.timer_text);
        timerSwitchBtn = findViewById(R.id.btn_timer_switch);
        intensityBtn = findViewById(R.id.btn_intensity);
        pulseBtn = findViewById(R.id.btn_pulse);
    }

    private void registerViews() {
        backBtn.setOnClickListener(view -> {

            AlertDialog alertDialog = new AlertDialog.Builder(CountDownActivity.this)
                    .setTitle("To disconnect?")
//                    .setMessage("Please clear data and grant all permissions.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        BLEManager.getInstance().disConnectDevice();
                        finish();
                    }).setNegativeButton("No", (dialog, which) -> {

                    })
                    .setCancelable(false)
                    .show();
        });

        deviceInfoBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, DeviceInfoActivity.class);
            startActivity(intent);
        });

        timerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, TimerActivity.class);
            startActivity(intent);
        });

        intensityBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, IntensityActivity.class);
            startActivity(intent);
        });

        pulseBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, PulseActivity.class);
            startActivity(intent);
        });

        onGlobalTimerChanged = value -> {
            int progress = getProgress(value);
            timerProgressBar.setProgress(progress);
            Log.d(TAG, "progress = " + progress);
            setTimerText(value);
        };

        LightGlobalConfig.globalTimerSet.addOnValueChangeListener(onGlobalTimerChanged);

        timerSwitchBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            LightGlobalConfig.isCountingDown.setValue(isChecked);
            timerSwitchBtn.setBackground(getResources().getDrawable(isChecked ? R.drawable.ic_on : R.drawable.ic_off));

            if (isChecked) {
                persistentlyGetTimer();
                startCountdown();
            } else {
                stopCountdown();
            }
        });
    }

    private void startCountdown() {
        BLEManager.getInstance().getBleMessageSender().sendSetOn();
    }

    private void stopCountdown() {
        BLEManager.getInstance().getBleMessageSender().sendSetOff();
    }

    private void persistentlyGetTimer() {

        Thread getTimerThread = new Thread(() -> {
            while (LightGlobalConfig.isCountingDown.getValue()) {
                BLEManager.getInstance().getBleMessageSender().sendGetTime();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        getTimerThread.start();

    }

    private void unregisterViews() {
        LightGlobalConfig.globalTimerSet.removeOnValueChangeListener(onGlobalTimerChanged);
    }

    private void refreshViews() {
        int remainingTime = LightGlobalConfig.globalTimerSet.getValue();
        int progress = getProgress(remainingTime);
        timerProgressBar.setProgress(progress);
        setTimerText(remainingTime);

        Log.d(TAG, "refresh clock, remaining time" + remainingTime);
    }

    private int getProgress(int remainingTime) {
        return Math.round((float) remainingTime / MAX_TIME * 100);
    }

    private void setTimerText(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = minutes == 0 ? totalSeconds : totalSeconds % 60;

        Log.w(TAG, "totalSeconds = " + totalSeconds + ", minutes = " + minutes + ", seconds = " + seconds);

        String minutesString = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String secondsString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);

        countdownText.setText(TIMER_TEXT_PREFIX + minutesString + ":" + secondsString);
    }
}
