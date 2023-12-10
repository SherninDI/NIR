package com.example.nir;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    Button connect;
    Button refresh;
    TextView deviceText;
    TextView contentText;

    UsbDevice usbDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect = findViewById(R.id.connect);
        refresh = findViewById(R.id.refresh);
        deviceText = findViewById(R.id.device);
        contentText = findViewById(R.id.content);

        File file = new File(this.getFilesDir(), "groups.grf");
        FileHandler fileHandler = new FileHandler(file);
        byte[] allBytes = new byte[51200];
        try {
            fileHandler.writeBytes(allBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileHandler.close();
        }

        usbDevice = getDevice();
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usbDevice != null) {
                    Intent intent = new Intent(getApplicationContext(), DataActivity.class);
                    startActivity(intent);
                }
                Intent intent = new Intent(getApplicationContext(), DataActivity.class);
                startActivity(intent);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usbDevice = getDevice();
            }
        });
    }

    private UsbDevice getDevice() {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice usbDevice : deviceList.values()) {
            device = usbDevice;
        }
        if (device == null) {
            deviceText.setText(getString(R.string.device_not_found));
            contentText.setText(R.string.device_not_found_content);
        } else {
            StringBuilder chars = new StringBuilder(getString(
                    R.string.device_found_content,
                    device.getManufacturerName(),
                    device.getDeviceId(),
                    device.getProductId(),
                    device.getVendorId(),
                    device.getVersion(),
                    device.getSerialNumber(),
                    device.getDeviceProtocol(),
                    device.getDeviceClass(),
                    device.getDeviceSubclass(),
                    device.getInterfaceCount()
            ));
            for (int intf = 0; intf < device.getInterfaceCount(); intf++) {
                UsbInterface usbInterface = device.getInterface(intf);
                String inter = "Протокол" + usbInterface.getName() + "\n";
                chars.append(inter).append(usbInterface.getInterfaceProtocol()).append("\n").append(usbInterface.getEndpointCount()).append("\n");
                for (int nEp = 0; nEp < usbInterface.getEndpointCount(); nEp++) {
                    UsbEndpoint tmpEndpoint = usbInterface.getEndpoint(nEp);
                    chars.append(tmpEndpoint.getType()).append("\n").append(tmpEndpoint.getDirection()).append("\n").append(tmpEndpoint.getMaxPacketSize()).append("\n");
                }
            }
            deviceText.setText(getString(R.string.device_found, device.getProductName()));
//            contentText.setMovementMethod(new ScrollingMovementMethod());
            contentText.setText(chars.toString());
        }
        return device;
    }

}