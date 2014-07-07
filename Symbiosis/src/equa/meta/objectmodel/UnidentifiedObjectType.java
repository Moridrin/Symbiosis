package equa.meta.objectmodel;

import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import equa.meta.requirements.FactRequirement;

/**
 *
 * @author FrankP
 */
@Entity
public class UnidentifiedObjectType extends ObjectType {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private UnidentifiedObject unidentifiedObject;

    public UnidentifiedObjectType() {
    }

    /**
     *
     * @param parent
     */
    UnidentifiedObjectType(FactType parent) {
        super(parent, ObjectType.UNIDENTIFIED_OBJECTTYPE);
        init(parent, new ArrayList<String>(), null);
        unidentifiedObject = new UnidentifiedObject(this);
    }

    /**
     * parses the substitutionvalue out of expression. If expression is not
     * parseable null will be returned
     *
     * @param expression
     * @param source the source of expression
     * @return the resulting value; if not parsable: null
     */
    @Override
    public Value parse(String expression, boolean otherOptions, FactRequirement source) {
        String trimmed = expression.trim();
        if (trimmed.equals("<" + getName() + ">")) {
            return unidentifiedObject;
        } else {
            return null;
        }
    }

    @Override
    public String makeExpression(Value value) {
        return "<" + getName() + ">";
    }

//    @Override
//    public void deobjectify() {
//        // isn't allowed (importancy of use of pure value facts is unknown)
//    }
}
