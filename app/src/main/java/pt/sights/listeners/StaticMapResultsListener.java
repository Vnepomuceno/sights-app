package pt.sights.listeners;

import android.graphics.Bitmap;

import pt.sights.data.Sight;

/**
 * Created by valternepomuceno on 08/12/14.
 */
public interface StaticMapResultsListener {

	void onResultsFinished(Bitmap bitmap, Sight sight);
	void onResultsFailed(Exception e);

}
