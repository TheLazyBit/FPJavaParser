package name.voided.datatypes;

import java.util.Iterator;

public interface ResettableIterator<T> extends Iterator<T> {
    void reset();

    void clearBuffer();
}
