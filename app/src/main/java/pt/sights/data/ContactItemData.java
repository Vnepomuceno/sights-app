package pt.sights.data;

/**
 *
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
	 *
	 * @param type
	 * @param title
	 */
	public ContactItemData(Type type, String title) {
		this.type = type;
		this.title = title;
	}

	/**
	 *
	 * @return
	 */
	public String getTitle() { return this.title; }

	/**
	 *
	 * @return
	 */
	public int getImageUrl() { return this.imageUrl; }

	/**
	 *
	 * @param imageUrl
	 */
	public void setImageUrl(int imageUrl) { this.imageUrl = imageUrl; }

}
