package name.voided.parser.templates;

import name.voided.datatypes.*;
import name.voided.datatypes.IStepper.Position;
import name.voided.datatypes.tuple.Tuple2;
import name.voided.datatypes.tuple.Tuple3;

@FunctionalInterface
public interface ParserFunction<TIn, TOut> {
    Result<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> parse( ImmutableStepper<TIn> source );
}
