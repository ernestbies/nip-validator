package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Ernest Bieś, PWSZ Tarnów 2020
 */
public interface NIPValidator extends Remote {
    public boolean validate(String nip) throws RemoteException;
}
