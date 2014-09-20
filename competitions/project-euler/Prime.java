
import java.util.BitSet;

// Hmmm, the difference for calling this "Prime" or "PrimeUtils" is if I want
// to have a `Prime` variable have methods called directly on it, e.g. 
// `prime.nextPrime()` and then it would be kinda modelled after
// java.util.Random. The only other instance method might be `previousPrime()`?
// But, then there still should be static versions of each of those. So, the
// top idea right now is singleton pattern so that the primes table is only
// created once, lazily. Hmmm, but that doesn't seem "right"? Having just
// all static methods and private constructor may work.
// Thought: Is there a need to have a `Prime` instance? Perhaps extend `Number`?
// Nah, for extending `java.lang.Number`. Most/all primes at this time will only be
// int or long, which entire class instances aren't needed for. Maybe in the
// future when primes are much larger, then may need a new "prime" data
// structure. And, at that time, maybe can use "BigPrime".
// Conclusion: Keeping this just a static class for now, until a specific use
// case comes up for having a `Prime` instance. It is easier to go from static
// to instantiable class than the other way around (and less wasted time/work).
// And, choosing to call this "Prime" instead of "PrimeUtils" or "PrimeNumber*"
// because of high math context.
/** Static helper methods for dealing with prime numbers (2, 3, 5, 7, 11...).
 * Note: Testing for prime near Integer.MAX_VALUE has not been tested due to
 * memory constraints.
 * TODO: One possible enhancement is only storing odd numbers in the primesSieve. */
public class Prime {

    public static final int INVALID_PRIME = -1;

    private static final int DEFAULT_TABLE_SIZE = 1000000; // 1 million.
    
    private int mTableSize;
    
    private BitSet mPrimes;

    /** Use `getInstance` to instantiate this class. */
    private Prime(int size) {
        generatePrimesTable(size);
    }
    
    /** Returns a simple instance of this class using
     * DEFAULT_TABLE_SIZE = 1000000, which may change. */
    public static Prime getInstance() {
        return getInstance(DEFAULT_TABLE_SIZE);
    }
    
    /** Returns a simple instance of this class. */
    public static Prime getInstance(int size) {
        return new Prime(size);
    }
    
    /** Creates a table to hold cache of all primes and composites up to size,
     * which is always rounded up to the next multiple of 64. E.g., if input
     * is 64, then table size will be 128 to ensure isPrime(64) doesn't need
     * to regenerate table. */
    private void generatePrimesTable(int size) {
        if (size < 0) { throw new IllegalArgumentException("Size must be non-negative"); }
        
        // Allow table size to be INT_MAX, and prevents being negative from overflow.
        if (size > Integer.MAX_VALUE - 64) { size = Integer.MAX_VALUE - 64; }
        
        mTableSize = ((size + 64) / 64) * 64; // Rounds up to next multiple of 64.
        mPrimes = new BitSet(mTableSize);
        mPrimes.set(0, mTableSize);
        mPrimes.clear(0, 2); // Zero and one are not prime.
        clearMultiples(2); // Doing this to iterate slightly faster.
        for (int i = 3; i < mTableSize; i += 2) {
            if (mPrimes.get(i)) {
                clearMultiples(i);
            }
        }
    }
    
    /** Clears bits that are multiples of number, but not the number itself. */
    private void clearMultiples(int number) {
        for (int i = number * 2; i < mTableSize; i += number) {
            mPrimes.clear(i);
        }
    }
    
    /** Returns true if input is prime, otherwise false. If input is less than
     * two, then this will always return false. If input is greater than or
     * equals to table size, then the table will be regenerated (TODO: This
     * could be done better). */
    public boolean isPrime(int number) {
        if (number <= 1) { return false; }
        if (number >= mTableSize) {
            generatePrimesTable(number);
        }
        return mPrimes.get(number);
    }
    
    /** Returns the nearest larger number that is a prime. If input is less
     * than two, then this will always return 2. This is much more efficient
     * if the next larger prime is already included in the table. If the next
     * larger prime isn't found in table, then the table will be increased by
     * about 1024 numbers. If next largest prime would be greater than
     * Integer.MAX_VALUE, then returns INVALID_PRIME = -1. */
    public int getNext(int number) {
        if (number <= 1) { return 2; }
        if (number >= mTableSize) {
            if (number + 1024 < 0) { return INVALID_PRIME; }
            generatePrimesTable(number + 1024);
        }
        
        for (int i = number + (number % 2 == 0 ? 1 : 2); i < mTableSize; i += 2) {
            if (mPrimes.get(i)) {
                return i;
            }
            // Extend primes table if necessary until a prime is found.
            if (i >= mTableSize - 3) {
            if (mTableSize + 1024 < 0) { return INVALID_PRIME; }
                generatePrimesTable(mTableSize + 1024);
            }
        }
        
        // This should never be reached.
        throw new RuntimeException("Something wrong with regenerating table likely for large input: " + number);
    }
    
    /** Returns primes table. */
    public String debugToString() {
        return mPrimes.toString();
    }
    
    /** Returns the number of primes in table. */
    public int size() {
        return mPrimes.size();
    }

}