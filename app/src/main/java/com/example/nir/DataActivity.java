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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataActivity extends AppCompatActivity {
    private final String TAG = DataActivity.class.getSimpleName();
    Button send;
    Button receive;
    Button add;
    Button save;
    Button reset;
    Button cancel;

    RecyclerView groupList;
    GroupAdapter groupAdapter;
    private List<String> groups = new ArrayList<>();

    private DatabaseAdapter databaseAdapter;
    private List<String> subgroups = new ArrayList<String>();


    private File file;
    private FileHandler fileHandler;

    private byte[] groupsByte = new byte[51200];
    private byte[] group = new byte[512];
    private int groupSize = 512;


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

    CommandFormat commandFormat;
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        commandFormat = new CommandFormat();

        send = findViewById(R.id.send);
        receive = findViewById(R.id.receive);
        add = findViewById(R.id.add);
        save = findViewById(R.id.save);
        reset = findViewById(R.id.reset);
        cancel = findViewById(R.id.cancel);

        groupList = findViewById(R.id.group_list);

        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        file = new File(this.getFilesDir(), "groups.grf");
        fileHandler = new FileHandler(file);
        try {
            groupsByte = fileHandler.readBytes(51200);
            for (int i = 0; i < groupsByte.length / groupSize; i++) {
                group = fileHandler.readBytesFromPosition(i);
                GroupFormat groupFormat = new GroupFormat(group);
                if (groupFormat.readTitleLength() != 0) {
                    groups.add(groupFormat.readTitle());
                }
//                Log.i(TAG, i + " " + bytesToHex(group));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (groups.size() != 0) {
            groupAdapter = new GroupAdapter(this, groups);
            groupList.setAdapter(groupAdapter);
            groupAdapter.notifyDataSetChanged();
            groupAdapter.setOnGroupClickListener(new GroupAdapter.GroupClickListener() {
                @Override
                public void onGroupClick(int position, View itemView) {
                    TextView textView = (TextView) itemView.findViewById(R.id.tvGroupName) ;
//                Log.e(TAG, textView.getText().toString());
                    Intent intent = new Intent(DataActivity.this, GroupDataActivity.class);
                    intent.putExtra("group", position);
                    intent.putExtra("new", false);
                    intent.putExtra("name", textView.getText().toString());
                    startActivity(intent);
                }
            });
        }


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataActivity.this, GroupDataActivity.class);
                intent.putExtra("new", true);
                intent.putExtra("size", groups.size());
                startActivity(intent);
//                Log.e(TAG, String.valueOf(groups.size()));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandsHandler.obtainMessage(Constants.SYS_PUT, groupsByte.length,-1, groupsByte).sendToTarget();
            }
        });

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandsHandler.obtainMessage(Constants.SYS_GET).sendToTarget();
            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandsHandler.obtainMessage(Constants.SYS_RESET).sendToTarget();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandsHandler.obtainMessage(Constants.SYS_CANCEL).sendToTarget();
            }
        });

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
//                startActivity(intent);
////                subgroups = databaseAdapter.getAllSubgroups(1);
////                for (String subgroup: subgroups) {
////                    Log.d(TAG,subgroup);
////                }
//            }
//        });

//        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        UsbDevice usbDevice = findDevice();
//        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        registerReceiver(usbReceiver, filter);
//        if (usbDevice != null) {
//            this.usbDevice = usbDevice;
//            registerReceiver(usbReceiver, filter);
//            usbManager.requestPermission(this.usbDevice, mPermissionIntent);
//            for (int intf = 0; intf < this.usbDevice.getInterfaceCount(); intf++) {
//                usbInterface = this.usbDevice.getInterface(intf);
//                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {
//                    for (int nEp = 0; nEp < usbInterface.getEndpointCount(); nEp++) {
//                        UsbEndpoint tmpEndpoint = usbInterface.getEndpoint(nEp);
//                        if (tmpEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) continue;
//
//                        if ((outEndpoint == null)
//                                && (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_OUT)) {
//                            outEndpoint = tmpEndpoint;
//                            System.out.println("endpO" + outEndpoint.getMaxPacketSize());
//                        } else if ((inEndpoint == null)
//                                && (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_IN)) {
//                            inEndpoint = tmpEndpoint;
//                            System.out.println("endpI" + inEndpoint.getMaxPacketSize());
//                        }
//                    }
//                    if (outEndpoint == null) {
//                        Toast.makeText(this, "no endpoints", Toast.LENGTH_LONG).show();
//                    }
//                    usbConnection = usbManager.openDevice(this.usbDevice);
//                    if (usbConnection == null) {
//                        Toast.makeText(this, "can't open device", Toast.LENGTH_SHORT).show();
//                        return;
//                    } else {
//                        usbConnection.claimInterface(usbInterface, true);
//                        startIoManager();
//                    }
//                }
//            }
//        }

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
//        if (usbManager != null) {
//            stopIoManager();
//            usbConnection.close();
//            timer.cancel();
//            unregisterReceiver(usbReceiver);
//        }
        databaseAdapter.close();
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
//        Log.d(TAG, message);
    }

    private void sendMessage(byte[] command) {
        if (inputOutputManager == null) {
            Toast.makeText(this, "mInputOutputManager == null", Toast.LENGTH_LONG).show();
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
//                    sendMessage(command);
                    Log.e(TAG, "SYS_NOP " + bytesToHex(command));
                    break;
                case Constants.SYS_RESET:
                    command = commandFormat.getCommand(Constants.SYS_RESET, data);
//                    sendMessage(command);
                    Log.e(TAG, "SYS_RESET " + bytesToHex(command));
                    break;
                case Constants.SYS_PUT:
                    byte[] buf = (byte[]) message.obj;
                    command = commandFormat.getCommand(Constants.SYS_PUT, buf);
//                    sendMessage(command);
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
//                    sendMessage(command);
                    Log.e(TAG, "SYS_GET " + bytesToHex(command));
                    break;
                case Constants.SYS_CANCEL:
                    command = commandFormat.getCommand(Constants.SYS_CANCEL, data);
//                    inputOutputManager.writeAsync(command);
                    Log.e(TAG, "SYS_CANCEL " + bytesToHex(command));
                    break;
                case Constants.SYS_DATA:
                    command = commandFormat.getCommand(Constants.SYS_DATA, (byte[])message.obj);
//                    sendMessage(command);
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