package pt.sights.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pt.sights.R;
import pt.sights.data.DataManager;

import static pt.sights.data.DataManager.LiquidActivityType.ABOUT;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_FACEBOOK;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_GITHUB;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_INSTAGRAM;
import static pt.sights.data.DataManager.LiquidEventType.GO_TO_LINKEDIN;

public class AboutActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		final DataManager dataManager = (DataManager) getApplication();
		TextView versionTv = (TextView)findViewById(R.id.about_version);
		TextView descriptionTv = (TextView)findViewById(R.id.about_app_description);
		Button facebookBtn = (Button)findViewById(R.id.about_facebook_btn);
		Button instagramBtn = (Button)findViewById(R.id.about_instagram_btn);
		Button linkedinBtn = (Button)findViewById(R.id.about_linkedin_btn);
		Button githubBtn = (Button)findViewById(R.id.about_github_btn);
		String version;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			version = "1.0";
		}

		dataManager.trackLiquidEvent(ABOUT, ENTER);

		versionTv.setText("Version " + version);
		descriptionTv.setText(Html.fromHtml(getString(R.string.app_description)));

		facebookBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.trackLiquidEvent(ABOUT, GO_TO_FACEBOOK);
				Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
				facebookIntent.setData(Uri.parse(getResources().getString(R.string.facebook_url)));
				startActivity(facebookIntent);
			}
		});
		instagramBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.trackLiquidEvent(ABOUT, GO_TO_INSTAGRAM);
				Intent instagramIntent = new Intent(Intent.ACTION_VIEW);
				instagramIntent.setData(Uri.parse(getResources().getString(R.string.instagram_url)));
				startActivity(instagramIntent);
			}
		});
		linkedinBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.trackLiquidEvent(ABOUT, GO_TO_LINKEDIN);
				Intent linkedinIntent = new Intent(Intent.ACTION_VIEW);
				linkedinIntent.setData(Uri.parse(getResources().getString(R.string.linkedin_url)));
				startActivity(linkedinIntent);
			}
		});
		githubBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataManager.trackLiquidEvent(ABOUT, GO_TO_GITHUB);
				Intent githubIntent = new Intent(Intent.ACTION_VIEW);
				githubIntent.setData(Uri.parse(getResources().getString(R.string.github_url)));
				startActivity(githubIntent);
			}
		});


		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		try {
			if (getSupportActionBar() != null) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		//int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}
}
