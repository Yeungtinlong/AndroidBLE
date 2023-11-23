package yc.bluetooth.androidble;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.LightGlobalConfig;

public class CountDownActivity extends AppCompatActivity {


    private static final String TAG = "CountDownActivity";

    private static final int MAX_TIME = 1800;
    private static final String TIMER_TEXT_PREFIX = "Countdown \n";

    private Button backBtn;
    private ProgressBar timerProgressBar;
    private TextView countdownText;

    private long latestExitTime = 0;
    private final static long EXIT_APPLICATION_BACK_BUTTON_INTERVAL_MILLIS = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransparentStatusBar.setTransparent(this);
        setContentView(R.layout.activity_countdown);

        // 初始化UI
        initViews();

        // 注册UI时间
        registerViews();

        // 刷新UI
        refreshViews();
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
        timerProgressBar = findViewById(R.id.timer).findViewById(R.id.timer_progress);
        countdownText = findViewById(R.id.timer_text);
    }

    private void registerViews() {
        backBtn.setOnClickListener(view -> {
//            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        LightGlobalConfig.globalTimerSet.addOnValueChangeListener(value -> {
            int progress = getProgress(value);
            timerProgressBar.setProgress(progress);
            Log.d(TAG, "progress = " + progress);
            setTimerText(value);
        });
    }

    private void refreshViews() {
        int remainingTime = LightGlobalConfig.globalTimerSet.getValue();
        int progress = getProgress(remainingTime);
        timerProgressBar.setProgress(progress);
        setTimerText(remainingTime);
    }

    private int getProgress(int remainingTime) {
        return Math.round((float) remainingTime / MAX_TIME * 100);
    }

    private void setTimerText(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % minutes;

        String minutesString = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String secondsString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);

        countdownText.setText(TIMER_TEXT_PREFIX + minutesString + ":" + secondsString);
    }
}
