package server;
import java.rmi.*;
import java.rmi.server.*;

import client.*;

//import java.rmi.registry.*;
import java.util.Vector;

public class ServerImpl extends UnicastRemoteObject implements Server{
	//****************** CAMPI DATI **********************************
	private static final String HOST = "localhost";
	private static final long serialVersionUID = 1L;

	private Vector<Server> serverList = new Vector<Server>();
	
	private String name;
	Vector<Client> clientList = new Vector<Client>();
	Vector<Registro> listaRisorse = new Vector<Registro>();
	ServerGui gui;
	
	//**************** COSTRUTTORE *************************
		ServerImpl(String n)throws RemoteException{ 
			name = n;
			gui = new ServerGui(name,this);
			try{
				refreshList();		
			}catch(Exception e){}
		}
	
	//***************** CLASSI INTERNE ***********************************
		
	// THREAD PER CONNESSIONE DI UN CLIENT
	private class AddClient extends Thread{
		Client c;
		AddClient(Client c){this.c = c;}
		public void run(){
			try{
				synchronized(clientList){
					clientList.add(c);
					String nomeClient = clientList.get(clientList.size()-1).getNome();
					gui.addClient(nomeClient);
					gui.addLog("Il client "+nomeClient +" si è connesso al server "+ name);
					c.setStatus(true);
				}
			}catch(RemoteException r){}
		}
	}
	
	// THREAD PER LA DISCONNESSIONE DI UN CLIENT
	private class DisconnectClient extends Thread{
		Client c;
		DisconnectClient(Client c){this.c = c;}
		public void run(){
			try{
				synchronized(c){
					while(c.isConnected() == true)
						c.wait();
					synchronized(clientList){
						String clientName = c.getNome();
						c.setStatus(false);
						int index = clientList.indexOf(c);
						clientList.remove(index);
						gui.refreshClient(getClientName());
						gui.addLog("Il client "+ clientName +" si è disconnesso.");
					}
			}
			}catch(RemoteException r){}
			catch(Exception exc){}
		}
	}
	
	// THREAD PAER L'AGGIUNTA DI UNA RISORSA AL REGISTRO DEL SERVER
	private class AggiungiRisorsaRegistro extends Thread{
		String s;
		AggiungiRisorsaRegistro(String s){this.s = s;}
		public void run(){
			synchronized(listaRisorse){
				boolean found = false;
				for(int i=0; i<listaRisorse.size() && found==false; ++i)
					if(listaRisorse.get(i).getnomeRisorsa() == s){
						found = true;
					}
				
				if(found == false){
					listaRisorse.add(new Registro(s));			
					gui.addLog("Aggiunta risorsa "+ s +" al registro Risorse Disponibili");
				}
				}
			}
		}
	
	// THREAD CHE PRIMA DELLA DISCONNESSIONE DEL CLIENT ELIMINA IL RIFERIMENTO A RISORSE CHE SPARIRANNO
	private class ControllaRisorseRegistro extends Thread{
		Client c;
		ControllaRisorseRegistro(Client c){ this.c = c;}
		public void run(){
			try{
				synchronized(c){
					Vector<String> listaR = c.getRisorseList();
					for(String s : listaR){
						for (int i=0; i<listaRisorse.size();++i){
							if(s.equals(listaRisorse.get(i).getnomeRisorsa()))
								listaRisorse.get(i).removeOccorrenza();
							if(listaRisorse.get(i).getOccorrenze()==0){
								gui.addLog("Rimossa Risorsa "+listaRisorse.get(i).getnomeRisorsa()+" dal Registro Risorse Disponibili");
								listaRisorse.remove(i);
							}
						}
					}
					c.setStatus(false);
					c.notify(); // risveglio thread della disconnessione
				}
			}catch(RemoteException e){}
		}
	}
	
	// THREAD CHE RESTITUISCE LA LISTA DI CLIENTI CHE POSSIEDONO UNA RISORSA
	private class GetClientWithResource extends Thread{
		String nomeRisorsa;
		int partiRisorsa;
		Vector<Client> list;
		GetClientWithResource(String n, int k, Vector<Client> l){
			nomeRisorsa = n;
			partiRisorsa = k;
			list = l;
		}
		public void run(){
			try{	
			synchronized(listaRisorse){
				//prima lo cerco nei client connessi al server
					if(findResource(nomeRisorsa,partiRisorsa)==true){
						synchronized(clientList){
							for(int i=0; i<clientList.size(); ++i){ 
								Client c = (Client)clientList.get(i);
								if(c.hasRisorsa(nomeRisorsa, partiRisorsa) == true){	
									list.add(c);
								}
									
							}
						}
					}
					else{
						gui.addLog("Reindirizzamento ricerca negli altri server del sistema");
					}
			}
		
		//poi controllo anche negli altri server connessi al sistema
			synchronized(serverList){
				for(int i=0; i<serverList.size(); ++i){
					Server s = (Server)serverList.get(i);
					if(s.findResource(nomeRisorsa, partiRisorsa) == true){
						Object[] vec = s.getClientList();
						synchronized(s){
							for(int j=0; j < vec.length; ++j){
								Client c = (Client)vec[j];
								if(c.hasRisorsa(nomeRisorsa, partiRisorsa) == true /*&& list.contains(c)==false*/)
									{
									list.add(c);		
									}
							}
						}
					}
				}
			}
			}catch(RemoteException e){System.err.println( "Connection Problem"+Thread.currentThread() );}
		}
	}
	
	// *********************** METODI ******************************
	
	public void connect(Client c) throws RemoteException{
		new AddClient(c).start();
	}
	
	public void removeClient(Client c) throws RemoteException{
		new ControllaRisorseRegistro(c).start();
		new DisconnectClient(c).start();
	}
	
	public void aggiungiRisorsa(String s)throws RemoteException{
		new AggiungiRisorsaRegistro(s).start();
	}
	
	// Restituisce TRUE se il SERVER di invocazione contiene CLIENT con la risorsa cercata
	public boolean findResource(String n, int k)throws RemoteException{
		boolean found = false;
		synchronized(listaRisorse){
			for(int i=0; i<listaRisorse.size() && found == false; ++i){
				String s = n+k;
				if(listaRisorse.get(i).getnomeRisorsa().equals(s))
					found = true;
			}
		}
		return found;
	}
	
	
	//Restituisce una lista di client connessi al SISTEMA che hanno la risorsa cercata
	public synchronized Vector<Client> clientiConRisorsa(String n, int k)throws RemoteException{
		Vector<Client> clientToReturn = new Vector<Client>();
		// se non ho trovato la risorsa nel server, la cerco negli altri server
		GetClientWithResource t = new GetClientWithResource(n,k,clientToReturn);
		t.start();
		try{
			t.join();
		}catch(InterruptedException e){System.err.println(Thread.currentThread()+" sospeso");}
		return clientToReturn;
	}
	
	public Object[] getClientList() throws RemoteException{
			if(clientList.isEmpty() == false)
				return clientList.toArray();
			else 
				return null;
		}
	
	public String getName()throws RemoteException{return name;}
	private synchronized String[] getClientName()throws RemoteException{
		String[] names = new String[clientList.size()];
		for(int i=0; i<clientList.size();++i){
			names[i] = clientList.get(i).getNome();
		}
		return names;
	}
	
	//viene invocata per aggiornare la lista dei server connessi al sistema
	public void refreshList()throws Exception{
		String rmiObjName = "rmi://"+HOST+"/"+name;
		Naming.rebind(rmiObjName,this);
		String[] serList = Naming.list(HOST);
		
		for(int i=0; i<serList.length; i++){
			Server s = (Server)(Naming.lookup(serList[i]));
			if(s.equals(this)==false) // QUI C'ERA IL BUG -non aggiungo me stesso-
				addNewServer(s); // NON AGGIUNGEVO GLI ALTRI SERVER, MA MI FACEVO SOLO AGGIUNGERE
			s.addNewServer(this); // mi faccio aggiungere
		}
	}
	public void disconnectServer()throws Exception{
		Naming.unbind("rmi://"+HOST+"/"+name);
		String[] serList = Naming.list(HOST);
		
		for(int i=0; i<serList.length; i++){
			Server s = (Server)(Naming.lookup(serList[i]));
			s.removeServer(this);
		}
	}
	//Aggiungo il nuovo server che si è connesso
	public synchronized void addNewServer(Server s)throws Exception{
		serverList.add(s);
		String[] serList = Naming.list(HOST);
		gui.refresh(serList);
		gui.addLog("Il server "+s.getName()+" si è connesso al sistema.");
	}
	public synchronized void removeServer(Server s)throws Exception{
		String n = s.getName();
		serverList.remove(s);
		String[] serList = Naming.list(HOST);
		gui.refresh(serList);
		gui.addLog("Il server "+n+" si è disconnesso dal sistema.");
	}
	
	
}
