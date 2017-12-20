package jp.walkmate.tsndservice.Listener;

/**
 * Created by Hirobe on 2015/11/02.
 */
public interface TSNDConnectionListener {
    void onConnected();
    void onFailedToConnect();
}
