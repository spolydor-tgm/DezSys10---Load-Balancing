package src;

import java.util.HashMap;

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
	private HashMap<String, Integer> servers = new HashMap();

	public void balance() {
		switch (balanceMethod) {
			case 0:

				break;
			case 1:

				break;

		}
	}

	public void setBalanceMethod(int method) {
		balanceMethod = method;
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
		balance();
	}
}
