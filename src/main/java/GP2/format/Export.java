package GP2.format;

import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.person.Person;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

public class Export {
    public interface XLSHeaders {
        final String H_TRANSACTION_AMOUNTS	        = "transaction amounts" ;
        final String H_OWE					        = "(you owe) / owed to you" ;
        final String H_INDIVIDUAL_TOTALS	        = "individual \"spent\"" ;
        final static String H_ITEM			        = "Item" ;
        final String H_CATEGORY				        = "Category" ;
        final String H_VENDOR				        = "Vendor" ;
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
        final String keyVendor                      = "vendor" ;
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

    // members
    RowLayout header0   = new RowLayout();
    RowLayout header1   = new RowLayout();
    RowLayout template  = new RowLayout();
    Hashtable<String, ArrayList<RowLayout>> m_exportLinesGroup  ;	// String = groupName (Key)

    ArrayList<String> getSortedPersons(String sGroupName) {
        ArrayList<String> al = new ArrayList<String>() ;

        Hashtable<String, Person> aGroup = Utils.m_Groups.get(sGroupName).getCollection() ;
        List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
		Collections.sort(mapKeys);
        Iterator<String> iter = mapKeys.iterator();
		while (iter.hasNext()) {
			Person person = aGroup.get(iter.next());
            al.add(person.m_name);
        }
        return al ;
    }

	void buildHeaders(String group) {
        header0.empty();
        header1.empty();
        template.empty();

        boolean bSuppressCheckSum   = Utils.m_settings.getSuppressCStoUse() ;

        int pos = 1 ;
		header1.addCell(pos++, ExportKeys.keyItem,      XLSHeaders.H_ITEM) ;
		header1.addCell(pos++, ExportKeys.keyCategory,  XLSHeaders.H_CATEGORY) ;
		header1.addCell(pos++, ExportKeys.keyVendor,    XLSHeaders.H_VENDOR) ;
		header1.addCell(pos++, ExportKeys.keyDescription, XLSHeaders.H_DESCRIPTION) ;
		header1.addCell(pos++, ExportKeys.keyAmount,    XLSHeaders.H_AMOUNT) ;
		header1.addCell(pos++, ExportKeys.keyFrom,      XLSHeaders.H_FROM) ;
		header1.addCell(pos++, ExportKeys.keyTo,        XLSHeaders.H_TO) ;
		header1.addCell(pos++, ExportKeys.keyAction,    XLSHeaders.H_ACTION) ;

        ArrayList<String> persons = getSortedPersons(group);
        header0.addCell(pos, ExportKeys.keyTransactions, XLSHeaders.H_TRANSACTION_AMOUNTS) ;
		for (String p : persons) header1.addCell(pos++, ExportKeys.keyTransactions  + Constants._ID_SEPARATOR + p, p) ;
        header0.addCell(pos, ExportKeys.keyOwe, XLSHeaders.H_OWE) ;
        for (String p : persons) header1.addCell(pos++, ExportKeys.keyOwe  + Constants._ID_SEPARATOR + p, p) ;
        if ( (!bSuppressCheckSum) || (Utils.m_settings.bCheckSumTransaction) )
            header1.addCell(pos++, ExportKeys.keyCheckSumTransaction, XLSHeaders.H_CHECKSUM_TRANSACTION) ;
        header0.addCell(pos, ExportKeys.keySpent, XLSHeaders.H_INDIVIDUAL_TOTALS) ;
        for (String p : persons) header1.addCell(pos++, ExportKeys.keySpent  + Constants._ID_SEPARATOR + p, p) ;
        if ( (!bSuppressCheckSum) || (Utils.m_settings.bCheckSumGroupTotals) )
            header1.addCell(pos++, ExportKeys.keyCheckSumGroupTotals, XLSHeaders.H_CHECKSUM_GROUPTOTALS) ;
        header0.addCell(pos, ExportKeys.keyPaid, XLSHeaders.H_INDIVIDUAL_PAID) ;
        for (String p : persons) header1.addCell(pos++, ExportKeys.keyPaid  + Constants._ID_SEPARATOR + p, p) ;
        if ( (!bSuppressCheckSum) || (Utils.m_settings.bCheckSumIndividualTotals) ) {
            header0.addCell(pos, ExportKeys.keyCheckSumIndividualTotals, XLSHeaders.H_CHECKSUM_INDIVIDUALTOTALS) ;
            for (String p : persons) header1.addCell(pos++, ExportKeys.keyCheckSumIndividualTotals  + Constants._ID_SEPARATOR + p, p) ;
        }
    }

	String toCSVLine(RowLayout rl) {
		String sLine = "" ;
        int pos = 1 ;
        for (RowLayout.CellLayout c : rl.m_Cells) {
            while (pos < c.xlsPosition) {
                sLine += Constants._TAB_SEPARATOR ;
                pos++ ;
            }
            sLine += c.xlsPositionValue + Constants._TAB_SEPARATOR ;
            pos++;
        }
		return sLine ;
	}

	RowLayout buildTemplate(String group) {
		if (template.length() != 0) return template ;
        template = new RowLayout(header1) ;
		for (int i = 1; i <= template.length(); i++) {
			template.setValue(i, Constants._TAB_SEPARATOR);	// init
		}
		return template ;
	}

	RowLayout buildRow(String group, RowLayout line) {
		RowLayout templateRow = buildTemplate(group) ;
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

    RowLayout putRow(String item, String category, String vendor, String desc, String amt, String from, String to, String group, String action, Double rate) {
        RowLayout row = new RowLayout() ;

        boolean bSuppressCheckSum   = Utils.m_settings.getSuppressCStoUse() ;
		float fRate = rate.floatValue();
        int pos = 1 ;
		row.addCell(pos++, ExportKeys.keyItem,      item) ;
		row.addCell(pos++, ExportKeys.keyCategory,  category) ;
        row.addCell(pos++, ExportKeys.keyVendor,    vendor) ;
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
        if ( (!bSuppressCheckSum) || (Utils.m_settings.bCheckSumTransaction) )
            row.addCell(pos++, ExportKeys.keyCheckSumTransaction, Utils.roundAmount(csTransaction * fRate)) ;
        if ( (!bSuppressCheckSum) || (Utils.m_settings.bCheckSumGroupTotals) )
            row.addCell(pos++, ExportKeys.keyCheckSumGroupTotals, Utils.roundAmount(csGroupTotals * fRate)) ;

            // Create / Add to Collection
		if (m_exportLinesGroup == null) m_exportLinesGroup = new Hashtable<String, ArrayList<RowLayout>>() ;
		ArrayList<RowLayout> aGrp = m_exportLinesGroup.get(group) ;
		if (aGrp == null) {
			aGrp = new ArrayList<RowLayout>() ;
			aGrp.add(row) ;
			m_exportLinesGroup.put(group, aGrp) ;
		} else {
			aGrp.add(row) ;
		}

        return row ;
    }

    void dumpCollection() {
		Enumeration<String> keysGroup = m_exportLinesGroup.keys();
		while(keysGroup.hasMoreElements()) {
			String groupName = keysGroup.nextElement();
			ArrayList<RowLayout> rList = m_exportLinesGroup.get(groupName) ;
			System.out.println(groupName + ": ");
            for (RowLayout rl : rList) {
                rl.dumpCollection();
            }
        }
    }

    /*
     * class RowLayout
     */

    public class RowLayout {
		ArrayList<CellLayout> m_Cells  = new ArrayList<CellLayout>();

        public RowLayout(RowLayout rl) {
            for (CellLayout c : rl.m_Cells) {
                CellLayout cl = rl.getCell(c.xlsPositionName) ;
                this.addCell(cl.xlsPosition, cl.xlsPositionName, cl.xlsPositionValue) ;
            }
        }

        public RowLayout() {
        }

        void addCell(int xlsPos, String xlsKey, String sValue) {
            CellLayout cl = new CellLayout(xlsPos, xlsKey, sValue) ;
            m_Cells.add(cl) ;
        }

        CellLayout getCell (String posName) {
            for (CellLayout c : m_Cells) {
                if (c.xlsPositionName.equalsIgnoreCase(posName)) return c ;
            }
            return null ;
        }

        CellLayout getCell (int pos) {
            for (CellLayout c : m_Cells) {
                if (c.xlsPosition == pos) return c ;
            }
            return null ;
        }

        CellLayout setValue (int i, String v) {
            CellLayout cl = getCell(i) ;
            if (cl != null) {
                cl.xlsPositionValue = v;
                return cl ;
            }
            return null ;
        }
        CellLayout setValue (String posName, String v) {
            CellLayout cl = getCell(posName) ;
            if (cl != null) {
                cl.xlsPositionValue = v;
                return cl ;
            }
            return null ;
        }

        public int length() {
            return m_Cells.size();
        }

        public void empty(){
            m_Cells.clear();
        }

        void dumpCollection() {
            for (CellLayout c : m_Cells) {
                System.out.println(c.xlsPosition + "|" + c.xlsPositionName + "|" + c.xlsPositionValue);
            }
        }

        public class CellLayout {
            int		xlsPosition;
            String  xlsPositionName;
            String	xlsPositionValue;

            public CellLayout(int x, String n, String v) {
                xlsPosition = x;
                xlsPositionName = n;
                xlsPositionValue = v;
            }
        }
    }
}
