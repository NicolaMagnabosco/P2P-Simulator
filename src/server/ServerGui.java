package server;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

public class ServerGui extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextArea clientText;
	JTextArea serverText;
	JTextArea logText;
	JList logList;
	JScrollPane logScroller;
	JScrollPane scroll;
	JPanel client;
	JPanel server;
	JPanel log;
	Server SER;
	private DefaultListModel logModel = new DefaultListModel();
	
	public void addClient(String s){
		clientText.append(s+"\n");
	}
	public void addServer(String s){
		serverText.append(s+"\n");
	}
	public void addLog(String s){
		int index = logModel.size();
		logModel.add(index, s);
	}
	public void refreshClient(String[] s){
		clientText.setText("");
		for(int i=0; i<s.length;++i){
			clientText.append(s[i]+"\n");
		}
	}
	// METODO CHE AGGIORNA LA LISTA SERVER
	public void refresh(String[] s){
		serverText.setText("");
		for(int i=0; i<s.length; ++i){
			serverText.append(s[i]+"\n");
		}
	}
	// METODO PER DISEGNARE UNA TEXT AREA
	private void setUpTextArea(JPanel p, JTextArea a, String s){
		p.setBorder(BorderFactory.createTitledBorder(s));
		
		a.setSize(100,100);
		a.setBackground(Color.WHITE);
		a.setEditable(false);
		a.setAutoscrolls(true);
		a.setBorder(
				BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED,Color.DARK_GRAY,Color.DARK_GRAY), s ) );
	}
	private class ActionChiusura implements WindowListener{
        JFrame es;
        ActionChiusura(JFrame f){
            es=f;
        }
        public void windowClosing(WindowEvent e)
        {
            try
            {
                SER.disconnectServer();
            }

            catch(Exception exc){ exc.printStackTrace();
            }
        } 
        public void windowClosed(WindowEvent e) {        } 
        public void windowOpened(WindowEvent e) {} 
        public void windowIconified(WindowEvent e) {} 
        public void windowDeiconified(WindowEvent e) {} 
        public void windowActivated(WindowEvent e) {} 
        public void windowDeactivated(WindowEvent e) {}
        
    }
	// COSTRUTTORE
	ServerGui(String name,Server s){
		super(name);
		SER = s;
		client = new JPanel();
		server = new JPanel();
		log = new JPanel();
		
		serverText = new JTextArea(2,10);
		clientText = new JTextArea(2,10);
		logList = new JList(logModel);
		
		logList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		logList.setLayoutOrientation(JList.VERTICAL);
		logScroller = new JScrollPane(logList);
		logScroller.setPreferredSize(new Dimension(250, 80));
		logScroller.setBorder(BorderFactory.createTitledBorder("Log"));
		
		setUpTextArea(server,serverText,"Server Connessi");
		setUpTextArea(client,clientText,"Client Connessi");
		
		//***************AGGIUNGO AL LAYOUT***********
		client.add(clientText);
		server.add(serverText);
		
		add(server,BorderLayout.PAGE_START);
		add(client,BorderLayout.CENTER);
		add(logScroller,BorderLayout.SOUTH);
		
		//************* SET UP JFRAME *************
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowListener chiusura=new ActionChiusura(this);
        addWindowListener(chiusura);
		this.setVisible(true);
		this.setSize(400,400);
		this.setMinimumSize(new Dimension(350,350));
	}	
}
