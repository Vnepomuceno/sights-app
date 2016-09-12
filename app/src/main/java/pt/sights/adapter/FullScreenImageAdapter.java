package pt.sights.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.List;

import pt.sights.R;
import pt.sights.utils.TouchImageView;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	24th of August of 2015
 */
public class FullScreenImageAdapter extends PagerAdapter {

	public Context context;
	public List<Bitmap> images;

	public FullScreenImageAdapter(Context context, List<Bitmap> images) {
		this.context = context;
		this.images = images;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		TouchImageView imgDisplay;
		Button btnClose;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.full_screen_image_gallery, container,
				false);

		imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.full_screen_image);
		btnClose = (Button) viewLayout.findViewById(R.id.btnClose);

		Bitmap bitmap = images.get(position);
		imgDisplay.setImageBitmap(bitmap);

		// close button click event
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity)context).finish();
			}
		});

		container.addView(viewLayout);

		return viewLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((RelativeLayout)object);
	}

}
