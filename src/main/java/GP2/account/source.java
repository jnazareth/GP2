package GP2.account;

public abstract class source {
	protected String Date;
	protected String Category;
	protected String SubCategory;
	protected String Description;
	protected String Amount;
	protected String From;
	protected String To;
	protected String Group;
	protected String Action;

	public String getDate() {
		return Date;
	}
	public String getCategory() {
		return Category;
	}
	public String getSubCategory() {
		return SubCategory;
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
		this.Date = date;
	}
	public void setCategory(String category) {
		this.Category = category;
	}
	public void setSubCategory(String sub_category) {
		this.SubCategory = sub_category;
	}
	public void setDescription(String description) {
		this.Description = description;
	}
	public void setAmount(String amount) {
		this.Amount = amount;
	}
	public void setFrom(String from) {
		this.From = from;
	}
	public void setTo(String to) {
		this.To = to;
	}
	public void setGroup(String group) {
		this.Group = group;
	}
	public void setAction(String action) {
		this.Action = action;
	}
	@Override public String toString() {
return "source [date=" + Date + ", category=" + Category + ", subcategory=" + SubCategory + ", description=" + Description + ", amount=" + Amount + "]";
	}
}
