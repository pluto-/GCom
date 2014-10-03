package groupmanager;

import transfer.Host;

/**
 * Created by Patrik on 2014-10-03.
 */
public class Group {

    public Host leader;
    public String groupName;
    //TODO kanske lägga till multicast mode och message ordering status här


    public Host getLeader() {
        return leader;
    }

    public void setLeader(Host leader) {
        this.leader = leader;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
