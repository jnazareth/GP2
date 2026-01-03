package GP2.transaction;

import java.util.HashSet;
import java.util.Iterator;

import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;

public class IndividualAllocation extends AmountAllocationsBase {

    /**
     * Constructor to initialize IndividualAllocation with transaction details.
     * 
     * @param td Transaction direction
     * @param gName Group name
     * @param tt Transaction type
     */
    public IndividualAllocation(TransactionDirection td, String gName, TransactionType.TType tt) {
        super.m_keyAllocation = FTType.FromToType.Individual;
        super.m_groupName = gName;
        super.m_tDirection = td;
        super.m_tType = tt;
    }

    /**
     * Process individual allocations using the input processor.
     * 
     * @param ip Input processor
     */
    protected void process(InputProcessor ip) {
        try {
            InputProcessor._WhoFromTo whoFromTo = ip._Input.get(super.m_keyAllocation);
            if (whoFromTo == null) return;

            allocateAmounts(whoFromTo);
        } catch (Exception e) {
            System.err.println("Error: IndividualAllocation::process()::" + e.getMessage());
        }
    }

    /**
     * Allocate amounts to individuals based on the input data.
     * 
     * @param whoFromTo Data structure containing names and amounts
     */
    private void allocateAmounts(InputProcessor._WhoFromTo whoFromTo) {
        Iterator<InputProcessor._NameAmount> iterator = whoFromTo._Collection.iterator();
        while (iterator.hasNext()) {
            InputProcessor._NameAmount nameAmount = iterator.next();
            if (nameAmount.m_amount == null) continue;

            float individualAmount = nameAmount.m_amount;
            HashSet<String> individualSet = new HashSet<>();
            individualSet.add(nameAmount.m_name);

            super.allocateIndividualAmounts(m_groupName, individualAmount, individualSet);
        }
    }
}