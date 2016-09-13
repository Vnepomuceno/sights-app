package pt.sights.listeners;

import android.graphics.Bitmap;

import pt.sights.data.Sight;

/**
 * Listener interface to be used by the Download Image task.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	3rd of December of 2014
 */
public interface UrlImageListener {

	void onResultsFinished(Bitmap[] image, Sight sight);

}
