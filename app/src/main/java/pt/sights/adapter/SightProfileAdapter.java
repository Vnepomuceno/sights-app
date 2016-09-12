package pt.sights.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pt.sights.R;
import pt.sights.activities.SightDetailActivity;
import pt.sights.data.DataManager;

import java.util.List;

/**
 * Created by valternepomuceno on 06/06/15.
 */
public class SightProfileAdapter extends RecyclerView.Adapter<SightProfileAdapter.SightViewHolder>
	implements View.OnClickListener {

	private final DataManager dataManager;

	private final List<SightProfile> sights;
	private final Context context;
	private final RecyclerView recyclerView;
	private int cardType;

	public SightProfileAdapter(List<SightProfile> sights, Context context, RecyclerView recyclerView,
	                           int cardType) {
		this.dataManager = (DataManager) context.getApplicationContext();

		this.sights = sights;
		this.context = context;
		this.recyclerView = recyclerView;
		this.cardType = cardType;
	}

	@Override
	public SightViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.profile_sight_item, parent, false);
		itemView.setOnClickListener(this);

		return new SightViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(SightViewHolder holder, int position) {
		SightProfile sp = sights.get(position);

		holder.id = sp.id;
		holder.name.setText(sp.name);
		holder.sightPhoto.setImageBitmap(sp.image);
		holder.sightPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

		if (cardType == 1) {
			holder.eventDescription.setText(sp.eventDate);
		} else if (cardType == 2) {
			holder.eventDate.setText(sp.eventDate);
			holder.eventDescription.setText(sp.rating);
		}
	}

	@Override
	public int getItemCount() {
		return sights == null ? 0 : sights.size();
	}

	@Override
	public void onClick(View v) {
		String sightName = sights.get(recyclerView.getChildPosition(v)).name;
		int itemPosition = dataManager.getSightPositionByName(sightName);

		dataManager.setSightDetailPos(itemPosition);

		Intent intent = new Intent(context, SightDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public class SightViewHolder extends RecyclerView.ViewHolder {

		public int id;
		public final ImageView sightPhoto;
		public final ImageView eventIcon, dateIcon;
		public final TextView name;
		public final TextView eventDate;
		public final TextView eventDescription;

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		public SightViewHolder(View itemView) {
			super(itemView);

			sightPhoto = (ImageView) itemView.findViewById(R.id.profile_sight_item_iv);
			name = (TextView) itemView.findViewById(R.id.profile_sight_item_title);
			eventDate = (TextView) itemView.findViewById(R.id.profile_sight_item_date);
			eventDescription = (TextView) itemView.findViewById(R.id.profile_sight_item_checkins);
			eventIcon = (ImageView) itemView.findViewById(R.id.profile_card_event_icon);
			dateIcon = (ImageView) itemView.findViewById(R.id.profile_card_date_icon);

			if (cardType == 1) {
				eventDate.setVisibility(View.GONE);
				dateIcon.setVisibility(View.GONE);
				eventIcon.setBackground(context.getDrawable(R.drawable.sight_action_favorite));
			} else if (cardType == 2) {
				eventDate.setVisibility(View.VISIBLE);
				dateIcon.setVisibility(View.VISIBLE);
				eventIcon.setBackground(context.getDrawable(R.drawable.sight_action_rate));
			}
		}
	}
}
