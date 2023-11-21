package yc.bluetooth.androidble;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.window.OnBackInvokedCallback;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CountDownActivity extends AppCompatActivity {


    private Button backBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_countdown);
        initViews();
        registerViews();
    }


    private void initViews() {
        backBtn = findViewById(R.id.btn_backToMain);
    }

    private void registerViews() {
        backBtn.setOnClickListener(view -> {
//            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
