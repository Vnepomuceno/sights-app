package pt.sights.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import pt.sights.R;
import pt.sights.data.DataManager;

import static pt.sights.data.DataManager.LiquidActivityType.FEEDBACK;
import static pt.sights.data.DataManager.LiquidEventType.ENTER;

public class FeedbackFragment extends Fragment {

	private DataManager dataManager;

	private EditText feedbackEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dataManager = (DataManager) getActivity().getApplication();

		dataManager.trackLiquidEvent(FEEDBACK, ENTER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);

		final Context context = getActivity().getApplicationContext();

		feedbackEditText = (EditText) rootView.findViewById(R.id.feedback_edit_text);
		Button sendFeedbackBtn = (Button) rootView.findViewById(R.id.send_feedback_btn);
		sendFeedbackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DataManager.suggestedFeedback = feedbackEditText.getText().toString();
				dataManager.submitFeedback(DataManager.suggestedFeedback);

				Intent thankYouIntent = new Intent(context, ThankYouActivity.class);
				startActivity(thankYouIntent);
			}
		});

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_sight_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
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
