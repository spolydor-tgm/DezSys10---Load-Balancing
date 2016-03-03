package at.tgm.hfockspolydor.loadbalancer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Stefan Polydor &lt;spolydor@student.tgm.ac.at&gt; & Hagen Aad Fock &lt;hfock@student.tgm.ac.at&gt;
 * @version 03.03.16
 */
public class Client {
    private static int anfrageNr = 0; // Auf 2 aendern
    private int anfrage = incrementAnfrageNrForgiven();
    private int port;
    private String host;

    /**
     * Konstruktor fuer die Client Klasse
     * Erstellt die Verbindung zum Load Balancer und schickt Anfragen
     * @param host Die Ip Adresse vom Load Balancer
     * @param port Der Port vom Load Balancer
     */
    public Client(String host, int port){
        this.port = port;
        this.host = host;
        try {
            // Der Socket für die Verbindung zum Load Balancer
            Socket echoSocket = new Socket(host, port);
            // Der PrintWriter der auf den Socket zum Load Balancer ausgelegt ist
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            // Das Zeitformat fuer das Date damit es in der folgenden Maske angezeigt wird dd/MM/yy HH:mm:ss
            DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            // Das Date Objekt fuer die Feststellung des Sendezeitpunktes der Anfrage fuer den Load Balancer
            Date date = new Date();
            // OutputStream der die Nachricht an den Loadbalancer uebermittelt
            out.println(df.format(date) + "  : " + "CLIENT " + InetAddress.getLocalHost() + " Anfr:" + anfrage);
            // Das Sout damit man sieht was der Client geschickt hat
            System.out.println(df.format(date) + "  : " + "CLIENT" + anfrage + " an LB Adr: " + host + ":" + port);
            /*
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }
            */
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
            System.exit(1);
        }

    }

    /**
     * In dieser Methode wird mitgezaehlt wie viele Anfragen von diesem Client geschickt wurden
     * @return Anzahl der Anfragen
     */
    private int incrementAnfrageNrForgiven() {
        Client.anfrageNr+= 1;
        return Client.anfrageNr;
    }

    /**
     * die Main erstellt 5 Client Objekte fuer die Verbindung zum Load Balancer
     * @param args --
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        Client cl1 = new Client("192.168.1.27", 12345);
            Thread.sleep(2000);
        Client cl2 = new Client("192.168.1.27", 12345);
            Thread.sleep(2000);
        Client client = new Client("192.168.1.27", 12345);
            Thread.sleep(2000);
        Client client2 = new Client("192.168.1.27", 12345);
            Thread.sleep(2000);
        Client client3 = new Client("192.168.1.27", 12345);
    }
}
