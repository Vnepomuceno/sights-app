package pt.sights.utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	24th of January of 2015
 */
public class Format {

	public static float precisionFloat(int numPlaces, float f) {
		BigDecimal bd = new BigDecimal(String.valueOf(f));
		bd = bd.setScale(numPlaces, BigDecimal.ROUND_HALF_UP);

		return bd.floatValue();
	}

	public static int getNumDaysBetweenDates(Date date) {
		Calendar today = Calendar.getInstance();
		Calendar thatDate = Calendar.getInstance();
		thatDate.setTime(date);

		return (int)((today.getTime().getTime() - date.getTime()) / (24 * 60 * 60 * 1000));
	}

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
