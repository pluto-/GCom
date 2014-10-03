import util.Host;
import util.NameServerGroupManagement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class NameService implements NameServerGroupManagement {

    Map<String, Host> groups;

    public NameService(/* port, address etc */) {
        groups = new HashMap<String, Host>();
    }

    public Host joinGroup(Host newMember, String groupName) {
        if(!groups.containsKey(groupName)) {
            groups.put(groupName, newMember);
        }
        Host leader = groups.get(groupName);

        // USE LEADERS addMember method.

        return leader;
    }
}
