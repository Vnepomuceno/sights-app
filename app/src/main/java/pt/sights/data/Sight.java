package pt.sights.data;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import citysdk.tourism.client.poi.base.POITermType;
import citysdk.tourism.client.poi.single.PointOfInterest;

/**
 * Sights data model class.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	3rd of December of 2014
 */
public class Sight implements Comparable<Sight> {

	private static int idCounter = 0;
	private final int id;

	private final PointOfInterest poi;
	private final List<Bitmap> images;
	private Bitmap staticMap;

	public final String name;
	private String email;
	private String phone;
	private String website;
	private String facebook;
	private String address;
	private String distance;

	private float rating;
	private int numReviews;

	private ParseObject checkinParseObj, favouriteParseObj, ratingParseObj;
	private int numFavourites = -1;
	private boolean favourited = false, rated = false;

	/**
	 * Constructor for Sight class.
	 * @param poi CitySDK Point Of Interest data model.
	 */
	public Sight(PointOfInterest poi) {
		this.poi = poi;
		this.images = new ArrayList<>();
		this.name = poi.getLabel().get(0).getValue();

		scrapSightData();
		id = idCounter++;
	}

	@Override
	public int compareTo(@NonNull Sight another) {
		return this.name.compareTo(another.name);
	}

	/**
	 * Scraps and stores Address, Phone, Email, Website and Facebook from CitySDK response.
	 */
	private void scrapSightData() {
		for (int i = 0; i < poi.getLink().size(); i++) {
			POITermType poiLink = poi.getLink().get(i);

			if (poiLink.getType().startsWith("text")) {
				if (poiLink.getHref().contains("facebook")) {
					facebook = poiLink.getHref();
				} else if (poiLink.getHref().contains("@")) {
					email = poiLink.getHref();
				} else {
					website = poiLink.getHref();
				}
			}
		}

		String vCard = poi.getLocation().getAddress().getValue();
		String[] vCardSplit = vCard.split("\r\n");
		for (String cardSplit : vCardSplit) {
			if (cardSplit.startsWith("ADR;WORK")) {
				String[] addressSplit = cardSplit.split(";");
				for (String split : addressSplit) {
					if (!split.equals("") && !split.startsWith("ADR") && !split.startsWith("WORK")) {
						if (split.contains("\n")) {
							address = split.split("\n")[0];
							break;
						} else {
							address = split;
							break;
						}
					}
				}
			} else if (cardSplit.startsWith("TEL;WORK:")) {
				phone = cardSplit.substring("TEL;WORK:".length());
			} else if (cardSplit.startsWith("EMAIL;INTERNET:")) {
				email = cardSplit.substring("EMAIL;INTERNET:".length());
			}
		}
	}

	/**
	 * Calculates the number of image links in the Point Of Interest.
	 * @return Number of images.
	 */
	public int getNumberOfImages() {
		int n = 0;

		for (int i = 0; i < poi.getLink().size(); i++)
			if (poi.getLink().get(i).getType().startsWith("image"))
				n++;

		return n;
	}

	public String[] getCoordinates() {
		if (poi.getLocation().getPoint() != null && poi.getLocation().getPoint().size() > 0)
			return poi.getLocation().getPoint().get(0).getPoint().getPosList().split(" ");
		else
			return null;
	}
	public Bitmap getCoverImage() {
		if (images != null && images.size() > 0)
			return images.get(0);
		else
			return null;
	}
	public int id() { return id; }
	public String getEmail() { return email == null ? "" : email; }
	public String getWebsite() { return website == null ? "" : website; }
	public String getFacebook() { return facebook == null ? "" : facebook; }
	public String getPhone() { return phone == null ? "" : phone; }
	public String getAddress() { return address == null ? "" : address; }
	public ParseObject getFavourite() { return favouriteParseObj; }
	public void setFavourite(ParseObject parseObject) { favouriteParseObj = parseObject; }
	public ParseObject getCheckin() { return checkinParseObj; }
	public void setCheckin(ParseObject parseObject) { checkinParseObj = parseObject; }
	public void addImage(Bitmap bitmap) { images.add(bitmap); }
	public PointOfInterest getPoi() { return this.poi; }
	public List<Bitmap> getImages() { return this.images; }
	public Bitmap getStaticMapBitmap() { return this.staticMap; }
	public void setStaticMapBitmap(Bitmap staticMap) { this.staticMap = staticMap; }
	public float getRating() { return this.rating; }
	public ParseObject getRatingParseObj() { return this.ratingParseObj; }
	public void setRating(float rating) { this.rating = rating; }
	public void setRating(ParseObject parseObject) { this.ratingParseObj = parseObject; }
	public int getNumReviews() { return this.numReviews; }
	public void setNumReviews(int numReviews) { this.numReviews = numReviews; }
	public int getNumFavourites() { return this.numFavourites; }
	public void setNumFavourites(int n) { this.numFavourites = n; }
	public void swapFavourited() { this.favourited = !this.favourited; }
	public void updateFavourited() { this.favourited = this.favouriteParseObj != null; }
	public void updateRated() { this.rated = this.ratingParseObj != null; }
	public void updateRated(boolean bool) { this.rated = bool; }
	public boolean isFavourited() { return this.favourited; }
	public boolean isRated() { return this.rated; }
	public boolean hasCheckedIn() { return this.checkinParseObj != null; }
	public void setDistance(String distance) { this.distance = distance; }
	public String getDistance() { return this.distance; }
}
