package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import equa.code.CodeClass;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.DuplicateException;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.BaseValue;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Range;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.traceability.ModelElement;
import equa.util.Naming;

public class Constructor extends OperationWithParams {

    private static final long serialVersionUID = 1L;
    private final List<Relation> relations;

    /**
     * creation of a constructor in hehalf of parent with give params, based on
     * source
     *
     * @param relations
     * @param parent
     * @param source
     * @throws DuplicateException if there exists at parent a constructor with
     * the same signature
     */
    public Constructor(List<Relation> relations, ObjectType parent, ModelElement source) throws DuplicateException {
        super(parent, null, source);
        this.relations = relations;
        List<Param> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        boolean addIdd = true;
        // collecting parameters of supertype-constructor
        for (ObjectType supertype : parent.allSupertypes()) {
            Iterator<Param> itParams = supertype.getCodeClass().constructorParams();
            while (itParams.hasNext()) {
                Param param = itParams.next();
                if (!param.occursIn(params)) {
                    if (param.getRelation().isPartOfId()) {
                        addIdd = false;
                    }
                    String paramName = detectUniqueName(param.getName(), names);
                    Param paramNew = new Param(paramName, param.getType(), param.getRelation());
                    params.add(paramNew);
                    if (param.isGenerated()) {
                        paramNew.setGenerated();
                    }
                }
            }
        }

        for (Relation relation : relations) {
            if (((addIdd && (relation.isPartOfId() || relation.isMandatory()))
                    || (!addIdd && (!relation.isPartOfId() && relation.isMandatory())))
                    //((relation.isPartOfId() || relation.isMandatory()))
                    && !relation.isDerivable() && !relation.targetType().isSingleton()
                    && relation.isNavigable() && relation.hasNoDefaultValue()) {
                // only mandatory non derivable relations without default value
                // are important

                String paramName;
                SubstitutionType target = relation.targetType();
                Relation mandatorialInverse = null;
                ObjectType targetOt = null;
                if (target instanceof ObjectType) {
                    targetOt = (ObjectType) target;
                    mandatorialInverse = targetOt.retrieveMandatorialRelationTo(parent);
                }
                if (mandatorialInverse != null) {
                    Relation mandatorialRelation = mandatorialInverse.inverse();
                    if (mandatorialRelation.isResponsible()) {
                        // pick up necessary init-data in behalf of call
                        // constructor
                        Iterator<Param> itParams = targetOt.getCodeClass().constructorParams();
                        while (itParams.hasNext()) {
                            Param param = itParams.next();
                            if (!param.isGenerated()) {
                                if (!param.getName().equals(mandatorialInverse.name())) {
                                    // no parameter which is based on this
                                    // particular relation

                                    if (relation.hasMultipleTarget() || relation.isSeqRelation()) {
                                        paramName = param.getRelation().getPluralName();
                                    } else {
                                        paramName = param.getName();
                                    }
                                    if (!relation.name().equals(paramName)) {
                                        paramName = relation.name() + Naming.withCapital(paramName);
                                        // FP change by JH rejected
                                    }
                                    paramName = detectUniqueName(paramName, names);
                                    Param paramNew;
                                    if (relation.hasMultipleTarget() || relation.isSeqRelation()) {
                                        // sometimes an objecttype needs a
                                        // frozen collection of
                                        // values; in that case you need that
                                        // collection
                                        // at initialize-time
                                        paramNew = new Param(paramName, new CT(CollectionKind.COLL,
                                                param.getType()), relation);

                                    } else {
                                        paramNew = new Param(paramName, param.getType(), relation);
                                    }

                                    params.add(paramNew);
                                }
                            }
                        }
                    } else { // mandatorial reverse and not responsible
                        Param param;
                        if (relation.hasMultipleTarget()) {
                            param = new Param(relation.name(), new CT(CollectionKind.COLL, targetOt), relation);
                        } else {
                            param = new Param(relation.name(), targetOt, relation);
                        }
                        params.add(param);
                    }
                } else { // no mandatorial reverse

                    paramName = detectUniqueName(relation.name(), names);
                    if (relation.hasMultipleTarget() || relation.isSeqRelation()) {
                        // sometimes an objecttype needs a frozen collection
                        // of
                        // values; in that case you need that collection
                        // at initialize-time
                        params.add(new Param(relation.getPluralName(), new CT(CollectionKind.LIST, target), relation));
                    } else {
                        Param param = new Param(paramName, target, relation);
                        if (relation.isAutoIncr()) {
                            param.setGenerated();
                        }
                        params.add(param);
                    }

                }
            }
        }

        setParams(params);
    }

    /**
     *
     * @return name of the parent object type.
     */
    @Override
    public String getName() {
        return ((ObjectType) getParent()).getName();
    }

    /**
     *
     * @return 1, always.
     */
    @Override
    public int order() {
        return 1;
    }

    @Override
    public int compareTo(Operation o) {
        if (order() != o.order()) {
            return order() - o.order();
        }

        return ((Constructor) o).getParent().getName().compareTo(getParent().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Constructor) {
            return ((Constructor) o).getParent().equals(getParent());
        }
        return false;
    }

    /**
     * renaming of constructor is impossible.
     *
     * @param newName is not used.
     */
    @Override
    public void rename(String newName) {
        // renaming of constructor is impossible
    }

    /**
     *
     * @return Abbreviation of the access modifier, the name of constructor and
     * type of parameter(s).
     */
    @Override
    public String getNameParamTypesAndReturnType() {
        return getAccess().getAbbreviation() + " " + getName() + paramList(false);
    }

    private String searchParamName(Relation r) {
        for (Param p : getParams()) {
            if (p.getRelation().equals(r)) {
                return p.getName();
            }
        }
        return null;
    }

    private List<String> searchParams(Relation r) {
        List<String> pl = new ArrayList<>();
        for (Param p : getParams()) {
            if (p.getRelation().equals(r)) {
                pl.add(p.getName());
            }
        }
        return pl;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        // single inheritance only
        Iterator<ObjectType> supers = ((ObjectType) getParent()).supertypes();
        // inheritance, call super constructor
        if (supers.hasNext()) {
            ObjectType superOt = supers.next();
            Constructor superC = superOt.getCodeClass().getConstructor();
            List<Param> superParams = superC.getParams();
            List<String> superParamNames = new ArrayList<>();
            for (Param p : superParams) {
                String search = searchParamName(p.getRelation());
                superParamNames.add(search);
            }
            list.add(l.constructorHeaderAndSuper(this, superParamNames));
            // no inheritance, call a normal constructor with all values
        } else {
            list.add(l.operationHeader(this));
        }
        // the remaining params are assigned with this.
        Iterator<Field> fields = getCodeClass().getFields();
        while (fields.hasNext()) {
            Field f = fields.next();
            Relation r = f.getRelation();
            if (r != null) {
                if (r.isMandatory()) {
                    if (r.hasNoDefaultValue()) {
                        if (r.targetType() instanceof ObjectType) {
                            ObjectType ot = (ObjectType) r.targetType();
                            if (getParent().equals(ot.getResponsible())) {
                                List<String> constructorParams = new ArrayList<>();
                                Iterator<Param> it = ot.getCodeClass().constructorParams();
                                Iterator<String> params = searchParams(r).iterator();
                                while (it.hasNext()) {
                                    Param p = it.next();
                                    if (p.getType().equals(getParent())) {
                                        constructorParams.add(l.thisKeyword());
                                    } else if (p.getRelation().isAutoIncr()) {
                                        constructorParams.add(l.autoIncr(p.getRelation().inverse().fieldName()));
                                    } else {
                                        constructorParams.add(params.next());
                                    }
                                }
                                list.add(l.assignment(f.getName(),
                                        l.callConstructor(ot.getName(), constructorParams.toArray(new String[0]))));

                                Relation inverse = r.inverse();
                                if (inverse != null && inverse.isNavigable()) {
                                    if (inverse != null && inverse.isNavigable()) {
                                        // register
                                        if (r.isCollectionReturnType()) {
                                            list.add(l.callMethod(r.fieldName(), RegisterMethod.NAME, l.thisKeyword()) + l.endLine());
                                        }
                                    }
                                }
                            } else {
                                list.add(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), searchParamName(r)));
                            }
                        } else {
                            list.add(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), searchParamName(r)));
                        }
                    } else {
                        if (r.targetType().equals(BaseType.STRING)) {
                            list.add(l.assignment(r.fieldName(), l.stringSymbol() + r.getDefaultValue() + l.stringSymbol()));
                        } else {
                            if (r.hasMultipleTarget()) {
                                list.add(l.assignment(r.fieldName(), l.assignCollection(r.collectionType())));
                            }
                            if (r.isCollectionReturnType()) {
                                if (r.targetType() instanceof ConstrainedBaseType) {
                                    list.add(l.add(r.fieldName(), r.collectionType().getKind(),
                                            l.newInstance(r.targetType(), r.getDefaultValue()))
                                            + l.endLine());
                                } else {
                                    list.add(l.add(r.fieldName(), r.collectionType().getKind(), r.getDefaultValue()) + l.endLine());
                                }
                            } else if (r.isMapRelation()) {
                                MapType mt = (MapType) r.collectionType();
                                if (mt.getKeyType() instanceof ConstrainedBaseType) {
                                    ConstrainedBaseType cbt = (ConstrainedBaseType) mt.getKeyType();
                                    Iterator<Range> ranges = cbt.getValueConstraint().ranges();
                                    while (ranges.hasNext()) {
                                        Range range = ranges.next();
                                        BaseType bt = range.getLower().getType();
                                        if (bt.equals(BaseType.INTEGER) || bt.equals(BaseType.NATURAL)) {
                                            int lower = Integer.parseInt(range.getLower().getName());
                                            int upper = Integer.parseInt(range.getUpper().getName());
                                            for (int i = lower; i <= upper; i++) {
                                                list.add(l.put(r.fieldName(), l.newInstance(cbt, String.valueOf(i)), r.getDefaultValue()) + l.endLine());
                                            }
                                        }
                                    }
                                    Iterator<BaseValue> baseValues = cbt.getValueConstraint().values();
                                    while (baseValues.hasNext()) {
                                        BaseValue bv = baseValues.next();
                                        String key = bv.getName();
                                        String value = r.getDefaultValue();
                                        if (cbt.getBaseType().equals(BaseType.STRING)) {
                                            key = l.stringSymbol() + bv.getName() + l.stringSymbol();
                                        }
                                        if (bv.getType().equals(BaseType.STRING)) {
                                            value = l.stringSymbol() + r.getDefaultValue() + l.stringSymbol();
                                        }
                                        list.add(l.put(r.fieldName(), l.newInstance(cbt, key), value) + l.endLine());
                                    }
                                }
                            } else if (r.targetType() instanceof ConstrainedBaseType) {
                                ConstrainedBaseType cbt = (ConstrainedBaseType) r.targetType();
                                if (cbt.getBaseType().equals(BaseType.STRING)) {
                                    list.add(l.assignment(r.fieldName(),
                                            l.newInstance(r.targetType(), l.stringSymbol() + r.getDefaultValue() + l.stringSymbol())));
                                } else {
                                    list.add(l.assignment(r.fieldName(), l.newInstance(r.targetType(), r.getDefaultValue())));
                                }
                            } else {
                                list.add(l.assignment(r.fieldName(), r.getDefaultValue()));
                            }
                        }
                    }
                } else {
                    if (r.hasMultipleTarget()) {
                        list.add(l.assignment(r.fieldName(), l.assignCollection(r.collectionType())));
                    }
                }
            } else {

            }
        }
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }

    @Override
    public String callString() {
        return "new " + getName() + paramCallList();
    }

    @Override
    public void initSpec() {
        ObjectType ot = (ObjectType) getParent();

        IBooleanOperation isCorrectValue = (IBooleanOperation) getCodeClass().getOperation("isCorrectValue");
        if (isCorrectValue != null) {
            setPreSpec(new BooleanCall(isCorrectValue, false));
        }

        Map<String, String> propMapping = new HashMap<>();
        Set<Property> properties = ot.getCodeClass().publicProperties();
        for (Property property : properties) {
            if (!property.isDerivable()) {
                String key;
                if (property.relation.hasMultipleTarget()) {
                    key = property.callString() + "(i)";
                } else {
                    key = property.callString();
                }
                List<Param> paramsRelation = getParams(property.relation);
                if (!paramsRelation.isEmpty()) {
                    if (property.relation.targetType() instanceof ObjectType) {
                        ObjectType target = (ObjectType) property.relation.targetType();
                        ObjectType responsible = target.getResponsible();
                        if (responsible != null && responsible.equals(ot)) {
                            propMapping.put(key, target.getCodeClass().getConstructor().callString(paramsRelation));
                        } else {
                            if (property.relation.hasMultipleTarget()) {
                                propMapping.put(key, paramsRelation.get(0).getName() + "(i)");
                            } else {
                                propMapping.put(key, paramsRelation.get(0).getName());
                            }
                        }
                    } else {
                        if (property.relation.hasMultipleTarget()) {
                            propMapping.put(key, paramsRelation.get(0).getName() + "(i)");
                        } else {
                            propMapping.put(key, paramsRelation.get(0).getName());
                        }
                    }
                } else if (property.relation.isMandatory()) {
                    if (!property.relation.hasNoDefaultValue()) {

                        propMapping.put(key, property.relation.getDefaultValue());
                    } else {
                        propMapping.put(key, Naming.withoutCapital(property.getReturnType().getSpec()));
                    }
                } else if (property.getReturnType().getType() instanceof CT) {
                    propMapping.put(property.callString(), "empty");
                } else {
                    propMapping.put(key, "?");
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(self()).append(" is created with properties ").append(propMapping.toString());

        for (Param param : getParams()) {
            Relation relation = param.getRelation();
            if (relation.isResponsible()) {
                Relation inverse = relation.inverse();
                if (inverse != null && inverse.isNavigable()) {
                    sb.append(" AND ").append(param.getName());
                    if (inverse.hasMultipleTarget()) {
                        sb.append(".contains(").append(self()).append(")");
                    } else {
                        sb.append(" knows ").append(self());
                    }
                }
            }
        }
        setPostSpec(new InformalPredicate(sb.toString()));

    }

    private List<Param> getParams(Relation relation) {
        List<Param> params = new ArrayList<>();
        for (Param param : getParams()) {
            if (param.getRelation().equals(relation)) {
                params.add(param);
            }
        }
        return params;
    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        if (actualParams.size() == getParams().size()) {
            return "new " + super.callString(actualParams);
        } else {
            return "new " + super.callString(getParams());
        }

    }

    @Override
    public boolean isUnspecified() {
        return false;
    }

    @Override
    public void setUnspecified(boolean abstrct) {
        throw new UnsupportedOperationException("Constructor cannot be abstract.");
    }

    public boolean adaptName(CodeClass codeClass) {
        return false;
    }

}
