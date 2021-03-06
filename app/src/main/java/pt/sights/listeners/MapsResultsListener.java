package pt.sights.listeners;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

/**
 * Listener interface to be used by activity City Map.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	29th of November of 2014
 */
public interface MapsResultsListener {

	void onResultsFinished(GoogleMap map);
	MapView getMapView();

}
