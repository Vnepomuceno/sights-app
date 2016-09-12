package pt.sights.activities;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;
import pt.sights.R;
import pt.sights.adapter.ContactAdapter;
import pt.sights.data.ContactItemData;
import pt.sights.data.DataManager;
import pt.sights.data.Sight;
import pt.sights.listeners.GoogleDistanceResultsListener;
import pt.sights.listeners.StaticMapResultsListener;
import pt.sights.listeners.UrlImageListener;
import pt.sights.tasks.DownloadImageTask;
import pt.sights.tasks.GoogleDistanceTask;
import pt.sights.tasks.GoogleStaticMapApiTask;
import pt.sights.utils.Format;
import pt.sights.utils.ScrollViewX;
import pt.sights.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static pt.sights.data.DataManager.LiquidActivityType.RATE_DIALOG;
import static pt.sights.data.DataManager.LiquidActivityType.CHECK_IN_DIALOG;
import static pt.sights.data.DataManager.LiquidActivityType.SIGHT_DETAIL;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;
import static pt.sights.data.DataManager.LiquidEventType.FAVOURITE;
import static pt.sights.data.DataManager.LiquidEventType.SHOW_LESS_DESCRIPTION;
import static pt.sights.data.DataManager.LiquidEventType.SHOW_MORE_DESCRIPTION;

public class SightDetailActivity extends AppCompatActivity
		implements UrlImageListener,StaticMapResultsListener, GoogleDistanceResultsListener {

	private DataManager dataManager;
	private Sight sight;

	private String descriptionStr;
	private int numberOfPhotos, displayHeight;
	private float photoGalleryHeightPercent = 0.19f, staticMapHeightPercent = 0.35f;
	private boolean finishedFetchingStaticMap = false, descriptionExpanded = false;

	private LinearLayout photoGallery;
	private RelativeLayout loadingStaticMapLayout, loadingPhotosLayout, goToMapLayout;
	private Button readMoreLessBtn;
	private TextView sightNameText, addressLink, descriptionText, ratingTv, numReviewsTv,
			numFavouritesTv, distanceTv, titlePhotos;
	private ImageView coverImageIv, ratingStar1, ratingStar2, ratingStar3, ratingStar4, ratingStar5,
			favouriteIconIv, checkinIconIv, rateIconIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sight_detail);

		dataManager = (DataManager) getApplication();
		sight = dataManager.getSightUnderInspection();

		dataManager.getSightRatingParse(this);
		initUI();

		getDistanceToSight();
		downloadSightCoverImages();

		populateCoverImage();
		populateSightInfoPanel();
		populatePhotoGallery();
		populateStaticMap();
		populateCheckInIcon();
		populateFavouriteIcon();
		populateRatingIcon();
		populateFiveStarRating(sight.getRating());
		populateNumberOfFavourites();
		populateRating();

		dataManager.trackLiquidEvent(SIGHT_DETAIL, ENTER);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Callback method when fetching photo gallery of a sight
	 * @param images Array of bitmap images of the sight
	 * @param s Sight of the fetched photo gallery
	 */
	@Override
	public void onResultsFinished(Bitmap[] images, Sight s) {
		for (Bitmap bm : images)
			this.sight.addImage(/*Graphics.applyDarkGradient(*/bm/*, getApplicationContext())*/);

		if (sight.getImages().size() == numberOfPhotos)
			populatePhotoGallery();
	}

	/**
	 * Callback method when fetching static map of a sight
	 * @param bitmap Bitmap image of the static map
	 * @param sight Sight of the fetched static map
	 */
	@Override
	public void onResultsFinished(Bitmap bitmap, Sight sight) {
		this.sight.setStaticMapBitmap(bitmap);
		this.finishedFetchingStaticMap = true;

		if (loadingStaticMapLayout != null) {
			loadingStaticMapLayout.setVisibility(View.GONE);
		}

		populateStaticMap();
	}

	/**
	 * Callback method when fetching distance of a sight
	 * @param distance Google distance to a sight
	 */
	@Override
	public void onResultsFinished(String distance) {
		this.sight.setDistance(distance);
		distanceTv.setText(distance);
	}

	/**
	 * Callback method when result to AsyncTask is unsuccessful
	 * @param e Exception thrown by AsyncTask
	 */
	@Override
	public void onResultsFailed(final Exception e) {
		SightDetailActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(SightDetailActivity.this, e.toString(), Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * Initializes and loads UI elements, and adds click listeners to buttons of the activity
	 */
	private void initUI() {
		setTitle("");

		displayHeight = dataManager.getScreenHeight(this);

		populateContactInfoList();

		Button checkinSightBtn = (Button) findViewById(R.id.check_in_button);
		Button favoriteSightBtn = (Button) findViewById(R.id.favorite_sight_button);
		Button rateSightBtn = (Button) findViewById(R.id.rate_sight_button);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		loadingStaticMapLayout = (RelativeLayout) findViewById(R.id.static_map_spinner_layout);
		loadingPhotosLayout = (RelativeLayout) findViewById(R.id.photos_spinner_layout);
		goToMapLayout = (RelativeLayout) findViewById(R.id.go_to_map_layout);
		sightNameText = (TextView) findViewById(R.id.text_detail_sight_name);
		coverImageIv = (ImageView) findViewById(R.id.sight_detail_cover_iv);
		ImageView goToMapIv = (ImageView) findViewById(R.id.go_to_map_iv);
		distanceTv = (TextView) findViewById(R.id.proximity_tv);
		titlePhotos = (TextView) findViewById(R.id.title_photos);
		ratingStar1 = (ImageView) findViewById(R.id.rating_star_1);
		ratingStar2 = (ImageView) findViewById(R.id.rating_star_2);
		ratingStar3 = (ImageView) findViewById(R.id.rating_star_3);
		ratingStar4 = (ImageView) findViewById(R.id.rating_star_4);
		ratingStar5 = (ImageView) findViewById(R.id.rating_star_5);
		ratingTv = (TextView) findViewById(R.id.sight_detail_rating);
		numReviewsTv = (TextView) findViewById(R.id.sight_detail_num_reviews);
		favouriteIconIv = (ImageView) findViewById(R.id.favourite_icon_iv);
		checkinIconIv = (ImageView) findViewById(R.id.checkin_icon_iv);
		rateIconIv = (ImageView) findViewById(R.id.rate_icon_iv);
		numFavouritesTv = (TextView) findViewById(R.id.num_favourites_tv);
		photoGallery = (LinearLayout) findViewById(R.id.sight_photo_gallery);
		descriptionText = (TextView) findViewById(R.id.sight_detail_description);
		readMoreLessBtn = (Button) findViewById(R.id.sight_detail_read_more);
		addressLink = (TextView) findViewById(R.id.sight_detail_address);
		descriptionText = (TextView) findViewById(R.id.sight_detail_description);

		final ColorDrawable cd = new ColorDrawable(Color.rgb(0, 150, 136));
		toolbar.setBackgroundDrawable(cd);
		cd.setAlpha(0);

		final ScrollViewX scrollView = (ScrollViewX) findViewById(R.id.scroll_view);
		scrollView.setOnScrollViewListener(new ScrollViewX.OnScrollViewListener() {
			@Override
			public void onScrollChanged(ScrollViewX v, int l, int t, int oldl, int oldt) {
				cd.setAlpha(getAlphaforActionBar(v.getScrollY()));
			}

			private int getAlphaforActionBar(int scrollY) {
				int minDist = 0, maxDist = 600;

				if (scrollY > maxDist / 2)
					setTitle(sight.getPoi().getLabel().get(0).getValue());
				else
					setTitle("");

				if (scrollY > maxDist) {
					return 255;
				} else {
					if (scrollY < minDist) {
						return 0;
					} else {
						return (int) ((255.0 / maxDist) * scrollY);
					}
				}
			}
		});

		setSupportActionBar(toolbar);

		try {
			if (getSupportActionBar() != null) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		final Context context = this;

		if (sight.getCoordinates() != null) {
			goToMapIv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Sight sight = dataManager.getSightUnderInspection();
					String[] coord = sight.getCoordinates();
					String uri = String.format(Locale.ENGLISH,
							"http://maps.google.com/maps?daddr=%f,%f(%s)",
							Float.parseFloat(coord[0]),
							Float.parseFloat(coord[1]),
							sight.name);

					Intent mapIntent = new Intent(Intent.ACTION_VIEW);
					mapIntent.setData(Uri.parse(uri));
					mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(mapIntent);
				}
			});
		} else {
			goToMapLayout.setVisibility(View.GONE);
			goToMapIv.setVisibility(View.GONE);
		}

		readMoreLessBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreLessDescription(true);
				if (descriptionExpanded)
					dataManager.trackLiquidEvent(SIGHT_DETAIL, SHOW_MORE_DESCRIPTION);
				else
					dataManager.trackLiquidEvent(SIGHT_DETAIL, SHOW_LESS_DESCRIPTION);
			}
		});
		favoriteSightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.favoriteSight(SightDetailActivity.this);
				dataManager.getSightUnderInspection().swapFavourited();
				populateFavouriteIcon();
				dataManager.trackLiquidEvent(SIGHT_DETAIL, FAVOURITE);
			}
		});
		checkinSightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dataManager.getSightUnderInspection().getCheckin() == null)
					populateCheckInDialog();
				//else
			}
		});
		rateSightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				populateRateDialog();
			}
		});

		// TODO: Get a way to truncate to 1 line the textViews of the recyclerView
		//itemContactTv.setMaxLines(1);
		//itemContactTv.setEllipsize(TextUtils.TruncateAt.END);

		numberOfPhotos = sight.getNumberOfImages();
		descriptionStr = sight.getPoi().getDescription().size()  > 0 ?
				sight.getPoi().getDescription().get(0).getValue() : "";

		if (sight.getPoi().getDescription().size() > 0)
			showMoreLessDescription(false);
		else {
			descriptionText.setVisibility(View.GONE);
			findViewById(R.id.title_description).setVisibility(View.GONE);
			findViewById(R.id.sight_detail_read_more).setVisibility(View.GONE);
		}
	}

	/**
	 * Populates Check-In Dialog UI
	 */
	private void populateCheckInDialog() {
		dataManager.trackLiquidEvent(CHECK_IN_DIALOG, ENTER);

		final Dialog checkInDialog = new Dialog(SightDetailActivity.this);
		checkInDialog.setContentView(R.layout.dialog_check_in_sight);

		checkInDialog.setTitle(R.string.check_in_dialog_title);

		final TextView sightNameTv = (TextView)checkInDialog.findViewById(R.id.check_in_sight_tv);
		final Button checkinBtn = (Button)checkInDialog.findViewById(R.id.dialog_button_check_in);
		final EditText checkinCommentEt =
				(EditText)checkInDialog.findViewById(R.id.dialog_check_in_comment);

		sightNameTv.setText(dataManager.getSightUnderInspection().name);

		checkinBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.checkInSight(checkinCommentEt.getText().toString());

				populateCheckInIcon();

				checkInDialog.dismiss();
			}
		});

		checkInDialog.show();
	}

	/**
	 * Populates Rate Dialog UI
	 */
	private void populateRateDialog() {
		dataManager.trackLiquidEvent(RATE_DIALOG, ENTER);

		final Dialog rateDialog = new Dialog(SightDetailActivity.this);
		rateDialog.setContentView(R.layout.dialog_rate_sight);

		final TextView yourRatingTv = (TextView)rateDialog.findViewById(R.id.your_rating_tv);
		final Button star1Btn = (Button)rateDialog.findViewById(R.id.star_1_button);
		final Button star2Btn = (Button)rateDialog.findViewById(R.id.star_2_button);
		final Button star3Btn = (Button)rateDialog.findViewById(R.id.star_3_button);
		final Button star4Btn = (Button)rateDialog.findViewById(R.id.star_4_button);
		final Button star5Btn = (Button)rateDialog.findViewById(R.id.star_5_button);
		final float[] rating = new float[1];

		final Sight sight = dataManager.getSightUnderInspection();
		if (sight.isRated()) {
			rateDialog.setTitle(R.string.rating_dialog_title_rated);

			ParseObject ratingObj = sight.getRatingParseObj();
			populateRatingBar(String.valueOf(ratingObj.getInt("rating")), yourRatingTv, star1Btn,
					star2Btn, star3Btn, star4Btn, star5Btn, rating);
		} else {
			rateDialog.setTitle(R.string.rating_dialog_title);
		}

		star1Btn.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onClick(View v) {
				if (!sight.isRated())
					populateRatingBar("1", yourRatingTv, star1Btn, star2Btn, star3Btn, star4Btn,
							star5Btn, rating);
			}
		});
		star2Btn.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onClick(View v) {
				if (!sight.isRated())
					populateRatingBar("2", yourRatingTv, star1Btn, star2Btn, star3Btn, star4Btn,
							star5Btn, rating);
			}
		});
		star3Btn.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onClick(View v) {
				if (!sight.isRated())
					populateRatingBar("3", yourRatingTv, star1Btn, star2Btn, star3Btn, star4Btn,
							star5Btn, rating);
			}
		});
		star4Btn.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onClick(View v) {
				if (!sight.isRated())
					populateRatingBar("4", yourRatingTv, star1Btn, star2Btn, star3Btn, star4Btn,
							star5Btn, rating);
			}
		});
		star5Btn.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onClick(View v) {
				if (!sight.isRated())
					populateRatingBar("5", yourRatingTv, star1Btn, star2Btn, star3Btn, star4Btn,
							star5Btn, rating);
			}
		});

		Button clearButton = (Button)rateDialog.findViewById(R.id.dialog_button_clear);
		Button submitButton = (Button)rateDialog.findViewById(R.id.dialog_button_submit);
		Button closeButton = (Button)rateDialog.findViewById(R.id.dialog_button_close);

		if (!sight.isRated()) {
			clearButton.setVisibility(View.VISIBLE);
			submitButton.setVisibility(View.VISIBLE);
			closeButton.setVisibility(View.GONE);
			clearButton.setOnClickListener(new View.OnClickListener() {
				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				@Override
				public void onClick(View v) {
					populateRatingBar("0", yourRatingTv, star1Btn, star2Btn, star3Btn, star4Btn,
							star5Btn, rating);
				}
			});
			submitButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (rating[0] != 0) {
						dataManager.rateSight(rating[0]);
						dataManager.getSightUnderInspection().updateRated(true);
						DataManager.userPlacedRating = rating[0];

						populateRatingIcon();
						rateDialog.dismiss();
					}
				}
			});
		}
		else {
			clearButton.setVisibility(View.GONE);
			submitButton.setVisibility(View.GONE);
			closeButton.setVisibility(View.VISIBLE);
			closeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					rateDialog.dismiss();
				}
			});
		}

		rateDialog.show();
	}

	/**
	 * Populates Rating Bar UI
	 * @param ratingStr Rating caption
	 * @param yourRating TextView for your rating
	 * @param star1 Button for star 1 of the rating system
	 * @param star2 Button for star 2 of the rating system
	 * @param star3 Button for star 3 of the rating system
	 * @param star4 Button for star 4 of the rating system
	 * @param star5 Button for star 5 of the rating system
	 * @param rating Sight rating (from 0.0 to 5.0)
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void populateRatingBar(String ratingStr, TextView yourRating, Button star1, Button star2,
	                               Button star3, Button star4, Button star5, float[] rating) {
		switch (ratingStr) {
			case "0":
				rating[0] = 0;
				yourRating.setText("-.-");
				star1.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star2.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star3.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star4.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star5.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				break;
			case "1":
				rating[0] = 1f;
				yourRating.setText("1.0");
				star1.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_100)));
				star2.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star3.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star4.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star5.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				break;
			case "2":
				rating[0] = 2f;
				yourRating.setText("2.0");
				star1.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_100)));
				star2.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
				star3.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star4.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star5.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				break;
			case "3":
				rating[0] = 3f;
				yourRating.setText("3.0");
				star1.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_100)));
				star2.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
				star3.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_300)));
				star4.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				star5.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				break;
			case "4":
				rating[0] = 4f;
				yourRating.setText("4.0");
				star1.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_100)));
				star2.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
				star3.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_300)));
				star4.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_400)));
				star5.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
				break;
			case "5":
				rating[0] = 5f;
				yourRating.setText("5.0");
				star1.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_100)));
				star2.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
				star3.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_300)));
				star4.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_400)));
				star5.setBackgroundTintList(
						ColorStateList.valueOf(getResources().getColor(R.color.teal_500)));
				break;
		}
	}

	/**
	 * Fetches and displays contact information for a sight
	 */
	private void populateContactInfoList() {
		List<String[]> contactInfoLst = new ArrayList<>();

		if (!sight.getPhone().equals("")) {
			String[] contactInfo = new String[2];
			contactInfo[0] = "PHONE";
			contactInfo[1] = sight.getPhone();
			contactInfoLst.add(contactInfo);
		}
		if (!sight.getEmail().equals("")) {
			String[] contactInfo = new String[2];
			contactInfo[0] = "EMAIL";
			contactInfo[1] = sight.getEmail();
			contactInfoLst.add(contactInfo);
		}
		if (!sight.getWebsite().equals("")) {
			String[] contactInfo = new String[2];
			contactInfo[0] = "WEBSITE";
			contactInfo[1] = sight.getWebsite();
			contactInfoLst.add(contactInfo);
		}
		if (!sight.getFacebook().equals("")) {
			String[] contactInfo = new String[2];
			contactInfo[0] = "FACEBOOK";
			contactInfo[1] = sight.getFacebook();
			contactInfoLst.add(contactInfo);
		}

		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.sight_detail_contact_info_rv);
		ContactItemData[] itemsData = new ContactItemData[contactInfoLst.size()];

		for (int i = 0; i < contactInfoLst.size(); i++) {
			switch (contactInfoLst.get(i)[0]) {

				case "PHONE":
					itemsData[i] = new ContactItemData(ContactItemData.Type.PHONE,
							contactInfoLst.get(i)[1]);
					itemsData[i].setImageUrl(R.drawable.contact_action_call);
					break;

				case "EMAIL":
					itemsData[i] = new ContactItemData(ContactItemData.Type.EMAIL,
							contactInfoLst.get(i)[1]);
					itemsData[i].setImageUrl(R.drawable.contact_action_email);
					break;

				case "WEBSITE":
					itemsData[i] = new ContactItemData(ContactItemData.Type.WEBSITE,
							contactInfoLst.get(i)[1].length() > 40
									? contactInfoLst.get(i)[1].substring(0, 40) + " ..."
									: contactInfoLst.get(i)[1]);
					itemsData[i].setImageUrl(R.drawable.contact_action_website);
					break;

				case "FACEBOOK":
					itemsData[i] = new ContactItemData(ContactItemData.Type.FACEBOOK,
							contactInfoLst.get(i)[1]);
					itemsData[i].setImageUrl(R.drawable.contact_action_facebook);
					break;

			}
		}

		ContactAdapter cAdapter = new ContactAdapter(this, itemsData, recyclerView);

		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(cAdapter);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setMinimumHeight(90 * itemsData.length);
	}

	/**
	 * Populates  5-Star Sight Rating UI
	 * @param rating Sight rating (from 0.0 to 5.0)
	 */
	private void populateFiveStarRating(float rating) {
		ratingTv.setText(String.valueOf(Format.precisionFloat(1, rating)));
		float roundedRating = 0.5f * Math.round(rating / 0.5f);

		if (roundedRating == 0.0) {
			ratingStar1.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		}
		else if (roundedRating == 0.5) {
			ratingStar1.setBackgroundResource(R.drawable.rating_half_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 1.0) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 1.5) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_half_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 2.0) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 2.5) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_half_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 3.0) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_no_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 3.5) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_half_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 4.0) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_no_star);
		} else if (roundedRating == 4.5) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_half_star);
		} else if (roundedRating == 5.0) {
			ratingStar1.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar2.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar3.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar4.setBackgroundResource(R.drawable.rating_full_star);
			ratingStar5.setBackgroundResource(R.drawable.rating_full_star);
		}
	}

	/**
	 * Populates Sight Number of Favourites UI
	 */
	private void populateNumberOfFavourites() {
		Sight sight = dataManager.getSightUnderInspection();

		dataManager.getParseFavourite(this);

		if (sight.getNumFavourites() != -1)
			updateNumFavourites();

		dataManager.countFavourites(this, sight.id());
	}

	private void populateRating() {
		dataManager.getUserSightRatingParse(this);
	}

	/**
	 * Fetches the distance (km) between current sight and user location using Google Distance API
	 */
	private void getDistanceToSight() {
		Sight sight = dataManager.getSightUnderInspection();

		if (sight.getDistance() != null) {
			distanceTv.setText(sight.getDistance());
		}

		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = service.getBestProvider(criteria, false);
		Location location = service.getLastKnownLocation(provider);

		if (location != null) {
			LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
			String[] sightLocationArr = sight.getCoordinates();

			if (sightLocationArr != null) {
				LatLng sightLocation = new LatLng(Double.parseDouble(sightLocationArr[0]),
						Double.parseDouble(sightLocationArr[1]));

				String userLocationStr = userLocation.latitude + "," + userLocation.longitude;
				String sightLocationStr = sightLocation.latitude + "," + sightLocation.longitude;

				GoogleDistanceTask distanceTask = new GoogleDistanceTask();
				distanceTask.setOnResultsListener(this);
				distanceTask.execute(userLocationStr, sightLocationStr);
			}
		}
	}

	/**
	 * Populates Cover Image UI
	 */
	private void populateCoverImage() {
		if (sight.getImages().size() > 0) {
			coverImageIv.setImageBitmap(dataManager.getSightUnderInspection().getImages().get(0));
		} else if (sight.getStaticMapBitmap() != null) {
			final Context context = this;
			ArrayList<Bitmap> bitmapArr = new ArrayList<>();
			bitmapArr.add(dataManager.getSightUnderInspection().getStaticMapBitmap());
			coverImageIv.setImageBitmap(dataManager.getSightUnderInspection().getStaticMapBitmap());

			coverImageIv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Sight sight = dataManager.getSightUnderInspection();
					String[] coord = sight.getCoordinates();
					String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f(%s)",
							Float.parseFloat(coord[0]),
							Float.parseFloat(coord[1]),
							sight.name);

					Intent mapIntent = new Intent(Intent.ACTION_VIEW);
					mapIntent.setData(Uri.parse(uri));
					mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(mapIntent);
				}
			});
		} else {
			Bitmap defaultCoverImage = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_sight_cover_image);
			coverImageIv.setImageBitmap(defaultCoverImage);
		}

		coverImageIv.getLayoutParams().height = (int) (displayHeight * 0.5);
		coverImageIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		coverImageIv.requestLayout();
	}

	/**
	 * Populates Photo Gallery UI
	 */
	private void populatePhotoGallery() {
		if (sight.getImages().size() == numberOfPhotos) {
			int photoSize = (int)(dataManager.getScreenHeight(this) * photoGalleryHeightPercent);
			ArrayList<Bitmap> photos = (ArrayList<Bitmap>)
					dataManager.getSightUnderInspection().getImages();

			final int sightPos = dataManager.getSightPositionByName(
					dataManager.getSightUnderInspection().name);

			if (numberOfPhotos > 0) {
				for (int i = 0; i < photos.size(); i++) {
					Bitmap bm = photos.get(i);
					LinearLayout layout = new LinearLayout(getApplicationContext());
					layout.setLayoutParams(new ViewGroup.LayoutParams(photoSize, photoSize));
					layout.setGravity(Gravity.CENTER);

					ImageView imageView = new ImageView(getApplicationContext());
					imageView.setLayoutParams(new ViewGroup.LayoutParams(photoSize - 10,
							photoSize - 10));
					imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					imageView.setImageBitmap(bm);
					final int imgNum = i;
					imageView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent galleryIntent = new Intent(SightDetailActivity.this,
									FullScreenGalleryActivity.class);
							galleryIntent.putExtra("imgId", imgNum);
							galleryIntent.putExtra("sightId", sightPos);
							startActivity(galleryIntent);
						}
					});

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
						imageView.setElevation(5f);

					layout.addView(imageView);

					photoGallery.addView(layout);

					if (loadingPhotosLayout != null)
						loadingPhotosLayout.setVisibility(View.GONE);
				}
			} else {
				titlePhotos.setVisibility(View.GONE);
				photoGallery.setVisibility(View.GONE);
				loadingPhotosLayout.setVisibility(View.GONE);
			}
		} else {
			if (loadingPhotosLayout != null && sight.getImages().size() != 0) {
				loadingPhotosLayout.setVisibility(View.VISIBLE);
				loadingPhotosLayout.setMinimumHeight((int)(dataManager.getScreenHeight(this)
						* photoGalleryHeightPercent));
			}
		}
	}

	/**
	 * Populates Sight Info Panel UI
	 */
	private void populateSightInfoPanel() {
		sightNameText.setText(sight.name);

		if (!sight.getAddress().equals("")) {
			String addressStr = sight.getAddress();
			addressLink.setText(addressStr);
		} else {
			addressLink.setVisibility(View.GONE);
		}
	}

	/**
	 * Populates Sight Static Map UI
	 */
	private void populateStaticMap() {
		if (sight.getNumberOfImages() == 0) {
			TextView mapTitle = (TextView) findViewById(R.id.title_map);
			RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.static_map_layout);
			mapTitle.setVisibility(View.GONE);
			mapLayout.setVisibility(View.GONE);
		} else {
			final Context context = this;
			ImageView staticMap = (ImageView) findViewById(R.id.static_map_detail);
			staticMap.setImageBitmap(dataManager.getSightUnderInspection().getStaticMapBitmap());
			staticMap.setScaleType(ImageView.ScaleType.CENTER_CROP);
			staticMap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Sight sight = dataManager.getSightUnderInspection();
					String[] coord = sight.getCoordinates();
					String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f(%s)",
							Float.parseFloat(coord[0]),
							Float.parseFloat(coord[1]),
							sight.name);

					Intent mapIntent = new Intent(Intent.ACTION_VIEW);
					mapIntent.setData(Uri.parse(uri));
					mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(mapIntent);
				}
			});
		}
	}

	/**
	 * Initiates multiple download tasks
	 */
	private void downloadSightCoverImages() {
		if (sight.getPoi().getLink().size() > 0 && sight.getImages().size() == 1) {
			boolean firstImage = true;
			for (int i = 0; i < sight.getPoi().getLink().size(); i++) {
				if (sight.getPoi().getLink().get(i).getType().startsWith("image")) {
					if (firstImage) {
						firstImage = false;
					} else {
						DownloadImageTask task = new DownloadImageTask();
						task.setOnResultsListener(this, sight);
						task.execute(sight.getPoi().getLink().get(i).getHref());
					}
				}
			}
		}

		if (sight.getStaticMapBitmap() == null && sight.getCoordinates() != null) {
			int screenWidth = dataManager.getScreenWidth(this);
			int sightMapHeight = (int)(dataManager.getScreenHeight(this) * staticMapHeightPercent);
			String url = Utils.getStaticMapUrl(screenWidth / 2, sightMapHeight / 2,
					sight.getCoordinates());

			GoogleStaticMapApiTask staticMapTask = new GoogleStaticMapApiTask();
			staticMapTask.setOnResultsListener(this, sight);
			staticMapTask.execute(url);
		}
		else {
			finishedFetchingStaticMap = true;
		}

		if (finishedFetchingStaticMap && loadingStaticMapLayout != null) {
			loadingStaticMapLayout.setVisibility(View.GONE);
		} else if (loadingStaticMapLayout != null) {
			loadingStaticMapLayout.setVisibility(View.VISIBLE);
			loadingStaticMapLayout.setMinimumHeight((int)(dataManager.getScreenHeight(this)
					* staticMapHeightPercent));
		}
	}

	/**
	 * Manages logic of Show More / Less Description UI
	 * @param onClick
	 */
	private void showMoreLessDescription(boolean onClick) {
		if (onClick) descriptionExpanded = !descriptionExpanded;
		descriptionStr = sight.getPoi().getDescription().get(0).getValue();
		descriptionText.setText(descriptionStr);

		if (!descriptionExpanded) {
			descriptionText.setMaxLines(4);
			descriptionText.setEllipsize(TextUtils.TruncateAt.END);
			readMoreLessBtn.setText(R.string.read_more);
		} else {
			descriptionText.setMaxLines(Integer.MAX_VALUE);
			descriptionText.setEllipsize(null);
			readMoreLessBtn.setText(R.string.read_less);
		}
	}

	/**
	 * Updates Number of Favourites UI
	 */
	public void updateNumFavourites() {
		numFavouritesTv.setText(
				String.valueOf(dataManager.getSightUnderInspection().getNumFavourites()));
	}

	/**
	 * Updates Rating Data UI
	 */
	public void updateRatingData() {
		int numReviews = dataManager.getSightUnderInspection().getNumReviews();

		numReviewsTv.setText(
				String.valueOf(numReviews) + (numReviews == 1 ? " review" : " reviews"));
		ratingTv.setText(String.valueOf(dataManager.getSightUnderInspection().getRating()));

		populateFiveStarRating(sight.getRating());
	}

	/**
	 * Populates Favourite Icon UI
	 */
	public void populateFavouriteIcon() {
		if (dataManager.getSightUnderInspection().isFavourited()) {
			int red_600 = getResources().getColor(R.color.red_600);
			favouriteIconIv.setBackground(
					getResources().getDrawable(R.drawable.sight_action_favorite_red));
			((TextView)findViewById(R.id.num_favourites_tv)).setTextColor(red_600);
			((TextView)findViewById(R.id.favourite_action_title)).setTextColor(red_600);
		} else {
			int teal_500 = getResources().getColor(R.color.teal_500);
			favouriteIconIv.setBackground(
					getResources().getDrawable(R.drawable.sight_action_favorite));
			((TextView)findViewById(R.id.num_favourites_tv)).setTextColor(teal_500);
			((TextView)findViewById(R.id.favourite_action_title)).setTextColor(teal_500);
		}
	}

	/**
	 * Populates Rating Icon UI
	 */
	public void populateRatingIcon() {
		if (dataManager.getSightUnderInspection().isRated()) {
			int amber_500 = getResources().getColor(R.color.amber_500);
			rateIconIv.setBackground(getResources().getDrawable(R.drawable.sight_action_rate_sel));
			((TextView)findViewById(R.id.rating_placed_tv)).setTextColor(amber_500);
			((TextView)findViewById(R.id.rate_action_title)).setTextColor(amber_500);
			((TextView)findViewById(R.id.rating_placed_tv))
					.setText(String.valueOf((int)DataManager.userPlacedRating) + "/5");
		} else {
			int teal_500 = getResources().getColor(R.color.teal_500);
			rateIconIv.setBackground(getResources().getDrawable(R.drawable.sight_action_rate));
			((TextView)findViewById(R.id.rating_placed_tv)).setTextColor(teal_500);
			((TextView)findViewById(R.id.rate_action_title)).setTextColor(teal_500);
		}
	}

	/**
	 * Populates Check-In Icon UI
	 */
	private void populateCheckInIcon() {
		if (dataManager.getSightUnderInspection().hasCheckedIn()) {
			int purple_600 = getResources().getColor(R.color.purple_600);
			checkinIconIv.setBackground(getResources()
					.getDrawable(R.drawable.sight_action_place_purple));
			((TextView)findViewById(R.id.checkin_action_title)).setTextColor(purple_600);

		} else {
			int teal_500 = getResources().getColor(R.color.teal_500);
			checkinIconIv.setBackground(getResources().getDrawable(R.drawable.sight_action_place));
			((TextView)findViewById(R.id.checkin_action_title)).setTextColor(teal_500);
		}
	}
}
