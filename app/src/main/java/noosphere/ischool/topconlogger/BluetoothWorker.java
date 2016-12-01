package noosphere.ischool.topconlogger;

import android.app.Application;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class BluetoothWorker {

    private static BluetoothWorker bluetoothWorker;
    private BluetoothSPP bluetoothSPP;


    private BluetoothWorker(Application application) {
        bluetoothSPP = new BluetoothSPP(application);
    }

    public static BluetoothWorker getInstance(Application application) {
        if(bluetoothWorker == null) {
            bluetoothWorker = new BluetoothWorker(application);
        }
        return bluetoothWorker;
    }

    public BluetoothSPP getBluetoothSPP() {
        return bluetoothSPP;
    }
}
