package Progetto.Server;

import java.io.Serializable;
import java.sql.*;
import java.util.LinkedList;
public class Wallet implements Serializable{

    /**
     * OVERVIEW: classe che rappresenta un posrtafoglio associato all'utente
     */
    protected LinkedList<Timestamp> historyWin = null;
    protected double balanceWin;
    protected LinkedList<Timestamp> historyBtc = null;
    protected double balanceBtc;

    /**
     * @effects costruttore che inizializza i campi della classe
     */
    
    public Wallet(){
        historyWin = new LinkedList<Timestamp>();
        balanceWin = 0;
        historyBtc = new LinkedList<Timestamp>();
    }
    /**
     * 
     * @return cronologia transazioni wincoin
     */

    public synchronized LinkedList<Timestamp> getHistoryWin(){
        LinkedList<Timestamp> tmp = new LinkedList<Timestamp>();
        tmp.addAll(historyWin);
        return tmp;
    }
    /**
     * 
     * @return saldo wincoin
     */

    public synchronized double getBalanceWin (){
        return balanceWin;
    }

    /**
     * 
     * @return saldo bitcoin
     */

    public synchronized double getBalanceBtc(){
        return balanceBtc;
    }

    /**
     * 
     * @return lista transazioni bitcoin
     */
    public synchronized LinkedList<Timestamp> getHistoryBtc(){
        LinkedList<Timestamp> tmp = new LinkedList<Timestamp>();
        tmp.addAll(historyBtc);
        return tmp;
    }

    /**
     * @effects aggiunge alla lista delle transazioni wincoin una nuova transazione
     * @param newValue monete da aggiungere al saldo
     * @param time data e ora della transazione
     * @return true se la transazione è stata aggiunta con successo false altrimenti
     */
    public synchronized boolean addTransactionWin(double newValue, Timestamp time){
        if(newValue<0 || time == null) return false;
        historyWin.add(time);
        balanceWin += newValue;
        return true;
    }

    /**
     * @effects aggiunge alla lista delle transazioni bitcoin una nuova transazione
     * @param newValue monene da aggiungre al saldo bitcoin
     * @param time data e ora transazione
     * @return true se la transazione è stata aggiunta con successo false altrimenti
     */

    public synchronized boolean addTransactionBtc(double newValue, Timestamp time){
        if(newValue<0 || time == null) return false;
        historyBtc.add(time);
        balanceBtc += newValue;
        return true;
    }

    /**
     *  
     * @param newV monete da settare nel saldo wincoin (invocato per azzerare il saldo dopo una nuova conversione in bitcoin)
     */

    public synchronized void setBalance(double newV){
        this.balanceWin = newV;
        addTransactionWin(newV, new java.sql.Timestamp(System.currentTimeMillis()));
    }
    
}
