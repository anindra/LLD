package org.circularBuffer.exception;

public class NoDataToRead extends Exception{

    public NoDataToRead(int nChar)
    {
        System.out.println("Buffer doesn't have "+nChar+" to read");
    }
}
