package GP2.group;

import GP2.person.Person;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.xcur.CrossCurrency;

import java.util.Hashtable;
import java.util.Enumeration;

public class GroupAccount extends Object {
	private Hashtable<String, _AGroup> m_Groups = new Hashtable<String, _AGroup>(); // String/key = groupName

	public _AGroup get(String group) {
		return m_Groups.get(group) ;
	}

	public void put(String g, _AGroup ag) {
		m_Groups.put(g, ag);
	}

	public Enumeration<String> keys() {
		return m_Groups.keys();
	}

	public int size() {
		return m_Groups.size();
	}

	public void dumpCollection() {
		Enumeration<String> keysGroup = this.m_Groups.keys();
		while(keysGroup.hasMoreElements()) {
			String groupName = keysGroup.nextElement();
            System.out.print(groupName + "\t::");

			_AGroup aG = this.get(groupName);
			aG.dumpCollection() ;
            System.out.println("");
		}
	}

// --------------------------------------------------------------------------------------
    public class _AGroup extends Hashtable<String, Person> {
		private Hashtable<String, Person> m_aGroup = new Hashtable<String, Person>() ;
        private EntryType m_gType ;
        public PGState m_gState ;
		public CrossCurrency m_ccurrency ;

		public _AGroup () {
            m_gType = EntryType.Group ;
            m_gState = new PGState(EntryState.Add) ;
        }

		public Person put(String s, Person p) {
			return m_aGroup.put(s, p) ;
		}

		public Hashtable<String, Person> getCollection() {
			return m_aGroup;
		}

		public EntryType getType() {
			return m_gType ;
		}

		public boolean isActive() {
			return m_gState.getActive();
		}

        public void dumpCollection() {
            Enumeration<String> keysPeople = m_aGroup.keys();
            while(keysPeople.hasMoreElements()){
                Person Person = m_aGroup.get(keysPeople.nextElement());
                System.out.print(Person.m_name + "\t");
            }
			return ;
		}

		//--------------------------------------------
		/*public class XCurrency {
			String m_Currency = "usd" ;
			String m_Format = "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)" ;
			String m_Baseline = "usd";
			Double m_Rate = 1.0d ;

			public XCurrency (String c, String f, String b, Double d) {
				if ((c != null) && (c.length() != 0)) m_Currency = c;
				if ((f != null) && (f.length() != 0)) m_Format = f;
				if ((b != null) && (b.length() != 0)) m_Baseline = b;
				if ((d != null) && (d != 0d)) m_Rate = d;
			}

			public XCurrency (EnumMap<CrossCurrency.XCurrencyProperties, String> xProperties) {
				//String c, f, b; Double d ;
				for (CrossCurrency.XCurrencyProperties p : xProperties.keySet()) {
					//System.out.println(p + ":::" + xProperties.get(p));
					String xcp = xProperties.get(p);
					if (p.compareTo(CrossCurrency.XCurrencyProperties.Currency) == 0)
						m_Currency = xcp ;
					else if (p.compareTo(CrossCurrency.XCurrencyProperties.Format) == 0)
						m_Format = xcp ;
					else if (p.compareTo(CrossCurrency.XCurrencyProperties.Baseline) == 0)
						m_Baseline = xcp ;
					else if (p.compareTo(CrossCurrency.XCurrencyProperties.Rate) == 0)
						m_Rate = Double.valueOf(xcp) ;
				}
			}
			public String toString() {
				return (m_Currency + "|" + m_Format + "|" + m_Baseline + "|" + m_Rate) ;
			}
		}*/
    }
}
