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
	private int port = 1234;
	private String host = "localhost";
	private BufferedReader in;
	private boolean run = true;
	private Socket clientSocket;

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

	public void connectToLB() {
		try {
			clientSocket = new Socket(host, port);
			in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Date date = new Date();
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

	public void shutDown() {
		run = false;
		try {
			clientSocket.close();
		} catch (IOException e) {}
	}

	public static void main(String[] args) throws InterruptedException {
		Server server = new Server(12345);
		Thread t = new Thread(server);
		t.start();
		Thread.sleep(2045);
		Server server2 = new Server(12345);
		Thread t2 = new Thread(server2);
		t2.start();
	}
}
