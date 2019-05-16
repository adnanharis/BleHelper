package in.rootcode.bt_0_location.in.rootcode.locationhelper;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by Adnan Haris on 6/4/18.
 */

public class LocationHandler {

    public static void setUp(FragmentActivity activity) {
        LocationApi.getInstance().setUpGClient(activity);
    }

    public static void cleanUp() {
        LocationApi.getInstance().cleanUpGClient();
    }
}

class LocationApi implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static volatile LocationApi instance;
    private GoogleApiClient googleApiClient;
    private FragmentActivity mActivity;
    private static final int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private static final String LOG_TAG = LocationApi.class.getSimpleName();

    private LocationApi() { }

    static LocationApi getInstance() {
        if (instance == null) {
            synchronized (LocationApi.class) {
                if (instance == null) {
                    instance = new LocationApi();
                }
            }
        }
        return instance;
    }

    synchronized void setUpGClient(FragmentActivity activity) {
        mActivity = activity;
        if (googleApiClient == null) {
            // Null check to avoid location client getting initialized and managed again with same id 0
            googleApiClient = new GoogleApiClient.Builder(activity)
                    .enableAutoManage(activity, 0, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            enableGps();
        }
    }

    synchronized void cleanUpGClient() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(googleApiClient, this);
            googleApiClient.stopAutoManage(mActivity);
            googleApiClient.disconnect();
            googleApiClient = null;
            mActivity = null;
        }
    }

    private void enableGps() {
        if (googleApiClient == null || mActivity == null || !googleApiClient.isConnected()) {
            return;
        }

        int permissionLocation = ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(3 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, this);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Nothing to anything. GPS is already ON. BLE will work
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Ask to turn on GPS automatically
                            status.startResolutionForResult(mActivity,
                                    REQUEST_CHECK_SETTINGS_GPS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(LOG_TAG, "Error while asking user for GPS permission", e);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied.
                        // However, we have no way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        enableGps();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) { }
}