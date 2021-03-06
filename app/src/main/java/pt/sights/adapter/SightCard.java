package pt.sights.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	21th of December of 2014
 */
public class SightCard implements Comparable<SightCard> {

	public int id;
	public String name;
	public Bitmap image;
	public float rate;
	public String when;
	public int whenInDays;
	public String userRating;

	@Override
	public int compareTo(@NonNull SightCard another) {
		return name.compareTo(another.name);
	}

}
