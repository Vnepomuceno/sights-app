package pt.sights.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.sights.R;
import pt.sights.data.DataManager;

import static pt.sights.data.DataManager.LiquidActivityType.REGISTER;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;

public class RegisterActivity extends Activity implements LoaderCallbacks<Cursor> {

	private UserRegisterTask mAuthTask = null;

	private DataManager dataManager;

	// UI references.
	private EditText mUsernameView;
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView, mConfirmPasswordView;
	private View mRegisterFormView;
	private ProgressBar registerProgressBar;
	private TextView progressLabelTv;
	private Button mEmailRegisterButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Set up the login form.
		registerProgressBar = (ProgressBar) findViewById(R.id.register_progress);
		registerProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		progressLabelTv = (TextView) findViewById(R.id.register_progress_text);

		mUsernameView = (EditText) findViewById(R.id.username_register_et);
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email_register_et);
		populateAutoComplete();

		mPasswordView = (EditText) findViewById(R.id.password_register_et);
		mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password_register_et);
		mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptRegister();
					return true;
				}
				return false;
			}
		});

		mEmailRegisterButton = (Button) findViewById(R.id.email_sign_up_button);
		mEmailRegisterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (attemptRegister())
					showProgress(false);
				else
					showProgress(true);
			}
		});

		mRegisterFormView = findViewById(R.id.register_form);

		dataManager = (DataManager) getApplication();
		dataManager.trackLiquidEvent(REGISTER, ENTER);
	}

	private void populateAutoComplete() {
		getLoaderManager().initLoader(0, null, this);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private boolean attemptRegister() {
		if (mAuthTask != null) {
			return true;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String username = mUsernameView.getText().toString();
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();
		String confirmPassword = mConfirmPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;


		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} else if (username.contains(" ")) {
			mUsernameView.setError(getString(R.string.username_no_spaces));
			focusView = mUsernameView;
			cancel = true;
		}

		// Check for a valid email.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!isEmailValid(email)) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		// Check for a valid password
		if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}
		if (!password.equals(confirmPassword)) {
			mPasswordView.setError(getString(R.string.error_password_not_matched));
			mConfirmPasswordView.setError(getString(R.string.error_password_not_matched));
			focusView = mConfirmPasswordView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			mAuthTask = new UserRegisterTask();
			mAuthTask.execute((Void) null);
		}

		return cancel;
	}

	private boolean isEmailValid(String email) {
		return email.contains("@");
	}

	private boolean isPasswordValid(String password) {
		return password.length() > 4;
	}

	/**
	 * Shows the progress UI and hides the register form.
	 * @param e Exception thrown when attempting to register, null in case the attempt is successful
	 * @param show True if progress section is to be shown, false otherwise
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			progressLabelTv.setVisibility(show ? View.VISIBLE : View.GONE);
			registerProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			registerProgressBar.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					registerProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mEmailRegisterButton.setVisibility(show ? View.GONE : View.VISIBLE);
		} else {
			registerProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
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

		addEmailsToAutoComplete(emails);
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


	private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
		//Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(RegisterActivity.this,
						android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

		mEmailView.setAdapter(adapter);
	}

	/**
	 * Represents an asynchronous registration task used to authenticate
	 * the user.
	 */
	private class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			dataManager.signUp(
					RegisterActivity.this,
					mUsernameView.getText().toString(),
					mEmailView.getText().toString(),
					mPasswordView.getText().toString()
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



