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
        final String H_TRANSACTION_AMOUNTS	= "transaction amounts" ;
        final String H_OWE					= "(you owe) / owed to you" ;
        final String H_INDIVIDUAL_TOTALS	= "individual \"spent\"" ;
        final static String H_ITEM			= "Item" ;
        final String H_CATEGORY				= "Category" ;
        final String H_VENDOR				= "Vendor" ;
        final String H_DESCRIPTION			= "Description" ;
        final String H_AMOUNT				= "Amount" ;
        final String H_FROM					= "From" ;
        final String H_TO					= "To" ;
        final String H_ACTION				= "Action" ;
        final String H_CHECKSUM				= "CheckSum" ;
        final String H_INDCHECKSUM			= "IndCheckSum" ;
        final String H_INDIVIDUAL_PAID		= "individual \"paid\"" ;
    }

    public interface ExportKeys {
        final String keyItem            = "item" ;
        final String keyCategory        = "category" ;
        final String keyVendor          = "vendor" ;
        final String keyDescription     = "description" ;
        final String keyAmount          = "amount" ;
        final String keyFrom            = "from" ;
        final String keyTo              = "to" ;
        final String keyAction          = "action" ;
        final String keyTransactions    = "transactions" ;
        final String keyOwe             = "owe" ;
        final String keyCheckSum        = "checksum" ;
        final String keySpent           = "spent" ;
        final String keyIndividualCheckSum  = "individualchecksum" ;
        final String keyPaid            = "paid" ;
    }

    // members
    RowLayout header0   = new RowLayout();
    RowLayout header1   = new RowLayout();
    RowLayout template  = new RowLayout();
    Hashtable<String, ArrayList<RowLayout>> m_exportLinesGroup  ;	// String = groupName (Key)

    ArrayList<String> getSortedPersons(String group) {
        ArrayList<String> al = new ArrayList<String>() ;

        Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(group) ;
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
        header1.addCell(pos++, ExportKeys.keyCheckSum, XLSHeaders.H_CHECKSUM) ;
        header0.addCell(pos, ExportKeys.keySpent, XLSHeaders.H_INDIVIDUAL_TOTALS) ;
        for (String p : persons) header1.addCell(pos++, ExportKeys.keySpent  + Constants._ID_SEPARATOR + p, p) ;
        header1.addCell(pos++, ExportKeys.keyIndividualCheckSum, XLSHeaders.H_INDCHECKSUM) ;
        header0.addCell(pos, ExportKeys.keyPaid, XLSHeaders.H_INDIVIDUAL_PAID) ;
        for (String p : persons) header1.addCell(pos++, ExportKeys.keyPaid  + Constants._ID_SEPARATOR + p, p) ;
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

    RowLayout putRow(String item, String category, String vendor, String desc, String amt, String from, String to, String group, String action, String def) {
        RowLayout row = new RowLayout() ;

        int pos = 1 ;
		row.addCell(pos++, ExportKeys.keyItem,      item) ;
		row.addCell(pos++, ExportKeys.keyCategory,  category) ;
        row.addCell(pos++, ExportKeys.keyVendor,    vendor) ;
		row.addCell(pos++, ExportKeys.keyDescription, desc) ;
		row.addCell(pos++, ExportKeys.keyAmount,    amt) ;
		row.addCell(pos++, ExportKeys.keyFrom,      from) ;
		row.addCell(pos++, ExportKeys.keyTo,        to) ;
		row.addCell(pos++, ExportKeys.keyAction,    action) ;

		float cs = 0, indcs = 0 ;
        Hashtable<String, Person> aGroup = Utils.m_GroupCollection.get(group) ;
        List<String> mapKeys = new ArrayList<String>(aGroup.keySet());
        Collections.sort(mapKeys);
        Iterator<String> iter = mapKeys.iterator();
        while (iter.hasNext()) {
            Person person = aGroup.get(iter.next());
            row.addCell(pos++, ExportKeys.keyTransactions + Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.TRANSACTION))) ;
            row.addCell(pos++, ExportKeys.keyOwe +          Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.OWE_OWED))) ;
            cs = person.m_amount.get(Person.AccountEntry.checksumTRANSACTION) ;
            row.addCell(pos++, ExportKeys.keySpent +        Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.SPENT))) ;
            indcs = person.m_amount.get(Person.AccountEntry.checksumINDIVIDUAL) ;
            row.addCell(pos++, ExportKeys.keyPaid +         Constants._ID_SEPARATOR + person.m_name,    Utils.roundAmount(person.m_amount.get(Person.AccountEntry.PAID))) ;
        }
        row.addCell(pos++, ExportKeys.keyCheckSum, Utils.roundAmount(cs)) ;
        row.addCell(pos++, ExportKeys.keyIndividualCheckSum, Utils.roundAmount(indcs)) ;

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
