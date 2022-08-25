package name.voided.parser.templates;

import name.voided.datatypes.*;
import name.voided.datatypes.IStepper.Position;
import name.voided.datatypes.tuple.Tuple2;
import name.voided.datatypes.tuple.Tuple3;
import name.voided.datatypes.tuple.Tuples;
import name.voided.parser.FluentParser;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static name.voided.datatypes.Result.failure;
import static name.voided.datatypes.Result.success;
import static name.voided.parser.FluentParser.concat;

public class Parser<TIn, TOut> {
    public final static String UNKNOWN = "_unknown_";
    private final ParserFunction<TIn, TOut> parserFn;
    public final String label;

    public Parser( String label, ParserFunction<TIn, TOut> parserFn ) {
        this.parserFn = parserFn;
        this.label = label;
    }

    public Result<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> parse( ImmutableStepper<TIn> source ) {
        return parserFn.parse( source );
    }

    public Result<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> parse( Iterator<TIn> source, TIn lineBreak ) {
        return parse( new ImmutableStepper<>( source, lineBreak ) );
    }

    public <TOther, TResult> Parser<TIn, TResult> andThen( Parser<TIn, TOther> then, BiFunction<TOut, TOther, TResult> combiner ) {
        return FluentParser.andThen( this, then, combiner );
    }

    public <TOther> Parser<TIn, Tuple2<TOut, TOther>> andThen( Parser<TIn, TOther> then ) {
        return FluentParser.andThen( this, then );
    }

    public Parser<TIn, TOut> orElse( Parser<TIn, TOut> or ) {
        var newLabel = "[" + label + " orElse " + or.label + "]";
        return new Parser<TIn, TOut>( newLabel, ( str ) -> switch ( parse( str ) ) {
            case Result.Success success -> success;
            case Result.Failure ignored -> or.withLabel( newLabel ).parse( str );
        } );
    }

    public <TResult> Parser<TIn, TResult> map( Function<TOut, TResult> m ) {
        return FluentParser.<TIn, TOut, TResult>mapParser( m ).apply( this ).withLabel( "[" + label + " mapped]" );
    }

    public <TOther> Parser<TIn, TOther> andThenDiscardingThis( Parser<TIn, TOther> then ) {
        return andThen( then, ( a, b ) -> b );
    }

    public <TOther> Parser<TIn, TOut> andThenDiscardingThen( Parser<TIn, TOther> then ) {
        return andThen( then, ( a, b ) -> a );
    }

    public <TSep> Parser<TIn, List<TOut>> sepBy( Parser<TIn, TSep> sep ) {
        return new Parser<>( "[" + label + " sepBy " + sep.label + "]", str -> switch ( parse( str ) ) {
            case Result.Failure ignored -> success( Tuples.of( List.of(), str ) );
            case Result.Success ignored -> this.andThen( FluentParser.many( sep.andThenDiscardingThis( this ) ),
                    ( a, b ) -> concat( List.of( a ), b ) ).parse( str );
        } );
    }

    public Parser<TIn, TOut> withLabel( String label ) {
        return new Parser<>( label, str -> switch ( parse( str ) ) {
            case Result.Failure<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> failure -> failure( Tuples.of( label, failure.value().v1, failure.value().v2 ) );
            case Result.Success<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> success -> success;
        } );
    }

    public Parser<TIn, TOut> peek( Parser<TIn, TOut> peeper ) {
        return new Parser<TIn, TOut>( label, str -> switch ( parse( str ) ) {
            case Result.Failure<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> failure -> failure;
            case Result.Success<Tuple2<TOut, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> success -> switch ( peeper.parse( success.value().v1 ) ) {
                case Result.Failure peep -> peep;
                case Result.Success ignored -> success;
            };
        } );
    }
}
