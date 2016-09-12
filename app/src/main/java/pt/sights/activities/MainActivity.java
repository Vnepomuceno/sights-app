package pt.sights.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import pt.sights.R;
import pt.sights.adapter.DrawerLayoutAdapter;
import pt.sights.data.DataManager;

public class MainActivity extends AppCompatActivity {

	private DataManager dataManager;

	private DrawerLayout drawerLayout;
	private int position;
	private boolean loadedFragment = false;

	private final String[] TITLES = { "Explore", "City Map", "My Profile", "Send feedback", "Rate on Play Store", "About Sights", "Sign Out" };
	private final int[] ICONS = {
		R.drawable.drawer_layout_explore,
		R.drawable.drawer_layout_map,
		R.drawable.drawer_layout_profile,
		R.drawable.drawer_layout_feedback,
		R.drawable.drawer_layout_rate,
		R.drawable.drawer_layout_about,
		R.drawable.drawer_layout_logout,
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dataManager = (DataManager) getApplication();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.getBackground().setAlpha(255);
		toolbar.setTitle(R.string.title_explore);
		setSupportActionBar(toolbar);

		RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.drawer_layout_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		RecyclerView.Adapter mAdapter = new DrawerLayoutAdapter(TITLES, ICONS, this, dataManager);

		mRecyclerView.setAdapter(mAdapter);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				try {
					if (getSupportActionBar() != null) {
						getSupportActionBar().setTitle(R.string.app_name);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (getSupportActionBar() != null) {
					getSupportActionBar().setTitle(TITLES[position]);
				}
			}
		};

		drawerLayout.setDrawerListener(mDrawerToggle);

		mDrawerToggle.syncState();

		selectItem(0);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void selectItem(int position) {
		Fragment fragment = null;

		switch (position) {

			case 0: // EXPLORE
				fragment = new ExploreFragment();
				break;

			case 1: // MAP
				Intent mapIntent = new Intent(this, CityMapActivity.class);
				startActivity(mapIntent);
				break;

			case 2: // PROFILE
				fragment = new ProfileFragment();
				break;

			case 3: // FEEDBACK
				fragment = new FeedbackFragment();
				break;

			case 4: // RATE
				Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
				Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
				try {
					startActivity(goToMarket);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
							"http://play.google.com/store/apps/details?id=" + this.getPackageName())));
				}
				break;

			case 5: // ABOUT
				Intent aboutIntent = new Intent(this, AboutActivity.class);
				startActivity(aboutIntent);
				break;

			case 6: // SIGN OUT
				dataManager.signOut(this);
				Intent loginIntent = new Intent(this, LoginActivity.class);
				startActivity(loginIntent);
				break;

			default:
				break;
		}

		if (fragment != null) {
			if (!loadedFragment || this.position != position) {
				loadedFragment = true;
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			}

			drawerLayout.closeDrawer(Gravity.START);
		} else {
			Log.e("MainActivity", "Error in creating fragment");
		}

		this.position = position;
	}

	@Override
	public void onBackPressed() {
		finishAffinity();
	}
}
