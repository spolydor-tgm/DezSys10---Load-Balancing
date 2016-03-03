package at.tgm.hfockspolydor.loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Stefan Polydor &lt;spolydor@student.tgm.ac.at&gt;
 * @version 26.02.16
 */
public class Server implements Runnable {
	private static int numbersForgiven = 0;
	private int name = incrementNumbersForgiven();
	// Der Port fuer den Socket
	private int port = 1234;
    // Der Hostname fuer den Socket
	private String host = "localhost";
	private BufferedReader in;
	private boolean run = true;
	private Socket clientSocket;

    // Diese Methode zaehlt hoch wie viele Server schon von dieser Einheit initialisiert wurden
	private int incrementNumbersForgiven() {
		Server.numbersForgiven+= 1;
		return Server.numbersForgiven;
	}

	public Server() {}

	public Server(int port) {
		this.port = port;
	}

	public void setHost(String hostname) {
		host = hostname;
	}

    /**
     * Diese Methode ist fuer die Verbindung zum Load Balancer da.
     * Damit der Load Balancer weiss, welche Server er zum Load Balancen hat
     * und fuer das Empfangen der einzelnen Anfragen die vom Load Balancer weitergeleitet werden
     */
	public void connectToLB() {
		try {
            // Der Socket fuer die Verbindung
			clientSocket = new Socket(host, port);
            // Der Buffered Reader fuer die Anfragen vom Load Balancer
			in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
            // Der PrintWriter der auf den Socket zum Load Balancer ausgelegt ist
			PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.println("SERVER");
            String line;
            if ((line = in.readLine()) != null)
                name = Integer.parseInt(line);
            // Das Zeitformat fuer das Date damit es in der folgenden Maske angezeigt wird dd/MM/yy HH:mm:ss
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            // Das Date Objekt fuer die Feststellung des Sendezeitpunktes der Anfrage fuer den Load Balancer
			Date date = new Date();
            // OutputStream der die Nachricht an den Loadbalancer uebermittelt
			pw.println(df.format(date) + "  : " + "SERVER" + name + " " + InetAddress.getLocalHost());
		} catch (IOException e) {
			System.err.println("Error during getting InputStreamReader");
		}
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
        // Zu erst wird die Verbindung zum Server hergestellt
		connectToLB();
		String line;
		while (run) {
			try {
				if ((line = this.in.readLine()) != null) {
					DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
					Date date = new Date();
					System.out.println(df.format(date) + "  : <- Anfragezeitpunkt; Anfrageverlauf -> " + line);
				}
			} catch (IOException ioe) {
				System.err.println("Error occured");
			}
		}
	}

    /**
     * Zum schliessen der Socketverbindung und um den Thread (die run()) Methode zu beenden
     */
	public void shutDown() {
		run = false;
		try {
			clientSocket.close();
		} catch (IOException e) {}
	}

	/**
	 *
	 * @param args --
	 * @throws InterruptedException --
	 */
	public static void main(String[] args) throws InterruptedException {
		Server server = new Server(12345);
		server.setHost("192.168.1.27");
		Thread t = new Thread(server);
		t.start();
		Thread.sleep(2045);
		Server server2 = new Server(12345);
		server2.setHost("192.168.1.27");
		Thread t2 = new Thread(server2);
		t2.start();
	}
}
