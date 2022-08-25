package name.voided.parser;

import java.util.Iterator;

public class CharactersInStringIterator implements Iterator<Character> {
    public final String intern;
    public int position;

    public CharactersInStringIterator( String text ) {
        intern = text;
        position = 0;
    }


    @Override
    public boolean hasNext() {
        return position < intern.length();
    }

    @Override
    public Character next() {
        return intern.charAt( position++ );
    }
}
