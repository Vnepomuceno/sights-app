package pt.sights.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import pt.sights.data.Sight;
import pt.sights.listeners.StaticMapResultsListener;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

/**
 * Created by valternepomuceno on 29/11/2014.
 */
public class GoogleStaticMapApiTask extends AsyncTask<String, Integer, Bitmap> {

	private StaticMapResultsListener listener;
	private Sight sight;

	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(params[0]);

		InputStream in;
		try {
			in = httpClient.execute(request).getEntity().getContent();
			bitmap = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			e.printStackTrace();
			listener.onResultsFailed(e);
		}

		return bitmap;
	}

	@Override
	public void onPostExecute(Bitmap bitmap) {
		listener.onResultsFinished(bitmap, sight);
	}

	public void setOnResultsListener(StaticMapResultsListener listener, Sight sight) {
		this.listener = listener;
		this.sight = sight;
	}

}
