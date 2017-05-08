package com.dji.DjiPocketFly;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

public class DjiPocketFlyApplication extends Application{

    public static final String FLAG_CONNECTION_CHANGE = "fpv_connection_change";
    private static final String TAG = DjiPocketFlyApplication.class.getName();


    private static BaseProduct mProduct;

    private Handler mHandler;

    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler(Looper.getMainLooper());


        // On initialise le sdk pour l'utilisation des services
        DJISDKManager.getInstance().registerApp(this, mDJISDKManagerCallback);

    }

    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {

        @Override
        public void onRegister(DJIError error) {

            Log.d(TAG, error == null ? "Success" : error.getDescription());
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Validation de l'api Key", Toast.LENGTH_LONG).show();

                    }
                });

                DJISDKManager.getInstance().startConnectionToProduct(); // Initialisation de la entre l'application et le drone.

                Log.d(TAG, "Register success");

            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Echec de la validation de l'api, veuillez verifier votre réseau cellulaire", Toast.LENGTH_LONG).show();

                    }
                });

                Log.d(TAG, "Register failed");

            }
            Log.e(TAG, error == null ? "success" : error.getDescription());
        }

        // Verification du changeement de la connection du drone ou changement du drone

        @Override
        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {

            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setBaseProductListener(mDJIBaseProductListener);
            }
            notifyStatusChange(); // On notifie le changement en appelant la fonction
        }
    };

    private BaseProduct.BaseProductListener mDJIBaseProductListener = new BaseProduct.BaseProductListener() {

        @Override
        public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
            if(newComponent != null) {
                newComponent.setComponentListener(mDJIComponentListener);
            }
            notifyStatusChange(); // On notifie le changement en appelant la fonction
        }

        @Override
        public void onConnectivityChange(boolean isConnected) {

            notifyStatusChange();// On notifie le changement en appelant la fonction
        }

    };

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft){
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }

    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }

    };

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

}