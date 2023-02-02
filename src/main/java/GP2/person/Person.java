package GP2.person;

import java.util.EnumMap;

public class Person extends Object {
	public static enum AccountEntry {
		FROM,
		TO,
		TRANSACTION,
		OWE_OWED,
		checksumTRANSACTION,
		SPENT,
		PAID,
		checksumINDIVIDUAL;

		public static final int size;
		static { size = values().length; }
	}

	// members
	public String	m_name;
	public EnumMap<AccountEntry, Float> m_amount = new EnumMap<AccountEntry, Float>(AccountEntry.class);
	public boolean	m_active;

	// methods
	private void initAmounts() {
		for (AccountEntry ae : AccountEntry.values()) {
			m_amount.put(ae, 0.0f) ;
		}
    }

	public Person() {
		m_name = "" ;
		initAmounts() ;
		m_active = false ;
	}

	public Person(String name, boolean active) {
		m_name = name ;
		initAmounts() ;
		m_active = active ;
	}

	public Float incAmount(AccountEntry ae, float f) {
		return m_amount.put( ae, (f + m_amount.get(ae)) ) ;
	}

	public Float decAmount(AccountEntry ae, float f) {
		return m_amount.put( ae, (f - m_amount.get(ae)) ) ;
	}

	public String toString() {
		String sAmtsE = "" ;
		for (AccountEntry ae : AccountEntry.values()) {
			sAmtsE += (ae.ordinal() + "::" + ae.name() + "::" + m_amount.get(ae) + "\n") ;
		}
		return m_name + "," + sAmtsE + m_active ;
	}
} // end of class
