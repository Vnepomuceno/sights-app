package pt.sights.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseUser;
import pt.sights.R;
import pt.sights.adapter.SightProfileAdapter;
import pt.sights.data.DataManager;

import static pt.sights.data.DataManager.LiquidActivityType.PROFILE;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	15th of November of 2014
 */
public class ProfileFragment extends Fragment {

	private DataManager dataManager;

	private RecyclerView favouritesRv, ratedRv;
	private ProgressBar favouritesPb, ratedPb;

	private int displayHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dataManager = (DataManager) getActivity().getApplication();

		displayHeight = dataManager.getScreenHeight(getActivity());

		dataManager.retrieveFavouriteSights(this);
		dataManager.retrieveRatedSights(this);

		dataManager.trackLiquidEvent(PROFILE, ENTER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

		Button logoutButton = (Button) rootView.findViewById(R.id.logout_button);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.signOut(ProfileFragment.this.getActivity().getApplicationContext());
			}
		});

		ImageView coverImageIv = (ImageView) rootView.findViewById(R.id.profile_header);
		TextView usernameTv = (TextView) rootView.findViewById(R.id.profile_username_tv);
		TextView emailTv = (TextView) rootView.findViewById(R.id.profile_email_tv);
		favouritesRv = (RecyclerView) rootView.findViewById(R.id.favourite_sights_rv);
		ratedRv = (RecyclerView) rootView.findViewById(R.id.rated_sights_rv);
		favouritesPb = (ProgressBar) rootView.findViewById(R.id.spinner_profile_favourites);
		ratedPb = (ProgressBar) rootView.findViewById(R.id.spinner_profile_rated);

		favouritesRv.setHasFixedSize(false);
		favouritesRv.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
		favouritesRv.setItemAnimator(new DefaultItemAnimator());
		ratedRv.setHasFixedSize(false);
		ratedRv.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
		ratedRv.setItemAnimator(new DefaultItemAnimator());

		coverImageIv.setMinimumHeight((int) (displayHeight * 0.4));
		coverImageIv.setScaleType(ImageView.ScaleType.CENTER_CROP);

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			usernameTv.setText("@" + currentUser.getUsername());
			emailTv.setText(currentUser.getEmail());
		}

		updateFavouriteSightsSection(false);
		updateRatedSightsSection(false);

		return rootView;
	}

	public void updateFavouriteSightsSection(boolean finishedRetrieving) {
		if (finishedRetrieving || dataManager.getProfileFavouriteSights().size() > 0)
			favouritesPb.setVisibility(View.GONE);
		else
			favouritesPb.setVisibility(View.VISIBLE);

		SightProfileAdapter sightProfileAdapter = new SightProfileAdapter(
				dataManager.getProfileFavouriteSights(),
				getActivity().getApplicationContext(),
				favouritesRv,
				1);
		dataManager.setSightProfileFavouritesAdapter(sightProfileAdapter);
		favouritesRv.setAdapter(sightProfileAdapter);

		if (dataManager.getFavouriteSights() != null && favouritesRv != null && sightProfileAdapter != null) {
			int numFavs = dataManager.getFavouriteSights().size();
			favouritesRv.getLayoutParams().height = (int) getResources().getDimension(R.dimen.profile_sight_rv_height) * numFavs;
		}
	}

	public void updateRatedSightsSection(boolean finishedRetrieving) {
		if (finishedRetrieving || dataManager.getProfileRatedSights().size() > 0)
			ratedPb.setVisibility(View.GONE);
		else
			ratedPb.setVisibility(View.VISIBLE);

		SightProfileAdapter sightProfileAdapter = new SightProfileAdapter(
				dataManager.getProfileRatedSights(),
				getActivity().getApplicationContext(),
				ratedRv,
				2);
		dataManager.setSightProfileRatedAdapter(sightProfileAdapter);
		ratedRv.setAdapter(sightProfileAdapter);

		if (dataManager.getRatedSights() != null && ratedRv != null && sightProfileAdapter != null) {
			int numRated = dataManager.getRatedSights().size();
			ratedRv.getLayoutParams().height = (int) getResources().getDimension(R.dimen.profile_sight_rv_height) * numRated;
		}
	}

}
