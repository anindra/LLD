package org.circularBuffer;

import org.circularBuffer.exception.NoCapacityLeft;
import org.circularBuffer.exception.NoDataToRead;

import java.util.ArrayList;
import java.util.List;

public class CircularBuffer implements ICircularBuffer{

    private int size;
    private int availableSize;
    private List<Character> buffer;
    private int readPointer;
    private int writePointer;

    public CircularBuffer(int size)
    {
        this.size = size;
        this.availableSize = size;
        buffer = new ArrayList<>();
        readPointer = 0;
        writePointer = 0;
    }

    @Override
    public String read(int nChar) throws NoDataToRead {
        if(nChar <= 0)
            return "";

        if(size - availableSize < nChar)
            throw new NoDataToRead(nChar);

        StringBuilder result = new StringBuilder();
        for(int i=0;i<nChar;i++)
        {
            result.append(buffer.get(readPointer++));
            availableSize++;
            if(readPointer >= size)
                readPointer = 0;
        }

        return result.toString();
    }

    @Override
    public void write(String data) throws NoCapacityLeft {
        if(availableSize < data.length())
            throw new NoCapacityLeft();

        for(char c: data.toCharArray())
        {
            buffer.add(writePointer++,c);
            availableSize--;
            if(writePointer >= size)
                writePointer=0;
        }
    }

    @Override
    public int getAvailableSize() {
        return availableSize;
    }

    @Override
    public int getTotalSize() {
        return size;
    }


}
