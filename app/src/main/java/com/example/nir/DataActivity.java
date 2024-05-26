package com.example.nir;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataActivity extends AppCompatActivity {
    private final String TAG = DataActivity.class.getSimpleName();
    private Button send, receive, reset;
    private RecyclerView groupList;
    private GroupAdapter groupAdapter;
    private List<String> groups = new ArrayList<>();
    private byte[] groupsByte = new byte[51200];
    private byte[] group = new byte[512];
    private int groupSize = 512;
    int counter = 0;


    private static final String FILE_NAME = "groups.grf";
    private static final String SAVE_FILE_NAME = "save.grf";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //not used yet
                        }
                    } else {
                        Log.i(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbInterface usbInterface;
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;
    private UsbDeviceConnection usbConnection;
    private InputOutputManager inputOutputManager;
    private CommandFormat commandFormat;

    private ByteBuffer groupDataBuffer;
    InputOutputManager.Listener listener = new InputOutputManager.Listener() {
        @Override
        public void onNewData(final byte[] data) {
            DataActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DataActivity.this.updateReceivedData(data);
                }
            });
        }
        @Override
        public void onRunError(Exception e) {
            Log.i("InputOutputManager", "Runner stopped.");
        }
    };

    UsbDevice findDevice() {
        HashMap<String, UsbDevice> deviceList = this.usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            return device;
        }
        return null;
    }

    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            commandsHandler.obtainMessage(Constants.SYS_NOP).sendToTarget();
            if (counter == groupDataBuffer.array().length) {
                writeGroups();
                counter = 0;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        send = findViewById(R.id.send);
        receive = findViewById(R.id.receive);
        reset = findViewById(R.id.reset);
        groupList = findViewById(R.id.group_list);

        commandReset();

//        file = new File(this.getFilesDir(), "groups.grf");
//        fileHandler = new FileHandler(file);
        byte[] groupData = new byte[51200];
        groupDataBuffer = ByteBuffer.wrap(groupData);

        commandFormat = new CommandFormat();

        if (groups.size() != 0) {
            openGroups();
            groupAdapter = new GroupAdapter(getApplicationContext(), groups);
            groupList.setAdapter(groupAdapter);
            groupAdapter.notifyDataSetChanged();
            groupAdapter.setOnGroupClickListener(new GroupAdapter.GroupClickListener() {
                @Override
                public void onGroupClick(int position, View itemView) {
                    TextView textView = (TextView) itemView.findViewById(R.id.tvGroupName) ;
                    Intent intent = new Intent(DataActivity.this, GroupDataActivity.class);
                    intent.putExtra("group_position", position);
//                    intent.putExtra("new", false);
//                    intent.putExtra("name", textView.getText().toString());
                    startActivity(intent);

                    Log.e(TAG,"click group pos " + position);
                }
            });
        } else {

        }

        send.setOnClickListener(v -> commandSend());
        receive.setOnClickListener(v -> commandReceive());
        reset.setOnClickListener(v -> commandReset());

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = findDevice();
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        if (usbDevice != null) {
            this.usbDevice = usbDevice;
            registerReceiver(usbReceiver, filter);
            usbManager.requestPermission(this.usbDevice, mPermissionIntent);
            for (int intf = 0; intf < this.usbDevice.getInterfaceCount(); intf++) {
                usbInterface = this.usbDevice.getInterface(intf);
                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {
                    for (int nEp = 0; nEp < usbInterface.getEndpointCount(); nEp++) {
                        UsbEndpoint tmpEndpoint = usbInterface.getEndpoint(nEp);
                        if (tmpEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) continue;

                        if ((outEndpoint == null)
                                && (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_OUT)) {
                            outEndpoint = tmpEndpoint;
                            System.out.println("endpO" + outEndpoint.getMaxPacketSize());
                        } else if ((inEndpoint == null)
                                && (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_IN)) {
                            inEndpoint = tmpEndpoint;
                            System.out.println("endpI" + inEndpoint.getMaxPacketSize());
                        }
                    }
                    if (outEndpoint == null) {
                        Toast.makeText(this, "no endpoints", Toast.LENGTH_LONG).show();
                    }
                    usbConnection = usbManager.openDevice(this.usbDevice);
                    if (usbConnection == null) {
                        Toast.makeText(this, "can't open device", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        usbConnection.claimInterface(usbInterface, true);
                        startIoManager();
                    }
                }
            }
        }
    }

    public void commandReset() {
        commandsHandler.obtainMessage(Constants.SYS_RESET).sendToTarget();
        Toast.makeText(getApplicationContext(), "Аппарат готов к управлению", Toast.LENGTH_SHORT).show();
    }

    public void commandCancel() {
        commandsHandler.obtainMessage(Constants.SYS_CANCEL).sendToTarget();
        Toast.makeText(getApplicationContext(), "Отмена", Toast.LENGTH_SHORT).show();
    }

    public void commandReceive() {
        commandsHandler.obtainMessage(Constants.SYS_GET).sendToTarget();
    }

    public void commandSend() {
        try {
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            byte[] groupsBytes = fileHandler.readBytes(51200);
            commandsHandler.obtainMessage(Constants.SYS_PUT, groupsByte.length,-1, groupsBytes).sendToTarget();
            Toast.makeText(getApplicationContext(), "Данные отправлены", Toast.LENGTH_SHORT).show();
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void cleanGroups() {
        try {
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            for (int i = 0; i < groupsByte.length / groupSize; i++) {
                byte[] group = new byte[groupSize];
                GroupFormat groupFormat = new GroupFormat(group);
                groupFormat.writeNumber(i);
                groupFormat.writeFileId();
                groupFormat.writeCRC();
                fileHandler.writeBytesToPosition(group, i);
            }
            groups.clear();
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        groupAdapter = new GroupAdapter(getApplicationContext(), groups);
        groupList.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
    }

    public void saveGroups() {
        try {
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            byte[] groupsBytes = fileHandler.readBytes(51200);
            File saveFile = new File(this.getFilesDir(), SAVE_FILE_NAME);
            try(FileOutputStream fos = new FileOutputStream(saveFile)) {
                fos.write(groupsBytes);
            }
            catch(IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            fileHandler.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openGroups() {
        groups.clear();
        File saveFile = new File(this.getFilesDir(), SAVE_FILE_NAME);
        // если файл не существует, выход из метода
        if(!saveFile.exists()) return;
        try(FileInputStream fin =  new FileInputStream(saveFile)) {
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            fileHandler.writeBytes(bytes);
            for (int i = 0; i < bytes.length / groupSize; i++) {
                group = fileHandler.readBytesFromPosition(i);
                GroupFormat groupFormat = new GroupFormat(group);
                if (groupFormat.readTitleLength() != 0) {
                    String title = groupFormat.readTitle() +
                            " " + "(" + groupFormat.readStepCount() + ")";
                    groups.add(title);
                }
            }
            fileHandler.close();

        }
        catch(IOException ex) {

            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        groupAdapter = new GroupAdapter(getApplicationContext(), groups);
        groupList.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
    }
//    public void refreshGroups() {
//        readGroups();
//        showGroups();
//    }
//



//    public void readGroups() {
//        try {
//            groupsByte = fileHandler.readBytes(51200);
//            for (int i = 0; i < groupsByte.length / groupSize; i++) {
//                group = fileHandler.readBytesFromPosition(i);
//                GroupFormat groupFormat = new GroupFormat(group);
//                if (groupFormat.readTitleLength() != 0) {
//                    groups.add(groupFormat.readTitle());
//                }
////                Log.i(TAG, i + " " + bytesToHex(group));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
    public void writeGroups() {
        try {
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            fileHandler.writeBytesToPosition(groupDataBuffer.array(), 0);
            fileHandler.close();
            groupDataBuffer.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    public void showGroups() {
//        if (groups.size() != 0) {
//            groupAdapter = new GroupAdapter(getApplicationContext(), groups);
//            groupList.setAdapter(groupAdapter);
//            groupAdapter.notifyDataSetChanged();
//            groupAdapter.setOnGroupClickListener(new GroupAdapter.GroupClickListener() {
//                @Override
//                public void onGroupClick(int position, View itemView) {
//                    TextView textView = (TextView) itemView.findViewById(R.id.tvGroupName) ;
//                    Intent intent = new Intent(DataActivity.this, GroupDataActivity.class);
//                    intent.putExtra("group_position", position);
////                    intent.putExtra("new", false);
////                    intent.putExtra("name", textView.getText().toString());
//                    startActivity(intent);
//
//                    Log.e(TAG,"click group pos " + position);
//                }
//            });
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.data_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_clean:
                Toast.makeText(this, "Группы очищены", Toast.LENGTH_SHORT).show();
                cleanGroups();
                return true;
            case R.id.action_open:
                Toast.makeText(this, "Файл открыт", Toast.LENGTH_SHORT).show();
                openGroups();
                return true;
            case R.id.action_save:
                Toast.makeText(this, "Файл сохранен", Toast.LENGTH_SHORT).show();
                saveGroups();
                return true;
            case R.id.action_refresh:
                Toast.makeText(this, "Группы обновлены", Toast.LENGTH_SHORT).show();
                saveGroups();
                openGroups();
                return true;
            case R.id.action_add:
                Intent intent = new Intent(DataActivity.this, GroupDataActivity.class);
//                intent.putExtra("new", true);
                intent.putExtra("group_position", groups.size());
                startActivity(intent);
                Log.e(TAG, "add group pos " + String.valueOf(groups.size()));
                return true;
//            case R.id.action_cancel:
//                commandCancel();
//                return true;
//            case R.id.action_refresh:
//                refreshGroups();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void stopIoManager() {
        if (inputOutputManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            inputOutputManager.stop();
            inputOutputManager = null;
        }
    }

    private void startIoManager() {
        if (usbConnection != null) {
            Log.i(TAG, "Starting io manager ..");
            inputOutputManager = new InputOutputManager(inEndpoint, outEndpoint, usbConnection, listener);
            mExecutor.submit(inputOutputManager);

            timer.scheduleAtFixedRate(timerTask,100,400);
        } else Toast.makeText(this, "mConnection == null", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usbManager != null) {
            stopIoManager();
            usbConnection.close();
            timer.cancel();
            unregisterReceiver(usbReceiver);
        }
    }


    private void updateReceivedData(byte[] data) {


        int blockSize = 11;
        int blockCount = data.length / blockSize;

        byte[] range = null;
            for (int i = 0; i < blockCount; i++) {
                int idx = i * blockSize;
                range = Arrays.copyOfRange(data, idx, idx + blockSize);

                switch (range[1]) {
                    case (byte) Constants.SYS_STAT:

                        break;
                    case (byte) Constants.SYS_RUN:
                        Log.i(TAG, "SYS_RUN " + bytesToHex(range));
                        break;
                    case (byte) Constants.SYS_FILE:
                        Log.i(TAG, "SYS_FILE " + bytesToHex(range));
                        break;
                    case (byte) Constants.SYS_DATA:
                        byte[] res = Arrays.copyOfRange(range,2,10);
                        counter += res.length;
                        groupDataBuffer.put(res);
//                        Log.i(TAG, "SYS_DATA " + bytesToHex(res) + " " + counter);
//                        Log.i(TAG, String.valueOf(counter));
                        if (counter == groupDataBuffer.array().length) {
                            Toast.makeText(getApplicationContext(), R.string.received, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default: break;
                }
        }
    }

    private void sendMessage(byte[] command) {
        if (inputOutputManager == null) {
//            Toast.makeText(this, "mInputOutputManager == null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            //data = textTransmitView.getText().toString().getBytes();
            inputOutputManager.writeAsync(command);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private final Handler commandsHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            byte[] command;
            byte[] data = new byte[9];
            switch (message.what) {
                case Constants.SYS_NOP:
                    command = commandFormat.getCommand(Constants.SYS_NOP, data);
                    sendMessage(command);
//                    Log.e(TAG, "SYS_NOP " + bytesToHex(command));
                    break;
                case Constants.SYS_RESET:
                    command = commandFormat.getCommand(Constants.SYS_RESET, data);
                    sendMessage(command);
                    Log.e(TAG, "SYS_RESET " + bytesToHex(command));
                    break;
                case Constants.SYS_PUT:
                    byte[] buf = (byte[]) message.obj;
                    command = commandFormat.getCommand(Constants.SYS_PUT, buf);
                    sendMessage(command);
                    Log.e(TAG, "SYS_PUT " + bytesToHex(command));

                    int blockSize = 8;
                    int blockCount = buf.length / blockSize;
                    for (int i = 0; i < blockCount; i++) {
                        int idx = i * blockSize;
                        byte[] range = Arrays.copyOfRange(buf, idx, idx + blockSize);
//                        Log.e(TAG, "SYS_PUT " + bytesToHex(range) + " " + idx);
                        commandsHandler.obtainMessage(Constants.SYS_DATA, idx,0, range).sendToTarget();
                    }
                    break;
                case Constants.SYS_GET:
                    command = commandFormat.getCommand(Constants.SYS_GET, data);
                    sendMessage(command);
                    Log.e(TAG, "SYS_GET " + bytesToHex(command));
                    break;
                case Constants.SYS_CANCEL:
                    command = commandFormat.getCommand(Constants.SYS_CANCEL, data);
                    sendMessage(command);
                    Log.e(TAG, "SYS_CANCEL " + bytesToHex(command));
                    break;
                case Constants.SYS_DATA:
                    command = commandFormat.getCommand(Constants.SYS_DATA, (byte[])message.obj);
                    sendMessage(command);
                    Log.e(TAG, "SYS_DATA " + bytesToHex(command) + " " + message.arg1);
                    break;
            }
            return false;
        }
    });

    public static String bytesToHex(byte[] byteArray)
    {
        String hex = "";
        // Iterating through each byte in the array
        for (byte i : byteArray) {
            hex += String.format("%02X", i);
            hex += " ";
        }
        return hex;
    }
}