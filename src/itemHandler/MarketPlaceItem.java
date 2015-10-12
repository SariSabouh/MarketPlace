package itemHandler;

import org.joda.time.DateTime;

public abstract class MarketPlaceItem {

	private DateTime purchaseDate, usedBy, expiredOn;
	int id;
	
	public DateTime getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(DateTime purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
	public DateTime getUsedBy() {
		return usedBy;
	}
	public void setUsedBy(DateTime usedBy) {
		this.usedBy = usedBy;
	}
	public DateTime getExpiredOn() {
		return expiredOn;
	}
	public void setExpiredOn(DateTime expiredOn) {
		this.expiredOn = expiredOn;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
