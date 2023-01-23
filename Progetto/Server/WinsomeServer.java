package Progetto.Server;

import Progetto.Client.WinsomeClientInterface;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.rmi.*;
import java.rmi.server.RemoteObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.lang.reflect.Type;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;


public class WinsomeServer extends RemoteObject implements WinsomeServerInterface{

    /**
     * OVERVIEW: classe che si occupa della gestione delle richieste sul social network da parte dei vari client
                tutti i metodi ritornano una stringa o un intero che rappresenta il risultato della
                relativa richiesta che verrà poi inviata al client 
     */

    /**
     * Struttura dati condivise 
     */

    // lista utenti registrati nella rete
    protected static final List<User> registeredUsers= new LinkedList<User>();

    //per ogni  utente -> lista post da lui creati 
    protected static final Map<String,List<Post>> utentePosts= new HashMap<String,List<Post>>();
    
    //per ogni utente > lista utenti da cui è seguito
    protected static final Map<String,List<User>> followers=new HashMap<String,List<User>>();//chi mi segue
    
    //per ogni utente -> lista utenti che segue
    protected static final Map<String,List<User>> seguiti=new HashMap<String,List<User>>();//chi seguo
    
    //per ogni utente -> oggetto remoto a lui relativo per callback (notifiche e aggiornamneto lista followers)
    protected static final Map<String,WinsomeClientInterface> regCallback = new HashMap<String,WinsomeClientInterface>();
    
    //per ogni utente -> true se è loggato attualmente nella rete, false altrimenti
    protected static final Map<String,AtomicBoolean> logged = new HashMap<String,AtomicBoolean>();
    
    //per ogni utente -> relativo portafoglio
    protected static final Map<String,Wallet> portafogli = new HashMap<String,Wallet>();


    //nome file su cui sono salvati i dati della rete sociale per serializzazione e deserializzazione
    protected static String registrati ="Progetto/Server/Files/utentiRegistrati.txt";
    protected static String log ="Progetto/Server/Files/loggati.txt";
    protected static String follower ="Progetto/Server/Files/followers.txt";
    protected static String following ="Progetto/Server/Files/following.txt";
    protected static String post = "Progetto/Server/Files/posts.txt";
    protected static String portaf = "Progetto/Server/Files/portafogli.txt";

    protected User currentUser;//utente attualmente loggato
    
    private int c;//contatore server

    /**
     * @effects costruttore che recupera ad ogni riavvio le varie informazioni delgi utenti e setta le variabili d'istanza della classe
     * @param counter numero del server instanziato (per deserializzazione)
     */
    public WinsomeServer(int counter){
        super();    
        this.c = counter;
        currentUser = null;

         //prima istanza del server creata, vanno recuperati i dati dai file
        if(c==1){
            
        
        //recupero dati
        Gson gson = new Gson();

        //registrati
        try (FileInputStream is = new FileInputStream(registrati);
        JsonReader reader = new JsonReader(new InputStreamReader(is))){

            Type registeredUse = new TypeToken<List<User>>(){}.getType();
            List<User> registra = gson.fromJson(reader, registeredUse);
            if(registra!=null){
                synchronized(registeredUsers){
                    
                            registeredUsers.addAll(registra);
                            
                        }                    
                }
             
        }catch(IOException e){
            e.printStackTrace();
            
        }

        //follower
        try (BufferedReader reader = new BufferedReader(new FileReader(follower))){
            
            Type followers = new TypeToken<Map<String, List<User>>>(){}.getType();
            Map<String,List<User>> followMap = gson.fromJson(reader, followers);
            if(followMap!=null){
                synchronized(WinsomeServer.followers){         
                    for (String user : followMap.keySet()){
                       
                        WinsomeServer.followers.put(user,followMap.get(user));
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            
        }


        //seguiti
        try (BufferedReader reader = new BufferedReader(new FileReader(following))){
            Type seguiti = new TypeToken<Map<String,List<User>>>(){}.getType();
            Map<String,List<User>> seguitiMap = gson.fromJson(reader, seguiti);
            if(seguitiMap!=null){
                synchronized(WinsomeServer.seguiti){
                    for (String user : seguitiMap.keySet()){
                        WinsomeServer.seguiti.put(user,seguitiMap.get(user));
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            
        }


        //posts

        try (BufferedReader reader = new BufferedReader(new FileReader(post))){
            Type posts = new TypeToken<Map<String,List<Post>>>(){}.getType();
            Map<String,List<Post>> postMap = gson.fromJson(reader, posts);
            if(postMap!=null){
                synchronized(WinsomeServer.utentePosts){
                    for (String user : postMap.keySet()){
                        WinsomeServer.utentePosts.put(user,postMap.get(user));
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            
        }

        //wallet

        try (BufferedReader reader = new BufferedReader(new FileReader(portaf))){
            Type p = new TypeToken<Map<String,Wallet>>(){}.getType();
            Map<String,Wallet> walletMap = gson.fromJson(reader, p);
            if(walletMap!=null){
                synchronized(WinsomeServer.portafogli){
                    for (String user : walletMap.keySet()){
                        WinsomeServer.portafogli.put(user,walletMap.get(user));
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            
        }
        //loggati
        try (BufferedReader reader = new BufferedReader(new FileReader(log))){
            Type l = new TypeToken<Map<String,AtomicBoolean>>(){}.getType();
            Map<String,AtomicBoolean> loggedMap = gson.fromJson(reader, l);
            if(loggedMap!=null){
                synchronized(WinsomeServer.logged){
                    for (String user : loggedMap.keySet()){
                        WinsomeServer.logged.put(user,loggedMap.get(user));
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            
        }
    }else{
        return;
    }
       

    } 
    
    /**
     * @effects restituisce true se l'utente è registrato, false altrimenti
     * @param username
     */
    protected static synchronized boolean getByNameUser (String username){
        boolean tmp = false;
        for (User user : registeredUsers) {
            if(user.getUsername().equals(username))
                return tmp = true;
        }
        return tmp;//utente inesistente
    }

    

    /**
     * 
     * @return true se l'aggiornamento della memoria è andato a buon fine, false altrimenti
     */
     public synchronized static boolean updateData(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //salvatagio utenti registrati
        try(BufferedWriter w= new BufferedWriter(new PrintWriter(new FileWriter(registrati)))){
            
            Type registered = new TypeToken<List<User>>(){}.getType();
            String users = gson.toJson(registeredUsers,registered);
            w.write(users);
            w.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        //salvataggio follower
        try(BufferedWriter w = new BufferedWriter(new PrintWriter(new FileWriter(follower)))){

            Type followers = new TypeToken<Map<String,List<User>>>(){}.getType();
            String f = gson.toJson(WinsomeServer.followers,followers);
            w.write(f);
            w.flush();
        }catch(IOException e){
            e.printStackTrace();
        }


        //salvataggio following
        try(BufferedWriter w = new BufferedWriter(new PrintWriter(new FileWriter(following)))){

            Type following = new TypeToken<Map<String,List<User>>>(){}.getType();
            String f = gson.toJson(WinsomeServer.seguiti,following);
            w.write(f);
            w.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        //salvataggio post

        try(BufferedWriter w = new BufferedWriter(new PrintWriter(new FileWriter(post)))){

            Type postsType = new TypeToken<Map<String,List<Post>>>(){}.getType();
            String f = gson.toJson(WinsomeServer.utentePosts,postsType);
            w.write(f);
            w.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        //salvataggio portafoglio
        try(BufferedWriter w = new BufferedWriter(new PrintWriter(new FileWriter(portaf)))){

            Type wallType = new TypeToken<Map<String,Wallet>>(){}.getType();
            String f = gson.toJson(WinsomeServer.portafogli,wallType);
            w.write(f);
            w.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        //salvataggio loggati
        try(BufferedWriter w = new BufferedWriter(new PrintWriter(new FileWriter(log)))){

            Type logType = new TypeToken<Map<String,AtomicBoolean>>(){}.getType();
            String f = gson.toJson(WinsomeServer.logged,logType);
            w.write(f);
            w.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
       



       return true;
    } 

    /**
     * 
     * @return ultimo id relativo ai post
     */

    protected synchronized int lastPost(){
        int max = -1;
        if(!utentePosts.isEmpty()){
            for (String user : utentePosts.keySet()) {
                for (Post p : utentePosts.get(user)) {
                    int id = p.getID();
                    if(max < id){
                        max = id;
                    }
                }
            }
        }
        return max;
    }

    /**
     * 
     * @return ultimo id relativo agli utenti
     */

    protected synchronized int lastUser(){
        int max = -1;
        if(!registeredUsers.isEmpty()){
            for (User user : registeredUsers) {
                int i = user.getId();
                if(i > max){
                   max = i; 
                }
            }
        }
        return max;
    }

    
/**
 * 
 * @param u nome dell'utente di cui si vuole ottenere l'oeggteo remoto (per follow/unfollow  e refresh follower al login)
 * @return oggetto remoto relativo al client o null se il cliente non  èancora registrato alla callback
 */
    protected synchronized WinsomeClientInterface getRemote(String u){
        if(!regCallback.containsKey(u)){
            return null;
        }
        return regCallback.get(u);
    }
    
    /**
     * 
     * @param idU del'utente da ottenere
     * @return l'utente richiesto o null se l'utente non è presnete nella lista degli utenti registrati
     */
    protected synchronized User getUserById (int idU){
        for (User user : registeredUsers) {
            if(user.getId() == idU) return user;
        }
        return null;

    }
/**
 * 
 * @param u username dell'utente da ottenere
 * @return l'utente richiesto o null se quest'ultimo non è presente nella lista dei registrati
 */
    protected synchronized User getUser(String u){
        for(User us : registeredUsers){
            if(us.getUsername().equals(u))
                return us;
        }
        return null;
    }

    /**
     * 
     * @param list dove cercare
     * @param username username dell'utente da cercare
     * @return true se l'utente è stato trovato false altrimenti
     */

    protected synchronized boolean searchUser(List<User> list, String username){
        for(User u : list){
            if(u.getUsername().equals(username))
                return true;
        }
        return false;
    }

    
    /**
     * @effect registra l'utente alla call back per ricevere notifiche riguardo l'aggiornamento dei follower
     * @param remObj oggetto remoto client
     * @param u nome utente di cui attivare le notifiche
     * @throws RemoteException se c'è un errore causato da RMI
     */

    public synchronized String regNotifiche(String u, WinsomeClientInterface remObj) throws RemoteException{
        String response = "";
        
        if (getByNameUser(u) && remObj != null){
            
            if(!(regCallback.keySet().contains(u))){
                regCallback.put(u,remObj);
            }
            else return response = "utente gia' registrato alle notifiche";

            try{
                regCallback.get(u).refreshFollower(new LinkedList<User>(followers.get(u)));
            }catch(RemoteException e){
                e.printStackTrace();
            }
            response = "registrazione alle notifiche avvenuta con successo";
            
        } else{
            response = "impossibile registrarsi alle notifiche perchè l'utente non e' registrato sulla rete";
        }  
        return response;
    }

    /**
     * @effect deregistra l'utente alla call back per ricevere notifiche riguardo l'aggiornamento dei follower
     * @param remObj oggetto remoto client
     * @param u nome utente di cui attivare le notifiche
     * @throws RemoteException se c'è un errore causato da RMI
     */
    
    public synchronized String disattivaNotifiche(String u, WinsomeClientInterface remObj){
        String response = "";
        if(getByNameUser(u) && remObj != null){
            
            if(!regCallback.keySet().contains(u)){
                 response = "l'utente non era registrato alle notifiche"; //l'utente non era registrato alle notifiche
            }
            else {
                regCallback.remove(u);
                 response = "disattivazione notifiche avvenuta con successo" ;
            }
        }
         else  response = "l'utente non è registrato sulla rete";//l'utente non è registrato nella rete
        return response;
    }

    /**
        *   @effect registra l'utente alla rete sociale (da remoto) e inizializza i relativi campi delle strutture dati a lui destinate
        *   @return 0 -> se la registrazione è andata buon fine
                1 -> se l'usernam escelto è già utilizzato
                2 -> se i parametri sono insìesistenti -> non dovrebbe mai succedere
                3 -> se c'è già un utente loggato sulla macchina -> non dovrebbe mai succedere 
        *   @param username
        *   @param psw
        *   @param tags lista dei tag
        *   @throws RemoteException se c'è un errore causato da RMI
     */ 

    public synchronized int register(String username, String psw, LinkedList<String> tags) throws RemoteException{
        
        if(currentUser!=null){
            return 3;
        }
        
        
        if(username == null || psw == null || tags == null || tags.isEmpty() || username.length()<3|| username.length()>20 || psw.length()<3 || psw.length()>20 || tags.size()>5){
            return 2;
        }
        
        User tmp = new User(username, psw, tags, lastUser()+1);
        for (User u : registeredUsers) {
            if(u.username.equals(username))
                return 1;
        }
        
        registeredUsers.add(tmp);
        utentePosts.put(tmp.getUsername(), new LinkedList<Post>());
        followers.put(tmp.getUsername(),new LinkedList<User>());//value -> chi mi segue
        seguiti.put(tmp.getUsername(),new LinkedList<User>());//value -> chi seguo
        logged.put(tmp.getUsername(), new AtomicBoolean(false));
        portafogli.put(tmp.getUsername(), new Wallet());
        
        return 0;
    }

    /**
     * @effects : permette all'utenteregistrato di identificarsi 
     * @param username
     * @param passw
     * @return messaggio risultato operazione
     */

    public synchronized String login(String username, String passw){
        String response;
        if(username == null || passw== null){
            response = "4/parametri inesistenti";
            
        }
        if(getByNameUser(username)){
        
                if(logged.get(username).get()==true || currentUser!= null){
                    response = "3/utente gia' loggato";
                    return response;
                    
                }
            
                
            if(getUser(username).getPsw().equals(passw)){
                logged.get(username).set(true);
                currentUser = getUser(username);
                response = "0/utente loggato correttamente";
                
            } else{
                response = "2/password errata";
                
            } 
        
        }else {
            response = "1/utente non registrato";
        }
        return response;    
    }

    /**
     * @effects logout dalla rete sociale
     * @param username
     * @return messaggio risultato operazione
     */

    public synchronized String logout (String username){
        String response;
        if(getByNameUser(username)){
            
            if(logged.get(username).get() == false || currentUser==null) {
                response = "utente non connesso";
            return response;
            }
            logged.get(username).set(false);
            currentUser = null;
            response ="utente unloggato correttamente";
            
            return response;
        }else {
            response = "utente non registrato";
            
            return response;
        }
    }
    /**
     * 
     * @return lista utenti con almeno un tag in comune con l'utente
     */

    public synchronized String listUsers(){
        
        LinkedList<User> res = new LinkedList<User>();
        for (String tag : currentUser.getTags()) {
            for(User user : registeredUsers){
                if(user.username.equals(currentUser.username))
                    continue;
                for (String tag2 : user.getTags()) {
                    if(tag2.equals(tag)) {
                        res.add(user);
                        break;
                    }
                    
                }
            }
        }
        String msg = "Lista utenti:";
        if(res != null)
            for(User u : res){
                msg = msg +"|\t"+ u.username + " tags: "+u.tags.toString()+" | ";
            }
        
        return msg;
    }

    /**
     * 
     * @return lista degli utenti che l'utente segue
     */
    public synchronized String listFollowing(){
        
        LinkedList<User> tmp = new LinkedList<User>();
        tmp.addAll(seguiti.get(currentUser.getUsername()));
        String msg = "lista following:";
        if(tmp != null)
            for(User u : tmp){
                msg = msg +"|\t"+ u.getUsername();
            }
        
        return msg;    
    }
    /**
     * @effects aggiunge l'utente alla lista delgi utenti seguiti dall'utente
     * @param idUser dell'utente da seguire
     * @return messaggio risultato operazione
     */
    
    public synchronized String followUser(int idUser){
        String response = "";
        
        User tmp = getUserById(idUser);
        if(tmp != null ){
            followers.get(tmp.getUsername()).add(currentUser);
            seguiti.get(currentUser.getUsername()).add(tmp);
            response = "following eseguito con successo";
            
            return response;
        }
        response = "utente inesistente o segui gia' l'utente";
        
        return response;
    }
    /**
     * @effects rimuove l'utente specificato dalla lista dei seguiti dell'utente corrente
     * @param idUser dell'utente da non seguire più
     * @return messaggio risultato dell'operazione
     */

    public synchronized String unfollowUser(int idUser){
        String response = "";
        
        User tmp = getUserById(idUser);
        if(tmp!=null){
            if(followers.get(tmp.getUsername()).contains(currentUser))
                followers.get(tmp.getUsername()).remove(currentUser);
                
                seguiti.get(currentUser.getUsername()).remove(tmp);
                
                response ="unfollowing eseguito con successo";
                
                return response;
        }
        response ="utente inesistente";
            
        return response;
    }

    /**
     * 
     * @return lista post creati dall'utente corrente
     */

    public synchronized String viewBlog(){
        
            List<Post> tmp = new LinkedList<Post>();
            tmp.addAll((List<Post>) utentePosts.get(currentUser.getUsername()));
            String response = "Blog: ";
            for(Post p : tmp){
                response = response + " | iD Post: " + p.getID() +" Autore: " + p.getAutore().getUsername() + 
                " Titolo: " + p.getTitolo() + "|\t";
            }            
            return response;
        
        
    }
    /**
     * @effcts: aggiunge il post alla lista dei post dell'utente
     * @param titolo del post, terminato da ";"
     * @param contenuto del post
     * @return messaggio risultato dell'operazione
     */

    public synchronized String createPost(String titolo, String contenuto){
        String response = "";
        Post toAdd = new Post(currentUser,titolo , contenuto, null, lastPost()+1);
        utentePosts.get(currentUser.getUsername()).add(toAdd);
        response="post creato correttamente";
        return response;
    }
    /**
     * 
     * @return lista post creati dagli utenti che l'utente corrente segue
     */

    public synchronized String showFeed(){
        
        String response ="Feed: ";
        LinkedList<Post> feed = new LinkedList<Post>();
        for (User follower : seguiti.get(currentUser.getUsername())) {
            for (Post post : utentePosts.get(follower.getUsername())) {
                feed.add(post);
            }
        }
        
        for(Post p : feed){
            response = response + " | iDPost: " + p.getID() +" Autore: " + p.getAutore().getUsername() + 
            " Titolo: " + p.getTitolo() + "|\t";
        }

      
        
        return response;
    }

    /**
     * 
     * @param idPost del postda visualizzare
     * @return post richiesto
     */

    public synchronized String showPost(int idPost){
        String response = "";
        for (String user : utentePosts.keySet()) {
            String comm="";
            int i = 0;
            for (Post post : utentePosts.get(user)) {
                if(post.getID() == idPost){
                    LinkedList<Commenti> commenti = post.getCommenti();
                    if(commenti != null){
                        
                        for(Commenti c : commenti){
                            if(i==0){
                                comm = comm + c.getAutoreC().username+": "+ c.getTestoCommento()+"/";
                            }
                            else{
                                comm = comm + " |  | "+c.getTestoCommento()+"/";
                            }
                            i++;
                        }
                        comm = comm+"/";
                    }
                    response = 
                        "\t |Data e ora  | "+post.getTimestamp() +
                        "\t | iD         | "+post.getID() +
                        "\t | Autore     | "+post.getAutore().username+
                        "\t | Titolo     | "+post.getTitolo() +
                        "\t | Testo      | "+post.getTesto() +
                        "\t | Likes      | "+post.getLikes() +
                        "\t | Dislikes   | "+post.getDislike() +
                        "\t | Commenti   | "+comm
                    
                    ;
                    
                    return response;
                }
            }
        }
        response = "post non trovato";
        
        return response;
    }

    /**
     * @effects rimuove il post richiesto se si è l'autore del post e se quest'ultimo esiste
     * @param idPost del post da rimuovere
     * @return messaggio risultato dell'operazione
     */

    public synchronized String deletePost(int idPost){
       
        String response = "";
        for (String user : utentePosts.keySet()) {
            for (Post post : utentePosts.get(user)) {
                if(post.getID() == idPost && currentUser.getUsername().equals(post.getAutore().getUsername())){
                    utentePosts.get(user).remove(post);
                    response = "post cancellato correttamente";
                    
                    return response;
                }
                    
            }
        }
        response ="post non trovato";
            
            return response;
    }

    /**
     * @effects rewin del post richiesto se non si è l'autore del post e il post esiste e l'utente segue l'utente autore del post da condividere
     * @param idPost del post da condividere
     * @return messaggio risultato dell'operazione
     */

    public synchronized String rewinPost(int idPost){
        String response="";
        for (String user : utentePosts.keySet()) {
            for (Post post : utentePosts.get(user)) {
                if(post.getID() == idPost && seguiti.get(currentUser.getUsername()).contains(post.getAutore())){
                    Post x = post.getCopia(post, lastPost()+1);
                    x.autore = currentUser;
                    utentePosts.get(currentUser.getUsername()).add(x);
                    response = "post condiviso correttamente";
                    return response;
                }
            }
        }
        response = "post non trovato";
        return response;
    }
    /**
     * @effect aggiunge il commento al post richiesto se l'utente segue l'autore del post e
     *          l'utente corrente non è l'autore del post
     * @param idPost a cui aggiungere il commento
     * @param comm da aggiungere
     * @return messaggio risultato dell'operazione
     */

    public synchronized String addComment(int idPost, String comm){
        String response = "";
        for (String user : utentePosts.keySet()) {
            for (Post post : utentePosts.get(user)) {
                
                if(post.getID() == idPost  && searchUser(seguiti.get(currentUser.getUsername()), post.getAutore().getUsername()) ){
                    if( post.addCommento(comm, currentUser)){
                        response = "commento aggiunto correttamente";
                        return response;
                    }else{
                        response = "non puoi commentare un tuo post";
                        return response;
                    }
                    
                    
                }
                    
            }
        }
        response = "non segui l'autore del post o post inesistente";
        
            return response;
    }

    /***
     * @effects si aggiunge un like o dislike al post selezionato se non si è l'autore del post e si segue l'autore del post da votare e se il post esiste
     * @param idPostda votare
     * @param voto 1 se voto positivo; -1 se voto negativo
     * @return messaggio risultato dell'operazione
     */

    public synchronized String rate(int idPost, int voto){
        String response = "";
        for (String user : utentePosts.keySet()) {
            for (Post post : utentePosts.get(user)) {
                if(post.getID() == idPost && post.getAutore() != currentUser &&  searchUser(seguiti.get(currentUser.getUsername()), post.getAutore().getUsername()) ){
                    if(voto == 1 && post.addLike(currentUser) ){
                        response = "like aggiunto correttamente";
            
                        return response;
                    }

                    if(voto == -1 && post.addDislike(currentUser))
                    {
                        response="dislike aggiunto correttamente";
            
                        return response;
                    }
                }else{
                    response = "non puoi votare il tuo stesso post o non segui l'autore del post";
                    return response;
                }
            }
        }
            response = "post non trovato o voto gia' aggiunto";
            
            return response;
    }
    /**
     * 
     * @param u username utente di cui ottenere il portafoglio
     * @return bilancio portafoglio e cronologia transazioni dell'utente richiesto
     */

    public synchronized String getWallet(String u){
        String response = "";
        String s ="";
        
        Wallet w = portafogli.get(u);
        LinkedList<java.sql.Timestamp> list = w.getHistoryWin();
        for (java.sql.Timestamp time : list) {
            s = s + " /\t" + time.toString();
        }
        response = "Balance: "+w.getBalanceWin()+" Storia: \t"+ s;

        return response;
    }

    /**
     * @effects converte il saldo in bit coin aggiungendo la relativa transazione alla cronoogia e azzera saldo wincoin 
     * @return valore bitcoin convertiti
     */

    public synchronized double getWalletInBitcoin(){
        double saldo = portafogli.get(currentUser.getUsername()).getBalanceWin();
        
        double btc = -1;
        try{
            URL u = new URL("https://www.random.org/integers/?num=1&min=1&max=50&col=1&base=10&format=plain&rnd=new");
            URLConnection uc = u.openConnection();
            InputStream raw = uc.getInputStream();
            int rate = raw.read();
            btc = rate * saldo;
            Wallet w = portafogli.get(currentUser.getUsername());
            w.addTransactionBtc(btc, new java.sql.Timestamp(System.currentTimeMillis()));
            w.setBalance(0);

        
        }catch(MalformedURLException e){
            e.printStackTrace();

        }catch(IOException e){
            e.printStackTrace();
            
        }

        return btc;

    }

    
}
 
