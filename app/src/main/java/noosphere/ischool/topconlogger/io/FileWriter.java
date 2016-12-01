package noosphere.ischool.topconlogger.io;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileWriter implements Writer {

    private String filename;

    public FileWriter() {
        Date date = new Date();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US);
        String s = formatter.format(date);

        filename = "topcon_log_" + s;
    }

    @Override
    public void writeMessage(String message) {
        File file = new File(Environment.getExternalStorageDirectory().toString(), filename);
        try {
            java.io.FileWriter fw = new java.io.FileWriter(file, true);
            fw.append(message);
            fw.close();

        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }
}
