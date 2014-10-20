package client;
import java.rmi.*;

import server.Server;

import java.util.Vector;

public interface Client extends Remote{
	void addRisorsa(String n, int k)throws RemoteException;
	void askResource(String n, int k)throws RemoteException;
	void addRegistro(String client,String parte)throws RemoteException;
	void connectToOtherS(Server s)throws RemoteException;
	void disconnect()throws RemoteException;
	
	//Usato dal server per settare il flag di connessione
	void setStatus(boolean value)throws RemoteException;
	Vector<String> getRisorseList()throws RemoteException;
	boolean hasRisorsa(String n, int k)throws RemoteException;
	String getNome()throws RemoteException;
	int getCapacita()throws RemoteException;
	
	boolean isConnected()throws RemoteException;
	
}
