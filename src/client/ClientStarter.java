package client;
import server.Server;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;

public class ClientStarter {
	private static final String HOST = "localhost";
	
	public static void main(String[] args)throws Exception{
		try{
			if(args.length >= 3){
				String nameClient = (String)args[0];
				String nameServer = (String)args[1];
				int capacita = Integer.valueOf(args[2]);
				Server server = (Server)Naming.lookup("rmi://"+HOST+"/"+nameServer);
				Client client = new ClientImpl(nameClient,server,capacita);
		
				List<String> nomeRis = new ArrayList<String>();
				List<Integer> numRis = new ArrayList<Integer>();
				if(args.length > 3){
					//ho almeno una risorsa passata con sintassi "Nome Ris" "Num Parti"
					for(int i = 2; i<args.length; i++){
						if(i%2 == 1){ //  il nome della risorsa
							nomeRis.add(args[i]);
						}
						else
							numRis.add(Integer.valueOf(args[i]));
					}
					//creo un nuovo array di risorse in base a quante ne ho trovate
					//riempio l'array di risorse
					for(int j = 0; j<nomeRis.size();j++){
						client.addRisorsa(nomeRis.get(j), numRis.get(j));
					}
				}
				
			}
			else
				System.out.println("Mancano parametri del main");
			
		}catch(RemoteException e){System.err.println("RMIRegistry already exists");}
		catch(Exception ex){ex.printStackTrace();}
	}
}
