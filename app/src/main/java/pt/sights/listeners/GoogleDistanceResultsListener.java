package pt.sights.listeners;

/**
 * Listener interface to be used by Google Distance API task.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	18th of March of 2015
 */
public interface GoogleDistanceResultsListener {

	void onResultsFinished(String distance);
	void onResultsFailed(Exception e);

}
