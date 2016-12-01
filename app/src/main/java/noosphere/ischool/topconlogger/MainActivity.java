package noosphere.ischool.topconlogger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import noosphere.ischool.topconlogger.io.ConsoleWriter;
import noosphere.ischool.topconlogger.io.FileWriter;
import noosphere.ischool.topconlogger.io.Writer;


public class MainActivity  extends AppCompatActivity {


    public static final String EXTRA_IS_RUNNING = "EXTRA_IS_RUNNING";
    private FloatingActionButton starter;
    private BluetoothSPP bt;
    private List<Writer> writers;
    private ProgressDialog progressDialog;


    private boolean isRunning;

    @Override
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
        isRunning = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpBluetooth();

        this.starter = (FloatingActionButton) findViewById(R.id.starter);
        starter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRunning) {
                    isRunning = false;
                    bt.send(CommandSet.STOP_RECEIVE_DATA, true);
                    makeButtonStart();
                    Toast.makeText(MainActivity.this,  R.string.data_stopped, Toast.LENGTH_LONG).show();
                } else {
                    buildWriters();
                    bt.send(CommandSet.START_RECEIVE_DATA, true);
                    makeButtonStop();
                    Toast.makeText(MainActivity.this,  R.string.data_started, Toast.LENGTH_LONG).show();
                    isRunning = true;
                }
            }
        });
        if(isRunning) {
            buildWriters();
            makeButtonStop();
        } else {
            makeButtonStart();
        }
    }

    private void makeButtonStart() {
        starter.setImageDrawable(getResources().getDrawable(R.drawable.start));
        ColorStateList rippleColor = ContextCompat.getColorStateList(this, R.color.colorStart);
        starter.setBackgroundTintList(rippleColor);
    }

    private void makeButtonStop() {
        starter.setImageDrawable(getResources().getDrawable(R.drawable.stop));
        ColorStateList rippleColor = ContextCompat.getColorStateList(this, R.color.colorStop);
        starter.setBackgroundTintList(rippleColor);
    }

    private void buildWriters() {
        writers = new ArrayList<>();
        TextView data = (TextView) findViewById(R.id.data);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        writers.add(new ConsoleWriter(data, scrollView));

        writers.add(new FileWriter());
    }

    public void setUpBluetooth() {
        bt = BluetoothWorker.getInstance(getApplication()).getBluetoothSPP();
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(MainActivity.this, R.string.device_connected, Toast.LENGTH_LONG).show();
                closeConnectionProgress();
                starter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDeviceDisconnected() {
                Toast.makeText(MainActivity.this, R.string.device_disconnected, Toast.LENGTH_LONG).show();
                closeConnectionProgress();
                starter.setVisibility(View.GONE);
                isRunning = false;
                makeButtonStart();
            }

            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(MainActivity.this, R.string.device_connection_failed, Toast.LENGTH_LONG).show();
                starter.setVisibility(View.GONE);
                closeConnectionProgress();
                isRunning = false;
                makeButtonStart();
            }
        });
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                for(Writer writer : writers) {
                    writer.writeMessage(message);
                }
            }
        });
    }

    private void showConnectionProgress(String deviceName) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting to " + deviceName);
        progressDialog.show();
    }

    private void closeConnectionProgress() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                if(data != null) {
                    showConnectionProgress(data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS));
                    bt.connect(data);
                }
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connect) {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_RUNNING, isRunning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isRunning = savedInstanceState.getBoolean(EXTRA_IS_RUNNING);
    }
}
