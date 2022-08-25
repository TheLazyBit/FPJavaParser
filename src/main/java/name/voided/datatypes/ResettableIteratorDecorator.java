package name.voided.datatypes;

import java.util.Iterator;
import java.util.LinkedList;

final public class ResettableIteratorDecorator<T> implements ResettableIterator<T> {
    private final Iterator<T> source;
    private final LinkedList<T> past = new LinkedList<>();
    private int position = 0;

    public ResettableIteratorDecorator( Iterator<T> source ) {
        this.source = source;
    }

    public void reset() {
        position = 0;
    }

    @Override
    public void clearBuffer() {
        past.clear();
        position = 0;
    }

    @Override
    public boolean hasNext() {
        return ( position < past.size() )
                || source.hasNext();
    }

    @Override
    public T next() {
        if ( position < past.size() )
            return past.get( position++ );

        var n = source.next();
        position++;
        past.add( n );
        return n;
    }
}
