package equa.code.operations;

import java.io.Serializable;
import java.util.List;

import equa.code.Language;
import equa.meta.classrelations.Relation;

public class Param implements ActualParam, Comparable<Param>, Serializable {

    private static final long serialVersionUID = 1L;
    private STorCT type;
    private String name;
    private boolean generated;
    private IPredicate preSpec;
    private String explanation;
    private final Relation relation;

    /**
     * creation of parameter with given name and type; precondition is unknown
     * and explanation is empty
     *
     * @param name
     * @param type
     * @param relation
     */
    public Param(String name, STorCT type, Relation relation) {
        this.type = type;
        this.name = name;
        preSpec = null;
        explanation = "";
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated() {
        generated = true;
    }

    /**
     *
     * @return the precondition belonging tot this parameter, could be
     * unknown(=null)
     */
    public IPredicate getPreSpec() {
        return this.preSpec;
    }

    /**
     * changing of the precondition of this parameter
     *
     * @param preSpec
     */
    public void setPreSpec(IPredicate preSpec) {
        this.preSpec = preSpec;
    }

    /**
     *
     * @return the explanation on this paramter, could be unknown (=isEmpty)
     */
    public String getExplanation() {
        return this.explanation;
    }

    /**
     * changing of the explanation on this parameter
     *
     * @param explanation
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * @return the name of this parameter
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the type of this parameter
     */
    public STorCT getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(STorCT type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Param) {
            return ((Param) o).name.equals(name);
        }
        return false;
    }

    @Override
    public int compareTo(Param o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return getName() + " : " + type.toString();
    }

    /**
     *
     * @param params
     * @return true if this param occurs in params otherwise false
     */
    public boolean occursIn(List<Param> params) {
        for (Param param : params) {
            if (getName().equals(param.getName()) && getType().equals(param.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String expressIn(Language l) {
//    	if (type instanceof ObjectType) {
//			ObjectType ot = (ObjectType) type;
//    		List<Param> params = ot.transformToBaseTypes(this);
//    		StringBuilder sb = new StringBuilder(name);
//    		sb.append(l.memberOperator());
//    		sb.append(l.getProperty(params.get(0).name));
//    		for (int i = 1; i < params.size(); i++) {
//    			sb.append(", ");
//    			sb.append(l.memberOperator());
//    			sb.append(l.getProperty(params.get(i).name));
//    		}
//    		return sb.toString();
//    	}
        return name;
    }

    @Override
    public String callString() {
        return getName();
    }

}
