package Progetto.Server;
import java.sql.Timestamp;
import java.util.*;



import java.io.Serializable;


public class Post implements Serializable{

    /**
     * OVERVIEW: classe rapprresenta un post all'interno della rete sociale
     */
    
    protected final int idPost;
    protected User autore;
    protected final String titolo;
    protected final String testo;
    protected LinkedList<Commenti> comm;
    protected LinkedList<Voti> voti;
    protected final Timestamp timestamp;
    protected int like;
    protected int dislike;
    protected Post rewinned;
    protected int lastIter;

    /**
     * 
     * @param aut utente autore del post
     * @param tit titolo del post
     * @param testo contenuto del post
     * @param rewin null se non è un post ricondiviso altrimenti il riferimento del post rewinnato
     * @param id id del post da creare
     * @effects inizializza i campi della classe
     */

    public Post(User aut, String tit, String testo, Post rewin, int id){
        if(rewin != null){
            this.rewinned = rewin;
        }
        else rewinned = null;

        this.autore = aut;
        this.titolo = tit;
        this.testo = testo;
        this.timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        this.idPost = id;
        this.comm = new LinkedList<Commenti>();
        this.like = 0;
        this.dislike = 0;
        this.voti = new LinkedList<Voti>();
      
    }


    /**
     * 
     * @param u utente che ha messo like
     * @return true se l'aggiunta del like è andata a buon fine, false altrimenti
     */

    public boolean addLike(User u){
        List<User> list = getVotanti();
        if(!list.contains(u)){
            voti.add(new Voti(1, u));
            like++;
            return true;
        }
        return false;
        
    }

    /**
     * @effect: aggiunge un'iterazione al post per il calcolo delle ricompense
     */

    public synchronized void setLastIter(){
        lastIter++;
    }
    

    
    /**
     * 
     * @param u utente che ha messo dislike
     * @return true se l'aggiunta del dislike è andata a buon fine, false altrimenti
     */
    public boolean addDislike(User u){
        List<User> list = getVotanti();
        if(!list.contains(u)){
            voti.add(new Voti(-1, u));
            dislike++;
            return true;
        }
        return false;
    }

    /**
     * 
     * @return numero like
     */

    public int getLikes(){
        return like;
    }

    /**
     * 
     * @return numero dislike
     */

    public int getDislike(){
        return dislike;
    }
    /**
     * 
     * @return utente autore del post
     */

    public User getAutore(){
        return autore;
    }
    /**
     * 
     * @return titolo del post
     */

    public String getTitolo(){
        return titolo;
    }


    /**
     * 
     * @return contenuto del post
     */

    public String getTesto(){
        return testo;
    }
    /**
     * 
     * @return il post ricondiviso (null se non lo è)
     */

    public Post getRewinPost(){
        return rewinned;
    }

    /**
     * 
     * @return data e ora del post
     */

    public Timestamp getTimestamp(){
        return timestamp;
    }

    /**
     * 
     * @return lista commenti del post
     */

    public LinkedList<Commenti> getCommenti(){
        return comm;
    }
    /**
     * 
     * @return id del post
     */

    public int getID(){
        return idPost;
    }
    
    /**
     * @effects aggiunge un commento alla lista di commenti del post
     * @param commento da aggiungere al post
     * @param u utente autore del commento
     * @return true se l'aggiunta del commento è andata a buon fine, false altrimenti
     */

    public boolean addCommento(String commento,User u){
    Commenti tmp = new Commenti(commento, u);
    if(!comm.contains(tmp) && !u.getUsername().equals(autore.getUsername())){
        comm.add(new Commenti(commento,u));
        return true;
        }
        return false;
    }


    /**
     * 
     * @return lista utenti che hanno votato il post
     */
    public LinkedList<User> getVotanti(){
        LinkedList<User> votanti = new LinkedList<User>();
        for (Voti voto : voti) {
            votanti.add(voto.getAutore());
        }
        return votanti;
    }

    /**
     * 
     * @param p post da copiare
     * @param currentIdPost id da assegnare al post copia
     * @return il post copiato
     */

    public Post getCopia(Post p, int currentIdPost){
        Post x = new Post(autore,titolo,testo,rewinned, currentIdPost);
        x.comm = p.getCommenti();
        x.dislike = p.dislike;
        x.like = p.like;
        x.voti = p.voti;
        return x;
    }
    /**
     * 
     * @return lista utentei che hanno commentato il post
     */

    public LinkedList<User> getCommentatori(){
        LinkedList<User> chiComm = new LinkedList<User>();
        for (Commenti c : comm) {
            chiComm.add(c.getAutoreC());
        }
        return chiComm;
    }
}


