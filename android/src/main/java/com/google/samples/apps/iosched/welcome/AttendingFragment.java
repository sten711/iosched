/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.samples.apps.iosched.welcome;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.settings.SettingsUtils;

import static com.google.samples.apps.iosched.util.LogUtils.LOGD;
import static com.google.samples.apps.iosched.util.LogUtils.makeLogTag;

/**
 * The attending in person fragment in the welcome screen.
 */
public class AttendingFragment extends WelcomeFragment implements WelcomeActivity.WelcomeActivityContent, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = makeLogTag(AttendingFragment.class);
    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 102;
    private GoogleApiClient apiClient;

    @Override
    public boolean shouldDisplay(Context context) {
        return !SettingsUtils.hasAnsweredLocalOrRemote(context);
    }


    @Override
    protected View.OnClickListener getPositiveListener() {
        return new WelcomeFragmentOnClickListener(mActivity) {
            @Override
            public void onClick(View v) {
                // Ensure we don't run this fragment again
                LOGD(TAG, "Marking attending flag.");
                SettingsUtils.setAttendeeAtVenue(mActivity, true);
                SettingsUtils.markAnsweredLocalOrRemote(mActivity, true);
                doNext();
            }
        };
    }

    @Override
    protected View.OnClickListener getNegativeListener() {
        return new WelcomeFragmentOnClickListener(mActivity) {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "Marking not attending flag.");
                SettingsUtils.setAttendeeAtVenue(mActivity, false);
                SettingsUtils.markAnsweredLocalOrRemote(mActivity, true);
                doNext();
            }
        };
    }

    @Override
    protected String getPositiveText() {
        return getResourceString(R.string.attending_in_person);
    }

    @Override
    protected String getNegativeText() {
        return getResourceString(R.string.attending_remotely);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.welcome_attending_fragment, container, false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (!requestPermissionIfNeeded(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_COARSE_LOCATION_PERMISSION)) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            handleLastLocation(lastLocation);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    ((TextView) getView().findViewById(R.id.welcome_attending_textview)).setText(R.string.welcome_to_mv);
                }
            });
        }
    }

    private void handleLastLocation(Location lastLocation) {
        if (lastLocation != null) {

            //SF
            Location location = new Location(LocationManager.PASSIVE_PROVIDER);
            location.setLatitude(37.426);
            location.setLongitude(-122.080);

            //If in 100km range
            if (lastLocation.distanceTo(location) < 1000 * 100) {
                ((TextView) getView().findViewById(R.id.welcome_attending_textview)).setText(R.string.welcome_to_mv);            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();

        apiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        apiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        apiClient.disconnect();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_COARSE_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions.length > 0) {
                if (apiClient.isConnected()) {
                    handleLastLocation(LocationServices.FusedLocationApi.getLastLocation(apiClient));
                }
            }
        }
    }
}
