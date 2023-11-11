package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote {
    public abstract void add(int index, String value, int tag) throws RemoteException;
    public abstract String get(int index, int tag) throws RemoteException;
}
