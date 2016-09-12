package pt.sights.activities;

import android.annotation.TargetApi;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import pt.sights.R;
import pt.sights.data.DataManager;
import pt.sights.data.Sight;
import pt.sights.listeners.MapsResultsListener;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	27th of February of 2015
 */
public class CityMapActivity extends AppCompatActivity implements MapsResultsListener, LocationListener {

	private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private DataManager dataManager;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		dataManager = (DataManager) getApplication();

		setUpMapIfNeeded();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.title_map);

		setSupportActionBar(toolbar);

		try {
			if (getSupportActionBar() != null) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
	 * installed) and the map has not already been instantiated.. This will ensure that we only ever
	 * call {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p/>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
	 * install/update the Google Play services APK on their device.
	 * <p/>
	 * A user can return to this FragmentActivity after following the prompt and correctly
	 * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
	 * have been completely destroyed during this process (it is likely that it would only be
	 * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();

			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the camera. In this case, we
	 * just add a marker near Africa.
	 * <p/>
	 * This should only be called once and when we are sure that {@link #mMap} is not null.
	 */
	private void setUpMap() {
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);

		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(38.7071, -9.1782));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

		mMap.moveCamera(center);
		mMap.animateCamera(zoom);

		List<Sight> sights = dataManager.getSightLocations();
		for (int i = 0; i < sights.size(); i++) {
			Sight sight = sights.get(i);
			String[] sightCoord = sight.getCoordinates();
			if (sightCoord != null) {
				mMap.addMarker(new MarkerOptions()
						.position(new LatLng(Double.parseDouble(sightCoord[0]),
								Double.parseDouble(sightCoord[1])))
						.title(sight.name));
			}
		}

		/*Location myLocation = mMap.getMyLocation();

		if (myLocation != null) {
			LatLng myLatLgn = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
			CameraPosition myPosition = new CameraPosition.Builder()
					.target(myLatLgn).zoom(13).bearing(90).tilt(30).build();
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
			CameraUpdate centerToLocation = CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
			CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

			mMap.moveCamera(centerToLocation);
			mMap.animateCamera(zoom);
		}*/
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mMap != null) {
			mMap.clear();

			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.title("My location");

			mMap.addMarker(markerOptions);
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(location.getLatitude(), location.getLongitude()), 13));
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onResultsFinished(GoogleMap map) {
		mMap = map;

		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.setMyLocationEnabled(true);
	}

	@Override
	public MapView getMapView() {
		return null;
	}
}
