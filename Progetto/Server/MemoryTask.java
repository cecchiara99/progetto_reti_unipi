package Progetto.Server;

public class MemoryTask implements Runnable{

    /**
     * OVERVIEW: classe che gestisce l'aggiornamento periodico della memoria
     */
    protected int TIME;
    public MemoryTask(int timeout){
        this.TIME = timeout;
    }
    public void run(){
        
        while(!MainServer.exit.get()){
            try{
                Thread.sleep(TIME);
            }catch(InterruptedException e){
                WinsomeServer.updateData();
                System.out.println("> memoria aggiornata\n");
                break;
            }

            WinsomeServer.updateData();
        }
    }
    
}
