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

        public static final int SIZE = values().length;
	}

	// members
	public String	m_name;
	public EnumMap<AccountEntry, Float> m_amount = new EnumMap<AccountEntry, Float>(AccountEntry.class);
	@SuppressWarnings("unused")
	private EntryType m_groupType ;
	public PGState m_groupState ;

    // Constructors
    public Person() {
        this("", false);
    }

    public Person(String name, boolean active) {
        this(name, active ? EntryState.Enable : EntryState.Disable);
    }

    public Person(String name, EntryState entryState) {
        this.m_name = name;
        initializeAmounts();
        this.m_groupType = EntryType.Self;
        this.m_groupState = new PGState(entryState);
    }

    // Initialize all account entries to zero
    private void initializeAmounts() {
        for (AccountEntry entry : AccountEntry.values()) {
            m_amount.put(entry, 0.0f);
        }
    }

    public Float incrementAmount(AccountEntry entry, float value) {
		return m_amount.put( entry, (value + m_amount.get(entry)) ) ;
	}

    public Float decrementAmount(AccountEntry entry, float value) {
		return m_amount.put( entry, (value - m_amount.get(entry)) ) ;
	}

	public boolean isActive() {
		return m_groupState.isActive();
	}

    // Override toString to provide a detailed string representation
    @Override
    public String toString() {
        StringBuilder amountsString = new StringBuilder();
        for (AccountEntry entry : AccountEntry.values()) {
            amountsString.append(entry.ordinal())
                         .append("::")
                         .append(entry.name())
                         .append("::")
                         .append(m_amount.get(entry))
                         .append("\n");
        }
        return m_name + "," + amountsString.toString();
    }
} // end of class
