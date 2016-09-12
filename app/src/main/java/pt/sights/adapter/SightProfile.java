package pt.sights.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	6th of June of 2015
 */
public class SightProfile implements Comparable<SightProfile> {

	public int id;
	public String name;
	public Bitmap image;
	public String eventDate;
	public int nDaysAgo;
	public String rating;

	@Override
	public int compareTo(@NonNull SightProfile another) { return name.compareTo(another.name); }
}
