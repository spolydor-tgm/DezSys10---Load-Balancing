package at.tgm.hfockspolydor.loadbalancer;

/**
 * @author Stefan Polydor &lt;spolydor@student.tgm.ac.at&gt;
 * @version 26.02.16
 */
public class CalculatorImpl implements Calculator{
	public double pi(int iterations) {
		double res = 0;
		for (int i = 1; i < iterations; i += 4)
			res += 1.0 / i - 1.0 / (i + 2);
		return res*4;
	}
}
