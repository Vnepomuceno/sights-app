package pt.sights.listeners;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

/**
 * Created by valternepomuceno on 29/11/2014.
 */
public interface MapsResultsListener {

	void onResultsFinished(GoogleMap map);
	MapView getMapView();

}
