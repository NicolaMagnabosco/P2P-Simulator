package client;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Vector;

public class RisorsaImpl extends UnicastRemoteObject implements Risorsa{
	private String nome;
	private int n_parti;
	
	public RisorsaImpl(String n, int p)throws RemoteException{
		nome = n;
		n_parti = p;
		
	}
	public String getName()throws RemoteException{return nome;}
	public int getNumParti()throws RemoteException{return n_parti;}
	// Controlla se due risorse sono uguali, ossia se hanno lo stesso nome e lo stesso numero di parti
	public boolean isTheSame(String s, int k)throws RemoteException{
		
		if(this.getName().equals(s) && this.getNumParti() == k)
			return true;
		else
			return false;
					
	}
}
