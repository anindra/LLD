package org.example.Controller;

public interface IRandomNumber {

    long generateRandonNumber(long lowerLimit, long upperLimit);

    long generateRandomNumberWithOnlyUpperLimit(long upperLimit);

    long generateRandomNumberWithOnlyLowerLimit(long lowerLimit);
}
