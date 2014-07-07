package equa.meta.objectmodel;

import equa.code.operations.ActualParam;
import equa.code.operations.STorCT;
import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.requirements.FactRequirement;

public interface SubstitutionType extends STorCT {

    /**
     *
     * @param value
     * @return a complete expression based on value and in case of an
     * objecttype: the OTE of this substitutiontype.
     */
    String makeExpression(Value value);

    /**
     * @param expression
     * @param parsingOptions must the parser take other options into account (if
     * yes, this can result in a NotParasbleException)
     * @param source the source of expression
     * @return parsed substitutionvalue out of expression. If expression is not
     * parsable null will be returned
     * @throws NotParsableException if parsing has more dan one option or if
     * some of the constants doesn't match
     *
     */
    Value parse(String expression, boolean parsingOptions, FactRequirement source)
            throws MismatchException;

    /**
     * this substitutiontype gets involved in role
     *
     * @param role this substitutiontypetype doesn't play a role in given role
     * yet
     */
    void involvedIn(Role role);

    /**
     * this substitutiontype will stop playing a role in role
     *
     * @param role
     */
    void resignFrom(Role role);

    /**
     *
     * @param st
     * @return true if this substitutiontype is equal or a supertype of st,
     * otherwise false
     */
    boolean isEqualOrSupertypeOf(SubstitutionType st);

    /**
     *
     * @return true if this type is an objecttype with direct or indirect
     * abstract roles, otherwise false
     */
    boolean hasAbstractRoles();

    /**
     *
     * @return the literal of this type with the exclusive meaning of
     * 'undefined', but if this substitutiontype doesn't possess an undefined
     * value null will be returned
     */
    String getUndefinedString();

    ActualParam getUndefined();

    /**
     *
     * @return true if values of this substitutiontype could be removed;
     * basetypes always return false
     */
    boolean isRemovable();

    boolean isAddable();

    boolean isSettable();

    boolean isAdjustable();

    boolean isInsertable();

    /**
     *
     * @return true if a value of this type can always be parsed, else false
     */
    boolean isParsable();

    /**
     *
     * @return true if type is Natural, Character or a subrange of Integer,
     * Natural or Character, else false
     */
    public boolean isSuitableAsIndex();

    /**
     *
     * @return true if this type offers the opportunity to do arithmetical
     * adding else false
     */
    public boolean isNumber();

    /**
     *
     * @return true if this type is a (constrained) base type
     */
    public abstract boolean isTerminal();

    /**
     *
     * @return true if element of this type can be replaced, without exception,
     * by anonother element with the same value
     */
    public boolean isValueType();

    public boolean isSingleton();

    public boolean isMutable();
}
