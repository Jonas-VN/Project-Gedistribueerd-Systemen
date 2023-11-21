package Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface BulletinBoard extends Remote {
    int getSize() throws RemoteException;
    void add(byte[] message, int index, byte[] tag) throws RemoteException, NoSuchAlgorithmException, InterruptedException;
    byte[] get(int index, byte[] tag) throws RemoteException, NoSuchAlgorithmException, InterruptedException;
    void ignoreTag(byte[] tag) throws RemoteException, NoSuchAlgorithmException, InterruptedException;
}
