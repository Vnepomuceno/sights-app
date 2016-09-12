package pt.sights.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseUser;

import pt.sights.R;
import pt.sights.data.DataManager;

import static pt.sights.data.DataManager.LiquidActivityType.LOGIN;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	15th of November of 2014
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

	private UserLoginTask mAuthTask = null;

	private DataManager dataManager;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private Button mResetPwdBtn, mRegisterBtn;
	private ProgressBar loginProgressBar;
	private TextView progressLabelTv;
	private ImageView appLogoIv;
	private String usernameVal, passwordVal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		dataManager = (DataManager) getApplication();
		ParseUser currentUser = ParseUser.getCurrentUser();

		if (currentUser != null && DataManager.isInForegroundMode) {
			Intent exploreIntent = new Intent(this, MainActivity.class);
			startActivity(exploreIntent);
		}

		// Set up the login form.
		appLogoIv = (ImageView) findViewById(R.id.login_app_logo);
		mUsernameView = (EditText) findViewById(R.id.username_login_et);
		mPasswordView = (EditText) findViewById(R.id.password_login_et);
		loginProgressBar = (ProgressBar) findViewById(R.id.login_progress);
		loginProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		progressLabelTv = (TextView) findViewById(R.id.login_progress_text);
		populateAutoComplete();
		DataManager.isInForegroundMode = true;

		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (attemptLogin())
					showProgress(false);
				else
					showProgress(true);
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mResetPwdBtn = (Button) findViewById(R.id.email_goto_reset_pwd_button);
		mResetPwdBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent resetPwdIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
				startActivity(resetPwdIntent);
			}
		});
		mRegisterBtn = (Button) findViewById(R.id.email_goto_sign_in_button);
		mRegisterBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(registerIntent);
			}
		});

		mUsernameView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				appLogoIv.setVisibility(View.GONE);
			}
		});

		dataManager.trackLiquidEvent(LOGIN, ENTER);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

				// Select only email addresses.
				ContactsContract.Contacts.Data.MIMETYPE +
						" = ?", new String[]{ContactsContract.CommonDataKinds.Email
				.CONTENT_ITEM_TYPE},

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> emails = new ArrayList<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	private interface ProfileQuery {
		String[] PROJECTION = {
				ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
		};

		int ADDRESS = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		DataManager.isInForegroundMode = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		DataManager.isInForegroundMode = true;
	}

	/**
	 *
	 */
	private void populateAutoComplete() {
		getLoaderManager().initLoader(0, null, this);
	}


	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private boolean attemptLogin() {
		if (mAuthTask != null) {
			return true;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String username = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid email address.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}
		else if (username.contains(" ")) {
			mUsernameView.setError(getString(R.string.username_no_spaces));
			focusView = mUsernameView;
			cancel = true;
		}

		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			mAuthTask = new UserLoginTask();
			usernameVal = mUsernameView.getText().toString();
			passwordVal = mPasswordView.getText().toString();
			mAuthTask.execute((Void) null);
		}

		return cancel;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 * @param show True if progress section is to be shown, false otherwise
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			progressLabelTv.setVisibility(show ? View.VISIBLE : View.GONE);
			loginProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			loginProgressBar.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					loginProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mResetPwdBtn.setVisibility(show ? View.GONE : View.VISIBLE);
			mRegisterBtn.setVisibility(show ? View.GONE : View.VISIBLE);
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			loginProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void onBackPressed() {
		finishAffinity();
	}

	/**
	 * Represents an asynchronous login task used to authenticate
	 * the user.
	 */
	private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			dataManager.userId = usernameVal;
			dataManager.signIn(
					LoginActivity.this,
					usernameVal,
					passwordVal,
					DataManager.isInForegroundMode
			);

			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}
}



