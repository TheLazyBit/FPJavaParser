package name.voided.parser;

import name.voided.datatypes.IStepper;
import name.voided.datatypes.ImmutableStepper;
import name.voided.datatypes.Result;
import name.voided.datatypes.tuple.Tuple2;
import name.voided.datatypes.tuple.Tuple3;
import name.voided.datatypes.tuple.Tuples;
import name.voided.parser.templates.Parser;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static name.voided.parser.FluentParser.*;
import static name.voided.parser.JsonParser.JsonArrayParser.jsonArray;
import static name.voided.parser.JsonParser.JsonBoolParser.jsonBool;
import static name.voided.parser.JsonParser.JsonNullParser.jsonNull;
import static name.voided.parser.JsonParser.JsonNumberParser.jsonNumber;
import static name.voided.parser.JsonParser.JsonObjectParser.jsonObject;
import static name.voided.parser.JsonParser.JsonStringParser.*;

public class JsonParser {

    sealed interface IJsonValue {
    }

    public record JsonString(String value) implements IJsonValue {

        @Override
        public String toString() {
            return "JsonString{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public static class JsonStringParser {
        public static Parser<Character, Character> jsonUnescapedCharacter() {
            return FluentParser.<Character>satisfy( c -> '\\' != c && '\"' != c )
                    .withLabel( "char" );
        }

        public static Parser<Character, Character> jsonEscapedCharacter() {
            return choice( Stream.of(
                                    Tuples.of( "\\\\", '\\' ),
                                    Tuples.of( "\\\"", '\"' ),
                                    Tuples.of( "\\/", '/' ),
                                    Tuples.of( "\\b", '\b' ),
                                    Tuples.of( "\\f", '\f' ),
                                    Tuples.of( "\\n", '\n' ),
                                    Tuples.of( "\\r", '\r' ),
                                    Tuples.of( "\\t", '\t' )
                            )
                            .map( escaped -> parseString( escaped.v0 ).map( e -> escaped.v1 ) )
                            .collect( Collectors.toList() )
            ).withLabel( "escaped char" );
        }

        public static Parser<Character, Character> jsonUnicodeCharacter() {
            var backslash = parseCharacter( '\\' );
            var u = parseCharacter( 'u' );
            var hexDigit = anyOf( FluentParser::parseCharacter, List.of(
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'A', 'B', 'C', 'D', 'E', 'F',
                    'a', 'b', 'c', 'd', 'e', 'f'
            ) );

            return backslash
                    .andThenDiscardingThis( u )
                    .andThenDiscardingThis( hexDigit )
                    .andThen( hexDigit, ( a, b ) -> a + "" + b )
                    .andThen( hexDigit, ( a, b ) -> a + b )
                    .andThen( hexDigit, ( a, b ) -> a + b )
                    .map( c -> Integer.parseInt( c, 16 ) )
                    .map( Character::toChars )
                    .map( cs -> String.valueOf( cs ).charAt( 0 ) )
                    .withLabel( "unicode" ); // we know it fits into just one character here
        }

        public static Parser<Character, String> quotedString() {
            var quote = parseCharacter( '\"' );
            var jsonCharacter = jsonUnescapedCharacter()
                    .orElse( jsonEscapedCharacter() )
                    .orElse( jsonUnicodeCharacter() );

            return between( quote, many( jsonCharacter ), quote )
                    .map( FluentParser::join );
        }

        public static Parser<Character, JsonString> jsonString() {
            return quotedString().map( JsonString::new ).withLabel( "jsonString" );
        }
    }

    public static class JsonNumberParser {
        public static Parser<Character, Optional<Character>> optSign() {
            return optional( parseCharacter( '-' ) );
        }

        public static Parser<Character, Optional<Character>> optPlusMinus() {
            return optional( parseCharacter( '-' ).orElse( parseCharacter( '+' ) ) );
        }

        public static Parser<Character, Character> zero() {
            return parseCharacter( '0' );
        }

        public static Parser<Character, Character> oneToNine() {
            return satisfy( c -> Character.isDigit( c ) && '0' != c );
        }

        public static Parser<Character, Character> digit() {
            return satisfy( Character::isDigit );
        }

        public static Parser<Character, Character> point() {
            return satisfy( c -> '.' == c );
        }

        public static Parser<Character, Character> e() {
            return parseCharacter( 'e' ).orElse( parseCharacter( 'E' ) );
        }

        public static Parser<Character, String> nonZeroInt() {
            return oneToNine().andThen( many( digit() ), ( a, b ) -> a + join( b ) );
        }

        public static Parser<Character, String> intPart() {
            return zero().map( z -> z + "" ).orElse( nonZeroInt() );
        }

        public static Parser<Character, String> fractionPart() {
            return point().andThenDiscardingThis( many( digit() ) ).map( FluentParser::join );
        }

        public static Parser<Character, Tuple2<Optional<Character>, String>> exponentPart() {
            return e().andThenDiscardingThis( optPlusMinus() ).andThen( many( digit() ).map( FluentParser::join ) );
        }

        public static Parser<Character, String> number() {
            return optSign()
                    .andThen( intPart() )
                    .andThen( optional( fractionPart() ) )
                    .andThen( optional( exponentPart() ), ( a, b ) -> Tuples.of( a.v0, a.v1, b ) )
                    .map( tuple3 -> tuple3.destructure(
                            ( ignore, fraction, exponent ) -> ignore.destructure(
                                    ( sign, integer ) ->
                                            sign.map( ch -> ch + "" ).orElse( "" )
                                                    + integer
                                                    + fraction.map( str -> "." + str ).orElse( "" )
                                                    + exponent.map( innerIgnore -> innerIgnore.destructure( ( expSign, exponentValue ) ->
                                                            "e"
                                                                    + expSign.map( ch -> ch + "" ).orElse( "" )
                                                                    + exponentValue ) )
                                                    .orElse( "" )
                            ) ) ).map( Objects::toString );
        }

        public static Parser<Character, JsonNumber> jsonNumber() {
            return number().map( Double::valueOf ).map( JsonNumber::new ).withLabel( "jsonNumber" );
        }
    }

    public record JsonNumber(double value) implements IJsonValue {

        @Override
        public String toString() {
            return "JsonNumber{" +
                    "value=" + value +
                    '}';
        }
    }

    public static class JsonArrayParser {

        public static Parser<Character, JsonArray> jsonArray() {
            var left = parseCharacter( '[' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var right = parseCharacter( ']' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var comma = parseCharacter( ',' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var value = jsonValue().andThenDiscardingThen( optional( whiteSpaces() ) );
            var values = value.sepBy( comma );
            return between( left, values, right ).map( JsonArray::new ).withLabel( "jsonArray" );
        }
    }

    record JsonArray(List<IJsonValue> values) implements IJsonValue {
        JsonArray( List<IJsonValue> values ) {
            this.values = List.copyOf( values );
        }

        @Override
        public String toString() {
            return "JsonArray{" +
                    "values=" + values +
                    '}';
        }
    }


    public static Parser<Character, IJsonValue> jsonValue() {
        return new Parser<>( "jsonValue", ( src ) ->
                jsonString().map( it -> ( IJsonValue ) it )
                        .orElse( jsonNumber().map( it -> it ) ) // weak ass java generics jank, don't want to weaken the types on the methods -> have to map it to itself for the compiler to swallow it
                        .orElse( jsonArray().map( it -> it ) )
                        .orElse( jsonObject().map( it -> it ) )
                        .orElse( jsonBool().map( it -> it ) )
                        .orElse( jsonNull().map( it -> it ) )
                        .parse( src ) );
    }


    record JsonObject(Map<String, IJsonValue> entries) implements IJsonValue {

        public static JsonObject fromProperties( List<Tuple2<String, IJsonValue>> entries ) {
            return new JsonObject( Map.copyOf( entries.stream().collect( Collectors.toMap(
                    entry -> entry.v0,
                    entry -> entry.v1 ) ) ) );
        }

        @Override
        public String toString() {
            return "JsonObject{" +
                    "entries=" + entries +
                    '}';
        }
    }

    public static class JsonObjectParser {

        public static Parser<Character, JsonObject> jsonObject() {
            var left = parseCharacter( '{' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var right = parseCharacter( '}' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var comma = parseCharacter( ',' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var colon = parseCharacter( ':' ).andThenDiscardingThen( optional( whiteSpaces() ) );
            var key = quotedString().andThenDiscardingThen( optional( whiteSpaces() ) );
            var value = JsonParser.jsonValue().andThenDiscardingThen( optional( whiteSpaces() ) );
            var keyValue = key.andThenDiscardingThen( colon ).andThen( value );
            var keyValues = keyValue.sepBy( comma );

            return between( left, keyValues, right ).map( JsonObject::fromProperties ).withLabel( "jsonObject" );
        }
    }

    public static class JsonNullParser {
        public static Parser<Character, JsonNull> jsonNull() {
            return parseString( "null" ).map( it -> new JsonNull() ).withLabel( "jsonNull" );
        }
    }

    record JsonNull() implements IJsonValue {
        @Override
        public String toString() {
            return "JsonNull{" +
                    '}';
        }
    }

    public static class JsonBoolParser {

        public static Parser<Character, JsonBool> jsonBool() {
            return parseString( "true" )
                    .orElse( parseString( "false" ) )
                    .map( Boolean::valueOf )
                    .map( JsonBool::new )
                    .withLabel( "jsonBool" );
        }
    }

    record JsonBool(boolean value) implements IJsonValue {

        @Override
        public String toString() {
            return "JsonBool{" +
                    "value=" + value +
                    '}';
        }
    }


    public static void main( String[] args ) {
        var input = toStepper( "{\"widget\": {\n" +
                "    \"debug\": \"on\",\n" +
                "    \"window\": {\n" +
                "        \"title\": \"Sample Konfabulator Widget\",\n" +
                "        \"name\": \"main_window\",\n" +
                "        \"width\": 500,\n" +
                "        \"height\": 500\n" +
                "    },\n" +
                "    \"image\": {\n" +
                "        \"src\": \"Images/Sun.png\",\n" +
                "        \"name\": \"sun1\",\n" +
                "        \"hOffset\": 250,\n" +
                "        \"vOffset\": 250,\n" +
                "        \"alignment\": \"center\"\n" +
                "    },\n" +
                "    \"text\": {\n" +
                "        \"data\": \"Click Here\",\n" +
                "        \"size\": 36,\n" +
                "        \"style\": \"bold\",\n" +
                "        \"name\": \"text1\",\n" +
                "        \"hOffset\": 250,\n" +
                "        \"vOffset\": 100,\n" +
                "        \"alignment\": \"center\",\n" +
                "        \"onMouseUp\": \"sun1.opacity = (sun1.opacity / 100) * 90;\"\n" +
                "    }\n" +
                "}}" );

        switch ( jsonValue().parse( input ) ) {
            // more weak junk generics -> the compiler should absolutely be able to derive the arguments here
            case Result.Failure<Tuple2<IJsonValue, ImmutableStepper<Character>>, Tuple3<String, String, IStepper.Position>> failure -> System.out.println( "Not valid json: " + formatResult( failure ) );
            case Result.Success<Tuple2<IJsonValue, ImmutableStepper<Character>>, Tuple3<String, String, IStepper.Position>> success -> {
                switch ( success.value().v0 ) {
                    case JsonObject object -> System.out.println( "It's an object! " + object );
                    case JsonArray array -> System.out.println( "It's an array! " + array );
                    case JsonBool bool -> System.out.println( "It's a boolean! " + bool );
                    case JsonNull aNull -> System.out.println( "It's a null! " + aNull );
                    case JsonNumber number -> System.out.println( "It's a number! " + number );
                    case JsonString str -> System.out.println( "It's a string! " + str );
                }
            }
        }
    }
}
