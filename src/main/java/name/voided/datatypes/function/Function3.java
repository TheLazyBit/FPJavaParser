package name.voided.datatypes.function;

@FunctionalInterface
public interface Function3<T0, T1, T2, R> {

    R apply( T0 a, T1 b, T2 c );

    default R a( T0 a, T1 b, T2 c ) {
        return apply( a, b, c );
    }

    default Function2<T1, T2, R> apply( T0 a ) {
        return ( b, c ) -> apply( a, b, c );
    }

    default Function2<T1, T2, R> a( T0 a ) {
        return apply( a );
    }

    default Function1<T2, R> apply( T0 a, T1 b ) {
        return ( c ) -> apply( a, b, c );
    }

    ;

    default Function1<T2, R> a( T0 a, T1 b ) {
        return apply( a, b );
    }

    default <TNew> Function3<T0, T1, T2, TNew> map( Function1<R, TNew> f ) {
        return ( a, b, c ) -> f.a( a( a, b, c ) );
    }
}
