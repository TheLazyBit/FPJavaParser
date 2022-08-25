package name.voided.datatypes;

/**
 * An alternative to an Iterator.
 * <p>
 * The idea is that instead of mutably iterating (and consuming) an input source,
 * the stepper will report the current value and step() will return a stepper further in.
 *
 * @param <E>
 */
public interface IStepper<E> {

    record Position(int line, int column) {

        public Position incrementLine() {
            return new Position( line + 1, 0 );
        }

        public Position incrementColumn() {
            return new Position( line, column + 1 );
        }

        @Override
        public String toString() {
            return "Position{" + "line=" + line + ", column=" + column + '}';
        }
    }

    boolean canStep();

    boolean hasContent();

    E get();

    Position getPosition();

    IStepper<E> step();
}
