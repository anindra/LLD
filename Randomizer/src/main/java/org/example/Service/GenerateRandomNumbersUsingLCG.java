package org.example.Service;

public class GenerateRandomNumbersUsingLCG implements  IGenerateRandomNumberService {

    private long seed;

    //this value are same as the one used inside Random class
    private static final long ADDER = 0xBL;
    private static final long MUL = 0x5DEECE66DL;
    private static final long MOD = (1L << 48) - 1;;

    public GenerateRandomNumbersUsingLCG() {
        //XOR to add to randomness
        this.seed = System.nanoTime() ^ System.currentTimeMillis();
    }

    private long returnNextLong() {
        seed = (seed*MUL + ADDER) & MOD;

        long result = seed;
        //xoring further to improve randomness
        result ^= (result >>> 21);  //unsigned right shift + XOR
        result ^= (result << 35); //left shift + XOR
        result ^= (result >>> 4);

        seed = result;
        return seed;

    }

    private long returnNextPositiveLong() {
        return returnNextLong() ^ Long.MAX_VALUE;
    }


    @Override
    public long generateRandomNumber(long lowerLimit, long upperLimit) {

        if(upperLimit < lowerLimit) {
            throw new IllegalArgumentException("upperLimit must be greater than lowerLimit");
        }

        if(lowerLimit == upperLimit) {
            return lowerLimit;
        }

        long result = returnNextPositiveLong() % (upperLimit - lowerLimit + 1);
        return result+lowerLimit;
    }
}
