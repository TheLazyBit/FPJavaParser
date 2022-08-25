package name.voided.datatypes.tuple;

import name.voided.datatypes.function.Function2;
import name.voided.datatypes.function.Function3;
import name.voided.datatypes.function.Function4;

import java.util.Objects;

public final class Tuple3<T0, T1, T2> implements ITuple {

    public final T0 v0;
    public final T1 v1;
    public final T2 v2;

    Tuple3( T0 v0, T1 v1, T2 v2 ) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }


    @Override
    public String toString() {
        return "(" +
                v0 +
                ", " + v1 +
                ", " + v2 +
                ')';
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Tuple3<?, ?, ?> tuple3 = ( Tuple3<?, ?, ?> ) o;
        return Objects.equals( v0, tuple3.v0 ) &&
                Objects.equals( v1, tuple3.v1 ) &&
                Objects.equals( v2, tuple3.v2 );
    }

    @Override
    public int hashCode() {
        return Objects.hash( v0, v1, v2 );
    }

    public <R> R destructure( Function3<T0, T1, T2, R> f ) {
        return Tuples.destructure( f, this );
    }
}
