package pt.sights.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.sights.R;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	15th of November of 2014
 */
public class HistoryFragment extends Fragment {

	public HistoryFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_history, container, false);
	}

}
