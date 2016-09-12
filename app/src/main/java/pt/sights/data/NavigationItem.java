package pt.sights.data;

import android.graphics.drawable.Drawable;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	24th of October of 2014
 */
public class NavigationItem {
	private String mText;
	private Drawable mDrawable;

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		mText = text;
	}

	public Drawable getDrawable() {
		return mDrawable;
	}

	public void setDrawable(Drawable drawable) {
		mDrawable = drawable;
	}
}
