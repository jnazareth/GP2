package GP2.transaction;

import java.util.HashSet;
import java.util.Iterator;

import GP2.group.Groups.Group;
import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public class RemainderAllocation extends AmountAllocationsBase {

    /**
     * Constructor to initialize RemainderAllocation with transaction details.
     * 
     * @param td Transaction direction
     * @param gName Group name
     * @param tt Transaction type
     */
    public RemainderAllocation(TransactionDirection td, String gName, TransactionType.TType tt) {
        super.m_keyAllocation = FTType.FromToType.Remainder;
        super.m_groupName = gName;
        super.m_tDirection = td;
        super.m_tType = tt;
    }

    /**
     * Retrieve individual inputs from the input processor.
     * 
     * @param ip Input processor
     * @return Set of individual names
     */
    private HashSet<String> getIndividualInputs(InputProcessor ip) {
        HashSet<String> individualInputs = new HashSet<>();
        InputProcessor._WhoFromTo whoFromTo = ip._Input.get(FTType.FromToType.Individual);
        if (whoFromTo != null) {
            for (InputProcessor._NameAmount nameAmount : whoFromTo._Collection) {
                individualInputs.add(nameAmount.m_name);
            }
        }
        return individualInputs;
    }

    /**
     * Get the set of active members excluding individuals with specific inputs.
     * 
     * @param allMembers Set of all active members
     * @param individualInputs Set of individual inputs
     * @return Set of remaining active members
     */
    private HashSet<String> getRemainingActiveMembers(HashSet<String> allMembers, HashSet<String> individualInputs) {
        HashSet<String> remainingMembers = new HashSet<>(allMembers);
        remainingMembers.removeAll(individualInputs);
        return remainingMembers;
    }

    /**
     * Process remainder allocations using the input processor.
     * 
     * @param ip Input processor
     */
    protected void process(InputProcessor ip) {
        try {
            InputProcessor._WhoFromTo whoFromTo = ip._Input.get(super.m_keyAllocation);
            if (whoFromTo == null) return;

            HashSet<String> remainingMembers = prepareRemainingMembers(ip);
            if (remainingMembers == null) return;

            allocateRemainderAmounts(whoFromTo, remainingMembers);
        } catch (Exception e) {
            System.err.println("Error: RemainderAllocation::process()::" + e.getMessage());
        }
    }

    /**
     * Prepare the set of remaining members for allocation.
     * 
     * @param ip Input processor
     * @return Set of remaining members
     */
    private HashSet<String> prepareRemainingMembers(InputProcessor ip) {
        try {
            Group group = Utils.m_Groups.get(super.m_groupName);
            HashSet<String> allActiveMembers = group.getAllActive(super.m_groupName);
            HashSet<String> individualInputs = getIndividualInputs(ip);
            return getRemainingActiveMembers(allActiveMembers, individualInputs);
        } catch (Exception e) {
            System.err.println("Error: RemainderAllocation::prepareRemainingMembers::" + e.getMessage());
            return null;
        }
    }

    /**
     * Allocate remainder amounts to the remaining members.
     * 
     * @param whoFromTo Data structure containing names and amounts
     * @param remainingMembers Set of remaining members
     */
    private void allocateRemainderAmounts(InputProcessor._WhoFromTo whoFromTo, HashSet<String> remainingMembers) {
        int remainingSize = remainingMembers.size();
        for (InputProcessor._NameAmount nameAmount : whoFromTo._Collection) {
            if (nameAmount.m_amount == null) continue;

            float individualAmount = nameAmount.m_amount / remainingSize;
            super.allocateIndividualAmounts(m_groupName, individualAmount, remainingMembers);
        }
    }
}