package GP2.transaction;

import java.util.Enumeration;
import java.util.Hashtable;

import GP2.person.GPAction.TransactionType;
import GP2.person.Person;
import GP2.utils.Utils;

public class Transaction {

    public enum TransactionDirection {
        FROM(0),
        TO(1);

        private final int value;

        TransactionDirection(int value) {
            this.value = value;
        }
    }

    private String m_groupName;
    private TransactionType.TType m_tType;
    private AmountAllocations fromAllocations;
    private AmountAllocations toAllocations;

    /**
     * Constructor to initialize Transaction with group and allocation details.
     * 
     * @param gName Group name
     * @param from From allocation string
     * @param to To allocation string
     * @param amt Amount string
     * @param tt Transaction type
     */
    public Transaction(String gName, String from, String to, String amt, TransactionType.TType tt) {
        m_groupName = gName;
        m_tType = tt;
        float fAmt = parseAmount(amt);

        fromAllocations = new AmountAllocations(TransactionDirection.FROM, gName, from, fAmt, tt);
        toAllocations = new AmountAllocations(TransactionDirection.TO, gName, to, fAmt, tt);
    }

    /**
     * Parse the amount from string to float.
     * 
     * @param amt Amount string
     * @return Parsed float amount
     */
    private float parseAmount(String amt) {
        try {
            return Float.parseFloat(amt);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    /**
     * Initialize the transaction by resetting amounts for each person in the group.
     */
    public void init() {
        try {
            Hashtable<String, Person> group = Utils.m_Groups.get(m_groupName).getCollection();
            for (Person person : group.values()) {
                resetPersonAmounts(person);
                group.put(person.m_name, person);
            }
        } catch (Exception e) {
            System.err.println("Error: Transaction::init()::" + e.getMessage());
        }
    }

    /**
     * Reset the amounts for a person.
     * 
     * @param person Person object
     */
    private void resetPersonAmounts(Person person) {
        person.m_amount.put(Person.AccountEntry.FROM, 0.0f);
        person.m_amount.put(Person.AccountEntry.TO, 0.0f);
        person.m_amount.put(Person.AccountEntry.TRANSACTION, 0.0f);
        person.m_amount.put(Person.AccountEntry.checksumTRANSACTION, 0.0f);
        person.m_amount.put(Person.AccountEntry.checksumGROUPTOTALS, 0.0f);
        person.m_amount.put(Person.AccountEntry.checksumINDIVIDUALTOTALS, 0.0f);
    }

    /**
     * Process the transaction allocations.
     */
    public void process() {
        try {
            fromAllocations.process();
            toAllocations.process();
        } catch (Exception e) {
            System.err.println("Error: Transaction::process()::" + e.getMessage());
        }
    }

    /**
     * Compute checksums for the transaction.
     * 
     * @param tType Transaction type
     * @param sGroupName Group name
     * @param cs Checksum array
     */
    private void computeCheckSums(TransactionType.TType tType, String sGroupName, Float[] cs) {
        try {
            Hashtable<String, Person> group = Utils.m_Groups.get(sGroupName).getCollection();
            float fromSum = 0.0f, toSum = 0.0f, spentSum = 0.0f, paidSum = 0.0f;

            for (Person person : group.values()) {
                if (person.isActive()) {
                    fromSum += person.m_amount.get(Person.AccountEntry.FROM);
                    toSum += person.m_amount.get(Person.AccountEntry.TO);
                    if (tType != TransactionType.TType.Clearing) {
                        spentSum += person.m_amount.get(Person.AccountEntry.SPENT);
                        paidSum += person.m_amount.get(Person.AccountEntry.PAID);
                    }
                }
            }

            cs[0] = Utils.truncate(fromSum - toSum).floatValue();
            cs[1] = Utils.truncate(spentSum - paidSum).floatValue();
        } catch (Exception e) {
            System.err.println("Error: Transaction::computeCheckSums()::" + e.getMessage());
        }
    }

    /**
     * Sum the transaction amounts and update checksums.
     */
    public void sum() {
        try {
            Hashtable<String, Person> group = Utils.m_Groups.get(m_groupName).getCollection();
            Float[] checkSums = {0.0f, 0.0f};

            for (Person person : group.values()) {
                if (person.isActive()) {
                    computeCheckSums(m_tType, m_groupName, checkSums);
                    updatePersonSums(person, checkSums);
                    group.put(person.m_name, person);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: Transaction::sum()::" + e.getMessage());
        }
    }

    /**
     * Update the sums and checksums for a person.
     * 
     * @param person Person object
     * @param checkSums Checksum array
     */
    private void updatePersonSums(Person person, Float[] checkSums) {
        float fromAmount = person.m_amount.get(Person.AccountEntry.FROM);
        float toAmount = person.m_amount.get(Person.AccountEntry.TO);
        float transactionSum = fromAmount - toAmount;
        person.incrementAmount(Person.AccountEntry.OWE_OWED, transactionSum);

        float individualSum = person.m_amount.get(Person.AccountEntry.OWE_OWED) + person.m_amount.get(Person.AccountEntry.SPENT);
        float paidAmount = person.m_amount.get(Person.AccountEntry.PAID);
        float individualChecksum = individualSum - paidAmount;

        person.m_amount.put(Person.AccountEntry.checksumTRANSACTION, checkSums[0]);
        person.m_amount.put(Person.AccountEntry.checksumGROUPTOTALS, checkSums[1]);
        person.m_amount.put(Person.AccountEntry.checksumINDIVIDUALTOTALS, individualChecksum);

        Utils.m_settings.bCheckSumTransaction = (checkSums[0] != 0.0f);
        Utils.m_settings.bCheckSumGroupTotals = (checkSums[1] != 0.0f);
        Utils.m_settings.bCheckSumIndividualTotals = (individualChecksum != 0.0f);
    }
}