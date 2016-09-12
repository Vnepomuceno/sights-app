package pt.sights.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import pt.sights.R;
import pt.sights.activities.MainActivity;
import pt.sights.data.DataManager;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	20th of February of 2015
 */
public class DrawerLayoutAdapter extends RecyclerView.Adapter<DrawerLayoutAdapter.ViewHolder> {

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;

	private final String mNavTitles[];
	private final int mIcons[];

	private LinearLayout selLinearLayout;
	private TextView selTextView;
	private ImageView selImageView;
	private int selPos;
	private static int holderId = 1;

	private final Context context;

	public DrawerLayoutAdapter(String[] titles, int[] icons, Context context, DataManager dataManager) {
		mNavTitles = titles;
		mIcons = icons;
		this.context = context;
		selPos = 1;
	}

	@Override
	public DrawerLayoutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final RecyclerView recyclerView = (RecyclerView)LayoutInflater.from(parent.getContext())
				.inflate(R.layout.activity_main, parent, false).findViewById(R.id.drawer_layout_recycler_view);

		if (viewType == TYPE_ITEM) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_layout_row, parent, false);
			ViewHolder viewHolderItem = new ViewHolder(view, viewType);

			final LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.drawer_layout_row);
			final TextView textView = (TextView)view.findViewById(R.id.drawer_layout_row_text);
			final ImageView imageView = (ImageView)view.findViewById(R.id.drawer_layout_row_icon);

			if (holderId == 1)
				setStyleSelectedDrawerRow(view, recyclerView, linearLayout, textView, imageView);

			linearLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setStyleSelectedDrawerRow(v, recyclerView, linearLayout, textView, imageView);
					((MainActivity)context).selectItem(recyclerView.getChildPosition(v)-1);
				}
			});

			holderId++;

			return viewHolderItem;

		} else if (viewType == TYPE_HEADER) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_layout_header, parent, false);
			ViewHolder viewHolderHeader = new ViewHolder(view, viewType);

			ParseUser currentUser = ParseUser.getCurrentUser();
			TextView usernameTv = (TextView)view.findViewById(R.id.drawer_username);
			TextView emailTv = (TextView)view.findViewById(R.id.drawer_email);

			if (currentUser != null) {
				usernameTv.setText("@" + currentUser.getUsername());
				emailTv.setText(currentUser.getEmail());
			}

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selPos = 2;
					((MainActivity)context).selectItem(selPos);
				}
			});

			return viewHolderHeader;
		}

		return null;
	}

	private void setStyleSelectedDrawerRow(View v, RecyclerView rv, LinearLayout ll, TextView tv, ImageView iv) {
		if (selLinearLayout != null && selTextView != null && selImageView != null) {
			selLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
			selTextView.setTextColor(context.getResources().getColor(R.color.grey_500));
			switch (selPos) {
				case 1: if (holderId != 1) selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_explore)); break;
				case 2: selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_map)); break;
				case 3: selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_profile)); break;
				case 4: selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_feedback)); break;
				case 5: selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_rate)); break;
				case 6: selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_about)); break;
				case 7: selImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_logout)); break;
			}
		}

		selectDrawerLayoutRow(v, rv, ll, tv, iv);

		selLinearLayout = ll;
		selTextView = tv;
		selImageView = iv;
		selPos = rv.getChildPosition(v);
	}

	private void selectDrawerLayoutRow(View v, RecyclerView rv, LinearLayout ll, TextView tv, ImageView iv) {
		ll.setBackgroundColor(context.getResources().getColor(R.color.teal_50));
		tv.setTextColor(context.getResources().getColor(R.color.teal_500));

		if (holderId == 1)
			iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_explore_sel));

		switch (rv.getChildPosition(v)) {
			case 1: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_explore_sel)); break;
			case 2: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_map_sel)); break;
			case 3: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_profile_sel)); break;
			case 4: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_feedback_sel)); break;
			case 5: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_rate_sel)); break;
			case 6: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_about_sel)); break;
			case 7: iv.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_layout_logout_sel)); break;
		}
	}

	@Override
	public void onBindViewHolder(DrawerLayoutAdapter.ViewHolder holder, int position) {
		if (holder.holderId == 1) {
			holder.textView.setText(mNavTitles[position - 1]);
			holder.imageView.setImageResource(mIcons[position - 1]);
		}
	}

	@Override
	public int getItemCount() {
		return mNavTitles.length + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if (isPositionHeader(position))
			return TYPE_HEADER;

		return TYPE_ITEM;
	}

	private boolean isPositionHeader(int position) {
		return position == 0;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final int holderId;

		TextView textView;
		ImageView imageView;

		public ViewHolder(View itemView, int viewType) {
			super(itemView);

			if (viewType == TYPE_ITEM) {
				textView = (TextView) itemView.findViewById(R.id.drawer_layout_row_text);
				imageView = (ImageView) itemView.findViewById(R.id.drawer_layout_row_icon);
				holderId = 1;
			} else {
				holderId = 0;
			}
		}
	}
}
