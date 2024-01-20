package GP2.transaction;

import java.util.HashSet;
import java.util.Iterator;

import GP2.group.Groups.Group;
import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public class RemainderAllocation extends amountAllocationsBase {
	public RemainderAllocation(TransactionDirection td, String gName, TransactionType.TType tt) {
        super.m_keyAllocation = FTType.FromToType.Remainder;

        super.m_groupName = gName;
        super.m_tDirection = td;
		super.m_tType = tt;
	}

    private HashSet<String> getIndivInput(InputProcessor ip) {
		HashSet<String> indivInput = new HashSet<String>() ;
		InputProcessor._WhoFromTo w = ip._Input.get(FTType.FromToType.Individual) ;
		if (w != null) {
			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator();
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				indivInput.add(na.m_name) ;
			}
		}
		return indivInput ;
	}

	private HashSet<String> getRemActive(HashSet<String> all, HashSet<String> indiv) {
		HashSet<String> diff = new HashSet<String>(all) ;
        diff.removeAll(indiv) ;
		return diff ;
	}

    protected void process(InputProcessor ip) {  
		try {
			InputProcessor._WhoFromTo w = ip._Input.get(super.m_keyAllocation) ;
			if (w == null) return ;

			HashSet<String> setRem = null ;
			int rSize = 1 ;
			try {	// prepare sets
                Group g = Utils.m_Groups.get(super.m_groupName) ;
        
				setRem = getRemActive(g.getAllActive(super.m_groupName), getIndivInput(ip));
				rSize = setRem.size() ;
			}  catch (Exception e) {
				System.err.println("Error:processSetRem::prepareSets::" + e.getMessage()) ;
				return ;
			}
			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				if (na.m_amount == null) continue ;
				float fIndivAmt = na.m_amount / rSize ;
				super.putIndivAmount(super.m_groupName, fIndivAmt, setRem) ;
			}
		} catch (Exception e) {
			System.err.println("Error:RemainderAllocation::process()::" + e.getMessage()) ;
		}
    }
}
