package GP2.transaction;

import GP2.input.FTType.FromToTypes;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public class AmountAllocations extends AmountAllocationsBase {
    private InputProcessor iProcessor;
    private AllAllocation allAllocation;
    private RemainderAllocation remAllocation;
    private IndividualAllocation indivAllocation;

    /**
     * Constructor to initialize allocations and process input.
     * 
     * @param td Transaction direction
     * @param gName Group name
     * @param in Input string
     * @param amt Amount
     * @param tt Transaction type
     */
    public AmountAllocations(TransactionDirection td, String gName, String in, float amt, TransactionType.TType tt) {
        initializeAllocations(td, gName, tt);
        processInput(gName, in, amt);
    }

    /**
     * Initialize allocation objects.
     * 
     * @param td Transaction direction
     * @param gName Group name
     * @param tt Transaction type
     */
    private void initializeAllocations(TransactionDirection td, String gName, TransactionType.TType tt) {
        iProcessor = new InputProcessor();
        allAllocation = new AllAllocation(td, gName, tt);
        remAllocation = new RemainderAllocation(td, gName, tt);
        indivAllocation = new IndividualAllocation(td, gName, tt);
    }

    /**
     * Process input and calculate allocations.
     * 
     * @param gName Group name
     * @param in Input string
     * @param amt Amount
     */
    private void processInput(String gName, String in, float amt) {
        int numActive = Utils.m_Groups.get(gName).getAllActiveCount(gName);
        String processedInput = convertPercentageToAmounts(amt, in);
        iProcessor.processFrTo("", 0, amt, numActive, processedInput);
    }

    /**
     * Convert percentage values in input to amounts.
     * 
     * @param amt Amount
     * @param in Input string
     * @return Processed input string
     */
    private String convertPercentageToAmounts(float amt, String in) {
        if (in.indexOf(FromToTypes.Percentage) == -1) {
            return in;
        }
        return Utils.stripPercentge(amt, in);
    }

    /**
     * Process all allocations.
     */
    protected void process() {
        try {
            allAllocation.process(iProcessor);
            remAllocation.process(iProcessor);
            indivAllocation.process(iProcessor);
        } catch (Exception e) {
            System.err.println("Error: AmountAllocations::process()::" + e.getMessage());
        }
    }
}