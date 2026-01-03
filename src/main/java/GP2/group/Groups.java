package GP2.group;

import GP2.person.Person;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.xcur.CrossCurrency;

import java.util.Hashtable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

public class Groups extends Object {
	private Hashtable<String, Group> m_Groups = new Hashtable<String, Group>(); // String/key = groupName

	public Group get(String group) {
		return m_Groups.get(group) ;
	}

	public void put(String g, Group ag) {
		m_Groups.put(g, ag);
	}

	public Enumeration<String> keys() {
		return m_Groups.keys();
	}

	public int size() {
		return m_Groups.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (String groupName : Collections.list(this.m_Groups.keys())) {
			sb.append(groupName).append("\t::")
			.append(this.get(groupName).toString())
			.append("\n");
		}

		return sb.toString();
	}

// --------------------------------------------------------------------------------------
    public class Group extends Hashtable<String, Person> {
		private Hashtable<String, Person> m_aGroup = new Hashtable<String, Person>() ;
        private EntryType       m_gType ;
        public PGState          m_gState ;
		public CrossCurrency    m_ccurrency ;
        public String           m_GroupName ;

		public Group (String name) {
            m_GroupName = name; 
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
			return m_gState.isActive();
		}

		public HashSet<String> getAllActive(String gName) {
			HashSet<String> allSet = new HashSet<String>() ;

			Enumeration<String> keysPeople = m_aGroup.keys();
			while(keysPeople.hasMoreElements()){
				Person person = m_aGroup.get(keysPeople.nextElement());
				if (person.isActive()) allSet.add(person.m_name) ;
			}
			return allSet ;	
		}

		public int getAllActiveCount(String gName) {
			return getAllActive(gName).size();
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(m_GroupName).append("::\n");

			for (String key : Collections.list(m_aGroup.keys())) {
				Person person = m_aGroup.get(key);
				sb.append(person.m_name).append("\t");
			}

			return sb.toString();
		}		
    }
}
