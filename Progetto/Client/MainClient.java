package Progetto.Client;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import Progetto.Server.WinsomeServerInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.net.Socket;

public class MainClient{

    /**
     * OVERVIEW: classe che gestisce l'esecuzione di un client, inizialmente avviene la fase di registrazione e/o login, se quest anon avviene
     * il client termina e non è possibile interagine nella rete sociale + iscrizione al gruppo multicast pe ricevere messaggi di aggiornamento portafoglio 
     * e registrazione/deregistrazione alle notifiche sull'aggiornamento dei followers
     */

    public static int TCP = 0;
    public static int REG_PORT = 0;
    public static String IP_MC = null; 
    public static int PORT_MC = 0;
    public static AtomicBoolean s  = new AtomicBoolean(false);

    /** 
    *    @effects: stampa l'elenco dei comandi disponibili 
    */

    protected void elencoComandi(){
        System.out.println("\n> Comandi disponibili: " +
        "\n\t logout"+
        "\n\t list users"+
        "\n\t list following"+
        "\n\t list followers"+
        "\n\t follow <username>"+
        "\n\t unfollow <username>"+
        "\n\t blog"+
        "\n\t post <title> <content>"+
        "\n\t show feed"+
        "\n\t show post <id>"+
        "\n\t delete <id>"+
        "\n\t rewin <id>"+
        "\n\t rate <id> <vote>"+
        "\n\t comment <id> <comment>"+
        "\n\t wallet"+
        "\n\t wallet btc\n");
    }

    /**
     * @effects legge il file di configurazione relativo al client e inizalizza i vari campi
     * @param fileToRead inidirizzo file di configurazione
     */
    
    protected void readFile(String fileToRead){
        try(BufferedReader in = new BufferedReader(new FileReader(fileToRead))){
           
            String s;
            
            while((s = in.readLine())!=null){
                
                StringTokenizer parameter = new StringTokenizer(s, " ");
                
                String par = null;

                try{
                    par = parameter.nextToken();

                }catch(NoSuchElementException e){
                    continue;
                }
                
                if(par.equals("#")){
                    String p =null;
                    String div = null;
                    String value = null;
                    try{
                        p=parameter.nextToken();
                        div = parameter.nextToken();
                        value = parameter.nextToken();
                        if(div.equals("=")){
                            switch (p){
                                case "TCP": {
                                    TCP = Integer.parseInt(value);
                                    if(TCP<1024 || TCP > 65535){
                                        TCP = 0;
                                    }
                                }break;
    
                                case "REG_PORT" : {
                                    REG_PORT  = Integer.parseInt(value);
                                    if(REG_PORT<1024 || REG_PORT > 65535){
                                        REG_PORT = 0;
                                    }
                                }break;
    
                                case "MC_PORT" :{
                                    PORT_MC = Integer.parseInt(value);
                                    if(PORT_MC<1024 || PORT_MC > 65535){
                                        PORT_MC = 0;
                                    }
                                }break;
                                
                                
                                case "MC_IP" :{
                                    StringTokenizer otteti = new StringTokenizer(value,".");
                                    if(otteti.countTokens()!= 4){
                                        continue;
                                    }
                                    IP_MC = value;
                                }break;
                            }
                        }else continue;
                        

                    }catch(NoSuchElementException | NumberFormatException e){
                        continue;
                    }               
                }else continue;
            }

        }catch(IOException | NoSuchElementException e){
            e.printStackTrace();
            System.out.println("impossibile caricare i dati di configurazione\n");
            System.exit(1);//errore lettura file
        }
        //controllo valori campi, nel caso formato errato terminazione secuzione del client
        if(TCP == 0 || REG_PORT == 0 || IP_MC == null || PORT_MC ==0){
            System.out.println("Errore nel caricamento dei parametri");
            System.exit(1);
        }
    }

    /**
     * 
     * @param command comando che il client richiede di effettuare
     * @param wc oggetto client pe rinteragire con la list adei followers
     * @return true se il comnado inserito è corretto, false altrimenti
     */
    protected boolean comandoCorretto(StringTokenizer command,WinsomeClient wc){
        try{
            switch(command.nextToken()){
                
                case "logout":{
                    return true;
                }
                case "list" : {
                    try{
                        String what = command.nextToken();
                        switch (what){
                            case "users" : return true;
                            case "following" : return true;
                            case "followers" :{
                                
                                wc.listFollowers();
                                System.out.print("\n< ");
                                return true;
                            }
                            default : return false;
                        }
                        
                    }catch (NoSuchElementException e){
                        return false;
                    }   
                }

                

                case "show" : {
                    try{
                        String what = command.nextToken();
                        switch (what){
                            case "feed" : return true;
                            case "post" : {
                                try{
                                    int id = Integer.parseInt(command.nextToken());
                                    if(id <0){
                                        return false;
                                    }
                                    return true;
                                }catch(NumberFormatException e){
                                    return false;
                                }
                            }
                        }
                    }catch(NoSuchElementException e){
                        return false;
                    }
                }

                case "delete" : {
                    try{
                        int id = Integer.parseInt(command.nextToken());
                        if(id <0 ) return false;
                        return true;
                    }catch(NumberFormatException e){
                        return false;
                    }catch (NoSuchElementException e){
                        return false;
                    }

                }

                case "rewin" : {
                    try{
                        int id = Integer.parseInt(command.nextToken());
                        if(id<0) return false;
                        return true;
                    }catch(NumberFormatException | NoSuchElementException e){
                        return false;
                    }
                }

                case "wallet" : {

                    if(command.hasMoreTokens()){
                        String btc =  command.nextToken();
                        if(btc.equals("btc")&& !command.hasMoreTokens()) return true;
                        return false;
                    }else return true;

                    
                }

                case "post": {
                    String title = "";
                    String content = "";
                    while(title.length()<20 && command.hasMoreTokens()){
                        title = title+" " + command.nextToken();
                    }
                    content = title;
                    title = title+"...";
                    if(command.hasMoreTokens()){
                        
                        while(content.length()<500 && command.hasMoreTokens())
                            content = content +" "+command.nextToken();
                    }
                    if(title.length()>0 && content.length()>0){
                       
                        return true;
                    }
                    else{
                        
                        return false;
                    }

                }

                case "comment" : {
                    String comment = "";
                    while(command.hasMoreTokens() && comment.length()<500){
                        comment = comment +" "+ command.nextToken();
                    }

                    if(comment.length()>0 && comment.length()<500){
                        return true;
                    }
                    return false;
                }

                case "blog" : return true;



                case "follow" :{
                    try{
                        String u = command.nextToken();
                        if(u.length() < 3 || u.length() > 20 || u == null ) return false;
                        return true;
                    }catch(NoSuchElementException e){
                        return false;
                    }
                }

                case "unfollow" : {
                    try{
                        String u = command.nextToken();
                        if(u.length() <3 || u.length() > 20) return false;
                        return true;
                    }catch(NoSuchElementException e){
                        return false;
                    }
                }

                case "rate" :{
                    try{
                        int id = Integer.parseInt(command.nextToken());
                        int vote = Integer.parseInt(command.nextToken());
                        if(id < 0 ) return false;
                        if(vote != -1 && vote != 1) return false;
                        return true;
                    }catch(NumberFormatException | NoSuchElementException e){
                        return false;
                    }
                }
                
                

                default : return false;
            }

        }catch(NoSuchElementException e){
            return false;
        }
    
    }
    
    public static void main (String [] args){
        
        
        MainClient client = new MainClient();

        String user = null; //necessario username al momento della disattivazione delle notifiche
        
        WinsomeClient wc = new WinsomeClient();

        System.out.println("\n***APERTURA WINSOMECLIENT***\n");   

        //leggere file di configurazione

         client.readFile("Progetto/configClient.txt");   
        
        //instanziare oggetto remoto del client 
        
        Registry regClient = null;
        int portRem =0;
        WinsomeClientInterface stub = null;

        try{
            while(true){
                try{
                    stub = (WinsomeClientInterface) UnicastRemoteObject.exportObject(wc, 20000+portRem);
                    break;
                }catch(ExportException  e){
                    portRem++;//si cerca una porta disponibile
                }
            }
            //creazione del registro
            LocateRegistry.createRegistry(MainClient.REG_PORT+portRem);
            regClient = LocateRegistry.getRegistry(MainClient.REG_PORT+portRem);
            regClient.bind("FOLLOWERS"+portRem, stub);
            
        }catch(java.rmi.AlreadyBoundException |RemoteException e){
            e.printStackTrace();
            System.exit(1);
        }  

        //connessione al server

        try(Socket socket = new Socket(InetAddress.getByName("localhost"),TCP);
        Scanner  cmdIn= new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));){          

            //siamo lato client -> importare oggetto remoto server per registrazione, non conosco la porta a cui è associato lo stub quindi me lo devo far inviare, sara' la prima cosa che 
            //fara' il server quando si instaura la connessione

            String obj  = reader.readLine();//recupero nome servizio + porta stub
            StringTokenizer s = new StringTokenizer(obj,":");
            
            int serverPort = 0;
            String nameServer = null;

            WinsomeServerInterface remObj = null;
            
            try{
                nameServer = s.nextToken();
                serverPort = Integer.parseInt(s.nextToken());
                Registry regS = LocateRegistry.getRegistry(serverPort);
    
                try{
                    remObj = (WinsomeServerInterface) regS.lookup(nameServer);
                }catch(NotBoundException e){
                    e.printStackTrace();
                    System.exit(1);
                }
            }catch (RemoteException | NumberFormatException | NoSuchElementException e){
                e.printStackTrace();
                System.exit(1);
            }

            //registrazione (vedi register WinsomeServer) e registrazione notifiche (in due momenti diversi) -> uso oggetto remoto server
            //o l'applicazione si chiude o se continua è perchè l'utente si è registrato o loggato, altrimenti non gli viene
            //data la possibilità di fare altro
            
            System.out.println("> nuovo utente?? [y/n/exit]\n");
            System.out.print("< ");
            String risposta = cmdIn.nextLine();
            
            while(!risposta.equals("y")&&!risposta.equals("n")&&!risposta.equals("exit")){
                System.out.println("> inserisci risposta corretta [y/n/exit]");
                System.out.print("< ");
                risposta = cmdIn.nextLine();
            }

            switch (risposta){
                case "n": { //l'utente è gia' registrato, vuole loggarsi
                    System.out.println("> inserire comando: login <username> <passw> con le proprie credenziali di accesso per accedere alla rete, exit per uscire");
                    System.out.print("< ");
                    String command = cmdIn.nextLine();
                    String us =null;
                    String psw=null;

                    try{
                        while(!command.startsWith("login")&&!command.equals("exit")){//finchè il comando non è di login o di uscita si resta piantati
                            System.out.println("> inserire comando login <username> <passw> o exit ");
                            System.out.print("< ");
                            command = cmdIn.nextLine();
                        }
                        StringTokenizer login = new StringTokenizer(command," ");

                        if(command.equals("exit")){//l'utente ha deciso di uscire
                            System.out.println("\n*** CHIUSURA WINSOMECLIENT ***\n");
                            System.exit(1);
                        }
                        while(!(login.countTokens()==3)){//l'utente si vuole loggare ma deve inserire i parametri nel modo giusto, altrimenti la richiesta non viene inoltrata al server
                            System.out.println("> username e/o password mancanti, reinserire comando login <username> <passw> o exit");
                            System.out.print("< ");
                            command = cmdIn.nextLine();
                            login = new StringTokenizer(command," ");
                            if(command.equals("exit")){//l'utente ha deciso di uscire
                                System.out.println("\n*** CHIUSURA WINSOMECLIENT ***\n");
                                System.exit(1);
                            }
                        }
                        //il comando è giusto, si procede al login
                        command = login.nextToken();
                        us = login.nextToken();
                        psw = login.nextToken();
                        out.println("login "+us+" "+psw);
                        out.flush();

                        

                    }catch(NoSuchElementException e){

                        System.out.println("> nessun comando inserito");
                        System.out.println("\n*** CHIUSURA WINSOMECLIENT ***\n");
                        System.exit(1);
                    }
                
                    //il comando di login è corretto e la richiesta fatta al server, si controlla il risultato dell'operazione                    
                    try{
                        String res  = reader.readLine();
                        StringTokenizer caso = new StringTokenizer(res, "/");
                    //
                    switch(caso.nextToken()){
                    
                        case "0" : {//login andato a buon fine
                            System.out.println("> utente loggato correttamente");
                            String notify = remObj.regNotifiche(us, stub);//metodo remoto server per rgeistrazione alle notifiche
                            System.out.println("> "+ caso.nextToken() +" "+notify);
                            user = us;
                          
                        } break;
                
                        case "1" : {//l'utente non è registrato
                            System.out.println("> utente non registrato, procedere alla registrazione? [y/n/exit]");
                            System.out.print("< ");
                            String ris = cmdIn.nextLine();

                            while(!ris.equals("y")&&!ris.equals("n")&&!ris.equals("exit")){
                                System.out.println("> inserisci risposta corretta [y/n/exit]\n");
                                System.out.print("< ");
                                ris = cmdIn.nextLine();
                            }

                            switch(ris){
                                case "n" : System.exit(1);//l'utente che ha provato a loggarsi non vuole registrarsi
                                case "y" : {System.out.println("> inserire comando register <username> <password> <tag1> ... <tag5>, exit per chiudere\n");
                                System.out.print("< ");
                                String prova = cmdIn.nextLine();
                                String u ="";
                                String p = "";
                                LinkedList<String> tags = new LinkedList<String>();
                                
                                
                                while(!prova.startsWith("register")&& !prova.equals("exit")){// si cicla finchè il comando inserito dall'utente non è quello di uscita o di registrazione
                                    System.out.println("> comando errato, inserire comando register <username> <password> <tag1> ... <tag5>, exit per chiudere");
                                    System.out.print("< ");
                                    prova = cmdIn.nextLine();
                                }
                                if(prova.equals("exit")){//l'utente ha deciso di uscire
                                    System.out.println("\n*** WINSOMECLIENT CHIUSO***\n");
                                    System.exit(1);
                                }
            
                                StringTokenizer comd = new StringTokenizer(prova," ");
                                while(comd.countTokens()<4 || comd.countTokens()>7){//l'utente vuole registrarsi ma finchè non inserisce i parametri corretti si cicla
                                    System.out.println("> comando errato, inserire comando register <username> <password> <tag1> ... <tag5>, exit per chiudere");
                                    System.out.print("< ");
                                    prova = cmdIn.nextLine();
                                    if(prova.equals("exit")){//l'utente ha deciso di uscire
                                        System.out.println("\n*** WINSOMECLIENT CHIUSO***\n");
                                        System.exit(1);
                                    }
                                    comd = new StringTokenizer(prova," ");
            
                                    
                                }
                                try{
                                    prova = comd.nextToken();
                                    u = comd.nextToken();
                                    p = comd.nextToken();
                                }catch(NoSuchElementException e){
                                    System.out.println("comando errato");
                                    System.exit(1);
                                }
                                while(comd.hasMoreTokens()){
                                        tags.add(comd.nextToken());
                                    }
                                
            
                                // se arrivo qua i parametri sono corretti
                        
                                int result = -1;
                                
                                try{
                                        result = remObj.register(u, p, tags); //metodo remoto server
                                }catch(RemoteException e){
                                        e.printStackTrace();
                                        System.exit(1);
                                }
            
                                switch(result){
                                    case 0: {
                                        System.out.println("> utente registrato correttamente");
                                        //login automatico alla registrazione
            
                                        out.println("login "+u+" "+p);
                                        out.flush();
                                        StringTokenizer c = new StringTokenizer(reader.readLine(),"/");
                                        if(c.nextToken().equals("0")){
                                            String notify = remObj.regNotifiche(u, stub);//registazione alle notifiche -> metodo remoto server
                                            System.out.println("> "+c.nextToken()+ " - " +notify);
                                            user = u;
                                        }else{
                                            System.out.println("impossibile loggarsi");//il login non è andato a buon fine -> non dovrebbe mai accadere
                                            System.exit(1);
                                        }
                                        
                                        
                                        System.out.println("> login avvenuto con successo, digitare un comando:\n");
                                    }break;
            
                                    case 1:{//l'utente che vuole registarsi ha inserito un username gia' utilizzato
                                        System.out.println("> utente gia' registrato username gia' utilizzato");
                                        System.exit(1);
                                    }break;
            
                                    case 2:{//i parametri inseriti non sono corretti -> non dovrebbe mai accadere
                                        System.out.println("> paramentri non corretti, massimo 5 tag e username e password maggiori di 3 caratteri e minori di 20");
                                        System.exit(1);
                                    }break;
            
                                    case 3: {//c'è gia' un utente loggato sulla macchina  -> non dovrebbe mai accadere
                                        System.out.println("> utente gia' loggato su questa macchina ");
                                        System.exit(1);
                                    }break;
            
                                    
                                }
                            }break;
                            case "exit":{
                                //l'utente ha deciso di uscire
                                System.out.println("\n*** CHIUSURA WINSOMECLIENT ***\n");
                                System.exit(1);
                            
                            }break;
                        }
                            
                        }break;
                
                        case "2": {//la password inserita non è corretta.
                            int attempt = 2;
                            while(attempt < 4 ){
                                
                                System.out.println("> password non corretta,reinseririla, tentativo :"+attempt + "\tdopo il terzo tentativo l'applicazione termina\tinserire exit per chiudere");
                                System.out.print("< ");
                                String password = cmdIn.nextLine();
                                if(password.equals("exit")){//l'utente ha deciso di uscire
                                    System.out.println("\n*** WINSOMECLIENT CHIUSO ***\n");
                                    System.exit(1);
                                }
                                out.println("login "+us+" "+password);
                                out.flush();
                                String resu = reader.readLine();
                                StringTokenizer r = new StringTokenizer(resu,"/");
                                if(r.nextToken().equals("0")){//la password inserita è corretta e il login andato a buon fine
                                    String notify = remObj.regNotifiche(us, stub);
                                    System.out.println("> "+r.nextToken()+ " - "+notify); 
                                    user = us;
                                    System.out.println("> login avvenuto con successo, digitare un comando:\n");
                                    break;
                                }

                                attempt++;//login non effettuato, nuovo tentativo di accesso

                            }

                            if(attempt == 4){//finito tenativi a disposizione dell'utente, si chiude l'app
                                System.out.println("\n*** WINSOMECLIENT CHIUSO***\n");
                                System.exit(1);
                            }
                        }break;
                
                        case "3" : {//l'utente è già loggato
                            System.out.println("> utente gia' loggato");
                            System.exit(1);
                        }break;

                        case "4" : {//parametri non corretti o inesistenti
                            System.out.println("> paraemtri inesistenti");
                            System.exit(1);
                        }break;

                        default : System.out.println("errore");
                    } 
                    }catch(NumberFormatException | NullPointerException e){
                        e.printStackTrace();
                        System.out.print("impossibile loggarsi\n");
                }          
                }break;

                case "y" : {//l'utente non è registrato 
            
                    System.out.println("> inserire comando register <username> <password> <tag1> ... <tag5>, exit per chiudere\n");
                    System.out.print("< ");
                    String prova = cmdIn.nextLine();
                    String u ="";
                    String p = "";
                    LinkedList<String> tags = new LinkedList<String>();
                    
                    
                    while(!prova.startsWith("register")&& !prova.equals("exit")){// si cicla finchè il comando inserito dall'utente non è quello di uscita o di registrazione
                        System.out.println("> comando errato, inserire comando register <username> <password> <tag1> ... <tag5>, exit per chiudere");
                        System.out.print("< ");
                        prova = cmdIn.nextLine();
                    }
                    if(prova.equals("exit")){//l'utente ha deciso di uscire
                        System.out.println("\n*** WINSOMECLIENT CHIUSO***\n");
                        System.exit(1);
                    }

                    StringTokenizer comd = new StringTokenizer(prova," ");
                    while(comd.countTokens()<4 || comd.countTokens()>7){//l'utente vuole registrarsi ma finchè non inserisce i parametri corretti si cicla
                        System.out.println("> comando errato, inserire comando register <username> <password> <tag1> ... <tag5>, exit per chiudere");
                        System.out.print("< ");
                        prova = cmdIn.nextLine();
                        if(prova.equals("exit")){//l'utente ha deciso di uscire
                            System.out.println("\n*** WINSOMECLIENT CHIUSO***\n");
                            System.exit(1);
                        }
                        comd = new StringTokenizer(prova," ");

                        
                    }
                    try{
                        prova = comd.nextToken();
                        u = comd.nextToken();
                        p = comd.nextToken();
                    }catch(NoSuchElementException e){
                        System.out.println("comando errato");
                        System.exit(1);
                    }
                    while(comd.hasMoreTokens()){
                            tags.add(comd.nextToken());
                        }
                    

                    // se arrivo qua i parametri sono corretti
            
                    int result = -1;
                    
                    try{
                            result = remObj.register(u, p, tags);
                    }catch(RemoteException e){
                            e.printStackTrace();
                            System.exit(1);
                    }

                    switch(result){
                        case 0: {
                            System.out.println("> utente registrato correttamente");
                            //login automatico alla registrazione

                            out.println("login "+u+" "+p);
                            out.flush();
                            StringTokenizer c = new StringTokenizer(reader.readLine(),"/");
                            if(c.nextToken().equals("0")){
                                String notify = remObj.regNotifiche(u, stub);//registazione alle notifiche -> metodo remoto server
                                System.out.println("> "+c.nextToken()+ " - " +notify);
                                user = u;
                            }else{
                                System.out.println("impossibile loggarsi");//il login non è andato a buon fine -> non dovrebbe mai accadere
                                System.exit(1);
                            }
                            
                            
                            System.out.println("> login avvenuto con successo, digitare un comando:\n");
                        }break;

                        case 1:{//l'utente che vuole registarsi ha inserito un username gia' utilizzato
                            System.out.println("> utente gia' registrato username gia' utilizzato");
                            System.exit(1);
                        }break;

                        case 2:{//i parametri inseriti non sono corretti -> non dovrebbe mai accadere
                            System.out.println("> paramentri non corretti, massimo 5 tag e username e password maggiori di 3 caratteri e minori di 20");
                            System.exit(1);
                        }break;

                        case 3: {//c'è gia' un utente loggato sulla macchina  -> non dovrebbe mai accadere
                            System.out.println("> utente gia' loggato su questa macchina ");
                            System.exit(1);
                        }break;

                        
                    }
                }break;

                case "exit" : {
                    System.out.println("***WINSOMECLIENT CHIUSO***");
                     System.exit(0);
                }
            }
            
            //capisci il ciclo multicast - non puoi farlo qui ne' creare metodo -> thread???
            
            //unirsi al gruppo multicast
            //ciclo per ricevere i messaggi muticast??? la receive è bloccante, l'attesa deve avvenire in contemporanea alla normale operatività, inoltre non sempre almeno un portafoglio viene aggiornato

            Thread multi = new Thread(new MulticastTask());
            multi.start();

            client.elencoComandi();

            //inizio invio richieste al server -> l'utente è registrato o loggato
            
            String line;
            
            StringTokenizer cmd;       
            
            System.out.print("< ");
            
            while(!(line = cmdIn.nextLine()).equals("logout")){//finchè l'utente non decide di scollegarsi è possibile interagire con la rete sociale
                
                cmd = new StringTokenizer(line," ");
        
                if(!client.comandoCorretto(cmd,wc)){//finchè il comnado da inviare al servre non è sintatticament corretto la richesta non viene inoltrata e si stampa l'elenco dei comndi disponibili
                    System.out.println("\n> impossibile inoltrare la richiesta\n");
                    client.elencoComandi();     
                    System.out.print("< ");
                    continue;                
    
                }else{        
                    if(line.equals("list followers")){
                    continue;
                    }
                out.println(line);
                out.flush();
                String res = "> "+ reader.readLine();
                System.out.println(res); 
                }
                
                System.out.print("\n< "); 

            }
        
            out.println("logout");
            out.flush();
            String res = "> "+reader.readLine();
            System.out.println(res); 

        //deregistrazione alle notifiche
            if(user!= null){
                String notify = remObj.disattivaNotifiche(user, stub);
            System.out.println("> "+notify);
            }
            
        //lasciare gruppo multicast
        MainClient.s.set(true);

        //unbind de prorpio oggetto remoto
        try{
            regClient.unbind("FOLLOWERS"+portRem);
        }catch(NotBoundException e){
            e.printStackTrace();
        }
    
    catch(IOException e){
        e.printStackTrace();
        System.exit(1);
    }

        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("***WINSOMECLIENT CHIUSO***");
        System.exit(0);
    }
}