package pt.sights.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import pt.sights.R;
import pt.sights.adapter.SightCardAdapter;
import pt.sights.adapter.SightCard;
import pt.sights.data.DataManager;
import pt.sights.data.Sight;
import pt.sights.listeners.StaticMapResultsListener;
import pt.sights.listeners.TourismResultsListener;
import pt.sights.listeners.UrlImageListener;
import pt.sights.tasks.DownloadImageTask;
import pt.sights.tasks.GoogleStaticMapApiTask;
import pt.sights.tasks.TourismApiTask;
import pt.sights.utils.Graphics;
import pt.sights.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static pt.sights.data.DataManager.LiquidActivityType.EXPLORE;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;
import static pt.sights.data.DataManager.LiquidEventType.NO_INTERNET;

public class ExploreFragment extends Fragment implements TourismResultsListener,
		UrlImageListener, StaticMapResultsListener {

	private DataManager dataManager;

	private List<Sight> locations;

	private RecyclerView recyclerView;
	private ProgressBar spinner;
	private TextView loadingSightCards, loadingSightsProgress;
	private TextView noInternetText;
	private Button noInternetRetry;

	private boolean noInternet;
	private boolean finishedFetching = false;

	private int cardLoaded = 0;

	private int displayHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dataManager = (DataManager) getActivity().getApplication();

		displayHeight = dataManager.getScreenHeight(getActivity());

		if (dataManager.getSightLocations() != null)
			locations = dataManager.getSightLocations();

		dataManager.trackLiquidEvent(EXPLORE, ENTER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_explore, container, false);

		initUI(rootView);

		noInternetRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				noInternet = !activeInternetConnectivity();
				fetchSights();
				dataManager.trackLiquidEvent(EXPLORE, NO_INTERNET);
			}
		});

		noInternet = !activeInternetConnectivity();
		fetchSights();

		recyclerView.setHasFixedSize(false);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());

		if (dataManager.getSightCardAdapter() != null)
			recyclerView.setAdapter(dataManager.getSightCardAdapter());

		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		//int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_card_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * Instantiates and initializes UI elements
	 * @param rootView Root view element
	 */
	private void initUI(View rootView) {
		spinner = (ProgressBar) rootView.findViewById(R.id.spinner_sight_cards);
		loadingSightCards = (TextView) rootView.findViewById(R.id.loading_sight_cards);
		loadingSightsProgress = (TextView) rootView.findViewById(R.id.loading_sight_progress);
		noInternetText = (TextView) rootView.findViewById(R.id.no_internet_text);
		noInternetRetry = (Button) rootView.findViewById(R.id.no_internet_refresh);
		recyclerView = (RecyclerView) rootView.findViewById(R.id.locations_list_rv);
	}

	/**
	 * Builds list of cards for sights
	 * @return List of Sight Cards
	 */
	private List<SightCard> createSightCardList() {
		ArrayList<SightCard> list = new ArrayList<>();

		for (Sight sight : locations) {
			SightCard sightCard = new SightCard();
			sightCard.id = sight.id();
			sightCard.name = sight.getPoi().getLabel().get(0).getValue();
			sightCard.rate = sight.getRating();

			if (sight.getImages().size() > 0) {
				sightCard.image = Graphics.applyDarkGradient(sight.getImages().get(0), 90);
			} else if (sight.getStaticMapBitmap() != null) {
				sightCard.image = Graphics.applyDarkGradient(sight.getStaticMapBitmap(), 60);
			} else {
				Bitmap defaultCoverImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_sight_cover_image);
				sightCard.image = Graphics.applyDarkGradient(defaultCoverImage,  0);
			}

			list.add(sightCard);
		}

		int i = 0;
		for (SightCard c : list)
			dataManager.getSightLocations().get(i++).setRating(c.rate);

		Collections.sort(list);
		Collections.sort(dataManager.getSightLocations());

		return list;
	}

	/**
	 * Checks availability of internet connection
	 * @return True if internet connection is active, False otherwise
	 */
	private boolean activeInternetConnectivity() {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

		return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
	}

	/**
	 * Fetches sights from CitySDK API via asynchronous task
	 */
	private void fetchSights() {
		if (dataManager.getPointsOfInterest() == null && !finishedFetching) {
			TourismApiTask task = new TourismApiTask();
			task.setOnResultsListener(this);
			task.execute();
		}
		else {
			finishedFetching = true;
		}

		if (noInternet) {
			setVisibilityNoInternet(true);
			setVisibilityLoadingCards(false);
		} else {
			setVisibilityNoInternet(false);

			if (finishedFetching || cardLoaded == dataManager.getNumberOfSights() - 1)
				setVisibilityLoadingCards(false);
			else
				setVisibilityLoadingCards(true);
		}
	}

	/**
	 * Updates UI progress message for card loading
	 */
	public void updateProgressMessage() {
		loadingSightCards.setText(getResources().getString(R.string.building_cards));
		loadingSightsProgress.setText(String.format(getResources().getString(R.string.building_cards_progress),
				cardLoaded, dataManager.getNumberOfSights()));
	}

	/**
	 * Manages logic for showing or dismissing No Internet warning
	 * @param visible Show warning when true, dismiss otherwise
	 */
	public void setVisibilityNoInternet(boolean visible) {
		if (visible) {
			noInternetText.setVisibility(View.VISIBLE);
			noInternetRetry.setVisibility(View.VISIBLE);
		} else {
			noInternetText.setVisibility(View.GONE);
			noInternetRetry.setVisibility(View.GONE);
		}
	}

	/**
	 * Manages logic for showing or dismissing Loading Sights message
	 * @param visible Show message when true, dismiss otherwise
	 */
	public void setVisibilityLoadingCards(boolean visible) {
		if (visible) {
			spinner.setVisibility(View.VISIBLE);
			loadingSightCards.setVisibility(View.VISIBLE);
			loadingSightsProgress.setVisibility(View.VISIBLE);
			updateProgressMessage();
		} else {
			spinner.setVisibility(View.GONE);
			loadingSightCards.setVisibility(View.GONE);
			loadingSightsProgress.setVisibility(View.GONE);
		}
	}

	/**
	 * Callback from CitySDK API asynchronous task
	 * @param locations Returned list of locations
	 */
	@Override
	public void onResultsFinished(List<Sight> locations) {
		this.locations = locations;
		loadingSightCards.setText(R.string.building_cards);

		dataManager.setSightLocations(locations);
		updateProgressMessage();

		if (locations != null) {
			for (Sight sight : locations) {
				boolean hasCover = false, hasMap = false;

				// Executes DOWNLOAD IMAGE task
				for (int i = 0; i < sight.getPoi().getLink().size(); i++) {
					if (sight.getPoi().getLink().get(i).getType().startsWith("image")) {
						hasCover = true;
						DownloadImageTask task = new DownloadImageTask();
						task.setOnResultsListener(this, sight);
						task.execute(sight.getPoi().getLink().get(i).getHref());
						break;
					}
				}

				// Executes GOOGLE STATIC MAP task
				if (!hasCover) {
					if (sight.getStaticMapBitmap() == null) {
						int width = dataManager.getScreenWidth(getActivity());
						String[] coordinates = sight.getCoordinates();

						if (coordinates != null) {
							hasMap = true;
							String url = Utils.getStaticMapUrl(width / 2, 620 / 2, coordinates);

							GoogleStaticMapApiTask staticMapTask = new GoogleStaticMapApiTask();
							staticMapTask.setOnResultsListener(this, sight);
							staticMapTask.execute(url);
						}
					}
				}

				// Increments card count
				if (!hasCover && !hasMap) cardLoaded++;
			}

			finishedFetching = false;

			if (dataManager.getSightCardAdapter() != null)
				recyclerView.setAdapter(dataManager.getSightCardAdapter());
		}
	}

	/**
	 * Callback from Download Image asynchronous task
	 * @param image Array of images
	 * @param sight Sight whose images are being fetched
	 */
	@Override
	public void onResultsFinished(Bitmap[] image, Sight sight) {
		sight.addImage(image[0]);

		try {
			if (cardLoaded + 1 == dataManager.getNumberOfSights()) {
				dataManager.setSightCardAdapter(new SightCardAdapter(createSightCardList(),
						getActivity().getApplicationContext(), recyclerView, displayHeight));

				if (dataManager.getSightCardAdapter() != null)
					recyclerView.setAdapter(dataManager.getSightCardAdapter());

				finishedFetching = true;
				setVisibilityLoadingCards(false);
			}
			else updateProgressMessage();

			if (finishedFetching && spinner != null && loadingSightCards != null) {
				spinner.setVisibility(View.GONE);
				loadingSightCards.setVisibility(View.GONE);
				loadingSightsProgress.setVisibility(View.GONE);
			}

			cardLoaded++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Callback from Google Static Maps API asynchronous task
	 * @param bitmap Static map
	 * @param sight Sight whose static map is being fetched
	 */
	@Override
	public void onResultsFinished(Bitmap bitmap, Sight sight) {
		sight.setStaticMapBitmap(bitmap);
		cardLoaded++;
		updateProgressMessage();
	}

	/**
	 * Handles display of exceptions to users occurred while performing network tasks
	 * @param e Exception thrown by task
	 */
	@Override
	public void onResultsFailed(final Exception e) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
				}
			});
		}

		cardLoaded++;
		updateProgressMessage();
	}

}
