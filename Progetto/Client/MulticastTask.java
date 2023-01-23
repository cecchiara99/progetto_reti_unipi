package Progetto.Client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.atomic.AtomicBoolean;

public class MulticastTask implements Runnable{

    /**
     * OVERVIEW: classe che gestisce l'iscrizione e la disiscrizione dal gruppo multicast e si pccupa diricevere gli eventuali messaggi 
     * di aggiornamento del portafoglio
     */
    
    /**
     * 
     * @param stop variabile atomica booleana che vale true se il client vuole disconnettersi, false altrimenti
     * @return true se l'utente si deve disiscrivere dal gruppo multicast, false altrimenti
     */

    public boolean getStop(AtomicBoolean stop){
        return stop.get();
    }

    /**
     * @effect metodo che si blocca finchè non riceve unmessaggio multicast e cicla finchè non riceve il segnale di uscita
     * @param m socket multicast su cui avviene la ricezione dei messaggi
     * @param g indirizzo + porta del gruppo multicast
     */

    public void receiveMsg(MulticastSocket m, InetAddress g){
        
        try{
            byte[] buffer = new byte[1024];
            
            while(getStop(MainClient.s)==false){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                m.receive(packet);//bloccante!!!
                String msg = new String(packet.getData());
                System.out.print(msg+"\n< ");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("deprecation")
    
    public void run(){
        MulticastSocket ms = null;
        InetAddress group = null;

        try{
            ms = new MulticastSocket(MainClient.PORT_MC);
            group = InetAddress.getByName(MainClient.IP_MC);

           
            
            
            ms.joinGroup(group); //iscrizione al gruppo multicast
            
            receiveMsg(ms,group);

            if(ms!=null){
                
                ms.leaveGroup(group);//disiscirizione dal gruppo muticast
                ms.close();//chiusura multlicast socket
            }

        }catch(IOException e){
            e.printStackTrace();
        }
            

        
    }
}
