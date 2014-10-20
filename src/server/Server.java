package server;
import java.rmi.*;

import client.Client;

import java.util.Vector;

public interface Server extends Remote{
	String getName() throws RemoteException;
	Object[] getClientList() throws RemoteException;
	public void disconnectServer()throws Exception;
	Vector<Client> clientiConRisorsa(String n, int k)throws RemoteException;
	boolean findResource(String n, int k)throws RemoteException;
	void connect(Client c) throws RemoteException;
	void removeClient(Client c) throws RemoteException;
	//void addServer() throws RemoteException;
	public void refreshList()throws Exception;
	public void addNewServer(Server s)throws Exception;
	public void removeServer(Server s)throws Exception;
	public void aggiungiRisorsa(String s)throws RemoteException;
}
