package name.voided.datatypes.function;

@FunctionalInterface
public interface Function0<R> {
    R apply();

    default R a() {
        return apply();
    }

    default <TNew> Function0<TNew> map( Function0<TNew> f ) {
        return f;
    }
}
