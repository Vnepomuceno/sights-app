package pt.sights.listeners;

import pt.sights.data.Sight;

import java.util.List;

/**
 * Created by valternepomuceno on 22/11/2014.
 */
public interface TourismResultsListener {

	void onResultsFinished(List<Sight> listSights);

}
