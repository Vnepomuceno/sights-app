package pt.sights.tasks;

import android.os.AsyncTask;

import pt.sights.listeners.GoogleDistanceResultsListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by valternepomuceno on 18/03/15.
 */
public class GoogleDistanceTask extends AsyncTask<String, Integer, String> {

	private GoogleDistanceResultsListener listener;

	@Override
	protected String doInBackground(String... params) {
		StringBuilder stringBuilder = new StringBuilder();
		String dist = "";

		try {
			String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
					+ params[0] + "&destination=" + params[1] + "&mode=walking&transit_routing_preference=less_walking";

			HttpPost httpPost = new HttpPost(url);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}

		} catch (IOException ioe) {
			listener.onResultsFailed(ioe);
		}

		JSONObject jsonObject;

		try {
			jsonObject = new JSONObject(stringBuilder.toString());

			JSONArray array = jsonObject.getJSONArray("routes");
			JSONObject routes = array.getJSONObject(0);
			JSONArray legs = routes.getJSONArray("legs");
			JSONObject steps = legs.getJSONObject(0);
			JSONObject distance = steps.getJSONObject("distance");
			dist = distance.getString("text");
		}
		catch (Exception e) {
			listener.onResultsFailed(e);
		}

		return dist;
	}

	@Override
	public void onPostExecute(String distance) { listener.onResultsFinished(distance); }

	public void setOnResultsListener(GoogleDistanceResultsListener listener) {
		this.listener = listener;
	}
}
