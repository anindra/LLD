package org.circularBuffer.exception;

public class NoCapacityLeft extends Exception{

    public NoCapacityLeft()
    {
        System.out.println("Buffer dont have capacity to add the given data");
    }
}
