package at.tgm.hfockspolydor.loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Stefan Polydor &lt;spolydor@student.tgm.ac.at&gt;
 * @version 26.02.16
 */
public class Balancer implements Runnable {
	/**
	 * 0 ... Weighted Distribution
	 * 1 ... Least Connection
	 */
	private int balanceMethod = 0; // Auswahl der Balancemethode
	private List<ServerList> servers = new ArrayList<ServerList>(); // Socket, Anz. Verbindungen
	private ServerSocket serverSocket; // Serversocket fuer Balancer
	private boolean run = true; // Stoppen des Threads
	private int anzServer = 0; // Speichern der Anzahl der verfuegbaren Server
	private int serverNumber = 1;
	private int wD = 0; // temp fuer Weighted (Servergewichtung)
	private int actState = 0; // damit gespeichert wird, welcher Server als letztes bei Weighted Distribution verwendet wurde

	/**
	 * Default Konstuktor
	 */
	public Balancer(){
		
	}

	/**
	 * Erzeugt einen Balancer, welcher dann mit .run() gestartet werden muss
	 * @param balanceMethod 0 = Weighted Distribution, 1 = Least Connection
	 * @param port auf dem der Balancer Laufen soll
	 */
	public Balancer(int balanceMethod, int port) {
		this.balanceMethod = balanceMethod;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Weighted Distribution - Balancemethode
	 * @return servernumber welcher Server die Anfrage zugewiesen bekommt
	 */
	private int weightedDistribution() {
		if (wD < 2) { // der Erste Server ist doppelt gewichtet, doppelt so Leistungsstark wie die anderen
			actState = 1;
			wD++;
			return actState-1;
		} else { // Wenn der erste Server schon 2 erhalten hat, dann bekommen die anderen die Anfragen
			actState+= 1;
			if (actState == anzServer)
				wD = 0;
		}
		if (! servers.get(actState - 1).socket.isClosed()) // Pruefen, ob Connection zu Server noch open ist, wenn ja, rueckgabe Server
			return actState-1;
		else { // Anfrage ignorieren, variablen setzten, wenn dies auftritt, wird spaeter nochmals die weightedDistribution aufgerufen bis ein passender Server gefunden wird,
			servers.remove(actState - 1);
			actState-= 1;
			anzServer-= 1;
		}
		if (anzServer == 0) // keiner da ist, dann Anfrage ignoriert
			return -1;
		return weightedDistribution();
	}

	private int leastConnection() {
		int temp = 999999;
		int temp2 = 1;
		int count = 1;
		for (ServerList server : servers) {
			if (server.anz < temp) {
				temp = server.anz;
				count = temp2;
				temp2++;
			} else
				temp2++;
		}
		if (! servers.get(count-1).socket.isClosed())
			return count-1;
		else {
			servers.remove(count-1);
			anzServer-= 1;
		}
		if (anzServer == 0)
			return -1;
		return leastConnection();
	}

	public void balance(String request) throws IOException {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date date = new Date();
		int place = 0;
		if (servers.isEmpty()) // Checken, ob ein Server verfuegbar ist, wenn nicht, laeuft der Balancer weiter, und Anfragen nicht weitergeleitet
			System.err.println(df.format(date) + "  : " + "Cannot Balance, No Server is connected");
		else { // Wenn Server verfuegbar, balancen; Verschiedene Balancemethoden
			switch (balanceMethod) {
				case 0:
					place = weightedDistribution();
					break;
				case 1:
					place = leastConnection();
					break;
			}
			if (place == -1)
				System.err.println(df.format(date) + "  : " + "Cannot Balance, No Server is connected");
			else {
				new PrintWriter(servers.get(place).socket.getOutputStream(), true).
						println("\'" + request + ";\' \'" + df.format(date) + "  : " + servers.get(place).sname + "\'");
				servers.get(place).anz += 1;
			}
		}
	}

	public void setBalanceMethod(int method) {
		balanceMethod = method;
	}

	public void shutDown() {
		run = false;
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		try {
			String line;
			while (run) {
				Socket clientSocket = serverSocket.accept();
				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				if ((line = in.readLine()) != null) { //if
					if (line.contains("SERVER")) {
						new PrintWriter(clientSocket.getOutputStream(), true).
								println(serverNumber);
						line = null;
						if ((line = in.readLine()) != null);
						servers.add(new ServerList(clientSocket, 0, line));
						anzServer+= 1;
						serverNumber+= 1;
					} else
						balance(line);
					System.out.println(line);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private class ServerList {
		public Socket socket;
		public int anz;
		public String sname;

		private ServerList(Socket socket, int anz, String sname) {
			this.socket = socket;
			this.anz = anz;
			this.sname = sname.split(":")[3];
		}
	}



	public static void main(String[] args) throws InterruptedException {
		System.out.println("Weighted Distribution is enabled!");
		Balancer b = new Balancer(0, 12345);
		Thread t = new Thread(b);
		t.start();
		/*
		Thread.sleep(30000);
		b.setBalanceMethod(1);
		System.out.println("Least Connection is enabled!");
		*/
	}
}
