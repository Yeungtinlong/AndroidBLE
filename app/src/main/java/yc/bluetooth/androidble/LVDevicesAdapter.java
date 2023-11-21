package yc.bluetooth.androidble;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 搜索到的设备列表适配器
 */
public class LVDevicesAdapter extends RecyclerView.Adapter<LVDevicesAdapter.DeviceViewHolder> {

    public class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView tvDeviceName;
        Button connectButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            connectButton = itemView.findViewById(R.id.connectButton);
        }
    }

    public interface OnDeviceConnectClickListener {
        void onClicked(int position);
    }

    private Context context;
    private List<BLEDevice> bleDevices;

    private OnDeviceConnectClickListener onDeviceConnectClickListener;

    public LVDevicesAdapter(Context context) {
        this.context = context;
        bleDevices = new ArrayList<>();
    }

    public void setOnDeviceConnectClickListener(OnDeviceConnectClickListener onDeviceConnectClickListener) {
        this.onDeviceConnectClickListener = onDeviceConnectClickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_lv_devices_item, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {

        if (bleDevices.get(position).getBluetoothDevice().getName() == null) {
            holder.tvDeviceName.setText("NULL");
        } else {
            holder.tvDeviceName.setText(bleDevices.get(position).getBluetoothDevice().getName());
        }

        holder.connectButton.setOnClickListener(view -> {
            onDeviceConnectClickListener.onClicked(position);
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return bleDevices.size();
    }

    public BLEDevice getDevice(int position) {
        return bleDevices.get(position);
    }

    /**
     * 初始化所有设备列表
     *
     * @param bluetoothDevices
     */
    public void addAllDevice(List<BLEDevice> bluetoothDevices) {
        if (bleDevices != null) {
            bleDevices.clear();
            bleDevices.addAll(bluetoothDevices);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加列表子项
     *
     * @param bleDevice
     */
    public void addDevice(BLEDevice bleDevice) {
        if (bleDevices == null) {
            return;
        }

        if (!bleDevices.contains(bleDevice)) {
            bleDevices.add(bleDevice);
        }

        Log.d(LVDevicesAdapter.class.getName(), "Thread Id: " + Thread.currentThread().getId() + ", Add to UI: " + bleDevice.getBluetoothDevice().getName());

        notifyDataSetChanged();   //刷新
    }

    /**
     * 清空列表
     */
    public void clear() {
        if (bleDevices != null) {
            bleDevices.clear();
        }
        notifyDataSetChanged(); //刷新
    }
}
