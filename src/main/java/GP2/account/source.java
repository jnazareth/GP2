package GP2.account;

public abstract class source {
	protected String Date;
	protected String Category;
	private String Description;
	protected String Amount;
	private String From;
	private String To;
	private String Group;
	private String Action;

	public String getDate() {
		return Date;
	}
	public String getCategory() {
		return Category;
	}
	public String getDescription() {
		return Description;
	}
	public String getAmount() {
		return Amount;
	}
	public String getFrom() {
		return From;
	}
	public String getTo() {
		return To;
	}
	public String getGroup() {
		return Group;
	}
	public String getAction() {
		return Action;
	}
	public void setDate(String date) {
		this.Date = Date;
	}
	public void setCategory(String category) {
		this.Category = Category;
	}
	public void setDescription(String Description) {
		this.Description = Description;
	}
	public void setAmount(String amount) {
		this.Amount = Amount;
	}
	public void setFrom(String From) {
		this.From = From;
	}
	public void setTo(String To) {
		this.To = To;
	}
	public void setGroup(String Group) {
		this.Group = Group;
	}
	public void setAction(String Action) {
		this.Action = Action;
	}
	@Override public String toString() {
		return "source [date=" + Date+ ", category=" + Category + "description=" + Description + ", amount=" + Amount + "]";
	}
}
