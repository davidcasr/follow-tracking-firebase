package co.davidcasr.followtrackingfirebasegooglemaps;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import co.davidcasr.followtrackingfirebasegooglemaps.models.Ubication;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "FollowTrackingFirebase";

    private String FIREBASE_URL = "https://trackingfirebase.firebaseio.com/";
    private String FIREBASE_USER = "user1";

    Firebase firebase;

    private boolean isFirstMessage = true;
    private boolean mRequestingLocationUpdates = false;
    private MenuItem mFollowButton;

    // Google Maps
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private Marker mMarker;
    private MarkerOptions mMarkerOptions;
    private LatLng mLatLng;
    private Polyline mPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start Firebase
        Firebase.setAndroidContext(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mFollowButton = menu.findItem(R.id.follow_locations);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);
        Log.e(TAG, "Map Ready");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.follow_locations:
                Log.e(TAG, "'Follow Route' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startFollowingLocation();
                    mFollowButton.setTitle("Stop Follow Route");
                }
                if (!mRequestingLocationUpdates) {
                    stopFollowingLocation();
                    mFollowButton.setTitle("Start Follow Route");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Query Firebase
    private void queryFirebase() {
        firebase = new Firebase(FIREBASE_URL);
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Ubication ubication = postSnapshot.getValue(Ubication.class);
                    double mLat = ubication.getLatitude();
                    double mLng = ubication.getLongitude();
                    mLatLng = new LatLng(mLat, mLng);
                    updatePolyline();
                    updateCamera();
                    updateMarker();
                    Log.e(TAG, String.valueOf(ubication.getLatitude()) + " " + String.valueOf(ubication.getLongitude()));

                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private void startFollowingLocation() {
        initializePolyline();
        queryFirebase();
    }

    private void stopFollowingLocation() {
        isFirstMessage = true;
    }

    // Map Editing Methods

    private void initializePolyline() {
        mGoogleMap.clear();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        mGoogleMap.addPolyline(mPolylineOptions);

        mMarkerOptions = new MarkerOptions();
    }

    private void updatePolyline() {
        mPolylineOptions.add(mLatLng);
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updateCamera() {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
    }

    private void updateMarker() {
        mMarker = mGoogleMap.addMarker(mMarkerOptions.position(mLatLng));
    }

}
