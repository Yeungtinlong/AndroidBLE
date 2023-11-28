package yc.bluetooth.androidble.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import yc.bluetooth.androidble.BLEDevice;
import yc.bluetooth.androidble.LVDevicesAdapter;
import yc.bluetooth.androidble.R;
import yc.bluetooth.androidble.TransparentStatusBar;
import yc.bluetooth.androidble.ble.BLEManager;
import yc.bluetooth.androidble.ble.BLEMessageSender;
import yc.bluetooth.androidble.ble.OnBleConnectListener;
import yc.bluetooth.androidble.ble.OnDeviceSearchListener;
import yc.bluetooth.androidble.permission.PermissionListener;
import yc.bluetooth.androidble.permission.PermissionRequest;
import yc.bluetooth.androidble.util.TypeConversion;

/**
 * BLE开发
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BLEMain";

    //bt_patch(mtu).bin

    // Osteo Strong


    // JDY
//    public static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";  //蓝牙通讯服务
//    public static final String READ_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";  //写特征
//    public static final String WRITE_UUID = "0000ffe2-0000-1000-8000-00805f9b34fb";  //写特征

    //动态申请权限
    private List<String> requestPermissionList = new ArrayList<>();
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权限
    private List<String> deniedPermissionList = new ArrayList<>();

    private static final int CONNECT_SUCCESS = 0x01;
    private static final int CONNECT_FAILURE = 0x02;
    private static final int DISCONNECT_SUCCESS = 0x03;
    private static final int SEND_SUCCESS = 0x04;
    private static final int SEND_FAILURE = 0x05;
    private static final int RECEIVE_SUCCESS = 0x06;
    private static final int RECEIVE_FAILURE = 0x07;
    private static final int START_DISCOVERY = 0x08;
    private static final int STOP_DISCOVERY = 0x09;
    private static final int DISCOVERY_DEVICE = 0x0A;
    private static final int DISCOVERY_OUT_TIME = 0x0B;
    private static final int SELECT_DEVICE = 0x0C;
    private static final int BT_OPENED = 0x0D;
    private static final int BT_CLOSED = 0x0E;
    private static final int IDENTIFY_SUCCESS = 0x0F;
    private static final int IDENTIFY_FAIL = 0x10;

    private Button btSearch;
    private LinearLayout llDeviceList;
    private RecyclerView lvDevices;
    private LVDevicesAdapter lvDevicesAdapter;

    private Context mContext;
    private BLEManager bleManager;
    private BLEBroadcastReceiver bleBroadcastReceiver;
    private BluetoothDevice curBluetoothDevice;  //当前连接的设备

    private BLEMessageSender bleMessageSender;

    //当前设备连接状态
    private boolean curConnState = false;


    private View loadingBlocker;

    private TextView loadingBlockerTextView;

    private Button btnTestSend;

    private PermissionListener permissionListener;

    private TextView searchingTextView;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case START_DISCOVERY:
                    Log.d(TAG, "开始搜索设备...");
                    break;

                case STOP_DISCOVERY:
                    Log.d(TAG, "停止搜索设备...");
                    break;

                case DISCOVERY_DEVICE:  //扫描到设备
                    BLEDevice bleDevice = (BLEDevice) msg.obj;
                    lvDevicesAdapter.addDevice(bleDevice);
                    searchingTextView.setVisibility(View.GONE);
                    break;

//                case SELECT_DEVICE:
//                    BluetoothDevice bluetoothDevice = (BluetoothDevice) msg.obj;
//                    tvName.setText(bluetoothDevice.getName());
//                    tvAddress.setText(bluetoothDevice.getAddress());
//                    curBluetoothDevice = bluetoothDevice;
//                    break;

                case CONNECT_FAILURE: //连接失败
                    onConnectFail((String) msg.obj);
//                    Log.d(TAG, "连接失败");
//                    tvCurConState.setText("连接失败");
                    curConnState = false;
                    break;

                case CONNECT_SUCCESS:  //连接成功
                    onConnectSuccess();

//                    tvCurConState.setText("连接成功");
                    curConnState = true;
//                    llDataSendReceive.setVisibility(View.VISIBLE);
//                    llDeviceList.setVisibility(View.GONE);
                    break;

                case DISCONNECT_SUCCESS:
//                    Log.d(TAG, "断开成功");
//                    tvCurConState.setText("断开成功");
                    closeBlock();
                    curConnState = false;
//
                    break;

                case SEND_FAILURE: //发送失败
                    byte[] sendBufFail = (byte[]) msg.obj;
                    String sendFail = TypeConversion.bytes2HexString(sendBufFail, sendBufFail.length);
                    Log.e(TAG, "发送数据失败，长度" + sendBufFail.length + "--> " + sendFail);
                    break;

                case SEND_SUCCESS:  //发送成功
                    byte[] sendBufSuc = (byte[]) msg.obj;
                    String sendResult = TypeConversion.bytes2HexString(sendBufSuc, sendBufSuc.length);
                    Log.i(TAG, "发送数据成功，长度" + sendBufSuc.length + "--> " + sendResult);
                    break;

                case RECEIVE_FAILURE: //接收失败
                    String receiveError = (String) msg.obj;
                    Log.e(TAG, receiveError);
                    break;

                case RECEIVE_SUCCESS:  //接收成功
                    byte[] recBufSuc = (byte[]) msg.obj;
                    String receiveResult = TypeConversion.bytes2HexString(recBufSuc, recBufSuc.length);
                    Log.i(TAG, "接收数据成功，长度" + recBufSuc.length + "--> " + receiveResult);

                    // 在主线程中处理接收到的信息
                    bleManager.sendMessageHandler(recBufSuc);

                    break;

                case IDENTIFY_SUCCESS:
                    Log.i(TAG, "验证身份成功");
                    gotoSecondaryActivity();

                    break;

                case IDENTIFY_FAIL:
                    Log.i(TAG, "验证身份失败");
                    closeBlock();
                    break;

                case BT_CLOSED:
                    Log.d(TAG, "系统蓝牙已关闭");
                    break;

                case BT_OPENED:
                    Log.d(TAG, "系统蓝牙已打开");
                    break;
            }

        }
    };

    private void onConnectSuccess() {
        Log.d(TAG, "连接成功");
        Toast.makeText(mContext, "Connect Success", Toast.LENGTH_SHORT).show();
    }

    private void onConnectFail(String errorMessage) {
        Log.d(TAG, "连接失败");
        curConnState = false;
        Toast.makeText(mContext, "Connect Fail >>> " + errorMessage, Toast.LENGTH_SHORT).show();
        closeBlock();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransparentStatusBar.setTransparent(this);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        //初始化视图
        initView();
        //初始化监听
        iniListener();
        //初始化数据
        initData();
        //注册广播
        initBLEBroadcastReceiver();

        //初始化权限
        //动态申请权限（Android 6.0）
        initPermissions();

        Log.d(TAG, "On Create.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //自动搜索设备
        searchBtDevice();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        btSearch = findViewById(R.id.bt_search);
        llDeviceList = findViewById(R.id.ll_device_list);
        lvDevices = findViewById(R.id.lv_devices);
        loadingBlocker = findViewById(R.id.mainBlocker);
        loadingBlockerTextView = loadingBlocker.findViewById(R.id.blocker_text);
        btnTestSend = findViewById(R.id.btn_test_send);
        searchingTextView = findViewById(R.id.searching_text);
    }


    /**
     * 初始化监听
     */
    private void iniListener() {

        btSearch.setOnClickListener(this);
        btnTestSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleManager.sendMessage("FB510051BF");
                Toast.makeText(mContext, "Send Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        btnTestSend.setVisibility(View.GONE);

        permissionListener = new PermissionListener() {
            @Override
            public void onGranted() {

                Log.d(TAG, "所有权限已被授予");

                //自动搜索设备
                searchBtDevice();
            }

            //用户勾选“不再提醒”拒绝权限后，关闭程序再打开程序只进入该方法！
            @Override
            public void onDenied(List<String> deniedPermissions) {
                deniedPermissionList = deniedPermissions;
                for (String deniedPermission : deniedPermissionList) {
                    Log.e(TAG, "被拒绝权限：" + deniedPermission);
                }
                new AlertDialog.Builder(mContext)
                        .setTitle("Lack of permissions")
                        .setMessage("Please clear data and grant all permissions.")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        };
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //列表适配器
        lvDevicesAdapter = new LVDevicesAdapter(this);
        lvDevicesAdapter.setOnDeviceConnectClickListener(new LVDevicesAdapter.OnDeviceConnectClickListener() {
            @Override
            public void onClicked(int position) {
                BLEDevice bleDevice = lvDevicesAdapter.getDevice(position);
                BluetoothDevice bluetoothDevice = bleDevice.getBluetoothDevice();
                if (bleManager != null) {
                    bleManager.stopDiscoveryDevice();
                }
                Message message = new Message();
                message.what = SELECT_DEVICE;
                message.obj = bluetoothDevice;
                mHandler.sendMessage(message);

//                Toast.makeText(mContext, "Request connecting to device: " + bleDevice.getBluetoothDevice().getName(), Toast.LENGTH_SHORT).show();

                if (!curConnState) {
                    if (bleManager != null) {
                        bleManager.connectBleDevice(mContext, bluetoothDevice, 15000, bleManager.getServiceUUID(), bleManager.getReadUUID(), bleManager.getWriteUUID(), onBleConnectListener);
                        openBlock();
                    }
                } else {
                    Toast.makeText(mContext, "当前设备已连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvDevices.setAdapter(lvDevicesAdapter);
        lvDevices.setLayoutManager(new LinearLayoutManager(this));

        //初始化ble管理器
        bleManager = BLEManager.getInstance();
        if (!bleManager.initBle(mContext)) {
            Log.d(TAG, "该设备不支持低功耗蓝牙");
            Toast.makeText(mContext, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        } else {
            if (!bleManager.isEnable()) {
                //去打开蓝牙
                bleManager.openBluetooth(mContext, false);
            }
        }
    }


    /**
     * 注册广播
     */
    private void initBLEBroadcastReceiver() {
        //注册广播接收
        bleBroadcastReceiver = new BLEBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//手机蓝牙状态监听
        registerReceiver(bleBroadcastReceiver, intentFilter);
    }

    /**
     * 初始化权限
     */
    private void initPermissions() {
        //Android 6.0以上动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final PermissionRequest permissionRequest = new PermissionRequest();

//            requestPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
//            requestPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);

            // Android 12所需权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
                requestPermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
                requestPermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            permissionRequest.requestRuntimePermission(MainActivity.this, requestPermissionList.toArray(new String[requestPermissionList.size()]), permissionListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //注销广播接收
        unregisterReceiver(bleBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_search:  //搜索蓝牙
                searchBtDevice();
                break;

//            case R.id.bt_connect: //连接蓝牙
//                if (!curConnState) {
//                    if (bleManager != null) {
//                        bleManager.connectBleDevice(mContext, curBluetoothDevice, 15000, SERVICE_UUID, READ_UUID, WRITE_UUID, onBleConnectListener);
//                    }
//                } else {
//                    Toast.makeText(this, "当前设备已连接", Toast.LENGTH_SHORT).show();
//                }
//                break;

//            case R.id.bt_disconnect: //断开连接
//                if (curConnState) {
//                    if (bleManager != null) {
//                        bleManager.disConnectDevice();
//                    }
//                } else {
//                    Toast.makeText(this, "当前设备未连接", Toast.LENGTH_SHORT).show();
//                }
//                break;

//            case R.id.bt_to_send: //发送数据
//                if (curConnState) {
//                    String sendMsg = etSendMsg.getText().toString();
//                    if (sendMsg.isEmpty()) {
//                        Toast.makeText(this, "发送数据为空！", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (bleManager != null) {
//                        bleManager.sendMessage(sendMsg);  //以16进制字符串形式发送数据
//                    }
//                } else {
//                    Toast.makeText(this, "请先连接当前设备", Toast.LENGTH_SHORT).show();
//                }
//                break;
        }

    }

    //////////////////////////////////  搜索设备  /////////////////////////////////////////////////
    private void searchBtDevice() {
        if (bleManager == null) {
            Log.d(TAG, "searchBtDevice()-->bleManager == null");
            return;
        }

        if (bleManager.isDiscovery()) { //当前正在搜索设备...
            bleManager.stopDiscoveryDevice();
        }

        if (lvDevicesAdapter != null) {
            lvDevicesAdapter.clear();  //清空列表
        }

        //开始搜索
        bleManager.startDiscoveryDevice(onDeviceSearchListener, 10000);
    }

    //扫描结果回调
    private OnDeviceSearchListener onDeviceSearchListener = new OnDeviceSearchListener() {

        @Override
        public void onDeviceFound(BLEDevice bleDevice) {
            Message message = new Message();
            message.what = DISCOVERY_DEVICE;
            message.obj = bleDevice;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDiscoveryOutTime() {
            Message message = new Message();
            message.what = DISCOVERY_OUT_TIME;
            mHandler.sendMessage(message);
        }
    };

    //连接回调
    private OnBleConnectListener onBleConnectListener = new OnBleConnectListener() {
        @Override
        public void onConnecting(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice) {

        }

        @Override
        public void onConnectSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            //因为服务发现成功之后，才能通讯，所以在成功发现服务的地方表示连接成功
        }

        @Override
        public void onConnectFailure(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, String exception, int status) {
            Message message = new Message();
            message.what = CONNECT_FAILURE;
            message.obj = exception;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDisConnecting(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice) {

        }

        @Override
        public void onDisConnectSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            Message message = new Message();
            message.what = DISCONNECT_SUCCESS;
            message.obj = status;
            mHandler.sendMessage(message);
        }

        @Override
        public void onIdentifySuccess() {
            Message message = new Message();
            message.what = IDENTIFY_SUCCESS;
            message.obj = null;
            mHandler.sendMessage(message);
        }

        @Override
        public void onIdentifyFail() {
            Message message = new Message();
            message.what = IDENTIFY_FAIL;
            message.obj = null;
            mHandler.sendMessage(message);
        }

        @Override
        public void onServiceDiscoverySucceed(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            //因为服务发现成功之后，才能通讯，所以在成功发现服务的地方表示连接成功
            Message message = new Message();
            message.what = CONNECT_SUCCESS;
            mHandler.sendMessage(message);
        }

        @Override
        public void onServiceDiscoveryFailed(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, String failMsg) {
            Message message = new Message();
            message.what = CONNECT_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReceiveMessage(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, byte[] msg) {
            Message message = new Message();
            message.what = RECEIVE_SUCCESS;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReceiveError(String errorMsg) {
            Message message = new Message();
            message.what = RECEIVE_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onWriteSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, byte[] msg) {
            Message message = new Message();
            message.what = SEND_SUCCESS;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onWriteFailure(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, byte[] msg, String errorMsg) {
            Message message = new Message();
            message.what = SEND_FAILURE;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReadRssi(BluetoothGatt bluetoothGatt, int Rssi, int status) {

        }

        @Override
        public void onMTUSetSuccess(String successMTU, int newMtu) {

        }

        @Override
        public void onMTUSetFailure(String failMTU) {

        }
    };


    /**
     * 蓝牙广播接收器
     */
    private class BLEBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_STARTED)) { //开启搜索
                Message message = new Message();
                message.what = START_DISCOVERY;
                mHandler.sendMessage(message);

            } else if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {//完成搜素
                Message message = new Message();
                message.what = STOP_DISCOVERY;
                mHandler.sendMessage(message);

            } else if (TextUtils.equals(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {   //系统蓝牙状态监听

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (state == BluetoothAdapter.STATE_OFF) {
                    Message message = new Message();
                    message.what = BT_CLOSED;
                    mHandler.sendMessage(message);

                } else if (state == BluetoothAdapter.STATE_ON) {
                    Message message = new Message();
                    message.what = BT_OPENED;
                    mHandler.sendMessage(message);

                }
            }
        }
    }

    private void gotoSecondaryActivity() {
        Intent intent = new Intent(mContext, CountDownActivity.class);
        startActivity(intent);
        closeBlock();
    }

    private void openBlock() {
        openBlock("Loading...");
    }

    private void openBlock(String message) {
        loadingBlocker.setVisibility(View.VISIBLE);
        loadingBlockerTextView.setText(message);
    }

    private void closeBlock() {
        loadingBlocker.setVisibility(View.GONE);
    }

    /**
     * 申请权限结果返回
     *
     * @param requestCode  请求码
     * @param permissions  所有申请的权限集合
     * @param grantResults 权限申请的结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, permissions.length + "to grant.");

        switch (requestCode) {
            case PermissionRequest.REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0) { //有权限申请
                    //存储被用户拒绝的权限
                    List<String> deniedPermissionList = new ArrayList<>();
                    //有权限被拒绝，分类出被拒绝的权限
                    for (int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        int grantResult = grantResults[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            if (!deniedPermissionList.contains(permission)) {
                                deniedPermissionList.add(permission);
                            }
                        }
                    }

                    if (deniedPermissionList.isEmpty()) {
                        //没有被拒绝的权限
                        if (permissionListener != null) {
                            permissionListener.onGranted();
                            Log.d(TAG, "权限都授予了");
                        }
                    } else {
                        //有被拒绝的权限
                        if (permissionListener != null) {
                            permissionListener.onDenied(deniedPermissionList);
                            Log.e(TAG, "有权限被拒绝了");
                        }
                    }
                }
                break;
        }
    }
}
