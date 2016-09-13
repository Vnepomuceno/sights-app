package pt.sights.utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * String formatter class.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	24th of January of 2015
 */
public class Format {

	/**
	 * Formats float number with a given number of decimal places.
	 * @param numPlaces Number of decimal places.
	 * @param f Float to format.
	 * @return Formatted float number.
	 */
	public static float precisionFloat(int numPlaces, float f) {
		BigDecimal bd = new BigDecimal(String.valueOf(f));
		bd = bd.setScale(numPlaces, BigDecimal.ROUND_HALF_UP);

		return bd.floatValue();
	}

	/**
	 * Returns the number of days between today and a given date.
	 * @param date Date to calculate number of days.
	 * @return Number of days between today and a given date.
	 */
	public static int getNumDaysBetweenDates(Date date) {
		Calendar today = Calendar.getInstance();
		Calendar thatDate = Calendar.getInstance();
		thatDate.setTime(date);

		return (int)((today.getTime().getTime() - date.getTime()) / (24 * 60 * 60 * 1000));
	}

	/**
	 * Formats a certain number of days in natural language.
	 * Examples: "today", "yesterday", "2 days ago", "1 week ago", "6 months ago".
	 * @param numDays Number of days to format.
	 * @return Formatted number of days in natural language.
	 */
	public static String getNumDaysInNaturalLang(int numDays) {
		String res = "";

		switch (numDays) {
			case 0: res = "today"; break;
			case 1: res = "yesterday"; break;
			default:
				int numWeeks = numDays / 7;
				int numMonths = numWeeks / 4;

				if (numWeeks >= 1) {
					if (numWeeks == 1)
						res = numWeeks + " week ago";
					else if (numWeeks > 1) {
						if (numMonths  >= 1) {
							if (numMonths == 1)
								res = numMonths + " month ago";
							else
								res = numMonths + " months ago";
						}
						else
							res = numWeeks + " weeks ago";
					}
				}
				else
					res = numDays + " days ago";
				break;
		}

		return res;
	}

}
