package name.voided.datatypes.function;

@FunctionalInterface
public interface Function4<T0, T1, T2, T3, R> {

    R apply( T0 a, T1 b, T2 c, T3 d );

    default R a( T0 a, T1 b, T2 c, T3 d ) {
        return apply( a, b, c, d );
    }

    default Function3<T1, T2, T3, R> apply( T0 a ) {
        return ( b, c, d ) -> apply( a, b, c, d );
    }

    default Function3<T1, T2, T3, R> a( T0 a ) {
        return apply( a );
    }

    default Function2<T2, T3, R> apply( T0 a, T1 b ) {
        return ( c, d ) -> apply( a, b, c, d );
    }

    default Function2<T2, T3, R> a( T0 a, T1 b ) {
        return apply( a, b );
    }

    default Function1<T3, R> apply( T0 a, T1 b, T2 c ) {
        return ( d ) -> apply( a, b, c, d );
    }

    ;

    default Function1<T3, R> a( T0 a, T1 b, T2 c ) {
        return apply( a, b, c );
    }


    default <TNew> Function4<T0, T1, T2, T3, TNew> map( Function1<R, TNew> f ) {
        return ( a, b, c, d ) -> f.a( a( a, b, c, d ) );
    }
}
