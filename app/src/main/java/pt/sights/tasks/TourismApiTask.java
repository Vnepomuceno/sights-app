package pt.sights.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.exceptions.ResourceNotAllowedException;
import citysdk.tourism.client.exceptions.ServerErrorException;
import citysdk.tourism.client.exceptions.UnknownErrorException;
import citysdk.tourism.client.exceptions.VersionNotAvailableException;
import citysdk.tourism.client.poi.lists.ListPointOfInterest;
import citysdk.tourism.client.poi.single.PointOfInterest;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.requests.TourismClient;
import citysdk.tourism.client.requests.TourismClientFactory;
import citysdk.tourism.client.terms.ParameterTerms;

import pt.sights.data.Sight;
import pt.sights.listeners.TourismResultsListener;

/**
 * Created by valternepomuceno on 28/11/2014.
 */
public class TourismApiTask extends AsyncTask<String, Integer, List<Sight>> {

	private TourismClient client;

	private TourismResultsListener listener;

	public void setOnResultsListener(TourismResultsListener listener) { this.listener = listener; }

	@Override
	protected List<Sight> doInBackground(String... params) {

		try {
			if (client == null) {
				String homeUrl = "http://tourism.citysdk.cm-lisboa.pt";
				client = TourismClientFactory.getInstance().getClient(homeUrl);
				client.useVersion("1.0");
			}

			ParameterList paramList = new ParameterList();
			paramList.add(new Parameter(ParameterTerms.CATEGORY, "Miradouros"));
			paramList.add(new Parameter(ParameterTerms.LIMIT, 50));

			ListPointOfInterest response = client.getPois(paramList);
			List<PointOfInterest> listPois = response.getPois();
			List<Sight> listSights = new ArrayList<>();

			if (listPois != null) {
				for (PointOfInterest poi : listPois) {
					Sight sight = new Sight(poi);
					listSights.add(sight);
				}
			}

			return listSights;

		}  catch (IOException | InvalidValueException | VersionNotAvailableException |
				ServerErrorException |UnknownErrorException | ResourceNotAllowedException |
				InvalidParameterException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void onPostExecute(List<Sight> listSights) {
		listener.onResultsFinished(listSights);
	}

}
