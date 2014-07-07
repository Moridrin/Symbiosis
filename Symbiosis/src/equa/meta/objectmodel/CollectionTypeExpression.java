/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.requirements.FactRequirement;
import equa.meta.traceability.ExternalInput;

/**
 *
 * @author frankpeeters
 */
public class CollectionTypeExpression extends TypeExpression {

    private static final long serialVersionUID = 1L;
    private String separator;

    CollectionTypeExpression(FactType parent, String begin, String separator, String end) {
        super(parent, Arrays.asList(new String[]{begin, end}));
        this.separator = separator;
    }

    public CollectionTypeExpression(CollectionTypeExpression clone) {
        super(clone);
        this.separator = clone.separator;
    }

    @Override
    public void setConstants(List<String> constants, ExternalInput input) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("change of constants in Collection Expression is still not supported. ");
    }

    public String getSeparator() {
        return separator;
    }

    public String getBegin() {
        return constant(0);
    }

    public String getEnd() {
        return constant(1);
    }

    public void setSeparator(String separator) {
        if (separator.isEmpty()) {
            return;
        }
        this.separator = separator;
    }

    public void setBeginEnd(String begin, String end, ExternalInput source) throws ChangeNotAllowedException {
        ArrayList<String> constants = new ArrayList<>();
        constants.add(begin);
        constants.add(end);
        setConstants(constants, source);
    }

    @Override
    String makeExpression(Tuple tuple) {
        CollectionType ct = (CollectionType) tuple.getFactType().getObjectType();
        List<Value> values = ct.elementsOf(tuple.getItem(0).getValue().toString());
        return makeExpression(values);
    }

    String makeExpression(List<Value> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBegin());
        if (!elements.isEmpty()) {
            sb.append(elements.get(0).toString());
            for (int i = 1; i < elements.size(); i++) {
                sb.append(separator);
                sb.append(elements.get(i).toString());
            }
        }
        sb.append(getEnd());
        return sb.toString();
    }

    /**
     *
     * @param trimmedExpression
     * @param complete complete expression must be parsed?
     * @param source
     * @return the list of values according to the roles of the parent-facctype,
     * accompanied by the index where the parsing stopped and information for
     * possible other parsing results,
     * @throws MismatchException
     */
    @Override
    public ParseResult parse(String trimmedExpression, boolean complete,
            FactRequirement source) throws MismatchException {

        if (!isParsable()) {
            throw new NotParsableException(this, "TE " + getParent().getName() + " not parsable");
        }

        String expressionLowerCase = trimmedExpression.toLowerCase();
        List<Value> values = new ArrayList<>();
        SubstitutionType st = ((CollectionType) getParent().getObjectType()).getElementType();

        // check if expression starts with the first constant 
        String constant = getBegin().toLowerCase();
        if (expressionLowerCase.indexOf(constant, 0) != 0) {
            throw new MismatchException(this, "first constant doesn't match", 0);
        }
        String separatorLC = separator.toLowerCase();
        int fromIndex = constant.length();
        int untoIndex = expressionLowerCase.indexOf(separatorLC, fromIndex);
        while (untoIndex != -1) {
            String subValue = trimmedExpression.substring(fromIndex, untoIndex);
            values.add(st.parse(subValue, complete, source));
            fromIndex = untoIndex + separatorLC.length();
            untoIndex = expressionLowerCase.indexOf(separatorLC, fromIndex);
        }
        if (getEnd().isEmpty()) {
            untoIndex = expressionLowerCase.length();
        } else {
            untoIndex = expressionLowerCase.indexOf(getEnd().toLowerCase(), fromIndex);
            if (untoIndex == -1) {
                throw new MismatchException(this, "end constant is not present");
            }
        }

        String subValue = trimmedExpression.substring(fromIndex, untoIndex);
        values.add(st.parse(subValue, complete, source));
        fromIndex = untoIndex + getEnd().length();

        if (complete) {
            // check if parsing expression is completed until the very end 
            if (fromIndex != expressionLowerCase.length()) {
                throw new MismatchException(this, "last constant doesn't match; "
                        + "it should end with: " + getEnd());
            }
        }

        if (!((CollectionType) getParent().getObjectType()).isSequence()) {
            Collections.sort(values);
        }

        ParseResult parseResult = new ParseResult(values, -1, false);
        return parseResult;
    }

    /**
     *
     * @param expressionParts
     * @param source
     * @return the list of values according to the roles of the parent-facctype
     * @throws MismatchException
     */
    @Override
    List<Value> parse(List<String> expressionParts, FactRequirement source)
            throws MismatchException {
        if (!getBegin().trim().equalsIgnoreCase(expressionParts.get(0).trim())) {
            throw new MismatchException(this, "begin doesn't match");
        }
        String separatorTrimmed = separator.trim();
        SubstitutionType st = ((CollectionType) getParent().getObjectType()).getElementType();
        List<Value> values = new ArrayList<>();
        for (int position = 1; position < expressionParts.size(); position += 2) {
            values.add(
                    st.parse(expressionParts.get(position), false, source));
            String delimiter = expressionParts.get(position + 1);
            if (position + 2 < expressionParts.size()) {
                if (!separatorTrimmed.equalsIgnoreCase(delimiter.trim())) {
                    throw new MismatchException(this, "separator doesn't match");
                }
            } else {
                if (!getEnd().trim().equalsIgnoreCase(delimiter.trim())) {
                    throw new MismatchException(this, "end doesn't match");
                }
            }
        }
        if (!((CollectionType) getParent().getObjectType()).isSequence()) {
            Collections.sort(values);
        }
        return values;
    }

    @Override
    public String parseResultString(ParseResult parseResult) {
        StringBuilder sb = new StringBuilder();
        List<Value> values = parseResult.getValues();
        sb.append(getBegin());
        for (Value value : values) {
            sb.append("<").append(value.toString());
            sb.append(">").append(separator);
        }
        sb.replace(sb.length() - separator.length(), sb.length(), getEnd());
        return sb.toString();
    }

    /**
     * matching of constants
     *
     * @param constants maybe null in case of abstract or value objecttype
     * @return true if match was correct, otherwise false
     */
    @Override
    public boolean matches(List<String> constants) {
        if (constants == null || constants.isEmpty()) {
            return false;
        }

        if (!constants.get(0).equalsIgnoreCase(getBegin())) {
            return false;
        }

        for (int i = 1; i < constants.size() - 1; i++) {
            if (!constants.get(i).equalsIgnoreCase(separator)) {
                return false;
            }
        }

        if (!constants.get(constants.size() - 1).equalsIgnoreCase(getEnd())) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return the type-expression where every substitution place is filled in
     * with the optional rolename followed by the name of the substitutiontype
     * between jagged parenthesis (< ... >)
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        CollectionType ct = (CollectionType) getParent().getObjectType();
        String role = ct.getElementRole().getNamePlusType();
        boolean mayBeEmpty = ct.mayBeEmpty();
        sb.append(getBegin());
        if (mayBeEmpty) {
            sb.append("[");
        }
        FactType parent = getParent();
        FrequencyConstraint fc = ((CollectionType) parent.getObjectType()).getFrequencyConstraint();
        if (fc == null || fc.getMin() != fc.getMax()) {
            sb.append("<").append(role).append(">{");
            sb.append(separator);
            sb.append("<").append(role).append(">}");
        } else {
            sb.append("<").append(role).append(">");
            for (int i=1; i<fc.getMin(); i++){
                sb.append(separator);
                 sb.append("<").append(role).append(">");
            }
        }
        if (mayBeEmpty) {
            sb.append("]");
        }
        sb.append(getEnd());

        return sb.toString();
    }

    /**
     * setting of the role name of all roles where this type expression refers
     * to
     *
     * @param roleNames
     * @throws DuplicateException
     */
    @Override
    public void setRoleNames(List<String> roleNames) throws DuplicateException {
    }
}
