package server;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class ServerStarter {
	private static final String HOST = "localhost";
	
	public static void main(String[] args)throws Exception{
		try{
			if(args.length >= 0){
				String nomeServer = args[0];
				ServerImpl server = new ServerImpl(nomeServer);
				//String rmiObjName = "rmi://"+HOST+"/"+nomeServer;
				//System.out.println(rmiObjName);
				//Naming.rebind(rmiObjName, server);
				//System.out.println("Creato server:"+nomeServer);
			}
		}catch(RemoteException e){System.err.println("RMIRegistry alredy exists");}
		catch(Exception ex){ex.printStackTrace();}
}
}
