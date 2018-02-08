package id.ac.its.smartmobility.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.ac.its.smartmobility.R;
import id.ac.its.smartmobility.model.Halte;
import id.ac.its.smartmobility.model.Lyn;

public class LynListActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    MarkerOptions markerOptions;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Halte halte;
    Marker marker_h;
    Lyn lyn;
    List<Lyn> lyns;
    boolean locsetted = false;
    int jumlahlyn = 0;
    DatabaseReference databaseHalte, databaseLyn;
    Polyline polyline;
    TextView full, nama, jarak;
    PolylineOptions[] map_poli = new PolylineOptions[100];
    String[] map_distance = new String[100];
    String[] map_duration = new String[100];
    Marker[] marker_lyns = new Marker[100];
    Handler mHandler;
    Runnable mAnimation;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_lyn_code)
    TextView tvLynCode;
    @BindView(R.id.tv_lyn_destination)
    TextView tvLynDestination;
    @BindView(R.id.tv_lyn_eta)
    TextView tvLynEta;
    @BindView(R.id.tv_lyn_fee)
    TextView tvLynFee;
    @BindView(R.id.tv_lyn_status)
    TextView tvLynStatus;
    @BindView(R.id.lyn)
    CardView cardlyn;
    @BindView(R.id.selesai)
    Button selesai;
    ProgressDialog cover;
    int checked=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyn_list);
        ButterKnife.bind(this);
        cover = new ProgressDialog(this);
        cover.setMessage("Memproses");
        cover.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        cover.setCancelable(false);
        cover.setCanceledOnTouchOutside(false);
        status();
        halte = Parcels.unwrap(getIntent().getParcelableExtra("halte"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryText, null));
        databaseHalte = FirebaseDatabase.getInstance().getReference("halte");
        databaseLyn = FirebaseDatabase.getInstance().getReference("lyn");
        mapFrag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        //
        databaseLyn.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (locsetted&&lyns.size()>0) {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        lyn = dsp.getValue(Lyn.class);

                            int index = getIndex(lyn);
                            lyns.set(index,lyn);
                            if (marker_lyns[index] != null) marker_lyns[index].remove();
                        if (lyn.isStatus()) {
                            markerOptions = new MarkerOptions();
                            LatLng lynloc = new LatLng(lyn.getLat(), lyn.getLng());
                            markerOptions.position(lynloc);
                            markerOptions.title(lyn.getPlate());
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_angkot));
                            marker_lyns[index] = mGoogleMap.addMarker(markerOptions);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Oye", "Failed to read value.", error.toException());
            }
        });

    }

    @OnClick(R.id.selesai)
    public void onViewClicked() {
        databaseHalte.child(halte.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int waiting = dataSnapshot.getValue(Halte.class).getWaiting();
                waiting = waiting - 1;
                databaseHalte.child(halte.getName()).child("waiting").setValue(waiting);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Selesai");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Intent intent = new Intent(LynListActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.infolyn, null);
                nama = (TextView) v.findViewById(R.id.nama);
                jarak = (TextView) v.findViewById(R.id.jarak);
                full = (TextView) v.findViewById(R.id.full);
                nama.setText(marker.getTitle());
                if (marker.getTitle().contentEquals(halte.getName())) {

                    jarak.setText((halte.getWaiting()+1)+" penunggu");
                    //jarak.setVisibility(View.GONE);
                    full.setVisibility(View.GONE);
                    cardlyn.setVisibility(View.GONE);
                    if (polyline != null) {
                        polyline.remove();
                    }
                } else {
                    tvLynDestination.setCompoundDrawables(
                            new IconicsDrawable(LynListActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_directions_car)
                                    .color(ResourcesCompat.getColor(getResources(), R.color.colorSecondaryText, null))
                                    .actionBar(),
                            null, null, null);
                    tvLynEta.setCompoundDrawables(
                            new IconicsDrawable(LynListActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_av_timer)
                                    .color(ResourcesCompat.getColor(getResources(), R.color.colorSecondaryText, null))
                                    .actionBar(),
                            null, null, null);
                    tvLynFee.setCompoundDrawables(
                            new IconicsDrawable(LynListActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_attach_money)
                                    .color(ResourcesCompat.getColor(getResources(), R.color.colorSecondaryText, null))
                                    .actionBar(),
                            null, null, null);
                    tvLynStatus.setCompoundDrawables(
                            new IconicsDrawable(LynListActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_group)
                                    .color(ResourcesCompat.getColor(getResources(), R.color.colorSecondaryText, null))
                                    .actionBar(),
                            null, null, null);
                    cardlyn.setVisibility(View.VISIBLE);
                    if (lyns.size()>0) {
                        for (Lyn h : lyns) {
                            if (h.getPlate().contentEquals(marker.getTitle())) {

                                final int index = lyns.indexOf(h);
                                lyn = h;
                                if (map_poli[index]==null){
                                    final LatLng halteloc = new LatLng(halte.getLat(), halte.getLng());
                                    LatLng lynloc = new LatLng(lyn.getLat(), lyn.getLng());
                                    GoogleDirection.withServerKey(getResources().getString(R.string.google_maps_key))
                                            .from(halteloc)
                                            .to(lynloc)
                                            .unit(Unit.METRIC)
                                            .transitMode(TransportMode.DRIVING)
                                            .execute(new DirectionCallback() {
                                                @Override
                                                public void onDirectionSuccess(Direction direction, String rawBody) {
                                                    if (direction.isOK()) {
                                                        ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                                        map_duration[index] = direction.getRouteList().get(0).getLegList().get(0).getDuration().getText();
                                                        map_distance[index] = direction.getRouteList().get(0).getLegList().get(0).getDistance().getText();
                                                        map_poli[index] = DirectionConverter.createPolyline(LynListActivity.this, directionPositionList, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));

                                                        if (lyn.isFull()) {
                                                            full.setText("Penuh");
                                                            tvLynStatus.setText("Penuh");
                                                        } else {
                                                            full.setText("Tersedia");
                                                            tvLynStatus.setText("Tersedia");
                                                        }
                                                        tvLynCode.setText(lyn.getPlate());
                                                        tvLynDestination.setText(map_distance[index]);
                                                        jarak.setText(map_distance[index]);
                                                        tvLynEta.setText(map_duration[index]);
                                                        tvLynFee.setText("Rp " + lyn.getPrice());
                                                        if (polyline != null) {
                                                            polyline.remove();
                                                        }

                                                        if (map_poli[index]!=null)
                                                            polyline = mGoogleMap.addPolyline(map_poli[index]);
                                                    } else {
                                                        Log.d("mapse", rawBody);
                                                        // Do something
                                                    }
                                                }

                                                @Override
                                                public void onDirectionFailure(Throwable t) {
                                                    Log.d("mapse", t.toString());
                                                }
                                            });
                                }
                                else{
                                    if (h.isFull()) {
                                        full.setText("Penuh");
                                        tvLynStatus.setText("Penuh");
                                    } else {
                                        full.setText("Tersedia");
                                        tvLynStatus.setText("Tersedia");
                                    }
                                    tvLynCode.setText(h.getPlate()+"\n"+h.getRoute());
                                    tvLynDestination.setText(map_distance[index]);
                                    jarak.setText(map_distance[index]);
                                    tvLynEta.setText(map_duration[index]);
                                    tvLynFee.setText("Rp " + h.getPrice());
                                    if (polyline != null) {
                                        polyline.remove();
                                    }

                                    if (map_poli[index]!=null)
                                        polyline = mGoogleMap.addPolyline(map_poli[index]);
                                }
                                break;
                            }
                        }
                    }
                }
                return v;
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (polyline != null) {
                    polyline.remove();
                }
                cardlyn.setVisibility(View.GONE);
            }
        });
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                mHandler = new Handler();
                final long start = SystemClock.uptimeMillis();
                final long duration = 400L;
                mHandler.removeCallbacks(mAnimation);
                mAnimation = new BounceAnimation(start, duration, marker, mHandler);
                mHandler.post(mAnimation);
                marker.showInfoWindow();
                return false;
            }
        });
    }


    private static class BounceAnimation implements Runnable {

        private final long mStart, mDuration;
        private final Interpolator mInterpolator;
        private final Marker mMarker;
        private final Handler mHandler;

        private BounceAnimation(long start, long duration, Marker marker, Handler handler) {
            mStart = start;
            mDuration = duration;
            mMarker = marker;
            mHandler = handler;
            mInterpolator = new BounceInterpolator();
        }

        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - mStart;
            float t = Math.max(1 - mInterpolator.getInterpolation((float) elapsed / mDuration), 0f);
            mMarker.setAnchor(0.5f, 1.0f + 0.5f * t);

            if (t > 0.0) {
                // Post again 16ms later.
                mHandler.postDelayed(this, 16L);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (!locsetted) {
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            lyns = new ArrayList<>();
            markerOptions = new MarkerOptions();
            final LatLng halteloc = new LatLng(halte.getLat(), halte.getLng());
            builder.include(halteloc);
            markerOptions.position(halteloc);
            markerOptions.title(halte.getName());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_halte));
            marker_h = mGoogleMap.addMarker(markerOptions);
            databaseLyn.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
//                        jumlahlyn++;
//                    }
//                    marker_lyns = new Marker[jumlahlyn + 1];
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            lyn = dsp.getValue(Lyn.class);
                            lyns.add(lyn);
                            final int index = getIndex(lyn);
                        if (lyn.isStatus()) {
                            markerOptions = new MarkerOptions();
                            LatLng lynloc = new LatLng(lyn.getLat(), lyn.getLng());
                            markerOptions.position(lynloc);
                            builder.include(lynloc);
                            markerOptions.title(lyn.getPlate());
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_angkot));
                            marker_lyns[index] = mGoogleMap.addMarker(markerOptions);
                            GoogleDirection.withServerKey(getResources().getString(R.string.google_maps_key))
                                    .from(halteloc)
                                    .to(lynloc)
                                    .unit(Unit.METRIC)
                                    .transitMode(TransportMode.DRIVING)
                                    .execute(new DirectionCallback() {
                                        @Override
                                        public void onDirectionSuccess(Direction direction, String rawBody) {
                                            if (direction.isOK()) {
                                                ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                                map_duration[index] = direction.getRouteList().get(0).getLegList().get(0).getDuration().getText();
                                                map_distance[index] = direction.getRouteList().get(0).getLegList().get(0).getDistance().getText();
                                                map_poli[index] = DirectionConverter.createPolyline(LynListActivity.this, directionPositionList, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                                            } else {
                                                Log.d("mapse", rawBody);
                                                // Do something
                                            }
                                        }

                                        @Override
                                        public void onDirectionFailure(Throwable t) {
                                            Log.d("mapse", t.toString());
                                        }
                                    });
                        }
                    }
                    LatLngBounds bounds = builder.build();
                    cover.dismiss();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                    mGoogleMap.moveCamera(cu);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("Oye", "Failed to read value.", error.toException());
                }
            });
            locsetted = true;
        }
    }

    private int getIndex(Lyn lyn){
        int index = 0;
        for (Lyn l : lyns) {
            if (l.getPlate() == lyn.getPlate()) {
                index = lyns.indexOf(l);
            }
        }
        return index;
    }
    public void statusCheckInt() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null) {
            if (connectivity.getActiveNetworkInfo().isConnected()) checked++;
            else buildAlertMessageNoInt();
        }else buildAlertMessageNoInt();
    }

    public void statusCheckGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) buildAlertMessageNoGps();
        else checked++;
    }

    public void status(){
        statusCheckInt();
        statusCheckGPS();
        if(checked==2)cover.show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Mohon aktifkan GPS Anda")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoInt() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Mohon aktifkan Internet Anda")
                .setCancelable(false)
                .setPositiveButton("Data", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings$DataUsageSummaryActivity"));
                        startActivity(intent);
                    }
                }).setNegativeButton("Wifi", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void onBackPressed() {


    }

}
