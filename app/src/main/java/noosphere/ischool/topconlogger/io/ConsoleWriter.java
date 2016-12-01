package noosphere.ischool.topconlogger.io;

import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class ConsoleWriter implements Writer {

    private TextView textView;
    private ScrollView scrollView;


    public ConsoleWriter(TextView textView, ScrollView scrollView) {
        this.textView = textView;
        this.scrollView = scrollView;
    }

    @Override
    public void writeMessage(String message) {
        textView.append(Html.fromHtml("<br/><font color=\"red\"> > </font>" + message));
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
