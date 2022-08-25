package name.voided.datatypes.function;

public class Functions {

    public static <R> Function0<R> returnF( R value ) {
        return () -> value;
    }

    public static <T0, R> Function1<T0, R> returnF( R value,
                                                    Class<? extends T0> cT0 ) {
        return ( a ) -> value;
    }

    public static <T0, T1, R> Function2<T0, T1, R> returnF( R value,
                                                            Class<? extends T0> cT0,
                                                            Class<? extends T1> cT1 ) {
        return ( a, b ) -> value;
    }

    public static <T0, T1, T2, R> Function3<T0, T1, T2, R> returnF( R value,
                                                                    Class<? extends T0> cT0,
                                                                    Class<? extends T1> cT1,
                                                                    Class<? extends T2> cT2 ) {
        return ( a, b, c ) -> value;
    }

    public static <T0, T1, T2, T3, R> Function4<T0, T1, T2, T3, R> returnF( R value,
                                                                            Class<? extends T0> cT0,
                                                                            Class<? extends T1> cT1,
                                                                            Class<? extends T2> cT2,
                                                                            Class<? extends T3> cT3 ) {
        return ( a, b, c, d ) -> value;
    }


}
