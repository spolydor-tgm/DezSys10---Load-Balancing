package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Polydor &lt;spolydor@student.tgm.ac.at&gt;
 * @version 26.02.16
 */
public class Balancer implements Runnable {
	/**
	 * 0 ... Weighted Distribution
	 * 1 ... Least Connection
	 */
	private int balanceMethod = 0;
	private Map<Socket, Integer> servers = new HashMap<Socket, Integer>();
	private ServerSocket serverSocket;
	private boolean run = true;

	public Balancer(){
		
	}

	public Balancer(int balanceMethod, int port) {
		this.balanceMethod = balanceMethod;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void balance() {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date date = new Date();
		if (servers.isEmpty())
			System.err.println(df.format(date) + "  : " + "Cannot Balance, No Server is connected");
		else {
			switch (balanceMethod) {
				case 0:

					break;
				case 1:

					break;
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

				while ((line = in.readLine()) != null) { //if
					if (line.contains("SERVER"))
						servers.put(clientSocket, servers.size()+1);
					else
						balance();
					System.out.println(line);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		Balancer b = new Balancer(0, 12345);
		Thread t = new Thread(b);
		t.run();
	}
}
