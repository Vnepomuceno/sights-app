package pt.sights.listeners;

import android.graphics.Bitmap;

import pt.sights.data.Sight;

/**
 * Listener interface to be used by the Google Static Maps API task.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since 8th of December of 2014
 */
public interface StaticMapResultsListener {

	void onResultsFinished(Bitmap bitmap, Sight sight);
	void onResultsFailed(Exception e);

}
