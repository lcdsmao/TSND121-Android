package jp.walkmate.tsndservice.Thread;

import jp.walkmate.tsndservice.Listener.TSNDConnectionListener;
import jp.walkmate.tsndservice.Service.TSNDService;

/**
 * Created by Hirobe on 2015/11/02.
 */
public class TSNDConnectionThread extends Thread{
    TSNDService TSNDService;
    TSNDConnectionListener connectionListener = null;

    boolean isConnected = false;
    boolean isTaskCompleted = false;

    public TSNDConnectionThread(TSNDService TSNDService){
        this.TSNDService = TSNDService;
        this.isTaskCompleted = false;
        this.isConnected = false;
    }

    @Override
    public void run(){
        isConnected = TSNDService.connect();

        isTaskCompleted =true;

        if(connectionListener != null){
            if(isConnected) {
                connectionListener.onConnected();
            }else{
                connectionListener.onFailedToConnect();
            }
        }
    }

    public void setConnectionListener(TSNDConnectionListener connectionListener){
        this.connectionListener = connectionListener;
    }

    public boolean isConnected(){
        return isConnected;
    }

    public boolean isTaskCompleted(){
        return isTaskCompleted;
    }
}
