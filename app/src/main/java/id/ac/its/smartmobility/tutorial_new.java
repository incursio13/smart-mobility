package id.ac.its.smartmobility;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class tutorial_new extends AppCompatActivity {
    private ImageSwitcher swt;
    private Button pr, nx;
    private int now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_new);
        swt = (ImageSwitcher) findViewById(R.id.imageSwt);
        pr = (Button) findViewById(R.id.previous);
        nx = (Button) findViewById(R.id.next);
        now = 0;
        swt.setFactory(new ViewSwitcher.ViewFactory(){
                           @Override
                           public View makeView() {
                               ImageView img = new ImageView(getApplicationContext());
                               img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                               return img;
                           }
                       }
        );
        swt.setImageResource(R.drawable.tutor_sub_1);
        pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(now > 0) {
                    now -= 1;
                }
                if(now == 0){
                    swt.setImageResource(R.drawable.tutor_sub_1);
                }
                else if(now == 1){
                    swt.setImageResource(R.drawable.tutor_sub_2);
                }
                else if(now == 2){
                    swt.setImageResource(R.drawable.tutor_sub_3);
                }
                else if(now == 3){
                    swt.setImageResource(R.drawable.tutor_sub_4);
                }
                else if (now == 4 || now == -1) {
                    Intent intent = new Intent(tutorial_new.this, id.ac.its.smartmobility.activity.MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        nx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (now < 4) {
                    now += 1;
                }
                if (now == 0) {
                    swt.setImageResource(R.drawable.tutor_sub_1);
                } else if (now == 1) {
                    swt.setImageResource(R.drawable.tutor_sub_2);
                } else if (now == 2) {
                    swt.setImageResource(R.drawable.tutor_sub_3);
                } else if (now == 3) {
                    swt.setImageResource(R.drawable.tutor_sub_4);
                }
                else if (now == 4 || now == -1) {
                    Intent intent = new Intent(tutorial_new.this, id.ac.its.smartmobility.activity.MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
