package Progetto.Client;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

import Progetto.Server.User;

public interface WinsomeClientInterface extends Remote {
    public void aggFollower(User u) throws RemoteException;
    public void remFollower(User u) throws RemoteException;
    public void refreshFollower(LinkedList<User> user) throws RemoteException;
}
