package gcom.communicator;

import gcom.utils.Host;

/**
 * Created by Jonas on 2014-10-09.
 */
public class RemoteClientException extends Exception {

    Host problemClient;

    public RemoteClientException(Host problemClient) {
        this.problemClient = problemClient;
    }

    public Host getProblemClient() {
        return problemClient;
    }
}
