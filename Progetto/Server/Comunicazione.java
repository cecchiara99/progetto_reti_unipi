package Progetto.Server;



import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

import Progetto.Client.WinsomeClientInterface;

import java.rmi.*;

public class Comunicazione implements Runnable{

    /**
     * OVERVIEW: classe che permette la comunicazione con un singolo client
     */

    //socket su cui sarà instaurata la connessione
    Socket socket;

    //port aremota su cui esportare stub
    int remP;

    //oggetto server per poter interagire con la rete
    WinsomeServer ws;

    //registro su cui è espportato lo stub
    Registry regServer;
    
    /**
     * @effects costruttore che inizializza i campi della classe
     */
    public Comunicazione(Socket s, int p, WinsomeServer winsomeServer,Registry regServeRegistry){
        this.socket = s;
        this.remP=p;
        this.ws = winsomeServer;
        this.regServer = regServeRegistry;
    }

    /**
     * @effect: permette la scelta del metodo della classe WinsomeServer da invocare e invia il relativo risultato dell'operazione
     *          n.b i comandi passati come parametro a questo metodo sono sicuramente corretti
     * 
     * @param out output stream per inviare il risultato dell'operazione al client
     * @param server su cui è implementato il relativo metodo
     * @param message comando digitato dall'utente (sicuramente corretto)
     */

    private void execFunz(PrintWriter out, WinsomeServer server, String message){

        if(out == null){
            return;
        }

        if(server == null || message == null){
            out.println("Errore: parametri scorretti\n");
            out.flush();
            return;
        }

        StringTokenizer line = new StringTokenizer(message," ");
        String comando = null;


        try{
        comando = line.nextToken();
        String res = "";
        
        switch(comando){
            case "login" : {
                res = server.login(line.nextToken(), line.nextToken());
                out.println(res);
                out.flush();

            } break; 

            case "logout" : {
                
                res = server.logout(server.currentUser.username); 
                out.println(res);
                out.flush();
                
                
            } break;

            case "list" :{
                String mode = line.nextToken();
                switch(mode){
                    case "users" : {
                                      
                        res = server.listUsers();
                        out.println(res);
                        out.flush();
                        
                    } break;

                    case "following" : {
                        
                        res = server.listFollowing();
                        out.println(res);
                        out.flush();
                        
                    }break;

                    default: {
                        out.println("funzionalità inesistente/");
                        out.flush();
                    }
                }

            } break;

            case "blog" : {
                
                res = server.viewBlog();
                out.println(res);
                out.flush();
                
            } break;

            case "post" :{
                String title = "";
                String content = "";
                    while(line.hasMoreTokens()){
                        title = title+" "+ line.nextToken();
                        if(title.endsWith(";")) break;
                    }

                    while(line.hasMoreTokens()){
                        content = content + " "+line.nextToken();
                    }
                if(title.length()>22){
                    res ="titolo troppo lungo, max 20 caratteri";
                

                }else{
                    if(content.length()>501){
                        res = "contenuto troppo lungo, max 500 caratteri";
                    }else{
                        res = server.createPost(title, content);
                    }
                }
                               
                
                out.println(res);
                out.flush();
                    
                
            }break;

            case "show" : {
                String what = line.nextToken();
                switch(what){
                    case "feed" : {
                        
                        res = server.showFeed();
                        out.println(res);
                        out.flush();
                            
                    }break;
                    case "post" : {
                        String id = line.nextToken();
                        int idP = -1;
                        try{
                        idP = Integer.parseInt(id);
                        } catch(NumberFormatException e){
                            out.println("id sbagliato");
                            out.flush();
                            
                        } 
                        res = server.showPost(idP);
                        out.println(res);
                        out.flush();
                    }break;

                    default : {out.println("funzionalità inesistente");
                                out.flush();}
                }
            }break;

            case "delete" : {
                
                String id = line.nextToken();
                int idP =-1;
                try{
                idP = Integer.parseInt(id);
                } catch(NumberFormatException e){
                    out.println("id sbagliato");
                }
                res = server.deletePost(idP);
                out.println(res);
                        
                out.flush();
            }break;

            case "rewin" : {
                
                
                String id = line.nextToken();
                int idP = -1;
                try{
                    idP = Integer.parseInt(id);
                }catch(NumberFormatException e){
                    out.println("inserire giusto id");
                    out.flush();
                }

                res = server.rewinPost(idP);
                out.println(res);
                out.flush();
                
            }break;

            case "comment" : {
                
                String id = line.nextToken();
                String comm = "";
                int idP = -1;

                try{
                    idP = Integer.parseInt(id);

                }catch(NumberFormatException e){
                    out.println("id non un numero");
                    out.flush();
                    return;
                }
                while(line.hasMoreTokens() && comm.length()<500){
                    comm = comm +" "+ line.nextToken();
                }

                if(comm.length()>0){
                    res = server.addComment(idP, comm);
                    out.println(res);
                    out.flush();
                }
                
            }break;

           case "follow" : {
            
                String un = line.nextToken();
            if(WinsomeServer.getByNameUser(un)&& !un.equals(server.currentUser.username) && !WinsomeServer.seguiti.get(server.currentUser.getUsername()).contains(server.getUser(un))){//non puoi seguire te stesso
                    WinsomeClientInterface rem = (WinsomeClientInterface) server.getRemote(un);
                    int idU = server.getUser(un).getId();
                    if(rem != null && WinsomeServer.logged.get(un).get()==true){
                        try{
                            res = server.followUser(idU);
                            rem.aggFollower(server.currentUser);//metodo remoto client
                            out.println(res);
                            out.flush();
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }else{
                        res = server.followUser(idU);
                        out.println(res);
                        out.flush();
                    }
                return;
            }
               
                
                else{
                    out.println("utente inesistente, impossibile aggiornare la lista dell'utente da seguire / non puoi seguire te stesso");
                    out.flush();
                    return;
                }
            
            }
            case "unfollow" : {
                
                    String un = line.nextToken();
                    
                    
                    if(WinsomeServer.getByNameUser(un)  && !un.equals(server.currentUser.username)){
                        
                        WinsomeClientInterface rem = (WinsomeClientInterface) server.getRemote(un);
                        int idU = server.getUser(un).getId();
                        if(rem != null && WinsomeServer.logged.get(un).get()==true){
                            try{
                                res = server.unfollowUser(idU);
                                rem.remFollower(server.currentUser);//metodo remoto client
                                out.println(res);
                                out.flush();
                            }catch (RemoteException e){
                                e.printStackTrace();
                            }
                        }else{
                            res = server.unfollowUser(idU);
                            out.println(res);
                            out.flush();
                        }
                    return;
                }else{
                    out.println("utente inesistente, impossibile aggiornare la lista dell'utente da non seguire più / non puoi unfolloware te stesso");
                    out.flush();
                    return;
                }
            } 

            case "rate":{
                
                String idd = line.nextToken();
                String voto = line.nextToken();
                try{
                    int idP = Integer.parseInt(idd);
                    int rate = Integer.parseInt(voto);
                    
                    res = server.rate(idP, rate);
                    out.println(res);
                    out.flush();
                    
                }catch(NumberFormatException e){
                    e.printStackTrace();
                    out.println("inserire numeri corretti\n");
                    out.flush();
                }          
            }break;

            case "wallet" :{

                if(line.hasMoreTokens()){
                    
                    double btc = server.getWalletInBitcoin();
                    out.println("conversione in Bitcoin eseguita -> btc = "+btc);
                    out.flush();
                    return;
                    

                }else{
                    
                    res = server.getWallet(server.currentUser.getUsername());
                    out.println(res);
                    out.flush();
                    return;
                }

            }

        }    
    }catch(NoSuchElementException e){
        e.printStackTrace();
        }
    }

    public void run(){
        

        String command;

        System.out.println("Connessione stabilita con: "+ socket.getInetAddress().getHostAddress()+"\t/port: "+socket.getPort()+"\n");//dati client
        //instaurazione della connessione
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            ){
            
            //esportazione oggetto remoto del server
            
            try{
                WinsomeServerInterface stub = (WinsomeServerInterface) UnicastRemoteObject.exportObject(ws, 2000+remP); 
                regServer.bind("REGISTRAZIONE"+remP, stub);

            }catch(AlreadyBoundException e ){
                e.printStackTrace();
            }

            //invio dati registry al client per poter recuperare lo stub e registrarsi
            out.println("REGISTRAZIONE"+remP+":"+(MainServer.PORT_REG));
            out.flush();
            
             
            //finchè non viene interrota la comunicazione lato cient ciclo di attesa richieste client
            while(!(command = reader.readLine()).equals("exit")){
                
                execFunz(out, ws, command);
                
            } 
            
        }catch(SocketException | NullPointerException e){
            try{
                //chiusura connessione
                socket.close();
                
                //unbind dell'oggetto remoto
                regServer.unbind("REGISTRAZIONE"+remP);
                
                
            }catch(NotBoundException| IOException x){
                x.printStackTrace();
            }
        }catch(IOException z){
            z.printStackTrace();
        }
        
        System.out.println("Connessione persa da: "+ socket.getInetAddress().getHostAddress()+"\t/port: "+socket.getPort()+"\n");
        return;
    }
}

