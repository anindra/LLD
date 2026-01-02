package org.circularBuffer;

import org.circularBuffer.exception.NoCapacityLeft;
import org.circularBuffer.exception.NoDataToRead;

public interface ICircularBuffer {

    String read(int nChar) throws NoDataToRead;

    void write(String data) throws NoCapacityLeft;

    int getAvailableSize();

    int getTotalSize();

}
