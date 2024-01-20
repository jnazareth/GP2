package GP2.transaction;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.Person;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public abstract class amountAllocationsBase {
	String                  m_groupName ;
    TransactionDirection    m_tDirection ;
    FTType.FromToType       m_keyAllocation;
	TransactionType.TType	m_tType;

    protected void putIndivAmount(String sGroupName, float fAmount, HashSet<String> indiv) {
		try {
			Hashtable<String, Person> aGroup = Utils.m_Groups.get(sGroupName).getCollection() ;
			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = aGroup.get(keysPeople.nextElement());
				if ( !(indiv.contains(person.m_name)) ) continue ;
                if ((m_tDirection.compareTo(TransactionDirection.FROM) == 0)) person.incAmount(Person.AccountEntry.FROM, fAmount) ;
                if ((m_tDirection.compareTo(TransactionDirection.TO) == 0)) person.incAmount(Person.AccountEntry.TO, fAmount) ;
				if (((m_tDirection.compareTo(TransactionDirection.FROM) == 0)) && ((m_tType.compareTo(TransactionType.TType.Clearing) != 0))) {
                    person.incAmount(Person.AccountEntry.PAID, fAmount) ;
				}
				if (((m_tDirection.compareTo(TransactionDirection.TO) == 0)) && ((m_tType.compareTo(TransactionType.TType.Clearing) != 0))) {
                    person.incAmount(Person.AccountEntry.TRANSACTION, fAmount) ;
                    person.incAmount(Person.AccountEntry.SPENT, fAmount) ;
				}
				// checksumINDIVIDUALTOTALS bug 3/13
				if ((m_tType.compareTo(TransactionType.TType.Clearing) == 0)) {
					if ((m_tDirection.compareTo(TransactionDirection.FROM) == 0)) person.incAmount(Person.AccountEntry.PAID, fAmount) ;
					if ((m_tDirection.compareTo(TransactionDirection.TO) == 0)) person.incAmount(Person.AccountEntry.PAID, -1*fAmount) ;
				}
				aGroup.put(person.m_name, person) ;
			}
		} catch (Exception e) {
			System.err.println("Error:amountAllocationsBase::putIndivAmount::" + e.getMessage()) ;
		}
	}

    protected void process(InputProcessor ip) {  
    }
}
