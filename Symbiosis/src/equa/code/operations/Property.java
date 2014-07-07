package equa.code.operations;

import static equa.code.ImportType.*;

import java.util.ArrayList;
import java.util.List;

import equa.code.CodeClass;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;

public class Property extends Operation implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    protected ReturnType returnType;
    protected String name;
    protected final GetOrSet getter;
    protected final GetOrSet setter;
    protected final Relation relation;

    /**
     * new property belonging to ot with returnType and give name is created
     * with getter- en setter-functionality, based on source; in case of
     * boolean-property first 'is' will be added at the head van the name
     *
     * @param relation the relation where this property is based on
     * @param ot
     * @see #: Naming.isIdentifier())
     */
    public Property(Relation relation, ObjectType ot) {
        super(ot, relation.getParent());
        this.relation = relation;
        this.getter = new GetOrSet();
        this.setter = new GetOrSet();

        if (relation.isCollectionReturnType()) {
            name = relation.getPluralName();
            returnType = new ReturnType(new CT(CollectionKind.LIST, relation.targetType()));
            setSetter(false);
        } else {
            name = relation.name();
            returnType = new ReturnType(relation.targetType());
            if (relation.isSettable()) {
                setSetter(true);
            } else {

                setSetter(false);
                if (relation instanceof IdRelation) {
                    if (relation.getOwner().isMutable()) {
                        setSetter(true);
                        setAccessSetter(AccessModifier.NAMESPACE);
                    }
                } else {
                    Relation inverse = relation.inverse();
                    if (inverse != null) {
                        ObjectType target = (ObjectType) relation.targetType();
                        if (!target.isCompositionOf(ot)) {
                            if (inverse.isSettable()/**
                                     * || inverse.isRemovable()*
                                     */
                                    ) {
                                setSetter(true);
                                setAccessSetter(AccessModifier.NAMESPACE);
                            }
//                        if (target.isRemovable()) {
//                            if (ot.getResponsible() != target) {
//                                setSetter(true);
//                                setAccessSetter(AccessModifier.NAMESPACE);
//                            }
//                        }
                        }

                    }
                }
            }
        }
    }

    /**
     *
     * @return true if this property can be inspected otherwise false
     */
    public boolean isGetter() {
        return this.getter.isPresent();
    }

    /**
     *
     * @return true if this property possesses a public getter otherwise false
     */
    public boolean isPublicGetter() {
        return this.getter.isPresent() && this.getter.getAccessModifier() == AccessModifier.PUBLIC;
    }

    /**
     * changing of the inspect-functionality of this property
     *
     * @param getter
     */
    final void setGetter(boolean getter) {
        this.getter.setPresent(getter);
    }

    /**
     * setting of access modifier of the getter of this property
     *
     * @param access
     */
    void setAccessGetter(AccessModifier access) {
        getter.setAccessModifier(access);
    }

    public final AccessModifier getAccessGetter() {
        return getter.getAccessModifier();
    }

    /**
     *
     * @return true if this property can be changed otherwise false
     */
    public boolean isSetter() {
        return this.setter.isPresent();
    }

    /**
     * changing of the change-functionality of this property
     *
     * @param setter
     */
    final void setSetter(boolean setter) {
        this.setter.setPresent(setter);
    }

    /**
     * setting of access modifier of the setter of this property
     *
     * @param access
     */
    public final void setAccessSetter(AccessModifier access) {
        setter.setAccessModifier(access);
    }

    public final AccessModifier getAccessSetter() {
        return setter.getAccessModifier();
    }

    /**
     *
     * @return the type of this property
     */
    public ReturnType getReturnType() {
        return returnType;
    }

    /**
     * changing of the returntype of this property
     *
     * @param returnType
     */
    public void setReturnType(ReturnType returnType) {
        if (returnType == null) {
            throw new RuntimeException("PROPERTY-RETURNTYPE "
                    + "CANNOT BE NULL");
        }
        this.returnType = returnType;
        //publisher.inform(this, null, null, this);
    }

    /**
     *
     * @return the name of this property
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets new name for this property.
     *
     * @param newName for this property
     */
    @Override
    public void rename(String newName) {
        name = newName;
        //publisher.inform(this, null, null, this);
    }

    /**
     *
     * @return 2, always.
     */
    @Override
    public int order() {
        return 2;
    }

    /**
     * Order comparison or name comparison is returned.
     *
     * @param o the behavioral feature to be compared with this property.
     * @return If the order of o is distinct to 2, the difference of orders is
     * returned; otherwise the String comparison of names is returned.
     */
    @Override
    public int compareTo(Operation o) {
        if (order() != o.order()) {
            return order() - o.order();
        }

        return name.compareTo(o.getName());

    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Property) {
            return compareTo((Property) object) == 0;
        } else {
            return false;
        }
    }

    public boolean adaptName(CodeClass codeClass) {
        int nr = 1;
        while (codeClass.getOperation(name + "_" + nr) != null) {
            nr++;
        }
        name = name + "_" + nr;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder propString = new StringBuilder();
        propString.append(getAccess().getAbbreviation());
        propString.append(" ");
        propString.append(name);
        propString.append(" : ");
        propString.append(returnType.toString());

        if (getter.isPresent()) {
            if (setter.isPresent()) {
                propString.append(" {").append(getter.getAccessString()).append("get, ").append(setter.getAccessString()).append("set}");
            } else {
                propString.append(" {").append(getter.getAccessString()).append("get}");
            }
        } else if (setter.isPresent()) {
            propString.append(" {").append(setter.getAccessString()).append("set}");
        }

        return propString.toString();
    }

    /**
     *
     * @return non public access strings of the getter and setter (if those
     * strings are present; see: {@link GetOrSet#isPresent()}).
     */
    @Override
    public String getNameParamTypesAndReturnType() {
        StringBuilder sb = new StringBuilder(getAccess().getAbbreviation());
        sb.append(" ").append(getName());
        sb.append(" : ").append(getReturnType());

        if (getter.isPresent()) {
            sb.append("{");
            String getterString = getter.getAccessStringNonPublic() + "get";
            if (setter.isPresent()) {
                String setterString = setter.getAccessStringNonPublic() + "set";
                sb.append(getterString).append(",").append(setterString);
            } else {
                sb.append(getterString);
            }
            sb.append("}");
        } else {
            if (setter.isPresent()) {
                sb.append("{");
                String setterString = setter.getAccessStringNonPublic() + "set";
                sb.append(setterString);
                sb.append("}");
            }
        }
        return sb.toString();
    }

    @Override
    public IndentedList getCode(Language l) {
        return l.property(this);
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        StringBuilder returnSpec = new StringBuilder();
        if (relation.hasMultipleTarget()) {
            if (relation.getMinFreq() == relation.getMaxFreq()) {
                returnSpec.append("a collection with the ").append(Integer.toString(relation.getMinFreq())).
                        append(" ").append(getName()).append(" of ").append(Naming.withoutCapital(getParent().getName()));
            } else {
                returnSpec.append("a collection with all ").append(getName()).append(" of ").append(Naming.withoutCapital(getParent().getName()));
            }
        } else {
            returnSpec.append(callString()).append(" of this ").append(Naming.withoutCapital(getParent().getName()));
        }

        //StringBuilder postSpec = new StringBuilder();
        //postSpec.append("The ").append(getName()).append(" of this ").append(Naming.withoutCapital(parent.getName()));
        if (relation.isDerivable()) {
            returnSpec.append("; the value is derived conform the rule: \"").append(relation.getDerivableText()).append("\"");
        } else if (relation instanceof IdRelation && relation.isQualifier()) {
            returnSpec.append("; the value can be retrieved at the qualified object.");
        }
        if (!relation.isMandatory() && !relation.hasMultipleTarget()) {
            if (relation.targetType().getUndefinedString() == null && !relation.targetType().equals(BaseType.BOOLEAN)) {
                IBooleanOperation isdefined = (IBooleanOperation) ((ObjectType) getParent()).getCodeClass().getOperation("isDefined", relation);
                BooleanCall undefinedCall = ((BooleanCall) isdefined.call()).withNegation();
                setPreSpec(undefinedCall);
            } else if (!relation.targetType().equals(BaseType.BOOLEAN)) {
                returnSpec.append("; @result could be undefined, " + "in that case ").append(relation.targetType().getUndefinedString()).append(" will be returned");
            }
        }
        returnType.setSpec(returnSpec.toString());

        if (relation.isSettable()) {
            setter.setSpec("property value is set to assigned value");
        }
    }

    @Override
    public List<ImportType> getImports() {
        List<ImportType> list = new ArrayList<>();
        if (relation.isCollectionReturnType()) {
            list.add(Linq);
            switch (relation.collectionType().getKind()) {
                case COLL:
                    list.add(Collections);
                    list.add(Collection);
                    break;
                case ITERATOR:
                    list.add(Iterator);
                    break;
                case LIST:
                    list.add(Collections);
                    list.add(ImportType.List);
                    list.add(ArrayList);
                    break;
                case MAP:
                    list.add(Collections);
                    list.add(Map);
                    list.add(HashMap);
                    break;
                case SET:
                    list.add(ImportType.List);
                    list.add(ArrayList);
                    break;
                case ARRAY:
                    break;
            }
        }
        return list;
    }

    @Override
    public String getSpec() {
        StringBuilder sb = new StringBuilder(super.getSpec());
        sb.append("Returns:\t");
        sb.append(returnType.getSpec());
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public boolean isDerivable() {
        return relation.isDerivable();
    }

    @Override
    public Call call(List<? extends ActualParam> actualParams) {
        return new Call(this);
    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        return getName();
    }

    @Override
    public String callString() {
        return getName();
    }

    @Override
    public Call call() {
        return new Call(this);
    }

    @Override
    public CodeClass getCodeClass() {
        return ((ObjectType) getParent()).getCodeClass();
    }
}
