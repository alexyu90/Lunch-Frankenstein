package com.example.android.datafrominternet;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.objectstorage.ObjectStorage;
import com.ibm.mobilefirstplatform.clientsdk.android.objectstorage.ObjectStorageContainer;
import com.ibm.mobilefirstplatform.clientsdk.android.objectstorage.ObjectStorageObject;
import com.ibm.mobilefirstplatform.clientsdk.android.objectstorage.ObjectStorageResponseListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
//import com.google.android.gms.fitness.data.DataPoint;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerialReadActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    public final String ACTION_USB_PERMISSION = "com.example.android.datafrominternet.USB_PERMISSION";
    Button startButton, sendButton, clearButton, stopButton, saveButton, ch1Button, ch2Button, ch1ch2Button, resetButton;
    TextView textView;
    EditText editText;
    EditText fileName;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    ScrollView scroll;
    GraphView mGraph;
    String projectID = "b1e597bdccb3477491c51b5ab39db3e5";
    String userID = "8cbecfff096e4ad8aa5a74e426c1409d";
    String password = "JGH-Mo7kA~t)5VDI";
    boolean isStart;


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }


    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                tvAppend(textView, data);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

            }
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            //tvAppend(textView,"Serial Connection Opened!");
                            Toast.makeText(context, "Serial Connection Opened!", Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_read);

        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_UK);
        ObjectStorage.initialize(ObjectStorage.BluemixRegion.LONDON);
        ObjectStorage.connect(projectID, userID, password, new ObjectStorageResponseListener<String>(){
            @Override
            public void onSuccess(String authToken) {
                //Handle success
                Log.e("value", authToken);
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                //Handle failure
                Log.e("value", "Failure");

            }
        });

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            // Code for Below 23 API Oriented Device
            // Do next code
        }

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        clearButton = (Button) findViewById(R.id.buttonClear);
        stopButton = (Button) findViewById(R.id.buttonStop);
        ch1Button = (Button) findViewById(R.id.buttonCh1);
        ch2Button = (Button) findViewById(R.id.buttonCh2);
        ch1ch2Button = (Button) findViewById(R.id.buttonCh1Ch2);
        resetButton = (Button) findViewById(R.id.buttonReset);
        saveButton = (Button) findViewById(R.id.buttonSave);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        scroll = (ScrollView) findViewById(R.id.scroll);
        fileName = (EditText) findViewById(R.id.fileName);
        mGraph = (GraphView) findViewById(R.id.graph);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        isStart = false;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        ch1ch2Button.setEnabled(bool);
        ch1Button.setEnabled(bool);
        ch2Button.setEnabled(bool);
        resetButton.setEnabled(bool);

        textView.setEnabled(bool);
    }

    public void onClickSave(View view) {
        //output recorded data to .txt file
        onClickReset(view);
        write(view);
        objectStore();
        //textView.setText("");
        fileName.setText("");
    }

    public void objectStore(){
        byte[] objectData = textView.getText().toString().getBytes();
        String containerName = "Docs";
        String objectName = fileName.getText().toString() + ".txt";
        ObjectStorageContainer container = new ObjectStorageContainer(containerName);
        container.storeObject(objectName, objectData, new ObjectStorageResponseListener<ObjectStorageObject>(){
            @Override
            public void onSuccess(ObjectStorageObject storedObject) {
                //Handle success
                Log.e("value", "Upload Success");
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                //Handle failure
                Log.e("value", "Upload Failure");
            }
        });
    }

    public void onClickGraph(View view){
        read(view);
        graphIt(readCVSFromAssetFolder());
    }

    private List<String[]> readCVSFromAssetFolder(){
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            String name = fileName.getText().toString() + ".txt";
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path,name);
            //FileInputStream fileInputStream= openFileInput("myText.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line = "";
            while((line = br.readLine()) != null){
                content = line.split(",");
                csvLine.add(content);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvLine;
    }

    public void graphIt(List<String[]> result){
        //need to add exceptions
        mGraph.removeAllSeries();
        DataPoint[] dataPoints = new DataPoint[result.size()];
        for (int i = 0; i < result.size(); i++){
            String [] rows = result.get(i);
            dataPoints[i] = new DataPoint(Integer.parseInt(rows[0]), Integer.parseInt(rows[1]));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        series.setDrawDataPoints(true);
        mGraph.addSeries(series);

        // activate horizontal and vertical zooming and scrolling
        mGraph.getViewport().setScalableY(true);
    }

    public void read(View view) {
        try {
            String name = fileName.getText().toString() + ".txt";
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path,name);
            //FileInputStream fileInputStream= openFileInput("myText.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String lines;
            while ((lines=bufferedReader.readLine())!=null) {
                stringBuffer.append(lines+"\n");
            }
            textView.setText(stringBuffer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(View view) {
        String Mytextmessage  = textView.getText().toString();
        try {
            String name = fileName.getText().toString() + ".txt";
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path,name);
            //FileOutputStream fileOutputStream = openFileOutput("myText.txt",MODE_PRIVATE);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(Mytextmessage.getBytes());
            fileOutputStream.close();
            Toast.makeText(getApplicationContext(),"Text Saved",Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x1B4F || deviceVID == 0x2341)//Sparkfun Vendor ID||Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }
                if (!keep)
                    break;
            }
        }
    }

    public void onClickSend(View view) {
        String string = editText.getText().toString();
        serialPort.write(string.getBytes());
        hideSoftKeyboard(SerialReadActivity.this);
        //tvAppend(textView, "\nData Sent : " + string + "\n");
        Toast.makeText(this, "Data Sent : " + string, Toast.LENGTH_SHORT).show();
    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        //tvAppend(textView,"\nSerial Connection Closed! \n");
        Toast.makeText(this, "Serial Connection Closed!", Toast.LENGTH_SHORT).show();
    }

    public void onClickClear(View view) {
        textView.setText("");
    }

    public void onClickCh1(View view) {
        String string = "1";
        serialPort.write(string.getBytes());
    }

    public void onClickCh2(View view) {
        String string = "2";
        serialPort.write(string.getBytes());
    }

    public void onClickCh1Ch2(View view) {
        String string = "3";
        serialPort.write(string.getBytes());
    }

    public void onClickReset(View view) {
        isStart = !isStart;
        String string;
        if(isStart){
            string = "rs";
        } else {
            string = "s";
        }
        serialPort.write(string.getBytes());
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
                scroll.post(new Runnable()
                {
                    public void run()
                    {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }
}
