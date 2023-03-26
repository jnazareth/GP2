package GP2.person;

import java.util.EnumMap;

import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;

public class Person extends Object {
	public static enum AccountEntry {
		FROM,
		TO,
		TRANSACTION,
		OWE_OWED,
		checksumTRANSACTION,
		SPENT,
		PAID,
		checksumGROUPTOTALS,
		checksumINDIVIDUALTOTALS;

		public static final int size;
		static { size = values().length; }
	}

	// members
	public String	m_name;
	public EnumMap<AccountEntry, Float> m_amount = new EnumMap<AccountEntry, Float>(AccountEntry.class);
	//public boolean	m_active;
	private EntryType m_gType ;
	public PGState m_gState ; 

	// methods
	private void initAmounts() {
		for (AccountEntry ae : AccountEntry.values()) {
			m_amount.put(ae, 0.0f) ;
		}
    }

	public Person() {
		m_name = "" ;
		initAmounts() ;
		//m_active = false ;

		m_gType = EntryType.Self ;
		m_gState = new PGState(EntryState.Disable) ;
	}

	public Person(String name, boolean active) {
		m_name = name ;
		initAmounts() ;
		//m_active = active ;

		m_gType = EntryType.Self ;
		if (active) m_gState = new PGState(EntryState.Enable) ;
		else m_gState = new PGState(EntryState.Disable);
	}

	public Person(String name, EntryState es) {
		m_name = name ;
		initAmounts() ;

		m_gType = EntryType.Self ;
		m_gState = new PGState(es);
	}

	public Float incAmount(AccountEntry ae, float f) {
		return m_amount.put( ae, (f + m_amount.get(ae)) ) ;
	}

	public Float decAmount(AccountEntry ae, float f) {
		return m_amount.put( ae, (f - m_amount.get(ae)) ) ;
	}

	public boolean isActive() {
		return m_gState.getActive();
	}

	public String toString() {
		String sAmtsE = "" ;
		for (AccountEntry ae : AccountEntry.values()) {
			sAmtsE += (ae.ordinal() + "::" + ae.name() + "::" + m_amount.get(ae) + "\n") ;
		}
		return m_name + "," + sAmtsE;// + m_active ;
	}
} // end of class
