package name.voided.datatypes.tuple;

import name.voided.datatypes.function.Function1;
import name.voided.datatypes.function.Function2;
import name.voided.datatypes.function.Function4;

import java.util.Objects;

public final class Tuple2<T0, T1> implements ITuple {

    public final T0 v0;
    public final T1 v1;

    Tuple2( T0 v0, T1 v1 ) {
        this.v0 = v0;
        this.v1 = v1;
    }

    @Override
    public String toString() {
        return "(" +
                v0 +
                ", " + v1 +
                ')';
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Tuple2<?, ?> that = ( Tuple2<?, ?> ) o;
        return v0 == that.v0 &&
                Objects.equals( v1, that.v1 );
    }

    @Override
    public int hashCode() {
        return Objects.hash( v0, v1 );
    }

    public <R> R destructure( Function2<T0, T1, R> f ) {
        return Tuples.destructure( f, this );
    }
}
