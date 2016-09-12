package pt.sights.listeners;

import android.graphics.Bitmap;

import pt.sights.data.Sight;


/**
 * Created by valternepomuceno on 03/12/2014.
 */
public interface UrlImageListener {

	void onResultsFinished(Bitmap[] image, Sight sight);

}
