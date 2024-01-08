package GP2.account;

public class mint extends source {
	private String Date;
	private String Category;
	private String Vendor;
	private String Description;
	private String Amount;
	private String Transaction_Type;
	private String Account_Name;
	private String From;
	private String To;
	private String Group;
	private String Action;

	public mint() {
		// no argument constructor required by Jackson
	}

	public mint(String Date, String Category, String Vendor, String Description, String Amount, String Transaction_Type, String Account_Name, String From, String To, String Group, String Action) {
		this.Date = Date;
		this.Category = Category;
		this.Vendor = Vendor;
		this.Description = Description;
		this.Amount = Amount;
		this.Transaction_Type = Transaction_Type;
		this.Account_Name = Account_Name;
		this.From = From;
		this.To = To;
		this.Group = Group;
		this.Action = Action;
	}

	public String getDate() {
		return Date;
	}
	public String getCategory() {
		return Category;
	}
	public String getVendor() {
		return Vendor;
	}
	public String getDescription() {
		return Description;
	}
	public String getAmount() {
		return Amount;
	}
	public String getTransaction_Type() {
		return Transaction_Type;
	}
	public String getAccount_Name() {
		return Account_Name;
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
	public void setDate(String Date) {
		this.Date = Date;
	}
	public void setCategory(String Category) {
		this.Category = Category;
	}
	public void setVendor(String Vendor) {
		this.Vendor = Vendor;
	}
	public void setDescription(String Description) {
		this.Description = Description;
	}
	public void setAmount(String Amount) {
		this.Amount = Amount ;
	}
	public void setTransaction_Type(String Transaction_Type) {
		this.Transaction_Type = Transaction_Type ;
	}
	public void setAccount_Name(String Account_Name) {
		this.Account_Name = Account_Name;
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
		return "mint [date=" + this.Date + ", category=" + this.Category  + ", vendor=" + this.Vendor + ", description=" + this.Description + ", amount=" + this.Amount + ", transaction_type=" + this.Transaction_Type + ", account_name=" + this.Account_Name + ", from=" + this.From + ", to=" + this.To + ", group=" + this.Group + ", action=" + this.Action + "]";
	}
}
