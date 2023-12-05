package yc.bluetooth.androidble.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.util.LogX;
import yc.bluetooth.androidble.util.MathUtils;

public class SuperSeekbar extends RelativeLayout {

    public interface OnValueChangedListener {
        void onValueChanged(float value);
    }

    private static final String TAG = "SuperSeekbar";

    private ImageView seekbarBg;
    private ImageView seekbarFg;
    private ImageView thumb;
    private RelativeLayout dragArea;

    private Button minusBtn;
    private Button addBtn;
    private EditText valueTxt;
    private Switch operatorSwitch;

    // attrs
    private float rangeMin = 0;
    private float rangeMax = 100;
    private float stepSize = 1;
    private boolean showSwitch = false;
    private boolean isOn = true;


    private float bgXLength;

    private float currentValue;

    private OnValueChangedListener onValueChangedListener;
    private TextWatcher onTextChangedListener;

    public float getValue() {
        if (!isOn)
            return rangeMin;

        return currentValue;
    }

    public float setValue(float value, boolean notify) {
        return setValue(value, true, notify);
    }

    public float setValue(float value, boolean setText, boolean notify) {
        currentValue = formatValue(value);
        float rate01 = valueToFormattedRate01(currentValue);

        LogX.d(TAG, "set value >>> rate01 = " + rate01 + ", currentValue = " + currentValue);

        if (!isOn)
            return currentValue;

        post(() -> setSeekbarByRate01(rate01));
        if (setText)
            setValueTxt(currentValue);

        if (notify && onValueChangedListener != null)
            onValueChangedListener.onValueChanged(currentValue);

        return currentValue;
    }


    public SuperSeekbar(Context context) {
        this(context, null);
    }

    public SuperSeekbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context, attrs);
        registerListeners();
    }

    private void initViews(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_seekbar, this, true);

        seekbarBg = view.findViewById(R.id.seekbar_bg);
        seekbarFg = view.findViewById(R.id.seekbar_fg);
        thumb = view.findViewById(R.id.seekbar_thumb);
        dragArea = view.findViewById(R.id.drag_area);

        minusBtn = view.findViewById(R.id.minus_btn);
        addBtn = view.findViewById(R.id.add_btn);
        valueTxt = view.findViewById(R.id.value_text);

        operatorSwitch = view.findViewById(R.id.operator_switch);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SuperSeekbar);
        stepSize = typedArray.getFloat(R.styleable.SuperSeekbar_stepSize, stepSize);
        showSwitch = typedArray.getBoolean(R.styleable.SuperSeekbar_showSwitch, false);
        rangeMin = typedArray.getFloat(R.styleable.SuperSeekbar_rangeMin, rangeMin);
        rangeMax = typedArray.getFloat(R.styleable.SuperSeekbar_rangeMax, rangeMax);

        initAttrs();

        // 回收
        typedArray.recycle();
    }

    private void initAttrs() {
        operatorSwitch.setVisibility(showSwitch ? VISIBLE : GONE);
        if (!showSwitch) {
            setOn();
        } else
            setOff();
    }

    public SuperSeekbar setRange(int rangeMin, int rangeMax) {
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        return this;
    }

    public SuperSeekbar setStepSize(int stepSize) {
//        if (step < 0 || step > rangeMax - rangeMin) {
//            LogX.e(TAG, "Step can't less than 0, or bigger than range.");
//            return null;
//        }

        this.stepSize = stepSize;
        return this;
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    private void registerListeners() {
        dragArea.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isOn)
                    return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE: {
                        float thumbLength = thumb.getRight() - thumb.getLeft();
                        bgXLength = seekbarBg.getRight() - seekbarBg.getLeft() - thumbLength;

                        float validX = 0;
                        float startOffset = seekbarBg.getLeft() + 0.5f * thumbLength;
                        float endX = startOffset + bgXLength;

                        if (event.getX() - startOffset < 0) {
                            validX = 0;
                        } else if (event.getX() > endX) {
                            validX = endX;
                        } else {
                            validX = event.getX() - startOffset;
                        }

                        float rate01 = mapXPosToProgressRate01(validX);

                        rate01 = snapRateByStep(rate01);

//                        LogX.d(TAG, "Progress (x: " + rate01 + ")");

                        setSeekbarByRate01(rate01);

                        currentValue = formatValue(MathUtils.project(rate01, 0, 1, rangeMin, rangeMax));

                        // Set Value TextView
                        setValueTxt(currentValue);
                        break;
                    }
                }
                return true;
            }
        });

        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOn)
                    return;

                currentValue = formatValue(currentValue + stepSize);
                float rate01 = valueToFormattedRate01(currentValue);
                setSeekbarByRate01(rate01);
                setValueTxt(currentValue);
            }
        });

        minusBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOn)
                    return;

                currentValue = formatValue(currentValue - stepSize);
                float rate01 = valueToFormattedRate01(currentValue);
                setSeekbarByRate01(rate01);
                setValueTxt(currentValue);
            }
        });

        operatorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setOn();
                else
                    setOff();
            }
        });

        onTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float value = 0;

                if (s == null || s.length() == 0) {
                    value = rangeMin;
                } else {
                    value = Float.parseFloat(s.toString());
                }

                int cursorPosBefore = valueTxt.getSelectionStart();

                valueTxt.removeTextChangedListener(this);
                setValue(formatValue(value), true, false);
                valueTxt.addTextChangedListener(this);

                int cursorPos = MathUtils.clamp(cursorPosBefore, 0, valueTxt.getText().length());
                valueTxt.setSelection(cursorPos);
            }
        };

        valueTxt.addTextChangedListener(onTextChangedListener);
    }

    private void unregisterListeners() {
        valueTxt.removeTextChangedListener(onTextChangedListener);
    }

    public SuperSeekbar setSwitch(boolean isOn) {
        if (isOn) {
            setOn();

        } else {
            setOff();
        }

        operatorSwitch.setChecked(isOn);
        return this;
    }

    private void setOn() {
        if (isOn)
            return;

        isOn = true;
        float rate01 = valueToFormattedRate01(currentValue);
        setSeekbarByRate01(rate01);
        setValueTxt(currentValue);

        valueTxt.setEnabled(true);
    }

    private void setOff() {
        if (!isOn)
            return;

        isOn = false;
        float rate01 = valueToFormattedRate01(0);
        setSeekbarByRate01(rate01);
        setValueTxt(0);

        valueTxt.setEnabled(false);
    }

    public boolean getOn() {
        return isOn;
    }

    private float valueToFormattedRate01(float value) {
        value = formatValue(value);
        float rate = value / (rangeMax - rangeMin);
        return snapRateByStep(rate);
    }

    private void setValueTxt(float value) {
        value = formatValue(value);
        valueTxt.setText(String.valueOf((int) value));
    }

    private float formatValue(float value) {
        int stepCount = Math.round(value / stepSize);
        return MathUtils.clamp(stepCount * stepSize, rangeMin, rangeMax);
    }

    private float snapRateByStep(float rate) {
        if (stepSize == -1)
            return rate;

        float stepIn01 = (float) stepSize / (rangeMax - rangeMin);
        float stepCount = Math.round(rate / stepIn01);
        return stepCount * stepIn01;
    }

    private float mapXPosToProgressRate01(float xPos) {
        return MathUtils.clamp(xPos / bgXLength, 0, 1);
    }

    private void setSeekbarByRate01(float rate) {
        float thumbLength = thumb.getRight() - thumb.getLeft();
        float length = seekbarBg.getRight() - seekbarBg.getLeft();
        float localXThumb = MathUtils.project(rate, 0, 1, thumbLength * 0.5f, length - thumbLength * 0.5f);

        // Set Seekbar
        setThumbPos(localXThumb);
        setFgSize(localXThumb);
    }

    private void setThumbPos(float localX) {
        thumb.setX(localX - (thumb.getRight() - thumb.getLeft()) * 0.5f);
    }

    private void setFgSize(float localX) {
        ViewGroup.LayoutParams layoutParams = seekbarFg.getLayoutParams();
        layoutParams.width = (int) localX;
        seekbarFg.setLayoutParams(layoutParams);
    }
}
