package name.voided.datatypes.function;

@FunctionalInterface
public interface Function2<T0, T1, R> {

    R apply(T0 a, T1 b);
    default R a(T0 a, T1 b) { return apply(a, b); }

    default Function1<T1, R> apply(T0 a) { return (b) -> apply(a, b); };
    default Function1<T1, R>  a(T0 a) { return apply(a); }

    default <TNew> Function2<T0, T1, TNew> map( Function1<R, TNew> f ) {
        return ( a, b ) -> f.a( a( a, b ) );
    }
}
