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

public abstract class AmountAllocationsBase {
    protected String m_groupName;
    protected TransactionDirection m_tDirection;
    protected FTType.FromToType m_keyAllocation;
    protected TransactionType.TType m_tType;

    /**
     * Allocate individual amounts to persons in the group.
     * 
     * @param groupName Name of the group
     * @param amount Amount to allocate
     * @param individuals Set of individual names to allocate amounts to
     */
    protected void allocateIndividualAmounts(String groupName, float amount, HashSet<String> individuals) {
        try {
            Hashtable<String, Person> group = Utils.m_Groups.get(groupName).getCollection();
            Enumeration<String> personKeys = group.keys();

            while (personKeys.hasMoreElements()) {
                Person person = group.get(personKeys.nextElement());
                if (!individuals.contains(person.m_name)) continue;

                updatePersonAmounts(person, amount);
                group.put(person.m_name, person);
            }
        } catch (Exception e) {
            System.err.println("Error: AmountAllocationsBase::allocateIndividualAmounts::" + e.getMessage());
        }
    }

    /**
     * Update the amounts for a person based on transaction direction and type.
     * 
     * @param person Person object to update
     * @param amount Amount to update
     */
    private void updatePersonAmounts(Person person, float amount) {
        if (m_tDirection == TransactionDirection.FROM) {
            person.incrementAmount(Person.AccountEntry.FROM, amount);
            if (m_tType != TransactionType.TType.Clearing) {
                person.incrementAmount(Person.AccountEntry.PAID, amount);
            }
        } else if (m_tDirection == TransactionDirection.TO) {
            person.incrementAmount(Person.AccountEntry.TO, amount);
            if (m_tType != TransactionType.TType.Clearing) {
                person.incrementAmount(Person.AccountEntry.TRANSACTION, amount);
                person.incrementAmount(Person.AccountEntry.SPENT, amount);
            }
        }

        // Handle clearing type transactions
        if (m_tType == TransactionType.TType.Clearing) {
            if (m_tDirection == TransactionDirection.FROM) {
                person.incrementAmount(Person.AccountEntry.PAID, amount);
            } else if (m_tDirection == TransactionDirection.TO) {
                person.incrementAmount(Person.AccountEntry.PAID, -amount);
            }
        }
    }

    /**
     * Process method to be implemented by subclasses.
     * 
     * @param ip Input processor
     */
    protected void process(InputProcessor ip) {
        // To be implemented by subclasses
    }
}