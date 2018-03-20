package id.ac.its.intravtasdriver.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.ac.its.intravtasdriver.R;
import id.ac.its.intravtasdriver.model.Lyn;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.plat_nomor)
    EditText platNomor;
    @BindView(R.id.route)
    EditText route;
    @BindView(R.id.harga)
    EditText harga;
    @BindView(R.id.next_regis)
    Button nextRegis;
    DatabaseReference databaseLyn;
    Lyn lyn;

    private LocationManager myManager;
    private LocationListener loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        if (!Prefs.getString("plat", "").isEmpty()) {
            move();
        }
        databaseLyn = FirebaseDatabase.getInstance().getReference("lyn");
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ifedit();
            }
        };
        route.addTextChangedListener(watcher);
        platNomor.addTextChangedListener(watcher);
        harga.addTextChangedListener(watcher);
    }

    private void ifedit() {
        if (!harga.getText().toString().isEmpty() && !platNomor.getText().toString().isEmpty()) {
            nextRegis.setVisibility(View.VISIBLE);
        } else nextRegis.setVisibility(View.GONE);
    }

    @OnClick(R.id.next_regis)
    public void onViewClicked() {
        String a = platNomor.getText().toString();
        int b = Integer.parseInt(harga.getText().toString());
        String c = route.getText().toString();
        lyn = new Lyn(a, b, false, true, -7.275622, 112.793449, c);
        databaseLyn.child(lyn.getPlate()).setValue(lyn);
        Prefs.putString("plat", a);
        Prefs.putInt("harga", b);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menyimpan");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                move();
            }
        }, 2000);
    }

    private void move() {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
        finish();
    }


}
