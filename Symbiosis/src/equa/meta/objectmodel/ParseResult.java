/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class ParseResult {

    private int indexRest;
    private List<Value> values;
    private boolean otherParsingPossible;

    public ParseResult(List<Value> values, int indexRest, boolean otherParsingPossible) {
        this.indexRest = indexRest;
        this.values = values;
        this.otherParsingPossible = otherParsingPossible;
    }

    public int getIndexRest() {
        return indexRest;
    }

    public List<Value> getValues() {
        return values;
    }

    public boolean otherParsingPossible() {
        return otherParsingPossible;
    }
}
