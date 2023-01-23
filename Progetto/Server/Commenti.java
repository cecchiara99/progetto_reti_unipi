package Progetto.Server;

import java.io.Serializable;
import java.sql.Timestamp;

public class Commenti implements Serializable{
    /**
     * OVERVIEW: classe che rappresneta un commento all'interno di un post
     */
    protected final String commento;
    protected final Timestamp time;
    protected final User autoreComm;

    /**
     * @effects costruttore che inizializza i campi della classe
     * @param commento del post
     * @param user autore del commento
     */
    public Commenti(String commento, User user){
        this.commento = commento;
        this.time = new java.sql.Timestamp(System.currentTimeMillis());
        this.autoreComm = user;
    }
    /**
     * 
     * @return data e ora commento
     */

    public Timestamp getTimeCommento(){
        return this.time;
    }
    /**
     * 
     * @return testo del commento
     */

    public String getTestoCommento(){
        return this.commento;
    }
    /**
     * 
     * @return utente autore del commento
     */

    public User getAutoreC(){
        return this.autoreComm;
    }
}