package pt.sights.data;

/**
 * Created by valternepomuceno on 25/03/15.
 */
public class ContactItemData {

	public enum Type { PHONE, EMAIL, WEBSITE, FACEBOOK }

	public final Type type;
	private final String title;
	private int imageUrl;

	public ContactItemData(Type type, String title) {
		this.type = type;
		this.title = title;
	}

	public String getTitle() { return this.title; }
	public int getImageUrl() { return this.imageUrl; }
	public void setImageUrl(int imageUrl) { this.imageUrl = imageUrl; }

}
