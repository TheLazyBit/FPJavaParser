package name.voided.datatypes.tuple;

import name.voided.datatypes.function.Function0;
import name.voided.datatypes.function.Function1;
import name.voided.datatypes.function.Function4;

import java.util.Objects;

public final class Tuple1<T0> implements ITuple {

    public final T0 v0;

    Tuple1( T0 v0 ) {
        this.v0 = v0;
    }

    @Override
    public String toString() {
        return "(" +
                v0 +
                ')';
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Tuple1<?> that = ( Tuple1<?> ) o;
        return v0 == that.v0;
    }

    @Override
    public int hashCode() {
        return Objects.hash( v0 );
    }

    public <R> R destructure( Function1<T0, R> f ) {
        return Tuples.destructure( f, this );
    }
}
