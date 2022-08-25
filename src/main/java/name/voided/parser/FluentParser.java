package name.voided.parser;

import name.voided.datatypes.*;
import name.voided.datatypes.IStepper.Position;
import name.voided.datatypes.tuple.Tuple2;
import name.voided.datatypes.tuple.Tuple3;
import name.voided.datatypes.tuple.Tuples;
import name.voided.parser.templates.Parser;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static name.voided.datatypes.Result.failure;
import static name.voided.datatypes.Result.success;
import static name.voided.parser.templates.Parser.UNKNOWN;

@FunctionalInterface
interface TriFunction<A, B, C, R> {
    R apply( A a, B b, C c );
}


public class FluentParser {
    // static function collection
    private FluentParser() {
    }

    public static <TIn> Parser<TIn, TIn> satisfy( Function<TIn, Boolean> property ) {
        return new Parser<>( UNKNOWN, ( source ) -> {

            if ( !source.hasContent() ) return failure( Tuples.of( UNKNOWN, "No more input", source.getPosition() ) );

            var next = source.get();
            if ( property.apply( next ) ) return success( Tuples.of( next, source.step() ) );

            return failure( Tuples.of( UNKNOWN, "Unexpected '" + next + "'", source.getPosition() ) );
        } );
    }

    public static Parser<Character, Character> parseCharacter( Character c ) {
        return satisfy( ( Function<Character, Boolean> ) c::equals ).withLabel( c + "" );
    }

    public static Parser<Character, Character> parseLetter() {
        return satisfy( ( Function<Character, Boolean> ) Character::isLetter ).withLabel( "Letter" );
    }

    public static <TIn, TOut> Parser<TIn, TOut> choice( List<Parser<TIn, TOut>> parsers ) {
        return parsers.stream().reduce( Parser::orElse ).orElseThrow();
    }

    public static <TFin, TPIn, TOut> Parser<TPIn, TOut> anyOf( Function<TFin, Parser<TPIn, TOut>> toParser,
                                                               List<TFin> in ) {
        return choice( in.stream().map( toParser ).collect( Collectors.toList() ) );
    }

    public static Parser<Character, Character> parseLowercase() {
        return satisfy( ( Function<Character, Boolean> ) Character::isLowerCase ).withLabel( "Lowercase" );
    }

    public static Parser<Character, Character> parseUppercase() {
        return satisfy( ( Function<Character, Boolean> ) Character::isUpperCase ).withLabel( "Uppercase" );
    }

    public static Parser<Character, Integer> parseDigit() {
        return satisfy( ( Function<Character, Boolean> ) Character::isDigit ).map( x -> Integer.valueOf( x + "" ) ).withLabel( "Digit" );
    }

    public static Parser<Character, Character> parseWhitespace() {
        return satisfy( ( Function<Character, Boolean> ) Character::isWhitespace ).withLabel( "Whitespace" );
    }

    public static Parser<Character, Character> parseAlphaNumeric() {
        return satisfy( ( Function<Character, Boolean> ) Character::isLetterOrDigit ).withLabel( "AlphaNumeric" );
    }

    public static Parser<Character, Character> parseAny() {
        return satisfy( ( Function<Character, Boolean> ) ( c ) -> true ).withLabel( "AnyCharacter" );
    }

    public static <TIn, TOut> Parser<TIn, TOut> returnParser( TOut value ) {
        return new Parser<>( UNKNOWN, ( str ) -> success( Tuples.of( value, str ) ) );
    }

    public static <TIn, TResult, TParameter> Function<Parser<TIn, TParameter>, Parser<TIn, TResult>> applyParser( Parser<TIn, Function<TParameter, TResult>> functionParser ) {
        return ( p ) -> bind( ( f ) -> bind(
                ( pArg ) -> returnParser( f.apply( pArg ) ),
                p
        ), functionParser );
    }

    public static <TPIn, TIn1, TIn2, TResult> Function<Parser<TPIn, TIn1>, Function<Parser<TPIn, TIn2>, Parser<TPIn, TResult>>> lift2( BiFunction<TIn1, TIn2, TResult> function ) {
        var curried =
                ( Function<TIn1, Function<TIn2, TResult>> ) ( arg1 ) ->
                        ( Function<TIn2, TResult> ) ( TIn2 arg2 ) -> function.apply( arg1, arg2 );
        return ( Parser<TPIn, TIn1> arg1 ) -> ( Parser<TPIn, TIn2> arg2 ) -> applyParser( applyParser(
                FluentParser.<TPIn, Function<TIn1, Function<TIn2, TResult>>>returnParser( curried )
        ).apply( arg1 ) ).apply( arg2 );
    }

    public static <TPIn, TIn1, TIn2, TIn3, TResult> Function<Parser<TPIn, TIn1>, Function<Parser<TPIn, TIn2>, Function<Parser<TPIn, TIn3>, Parser<TPIn, TResult>>>> lift3( TriFunction<TIn1, TIn2, TIn3, TResult> function ) {
        var curried =
                ( Function<TIn1, Function<TIn2, Function<TIn3, TResult>>> ) ( arg1 ) ->
                        ( Function<TIn2, Function<TIn3, TResult>> ) ( TIn2 arg2 ) ->
                                ( Function<TIn3, TResult> ) ( TIn3 arg3 ) ->
                                        function.apply( arg1, arg2, arg3 );

        return ( Parser<TPIn, TIn1> arg1 ) -> ( Parser<TPIn, TIn2> arg2 ) -> ( Parser<TPIn, TIn3> arg3 ) ->
                applyParser(
                        applyParser(
                                applyParser( FluentParser.<TPIn, Function<TIn1, Function<TIn2, Function<TIn3, TResult>>>>returnParser( curried )
                                ).apply( arg1 )
                        ).apply( arg2 )
                ).apply( arg3 );
    }

    public static <TPIn, T> Parser<TPIn, List<T>> sequence( List<Parser<TPIn, T>> parsers ) {
        BiFunction<T, List<T>, List<T>> cons = ( element, list ) -> concat( List.of( element ), list );
        var consP = FluentParser.<TPIn, T, List<T>, List<T>>lift2( cons );

        if ( parsers.isEmpty() )
            return returnParser( List.of() );

        var head = parsers.get( 0 );
        var tail = parsers.subList( 1, parsers.size() );
        return consP.apply( head ).apply( sequence( tail ) );
    }

    public static Parser<Character, String> parseString( String str ) {
        var list = new ArrayList<Parser<Character, Character>>( str.length() );
        for ( char c : str.toCharArray() ) {
            list.add( parseCharacter( c ) );
        }
        return sequence( list ).map( characters -> characters.stream()
                .reduce( new StringBuilder(),
                        StringBuilder::append,
                        StringBuilder::append )
                .toString() ).withLabel( "'" + str + "'" );
    }

    private static <TPIn, T> Tuple2<List<T>, ImmutableStepper<TPIn>> parseZeroOrMore( Parser<TPIn, T> parser, ImmutableStepper<TPIn> input ) {
        return switch ( parser.parse( input ) ) {
            case Result.Failure<Tuple2<T, ImmutableStepper<TPIn>>, ?> ignored -> Tuples.of( List.of(), input );
            case Result.Success<Tuple2<T, ImmutableStepper<TPIn>>, ?> success -> {
                var following = parseZeroOrMore( parser, success.value().v1 );
                yield Tuples.of(
                        concat( List.of( success.value().v0 ), following.v0 ),
                        following.v1 );
            }
        };
    }

    public static <TPIn, T> Parser<TPIn, List<T>> many( Parser<TPIn, T> parser ) {
        return new Parser<>( "many: " + parser.label, str -> Result.success( parseZeroOrMore( parser, str ) ) );
    }

    public static <TPIn, T> Parser<TPIn, List<T>> manyAtLeastOnce( Parser<TPIn, T> parser ) {
        return new Parser<>( parser.label, str -> {
            var hits = parseZeroOrMore( parser, str );
            if ( hits.v0.size() > 0 ) return success( hits );
            return failure( Tuples.of( "manyAtLeastOnce: " + parser.label, "Parser didn't find any hits", str.getPosition() ) );
        } );
    }

    public static Parser<Character, Integer> parseInteger() {
        return optional( parseCharacter( '-' ) )
                .andThen( manyAtLeastOnce( parseDigit() )
                                .map( FluentParser::join )
                                .map( Integer::valueOf ),
                        ( opt, i ) -> opt.map( c -> -i ).orElse( i ) );
    }

    public static <E> String join( List<E> list, String sep ) {
        return list.stream().map( Object::toString ).collect( Collectors.joining( sep ) );
    }

    public static <E> String join( List<E> list ) {
        return join( list, "" );
    }

    public static <TPIn, T> Parser<TPIn, Optional<T>> optional( Parser<TPIn, T> parser ) {
        return parser
                .map( Optional::of )
                .orElse( returnParser( Optional.empty() ) );
    }

    public static Parser<Character, List<Character>> whiteSpaces() {
        return manyAtLeastOnce( parseWhitespace() );
    }

    public static <TPIn, TLeft, TResult, TRight> Parser<TPIn, TResult> between( Parser<TPIn, TLeft> left, Parser<TPIn, TResult> find, Parser<TPIn, TRight> right ) {
        return left.andThenDiscardingThis( find ).andThenDiscardingThen( right );
    }

    /**
     * "bind" takes a parser-producing function f, and a parser p
     * and passes the output of p into f, to create a new parser
     * <p>
     * i.e. binds output of p to f
     * <p>
     * (fB.(pA.C), pA.B) -> pA.C
     * <p>
     * bind(x -> new parser, parser producing x)
     * <p>
     * Utility: allows us to create a closure over x -> "remember" a previous result
     */
    public static <TPIn, TPOut, TFOut> Parser<TPIn, TFOut> bind( Function<TPOut, Parser<TPIn, TFOut>> f, Parser<TPIn, TPOut> p ) {
        return new Parser<>( UNKNOWN, ( str ) -> switch ( p.parse( str ) ) {
            case Result.Failure<Tuple2<TPOut, ImmutableStepper<TPIn>>, Tuple3<String, String, Position>> failure -> failure( ( failure ).value() );
            case Result.Success<Tuple2<TPOut, ImmutableStepper<TPIn>>, Tuple3<String, String, Position>> success -> f.apply( success.value().v0 ).parse( success.value().v1 );
        } );
    }

    /**
     * returns the "backside" of a map.
     * mapper = mapParser(fA.B)
     * p?.B = mapper(p?.A)
     * <p>
     * fB.C -> pA.B -> pA.C
     * <p>
     * Utility: Transform the value captured by the parser monad
     */
    public static <TPIn, T, TResult> Function<Parser<TPIn, T>, Parser<TPIn, TResult>> mapParser( Function<T, TResult> f ) {
        return ( Parser<TPIn, T> p ) -> bind(
                ( x ) ->
                        returnParser( f.apply( x ) ),
                p
        ).withLabel( p.label );
    }

    /**
     * (p1A.B, p2A.C) -> pA.[B,C]
     */
    public static <TPIn, P1, P2> Parser<TPIn, Tuple2<P1, P2>> andThen( Parser<TPIn, P1> p1, Parser<TPIn, P2> p2 ) {
        return andThen( p1, p2, Tuples::of );
    }

    /**
     * (p1A.B, p2A.C, +(B.C).D) -> pA.D
     * use parser 1, then parser 2, then combine results
     */
    public static <TPIn, P1, P2, TResult> Parser<TPIn, TResult> andThen( Parser<TPIn, P1> p1, Parser<TPIn, P2> p2, BiFunction<P1, P2, TResult> combiner ) {
        return bind(
                p1Result -> bind(
                        p2Result -> returnParser( combiner.apply( p1Result, p2Result ) ),
                        p2 ),
                p1
        ).withLabel( "[" + p1.label + " andThen " + p2.label + "]" );
    }

    /**
     * apply and collect p until "until" succeeds, "until" result is not part of the aggregate stepper points at start of until
     */
    public static <TPIn, T, TUntil> Parser<TPIn, List<T>> until( Parser<TPIn, T> p, Parser<TPIn, TUntil> until ) {
        return new Parser<>( p.label + " until " + until.label, str -> switch ( until.parse( str ) ) {
            case Result.Success ignored -> success( Tuples.of( List.of(), str ) );
            case Result.Failure ignored -> switch ( p.parse( str ) ) {
                case Result.Failure<Tuple2<T, ImmutableStepper<TPIn>>, Tuple3<String, String, Position>> failure -> failure( failure.value() );
                case Result.Success<Tuple2<T, ImmutableStepper<TPIn>>, Tuple3<String, String, Position>> success -> until( p, until ).parse( success.value().v1 ) // keep consuming
                        .map( tail -> Tuples.of( concat( List.of( success.value().v0 ), tail.v0 ), tail.v1 ) );
            };
        } );
    }

    public static <TIn> Parser<TIn, Boolean> isEnd() {
        return new Parser<>( UNKNOWN, source -> {
            if ( source.canStep() ) {
                return failure( Tuples.of( "?end", "there is more input", source.getPosition() ) );
            }
            return success( Tuples.of( true, source ) );
        } );
    }

    public static <E> List<E> concat( List<E> a, List<E> b ) {
        return Stream.concat( a.stream(), b.stream() ).collect( Collectors.toList() );
    }

    public static <TIn, T> String formatResult( Result<Tuple2<T, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> result ) {
        return switch ( result ) {
            case Result.Failure<Tuple2<T, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> failure -> String.format( "Error parsing '%s' at %s%n\t%s", failure.value().v0, failure.value().v2, failure.value().v1 );
            case Result.Success<Tuple2<T, ImmutableStepper<TIn>>, Tuple3<String, String, Position>> success -> String.format( "%s", success.value().v0 );
        };
    }

    public static ImmutableStepper<Character> toStepper( String string ) {
        return new ImmutableStepper<>( new CharactersInStringIterator( string ), '\n' );
    }


    public static void main( String[] args ) {

    }

}
