package pt.sights.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by valternepomuceno on 07/12/14.
 */
class ImageAdapter extends PagerAdapter {

	private final Context context;
	private final List<Bitmap> bitmaps;

	public ImageAdapter(Context context, List<Bitmap> bitmaps) {
		this.context = context;
		this.bitmaps = bitmaps;
	}

	@Override
	public int getCount() {
		return bitmaps == null ? 0 : bitmaps.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ImageView imageView = new ImageView(context);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setImageBitmap(bitmaps.get(position));
		container.addView(imageView, 0);

		return imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((ImageView) object);

	}

	public Bitmap getFirstBitmap() {
		if (bitmaps.size() > 0)
			return bitmaps.get(0);
		return null;
	}
}
