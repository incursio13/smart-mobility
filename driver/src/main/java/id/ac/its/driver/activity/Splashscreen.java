package id.ac.its.driver.activity;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pixplicity.easyprefs.library.Prefs;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import id.ac.its.driver.R;
import id.ac.its.driver.model.Halte;
import id.ac.its.driver.model.Lyn;

public class Splashscreen extends AppCompatActivity {
    private AVLoadingIndicatorView avi;
    private DatabaseReference databaseHalte, databaseLyn;
    Halte halte;
    Lyn lyn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName("EzLyn")
                .setUseDefaultSharedPreference(true)
                .build();
        if (!Prefs.getBoolean("logged", false)) {
            Prefs.putBoolean("logged", true);
            databaseLyn = FirebaseDatabase.getInstance().getReference("lyn");
            databaseLyn.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild("lyn")) {
                        lyn = new Lyn("L 12 A", 5000, true, false, -7.275622, 112.793449);
                        databaseLyn.child(lyn.getPlate()).setValue(lyn);
                        lyn = new Lyn("L 23 B", 6000, true, true, -7.280443, 112.781068);
                        databaseLyn.child(lyn.getPlate()).setValue(lyn);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            databaseHalte = FirebaseDatabase.getInstance().getReference("halte");
            databaseHalte.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild("halte")) {
//                        halte = new Halte("Halte 1", 0, -7.279890, 112.784973);
//                        databaseHalte.child(halte.getName()).setValue(halte);
//                        halte = new Halte("Halte 2", 0, -7.279337, 112.789393);
//                        databaseHalte.child(halte.getName()).setValue(halte);
//                        halte = new Halte("Halte 3", 0, -7.278337, 112.789393);
//                        databaseHalte.child(halte.getName()).setValue(halte);
//                        halte = new Halte("Halte 4", 0, -7.277337, 112.789393);
//                        databaseHalte.child(halte.getName()).setValue(halte);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                avi.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(Splashscreen.this, Menu.class);
                        startActivity(i);
                        finish();
                    }
                }, 2000);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Log.e("Dexter", "There was an error: " + error.toString());
            }
        }).check();
    }
}
