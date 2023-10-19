package com.example.nir;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final String TAG = MainActivity.class.getSimpleName();
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
    private TextView mProgressBarTitle;
    private TextView mDumpTextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
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
        } else Toast.makeText(this, "mConnection == null", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usbManager != null) {
            stopIoManager();
            usbConnection.close();
        }
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        Log.d(TAG, message);
    }

//    public void onClickButtonSend(View view) {
//        byte[] data;
//        data = new byte[]{0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x09, 0x00, 0x04};
//        if (inputOutputManager == null) {
//            Toast.makeText(this, "mInputOutputManager == null", Toast.LENGTH_LONG).show();
//            return;
//        }
//        try {
//            //data = textTransmitView.getText().toString().getBytes();
//            inputOutputManager.writeAsync(data);
//        } catch (Exception e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }


//    public void onClickButtonConnect(View view) {
//
//    }

//    public static String bytesToHex(byte[] byteArray)
//    {
//        String hex = "";
//        // Iterating through each byte in the array
//        for (byte i : byteArray) {
//            hex += String.format("%02X", i);
//            hex += " ";
//        }
//        return hex;
//    }
}