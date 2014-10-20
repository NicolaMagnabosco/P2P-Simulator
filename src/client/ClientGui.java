package client;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ClientGui extends JFrame{
	private static final long serialVersionUID = 1L;
	private Client client;
	private JButton disconnect;
	private JButton searchButton;
	private JTextField searchName;
	private JTextField searchPart;
	private JList risorseList;
	private JList logList;
	private JList queueList;
	private JList registroList;
	private JScrollPane risorseScroller;
	private JScrollPane logScroller;
	private JScrollPane queueScroller;
	private JScrollPane registroScroller;
	private DefaultListModel risorseModel = new DefaultListModel();
	private DefaultListModel logModel = new DefaultListModel();
	private DefaultListModel queueModel = new DefaultListModel();
	private DefaultListModel registroModel = new DefaultListModel();
	private JPanel searchPanel = new JPanel();
	private JPanel bottom = new JPanel();
	private JPanel center = new JPanel();
	
	private class Search extends Thread{
		public void run(){
			try{
				int i = (Integer.parseInt(searchPart.getText()));
				if(i>0 && searchName.getText().isEmpty() == false)
					client.askResource(searchName.getText(), i);
				else
					JOptionPane.showMessageDialog(null, "Inserire parametri di ricerca validi. Es: Nome=A, Parti=7");
			}catch(Exception e){}
		}
	}
	
	private void setUpScroller(JList list, JScrollPane scroller, DefaultListModel model){
		
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		scroller.setPreferredSize(new Dimension(200, 100));
		scroller.setBorder(BorderFactory.createTitledBorder(scroller.getName()));
	}
	
	ClientGui(String name, Client c){
		super(name);
		client = c;
		searchName = new JTextField(10);
		searchName.setBorder(BorderFactory.createTitledBorder("Nome Risorsa"));
		searchName.setSize(new Dimension(20,5));
		searchPart = new JTextField(5);
		searchPart.setBorder(BorderFactory.createTitledBorder("N¡ Parti"));
		searchPart.setSize(new Dimension(20,5));
		searchButton = new JButton("Cerca");
		searchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Search().start();
			}
		});
		searchPanel.add(searchName);
		searchPanel.add(searchPart);
		searchPanel.add(searchButton,BorderLayout.SOUTH);
		searchPanel.setBorder(BorderFactory.createTitledBorder("Cerca Risorse"));
		
		registroList = new JList(registroModel);
		registroScroller = new JScrollPane(registroList);
		registroScroller.setName("Registro Richieste");
		setUpScroller(registroList,registroScroller,registroModel);
		
		risorseList = new JList(risorseModel);
		risorseScroller = new JScrollPane(risorseList);
		risorseScroller.setName("Lista Risorse");
		setUpScroller(risorseList,risorseScroller,risorseModel);
		
		logList = new JList(logModel);
		logScroller = new JScrollPane(logList);
		logScroller.setName("Log");
		setUpScroller(logList,logScroller,logModel);
		
		queueList = new JList(queueModel);
		queueScroller = new JScrollPane(queueList);
		queueScroller.setName("Coda Download");
		setUpScroller(queueList,queueScroller,queueModel);
		
		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(new ActionListener(){//classe anonima
			public void actionPerformed(ActionEvent e){
				try{
					client.disconnect();
					}catch(Exception ex){}
				synchronized(this){
					searchButton.setEnabled(false);
				}
			}
		});
		
		bottom.add(logScroller);
		bottom.add(disconnect);
		bottom.add(registroScroller);
		center.add(risorseScroller);
		center.add(queueScroller);
		
		add(searchPanel,BorderLayout.PAGE_START);
		add(center,BorderLayout.CENTER);
		add(bottom,BorderLayout.PAGE_END);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setSize(430,350);
		this.setMinimumSize(new Dimension(430,350));
	}
		
		public void initializeQueue(String s, int part){
			for(int i=0; i<part; i++)
				queueModel.add(queueModel.getSize(), s+":"+i);
		}
		public synchronized void addQueue(String s, int index){
			if(index < queueModel.getSize()){
				queueModel.remove(index);
				queueModel.add(index, s);
			}
		}
		public synchronized void cleanQueue(){
			queueModel.clear();
		}
		public void addResource(String s){
			int index = risorseModel.getSize();
			risorseModel.add(index, s);
		}
		public void addLog(String s){
			int index = logModel.getSize();
			logModel.add(index, s);
		}
		public synchronized void hideSearch(boolean b){
			searchButton.setEnabled(b);
		}
		public synchronized void addRegistro(String s){
			int index = registroModel.getSize();
			logModel.add(index, s);
		}
}
