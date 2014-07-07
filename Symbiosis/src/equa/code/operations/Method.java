package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.code.CodeClass;
import equa.meta.DuplicateException;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.traceability.ModelElement;
import equa.util.Naming;

public abstract class Method extends OperationWithParams {

    private static final long serialVersionUID = 1L;

    protected ReturnType returnType;
    protected String name;
    private boolean classMethod, overrideMethod;

    /**
     * creation of a void-method belonging to ft, with given name and params,
     * based on source
     *
     * @param ot
     * @param name must be an identifier (
     * @see #: Naming.isIdentifier() )
     * @param params
     * @param source
     * @throws DuplicateException if the signature of this method is in conflict
     * with another method of ot
     */
    public Method(ObjectType ot, String name, List<Param> params, ModelElement source) {
        super(ot, params, source);
        if (!Naming.isIdentifier(name)) {
            throw new RuntimeException(("NAME DOESN'T")
                    + (" SATISFY THE RULES OF AN IDENTIFIER"));
        }
        this.name = name;
        this.returnType = new ReturnType(null);
        classMethod = false;
    }

    /**
     * creation of a system class method
     *
     * @param om
     * @param name
     * @param params
     * @param source
     */
    public Method(ObjectModel om, String name, List<Param> params, ModelElement source) {
        super(om, params, source);
        if (!Naming.isIdentifier(name)) {
            throw new RuntimeException(("NAME DOESN'T")
                    + (" SATISFY THE RULES OF AN IDENTIFIER"));
        }
        this.name = name;
        this.returnType = new ReturnType(null);
        classMethod = true;
    }

    @Override
    public boolean adaptName(CodeClass codeClass) {
        int nr = 1;
        while (codeClass.getOperation(name + "_" + nr) != null) {
            nr++;
        }
        name = name + "_" + nr;
        return true;
    }

    @Override
    public void rename(String newName) {
        name = newName;
        //publisher.inform(this, null, null, this);
    }

    /**
     *
     * @return the returntype of this method, could be null (=void)
     */
    public ReturnType getReturnType() {
        return returnType;
    }

    /**
     * setting of the returntype of this method
     *
     * @param returnType null (=void) is allowed
     */
    public void setReturnType(ReturnType returnType) {
        if (returnType == null) {
            this.returnType = new ReturnType(null);
        } else {
            this.returnType = returnType;
        }
        //publisher.inform(this, null, null, this);

    }

    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return true if method belongs to classifier, false if method belongs to
     * instance
     */
    @Override
    public boolean isClassMethod() {
        return classMethod;
    }

    public boolean isOverrideMethod() {
        return overrideMethod;
    }

    public void setOverrideMethod(boolean overrideMethod) {
        this.overrideMethod = overrideMethod;
    }

    /**
     * setting of the aspect if method belongs to instance or class
     *
     * @param classMethod
     */
    public void setClassMethod(boolean classMethod) {
        this.classMethod = classMethod;
    }

    @Override
    public int order() {
        if (isClassMethod()) {
            return 4;
        } else {
            return 3;
        }
    }

    @Override
    public int compareTo(Operation o) {
        if (order() != o.order()) {
            return order() - o.order();
        }

        if (!name.equals(o.getName())) {
            return name.compareTo(o.getName());
        }

        return super.compareTo(o);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Method) {
            return compareTo((Method) object) == 0;
        } else {
            return false;
        }
    }

    public boolean hasSignature(String name, List<STorCT> params) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (getParams().size() != params.size()) {
            return false;
        }
        int i = 0;
        for (Param param : getParams()) {
            if (!param.getType().equals(params.get(i))) {
                return false;
            }
            i++;
        }
        return true;
    }

    @Override
    public String toString() {
        String str;
        if (returnType != null) {
            str = super.toString() + " : " + returnType.toString();
        } else {
            str = super.toString() + " : void";
        }
        if (isClassMethod()) {
            str += " {class method}";
        }

        return str;
    }

    @Override
    public String getNameParamTypesAndReturnType() {

        StringBuilder sb = new StringBuilder();
        sb.append(getAccess().getAbbreviation()).append(" ");
        if (isClassMethod()) {
            sb.append("$ ");
        }
        sb.append(getName());
        boolean withName = false;
        sb.append(paramList(withName));
        sb.append(" : ");
        if (returnType == null) {
            sb.append("void");
        } else {
            sb.append(getReturnType().toString());
        }
        return sb.toString();
    }

    static String addIndices(Relation relation, List<Param> params) {
        String indexString;

        List<Role> qualifiers = relation.qualifierRoles();
        if (qualifiers.isEmpty()) {
            indexString = "";
        } else {
            indexString = "(";
            for (Role role : qualifiers) {
                params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), relation));
                indexString += role.detectRoleName() + ",";
            }
            indexString = indexString.substring(0, indexString.length() - 1) + ")";
        }
        return indexString;
    }

    static List<Param> addQualifiers(List<Param> params, Relation relation) {
        if (relation.isMapRelation() || relation.isSeqRelation()) {
            for (Role role : relation.qualifierRoles()) {
                params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), new IdRelation(relation.getOwner(), role)));
            }
        }
        return params;
    }

    static List<Param> qualifierParams(Relation relation) {
        List<Param> params = new ArrayList<>();
        if (relation.isMapRelation()) {
            for (Role role : relation.qualifierRoles()) {
                params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), new IdRelation(relation.getOwner(), role)));
            }
        }
        return params;
    }

    static BooleanCall qualifiersCondition(Relation relation) {
        BooleanCall call = null;
        for (Role role : relation.qualifierRoles()) {
            if (role.getSubstitutionType().equals(BaseType.NATURAL)) {
                if (call == null) {
                    //call = new BooleanCall(getObjectModel().getIsNaturalMethod(), false);
                }
            }
        }
        return call;

    }

    @Override
    public String getSpec() {
        if (returnType != null) {
            StringBuilder sb = new StringBuilder(super.getSpec());
            sb.append("Returns:\t");
            sb.append(returnType.getSpec());
            sb.append(System.lineSeparator());
            return sb.toString();
        } else {
            return super.getSpec();
        }
    }

    public String returnValue() {
        if (returnType == null) {
            return null;
        } else {
            return returnType.getSpec();
        }
    }

}
