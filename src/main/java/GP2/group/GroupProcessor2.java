package GP2.group;

import GP2.group.Groups.Group;
import GP2.person.GPAction;
import GP2.person.GPAction.ActionItem;
import GP2.person.GPAction.PGState;
import GP2.person.GPAction.PGState.EntryState;
import GP2.person.GPAction.PGType.EntryType;
import GP2.person.GPAction.TransactionType;
import GP2.person.Person;
import GP2.utils.Constants;
import GP2.utils.Utils;
import GP2.xcur.CrossCurrency;

import java.util.*;

public class GroupProcessor2 {

    private static final boolean SKIP = true;
    private static final boolean DO_NOT_SKIP = false;

    private Hashtable<String, Person> findOrCreateGroup(String groupName) {
        try {
            if (Utils.m_Groups == null) {
                Utils.m_Groups = new Groups();
            }

            Group group = Utils.m_Groups.get(groupName);
            if (group == null) {
                group = Utils.m_Groups.new Group(groupName);
                Utils.m_Groups.put(groupName, group);
            }
            return group.getCollection();
        } catch (Exception e) {
            System.err.println("Error in findOrCreateGroup: " + e.getMessage());
            return null;
        }
    }

    private boolean groupExists(String groupName) {
        try {
            return Utils.m_Groups != null && Utils.m_Groups.get(groupName) != null;
        } catch (Exception e) {
            System.err.println("Error in groupExists: " + e.getMessage());
            return false;
        }
    }

    private boolean personExists(String groupName, String personName) {
        try {
            if (Utils.m_Groups == null) return false;
            Group group = Utils.m_Groups.get(groupName);
            if (group == null) return false;

            Hashtable<String, Person> groupCollection = group.getCollection();
            return groupCollection.get(personName) != null;
        } catch (Exception e) {
            System.err.println("Error in personExists: " + e.getMessage());
            return false;
        }
    }

    private static boolean doClearing(String groupName, boolean groupExists) {
        return groupExists ? DO_NOT_SKIP : logGroupPersonError(EntryType.Group, groupName);
    }

    private static boolean isEntryStateToggle(ActionItem item) {
        return EnumSet.of(EntryState.Enable, EntryState.Disable, EntryState.NoOp).contains(item.state);
    }

    private static boolean isEntryStateAdd(ActionItem item) {
        return item.state == EntryState.Add;
    }

    private static boolean logGroupPersonError(EntryType entryType, String name) {
        String errorMessage = entryType == EntryType.Group
                ? "Error: \"" + name + "\" group does not exist. Use: \"" + name + " (*:group)\" to create"
                : "Error: \"" + name + "\" person does not exist. Use: \"" + name + " (*:self)\" to create";
        System.err.println(errorMessage);
        return SKIP;
    }

    private boolean processActions(HashMap<String, LinkedHashSet<ActionItem>> actions, TransactionType.TType[] transactionType) {
        for (Map.Entry<String, LinkedHashSet<ActionItem>> entry : actions.entrySet()) {
            String groupName = entry.getKey();
            LinkedHashSet<ActionItem> actionItems = entry.getValue();

            for (ActionItem item : actionItems) {
                boolean groupExists = groupExists(groupName);

                if (item.ttype != null) {
                    if (item.ttype == TransactionType.TType.Skip) {
                        transactionType[0] = TransactionType.TType.Skip;
                        return SKIP;
                    }
                    if (item.ttype == TransactionType.TType.Clearing) {
                        transactionType[0] = TransactionType.TType.Clearing;
                        return doClearing(groupName, groupExists);
                    }
                }

                if (item.pgtype != null) {
                    if (item.pgtype == EntryType.Group) {
                        handleGroupEntry(item, groupName, groupExists);
                    } else if (item.pgtype == EntryType.Self) {
                        handleSelfEntry(item, groupName, groupExists);
                    }
                } else if (item.xcType != null && item.xcType == CrossCurrency.XCurrencyType.XCurrency) {
                    handleCrossCurrency(item, groupName);
                }
            }
        }
        return DO_NOT_SKIP;
    }

    private void handleGroupEntry(ActionItem item, String groupName, boolean groupExists) {
        if (isEntryStateToggle(item)) {
            if (!groupExists) {
                logGroupPersonError(EntryType.Group, groupName);
            } else {
                Hashtable<String, Person> group = findOrCreateGroup(groupName);
                Group groupObj = Utils.m_Groups.get(groupName);
                if (groupObj != null) groupObj.m_gState = new PGState(item.state);
            }
        } else if (isEntryStateAdd(item)) {
            findOrCreateGroup(groupName);
        }
    }

    private void handleSelfEntry(ActionItem item, String groupName, boolean groupExists) {
        if (isEntryStateToggle(item)) {
            if (!groupExists) {
                logGroupPersonError(EntryType.Group, groupName);
            } else {
                boolean personExists = personExists(groupName, item.name);
                if (!personExists) {
                    logGroupPersonError(EntryType.Self, item.name);
                } else {
                    Hashtable<String, Person> group = findOrCreateGroup(groupName);
                    Person person = group.get(item.name);
                    person.m_groupState = new PGState(item.state);
                }
            }
        } else if (isEntryStateAdd(item)) {
            if (!groupExists) {
                logGroupPersonError(EntryType.Group, groupName);
            } else {
                Hashtable<String, Person> group = findOrCreateGroup(groupName);
                Person person = new Person(item.name.trim(), EntryState.Add);
                group.put(item.name, person);
            }
        }
    }

    private void handleCrossCurrency(ActionItem item, String groupName) {
        for (Map.Entry<String, EnumMap<CrossCurrency.XCurrencyProperties, String>> xEntry : item.xCurrency.entrySet()) {
            EnumMap<CrossCurrency.XCurrencyProperties, String> xProperties = xEntry.getValue();
            Hashtable<String, Person> group = findOrCreateGroup(groupName);
            Group groupObj = Utils.m_Groups.get(groupName);
            if (groupObj != null) groupObj.m_ccurrency = new CrossCurrency(xProperties);
        }
    }

    public boolean doGroupAction(String action, String groupName, TransactionType.TType[] transactionType) {
        String group = groupName.isEmpty() ? Constants._DEFAULT_GROUP : groupName;
        HashMap<String, LinkedHashSet<ActionItem>> actions = GPAction.breakdownActions(action, group);
        boolean processResult = processActions(actions, transactionType);

        if (!actions.keySet().contains(groupName)) {
            System.err.println("Error: " + actions.keySet() + " missing under \"group\". Add to resolve.");
        }

        return processResult;
    }
}