package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Stefan Polydor &lt;spolydor@student.tgm.ac.at&gt;
 * @version 26.02.16
 */
public class Client {
    private static int anfrageNr = 0;
    private int anfrage = incrementAnfrageNrForgiven();
    private int port;
    private String host;

    public Client(String host, int port){
        this.port = port;
        this.host = host;
        try {
            Socket echoSocket = new Socket(host, port);
            PrintWriter out =
                    new PrintWriter(echoSocket.getOutputStream(), true);
            DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Date date = new Date();
            out.println(df.format(date) + "  : " + "CLIENT Anfr:" + anfrage);
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

    private int incrementAnfrageNrForgiven() {
        Client.anfrageNr+= 1;
        return Client.anfrageNr;
    }

    public static void main(String[] args) throws InterruptedException {
        Client cl1 = new Client("localhost", 12345);
            Thread.sleep(2000);
        Client cl2 = new Client("localhost", 12345);
            Thread.sleep(2000);
        Client client = new Client("localhost", 12345);
            Thread.sleep(4000);
        Client client2 = new Client("localhost", 12345);
            Thread.sleep(20000);
        Client client3 = new Client("localhost", 12345);
    }
}
