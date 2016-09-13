package pt.sights.listeners;

import pt.sights.data.Sight;

import java.util.List;

/**
 * Listener interface to be used by the CitySDK query task.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	22th of November of 2014
 */
public interface TourismResultsListener {

	void onResultsFinished(List<Sight> listSights);

}
