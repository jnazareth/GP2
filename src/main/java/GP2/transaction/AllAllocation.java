package GP2.transaction;

import java.util.Iterator;

import GP2.group.Groups.Group;
import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public class AllAllocation  extends amountAllocationsBase {
	public AllAllocation(TransactionDirection td, String gName, TransactionType.TType tt) {
        super.m_keyAllocation = FTType.FromToType.All;

        super.m_groupName = gName;
        super.m_tDirection = td;
		super.m_tType = tt;
	}

    protected void process(InputProcessor ip) {  
		try {
			InputProcessor._WhoFromTo w = ip._Input.get(super.m_keyAllocation) ;
			if (w == null) return ;

			Group g = Utils.m_Groups.get(super.m_groupName) ;
			Iterator<InputProcessor._NameAmt> iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				InputProcessor._NameAmt na = iter.next();
				if (na.m_amount == null) continue ;
				float fIndivAmt = na.m_amount / w._Count ;
				super.putIndivAmount(super.m_groupName, fIndivAmt, g.getAllActive(super.m_groupName)) ;
			}
		} catch (Exception e) {
			System.err.println("Error:AllAllocation::process()::" + e.getMessage()) ;
		}
    }
}
