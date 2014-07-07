package equa.meta.objectmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.requirements.FactRequirement;
import equa.meta.traceability.ExternalInput;

/**
 *
 * @author FrankP
 */
@Entity
public class TypeExpression implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private FactType parent;
    @ElementCollection
    private List<String> constants;
    @Column
    private boolean parsableConstants;
    @ElementCollection
    private List<Integer> roleNumbers;

    public TypeExpression() {
        constants = new ArrayList<>();
        initRoleNumbers();
    }

    /**
     *
     * @param facttype
     * @param constants
     * @param source
     */
    TypeExpression(FactType facttype, List<String> constants) {
        parent = facttype;
        setConstants(constants);
        initRoleNumbers();
    }

    TypeExpression(FactType facttype, List<String> constants, List<Integer> roleNumbers) {
        parent = facttype;
        setConstants(constants);
        checkRoleNumbers(roleNumbers);
        this.roleNumbers = roleNumbers;
    }

    public TypeExpression(TypeExpression clone) {
        parent = clone.parent;
        constants = new ArrayList<>(clone.constants);
        int last = constants.size() - 1;
        if (last > 0) {
            String lastConstant = constants.get(last);
            if (lastConstant.endsWith(".")) {
                constants.set(last, lastConstant.substring(lastConstant.length() - 1));
            } else {
                constants.set(last, lastConstant + ".");
            }
        }

        parsableConstants = clone.parsableConstants;
        roleNumbers = new ArrayList<>(clone.roleNumbers);
    }

    void substitute(int index, TypeExpression oTE) {
        int diff = oTE.roleNumbers.size() - 1;
        int[] roleNumbersNew = new int[roleNumbers.size() + diff];

        int indexRoleNumbersNew = 0;
        int indexReplacedItem = -1;
        for (int indexRoleNumbers = 0; indexRoleNumbers < roleNumbers.size(); indexRoleNumbers++) {
            if (roleNumbers.get(indexRoleNumbers) == index) {
                indexReplacedItem = indexRoleNumbers;
                for (int j = 0; j <= diff; j++) {
                    roleNumbersNew[indexRoleNumbersNew] = index + j;
                    indexRoleNumbersNew++;
                }
            } else {
                if (roleNumbers.get(indexRoleNumbers) < index) {
                    roleNumbersNew[indexRoleNumbersNew] = roleNumbers.get(indexRoleNumbers);
                } else {
                    roleNumbersNew[indexRoleNumbersNew] = roleNumbers.get(indexRoleNumbers) + diff;
                }
                indexRoleNumbersNew++;
            }
        }

        roleNumbers = new ArrayList<>(roleNumbersNew.length);
        for (Integer integer : roleNumbersNew) {
            roleNumbers.add(integer);
        }

        List<String> c = new ArrayList<>();
        for (int i = 0; i < indexReplacedItem; i++) {
            c.add(constants.get(i));
        }
        c.add(constants.get(indexReplacedItem) + oTE.constants.get(0));
        for (int j = 1; j < oTE.constants.size() - 1; j++) {
            c.add(oTE.constants.get(j));
        }
        c.add(oTE.constants.get(oTE.constants.size() - 1) + constants.get(indexReplacedItem + 1));
        for (int i = indexReplacedItem + 2; i < constants.size(); i++) {
            c.add(constants.get(i));
        }
        setConstants(c);
    }

    /**
     *
     * @param nrs all numbers refer to existing different rolenumbers
     */
    void mergeRoles(int[] nrs) {

        int[] rn = new int[roleNumbers.size() + 1 - nrs.length];
        int k = 0;
        for (int i = 0; i < roleNumbers.size(); i++) {
            if (!exists(roleNumbers.get(i), nrs)) {
                // role i must stay untouched
                rn[k] = roleNumbers.get(i);
                k++;
            }
        }
        for (int i = 0; i < rn.length - 1; i++) {
            rn[i] -= countSmaller(rn[i], nrs);
        }
        rn[k] = k;
        roleNumbers = new ArrayList<>(rn.length);
        for (int number : rn) {
            roleNumbers.add(number);
        }

        List<String> c = new ArrayList<>();
        for (int i = 0; i <= rn.length; i++) {
            c.add("");
        }
        setConstants(c);

    }

    static boolean exists(int i, int[] nrs) {
        for (int j = 0; j < nrs.length; j++) {
            if (nrs[j] == i) {
                return true;
            }
        }
        return false;
    }

    static int countSmaller(int nr, int[] nrs) {
        int countSmaller = 0;
        for (int j = 0; j < nrs.length; j++) {
            if (nrs[j] < nr) {
                countSmaller++;
            }
        }
        return countSmaller;
    }

    private void initRoleNumbers() {
        if (constants.isEmpty()) {
            roleNumbers = new ArrayList<>(0);
        } else {
            roleNumbers = new ArrayList<>(constants.size() - 1);
            for (int i = 0; i < constants.size() - 1; i++) {
                roleNumbers.add(i);
            }
        }
    }

    List<Integer> cloneRoleNumbers() {
        return new ArrayList<Integer>(roleNumbers);
    }

    List<String> cloneConstants() {
        return new ArrayList<>(constants);
    }

    /**
     *
     * @param i
     * @return the separator before the next substitutionvalue (could be a
     * constant of a substitutionvalue within a substitutionvalue)
     * seperator=null if its an empty terminator of the whole expression
     */
    private String separator(int i) {
        if (!constants.get(i).isEmpty()) {
            return constants.get(i);
        } else if (i == constants.size() - 1) {
            return null;
        } else {
            int roleNr = getRoleNumber(i);
            Role role = parent.roles.get(roleNr);
            if (role instanceof ObjectRole) {
                return ((ObjectType) role.getSubstitutionType()).getOTE().constants.get(0);
            } else {
                return "";
            }
        }
    }

    private boolean parsingDifficult(List<String> constants) {
        if (constants.isEmpty()) {
            return true;
        }

        // empty constants in the middle of an expression makes
        // parsing difficult
        for (int i = 1; i < constants.size() - 1; i++) {
            if (separator(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    String makeExpression(Tuple tuple) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < constants.size() - 1; i++) {
            sb.append(constants.get(i));
            sb.append(tuple.getItem(roleNumbers.get(i)).substitutionExpression());
        }

        sb.append(constants.get(constants.size() - 1));

        return sb.toString();
    }

    private void setConstants(List<String> constants) {
        trimHeadAndTailOf(constants);

        if (parent.getFTE() == this) {
            String lastConstant = constants.get(constants.size() - 1);
            if (!lastConstant.endsWith(".")) {
                constants.remove(constants.size() - 1);
                constants.add(lastConstant + ".");
            }
        }
        this.constants = constants;
        this.parsableConstants = !parsingDifficult(constants);
    }

    /**
     * constants of this type expression are set if fact type expression missing
     * dot at the end, this will be added
     *
     * @param constants size of constants must be the same as the original one
     * @param input the input from the current user of the project
     */
    public void setConstants(List<String> constants, ExternalInput input) throws ChangeNotAllowedException {
        setConstants(constants);
        parent.refreshFactRequirements();
    }

    /**
     * the substitution sequence of the roles (by number) within this type
     * expression
     *
     * @param roleNumbers
     */
    public void setRoleNumbers(List<Integer> roleNumbers) throws ChangeNotAllowedException {
        checkRoleNumbers(roleNumbers);
        this.roleNumbers = roleNumbers;
        parent.refreshFactRequirements();
    }

    public FactType getParent() {
        return parent;
    }

    /**
     *
     * @return true if this type expression offers a chance to parse an
     * expression
     */
    public boolean isParsable() {
        return parsableConstants;
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
    public ParseResult parse(String trimmedExpression, boolean complete,
            FactRequirement source) throws MismatchException {

        if (!isParsable()) {
            throw new NotParsableException(this, "TE " + parent.getName() + " not parsable");
        }
        String expressionLowerCase = trimmedExpression.toLowerCase();
        Value[] valuesArray = new Value[constants.size() - 1];

        // check if expression starts with the first constant 
        String constant = constants.get(0).toLowerCase();
        if (expressionLowerCase.indexOf(constant, 0) != 0) {

            throw new MismatchException(this, "first constant doesn't match", 0);
        }
        int fromIndex = constant.length();
        int untoIndex;
        boolean otherOptionPossible = false;

        for (int valueNr = 0; valueNr < valuesArray.length; valueNr++) {
            constant = constants.get(valueNr + 1).toLowerCase();
            String separator = separator(valueNr + 1);
            if (separator == null || separator.isEmpty()) {
                // can only be the last constant (as a consequence of isParsable()) 
                untoIndex = expressionLowerCase.length();
            } else {
                // searching for next constant in expression further on
                untoIndex = expressionLowerCase.indexOf(separator, fromIndex);
                if (untoIndex == -1) {
                    //return null;
                    throw new MismatchException(this, "constant " + separator
                            + " doesn't match", valueNr + 1);
                } else if (complete && otherOptionPossible(untoIndex + 1, /*constants, valueNr + 1,*/
                        separator,
                        expressionLowerCase)) {
                    otherOptionPossible = true;
                }
            }
            String substring = trimmedExpression.substring(fromIndex, untoIndex);

            // parsing of variable part by corresponding substitution type
            Value sv = parent.parse(substring, complete, roleNumbers.get(valueNr), source);

            // storing parsed substitution value on position corresponding to
            // the position of the role at the facttype
            valuesArray[roleNumbers.get(valueNr)] = sv;
            fromIndex = untoIndex + constant.length();
        }

        if (complete) {
            // check if parsing expression is completed until the very end 
            if (fromIndex != expressionLowerCase.length()) {
                //               return null;
                throw new MismatchException(this, "last constant doesn't match",
                        constants.size() - 1);
            }
        }

        ParseResult parseResult = new ParseResult(Arrays.asList(valuesArray), fromIndex, otherOptionPossible);
        return parseResult;
    }

    /**
     *
     * @param expressionParts
     * @param source
     * @return the list of values according to the roles of the parent-facctype
     * @throws MismatchException
     */
    List<Value> parse(List<String> expressionParts, FactRequirement source)
            throws MismatchException {
        int i = 0;
        for (String constant : constants) {
            if (!constant.trim().equalsIgnoreCase(expressionParts.get(i).trim())) {
                throw new MismatchException(this, "constant " + i / 2 + " doesn't match");
            }
            i += 2;
        }
        Value[] values = new Value[roleNumbers.size()];
        for (int position = 0; position < roleNumbers.size(); position++) {
            Value sv
                    = parent.parse(expressionParts.get(2 * position + 1), false,
                            roleNumbers.get(position), source);
            // storing parsed substitution value
            values[roleNumbers.get(position)] = sv;
        }
        return Arrays.asList(values);
    }

    public String parseResultString(ParseResult parseResult) {
        StringBuilder sb = new StringBuilder();
        sb.append(constants.get(0));
        for (int i = 1; i < constants.size(); i++) {
            sb.append("<").append(parseResult.getValues().get(i - 1).toString());
            sb.append(">").append(constants.get(i));
        }
        return sb.toString();
    }

    /**
     *
     * @param nr 0 <= nr < count of constants of this type expression @return
     * the constant with number nr
     *
     *
     */
    public String constant(int nr) {
        return constants.get(nr);
    }

    /**
     *
     * @return an iterator over all the constants of this type expression
     */
    public Iterator<String> constants() {
        return constants.iterator();
    }

    /**
     * matching of constants, types and roleNames
     *
     * @param constants
     * @param types ranking conform the ranking of this type expression
     * @param roleNames ranking conform the ranking of this type expression
     * @throws MismatchException
     */
//    public void matches(List<String> constants, List<SubstitutionType> types,
//            List<String> roleNames) throws MismatchException {
//        SubstitutionType[] sts = new SubstitutionType[parent.size()];
//        String[] rns = new String[parent.size()];
//        for (int i = 0; i < roleNumbers.length; i++) {
//            sts[roleNumbers[i]] = types.get(i);
//            rns[roleNumbers[i]] = roleNames.get(i);
//        }
//
//        parent.checkMatch((List<SubstitutionType>) Arrays.asList(sts),
//                (List<String>) Arrays.asList(rns), true);
//        if (!matches(constants)) {
//            throw new MismatchException("something got wrong with the constants");
//        }
//    }
    /**
     * matching of constants
     *
     * @param constants maybe null in case of abstract or value objecttype
     * @return true if match was correct, otherwise false
     */
    public boolean matches(List<String> constants) {
        if (constants == null) {
            return this.constants == null || this.constants.isEmpty();
        }

        for (int i = 0; i < constants.size(); i++) {
            if (!constants.get(i).equalsIgnoreCase(this.constants.get(i))) {
                return false;
            }
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
        if (constants.isEmpty()) {
            return "<" + parent.getName() + ">";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(constants.get(0));
        for (int i = 1; i < constants.size(); i++) {
            sb.append("<");
            Role r = parent.getRole(roleNumbers.get(i - 1));
            sb.append(r.getNamePlusType());
            sb.append(">");
            sb.append(constants.get(i));
        }
        return sb.toString();
    }

    /**
     *
     * @param i 0<= i < size of the facttype-parent of this type expression
     * @return the number of the role which is used
     * on position i withinthis type expression
     */
    public int getRoleNumber(int i) {
        return roleNumbers.get(i);
    }

    private boolean otherOptionPossible(int fromIndex, /*List<String> constants,
             int indexConstants,*/ String separator, String expressionLowerCase) {
//        String constant = constants.get(indexConstants).toLowerCase();
//        String separator = separator(indexConstants);
//        if (separator == null) {
//            return true;
//        } else {
//            separator = separator.toLowerCase();
//        }

        int unto = expressionLowerCase.indexOf(separator.toLowerCase(), fromIndex);
        if (unto == -1) {
            return false;
        } else {
            return true;
        }
//        if (indexConstants == constants.size() - 1) {
//            // last constant
//            return true;
//        } else {
//            return otherOptionPossible(unto + constant.length(), constants,
//                    indexConstants + 1, expressionLowerCase);
//        }
    }

    private void checkRoleNumbers(List<Integer> roleNumbers) {
        if (roleNumbers.size() != constants.size() - 1) {
            throw new RuntimeException("count of roleNumbers doesn't match");
        }

        boolean[] numbers = new boolean[roleNumbers.size()];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = false;
        }
        for (int number : roleNumbers) {
            if (numbers[number]) {
                throw new RuntimeException("numbers are "
                        + "not different");
            }
            numbers[number] = true;

        }
    }

    private void trimHeadAndTailOf(List<String> constants) {
        if (constants.isEmpty()) {
            return;
        }

        String constant = constants.get(0);
        int i = 0;
        while (i < constant.length() && constant.charAt(i) == ' ') {
            i++;
        }
        constants.set(0, constant.substring(i));

        constant = constants.get(constants.size() - 1);
        i = constant.length() - 1;
        while (i >= 0 && constant.charAt(i) == ' ') {
            i--;
        }
        constants.set(constants.size() - 1, constant.substring(0, i + 1));

    }

    /**
     * setting of the role name of all roles where this type expression refers
     * to
     *
     * @param roleNames
     * @throws DuplicateException
     */
    public void setRoleNames(List<String> roleNames) throws DuplicateException {
        List<String> rns = new ArrayList<>(roleNames);
        parent.setRoleNames(rns);
    }
}
