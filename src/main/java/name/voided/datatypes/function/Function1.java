package name.voided.datatypes.function;

@FunctionalInterface
public interface Function1<T0, R> {
    R apply( T0 a );

    default R a( T0 a ) {
        return apply( a );
    }

    default <TNew> Function1<T0, TNew> map( Function1<R, TNew> f ) {
        return ( a ) -> f.a( a( a ) );
    }
}
