package server;

public class Registro{
	String nomeRisorsa;
	int occorrenze = 1;
	Registro(String s){nomeRisorsa = s;}
	void addOccorrenza(){occorrenze++;}
	void removeOccorrenza(){occorrenze--;}
	int getOccorrenze(){return occorrenze;}
	String getnomeRisorsa(){return nomeRisorsa;}
}
