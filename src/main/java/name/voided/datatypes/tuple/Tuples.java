package name.voided.datatypes.tuple;

import name.voided.datatypes.function.*;

public class Tuples {

    private static final Tuple0 zeroTuple = new Tuple0();

    /*
        CREATORS
     */

    public static Tuple0 of() {
        return zeroTuple;
    }

    public static <T0> Tuple1<T0> of( T0 v0 ) {
        return new Tuple1<>( v0 );
    }

    public static <T0, T1> Tuple2<T0, T1> of( T0 v0, T1 v1 ) {
        return new Tuple2<>( v0, v1 );
    }

    public static <T0, T1, T2> Tuple3<T0, T1, T2> of( T0 v0, T1 v1, T2 v2 ) {
        return new Tuple3<>( v0, v1, v2 );
    }

    public static <T0, T1, T2, T3> Tuple4<T0, T1, T2, T3> of( T0 v0, T1 v1, T2 v2, T3 v3 ) {
        return new Tuple4<>( v0, v1, v2, v3 );
    }

    /*
        DESTRUCTURING
     */

    public static <R> R destructure( Function0<R> f, Tuple0 tuple ) {
        return destructure( f ).a( tuple );
    }

    public static <R> Function1<Tuple0, R> destructure( Function0<R> f ) {
        return ( tuple ) -> f.a();
    }

    public static <T0, R> R destructure( Function1<T0, R> f, Tuple1<T0> tuple ) {
        return destructure( f ).a( tuple );
    }

    public static <T0, R> Function1<Tuple1<T0>, R> destructure( Function1<T0, R> f ) {
        return ( tuple ) -> f.a( tuple.v0 );
    }

    public static <T0, T1, R> R destructure( Function2<T0, T1, R> f, Tuple2<T0, T1> tuple ) {
        return destructure( f ).a( tuple );
    }

    public static <T0, T1, R> Function1<Tuple2<T0, T1>, R> destructure( Function2<T0, T1, R> f ) {
        return ( tuple ) -> f.a( tuple.v0, tuple.v1 );
    }

    public static <T0, T1, T2, R> R destructure( Function3<T0, T1, T2, R> f, Tuple3<T0, T1, T2> tuple ) {
        return destructure( f ).a( tuple );
    }

    public static <T0, T1, T2, R> Function1<Tuple3<T0, T1, T2>, R> destructure( Function3<T0, T1, T2, R> f ) {
        return ( tuple ) -> f.a( tuple.v0, tuple.v1, tuple.v2 );
    }

    public static <T0, T1, T2, T3, R> R destructure( Function4<T0, T1, T2, T3, R> f, Tuple4<T0, T1, T2, T3> tuple ) {
        return destructure( f ).a( tuple );
    }

    public static <T0, T1, T2, T3, R> Function1<Tuple4<T0, T1, T2, T3>, R> destructure( Function4<T0, T1, T2, T3, R> f ) {
        return ( tuple ) -> f.a( tuple.v0, tuple.v1, tuple.v2, tuple.v3 );
    }
}
