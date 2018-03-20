package id.ac.its.intravtasuser.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.ac.its.intravtasuser.R;

/**
 * Created by exod on 8/28/2017.
 */

public class MenuActivity extends AppCompatActivity {
    @BindView(R.id.menuAngkot)
    Button menuAngkot;
    @BindView(R.id.menuNavigasi)
    Button menuNavigasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.menuAngkot, R.id.menuNavigasi})
    public void onViewClicked(View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        switch ( view.getId()){
            case R.id.menuAngkot:
                progressDialog.setMessage("Loading");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
                break;
            case R.id.menuNavigasi:
                progressDialog.setMessage("Loading");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Intent intent = new Intent(MenuActivity.this, Navigation.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
                break;
        }
    }

}
