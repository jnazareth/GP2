package GP2.transaction;

import GP2.input.FTType.FromToTypes;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public class amountAllocations extends amountAllocationsBase {
    InputProcessor          iProcessor = new InputProcessor();

    AllAllocation           allAllocation ;
    RemainderAllocation     remAllocation ;
    IndividualAllocation    indivAllocation ;

    public amountAllocations(TransactionDirection td, String gName, String in, float amt, TransactionType.TType tt) {
        allAllocation = new AllAllocation(td, gName, tt);
        remAllocation = new RemainderAllocation(td, gName, tt);
        indivAllocation = new IndividualAllocation(td, gName, tt);

        int numActive = Utils.m_Groups.get(gName).getAllActiveCount(gName);

        String s = percentageToAmounts(amt, in) ;
        iProcessor.processFrTo("", 0, amt, numActive, s) ;
    }

    private String percentageToAmounts(float amt, String in) {
		if (in.indexOf(FromToTypes.Percentage) == -1) return in;
		return Utils.stripPercentge(amt, in) ;
	}

    protected void process() {
        try {
            allAllocation.process(iProcessor);
            remAllocation.process(iProcessor);
            indivAllocation.process(iProcessor);
        } catch (Exception e) {
            System.err.println("Error:amountAllocations::process()::" + e.getMessage()) ;
        }
    }
}
