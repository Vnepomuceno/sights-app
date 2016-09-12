package pt.sights.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pt.sights.R;
import pt.sights.activities.SightDetailActivity;
import pt.sights.data.DataManager;

import java.util.List;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	21th of December of 2014
 */
public class SightCardAdapter extends RecyclerView.Adapter<SightCardAdapter.SightViewHolder>
	implements View.OnClickListener {

	private final DataManager dataManager;

	private final List<SightCard> sights;
	private final Context context;
	private final RecyclerView recyclerView;

	private final int displayHeight;

	public SightCardAdapter(List<SightCard> sights, Context context, RecyclerView recyclerView, int displayHeight) {
		this.sights = sights;
		this.context = context;
		this.recyclerView = recyclerView;
		this.dataManager = (DataManager) context.getApplicationContext();
		this.displayHeight = displayHeight;
	}

	@Override
	public SightViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.sight_card_list, parent, false);
		itemView.setOnClickListener(this);

		return new SightViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(SightViewHolder holder, int position) {
		SightCard si = sights.get(position);
		holder.name.setText(si.name);
		holder.cardImage.setImageBitmap(si.image);
		holder.cardImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
		holder.sightCardLayout.getLayoutParams().height = (int)(displayHeight*0.3);
	}

	@Override
	public int getItemCount() {
		return sights == null ? 0 : sights.size();
	}

	@Override
	public void onClick(View v) {
		int itemPosition = recyclerView.getChildPosition(v);
		dataManager.setSightDetailPos(itemPosition);

		Intent intent = new Intent(context, SightDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static class SightViewHolder extends RecyclerView.ViewHolder {

		public final TextView name;
		public final ImageView cardImage;
		public final RelativeLayout sightCardLayout;

		public SightViewHolder(View itemView) {
			super(itemView);
			name = (TextView) itemView.findViewById(R.id.sight_name);
			cardImage = (ImageView) itemView.findViewById(R.id.sight_card_image);
			sightCardLayout = (RelativeLayout) itemView.findViewById(R.id.sight_card_layout);
		}

	}
}
