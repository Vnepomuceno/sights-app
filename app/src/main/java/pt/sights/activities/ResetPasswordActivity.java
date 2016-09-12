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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.sights.R;
import pt.sights.data.DataManager;

import static pt.sights.data.DataManager.LiquidActivityType.RESET;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;

public class ResetPasswordActivity extends Activity implements LoaderCallbacks<Cursor> {

	private ResetPasswordTask mResetPwdTask = null;

	private DataManager dataManager;

	// UI references.
	private AutoCompleteTextView mEmailView;
	private ProgressBar resetPwdProgressBar;
	private TextView progressLabelTv;
	private Button mResetPwdButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);

		// Set up the login form.
		resetPwdProgressBar = (ProgressBar) findViewById(R.id.register_progress);
		resetPwdProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		progressLabelTv = (TextView) findViewById(R.id.register_progress_text);

		mEmailView = (AutoCompleteTextView) findViewById(R.id.email_register_et);
		populateAutoComplete();

		mResetPwdButton = (Button) findViewById(R.id.email_sign_up_button);
		mResetPwdButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (attemptRegister())
					showProgress(false);
				else
					showProgress(true);
			}
		});

		dataManager = (DataManager) getApplication();
		dataManager.trackLiquidEvent(RESET, ENTER);
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
		if (mResetPwdTask != null) {
			return true;
		}

		// Reset errors.
		mEmailView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();

		boolean cancel = false;
		View focusView = null;

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

		if (cancel) {
			focusView.requestFocus();
		} else {
			mResetPwdTask = new ResetPasswordTask();
			mResetPwdTask.execute((Void) null);
		}

		return cancel;
	}

	private boolean isEmailValid(String email) {
		return email.contains("@");
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

			progressLabelTv.setVisibility(show ? View.VISIBLE : View.GONE);
			resetPwdProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			resetPwdProgressBar.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					resetPwdProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mResetPwdButton.setVisibility(show ? View.GONE : View.VISIBLE);
		} else {
			resetPwdProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
				new ArrayAdapter<>(ResetPasswordActivity.this,
						android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

		mEmailView.setAdapter(adapter);
	}

	/**
	 * Represents an asynchronous registration task used to authenticate
	 * the user.
	 */
	private class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			dataManager.resetPassword(ResetPasswordActivity.this, mEmailView.getText().toString());

			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mResetPwdTask = null;
		}

		@Override
		protected void onCancelled() {
			mResetPwdTask = null;
		}
	}
}



