package Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface BulletinBoard extends Remote {
    int getSize() throws RemoteException;
    void add(byte[] message, int index, byte[] tag) throws RemoteException, NoSuchAlgorithmException;
    byte[] get(int index, byte[] tag) throws RemoteException, NoSuchAlgorithmException, InterruptedException;
}
