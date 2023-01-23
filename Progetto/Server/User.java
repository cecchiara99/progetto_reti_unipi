package Progetto.Server;

import java.io.Serializable;
import java.util.LinkedList;

public class User implements Serializable{

    /*
        OVERVIEW: classe rappresentante di un tipico utente del social
    */
    public final String username;
    protected final String psw;
    protected final LinkedList<String> tags;
    
    public int id;

    /**
     * 
     * @param u username
     * @param p password
     * @param tag lista di tag
     * @param currentId id utente
     * @effects costruttore che inizializza i parametri della classe
     */
 
    public User(String u, String p, LinkedList<String> tag, int currentId){
        this.username = u;
        this.psw = p;
        this.tags = new LinkedList<String>();
        tags.addAll(tag);
        this.id = currentId;
    }

    /**
     * 
     * @return username dell'utente
     */

    public String getUsername(){
        return username;
    }

    /**
     * 
     * @return id utente
     */
    public int getId(){
        return id;
    }

    /**
     * 
     * @return password utente
     */
    public String getPsw(){
        return psw;
    }
    /**
     * 
     * @return lista tag utente
     */

    public LinkedList<String> getTags() {
        return tags;
    }
    
}
