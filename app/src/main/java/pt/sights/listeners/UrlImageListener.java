package pt.sights.listeners;

import android.graphics.Bitmap;

import pt.sights.data.Sight;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	3rd of December of 2014
 */
public interface UrlImageListener {

	void onResultsFinished(Bitmap[] image, Sight sight);

}
