package GP2.account;

public class ffv  extends source {
	private String Date;
	private String Category;
	private String Description;
	private String Institution;
	private String Account;
	private String Is_Hidden;
	private String Is_Pending;
	private String Amount;
	private String From;
	private String To;
	private String Group;
	private String Action;

	public ffv() {
		// no argument constructor required by Jackson
	}

	public ffv(String Date, String Category, String Description, String Institution, String Account, String Is_Hidden, String Is_Pending, String Amount, String From, String To, String Group, String Action) {
		this.Date = Date;
		this.Category = Category;
		this.Description = Description;
		this.Institution = Institution;
		this.Account = Account;
		this.Is_Hidden = Is_Hidden;
		this.Is_Pending = Is_Pending;
		this.Amount = Amount;
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
		return "";
	}
	public String getDescription() {
		return Description;
	}
	public String getInstitution() {
		return Institution;
	}
	public String getAccount() {
		return Account;
	}
	public String getIs_Hidden() {
		return Is_Hidden;
	}
	public String getIs_Pending() {
		return Is_Pending;
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
	public void setDate(String Date) {
		this.Date = Date;
	}
	public void setCategory(String Category) {
		this.Category = Category;
	}
	public void setVendor(String Vendor) {
        // do nothing
    }
    public void setDescription(String Description) {
		this.Description = Description;
	}
	public void setInstitution(String Institution) {
		this.Institution = Institution ;
	}
	public void setAccount(String Account) {
		this.Account = Account;
	}
	public void setIs_Hidden(String Is_Hidden) {
		this.Is_Hidden = Is_Hidden;
	}
	public void setIs_Pending(String Is_Pending) {
		this.Is_Pending = Is_Pending;
	}
	public void setAmount(String Amount) {
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
		return "ffv [date=" + this.Date + ", category=" + this.Category + ", description=" + this.Description + ", institution=" + this.Institution + ", account=" + this.Account + ", is_hidden=" + this.Is_Hidden + ", is_pending=" + this.Is_Pending + ", amount=" + this.Amount  + ", from=" + this.From + ", to=" + this.To + ", group=" + this.Group + ", action=" + this.Action + "]";
	}
}
