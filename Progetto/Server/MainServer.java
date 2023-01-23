package Progetto.Server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;



public class MainServer {

    /*
    * OVERVIEW: classe che si occupa dell'esecuzione del server e la relativa terminazione 
    * e la gestione della rete sociale
    */

    //porta TCP
    public static int TCP = 0;

    public static AtomicBoolean exit = new AtomicBoolean(false);

    //contatore dei server in esecuzione
    public static AtomicInteger serverCounter = new AtomicInteger(0);

    //porta registro oggetto remoto
    public static int PORT_REG = 0;

    //indirizzo IP multicast
    public static String MC_IP = null;

    //porta multicast
    public static int MC_PORT = 0;

    //timeout check ricompense
    public static int TIMEOUT = 0;

    //percentuale ricompense
    public static int PERC = 0;

    //timeout memoria
    public static int M_TIMEOUT = 0;

    //timeout attesa connessioni 
    public static int S_TIMEOUT = 0;


    /**
     * @effect: legge il file di configurazione e setta i dati per inizializzare il server,
     *          se i dati non sono corretti termina l'esecuzione
     * @param fileConfig nome del file di configurazione
     */

    public void readFile(String fileConfig){
        try(BufferedReader input = new BufferedReader(new FileReader(fileConfig))){
           
            String s;
            
            while((s = input.readLine())!=null){//finchè nel file c'è da leggere si cicla
                
                StringTokenizer parameter = new StringTokenizer(s, " ");
                
                String par = null;

                try{
                    par = parameter.nextToken();

                }catch(NoSuchElementException e){
                    continue;
                }
                
                if(par.equals("#")){//la linea di testo continee un dato da dover leggere
                    String p =null;
                    String value = null;
                    try{
                        p=parameter.nextToken();
                        String div = parameter.nextToken();
                        value = parameter.nextToken();
                        if(div.equals("=")){
                            //switch case per settare i valori letti
                            switch (p){
                                case "TCP": {
                                    TCP = Integer.parseInt(value);
                                    if(TCP<1024 || TCP > 65535){
                                        TCP = 0;
                                    }
                                }break;
    
                                case "PORT_REG" : {
                                    PORT_REG  = Integer.parseInt(value);
                                    if(PORT_REG<1024 || PORT_REG > 65535){
                                        PORT_REG = 0;
                                    }
                                }break;
    
                                case "MC_PORT" :{
                                    MC_PORT = Integer.parseInt(value);
                                    if(MC_PORT<1024 || MC_PORT > 65535){
                                        MC_PORT = 0;
                                    }
                                }break;
                                
                                case "TIMEOUT": {
                                    TIMEOUT = Integer.parseInt(value);
                                    if(TIMEOUT<0){
                                        TIMEOUT = 0;
                                    }
                                }break;
    
                                case "PERC" :{
                                    PERC = Integer.parseInt(value);
                                    if(PERC <0 ){
                                        PERC = 0;
                                    }
                                }break;
    
                                case "S_TIMEOUT" :{
                                    S_TIMEOUT = Integer.parseInt(value);
                                    if(S_TIMEOUT<0){
                                        S_TIMEOUT = 0;
                                    }
                                }break;

                                case "M_TIMEOUT" :{
                                    M_TIMEOUT =Integer.parseInt(value);
                                    if(M_TIMEOUT < 0){
                                        M_TIMEOUT =0;
                                    }
                                }break;
    
                                case "MC_IP" :{
                                    StringTokenizer otteti = new StringTokenizer(value,".");
                                    if(otteti.countTokens()!= 4){
                                        continue;
                                    }
                                    MC_IP = value;
                                }break;
                            }
    
                        }else continue;
                        
                    }catch(NoSuchElementException | NumberFormatException e){//formato linea errata, si va avanti nella lettura
                        continue;
                    }               
                }else continue;
            }

        }catch(IOException | NoSuchElementException e){
            e.printStackTrace();
            System.out.println("impossibile caricare i dati di configurazione\n");
            System.exit(1);
        }

        // controllo correttezza parametri nel caso terminazione esecuzione server

        if(TCP == 0 || PORT_REG == 0 || MC_IP == null || MC_PORT ==0 || TIMEOUT ==0 || PERC == 0 || S_TIMEOUT ==0 || M_TIMEOUT ==0){
            System.out.println("Errore nel caricamento dei parametri");
            System.exit(1);
        }   


    }

    
    
    
    
    public static void main(String[] args){
        
        System.out.println("\n*** APERTURA SERVER ***\n");

        MainServer pm = new MainServer();
        
        //leggere file di configurazione (chiama metodo)

        pm.readFile("Progetto/configServer.txt");

        System.out.println("\nDigita off per chiudere il server\n");

        /** apertura input da linea di comando */
        Scanner CLI = new Scanner(System.in);


        /** creazione thread che gestisce i client che sis ocnnettono al server */
        Thread utenti = new Thread(new HandlerClients());
       
        /** creazione thread per il calcolo delle ricompense */
        Thread ricompense = new Thread(new CalcoloRicompense(new WinsomeServer(serverCounter.incrementAndGet()),MC_IP,MC_PORT,TIMEOUT,PERC,new java.sql.Timestamp(System.currentTimeMillis())));

        /** creazione thread per l'update della memoria */
        Thread memoria = new Thread(new MemoryTask(M_TIMEOUT));
        
        ricompense.start();

        utenti.start();

        memoria.start();

        /** finchè il server non viene volontariamente chiuso con l'apposito comando si cicla */
        
        while (!CLI.nextLine().equals("off"))
            System.out.println("Digita off per chiudere il server\n");
        
        
        //chiusura dell'input da linea di comando e terminazione server
        
        CLI.close();

        exit.set(true);  
        
        
        
        try{
            memoria.interrupt();
            ricompense.interrupt();
            utenti.join();        
        }catch(InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }
        
       
        
        System.out.println("\n***WINSOME SERVER CHIUSO***\n");
        System.exit(0);
    }
}
