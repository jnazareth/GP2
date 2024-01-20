package GP2.transaction;

import java.util.HashSet;
import java.util.Iterator;

import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;

public class IndividualAllocation extends amountAllocationsBase {
	public IndividualAllocation(TransactionDirection td, String gName, TransactionType.TType tt) {
        super.m_keyAllocation = FTType.FromToType.Individual;

        super.m_groupName = gName;
        super.m_tDirection = td;
		super.m_tType = tt;
	}
    
    protected void process(InputProcessor ip) {  
		try {
			InputProcessor._WhoFromTo w = ip._Input.get(super.m_keyAllocation) ;
			if (w == null) return ;

			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				if (na.m_amount == null) continue ;
				float fIndivAmt = na.m_amount ;
				HashSet<String> iSet = new HashSet<String>();
				iSet.add(na.m_name) ;
				super.putIndivAmount(super.m_groupName, fIndivAmt, iSet) ;
			}
		} catch (Exception e) {
			System.err.println("Error:IndividualAllocation::process()::" + e.getMessage()) ;
		}
    }
}
