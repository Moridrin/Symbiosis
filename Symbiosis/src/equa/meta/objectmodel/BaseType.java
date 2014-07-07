package equa.meta.objectmodel;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import equa.code.operations.ActualParam;
import equa.code.operations.Null;
import equa.code.operations.Param;
import equa.code.operations.ValueString;
import equa.meta.MismatchException;
import equa.meta.requirements.FactRequirement;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;

/**
 *
 * @author frankpeeters
 */
@Entity
public class BaseType extends ParentElement implements SubstitutionType, Serializable {

    private static final long serialVersionUID = 1L;
    public static final BaseType STRING = new BaseType("String", "", new String(new char[]{255, 255}), "null");
    public static final BaseType INTEGER = new BaseType("Integer", Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    public static final BaseType NATURAL = new BaseType("Natural", 0, Integer.MAX_VALUE, "-1");
    public static final BaseType REAL = new BaseType("Real", Double.MIN_VALUE, Double.MAX_VALUE, null);
    public static final BaseType CHARACTER = new BaseType("Character", Character.MIN_VALUE, Character.MAX_VALUE, null);
    public static final BaseType BOOLEAN = new BaseType("Boolean", Boolean.FALSE, Boolean.TRUE, null);
    public static final BaseType OBJECT = new BaseType("Object", "null");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String name;
    @Column
    private String minString;
    @Column
    private String maxString;
    @Column
    private String undefined;

    public BaseType() {
    }

    BaseType(String name, Object min, Object max, String undefined) {
        this.name = name;
        this.minString = min.toString();
        this.maxString = max.toString();
        this.undefined = undefined;

    }

    BaseType(String name, String undefined) {

        this.name = name;
        this.minString = null;
        this.maxString = null;
        this.undefined = undefined;

    }

    /**
     *
     * @return the smallest possible base value, if existing
     */
    public BaseValue getMinValue() {
        try {
            if (minString == null) {
                return null;
            }
            BaseValue bv = new BaseValue(minString, this);
            return bv;
        } catch (MismatchException ex) {
            Logger.getLogger(BaseType.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @return the biggest possible base value, if existing
     */
    public BaseValue getMaxValue() {
        try {
            if (maxString == null) {
                return null;
            }
            BaseValue bv = new BaseValue(maxString, this);
            return bv;
        } catch (MismatchException ex) {
            Logger.getLogger(BaseType.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public String makeExpression(Value value) {
        return ((BaseValue) value).toString();
    }

    @Override
    public Value parse(String expression, boolean otherOptions, FactRequirement source)
            throws MismatchException {
        return new BaseValue(expression, this);
    }

    @Override
    public void involvedIn(Role role) {
    }

    @Override
    public void resignFrom(Role role) {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Type o) {
        return this.getName().compareTo(o.getName());
    }

    /**
     *
     * @param name
     * @return the base type with the requested name
     */
    public static BaseType getBaseType(String name) {
        if (name.equalsIgnoreCase("string")) {
            return STRING;
        }
        if (name.equalsIgnoreCase("integer")) {
            return INTEGER;
        }
        if (name.equalsIgnoreCase("natural")) {
            return NATURAL;
        }
        if (name.equalsIgnoreCase("real")) {
            return REAL;
        }
        if (name.equalsIgnoreCase("character")) {
            return CHARACTER;
        }
        if (name.equalsIgnoreCase("boolean")) {
            return BOOLEAN;
        }
        if (name.equalsIgnoreCase("object")) {
            return OBJECT;
        }
        return null;
    }

    /**
     * if value doesn't match with this basetype, a MisMatchException will be
     * raised
     *
     * @param value
     * @throws MismatchException
     */
    public void checkSyntaxis(String value) throws MismatchException {

        if (this.equals(CHARACTER)) {
            if (value.length() != 1) {
                throw new MismatchException(null, "character base value consists "
                        + "out of exactly one character");
            }
        } else if (this.isNumber()) {
            try {

                Number number;
                number = NumberFormat.getNumberInstance().parse(value);
                if (number != null && number instanceof Long) {
                    // Long value
                } else {
                    throw new MismatchException(null, "base value string cannot be "
                            + "transformed to a number");
                }
            } catch (ParseException ex) {
                throw new MismatchException(null, "base value string cannot be "
                        + "transformed to a number");
            }

        } else if (this.equals(REAL)) {
            try {
                Double.parseDouble(value);
//                Number number;
//                number = DecimalFormat.getNumberInstance().parse(value);
//                if (number != null
//                        && ((number instanceof Double) || (number instanceof Long))) {
//                    // Double value
//                } else {
//                    throw new MismatchException("base value string cannot be "
//                            + "transformed to a decimal");
//                }
            } catch (NumberFormatException exc) {
                throw new MismatchException(null, "base value string cannot be "
                        + "transformed to a decimal");
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SubstitutionType) {
            return ((SubstitutionType) object).compareTo(this) == 0;
        }
        return false;
    }

    @Override
    public boolean isEqualOrSupertypeOf(SubstitutionType st) {
        return equals(st);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean hasAbstractRoles() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public boolean isParsable() {
        return true;
    }

    @Override
    public boolean isSuitableAsIndex() {
        return this.equals(BaseType.NATURAL) || this.equals(BaseType.CHARACTER);
    }

    @Override
    public boolean isNumber() {
        return this.equals(BaseType.NATURAL) || this.equals(BaseType.INTEGER) || this.equals(BaseType.REAL);
    }

    @Override
    public boolean isValueType() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void remove(ModelElement member) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPureFactType() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public List<Param> transformToBaseTypes(Param param) {
        List<Param> params = new ArrayList<>();
        params.add(param);
        return params;
    }

    @Override
    public String getUndefinedString() {
        return undefined;
    }

    @Override
    public ActualParam getUndefined() {

        if (undefined == null) {
            return Null.NULL;
        } else {
            return new ValueString(undefined, this);
        }
    }

    @Override
    public boolean isAddable() {
        return false;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

}
