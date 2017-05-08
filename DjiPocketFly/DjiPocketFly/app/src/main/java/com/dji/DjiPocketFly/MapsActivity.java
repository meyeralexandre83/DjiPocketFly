package com.dji.DjiPocketFly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import dji.common.flightcontroller.FlightControllerState;
import dji.midware.data.manager.P3.ServiceManager;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class MapsActivity extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener,OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private Marker droneMarker = null;
    private FlightController mFlightController;

    private Button mBtnLocate;
    private Button mBtnShow;

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void initUI() {

        mBtnLocate = (Button) findViewById(R.id.btn_locate);
        mBtnShow = (Button) findViewById(R.id.btn_show);

        mBtnLocate.setOnClickListener(this);
        mBtnShow.setOnClickListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Register BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(DjiPocketFlyApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initUI();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        PolygonOptions rectOptions = new PolygonOptions();
        rectOptions.add(new LatLng(43.7,7.05));
        rectOptions.add(new LatLng(43.71,7.05));
        rectOptions.add(new LatLng(43.71,7.06));
        rectOptions.add(new LatLng(43.7,7.06));

        Polygon polygon = mMap.addPolygon(rectOptions);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(43.7,7.05)));

    }


    private void onProductConnectionChange() {
        initFlightController();
    }

    private void initFlightController() {
        Log.e(TAG,"initFlightController");
        BaseProduct product = DjiPocketFlyApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {

                Log.e(TAG,"product connected");
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    Log.e("FlightController","onUpdate");
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    Log.i("Latitude : ", String.valueOf(droneLocationLat));
                    Log.i("Logigtude : ", String.valueOf(droneLocationLng));
                    updateDroneLocation();
                }
            });
        }
    }

    private void updateDroneLocation(){
        Log.e(TAG,"updateDroneLocation");
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_drone));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }
                if (checkGpsCoordinates(droneLocationLat, droneLocationLng)) {
                    droneMarker = mMap.addMarker(markerOptions);
                }
            }
        });
    }

    public static boolean checkGpsCoordinates(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    private void cameraUpdate(){
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        mMap.moveCamera(cu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_locate:{
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
