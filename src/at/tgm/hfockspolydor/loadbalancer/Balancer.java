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
	 * Bei uns ist der Erste Server doppelt so stark gewichtet, wie alle
	 * anderen, da er einfach doppelt so leistungsfaehig ist.
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

	/**
	 * Least Connection - Balancemethode
	 * Der Server, der die wenigsten Sessions/Verbindungen aufweist,
	 * bekommt die Request
	 * @return
	 */
	private int leastConnection() {
		int temp = 999999;
		int temp2 = 1;
		int count = 1; // Rueckgabe, welcher Server verwendet werden soll
		for (ServerList server : servers) {
			if (server.anz < temp) { // Wenn aktueller Server bei for-each niedrigere Anzahl an Verbindungen aufweist, wird dieser verwendet
				temp = server.anz; // speichern der aktuell niedrigsten Anzahl an Verbindungen der bis jetzt untersuchten Server
				count = temp2; // Speichern des Index, des Servers
				temp2++; // Erhoehen des Indexes
			} else // Wenn die Anzahl der Verbindungen hoeher ist, Index weiterzaehlen
				temp2++;
		}
		if (! servers.get(count-1).socket.isClosed()) // Pruefen, ob Connection zu Server noch open ist, wenn ja, rueckgabe Server
			return count-1;
		else { // Anfrage ignorieren, variablen setzten, wenn dies auftritt, wird spaeter nochmals die leastConnection aufgerufen bis ein passender Server gefunden wird,
			servers.remove(count-1);
			anzServer-= 1;
		}
		if (anzServer == 0) // keiner da ist, dann Anfrage ignoriert
			return -1;
		return leastConnection();
	}

	/**
	 * Diese Methode ist fuer das Balancen zustaendig und ruft je nach
	 * gesetzter BalanceMethode diese auf. Davor prueft sie, ob ueberhaupt
	 * ein Server verfuegbar ist. Wenn ein Server von der entsprechenden
	 * Methode ausgewaehlt ist, wird an diesen eine Nachricht geschickt,
	 * mit dem Request. Falls die Balancemethode feststellen sollte, dass
	 * kein Server verfuegbar ist, wird die Anfrage "ignoriert", da sie ja
	 * an keinen Server verschickt werden kann.
	 * @param request Request Line vom Client, die dann erweitert an den
	 *                Server gesendet wird
	 * @throws IOException
	 */
	public void balance(String request) throws IOException {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date date = new Date();
		int place = 0;
		if (servers.isEmpty()) // Checken, ob ein Server verfuegbar ist, wenn nicht, laeuft der Balancer weiter, und Anfragen nicht weitergeleitet
			System.err.println(df.format(date) + "  : " + "Cannot Balance, No Server is connected");
		else { // Wenn Server verfuegbar, balancen; Verschiedene Balancemethoden
			switch (balanceMethod) { // Balancemethdoen auswaehlen
				case 0:
					place = weightedDistribution();
					break;
				case 1:
					place = leastConnection();
					break;
			}
			if (place == -1) // Wenn kein Server verfuegbar ist, Ausgabe
				System.err.println(df.format(date) + "  : " + "Cannot Balance, No Server is connected");
			else { // Wenn erfolgreich ein Server fuer den Request ausgewaehlt wurde, wird dieser an ihn gesendet
				new PrintWriter(servers.get(place).socket.getOutputStream(), true).
						println("\'" + request + ";\' \'" + df.format(date) + "  : " + servers.get(place).sname + "\'");
				servers.get(place).anz += 1; // Serversessions erhoehen, fuer Least connection, damit auch von weightedDistribution zu leastConnection nahtlos umgeschalten werden kann
			}
		}
	}

	/**
	 * Damit kann die BalanceMethode geaendert werden, waehrend des Betriebs
	 * @param method 0 = Weighted Distribution, 1 = Least Connection
	 */
	public void setBalanceMethod(int method) {
		balanceMethod = method;
	}

	/**
	 * Um den Thread (die run()) Methode zu beenden
	 */
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
			while (run) { // laufen, bis shutDown() aufgerufen wird
				Socket clientSocket = serverSocket.accept(); // Eingehende Verbindung annehmen
				BufferedReader in = new BufferedReader( // InputStreamReader oeffnen, damit eingehende Nachrichten empfangen werden koennen
						new InputStreamReader(clientSocket.getInputStream()));

				if ((line = in.readLine()) != null) { //if, warten, bis Nachricht eintrifft
					if (line.contains("SERVER")) { // Unterscheiden, ob SERVER oder CLIENT
						new PrintWriter(clientSocket.getOutputStream(), true). // OutputStream oeffnen, damit wir ID/Name des Servers zurueckschicken koennen
								println(serverNumber);
						line = null; // null setzen, da sonst nicht auf die korrekte Registrierung des Servers gewartet werden kann
						if ((line = in.readLine()) != null); // Warten bis der Server sich offiziell registriert
						servers.add(new ServerList(clientSocket, 0, line)); // Server der Liste hinzufuegen
						anzServer+= 1;
						serverNumber+= 1;
					} else // Wenn CLIENT Request, dann Blance aufrufen und die Request mitgeben
						balance(line);
					System.out.println(line); // Ausgabe am Balancer ueber Eingang
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Speichert die Server (Socket, Anzahl Verbindungen und Servername)
	 */
	private class ServerList {
		public Socket socket;
		public int anz;
		public String sname;

		/**
		 * Object um den Server passend speichern zu koennen
		 * @param socket Socket der Verbindung zum Server
		 * @param anz der aktuellen Verbindungen, wird spaeter erhoeht/verringert
		 * @param sname der Name des Servers
		 */
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
