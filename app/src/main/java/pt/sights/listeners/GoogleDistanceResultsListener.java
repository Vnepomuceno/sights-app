package pt.sights.listeners;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	18th of March of 2015
 */
public interface GoogleDistanceResultsListener {

	void onResultsFinished(String distance);
	void onResultsFailed(Exception e);

}
