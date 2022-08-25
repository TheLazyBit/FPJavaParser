package name.voided.datatypes.tuple;

import name.voided.datatypes.function.Function0;
import name.voided.datatypes.function.Function4;

import java.util.Objects;

public final class Tuple0 implements ITuple {

    Tuple0() {
    }

    @Override
    public String toString() {
        return "(" +
                ')';
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    public <R> R destructure( Function0<R> f ) {
        return Tuples.destructure( f, this );
    }
}
