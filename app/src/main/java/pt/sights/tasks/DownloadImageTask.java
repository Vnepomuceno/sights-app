package pt.sights.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

import pt.sights.data.Sight;
import pt.sights.listeners.UrlImageListener;

/**
 * Created by valternepomuceno on 07/12/14.
 */
public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap[]> {

	private Sight sight;
	private UrlImageListener listener;

	public void setOnResultsListener(UrlImageListener listener, Sight sight) {
		this.listener = listener;
		this.sight = sight;
	}

	@Override
	protected Bitmap[] doInBackground(String... params) {
		Bitmap[] bitmaps = new Bitmap[params.length];

		for (int i = 0; i < params.length; i++) {
			String urlDisplay = params[i];
			Bitmap mIcon = null;

			try {
				InputStream in = new URL(urlDisplay).openStream();
				mIcon = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mIcon != null) bitmaps[i] = mIcon;
		}

		return bitmaps;
	}

	protected void onPostExecute(Bitmap[] result) {
		listener.onResultsFinished(result, sight);
	}

}
