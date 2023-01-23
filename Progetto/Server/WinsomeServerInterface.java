package Progetto.Server;
import Progetto.Client.WinsomeClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;;

public interface WinsomeServerInterface extends Remote {
    public int register(String username, String psw, LinkedList<String> tag) throws RemoteException;
    public String regNotifiche(String u, WinsomeClientInterface objR) throws RemoteException;
    public String disattivaNotifiche(String u, WinsomeClientInterface objR) throws RemoteException;
}
