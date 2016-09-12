package pt.sights.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

import pt.sights.R;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	5th of May of 2015
 */
public class Utils {

	/**
	 * Queries device screen height in case of success, and the default screen height otherwise
	 * @param activity Calling activity
	 * @return Screen height in pixels
	 */
	public static int getScreenHeight(Activity activity) {
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			return metrics.heightPixels;
		} catch (Exception e) {
			return activity.getResources().getDimensionPixelSize(R.dimen.default_screen_height);
		}
	}

	/**
	 * Queries device screen width in case of success, and the default screen width otherwise
	 * @param activity Calling activity
	 * @return Screen width in pixels
	 */
	public static int getScreenWidth(Activity activity) {
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			return metrics.widthPixels;
		} catch (Exception e) {
			return activity.getResources().getDimensionPixelSize(R.dimen.default_screen_width);
		}
	}

	/**
	 * Build a Google Static Maps URL to request the bitmap via API
	 * @param width Width of the bitmap
	 * @param height Height of the bitmap
	 * @param coordinates Coordinates of the sight
	 * @return Google Static Maps URL to request the bitmap
	 */
	public static String getStaticMapUrl(int width, int height, String[] coordinates) {
		return "https://maps.googleapis.com/maps/api/staticmap?" +
				"center=" + coordinates[0] + "," + coordinates[1] +
				"&zoom=15&size=" + width + "x" + height + "&scale=2" +
				"&markers=color:0x009688%7C" + coordinates[0] + "," + coordinates[1] +
				"&maptype=roadmap&key=AIzaSyBh49JMIcbXmkspjkA8hG0bmuAZHcNKy_o";
	}

}
