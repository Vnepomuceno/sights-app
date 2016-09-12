package pt.sights.listeners;

import pt.sights.data.Sight;

import java.util.List;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	22th of November of 2014
 */
public interface TourismResultsListener {

	void onResultsFinished(List<Sight> listSights);

}
