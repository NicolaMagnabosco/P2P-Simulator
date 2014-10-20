package client;
import java.rmi.*;

public interface Risorsa extends Remote{
	String getName()throws RemoteException;
	int getNumParti()throws RemoteException;
	boolean isTheSame(String s, int k)throws RemoteException;
}
