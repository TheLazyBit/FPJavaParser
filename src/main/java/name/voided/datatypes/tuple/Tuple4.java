package name.voided.datatypes.tuple;

import name.voided.datatypes.function.Function2;
import name.voided.datatypes.function.Function4;

import java.util.Objects;

public final class Tuple4<T0, T1, T2, T3> implements ITuple {

    public final T0 v0;
    public final T1 v1;
    public final T2 v2;
    public final T3 v3;

    Tuple4( T0 v0, T1 v1, T2 v2, T3 v3 ) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
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
        Tuple4<?, ?, ?, ?> tuple4 = ( Tuple4<?, ?, ?, ?> ) o;
        return Objects.equals( v0, tuple4.v0 ) &&
                Objects.equals( v1, tuple4.v1 ) &&
                Objects.equals( v2, tuple4.v2 ) &&
                Objects.equals( v3, tuple4.v3 );
    }

    @Override
    public int hashCode() {
        return Objects.hash( v0, v1, v2, v3 );
    }

    public <R> R destructure( Function4<T0, T1, T2, T3, R> f ) {
        return Tuples.destructure( f, this );
    }
}
