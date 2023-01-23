package Progetto.Server;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.*;





public class CalcoloRicompense implements Runnable{

    /**
     * OVERVIEW: classe che effettu ail calcolo periodico delle ricompense e invia a tutti coloro iscritti al gruppo multicast un
     * messaggio di possibile aggiornamento del portafoglio
     */
    //oggetto che mi permette di accedere alle struttura dati per il calcolo
    protected WinsomeServer ws  = null;

    //indirizzo multicast
    protected String IP_MC = null;

    //porta multlicast
    protected int PORT_MC =0;

    //timeout per il calcolo periodico
    protected int TIME = 0;

    //percentuale ricompense
    protected int P = 0;

    //marca temporale dell'ultimo calcolo effettuato
    protected Timestamp lastCheck = null;

    //socket per l'invio del pacchetto per il multicast
    protected DatagramSocket socketMc = null;
    

    /***
     * 
     * @effects: costruttore che inizializza la classe
     */
    public CalcoloRicompense(WinsomeServer w,String IP, int port, int time, int percentuale, Timestamp l){
        this.ws = w;
        this.IP_MC = IP;
        this.PORT_MC = port;
        this.TIME = time;
        this.P = percentuale;
        this.lastCheck = l;
    }

    /**
     * @effects: invia il messaggio di un possibile aggiornamento del portafoglio a tutti coloro che sono iscritti al gruppo multilcast sotto forma di pacchetto UDP
     * @param msg messaggio che deve essere  inviato agli utent iscritti al mutlicast
     * @param socket su cui inviare il messaggio di aggiornamento
     */

    public void sendMSG(String msg, DatagramSocket socket){
        byte [] byteMsg = msg.getBytes();
        try{
            DatagramPacket packet = new DatagramPacket(byteMsg, byteMsg.length, InetAddress.getByName(IP_MC), PORT_MC);
            socket.send(packet);
        }catch(IOException  e){
            e.printStackTrace();
        }
    }

    public void run(){

        //struttura dati che memorizza per ogni post coloro che lo hanno recentemente votato
        
        HashMap<Post,LinkedList<Voti>> curatoriVoti = new HashMap<Post,LinkedList<Voti>>();
        
        //struttura dati che memorizza per ogni post chi lo ha commentato recentemnte e quali commenti 
        HashMap<Post,HashMap<User,LinkedList<Commenti>>> numComm = new HashMap<Post,HashMap<User,LinkedList<Commenti>>>();


        //struttura dati che memorizza i post che devono essere calcolati
        LinkedList<Post> postsToCheck = new LinkedList<Post>();
        
        //lista dei vari curatori a cui aggiornare il portafoglio
        LinkedList<User> CuratoriToAdd = new LinkedList<User>();
        try{

            socketMc = new DatagramSocket();
            socketMc.setReuseAddress(true);
        
        }catch(IOException e){
            e.printStackTrace();
        }

        //finchè il server è in esecuzione si cila
        while(!MainServer.exit.get()){
            
        synchronized(ws){
            
            //per ogni utente che ha publicato un post si controlla, per ogni pst se c'è un voto o un commento recente
            //e si aggiunge alla lista di post da controllare e i vari voti e commenti nelle apposite strutture dati per il calcolo
            for (String u : WinsomeServer.utentePosts.keySet()) {
                for (Post post : WinsomeServer.utentePosts.get(u)) {

                    if(!post.getVotanti().isEmpty()){
                        for (Voti voto : post.voti) {
                            if(voto.getTimeVoto().compareTo(lastCheck)>0){
                                if(!curatoriVoti.keySet().contains(post)){
                                    curatoriVoti.put(post, new LinkedList<Voti>());
                                    curatoriVoti.get(post).add(voto);
                                }else{
                                    curatoriVoti.get(post).add(voto);
                                }
                                postsToCheck.add(post);
                            }
                        }
                    }

                    if(!post.comm.isEmpty()){
                        for (Commenti c : post.comm) {
                            if(c.getTimeCommento().compareTo(lastCheck)>0){
                                if(numComm.keySet().contains(post)){
                                    if(!numComm.get(post).containsKey(c.getAutoreC())){
                                        numComm.get(post).put(c.getAutoreC(), new LinkedList<Commenti>());
                                        numComm.get(post).get(c.getAutoreC()).add(c);
                                    }else{
                                        numComm.get(post).get(c.getAutoreC()).add(c);
                                        
                                    }
                                }
                                else{
                                    numComm.put(post, new HashMap<User, LinkedList<Commenti>>());
                                    numComm.get(post).put(c.getAutoreC(), new LinkedList<Commenti>());
                                    numComm.get(post).get(c.getAutoreC()).add(c);

                                }
                                
                                if(!postsToCheck.contains(post)){
                                    postsToCheck.add(post);
                                }
                                
                            }
                        }
                    }
                    
                    
                    post.setLastIter();

                    
                }

            }

            
            
            double Log1 = 0;
            int sommLikes = 0;
            int maxLog1 = 0;
            
            double sommLog2 = 0;
            double Log2 = 0;
            int curatori = 0;


            //calcolo ricompense

            for (Post p :  postsToCheck) {
               
                if(curatoriVoti.get(p)!=null){
                    for (Voti voto : curatoriVoti.get(p)) {
                        sommLikes+=voto.getVal();
                        curatori++;
                    }
     
                    
                    if(maxLog1<sommLikes){
                        maxLog1 = sommLikes;
                    }
                    
                    
                
                    for (Voti voto : curatoriVoti.get(p)) {
                        if(!CuratoriToAdd.contains(voto.getAutore()))
                        CuratoriToAdd.add(voto.getAutore());
                    }  
                }
              
               

               Log1=Math.log(maxLog1+1);

               HashMap<User,LinkedList<Commenti>> tmp = numComm.get(p);
               if(tmp!= null){
                for (User u : tmp.keySet()) {
                    sommLog2 += (2/(1+Math.exp(-(tmp.get(u).size()-1))));
                    curatori++;
                }

                for (User u : numComm.get(p).keySet()) {
                    if(!CuratoriToAdd.contains(u))
                        CuratoriToAdd.add(u);   
                }  
                
                
               }
               
               Log2 = Math.log(sommLog2+1);
               double guadagno = (Log1+Log2)/p.lastIter;

               double ricompensaAutore = (guadagno*P)/100;
               double ricompensaCuratore = (guadagno-ricompensaAutore)/curatori;

               //aggiornamento del portafoglio dell'autore del post
               WinsomeServer.portafogli.get(p.getAutore().getUsername()).addTransactionWin( ricompensaAutore, lastCheck);

               //aggiornamento dei portafogli dei curatori del post
               for (User user : CuratoriToAdd) {
                   WinsomeServer.portafogli.get(user.getUsername()).addTransactionWin(ricompensaCuratore, lastCheck) ;
               }
               
               //si setta nuova marca temporale per il prossimo calcolo
               lastCheck = new Timestamp(System.currentTimeMillis());
               

               //invio messaggio agli utenti iscritti al multicast
                sendMSG("\n>>> PORTAFOGLI AGGIORNATI <<<\n", socketMc);
               

            }
            
            //reset delle strutture dati per un nuovo calcolo
            curatoriVoti.clear();
            numComm.clear();
            postsToCheck.clear();
            CuratoriToAdd.clear();
            
        }
            //attesa 
            try{
                Thread.sleep(TIME);
            }catch(InterruptedException e){
               
                System.out.println("\n> calcolo ricompense interrotto\n");
            }
        
    }
        
        //chiusura multicast
        socketMc.close();
     
    
    return;
    }
}
