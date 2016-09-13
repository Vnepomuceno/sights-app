package pt.sights.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pt.sights.R;
import pt.sights.data.ContactItemData;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	25th of March of 2015
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder>
	implements View.OnClickListener{

	private final Context context;
	private final ContactItemData[] itemsData;
	private final RecyclerView recyclerView;

	/**
	 *
	 * @param context
	 * @param itemsData
	 * @param recyclerView
	 */
	public ContactAdapter(Context context, ContactItemData[] itemsData, RecyclerView recyclerView) {
		this.context = context;
		this.itemsData = itemsData;
		this.recyclerView = recyclerView;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemLayoutView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_contact_rv, parent, false);
		itemLayoutView.setOnClickListener(this);

		return new ViewHolder(itemLayoutView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.titleTv.setText(itemsData[position].getTitle());
		holder.imageIv.setImageResource(itemsData[position].getImageUrl());
	}

	@Override
	public int getItemCount() {
		return itemsData.length;
	}

	@Override
	public void onClick(View v) {
		int itemPos = recyclerView.getChildPosition(v);
		switch (itemsData[itemPos].type) {

			case PHONE:
				Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
				phoneIntent.setData(Uri.parse("tel:" + itemsData[itemPos].getTitle()));
				context.startActivity(phoneIntent);
				break;

			case EMAIL:
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
				emailIntent.setType("text/plain");
				emailIntent.setData(Uri.parse("mailto:" + itemsData[itemPos].getTitle()));
				context.startActivity(emailIntent);
				break;

			case WEBSITE:
				Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
				String url = itemsData[itemPos].getTitle();
				url = url.startsWith("http://") ? url : "http://" + url;
				websiteIntent.setData(Uri.parse(url));
				context.startActivity(websiteIntent);
				break;

			case FACEBOOK:
				break;

		}
	}

	/**
	 *
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {

		public final TextView titleTv;
		public final ImageView imageIv;

		public ViewHolder(View itemView) {
			super(itemView);

			titleTv = (TextView) itemView.findViewById(R.id.item_contact_title);
			imageIv = (ImageView) itemView.findViewById(R.id.item_contact_icon);
		}
	}

}
