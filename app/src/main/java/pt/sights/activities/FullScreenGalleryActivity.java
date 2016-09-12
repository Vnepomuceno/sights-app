package pt.sights.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import pt.sights.R;
import pt.sights.adapter.FullScreenImageAdapter;
import pt.sights.data.DataManager;
import pt.sights.utils.TouchImageView;

public class FullScreenGalleryActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen_image_gallery);

		DataManager dataManager = (DataManager) getApplication();
		Intent intent = getIntent();

		int imgPos = intent.getExtras().getInt("imgId");
		int sightPos = intent.getExtras().getInt("sightId");

		FullScreenImageAdapter imageAdapter = new FullScreenImageAdapter(this,
				dataManager.getSightLocations().get(sightPos).getImages());

		TouchImageView imageView = (TouchImageView) findViewById(R.id.full_screen_image);
		imageView.setImageBitmap(imageAdapter.images.get(imgPos));

		TextView textView = (TextView) findViewById(R.id.full_screen_caption);
		textView.setText(dataManager.getSightLocations().get(sightPos).name);

		ViewPager viewPager = (ViewPager) findViewById(R.id.pager_full_screen_gallery);
		if (viewPager != null)
			viewPager.setAdapter(imageAdapter);
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_full_screen_gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}*/
}
