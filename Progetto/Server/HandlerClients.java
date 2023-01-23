package Progetto.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;


public class HandlerClients implements Runnable {
    
    /**
     * OVERVIEW: classe che gestisce le connessioni dei vari client utilizzando un threadpool
     */

    public void run() {

        //creazione threadpool
        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket SS = null;

        //contatore per gestire le porte su cui sono allo cati gli stub
        AtomicInteger portRem = new AtomicInteger(-1);

        //registro per RMI
        Registry regServer = null;

        try{
            //creazione del registo
            LocateRegistry.createRegistry(MainServer.PORT_REG);

            //allocazione del registro
            regServer = LocateRegistry.getRegistry(MainServer.PORT_REG);
        }catch(RemoteException e){
            e.printStackTrace();
            System.exit(1);
        }
        
        //creazione server socket
        try{
            SS = new ServerSocket(MainServer.TCP);
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        
        //finchè il server non deve terminare ciclo di attesa connessioni client
        while(!MainServer.exit.get()){

            Socket socket;
            try{
                SS.setSoTimeout(MainServer.TIMEOUT);
                SS.setReuseAddress(true);
                socket = SS.accept();//bloccante
            
            }catch(SocketTimeoutException e){
                continue;
            }
            catch(IOException e){
                e.printStackTrace();
                break;
            }
        //nuova connessione -> sottomissione al threadpool
        pool.execute(new Comunicazione(socket,portRem.incrementAndGet(),new WinsomeServer(MainServer.serverCounter.incrementAndGet()),regServer));
        
        }

        //terminazione server chiusura server socket
        try{
            SS.close();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }

        //terminazione threadpool
        pool.shutdown();
        try{
            if(!pool.awaitTermination(10000, TimeUnit.MILLISECONDS)){
                pool.shutdownNow();
            }
        }catch(InterruptedException e){
            //ultimo update memoria per sicurezza
            e.printStackTrace();
            if(!WinsomeServer.updateData()){
                return;
            }
            return;
        }
    }
}
