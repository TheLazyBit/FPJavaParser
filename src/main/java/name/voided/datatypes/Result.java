package name.voided.datatypes;

import java.util.function.Function;

public sealed interface Result<TSuccess, TFailure> {
    record Success<TSuccess, TFailure>(TSuccess value) implements Result<TSuccess, TFailure> {

        @Override
        public <TNewSuccess> Result<TNewSuccess, TFailure> map( Function<TSuccess, TNewSuccess> m ) {
            return success( m.apply( value ) );
        }

        @Override
        public String toString() {
            return "Success " + value;
        }
    }

    record Failure<TSuccess, TFailure>(TFailure value) implements Result<TSuccess, TFailure> {

        @Override
        public <TNewSuccess> Result<TNewSuccess, TFailure> map( Function<TSuccess, TNewSuccess> m ) {
            return failure( value );
        }

        @Override
        public String toString() {
            return "Failure " + value;
        }
    }

    static <TSuccess, TFailure> Result.Success<TSuccess, TFailure> success( TSuccess value ) {
        return new Result.Success<>( value );
    }

    static <TSuccess, TFailure> Result.Failure<TSuccess, TFailure> failure( TFailure value ) {
        return new Result.Failure<>( value );
    }

    <TNewSuccess> Result<TNewSuccess, TFailure> map( Function<TSuccess, TNewSuccess> m );
}

// hello world -> print hello world x times.


// parseNumber = (str) -> Result<int, exception>
// res = parseNumber("someNumber ;)") type (number | exception)
// val hellos = res.map( n -> "hello world\n" * n ) type (string | exception)
// consume(hellos)
//
// Result = Success a | Failure b
// Result::map f a -> b = when
//      Success a -> Success f(a)
//      Failure b -> Failure b
//
// when (hellos)
//      Success str -> print(str)
//      Failure ex -> print("Please try a number moron!")
//