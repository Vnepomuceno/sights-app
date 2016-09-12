package pt.sights.data;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;
import pt.sights.R;
import pt.sights.activities.LoginActivity;
import pt.sights.activities.MainActivity;
import pt.sights.activities.ProfileFragment;
import pt.sights.activities.RegisterActivity;
import pt.sights.activities.SightDetailActivity;
import pt.sights.adapter.SightCardAdapter;
import pt.sights.adapter.SightCard;
import pt.sights.adapter.SightProfile;
import pt.sights.adapter.SightProfileAdapter;
import pt.sights.utils.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import citysdk.tourism.client.poi.single.PointOfInterest;
import io.lqd.sdk.Liquid;
import pt.sights.utils.Utils;

import static pt.sights.data.DataManager.LiquidActivityType.CHECK_IN_DIALOG;
import static pt.sights.data.DataManager.LiquidActivityType.FEEDBACK;
import static pt.sights.data.DataManager.LiquidActivityType.LOGIN;
import static pt.sights.data.DataManager.LiquidActivityType.LOGOUT;
import static pt.sights.data.DataManager.LiquidActivityType.RATE_DIALOG;
import static pt.sights.data.DataManager.LiquidActivityType.REGISTER;
import static pt.sights.data.DataManager.LiquidActivityType.RESET;
import static pt.sights.data.DataManager.LiquidActivityType.SIGHT_DETAIL;
import static pt.sights.data.DataManager.LiquidEventType.CHECK_IN;
import static pt.sights.data.DataManager.LiquidEventType.FAVOURITE;
import static pt.sights.data.DataManager.LiquidEventType.RATE_SIGHT;
import static pt.sights.data.DataManager.LiquidEventType.RESET_PWD;
import static pt.sights.data.DataManager.LiquidEventType.SEND_FEEDBACK;
import static pt.sights.data.DataManager.LiquidEventType.SIGN_IN;
import static pt.sights.data.DataManager.LiquidEventType.SIGN_OUT;
import static pt.sights.data.DataManager.LiquidEventType.SIGN_UP;
import static pt.sights.data.DataManager.LiquidEventType.UNFAVOURITE;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_FACEBOOK;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_GITHUB;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_INSTAGRAM;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_LINKEDIN;


/**
 * Created by valternepomuceno on 02/12/2014.
 */
public class DataManager extends Application {

	public enum LiquidActivityType { LOGIN, LOGOUT, REGISTER, RESET, SIGHT_DETAIL, CHECK_IN_DIALOG,
		RATE_DIALOG, EXPLORE, MAP, PROFILE, FEEDBACK, ABOUT }

	public enum LiquidEventType { ENTER, SIGN_IN, SIGN_OUT, SIGN_UP, RESET_PWD, RATE_SIGHT,
		CLOSE_RATE_DIALOG, CHECK_IN, FAVOURITE, UNFAVOURITE, SHOW_MORE_DESCRIPTION, SHOW_LESS_DESCRIPTION,
		SHOW_GALLERY, GO_TO_MAP, SEND_FEEDBACK, NO_INTERNET, GO_TO_FACEBOOK, GO_TO_INSTAGRAM,
		GO_TO_LINKEDIN, GO_TO_GITHUB }

	private Liquid liquid;
	private List<Sight> sights;
	private List<SightCard> userFavouriteSights, userRatedSights;
	private SightCardAdapter sightCardAdapter;
	private SightProfileAdapter sightProfileRatedAdapter;
	private int sightDetailPos;
	private Context context;
	public String userId;

	public static float userPlacedRating;
	public static String suggestedFeedback;
	public static boolean isInForegroundMode;

	private int screenHeight = 0, screenWidth = 0;

	public void onCreate() {
		super.onCreate();

		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getApplicationContext().getPackageName(),PackageManager.GET_META_DATA);
			Bundle bundle = appInfo.metaData;

			ParseCrashReporting.enable(this);
			Parse.initialize(this,
					bundle.getString("com.parse.Parse.APPLICATION_ID"),
					bundle.getString("com.parse.Parse.CLIENT_KEY"));
			PushService.setDefaultPushCallback(this, MainActivity.class);

			liquid = Liquid.initialize(this,
					bundle.getString("io.lqd.DEVELOPMENT_TOKEN"));

			ParseUser currentUser = ParseUser.getCurrentUser();
			if (currentUser != null) {
				userId = currentUser.getUsername();

				Intent exploreIntent = new Intent(this, MainActivity.class);
				exploreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(exploreIntent);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a List of PointOfInterest objects extracted from wrapper Sight
	 * @return List of points of interest
	 */
	public List<PointOfInterest> getPointsOfInterest() {
		List<PointOfInterest> list = new ArrayList<>();

		if (sights != null) {
			for (Sight wrapperPoi : sights) {
				list.add(wrapperPoi.getPoi());
			}
		}

		return (list.size() > 0) ? list : null;
	}

	/**
	 * Logs in user, checking credentials in Parse.
	 * @param context Android context of the application.
	 * @param userId Registration email of the user.
	 * @param password Password of the user.
	 */
	public void signIn(final Context context, String userId, String password, boolean isInForeground) {
		this.context = context;

		if (isInForeground) {
			ParseUser.logInInBackground(userId, password, new LogInCallback() {
				@Override
				public void done(ParseUser parseUser, ParseException e) {
					if (parseUser != null) {
						((LoginActivity) context).showProgress(true);
						Intent exploreIntent = new Intent(context, MainActivity.class);
						exploreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(exploreIntent);
					} else if (e != null) {
						((LoginActivity) context).showProgress(false);

						handleParseException(e);
					}
				}
			});

			this.userId = userId;

			liquid.identifyUser(userId);
			trackLiquidEvent(LOGIN, SIGN_IN);
		}
	}

	/**
	 * Logs out user from Parse authentication
	 * @param context Android context of the application.
	 */
	public void signOut(Context context) {
		this.context = context;

		ParseUser.logOut();
		trackLiquidEvent(LOGOUT, SIGN_OUT);

		Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(loginIntent);
	}

	/**
	 * Registers new user, storing credentials in Parse.
	 * @param context Android context of the application.
	 * @param userName Name of the user.
	 * @param userId Registration email of the user.
	 * @param password Password of the user.
	 */
	public void signUp(final Context context, String userName, String userId, String password) {
		this.context = context;

		ParseUser user = new ParseUser();
		user.setUsername(userName);
		user.setPassword(password);
		user.setEmail(userId);

		user.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException pe) {
				if (pe == null) {
					((RegisterActivity) context).showProgress(true);
					Toast.makeText(context, getResources().getString(R.string.successful_register), Toast.LENGTH_LONG).show();

					Intent loginIntent = new Intent(context, LoginActivity.class);
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(loginIntent);
				} else {
					((RegisterActivity) context).showProgress(false);

					handleParseException(pe);
				}
			}
		});

		trackLiquidEvent(REGISTER, SIGN_UP);
	}

	/**
	 * Resets password of a user via email
	 * @param context Android context of the application
	 * @param email Registration email of the user
	 */
	public void resetPassword(final Context context, String email) {
		this.context = context;

		ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(context, getResources().getString(R.string.successful_reset_pwd),
							Toast.LENGTH_LONG).show();

					Intent loginIntent = new Intent(context, LoginActivity.class);
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(loginIntent);
				} else {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});

		trackLiquidEvent(RESET, RESET_PWD);
	}

	/**
	 * Favourites sight under inspection to Parse
	 * @param context Android context of the application
	 */
	public void favoriteSight(Context context) {
		final Sight sight = getSightUnderInspection();

		if (sight.getFavourite() != null) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Favourite");
			query.whereEqualTo("userId", this.userId);
			query.whereEqualTo("sightId", sight.id());

			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> list, ParseException e) {
					if (e == null) {
						for (ParseObject po : list) {
							po.deleteInBackground();
						}
						sight.setFavourite(null);

						trackLiquidEvent(SIGHT_DETAIL, UNFAVOURITE);
					}
				}
			});

		} else if (userId != null) {
			ParseObject favourite = new ParseObject("Favourite");
			favourite.put("userId", userId);
			favourite.put("sightId", getSightUnderInspection().id());
			favourite.put("sightName", getSightUnderInspection().name);

			favourite.saveInBackground();

			sight.setFavourite(favourite);
		}

		countFavourites(context, getSightUnderInspection().id());

		trackLiquidEvent(SIGHT_DETAIL, FAVOURITE);
	}

	/**
	 * Queries to Parse if user favourited sight under inspection
	 * @param context Android context of the application
	 */
	public void getParseFavourite(final Context context) {
		final Sight sight = getSightUnderInspection();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Favourite");
		query.whereEqualTo("userId", this.userId);
		query.whereEqualTo("sightId", sight.id());

		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null && list.size() > 0) {
					sight.setFavourite(list.get(0));
					sight.updateFavourited();
					((SightDetailActivity) context).populateFavouriteIcon();
				}
			}
		});
	}

	/**
	 * Queries to Parse the rating the user attributed to sight under inspection
	 * @param context Android context of the application
	 */
	public void getUserSightRatingParse(final Context context) {
		final Sight sight = getSightUnderInspection();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Rating");
		query.whereEqualTo("userId", this.userId);
		query.whereEqualTo("sightId", sight.id());

		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null && list.size() > 0) {
					sight.setRating(list.get(0));
					sight.updateRated();
					((SightDetailActivity)context).populateRatingIcon();
				}
			}
		});
	}

	/**
	 * Queries to parse the average rating attributed to the sight under inspection
	 * @param context Android context of the application
	 */
	public void getSightRatingParse(final Context context) {
		final Sight sight = getSightUnderInspection();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Rating");
		query.whereEqualTo("sightId", sight.id());

		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null) {
					int sumRatings = 0;
					int numReviews = 0;

					for (ParseObject parseObject : list) {
						numReviews++;
						sumRatings += parseObject.getNumber("rating").intValue();
					}

					if (numReviews > 0) {
						float avgRating = sumRatings / (float)numReviews;
						sight.setRating(avgRating);
						sight.setNumReviews(numReviews);

						((SightDetailActivity) context).updateRatingData();
					}
				}
			}
		});
	}

	/**
	 * Checks-in sight under inspection to Parse
	 * @param checkInComment Comment written by user when checking-in
	 */
	public void checkInSight(String checkInComment) {
		Sight sight = getSightUnderInspection();

		if (sight.getCheckin() != null) {
			Toast.makeText(this, "You already checked-in in this sight today",
					Toast.LENGTH_SHORT).show();
		} else {
			ParseObject checkin = new ParseObject("CheckIn");

			checkin.put("userId", userId);
			checkin.put("sightId", sight.id());
			checkin.put("sightName", sight.name);
			checkin.put("comment", checkInComment);

			checkin.saveInBackground();
			sight.setCheckin(checkin);

			trackLiquidEvent(CHECK_IN_DIALOG, CHECK_IN);
		}
	}

	/**
	 * Rates sight under inspection to Parse
	 * @param ratingValue Rating value placed by the user from 0 to 5
	 */
	public void rateSight(float ratingValue) {
		Sight sight = getSightUnderInspection();
		userPlacedRating = ratingValue;

		ParseObject rating = new ParseObject("Rating");

		rating.put("userId", userId);
		rating.put("sightId", sight.id());
		rating.put("sightName", sight.name);
		rating.put("rating", ratingValue);

		rating.saveInBackground();

		trackLiquidEvent(RATE_DIALOG, RATE_SIGHT);
	}

	/**
	 * Stores feedback message sent by user to Parse
	 * @param feedbackMessage Feedback message entered by user
	 */
	public void submitFeedback(String feedbackMessage) {
		ParseObject feedback = new ParseObject("Feedback");

		feedback.put("userId", userId);
		feedback.put("feedback", feedbackMessage);

		feedback.saveInBackground();

		trackLiquidEvent(FEEDBACK, SEND_FEEDBACK);
	}

	/**
	 * Asynchronously counts favourites of a given sight in Parse
	 * @param context Android context of the application
	 * @param sightId Sight ID
	 */
	public void countFavourites(Context context, int sightId) {
		this.context = context;

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Favourite");
		query.whereEqualTo("sightId", sightId);
		query.countInBackground(new CountCallback() {
			@Override
			public void done(int i, ParseException pe) {
				if (pe == null) {
					getSightUnderInspection().setNumFavourites(i);
					((SightDetailActivity) (DataManager.this).context).updateNumFavourites();
				} else
					Toast.makeText(DataManager.this,
							"Failed counting favourites for sight.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Queries favourited sights by the user in Parse
	 * @param context Android context of the application
	 */
	public void retrieveFavouriteSights(final ProfileFragment context) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Favourite");
		query.whereEqualTo("userId", userId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null) {
					if (userFavouriteSights == null || list.size() != userFavouriteSights.size()) {
						userFavouriteSights = new ArrayList<>();

						for (int i = 0; i < list.size(); i++) {
							ParseObject parseObject = list.get(i);
							String sightName = parseObject.getString("sightName");

							for (int j = 0; j < sights.size(); j++) {
								Sight sight = sights.get(j);

								if (sight.name.equals(sightName)) {
									Date favouritedWhen = parseObject.getCreatedAt();
									int numDaysFavourited =
											Format.getNumDaysBetweenDates(favouritedWhen);

									SightCard card = new SightCard();
									card.id = sight.id();
									card.image = sight.getCoverImage();
									card.name = sight.name;
									card.when = "Favourited "
											+ Format.getNumDaysInNaturalLang(numDaysFavourited);
									card.whenInDays = numDaysFavourited;

									userFavouriteSights.add(card);
									break;
								}
							}
						}

						context.updateFavouriteSightsSection(true);
					}
				} else {
					Toast.makeText(DataManager.this,
							"Failed getting user favourite sights.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * Queries rated sights by the user in Parse
	 * @param context Android context of the application
	 */
	public void retrieveRatedSights(final ProfileFragment context) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Rating");
		query.whereEqualTo("userId", userId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null) {
					if (userRatedSights == null || list.size() != userFavouriteSights.size()) {
						userRatedSights = new ArrayList<>();

						for (int i = 0; i < list.size(); i++) {
							ParseObject parseObject = list.get(i);
							String sightName = parseObject.getString("sightName");

							for (int j = 0; j < sights.size(); j++) {
								Sight sight = sights.get(j);

								if (sight.name.equals(sightName)) {
									int rating = parseObject.getNumber("rating").intValue();
									Date ratedWhen = parseObject.getCreatedAt();
									int numDaysRated = Format.getNumDaysBetweenDates(ratedWhen);

									SightCard card = new SightCard();
									card.id = sight.id();
									card.image = sight.getCoverImage();
									card.name = sight.name;
									card.when = "Rated " +
											Format.getNumDaysInNaturalLang(numDaysRated);
									card.whenInDays = numDaysRated;
									card.userRating = "You rated " + rating + " stars";

									userRatedSights.add(card);
									break;
								}
							}
						}

						context.updateRatedSightsSection(true);
					}
				} else {
					Toast.makeText(DataManager.this,
							"Failed getting user rated sights.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * Tracks an event to Liquid, gathering the type of activity and event tracked
	 * @param typeActivity Type of Activity to track
	 * @param eventType Type of Event to track
	 */
	public void trackLiquidEvent(LiquidActivityType typeActivity, LiquidEventType eventType) {
		String eventName = "";
		HashMap<String, Object> attrs = new HashMap<>();
		attrs.put("userId", this.userId);

		switch (typeActivity) {
			case LOGIN:
				switch(eventType) {
					case ENTER:     eventName = "Enter Login User"; break;
					case SIGN_IN:   eventName = "Sign In User"; break;
				}
				break;

			case LOGOUT:
				switch (eventType) {
					case SIGN_OUT:  eventName = "Sign Out User"; break;
				}
				break;

			case REGISTER:
				switch (eventType) {
					case ENTER:     eventName = "Enter Register User"; break;
					case SIGN_UP:   eventName = "Sign Up User"; break;
				}
				break;

			case RESET:
				switch (eventType) {
					case ENTER:     eventName = "Enter Reset Password"; break;
					case RESET_PWD: eventName = "User Reset Password"; break;
				}
				break;

			case RATE_DIALOG:
				attrs.put("sightName", getSightUnderInspection().name);

				switch (eventType) {
					case ENTER:                 eventName = "Enter Rate Dialog"; break;
					case RATE_SIGHT:            eventName = "Rate Sight";
												attrs.put("rating", userPlacedRating); break;
					case CLOSE_RATE_DIALOG:     eventName = "Close Rate Dialog"; break;
				}
				break;

			case SIGHT_DETAIL:
				attrs.put("sightName", getSightUnderInspection().name);

				switch (eventType) {
					case ENTER:                 eventName = "Enter Sight Detail"; break;
					case FAVOURITE:             eventName = "Favourite Sight"; break;
					case UNFAVOURITE:           eventName = "Unfavourite Sight"; break;
					case CHECK_IN:              eventName = "Check-in Sight"; break;
					case SHOW_MORE_DESCRIPTION: eventName = "Show More Description"; break;
					case SHOW_LESS_DESCRIPTION: eventName = "Show Less Description"; break;
					case SHOW_GALLERY:          eventName = "Show Gallery"; break;
					case GO_TO_MAP:             eventName = "Go for Directions"; break;
				}
				break;

			case EXPLORE:
				switch (eventType) {
					case ENTER: eventName = "Enter Explore"; break;
				}
				break;

			case MAP:
				switch (eventType) {
					case ENTER: eventName = "Enter Map"; break;
				}
				break;

			case PROFILE:
				switch (eventType) {
					case ENTER: eventName = "Enter Profile"; break;
				}
				break;

			case FEEDBACK:
				switch (eventType) {
					case ENTER:         eventName = "Enter Feedback"; break;
					case SEND_FEEDBACK: eventName = "Send Feedback";
										attrs.put("feedback", suggestedFeedback); break;
				}
				break;

			case ABOUT:
				switch (eventType) {
					case ENTER:             eventName = "Enter About"; break;
					case GO_TO_FACEBOOK:    eventName = "Go to Facebook Page"; break;
					case GO_TO_LINKEDIN:    eventName = "Go to LinkedIn Page"; break;
					case GO_TO_GITHUB:      eventName = "Go to GitHub Page"; break;
					case GO_TO_INSTAGRAM:   eventName = "Go to Instagram Page"; break;
				}
				break;

		}

		if (liquid != null)
			liquid.track(eventName, attrs);
	}

	/**
	 * Prepares error message to display to user and shows it using Toast
	 * @param pe Parse exception
	 */
	private void handleParseException(ParseException pe) {
		String errorMessage;
		String parseExcStart = "com.parse.ParseException: ";

		switch (pe.getCode()) {

			case 100:
				errorMessage = "The connection failed. Make sure you are connected to the Internet.";
				break;

			case 101:
			case 125:
			case 202:
			case 203:
				errorMessage = pe.toString().startsWith(parseExcStart) ?
						pe.toString().substring(parseExcStart.length()) :
						pe.toString();
				errorMessage = Character.toUpperCase(errorMessage.charAt(0))
						+ errorMessage.substring(1);
				break;

			default:
				errorMessage = pe.toString();
				break;

		}

		Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
	}

	/**
	 * Queries Profile Favourite Sights
	 * @return List of SightProfile favourite objects
	 */
	public List<SightProfile> getProfileFavouriteSights() {
		List<SightProfile> list = new ArrayList<>();

		if (this.userFavouriteSights != null) {
			for (SightCard sc : this.userFavouriteSights) {
				SightProfile sp = new SightProfile();

				sp.id = sc.id;
				sp.name = sc.name;
				sp.image = sc.image;
				sp.eventDate = sc.when;
				sp.nDaysAgo = sc.whenInDays;

				list.add(sp);
			}

			Collections.sort(list, new Comparator<SightProfile>() {
				@Override
				public int compare(SightProfile lhs, SightProfile rhs) {
					Integer lhsInt = new Integer(lhs.nDaysAgo), rhsInt = new Integer(rhs.nDaysAgo);
					return lhsInt.compareTo(rhsInt);
				}
			});
		}

		return list;
	}

	/**
	 * Queries Profile Rated Sights
	 * @return List of SightProfile rated objects
	 */
	public List<SightProfile> getProfileRatedSights() {
		List<SightProfile> list = new ArrayList<>();

		if (this.userRatedSights != null) {
			for (SightCard sc : this.userRatedSights) {
				SightProfile sp = new SightProfile();

				sp.id = sc.id;
				sp.name = sc.name;
				sp.image = sc.image;
				sp.rating = sc.userRating;
				sp.eventDate = sc.when;
				sp.nDaysAgo = sc.whenInDays;

				list.add(sp);
			}

			Collections.sort(list, new Comparator<SightProfile>() {
				@Override
				public int compare(SightProfile lhs, SightProfile rhs) {
					Integer lhsInt = new Integer(lhs.nDaysAgo), rhsInt = new Integer(rhs.nDaysAgo);
					return lhsInt.compareTo(rhsInt);
				}
			});
		}

		return list;
	}

	/**
	 * Queries sight position using sight name
	 * @param name Sight name
	 * @return Sight position
	 */
	public int getSightPositionByName(String name) {
		for (int i = 0; i < sights.size(); i++) {
			Sight s = sights.get(i);
			if (s.name.equals(name))
				return i;
		}

		return -1;
	}

	/**
	 * Queries number of sights
	 * @return Number of sights
	 */
	public int getNumberOfSights() {
		if (sights != null)
			return sights.size();
		else
			return 0;
	}

	/**
	 * Queries and stores mobile screen height
	 * @param activity Calling activity
	 * @return Mobile screen height in pixels
	 */
	public int getScreenHeight(Activity activity) {
		if (screenHeight == 0)
			screenHeight = Utils.getScreenHeight(activity);

		return screenHeight;
	}

	/**
	 * Queries and stores mobile screen width
	 * @param activity Calling activity
	 * @return Mobile screen width in pixels
	 */
	public int getScreenWidth(Activity activity) {
		if (screenWidth == 0)
			screenWidth = Utils.getScreenWidth(activity);

		return screenWidth;
	}

	/**
	 * GETTERS and SETTERS for DataManager class
	 */
	public void setSightDetailPos(int pos) { sightDetailPos = pos; }
	public SightCardAdapter getSightCardAdapter() { return sightCardAdapter; }
	public List<Sight> getSightLocations() { return this.sights; }
	public void setSightLocations(List<Sight> locations) { this.sights = locations; }
	public Sight getSightUnderInspection() { return this.sights.get(sightDetailPos); }
	public List<SightCard> getFavouriteSights() { return this.userFavouriteSights; }
	public List<SightCard> getRatedSights() { return this.userRatedSights; }

	public void setSightProfileFavouritesAdapter(SightProfileAdapter sightProfileFavouritesAdapter) {
		SightProfileAdapter sightProfileFavouritesAdapter1 = sightProfileFavouritesAdapter;
	}
	public void setSightCardAdapter(SightCardAdapter sightCardAdapter) {
		this.sightCardAdapter = sightCardAdapter;
	}
	public void setSightProfileRatedAdapter(SightProfileAdapter sightProfileRatedAdapter) {
		this.sightProfileRatedAdapter = sightProfileRatedAdapter;
	}

}
