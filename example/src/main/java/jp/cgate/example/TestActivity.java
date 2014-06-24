package jp.cgate.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import jp.cgate.example.*;

public class TestActivity extends Activity
{
    private static final String TAG = TestActivity.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.root);


        BannerView b = new BannerView(this);
        b.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        rootLayout.addView(b, new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));


        /*
        final TestActivity a = this;
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click");

                WallActivity.show(a);
            }
        });
        */


    }
}
