package pt.sights.data;

/**
 * Contact Item data model class.
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	25th of March of 2015
 */
public class ContactItemData {

	public enum Type { PHONE, EMAIL, WEBSITE, FACEBOOK }

	public final Type type;
	private final String title;
	private int imageUrl;

	/**
	 * Constructor for Contact Item data model.
	 * @param type Contact type.
	 * @param title Contact value.
	 */
	public ContactItemData(Type type, String title) {
		this.type = type;
		this.title = title;
	}

	public String getTitle() { return this.title; }
	public int getImageUrl() { return this.imageUrl; }
	public void setImageUrl(int imageUrl) { this.imageUrl = imageUrl; }

}
