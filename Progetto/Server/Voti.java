package Progetto.Server;

import java.io.Serializable;
import java.sql.Timestamp;

public class Voti implements Serializable{
    /**
     * OVERVIEW: classe che rappresneta un voto all'interno di un post
     */
    protected final int value;
    protected final Timestamp timeV;
    protected final User autoreVoto;
    /**
     * @effects costruttore che inizializza i campi della classe
     * @param val 1 se like; -1 se dislike
     * @param user utente ch eeffettua la votazione
     */

    public Voti(int val, User user){
        this.value=val;
        this.timeV = new java.sql.Timestamp(System.currentTimeMillis());
        this.autoreVoto = user;
    }

    /**
     * 
     * @return data e ora voto
     */

    public Timestamp getTimeVoto(){
        return this.timeV;
    }
    /**
     * 
     * @return 1 se il voto è un like, -1 se il voto è un dislike
     */

    public int getVal(){
        return this.value;
    }
    /**
     * 
     * @return utente autore del voto
     */

    public User getAutore(){
        return this.autoreVoto;
    }
}
