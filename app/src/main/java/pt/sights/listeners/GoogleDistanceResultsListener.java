package pt.sights.listeners;

/**
 * Created by valternepomuceno on 18/03/15.
 */
public interface GoogleDistanceResultsListener {

	void onResultsFinished(String distance);
	void onResultsFailed(Exception e);

}
