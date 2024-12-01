package businessLogic;

import java.util.HashMap;
import java.util.Map;

public class PrimeManager {
    private final Map<Integer, Integer> primeFactorCountCache = new HashMap<>();

    public int countPrimeFactors(int n) {
        if (primeFactorCountCache.containsKey(n)) {
            return primeFactorCountCache.get(n);
        }

        int count = 0;
        int originalN = n;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            while (n % i == 0) {
                count++;
                n /= i;
            }
        }
        if (n > 1) {
            count++;
        }

        primeFactorCountCache.put(originalN, count);
        return count;
    }

    public boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }
}
