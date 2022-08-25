package name.voided.datatypes;

import java.util.Iterator;

final public class ImmutableStepper<E> implements IStepper<E> {
    private final Iterator<E> iter;
    private final E current;
    private final boolean hasContent;
    private ImmutableStepper<E> step;
    private final E lineBreak;
    private final Position position;

    public ImmutableStepper( Iterator<E> iter, E lineBreak ) {
        this( iter, lineBreak, new Position( 0, 0 ) );
    }

    private ImmutableStepper( Iterator<E> iter, E lineBreak, Position position ) {
        this.iter = iter;
        if ( iter.hasNext() ) {
            current = iter.next();
            hasContent = true;
        } else {
            current = null;
            hasContent = false;
        }
        this.position = position;
        this.lineBreak = lineBreak;
    }

    @Override
    public boolean canStep() {
        return step != null || iter.hasNext();
    }

    @Override
    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public E get() {
        return current;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public ImmutableStepper<E> step() {
        if ( step == null && !hasContent ) return this;
        if ( step == null ) {
            if ( get().equals( lineBreak ) )
                step = new ImmutableStepper<E>( iter, lineBreak, position.incrementLine() );
            else
                step = new ImmutableStepper<E>( iter, lineBreak, position.incrementColumn() );
        }
        return step;
    }
}
