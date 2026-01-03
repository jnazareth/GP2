package GP2.transaction;

import java.util.Iterator;

import GP2.group.Groups.Group;
import GP2.input.FTType;
import GP2.input.InputProcessor;
import GP2.person.GPAction.TransactionType;
import GP2.transaction.Transaction.TransactionDirection;
import GP2.utils.Utils;

public class AllAllocation extends AmountAllocationsBase {

    // Constructor to initialize the AllAllocation object
    public AllAllocation(TransactionDirection transactionDirection, String groupName, TransactionType.TType transactionType) {
        super.m_keyAllocation = FTType.FromToType.All;
        super.m_groupName = groupName;
        super.m_tDirection = transactionDirection;
        super.m_tType = transactionType;
    }

    // Process the input and allocate amounts
    protected void process(InputProcessor inputProcessor) {
        try {
            InputProcessor._WhoFromTo whoFromTo = inputProcessor._Input.get(super.m_keyAllocation);
            if (whoFromTo == null) return;

            Group group = Utils.m_Groups.get(super.m_groupName);
            allocateAmounts(whoFromTo, group);
        } catch (Exception e) {
            System.err.println("Error: AllAllocation::process()::" + e.getMessage());
        }
    }

    // Allocate amounts to individuals in the group
    private void allocateAmounts(InputProcessor._WhoFromTo whoFromTo, Group group) {
        Iterator<InputProcessor._NameAmount> iterator = whoFromTo._Collection.iterator();
        while (iterator.hasNext()) {
            InputProcessor._NameAmount nameAmount = iterator.next();
            if (nameAmount.m_amount == null) continue;

            float individualAmount = calculateIndividualAmount(nameAmount.m_amount, whoFromTo._Count);
            distributeAmountToGroup(individualAmount, group);
        }
    }

    // Calculate the individual amount by dividing the total amount by the count
    private float calculateIndividualAmount(Float totalAmount, int count) {
        return totalAmount / count;
    }

    // Distribute the calculated amount to all active members of the group
    private void distributeAmountToGroup(float individualAmount, Group group) {
        super.allocateIndividualAmounts(super.m_groupName, individualAmount, group.getAllActive(super.m_groupName));
    }
}