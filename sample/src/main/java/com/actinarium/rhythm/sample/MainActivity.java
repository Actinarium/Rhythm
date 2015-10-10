package com.actinarium.rhythm.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.RhythmDrawable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.frame);
        View subView = findViewById(R.id.subframe);
        final RhythmControl rhythmControl = ((RhythmSampleApplication) getApplication()).getRhythmControl();
        view.setBackgroundDrawable(rhythmControl.getGroup(0).makeDrawable());
        final RhythmDrawable drawable = rhythmControl.getGroup(1).makeDrawable();
//        drawable.setBounds(new Rect(subView.getLeft(), subView.getTop(), subView.getRight(), subView.getBottom()));
//        subView.getOverlay().add(drawable);
        drawable.setBackgroundDrawable(subView.getBackground());
        subView.setBackgroundDrawable(drawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
