package GP2.transaction;

import java.util.Enumeration;
import java.util.Hashtable;

import GP2.person.GPAction.TransactionType;
import GP2.person.Person;
import GP2.utils.Utils;

public class Transaction {
	public enum TransactionDirection {
        FROM (0),
        TO (1);
    
        private final int value;
        TransactionDirection(int value) {
            this.value = value;
        }
    }
    
    String                  m_groupName;
    TransactionType.TType   m_tType ;
    amountAllocations       fromAllocations ;
    amountAllocations       toAllocations ;

	public Transaction(String gName, String from, String to, String amt, TransactionType.TType tt) {
		m_groupName = gName;
        m_tType = tt;

		float fAmt = 0.0f ;
		try {
			fAmt = Float.parseFloat(amt) ;
		} catch (NumberFormatException e) {
		}

        fromAllocations = new amountAllocations(TransactionDirection.FROM, gName, from, fAmt, tt);
        toAllocations = new amountAllocations(TransactionDirection.TO, gName, to, fAmt, tt);
    }

    public void init() {
		try {
			Hashtable<String, Person> aGroup = Utils.m_Groups.get(m_groupName).getCollection() ;
			Enumeration<String> keysPeople = aGroup.keys();
			while(keysPeople.hasMoreElements()) {
				Person person = aGroup.get(keysPeople.nextElement());
				person.m_amount.put(Person.AccountEntry.FROM, 0.0f);
				person.m_amount.put(Person.AccountEntry.TO, 0.0f);
				person.m_amount.put(Person.AccountEntry.TRANSACTION, 0.0f);
				person.m_amount.put(Person.AccountEntry.checksumTRANSACTION, 0.0f);
				person.m_amount.put(Person.AccountEntry.checksumGROUPTOTALS, 0.0f);
				person.m_amount.put(Person.AccountEntry.checksumINDIVIDUALTOTALS, 0.0f);
				aGroup.put(person.m_name, person) ;
			}
		} catch (Exception e) {
			System.err.println("Error:Transaction::init()::" + e.getMessage()) ;
		}
	}

    public void process() {
		try {
			fromAllocations.process();
			toAllocations.process();
		} catch (Exception e) {
			System.err.println("Error:Transaction::process()::" + e.getMessage()) ;
		}
	}

	private void ComputeCheckSums(TransactionType.TType tType, String sGroupName, Float[] cs) {
		try {
			Hashtable<String, Person> aGroup = Utils.m_Groups.get(sGroupName).getCollection() ;
			Enumeration<String> keysPeople = aGroup.keys();
			Float f = 0.0f, t = 0.0f;			Float s = 0.0f, p = 0.0f;
			Float csT = 0.0f ;					Float csGT = 0.0f ;
			while(keysPeople.hasMoreElements()) {
				Person person = aGroup.get(keysPeople.nextElement());
				if (person.isActive()) {
					f += person.m_amount.get(Person.AccountEntry.FROM) ;
					t += person.m_amount.get(Person.AccountEntry.TO) ;
					if (!(tType.compareTo(TransactionType.TType.Clearing) == 0)) {
						s += person.m_amount.get(Person.AccountEntry.SPENT) ;
						p += person.m_amount.get(Person.AccountEntry.PAID) ;
					}
				}
			}
			csT = (f + ((-1)*t)) ;				csGT = (s + ((-1)*p)) ;
			cs[0] = Utils.truncate(csT).floatValue() ;
			cs[1] = Utils.truncate(csGT).floatValue() ;
		} catch (Exception e) {
			System.err.println("Error:Transaction::ComputeCheckSums()::" + e.getMessage()) ;
		}
	}

    public void sum() {
		try {
			Hashtable<String, Person> aGroup = Utils.m_Groups.get(m_groupName).getCollection() ;
			Enumeration<String> keysPeople = aGroup.keys();
			Float checkSums[] = {0.0f, 0.0f} ;
			while(keysPeople.hasMoreElements()) {
				Person person = aGroup.get(keysPeople.nextElement());
				if (person.isActive()) {
					ComputeCheckSums(m_tType, m_groupName, checkSums) ;
					Float csT = checkSums[0] ;	Float csGT = checkSums[1] ;

					Float f = person.m_amount.get(Person.AccountEntry.FROM) ;
					Float t = person.m_amount.get(Person.AccountEntry.TO) ;
					Float transactionSum = (f + ((-1)*t)) ;
					person.incAmount(Person.AccountEntry.OWE_OWED, transactionSum) ;

					Float i = person.m_amount.get(Person.AccountEntry.OWE_OWED) + person.m_amount.get(Person.AccountEntry.SPENT);
					Float o = person.m_amount.get(Person.AccountEntry.PAID);
					Float csIT = (i + ((-1)*o)) ;

					person.m_amount.put(Person.AccountEntry.checksumTRANSACTION, csT) ;
					person.m_amount.put(Person.AccountEntry.checksumGROUPTOTALS, csGT) ;
					person.m_amount.put(Person.AccountEntry.checksumINDIVIDUALTOTALS, csIT) ;

					Utils.m_settings.bCheckSumTransaction 		= (csT != 0.0f) ;
					Utils.m_settings.bCheckSumGroupTotals 		= (csGT != 0.0f) ;
					Utils.m_settings.bCheckSumIndividualTotals 	= (csIT != 0.0f) ;
				}
				aGroup.put(person.m_name, person) ;
			}
		} catch (Exception e) {
			System.err.println("Error:Transaction::sum()::" + e.getMessage()) ;
		}
    }
}
