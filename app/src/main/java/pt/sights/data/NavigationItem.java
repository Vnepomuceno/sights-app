package pt.sights.data;

import android.graphics.drawable.Drawable;

/**
 * Created by poliveira on 24/10/2014.
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
