package pt.sights.data;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import citysdk.tourism.client.poi.base.POITermType;
import citysdk.tourism.client.poi.single.PointOfInterest;

/**
 * Created by valternepomuceno on 03/12/2014.
 */
/**
 *
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
	 *
	 * @param poi
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
	 * Scraps address, phone, email, website and facebook
	 * from the reponse of CitySDK
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
	 * Calculates the number of image links in the PointOfInterest
	 * @return Number of images
	 */
	public int getNumberOfImages() {
		int n = 0;

		for (int i = 0; i < poi.getLink().size(); i++)
			if (poi.getLink().get(i).getType().startsWith("image"))
				n++;

		return n;
	}

	/**
	 *
	 * @return
	 */
	public String[] getCoordinates() {
		if (poi.getLocation().getPoint() != null && poi.getLocation().getPoint().size() > 0)
			return poi.getLocation().getPoint().get(0).getPoint().getPosList().split(" ");
		else
			return null;
	}

	/**
	 *
	 * @return
	 */
	public Bitmap getCoverImage() {
		if (images != null && images.size() > 0)
			return images.get(0);
		else
			return null;
	}

	/**
	 *
	 * @return
	 */
	public int id() { return id; }

	/**
	 *
	 * @return
	 */
	public String getEmail() { return email == null ? "" : email; }

	/**
	 *
	 * @return
	 */
	public String getWebsite() { return website == null ? "" : website; }

	/**
	 *
	 * @return
	 */
	public String getFacebook() { return facebook == null ? "" : facebook; }

	/**
	 *
	 * @return
	 */
	public String getPhone() { return phone == null ? "" : phone; }

	/**
	 *
	 * @return
	 */
	public String getAddress() { return address == null ? "" : address; }

	/**
	 *
	 * @return
	 */
	public ParseObject getFavourite() { return favouriteParseObj; }

	/**
	 *
	 * @param parseObject
	 */
	public void setFavourite(ParseObject parseObject) { favouriteParseObj = parseObject; }

	/**
	 *
	 * @return
	 */
	public ParseObject getCheckin() { return checkinParseObj; }

	/**
	 *
	 * @param parseObject
	 */
	public void setCheckin(ParseObject parseObject) { checkinParseObj = parseObject; }

	/**
	 *
	 * @param bitmap
	 */
	public void addImage(Bitmap bitmap) { images.add(bitmap); }

	/**
	 *
	 * @return
	 */
	public PointOfInterest getPoi() { return this.poi; }

	/**
	 *
	 * @return
	 */
	public List<Bitmap> getImages() { return this.images; }

	/**
	 *
	 * @return
	 */
	public Bitmap getStaticMapBitmap() { return this.staticMap; }

	/**
	 *
	 * @param staticMap
	 */
	public void setStaticMapBitmap(Bitmap staticMap) { this.staticMap = staticMap; }

	/**
	 *
	 * @return
	 */
	public float getRating() { return this.rating; }

	/**
	 *
	 * @return
	 */
	public ParseObject getRatingParseObj() { return this.ratingParseObj; }

	/**
	 *
	 * @param rating
	 */
	public void setRating(float rating) { this.rating = rating; }

	/**
	 *
	 * @param parseObject
	 */
	public void setRating(ParseObject parseObject) { this.ratingParseObj = parseObject; }

	/**
	 *
	 * @return
	 */
	public int getNumReviews() { return this.numReviews; }

	/**
	 *
	 * @param numReviews
	 */
	public void setNumReviews(int numReviews) { this.numReviews = numReviews; }

	/**
	 *
	 * @return
	 */
	public int getNumFavourites() { return this.numFavourites; }

	/**
	 *
	 * @param n
	 */
	public void setNumFavourites(int n) { this.numFavourites = n; }

	/**
	 *
	 */
	public void swapFavourited() { this.favourited = !this.favourited; }

	/**
	 *
	 */
	public void updateFavourited() { this.favourited = this.favouriteParseObj != null; }

	/**
	 *
	 */
	public void updateRated() { this.rated = this.ratingParseObj != null; }

	/**
	 *
	 * @param bool
	 */
	public void updateRated(boolean bool) { this.rated = bool; }

	/**
	 *
	 * @return
	 */
	public boolean isFavourited() { return this.favourited; }

	/**
	 *
	 * @return
	 */
	public boolean isRated() { return this.rated; }

	/**
	 *
	 * @return
	 */
	public boolean hasCheckedIn() { return this.checkinParseObj != null; }

	/**
	 *
	 * @param distance
	 */
	public void setDistance(String distance) { this.distance = distance; }

	/**
	 *
	 * @return
	 */
	public String getDistance() { return this.distance; }
}
