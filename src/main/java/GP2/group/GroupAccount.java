package GP2.group;

import GP2.person.Person;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;

import java.util.Hashtable;
import java.util.Enumeration;

public class GroupAccount extends Object {
	public Hashtable<String, _AGroup> m_Groups = new Hashtable<String, _AGroup>(); // String/key = groupName

	public _AGroup get(String group) {
		return m_Groups.get(group) ;
	}

	public void put(String g, _AGroup ag) {
		m_Groups.put(g, ag);
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
			//Hashtable<String, Person> aGroup = aG.getCollection();
			aG.dumpCollection() ;
            System.out.println("");
		}
	}

// --------------------------------------------------------------------------------------
    public class _AGroup extends Hashtable<String, Person> {
		private Hashtable<String, Person> m_aGroup = new Hashtable<String, Person>() ;
        private EntryType m_gType ;
        public PGState m_gState ; 
    
		public _AGroup () {
            m_gType = EntryType.Self ;
            m_gState = new PGState(EntryState.Disable) ;
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
    }
}
