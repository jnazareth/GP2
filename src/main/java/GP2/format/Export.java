package GP2.format;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.person.Person;

import java.util.*;

public class Export {

    public interface XLSHeaders {
        final String H_TRANSACTION_AMOUNTS	        = "transaction amounts" ;
        final String H_OWE					        = "(you owe) / owed to you" ;
        final String H_INDIVIDUAL_TOTALS	        = "individual \"spent\"" ;
        final static String H_ITEM			        = "Item" ;
        final String H_CATEGORY				        = "Category" ;
        final String H_SUBCATEGORY			        = "SubCategory" ;
        final String H_DESCRIPTION			        = "Description" ;
        final String H_AMOUNT				        = "Amount" ;
        final String H_FROM					        = "From" ;
        final String H_TO					        = "To" ;
        final String H_ACTION				        = "Action" ;
        final String H_CHECKSUM_TRANSACTION			= "cs(Transaction)" ;
        final String H_CHECKSUM_GROUPTOTALS         = "cs(GroupTotals)" ;
        final String H_INDIVIDUAL_PAID		        = "individual \"paid\"" ;
        final String H_CHECKSUM_INDIVIDUALTOTALS	= "cs(IndividualTotals)" ;
    }

    public interface ExportKeys {
        final String keyItem                        = "item" ;
        final String keyCategory                    = "category" ;
        final String keySubCategory                 = "subcategory" ;
        final String keyDescription                 = "description" ;
        final String keyAmount                      = "amount" ;
        final String keyFrom                        = "from" ;
        final String keyTo                          = "to" ;
        final String keyAction                      = "action" ;
        final String keyTransactions                = "transactions" ;
        final String keyOwe                         = "owe" ;
        final String keyCheckSumTransaction         = "checksumTransaction" ;
        final String keySpent                       = "spent" ;
        final String keyCheckSumGroupTotals         = "checksumGroupTotals" ;
        final String keyPaid                        = "paid" ;
        final String keyCheckSumIndividualTotals    = "checksumIndividualTotals" ;
    }

    // Members
    public RowLayout header0 = new RowLayout();
    public RowLayout header1 = new RowLayout();
    public RowLayout template = new RowLayout();
    public Hashtable<String, ArrayList<RowLayout>> m_exportLinesGroup;

    public ArrayList<String> getSortedPersons(String groupName) {
        ArrayList<String> personNames = new ArrayList<>();
        Hashtable<String, Person> group = Utils.m_Groups.get(groupName).getCollection();
        List<String> sortedKeys = new ArrayList<>(group.keySet());
        Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            personNames.add(group.get(key).m_name);
        }
        return personNames;
    }

    public void buildHeaders(String group) {
        header0.empty();
        header1.empty();
        template.empty();

        boolean suppressCheckSum = Utils.m_settings.getSuppressCStoUse();
        int pos = 1;

        int pos = 1 ;
		header1.addCell(pos++, ExportKeys.keyItem,      XLSHeaders.H_ITEM) ;
		header1.addCell(pos++, ExportKeys.keyCategory,  XLSHeaders.H_CATEGORY) ;
		header1.addCell(pos++, ExportKeys.keySubCategory,    XLSHeaders.H_SUBCATEGORY) ;
		header1.addCell(pos++, ExportKeys.keyDescription, XLSHeaders.H_DESCRIPTION) ;
		header1.addCell(pos++, ExportKeys.keyAmount,    XLSHeaders.H_AMOUNT) ;
		header1.addCell(pos++, ExportKeys.keyFrom,      XLSHeaders.H_FROM) ;
		header1.addCell(pos++, ExportKeys.keyTo,        XLSHeaders.H_TO) ;
		header1.addCell(pos++, ExportKeys.keyAction,    XLSHeaders.H_ACTION) ;

    private int addPersonHeaders(String group, int pos, boolean suppressCheckSum) {
        ArrayList<String> persons = getSortedPersons(group);

        pos = addTransactionHeaders(pos, persons);
        pos = addOweHeaders(pos, persons);
        pos = addSpentHeaders(pos, persons, suppressCheckSum);
        pos = addPaidHeaders(pos, persons, suppressCheckSum);

        return pos;
    }

    private int addTransactionHeaders(int pos, ArrayList<String> persons) {
        header0.addCell(pos, ExportKeys.keyTransactions, XLSHeaders.H_TRANSACTION_AMOUNTS);
        for (String person : persons) {
            header1.addCell(pos++, ExportKeys.keyTransactions + Constants._ID_SEPARATOR + person, person);
        }
        return pos;
    }

    private int addOweHeaders(int pos, ArrayList<String> persons) {
        header0.addCell(pos, ExportKeys.keyOwe, XLSHeaders.H_OWE);
        for (String person : persons) {
            header1.addCell(pos++, ExportKeys.keyOwe + Constants._ID_SEPARATOR + person, person);
        }
        return pos;
    }

    private int addSpentHeaders(int pos, ArrayList<String> persons, boolean suppressCheckSum) {
        header0.addCell(pos, ExportKeys.keySpent, XLSHeaders.H_INDIVIDUAL_TOTALS);
        for (String person : persons) {
            header1.addCell(pos++, ExportKeys.keySpent + Constants._ID_SEPARATOR + person, person);
        }
        if (!suppressCheckSum || Utils.m_settings.bCheckSumGroupTotals) {
            header1.addCell(pos++, ExportKeys.keyCheckSumGroupTotals, XLSHeaders.H_CHECKSUM_GROUPTOTALS);
        }
        return pos;
    }

    private int addPaidHeaders(int pos, ArrayList<String> persons, boolean suppressCheckSum) {
        header0.addCell(pos, ExportKeys.keyPaid, XLSHeaders.H_INDIVIDUAL_PAID);
        for (String person : persons) {
            header1.addCell(pos++, ExportKeys.keyPaid + Constants._ID_SEPARATOR + person, person);
        }
        if (!suppressCheckSum || Utils.m_settings.bCheckSumIndividualTotals) {
            header0.addCell(pos, ExportKeys.keyCheckSumIndividualTotals, XLSHeaders.H_CHECKSUM_INDIVIDUALTOTALS);
            for (String person : persons) {
                header1.addCell(pos++, ExportKeys.keyCheckSumIndividualTotals + Constants._ID_SEPARATOR + person, person);
            }
        }
        return pos;
    }

    public String toCSVLine(RowLayout rl) {
        StringBuilder sLine = new StringBuilder();
        int pos = 1;
        for (RowLayout.CellLayout c : rl.m_Cells) {
            while (pos < c.xlsPosition) {
                sLine.append(Constants._TAB_SEPARATOR);
                pos++;
            }
            sLine.append(c.xlsPositionValue).append(Constants._TAB_SEPARATOR);
            pos++;
        }
        return sLine.toString();
    }

    public RowLayout buildTemplate(String group) {
        if (template.length() != 0) return template;
        template = new RowLayout(header1);
        for (int i = 1; i <= template.length(); i++) {
            template.setValue(i, Constants._TAB_SEPARATOR); // init
        }
        return template;
    }

    public RowLayout buildRow(String group, RowLayout line) {
        RowLayout templateRow = buildTemplate(group);
        for (RowLayout.CellLayout c : templateRow.m_Cells) {
            try {
                RowLayout.CellLayout lineCell = line.getCell(c.xlsPositionName);
                if (lineCell != null) templateRow.setValue(c.xlsPositionName, lineCell.xlsPositionValue);
            } catch (IndexOutOfBoundsException ioobe) {
                System.err.println("buildRow Error: " + ioobe.getMessage());
            }
        }
		return templateRow ;
	}

    RowLayout putRow(String item, String category, String subcategory, String desc, String amt, String from, String to, String group, String action, Double rate) {
        RowLayout row = new RowLayout() ;

        boolean bSuppressCheckSum   = Utils.m_settings.getSuppressCStoUse() ;
		float fRate = rate.floatValue();

        int pos = 1 ;
		row.addCell(pos++, ExportKeys.keyItem,      item) ;
		row.addCell(pos++, ExportKeys.keyCategory,  category) ;
        row.addCell(pos++, ExportKeys.keySubCategory,    subcategory) ;
		row.addCell(pos++, ExportKeys.keyDescription, desc) ;

		String xAmt = String.valueOf(fRate * Float.valueOf(amt)) ;
        row.addCell(pos++, ExportKeys.keyAmount,    xAmt) ;
		row.addCell(pos++, ExportKeys.keyFrom,      from) ;
		row.addCell(pos++, ExportKeys.keyTo,        to) ;
		row.addCell(pos++, ExportKeys.keyAction,    action) ;

		float csTransaction = 0, csGroupTotals = 0, csIndividualTotals = 0 ;
        Hashtable<String, Person> aGroup = Utils.m_Groups.get(group).getCollection() ;
        List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
        Collections.sort(mapKeys);
        Iterator<String> iter = mapKeys.iterator();
        while (iter.hasNext()) {
            Person person = aGroup.get(iter.next());
            row.addCell(pos++, ExportKeys.keyTransactions + Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.TRANSACTION) * fRate)) ;
            row.addCell(pos++, ExportKeys.keyOwe +          Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.OWE_OWED) * fRate)) ;
            csTransaction = person.m_amount.get(Person.AccountEntry.checksumTRANSACTION) * fRate ;
            row.addCell(pos++, ExportKeys.keySpent +        Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.SPENT) * fRate)) ;
            csGroupTotals = person.m_amount.get(Person.AccountEntry.checksumGROUPTOTALS) * fRate ;
            row.addCell(pos++, ExportKeys.keyPaid +         Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.PAID) * fRate)) ;
            csIndividualTotals = person.m_amount.get(Person.AccountEntry.checksumINDIVIDUALTOTALS) * fRate ;
            if ( (!bSuppressCheckSum) || (Utils.m_settings.bCheckSumIndividualTotals) )
                row.addCell(pos++, ExportKeys.keyCheckSumIndividualTotals +         Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(csIndividualTotals * fRate)) ;
        }
    }

    private void addRowToCollection(String group, RowLayout row) {
        if (m_exportLinesGroup == null) {
            m_exportLinesGroup = new Hashtable<>();
        }
        m_exportLinesGroup.computeIfAbsent(group, k -> new ArrayList<>()).add(row);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> keysGroup = m_exportLinesGroup.keys();
        while (keysGroup.hasMoreElements()) {
            String groupName = keysGroup.nextElement();
            ArrayList<RowLayout> rList = m_exportLinesGroup.get(groupName);
            sb.append(groupName).append(": \n");
            for (RowLayout rl : rList) {
                sb.append(rl.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    /*
     * class RowLayout
     */

    public class RowLayout {
        ArrayList<CellLayout> m_Cells = new ArrayList<>();

        public RowLayout(RowLayout rl) {
            for (CellLayout c : rl.m_Cells) {
                CellLayout cl = rl.getCell(c.xlsPositionName);
                this.addCell(cl.xlsPosition, cl.xlsPositionName, cl.xlsPositionValue);
            }
        }

        public RowLayout() {
        }

        void addCell(int xlsPos, String xlsKey, String sValue) {
            CellLayout cl = new CellLayout(xlsPos, xlsKey, sValue);
            m_Cells.add(cl);
        }

        public CellLayout getCell(String posName) {
            for (CellLayout c : m_Cells) {
                if (c.xlsPositionName.equalsIgnoreCase(posName)) return c;
            }
            return null;
        }

        public CellLayout getCell(int pos) {
            for (CellLayout c : m_Cells) {
                if (c.xlsPosition == pos) return c;
            }
            return null;
        }

        public CellLayout setValue(int i, String v) {
            CellLayout cl = getCell(i);
            if (cl != null) {
                cl.xlsPositionValue = v;
                return cl;
            }
            return null;
        }

        public CellLayout setValue(String posName, String v) {
            CellLayout cl = getCell(posName);
            if (cl != null) {
                cl.xlsPositionValue = v;
                return cl;
            }
            return null;
        }

        public int length() {
            return m_Cells.size();
        }

        public void empty() {
            m_Cells.clear();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (CellLayout c : m_Cells) {
                sb.append(c.xlsPosition).append("|").append(c.xlsPositionName).append("|").append(c.xlsPositionValue).append("\n");
            }
            return sb.toString();
        }

        public class CellLayout {
            public int xlsPosition;
            public String xlsPositionName;
            public String xlsPositionValue;

            public CellLayout(int x, String n, String v) {
                xlsPosition = x;
                xlsPositionName = n;
                xlsPositionValue = v;
            }
        }
    }
}