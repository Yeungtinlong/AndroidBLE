package yc.bluetooth.androidble.ble;

import java.util.ArrayList;
import java.util.List;

public class CallbackValue<T> {

    public interface Action<T> {
        void invoke(T value);
    }


    private T value;

    private List<Action<T>> listeners = new ArrayList<>();

    public CallbackValue() {

    }

    public CallbackValue(T defaultValue) {
        this.value = defaultValue;
    }

    public void setValue(T value, boolean notify) {
        if (!this.value.equals(value)) {
            this.value = value;
            if (notify)
                notifyChanged();
        }
    }

    public void setValue(T value) {
        setValue(value, true);
    }

    public T getValue() {
        return value;
    }

    public void notifyChanged() {
        for (Action<T> action : listeners) {
            action.invoke(value);
        }
    }

    public void addOnValueChangeListener(Action<T> listener) {
        this.listeners.add(listener);
    }

    public void removeOnValueChangeListener(Action<T> listener) {
        this.listeners.remove(listener);
    }
}
