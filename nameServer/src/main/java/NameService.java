import util.Host;
import util.NameServerGroupManagement;

import java.net.InetAddress;
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
        Host leader;
        if(groups.containsKey(groupName)) {
            // Group exists, returns it's leader.
            leader = groups.get(groupName);
        } else {
            // Create new group with caller as group leader.
            groups.put(groupName, newMember);
            leader = groups.get(groupName);
        }

        // USE LEADERS addMember method.

        return leader;
    }
}
