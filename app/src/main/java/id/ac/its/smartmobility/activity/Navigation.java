package id.ac.its.smartmobility.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
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
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.ac.its.smartmobility.R;

public class Navigation extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, AdapterView.OnItemClickListener {

    MarkerOptions markerOptions;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location myloc;
    Location prevLoc;
    LatLng destinationOnClick;
    Marker mymarker, destination;
    boolean locsetted = false;
    Polyline polyline1,polyline2, polyline3;
    Handler mHandler;
    Runnable mAnimation;
    ProgressDialog progressDialog,cover;
    @BindView(R.id.myCurrentLoc)
    ImageButton myCurrentLoc;
    @BindView(R.id.btnFindPath)
    Button btnFindPath;
    @BindView(R.id.toMaps)
    ImageButton toMaps;
    @BindView(R.id.tvDistance)
    TextView tvDistance;
    @BindView(R.id.tvDuration)
    TextView tvDuration;
    //    @BindView(R.id.etDestination)
//    EditText etDestination;
    @BindView(R.id.autoCompleteTextView)
    AutoCompleteTextView au;
    int checked=0;
    String direct1, direct2, direct3;
    String time1, time2, time3;
    String distemp;

    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBL15hFQ1S6wycaw4PCA0CEuZamw_JjoU4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);
        cover = new ProgressDialog(this);
        cover.setMessage("Memproses");
        cover.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        cover.setCancelable(false);
        cover.setCanceledOnTouchOutside(false);
        status();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName("SmartMobility")
                .setUseDefaultSharedPreference(true)
                .build();

        mapFrag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
//        TextWatcher watcher = new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//        };
//        etDestination.addTextChangedListener(watcher);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mGoogleMap.setMyLocationEnabled(false);
//            mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        }

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng latLng){
                au.setText("");
                destinationOnClick = latLng;
                Log.d("halte", "onMapLongClick: ");
                if(destination!=null){
                    destination.remove();
                }
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                destination=mGoogleMap.addMarker(markerOptions.flat(true));
                if (polyline1 != null) {
                    polyline1.remove();
                }
                GoogleDirection.withServerKey(getResources().getString(R.string.google_maps_key))
                        .from(new LatLng(myloc.getLatitude(), myloc.getLongitude()))
                        .to(latLng)
                        .unit(Unit.METRIC)
                        .transitMode(TransportMode.DRIVING)
                        .alternativeRoute(true)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                if (direction.isOK()) {
                                    int routeSize= direction.getRouteList().size();
                                    ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                    direct1=direction.getRouteList().get(0).getLegList().get(0).getDistance().getText();
                                    time1=direction.getRouteList().get(0).getLegList().get(0).getDuration().getText();
                                    tvDistance.setText(direct1);
                                    tvDuration.setText(time1);

//                            waktuHalte.setText(direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());
                                    if (polyline1 != null) {
                                        polyline1.remove();
                                    }
                                    if (polyline2 != null) {
                                        polyline2.remove();
                                    }
                                    if (polyline3 != null) {
                                        polyline3.remove();
                                    }
                                    polyline1 = mGoogleMap.addPolyline(DirectionConverter.createPolyline(Navigation.this, directionPositionList, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null)));
                                    polyline1.setClickable(true);
                                    polyline1.setTag("line1");


                                    if (routeSize>1) {
                                        ArrayList<LatLng> directionPositionList1 = direction.getRouteList().get(1).getLegList().get(0).getDirectionPoint();
                                        direct2=direction.getRouteList().get(1).getLegList().get(0).getDistance().getText();
                                        time2=direction.getRouteList().get(1).getLegList().get(0).getDuration().getText();
                                        polyline2 = mGoogleMap.addPolyline(DirectionConverter.createPolyline(Navigation.this, directionPositionList1, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimaryYellowLight, null)));
                                        polyline2.setClickable(true);
                                        polyline2.setTag("line2");

                                        if (routeSize>2){
                                            ArrayList<LatLng> directionPositionList2 = direction.getRouteList().get(2).getLegList().get(0).getDirectionPoint();
                                            direct3=direction.getRouteList().get(2).getLegList().get(0).getDistance().getText();
                                            time3=direction.getRouteList().get(2).getLegList().get(0).getDuration().getText();
                                            polyline3 = mGoogleMap.addPolyline(DirectionConverter.createPolyline(Navigation.this, directionPositionList2, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimaryGreyLight, null)));
                                            polyline3.setClickable(true);
                                            polyline3.setTag("line3");
                                        }
                                    }
                                } else {
                                    Log.d("mapse", rawBody);
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                Log.d("mapse", t.toString());
                            }
                        });
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

        mGoogleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener(){
            @Override
            public void onPolylineClick(Polyline poly){

                if (poly.getTag()=="line1"){
                    Log.d("halte", "onPolylineClick: "+poly.getTag());
                    tvDistance.setText(direct1);
                    tvDuration.setText(time1);
                    polyline1.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                    if (polyline2 != null) {
                        polyline2.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryYellowLight, null));
                    }
                    if (polyline3 != null) {
                        polyline3.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryGreyLight, null));
                    }

                }
                else if (poly.getTag()=="line2"){
                    tvDistance.setText(direct2);
                    tvDuration.setText(time2);
                    polyline2.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryYellow, null));
                    polyline1.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight2, null));
                    if (polyline3 != null) {
                        polyline3.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryGreyLight, null));
                    }
                }
                else if (poly.getTag()=="line3"){
                    tvDistance.setText(direct3);
                    tvDuration.setText(time3);
                    polyline3.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryGrey, null));
                    polyline1.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight2, null));
                    polyline2.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryYellowLight, null));
                }
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
        myloc = location;
        if(mymarker!=null){
            mymarker.remove();
        }
        float bearing = 0f;
        if (prevLoc!=null) {
            bearing = prevLoc.bearingTo(myloc);
        }


        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(myloc.getLatitude(), myloc.getLongitude()));
        markerOptions.title("My Location");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.up1));
        mymarker=mGoogleMap.addMarker(markerOptions.flat(true).anchor(0.5f,0.5f).rotation(bearing));
        if (!locsetted) {
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(myloc.getLatitude(), myloc.getLongitude()));
            LatLngBounds bounds = builder.build();
            cover.dismiss();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mGoogleMap.moveCamera(cu);
            mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
            locsetted = true;
        }
        prevLoc = myloc;
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

    @OnClick({R.id.myCurrentLoc, R.id.btnFindPath, R.id.toMaps})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.myCurrentLoc:
                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(myloc.getLatitude(), myloc.getLongitude()));
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myloc.getLatitude(), myloc.getLongitude()),15.0f));
                break;
            case R.id.toMaps:
                String a2 = au.getText().toString();
                List<Address> addressList2 = null;
                if (a2!=null && !a2.equals("")) {
                    Geocoder geocoder2 = new Geocoder(this);
                    try {
                        addressList2 = geocoder2.getFromLocationName(a2, 3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList2.size() > 0) {
                        Address address = addressList2.get(0);
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + address.getLatitude() + ", " + address.getLongitude() + "");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    }
                }
                else if (destinationOnClick!=null){
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destinationOnClick.latitude + ", " + destinationOnClick.longitude + "");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
                break;
            case R.id.btnFindPath:
                String a = au.getText().toString();
                List<Address> addressList = null;
                if (a!=null && !a.equals("")){
                    Log.d("halte", "onViewClicked: ");
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(a,3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList.size()>0) {
                        Address address = addressList.get(0);

                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        if (destination != null) {
                            destination.remove();
                        }
                        markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        destination = mGoogleMap.addMarker(markerOptions.flat(true));
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                        GoogleDirection.withServerKey(getResources().getString(R.string.google_maps_key))
                                .from(new LatLng(myloc.getLatitude(), myloc.getLongitude()))
                                .to(latLng)
                                .unit(Unit.METRIC)
                                .transitMode(TransportMode.DRIVING)
                                .alternativeRoute(true)
                                .execute(new DirectionCallback() {
                                    @Override
                                    public void onDirectionSuccess(Direction direction, String rawBody) {
                                        if (direction.isOK()) {
                                            int routeSize = direction.getRouteList().size();
                                            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                            direct1 = direction.getRouteList().get(0).getLegList().get(0).getDistance().getText();
                                            time1 = direction.getRouteList().get(0).getLegList().get(0).getDuration().getText();
                                            tvDistance.setText(direct1);
                                            tvDuration.setText(time1);

//                            waktuHalte.setText(direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());
                                            if (polyline1 != null) {
                                                polyline1.remove();
                                            }
                                            if (polyline2 != null) {
                                                polyline2.remove();
                                            }
                                            if (polyline3 != null) {
                                                polyline3.remove();
                                            }
                                            polyline1 = mGoogleMap.addPolyline(DirectionConverter.createPolyline(Navigation.this, directionPositionList, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null)));
                                            polyline1.setClickable(true);
                                            polyline1.setTag("line1");


                                            if (routeSize > 1) {
                                                ArrayList<LatLng> directionPositionList1 = direction.getRouteList().get(1).getLegList().get(0).getDirectionPoint();
                                                direct2 = direction.getRouteList().get(1).getLegList().get(0).getDistance().getText();
                                                time2 = direction.getRouteList().get(1).getLegList().get(0).getDuration().getText();
                                                polyline2 = mGoogleMap.addPolyline(DirectionConverter.createPolyline(Navigation.this, directionPositionList1, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimaryYellowLight, null)));
                                                polyline2.setClickable(true);
                                                polyline2.setTag("line2");

                                                if (routeSize > 2) {
                                                    ArrayList<LatLng> directionPositionList2 = direction.getRouteList().get(2).getLegList().get(0).getDirectionPoint();
                                                    direct3 = direction.getRouteList().get(2).getLegList().get(0).getDistance().getText();
                                                    time3 = direction.getRouteList().get(2).getLegList().get(0).getDuration().getText();
                                                    polyline3 = mGoogleMap.addPolyline(DirectionConverter.createPolyline(Navigation.this, directionPositionList2, 5, ResourcesCompat.getColor(getResources(), R.color.colorPrimaryGreyLight, null)));
                                                    polyline3.setClickable(true);
                                                    polyline3.setTag("line3");
                                                }
                                            }
                                        } else {
                                            Log.d("mapse", rawBody);
                                        }
                                    }

                                    @Override
                                    public void onDirectionFailure(Throwable t) {
                                        Log.d("mapse", t.toString());
                                    }
                                });
                    }
                }
                break;
        }
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);

            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
//            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return (String) resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    public void onBackPressed(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Intent intent = new Intent(Navigation.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);

    }

}
