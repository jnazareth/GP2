package GP2.account;

import GP2.utils.Utils;

public class ffv2 extends source {
	private String Date;
	private String Category;
	private String SubCategory;
	private String Description;
	private String Account;
	private String TransactionType;
	private String Is_Hidden;
	private String Amount;
	private String From;
	private String To;
	private String Group;
	private String Action;

	public ffv2() {
		// no argument constructor required by Jackson
	}

	public ffv2(String Date, String Category, String Sub_Category, String Description, String Account, String Is_Hidden, String Amount, String TransactionType, String From, String To, String Group, String Action) {
		this.Date = Date;
		this.Category = Category;
		this.SubCategory = Sub_Category;
		this.Description = Description;
		this.Account = Account;
		this.TransactionType = TransactionType ;
		this.Is_Hidden = Is_Hidden;
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
	public String getSubCategory() {
		return SubCategory;
	}
	public String getDescription() {
		return Description;
	}
	public String getAccount() {
		return Account;
	}
	public String getTransactionType() {
		return TransactionType ;
	}
	public String getIs_Hidden() {
		return Is_Hidden;
	}
	public String getAmount() {
		return Utils.flipCreditDebit(Amount);
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
	public void setSubCategory(String Sub_Category) {
		this.SubCategory = Sub_Category;
	}
	public void setDescription(String Description) {
		this.Description = Description;
	}
	public void setAccount(String Account) {
		this.Account = Account;
	}
	public void setTransactionType(String TransactionType) {
		this.TransactionType = TransactionType;
	}
	public void setIs_Hidden(String Is_Hidden) {
		this.Is_Hidden = Is_Hidden;
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
		return "ffv2 [date=" + this.Date + ", category=" + this.Category + ", subcategory=" + this.SubCategory + ", description=" + this.Description + ", account=" + this.Account + ", transactiontype=" + this.TransactionType + ", is_hidden=" + this.Is_Hidden + ", amount=" + this.Amount  + ", from=" + this.From + ", to=" + this.To + ", group=" + this.Group + ", action=" + this.Action + "]";
	}
}
