package client;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Vector;

import server.Server;

public class ClientImpl extends UnicastRemoteObject implements Client{
	//CAMPI DATI
	private static final long serialVersionUID = 1L;
	String nome;
	int capacita;
	ClientGui gui;
	private Vector<Risorsa> listaRisorse = new Vector<Risorsa>();
	Server serv = null;
	Vector<Server> serverAttivi =  new Vector<Server>();
	Vector<Client> clientConRisorsa = new Vector<Client>();
	Vector<Client> clientAttivi = new Vector<Client>();
	private Vector<Boolean> partsToDownload = new Vector<Boolean>();
	long delay;
	int numThread = 0;
	boolean isConnected = false;
	Object lock = new Object();
	Object download = new Object();
	
	//COSTRUTTORE
	
	ClientImpl(String n, Server s, int c)throws RemoteException{
		nome = n;
		serv = s;
		capacita = c;
		gui = new ClientGui(nome,this);
		//modificare il valore del seguente metodo per allungare/accorciare il tempo di attesa simulato per il download
		setDelay(5000);
		// chiede una connessione al server
		serv.connect(this);
	}
	//METODI
	public synchronized void addRisorsa(String n, int k)throws RemoteException{
		Risorsa r = new RisorsaImpl(n,k);
		listaRisorse.add(r);
		serv.aggiungiRisorsa(n+k);
		String nomeR = listaRisorse.lastElement().getName()+listaRisorse.lastElement().getNumParti();
		gui.addLog("Aggiunta risorsa "+ nomeR);
		gui.addResource(nomeR);
		}
	//chiedo una connessione ad un altro server
	
	public void connectToOtherS(Server s)throws RemoteException{ 
		s.connect(this);
		}
	
	public void disconnect()throws RemoteException{
		serv.removeClient(this);
	}
	
	// Interroga il SERVER chiedendo la ricerca di una risorsa. Inserisce i client con la risorsa nella lista apposita
	public synchronized void askResource(String n, int k)throws RemoteException{
		//synchronized(clientConRisorsa){	
			clientConRisorsa.removeAllElements();
			clientConRisorsa = serv.clientiConRisorsa(n,k);
			//gui.addLog("Ricevuta lista per la risorsa: "+n+" "+k);
			if(clientConRisorsa.isEmpty() == true)
				gui.addLog("Risorsa non presente nel sistema");
			else{
				gui.addLog("Risorsa presente, si procede al download di "+n+k);
				download(n,k);
			}
		//}
	}
	
	private synchronized void download(String nomeRisorsa,int partiRisorsa){
		try{
			gui.initializeQueue(nomeRisorsa, partiRisorsa);
			int[] a = {clientConRisorsa.size(),partiRisorsa,capacita};
			numThread = min(a);
			gui.addLog("numThread:"+numThread);
			int rimanenti = partiRisorsa;
			partsToDownload = new Vector<Boolean>(partiRisorsa);
			
			for(int g=0; g<partiRisorsa; g++)
				partsToDownload.add(false);
			
			//lancio un thread che mi scarica dal client K la parte I della risorsa
				for(int i=0; i< rimanenti && clientConRisorsa.size() != 0; ++i){
	 
					synchronized(lock){
						while(numThread == 0){
							lock.wait(); // Aspetto client occupato
						}
						// Avvio thread numero i
						numThread--;
						new DownloadPart(clientConRisorsa,i,nomeRisorsa).start();
					}
				}				
				if(clientConRisorsa.isEmpty()){
					gui.addLog("Non ci sono pi client da cui scaricare");
					gui.cleanQueue();
					return;
				}
				//controllo se la risorsa  stata scaricata effettivamente
				else{
					synchronized(lock){
				
					while(controllaDownload(partsToDownload, partiRisorsa)==false)
						lock.wait(); // ci sono ancora thread che stanno finendo di scaricare
						gui.addLog("Download Risorsa: "+nomeRisorsa+partiRisorsa+" completato");
						this.addRisorsa(nomeRisorsa, partiRisorsa);
						gui.cleanQueue();
				}		
			}
		}catch(RemoteException e){System.out.println("Problema 1"); }
		 catch(InterruptedException exc){System.out.println("Problema 2");}
	}
	//controlla che tutte le parti siano state scaricate. Il download  simulato dal settaggio di tutte le parti della risorsa a true
	private boolean controllaDownload(Vector<Boolean> b, int k){
		int count = 0;
		for(Boolean boo : b)
			if(boo) count++;
		if(count == k)
			return true;
		return false;					
	}
	// setta il valore della cella a true per simulare il download
	private  void simulateDownload(Vector<Boolean> b, int index){
		b.set(index, true);
	}
	// THREAD per il download
	private class DownloadPart extends Thread{
			Vector<Client> lista;
			int indexOfPart;
			String nameOfResource;
			
			DownloadPart(Vector<Client> l,int indexOfPart, String name)throws RemoteException{
				lista = l;
				this.indexOfPart = indexOfPart;
				nameOfResource = name;
			}
			public void run(){
				boolean scaricata=false;
				//prendo il lock sui clientConRisorsa
				for(int i=0; i<lista.size() && scaricata == false;++i){
						Client c = (Client)lista.get(i);
						if(clientAttivi.contains(c) == false)
						{ 
							clientAttivi.add(c);
							synchronized(c){
							try{
								String nameClientAttivo = c.getNome();
								if(c.isConnected()==true){
								gui.addQueue(nameOfResource+":Parte"+indexOfPart+" In Download da "+nameClientAttivo, indexOfPart);
								sleep(delay); //simulo tempo scaricamento

								simulateDownload(partsToDownload,indexOfPart); // simulo lo scaricamento
								scaricata = true;
								gui.addLog("Scaricata "+nameOfResource+":"+indexOfPart);			
								gui.addQueue(nameOfResource+":Parte"+indexOfPart+ " Scaricata da "+nameClientAttivo, indexOfPart);
								c.addRegistro(nome, nameOfResource+":"+indexOfPart);
								}
								if(c.isConnected()==false){
									gui.addLog("Il client "+c.getNome()+" si  disconnesso");
									clientConRisorsa.remove(c);
								}
								clientAttivi.remove(c);
							}catch(InterruptedException e){System.err.println("Problemi durante il download Interrupted Exc");}
							catch(RemoteException ex){}
						}
					}
				}
				synchronized(lock){
					numThread++; // risveglio il thread che sta aspettando per lanciare altri download
					lock.notifyAll();
				}
			}
		}
	
	
	// Ritorna l'elemento minore di un array. PRECOND: l'array contiene solo elementi > 0(zero)
	private int min(int[] a){
		int minore = a[0];
		for(int i = 1; i<a.length; ++i){
			if(a[i] < minore)
				minore = a[i];
		}
		return minore;
	}
	//METODI GET
	
	public String getNome()throws RemoteException{return nome;}
	public int getCapacita()throws RemoteException{return capacita;}
	public Vector<String> getRisorseList()throws RemoteException{
		Vector<String> lista = new Vector<String>();
		for(int i=0; i<listaRisorse.size(); ++i){
			String nomeR = listaRisorse.get(i).getName();
			Integer partiR = listaRisorse.get(i).getNumParti(); 
			lista.add(nomeR+partiR);
			gui.addLog(nomeR+partiR);
		}
		return lista;
	}
	
	public synchronized void setStatus(boolean value)throws RemoteException{
		isConnected = value;
		this.notifyAll();
	}
	public boolean isConnected(){return isConnected;}
	
	//Interroga il client per sapere se ha una determinata risorsa
	public boolean hasRisorsa(String n, int k)throws RemoteException{
		boolean found = false;
		for(int i=0; i<listaRisorse.size() && found == false; ++i){
			Risorsa r = listaRisorse.get(i);
			if(r.isTheSame(n,k) == true){
				found = true;
				gui.addLog("RICHIESTA RISORSA: "+n+k);
			}
		}
		return found;
	}
	public void addRegistro(String client,String parte)throws RemoteException{
		gui.addRegistro("Il Client: "+client+" ha scaricato "+parte);
	}
	//METODI SET
	private void setDelay(long l){
		delay = l;
	}
}