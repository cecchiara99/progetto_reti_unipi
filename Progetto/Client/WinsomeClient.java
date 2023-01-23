package Progetto.Client;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import java.util.*;


import Progetto.Server.User;

public class WinsomeClient extends RemoteObject implements WinsomeClientInterface {

    /**
     * OVERVIEW: classe che i implementa i metodi remoti lato client e gestisce la lista dei followers
     */

     //lista dei followers
    protected LinkedList<String> followers;

    //costruttore che inizializza i campi della classe
    public WinsomeClient(){
        followers = new LinkedList<String>();
    }

    /**
     * @effects aggiunge l'utente alla lista locale di followers
     * @throws RemoteException errore dovuto a RMI
     */

    public synchronized void aggFollower(User u) throws RemoteException{
        
        if (u == null){
           return;
        }

        if(followers.contains(u.username)){
            return;
        }

        followers.add(u.username);
        System.out.print("\n> Nuovo follower: "+u.username+"\n< ");
    }

    /**
     * @effects rimuove l'utente dalla lista locale di followers
     * @throws RemoteException errore dovuto a RMI
     */

    public synchronized void remFollower(User u) throws RemoteException{
        
        if(followers.remove(u.username))
            System.out.print("\n> Follower perso: "+u.username+"\n< ");
        
        return;
        
    }

    /**
     * 
     * @effects aggiornamneto dei follower al nuovo login
     * @throws RemoteException errore dovuto a RMI
     */

    public synchronized void refreshFollower(LinkedList<User> users) throws RemoteException{
        if(users!=null)
        followers = new LinkedList<String>();
        for (User user : users) {
            followers.add(user.username);
        }
    } 

    /**
     * @return stampa lista followers dell'utente
     */

    public synchronized void listFollowers(){
        System.out.print("> Lista followers: ");
        for (String user : followers) {
            System.out.println(user + "\n");
        }
    }
}
