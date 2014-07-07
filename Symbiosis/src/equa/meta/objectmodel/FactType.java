package equa.meta.objectmodel;

import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.factbreakdown.CollectionNode;
import equa.factbreakdown.FactNode;
import equa.factbreakdown.ObjectNode;
import equa.factbreakdown.ParentNode;
import equa.factbreakdown.ValueLeaf;
import equa.factbreakdown.ValueNode;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.classrelations.CollectionIdRelation;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.QualifiedIdRelation;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.Impact;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Source;
import equa.meta.traceability.SystemInput;
import equa.project.ITerm;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.util.Naming;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author FrankP
 */
@Entity
public class FactType extends ParentElement implements Type, ITerm,
        ListModel<Role>, Publisher {

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.ALL)
    protected List<Role> roles;//*
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Population population;
    @Column
    private String name;
    @OneToOne(cascade = CascadeType.ALL)
    private DerivableConstraint derivableConstraint;
    @OneToOne(cascade = CascadeType.ALL)
    protected TypeExpression fte; //* if ot=null then fte != null
    @OneToOne(cascade = CascadeType.ALL)
    private ObjectType ot;//*
    @ManyToOne(cascade = CascadeType.PERSIST)
    private FactTypeClass factTypeClass;
    @Column
    private String plural;
    @Column
    private String documentation;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private final Set<ITerm> referencedTerms;
    @Transient
    private transient EventListenerList listenerList;
    @Transient
    private transient BasicPublisher publisher;
    private DefaultValueConstraint defaultValue;
    private List<Event> events = new ArrayList<Event>();

    public FactType() {
        this.referencedTerms = new TreeSet<>();
    }

    // in behalf of ElementsFactType
    protected FactType(String name, ObjectModel om, FactRequirement source) {
        super(om, source);
        this.referencedTerms = new TreeSet<>();
        this.population = new Population(this, 2);
        this.name = name;
        this.derivableConstraint = null;
        this.ot = null;
        this.factTypeClass = null;
        this.plural = null;
        this.documentation = null;

        initListeners();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initListeners();
    }

    /**
     * if isAnObjectType = true then creation of an objecttype name with OTE
     * based on constants otherwise creation of a facttype name with FTE based
     * on constants
     *
     * @param name
     * @param constants is a non empty list the size of constants = the number
     * of roles + 1; if size = 1 then facttype is a singleton objecttype and
     * isAnObjectType = true if size = 2 then facttype gets a single uniqueness
     * constraint
     * @param source the source of this fact type
     * @param objectModel the objectmodel where this facttype is registrated
     * @param types types.size() = constants.size() -1
     * @param roleNames types.size() = rolenames.size() and all non-empty
     * rolenames must be different
     */
    FactType(String name, List<String> constants,
            List<SubstitutionType> types, List<String> roleNames, boolean isAnObjectType,
            ObjectModel objectModel, FactRequirement source) {
        super(objectModel, source);
        this.referencedTerms = new TreeSet<>();
        initListeners();

        if (constants.isEmpty()) {
            throw new RuntimeException(("ARRAY IS EMPTY"));
        }
        if (constants.size() == 1 && isAnObjectType == false) {
            throw new RuntimeException(("FACTTYPE WITHOUT ROLES SHOULD BE AN OBJECTTYPE"));
        }
        if (constants.size() != types.size() + 1) {
            throw new RuntimeException(("EXPRESSION PARTS MUST BE EXACTLY ONE MORE THAN SUBSTITUTIONTYPES"));
        }
        if (roleNames.size() != types.size()) {
            throw new RuntimeException(("COUNT OF ROLENAMES MUST BE THE SAME AS ")
                    + ("COUNT OF SUBSTITUTIONTYPES"));
        }

        try {
            checkRoleNames(roleNames);
        } catch (DuplicateException exc) {
            throw new RuntimeException(exc.getMessage());
        }

        this.name = name;
        roles = new ArrayList<>(2);
        ObjectRole parentRole = null;
        int countParentCandidates = 0;
        for (int i = 0; i < types.size(); i++) {
            Role role;
            if (types.get(i) instanceof BaseType) {
                role = new BaseValueRole((BaseType) types.get(i), this);
            } else {
                ObjectType objectType = (ObjectType) types.get(i);
                if (types.get(i) instanceof ConstrainedBaseType) {
                    role = new CBTRole((ConstrainedBaseType) objectType, this);
                } else {
                    role = new ObjectRole(objectType, this);
                }

                if (objectType.isValueType()) {
                    role.setNavigable(false);
                    // mandatory constraint needed??
                } else {
                    parentRole = (ObjectRole) role;
                    countParentCandidates++;
                }
                //types.get(i).involvedIn(role);
            }
            role.setRoleName(roleNames.get(i));
            roles.add(role);
        }

        population = new Population(this, 4);

        derivableConstraint = null;

        RequirementModel rm = ((ObjectModel) getParent()).getProject().getRequirementModel();

        if (isAnObjectType) {

            fte = null;
            List<Integer> roleNumbers = new ArrayList<>(roles.size());
            for (int i = 0; i < roles.size(); i++) {
                roleNumbers.add(i);
            }
            createObjecttype(constants, roleNumbers);
            if (countParentCandidates == 1) {
                if (parentRole.isMultiple()) {
                    parentRole.setComposition(true);
                    parentRole.addAddable("objecttypes with 2 or more identifying"
                            + " roles, possess normally a role which offers the possibility of adding new components");
                    parentRole.addRemovable("objecttypes with 2 or more identifying "
                            + "roles, possess normally a role which offers the possibility of removing new components");

                    // default qualifier role?
                    if (roleNumbers.size() > 1) {
                        for (Role role : roles) {
                            if (role.getSubstitutionType().equals(BaseType.NATURAL)) {
                                role.setQualifier(parentRole);
                            }
                        }
                    }

                }
            }

        } else {
            fte = new TypeExpression(this, constants);
            ot = null;

            if (roles.size() == 1) {
                try {
                    String uniqueString = "Two (or more) facts about " + getFactTypeString() + " with the same value on "
                            + idString() + " are not allowed.";
                    RuleRequirement rule = rm.
                            addRuleRequirement(getCategory(),
                                    uniqueString,
                                    new SystemInput("redundancy or bad identification of facts is awkward"));
                    new UniquenessConstraint(roles.get(0), rule);
                    rule.getReviewState().setReviewImpact(Impact.ZERO);

                } catch (ChangeNotAllowedException ex) {
                    Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        factTypeClass = null;
    }

    /**
     * creation of an abstract or unidentified objecttype
     *
     * @param source
     * @param nameWithCapital
     */
    FactType(String nameWithCapital, boolean abstractObjectType,
            ObjectModel objectModel, Source source) {
        super(objectModel, source);
        this.referencedTerms = new TreeSet<>();
        initListeners();
        roles = new ArrayList<>(0);
        population = new Population(this, 0); // population always empty
        this.name = nameWithCapital;
        derivableConstraint = null;
        fte = null;
        if (abstractObjectType) {
            ot = new ObjectType(this, ObjectType.ABSTRACT_OBJECTTYPE);
            try {
                ot.setAbstract(true);
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ot = new UnidentifiedObjectType(this);

        }
        factTypeClass = null;

    }

    /**
     * creation of a singleton objecttype with given name, based on source; the
     * singleton is expressed by objectname
     *
     * @param typename
     * @param objectname is not empty
     * @param source
     */
    FactType(String typename, String objectname, ObjectModel om, FactRequirement source) {
        super(om, source);
        this.referencedTerms = new TreeSet<>();
        initListeners();
        if (objectname.isEmpty()) {
            throw new RuntimeException(("NAME OF OBJECT ")
                    + ("CANNOT BE EMPTY"));
        }
        roles = new ArrayList<>(0); // always without roles
        population = new Population(this, 0); // population always empty
        this.name = typename;
        derivableConstraint = null;
        fte = null;
        ot = new SingletonObjectType(this, objectname, source);
        factTypeClass = null;
    }

    /**
     * creation of a constrained BaseType with given typename and rolename,
     * based on source; this type stays always a concrete valuetype without
     * inheritance
     *
     * @param typeName
     * @param baseType
     * @param roleName
     * @param source
     */
    FactType(String typeName, BaseType baseType, ObjectModel om, FactRequirement source) {
        super(om, source);
        this.referencedTerms = new TreeSet<>();
        initListeners();
        this.name = Naming.withCapital(typeName);
        derivableConstraint = null;
        fte = null;

        roles = new ArrayList<>(1);
        Role role = new BaseValueRole(baseType, this);
        role.setRoleName(Naming.withoutCapital("value"));
        roles.add(role);

        String uniqueString = "Two or more values of " + getFactTypeString() + " are not allowed.";
        RuleRequirement defaultrule = om.getProject().getRequirementModel().
                addRuleRequirement(Category.SYSTEM, uniqueString,
                        new SystemInput("redundancy or bad identification of objects is awkward"));
        try {
            new UniquenessConstraint(role, defaultrule);
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
        }

        population = new Population(this, 4);

        ProjectRole projectRole = om.getProject().getCurrentUser();
        RuleRequirement valueRule = om.getProject().getRequirementModel().
                addRuleRequirement(getCategory(), "Value constraint in behalf of "
                        + name + ": values are still not defined",
                        new ExternalInput("", projectRole));

        ot = new ConstrainedBaseType(this, valueRule);
        factTypeClass = null;
    }

    FactType(String collectionTypeName, ObjectModel om, String begin, String separator, String end, FactRequirement source) {
        super(om, source);
        this.referencedTerms = new TreeSet<>();
        initListeners();
        this.name = Naming.withCapital(collectionTypeName);
        derivableConstraint = null;
        fte = null;

        roles = new ArrayList<>(1);
        Role role = new BaseValueRole(BaseType.STRING, this);
        role.setRoleName(Naming.withoutCapital("artificial_id"));
        roles.add(role);
        role.setHidden(true);

        population = new Population(this, 4);
        ot = new CollectionType(this, begin, separator, end);
        factTypeClass = null;

        try {
            String uniqueString = "Two (or more) facts about " + getFactTypeString() + " with the same collection values are not allowed.";
            RuleRequirement rule = om.getProject().getRequirementModel().
                    addRuleRequirement(source.getCategory(),
                            uniqueString,
                            new SystemInput("redundancy or bad identification of collections is awkward"));
            new UniquenessConstraint(roles.get(0), rule);
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addEvent(FactType condition, ObjectRole conditionCaller) {
        for (Event event : events) {
            if (event.getCondition().equals(condition) && event.getConditionCaller().equals(conditionCaller)) {
                return;
            }
        }
        events.add(new Event(this, condition, conditionCaller));
    }

    public void removeEvent(FactType condition, ObjectRole conditionCaller) {
        Event toRemove = null;
        for (Event event : events) {
            if (event.getCondition().equals(condition) && event.getConditionCaller().equals(conditionCaller)) {
                toRemove = event;
            }
        }
        if (toRemove != null) {
            events.remove(toRemove);
        }
    }
    
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public String getDefaultValue() {
        if (defaultValue == null) {
            return null;
        } else {
            return defaultValue.getValue();
        }
    }

    public void addDefaultValue(Boolean value) {
        if (isCandidateDefaultBoolean()) {
            if (defaultValue == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                RuleRequirement rule = rm.addRuleRequirement(getCategory(),
                        "",
                        new ExternalInput("", projectRole));
                this.defaultValue = new DefaultValueConstraint(this, value.toString(), rule);
            }
        }
    }

    void deleteDefaultValue() {
        if (defaultValue != null) {
            defaultValue = null;
            publisher.inform(this, "defaultValue", null, isMutable());
        }
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public boolean isCandidateDefaultBoolean() {
        if (roles.isEmpty()) {
            return false;
        }
        for (Role role : roles) {
            if (!role.isQualifier()) {
                return false;
            }
        }
        return true;

    }

    /**
     *
     * @return true if facts can be changed after creation, else false
     */
    public boolean isMutable() {
        return isSettable() || isAdjustable() || (ot != null && ot.isMutable());

    }

    public boolean isCandidateMutable() {
        return ot != null && ot.isCandidateMutable();
    }

    /**
     *
     * @return true if facts cannot be changed after creation, else false
     */
//    public boolean isImmutable() {
//        // return immutableConstraint != null || size() == 0;
//        return mutableConstraint == null || size() == 0;
//
//    }
    public boolean isParsable() {
        if (ot == null) {
            return fte.isParsable();
        } else {
            return ot.isParsable();
        }
    }

    /**
     *
     * @param bvr role must be part of this facttype
     * @param cbt bvr.getSubstitutionType() = cbt.getBaseType()
     */
    void replaceBaseType(BaseValueRole bvr, ConstrainedBaseType cbt) {
        if (bvr.getSubstitutionType() != cbt.getBaseType()) {
            throw new RuntimeException("role must have the same basetype as the"
                    + " constrained basetype");
        }
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i) == bvr) {
                List<UniquenessConstraint> ucs = bvr.ucs();
                CBTRole or = new CBTRole(cbt, this);
                for (UniquenessConstraint uc : ucs) {
                    uc.replaceRole(bvr, or);
                    try {
                        or.addConstraint(uc);
                    } catch (ChangeNotAllowedException ex) {
                        Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                or.qualified = bvr.qualified;
                roles.set(i, or);
                if (bvr.getDefaultValue() != null) {
                    try {
                        or.setDefaultValue(bvr.getDefaultValue());
                        bvr.removeDefaultValue();
                    } catch (MismatchException ex) {
                        Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ChangeNotAllowedException ex) {
                        Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                population.replaceTupleItems(i, or);
                or.setNavigable(false);
                // mandatory constraint necessary??
                publisher.inform(this, "rolesIterator", null, roles.iterator());
                return;
            }
        }

        throw new RuntimeException("role is not part of this facttype " + getName());

    }

    /**
     * substitutiontype of or will be replaced by ot
     *
     * @param or is a role of this facttype
     * @param ot is a supertype of or.getSubstitutionType()
     * @param source
     */
    public void generalizeRole(ObjectRole or, ObjectType ot, ModelElement source) {
        if (!or.getParent().equals(this)) {
            throw new RuntimeException(" role doesn't belong to facttype "
                    + getName());

        }
        SubstitutionType st = or.getSubstitutionType();
        if (!ot.isEqualOrSupertypeOf(st)) {
            throw new RuntimeException("replacing "
                    + st.getName() + " by "
                    + ot.getName() + " is impossible because "
                    + ot.getName() + " isn't a supertype of "
                    + st.getName());
        }
        addSource(source);
        or.generalize(ot);
        publisher.inform(this, "rolesIterator", null, roles.iterator());
        fireListChanged();
    }

    /**
     * substitutiontype of or is replaced by his subtype
     *
     * @param or or.getSubstitutionType() is abstract and possesses exactly one
     * subtype and or is a role of this facttype
     * @param source
     */
    public void specializeRole(ObjectRole or, Source source) {
        if (!or.getParent().equals(this)) {
            throw new RuntimeException(" role doesn't belong to facttype "
                    + getName());

        }
        ObjectType st = or.getSubstitutionType();
        if (!st.subtypes().hasNext()) {
            throw new RuntimeException(" actual substitutiontype of role doesn't "
                    + "possess a subtype");
        }
        st.subtypes().next();
        if (st.subtypes().hasNext()) {
            throw new RuntimeException(" actual substitutiontype of role does "
                    + "possess two or more subtypes");
        }
        addSource(source);
        or.specialize();
        publisher.inform(this, "rolesIterator", null, roles.iterator());
        fireListChanged();
    }

    private void createObjecttype(List<String> constants, List<Integer> roleNumbers) {

        ot = new ObjectType(this, constants, roleNumbers);
        try {

            for (Role role : roles) {
                if (role.containsUniqueness()) {
                    return;
                }
            }
            String uniqueString = "Two (or more) facts about " + getFactTypeString() + " with "
                    + uniqueString() + " are not allowed.";

            ObjectModel om = (ObjectModel) getParent();
            Project project = om.getProject();
            RuleRequirement rule = project.getRequirementModel().
                    addRuleRequirement(getCategory(), uniqueString,
                            new ExternalInput("redundancy or bad identification of objects is awkward",
                                    project.getCurrentUser()));
            new UniquenessConstraint(roles, rule);

        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    String uniqueString() {
        StringBuilder sb = new StringBuilder();
        if (roles.size() == 1) {
            sb.append("the same value on <");
        } else {
            sb.append("the same combination of values on <");
        }
        sb.append(roles.get(0).getNamePlusType());
        for (int i = 1; i < roles.size(); i++) {
            sb.append(",");
            sb.append(roles.get(i).getNamePlusType());
        }
        sb.append(">");
        return sb.toString();
    }

    final void initListeners() {
        listenerList = new EventListenerList();
        publisher = new BasicPublisher(new String[]{
            "typeExpression", "derivable", "name", "rolesIterator", "objectType"
        });
    }

    /**
     *
     * @return the name of this facttype
     */
    @Override
    public String getName() {
        return name;
    }

    public void addDerivableConstraint(RuleRequirement rule, String text) {
        if (derivableConstraint != null) {
            throw new RuntimeException("derivable constraint already exists");
        }

        derivableConstraint = new DerivableConstraint(this, rule, text);
        fireListChanged();
        publisher.inform(this, "derivable", null, derivableConstraint.getText());
    }

    public DerivableConstraint getDerivableConstraint() {
        return derivableConstraint;
    }

    void deleteDerivableConstraint() {
        if (derivableConstraint != null) {
            derivableConstraint = null;
            publisher.inform(this, "derivable", null, null);
        }
    }

    /**
     * facttype will be an objecttype with an OTE based on constants
     *
     * @param constants size of constants must be equal to number of roles +1;
     * @param roleNumbers the ranking of the roleNumbers conform the new OTE
     * @throws ChangeNotAllowedException facttype is already objectified
     */
    public void objectify(List<String> constants, List<Integer> roleNumbers)
            throws ChangeNotAllowedException {
        if (ot != null) {
            throw new ChangeNotAllowedException(("FACTTYPE IS ALREADY OBJECTIFIED"));
        }

        createObjecttype(constants, roleNumbers);
        publisher.inform(this, "objectType", null, ot);
        fireListChanged();

    }

    /**
     * facttype isn't objectified and will be objectified afterwards; constants
     * and rolenumbers are copied from FTE
     */
    public void objectify() {
        if (ot != null) {
            return;
        }
        System.out.println("objectify()");
        createObjecttype(fte.cloneConstants(), fte.cloneRoleNumbers());
        fireListChanged();
    }

    /**
     * postcondition: !isObjecttype()
     */
    public void deobjectify() throws ChangeNotAllowedException {
        if (ot.playsRoleRelations().isEmpty()) {
            deobjectifyWithoutException();
        } else {
            throw new ChangeNotAllowedException("fact type plays roles as an object type.");
        }
    }

    void deobjectifyWithoutException() {
        for (Role role : roles) {
            role.getSubstitutionType().resignFrom(role);
        }
        ot = null;

        fireListChanged();
        publisher.inform(this, "objectType", null, ot);
    }

    /**
     *
     * @return the facttype expression (could be unknown = null)
     */
    public TypeExpression getFTE() {
        return fte;
    }

    public TypeExpression getTE() {
        if (ot == null) {
            return fte;
        } else {
            return ot.getOTE();
        }
    }

    /**
     * removing of the FTE of this facttype
     *
     * @throws ChangeNotAllowedException if this facttype isn't an objecttype
     */
    public void removeFTE() throws ChangeNotAllowedException {
        if (ot != null) {
            throw new ChangeNotAllowedException(("CANNOT REMOVE FTE IF FACTYPE ")
                    + ("DOESN'T POSSESS AN OTE"));
        }
        fte = null;
        fireListChanged();
        publisher.inform(this, "fte", null, fte);

    }

    /**
     *
     * @param fn all including objectnodes are allready registered
     * @return the resulting tuple
     */
    public Tuple addFact(FactNode fn) throws MismatchException {
        List<Integer> roleNumbers;
        if (fn instanceof ObjectNode) {
            roleNumbers = getObjectType().getOTE().cloneRoleNumbers();
        } else {
            roleNumbers = fte.cloneRoleNumbers();
        }
        List<ValueNode> concreteNodes = fn.getConcreteNodes(roleNumbers);
        List<SubstitutionType> substitutionTypes = new ArrayList<>();
        for (ValueNode concreteNode : concreteNodes) {
            substitutionTypes.add((SubstitutionType) concreteNode.getDefinedType());
        }

        checkMatch(substitutionTypes, false);
        ArrayList<Value> substitutionValues = new ArrayList<>();
        int i = 0;
        for (ValueNode concreteNode : concreteNodes) {
            if (concreteNode instanceof ValueLeaf) {
                BaseType baseType = ((ValueLeaf) concreteNode).getDefinedType();
                substitutionValues.add(new BaseValue(concreteNode.getText(), baseType));
            } else {
                Tuple tuple = ((ObjectType) substitutionTypes.get(i)).getFactType().population.getTuple((ParentNode) concreteNode);
                if (tuple == null) {
                    throw new MismatchException(null, "tuple derived from "
                            + concreteNode.getText() + " doesn't exist");
                } else {
                    substitutionValues.add(tuple);
                }
            }
            i++;
        }

        FactRequirement source = fn.getExpressionTreeModel().getSource();
        addSource(source);
        return population.addTuple(substitutionValues, roles, substitutionTypes, source);
    }

    /**
     *
     * @param cn all including valuenodes are allready registered
     * @param sequence
     * @param rm
     * @param st
     * @return the resulting tuple
     * @throws equa.meta.MismatchException
     */
    public Tuple addCollectionFact(CollectionNode cn, boolean sequence, SubstitutionType st,
            RequirementModel rm) throws MismatchException {

        List<ValueNode> valueNodes = cn.getConcreteValueNodes();
        FactRequirement source = cn.getExpressionTreeModel().getSource();

        List<Value> values = new ArrayList<>();
        for (ValueNode valueNode : valueNodes) {
            SubstitutionType cst = (SubstitutionType) valueNode.getDefinedType();
            values.add(cst.parse(valueNode.getText(), true, source));
        }

        CollectionType ct = (CollectionType) getObjectType();
        CollectionTuple tuple = ct.addCollectionTuple(values, source);
        return tuple;
    }

    /**
     * removing of association of source with all registered facts at this
     * facttype; every tuple withhout any source will be removed; every facttype
     * with a changed empty population will be removed from om
     *
     * @param source
     * @param om the objectmodel where this facttype is registered
     * @throws equa.meta.ChangeNotAllowedException
     */
    public void removeAssociationsWithSource(Source source, ObjectModel om)
            throws ChangeNotAllowedException {
        if (om.getFactType(getName()) == null) {
            throw new RuntimeException(("FACTTYPE NOT KNOWN AT OBJECTMODEL"));
        }

        population.removeAssociationsWithSource(source, om);
        removeSource(source);
    }

    /**
     * this facttype, without FTE, gets a FTE based on constants; a call of this
     * method when there already exists a FTE will have no effect (in that case:
     * please use de setter of TypeExpression)
     *
     * @param constants size of constants must be equal to size of facttype plus
     * one
     * @param roleNumbers the ranks of the roles in the new FTE
     * @param source
     * @see TypeExpression
     */
    public void addFTE(List<String> constants, List<Integer> roleNumbers, Source source) {
        if (constants.size() != size() + 1) {
            throw new RuntimeException(("NUMBER OF CONSTANTS DOESN'T MATCH ")
                    + ("SIZE OF FACTTYPE"));
        }
        if (fte == null) {
            fte = new TypeExpression(this, constants, roleNumbers);
            addSource(source);
            publisher.inform(this, "fte", null, fte);
        }
    }

    /**
     * role will be replaced by a set of identifying roles of the
     * substitututiontype of this role; fte and ote will be replaced (if not
     * null); the populatyion of this facttype will be adjusted
     *
     * @param role
     * @throws ChangeNotAllowedException
     */
    public void deobjectifyRole(ObjectRole role)
            throws ChangeNotAllowedException {

        if (role.getSubstitutionType().getFactType().size() == 0) {
            throw new ChangeNotAllowedException("object type of role without identifying roles");
        }
        ObjectType otRole = role.getSubstitutionType();
        if (!otRole.getFactType().isElementary()) {
            throw new ChangeNotAllowedException("object type of role isn't elementary");
        }

        int index = roles.indexOf(role);
        if (index == -1) {
            throw new NullPointerException("wrong parent of role ");
        }

        // detect id-roles of otRole
        List<Role> idRoles = otRole.getFactType().idRoles();
        List<Role> idRolesNew = new ArrayList<>();
        // create new roles and remove the old one
        roles.remove(index);

        int i = index;
        for (Role r : idRoles) {
            Role idRole;

            if (r.getSubstitutionType() instanceof BaseType) {
                idRole = new BaseValueRole((BaseType) r.getSubstitutionType(), this);
            } else {
                idRole = new ObjectRole((ObjectType) r.getSubstitutionType(), this);
            }

            String roleName;
            if (otRole instanceof ConstrainedBaseType) {
                roleName = Naming.withoutCapital(otRole.getName());
            } else {
                roleName = r.getRoleName();
            }
            idRole.setRoleName(roleName);

            idRolesNew.add(idRole);
            roles.add(i, idRole);
            i++;
        }

        // reorganize uniqueness in behalf of the newly created roles
        for (UniquenessConstraint uc : role.ucs()) {
            uc.split(role, idRolesNew);
        }

        // reorganize mandatory constraint in case of an objecttype with one identifying role
        if (idRolesNew.size() == 1 && role.isMandatory() && !idRolesNew.get(0).getSubstitutionType().isValueType()) {
            new MandatoryConstraint(roles.get(index), (RuleRequirement) role.getMandatoryConstraint().sources().get(0));
        }

        // create a new empty population newPop based on the new structure after the deobjectifying 
// Population newPop = new Population(this, population.size());
        // adding tuples at ot; use every tuple of ot in behalf adjusting the old 
        // tuple in the current population; add this adjusted tuple in popNew
        Iterator<Tuple> itTuples = population.tuples();
        while (itTuples.hasNext()) {
            // next tuple has to be changed without calling the constructor of 
            // Tuple, because other tuples could refer to this tuple
            Tuple tuple = itTuples.next();
            tuple.deobjectifyItem(index, idRolesNew);
//  newPop.addTuple(tuple);       
        }

        // change to newPop
// population = newPop;
        role.remove();

        // deobjectfy fte and ote, if existing
        if (this.fte != null) {
            this.fte.substitute(index, otRole.getOTE());
        }

        if (this.ot != null) {
            this.ot.getOTE().substitute(index, otRole.getOTE());
        }

    }

    /**
     * if mapping[i] >= 0 then marked role i of this facttype correspondends to
     * role mapping[i] of OT; the marked roles will be deleted; a new merged
     * role will be put at the back end side of the roles of this facttype; the
     * population of this facttype will be adjusted; the structure of the type
     * expression(s) is adjusted; the constants are not adjusted
     *
     * @param mapping mapping.length = size of this facttype; all non-negative
     * numbers are different and element of the set of rolenumbers of OT all
     * marked roles undergo at least one uniqueness constraint
     * @param ot substitutiontypes of marked roles equals with mapped roles
     * within OT
     * @throws ChangeNotAllowedException
     */
    public ObjectRole mergeRoles(int[] mapping, ObjectType ot)
            throws ChangeNotAllowedException {

        /*
         * start of precondition check **************************************
         */
        if (mapping.length != size()) {
            throw new RuntimeException("length of mapping doesn't correspond with "
                    + "size of " + getName());
        }
        List<Role> mergingRoles = new ArrayList<>();
        List<Role> targetRoles = new ArrayList<>();
        FactType ft_of_ot = ot.getFactType();
        for (int i = 0; i < mapping.length; i++) {
            if (mapping[i] >= ft_of_ot.size()) {
                throw new RuntimeException("merging role doesn't exist at "
                        + ft_of_ot.getName());
            }
            if (mapping[i] >= 0) {
                Role targetRole = ft_of_ot.getRole(mapping[i]);
                if (targetRoles.contains(targetRole)) {
                    throw new RuntimeException("merging role coincides with "
                            + "other merging role within "
                            + ft_of_ot.getName());
                } else {
                    targetRoles.add(targetRole);
                    if (targetRole.getSubstitutionType().equals(roles.get(i).getSubstitutionType())) {
                        mergingRoles.add(roles.get(i));
                    } else {
                        throw new RuntimeException("merging role doesn't match "
                                + "with substitutiontype of role within "
                                + ft_of_ot.getName() + " "
                                + targetRole.getSubstitutionType() + " versus "
                                + roles.get(i).getSubstitutionType());
                    }
                }
            }
        }
        if (!ft_of_ot.isId(targetRoles)) {
            throw new RuntimeException("merging roles are not id of "
                    + ft_of_ot.getName());
        }
        List<UniquenessConstraint> commonUcs = commonUniqueness(mergingRoles);
        if (commonUcs.isEmpty()) {
            throw new RuntimeException("merging roles are not subject to a common"
                    + " uniqueness ");
        }

        // end of precondition check ***************************************
        int[] nrs = new int[mergingRoles.size()];
        int j = 0;
        for (Role role : mergingRoles) {
            nrs[j] = role.getNr();
            j++;
        }

        // create new role and remove the old ones
        ObjectRole newRole = new ObjectRole(ot, this);
        roles.add(newRole);
        for (Role role : mergingRoles) {
            roles.remove(role);
            role.remove();
        }

        // reorganize uniqueness in behalf of the newly created role
        // uc's which aren't common are thrown away!
        for (UniquenessConstraint uc : commonUcs) {
            uc.merge(mergingRoles, newRole);
        }

        // create a new empty population newPop based on the new structure after the merge
        Population newPop = new Population(this, population.size());

        // adding tuples at ot; use every tuple of ot in behalf adjusting the old 
        // tuple in the current population; add this adjusted tuple in popNew
        Iterator<Tuple> itTuples = population.tuples();
        while (itTuples.hasNext()) {
            // next tuple has to be changed without calling the constructor of 
            // Tuple, because other tuples could refer to this tuple
            Tuple tuple = itTuples.next();
            Value[] targetValues = new Value[ft_of_ot.size()];
            for (int i = 0; i < mapping.length; i++) {
                if (mapping[i] >= 0) {
                    targetValues[mapping[i]] = tuple.getItem(i).getValue();
                } else {
                }
            }

            // object types with multiple ids could cause undefined tuple items
            for (int i = 0; i < ft_of_ot.size(); i++) {
                if (targetValues[i] == null) {
                    targetValues[i] = new UnknownValue(ft_of_ot.getRole(i).getSubstitutionType());
                }
            }

            // new object tuple at the back end side:
            Tuple objectTuple = ft_of_ot.population.addTuple(Arrays.asList(targetValues), ft_of_ot.roles,
                    tuple.sources());
            tuple.merge(mapping, new TupleItem(objectTuple, newRole));
            newPop.addTuple(tuple);
        }

        // change to newPop
        population = newPop;

        // merge fte and ote, if existing
        if (this.fte != null) {
            this.fte.mergeRoles(nrs);
        }

        if (this.ot != null) {
            this.ot.getOTE().mergeRoles(nrs);
        }

        publisher.inform(this, "rolesIterator", null, roles.iterator());

        return newRole;
    }

    /**
     * parsing of substitutionvalue out of expression with the FTE of this
     * facttype If expression is not parsable null will be returned facttype
     * contains a FTE
     *
     * @param expression
     * @param source the source of the given expression
     * @return the substitutionvalue based on the given expression and the FTE
     * of this facttype
     */
    public Value parseWithFactTypeExpression(String expression, boolean otherOptions,
            FactRequirement source) throws MismatchException {
        if (fte == null) {
            throw new RuntimeException(("PARSING WITHOUT FTE IS IMPOSSIBLE"));
        }

        if (!fte.isParsable()) {
            throw new NotParsableException(fte, "FACTTYPE EXPRESSION OF "
                    + getName() + " IS NOT PARSABLE");
        }

        return parse(expression.trim(), fte, otherOptions, source);
    }

    /**
     * @param tuple
     * @return the complete expression based on tuple using the OTE of this
     * facttype, if existing, otherwise the FTE
     */
    public String makeExpression(Tuple tuple) {
        if (ot != null) {

            return tuple.getFactType().getObjectType().getOTE().makeExpression(tuple);
        } else {
            return Naming.withCapital(fte.makeExpression(tuple));
        }
    }

    Value parse(String trimmedExpression, TypeExpression te, boolean otherOptions,
            FactRequirement source) throws MismatchException {
        if (ot != null && ot.isAbstract()) {
            return new AbstractValue(this, source, ot, trimmedExpression);
        }

        ParseResult result = te.parse(trimmedExpression, otherOptions, source);
//        if (result.otherParsingPossible()) {
//            throw new NotParsableException("there are different parsings possible");
//        }

        List<Value> values = result.getValues();
        addSource(source);

        return population.addTuple(values, roles, source);
    }

    Value parse(String subValue, boolean parsingOptions, int position, FactRequirement source)
            throws MismatchException {
        SubstitutionType st = roles.get(position).getSubstitutionType();
        if (st instanceof ObjectType) {
            ObjectType objectType = (ObjectType) st;
            if (objectType.isAbstract()) {
                return new AbstractValue(this, source, objectType, subValue);
            }
        }
        Value value;

        try {
            value = st.parse(subValue, parsingOptions, source);
        } catch (MismatchException exc) {
            if (st instanceof ObjectType) {
                ObjectType objectType = (ObjectType) st;
                if (objectType.isSuperType()) {
                    return new AbstractValue(this, source, objectType, subValue);
                } else {
                    throw exc;
                }
            } else {
                throw exc;
            }
        }
        addSource(source);
        return value;
    }

    Value parse(List<String> expressionParts, TypeExpression te,
            FactRequirement source) throws MismatchException {
        List<Value> values = te.parse(expressionParts, source);
        addSource(source);
        return population.addTuple(values, roles, source);
    }

    /**
     *
     * @return the population with tuples of this facttype
     */
    public Population getPopulation() {
        return population;
    }

    int searchRoleNr(Role r) {
        int nr = 0;
        for (Role role : roles) {
            if (role == r) {
                return nr;
            }
            nr++;
        }
        return -1;
    }

    /**
     *
     * @param index 0 <= index < size() @ return the role with index
     */
    public Role getRole(int index) {
        return roles.get(index);
    }

    /**
     *
     * @return an iterator over the roles of this facttype
     */
    public Iterator<Role> roles() {
        return roles.iterator();
    }

    public boolean uses(ObjectType ot) {

        for (Role role : roles) {
            if (role.getSubstitutionType().equals(ot)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @return the count of the roles of this facttype
     */
    public int size() {
        return roles.size();
    }

    /**
     * the role name of role is set to name
     *
     * @param name new name with respect to role (removing of role name is
     * allowed; in that case: make use of empty string)
     * @param role
     * @throws DuplicateNameException if there is another role of this facttype
     * with the same name
     */
    public void setRoleName(String name, Role role) throws DuplicateException {

        String correctedName;
        if (name.isEmpty()) {
            correctedName = Naming.withoutCapital(role.getSubstitutionType().getName());
        } else {
            correctedName = name;
        }
        for (Role r : roles) {
            if (r != role && r.detectRoleName().equals(correctedName)) {
                throw new DuplicateException(("TWO ROLES WITH THE SAME NAME ")
                        + ("ARE NOT ALLOWED"));
            }
        }

        role.setRoleName(name);
    }

    /**
     * setting of rolenames of parent-facttype
     *
     * @param roleNames size must be the same as size of parent
     */
    public void setRoleNames(List<String> roleNames) throws DuplicateException {
        checkRoleNames(roleNames);
        for (int i = 0; i < this.size(); i++) {
            this.getRole(i).setRoleName(roleNames.get(i));
        }
    }

    public static void checkRoleNames(List<String> roleNames) throws DuplicateException {
        ArrayList<String> names = new ArrayList<>();
        for (String roleName : roleNames) {
            if (roleName != null && !roleName.isEmpty()) {
                String roleNameToLower = roleName.toLowerCase();
                if (names.contains(roleNameToLower)) {
                    throw new DuplicateException(("ROLENAMES MUST BE DIFFERENT"));
                } else {
                    names.add(roleNameToLower);
                }
            }

        }
    }

    @Override
    public int compareTo(Type o) {
        return this.getName().compareTo(o.getName());
    }

    void rename(String nameWithCapital) {
        name = nameWithCapital;
        publisher.inform(this, "name", null, name);
    }

    /**
     *
     * @return the name followed by the OTE and if not available, the FTE of
     * this facttype in String-format
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        if (ot == null) {
            text.append("FT ");
        } else {
            if (ot.isAbstract()) {
                text.append("AT ");
            } else if (ot.isValueType()) {
                text.append("VT ");
            } else {
                text.append("OT ");
            }
        }
        if (getDerivableText() != null) {
            text.append("/ ");
        } else {
            text.append("  ");
        }

        text.append(name).append(" : ").append("\"").append(getTypeExpression()).append("\"");

        return text.toString();
    }

    public String getKind() {
        if (ot == null) {
            return "FT";
        } else {
            return ot.getKind();
        }
    }

    public String inheritsString() {
        StringBuilder text = new StringBuilder();
        if (ot != null) {
            Iterator<ObjectType> itSuper = ot.supertypes();
            if (itSuper.hasNext()) {
                text.append(itSuper.next().getName());
                while (itSuper.hasNext()) {
                    text.append(", ").append(itSuper.next().getName());
                }
            }
            Iterator<Interface> itInterface = ot.interfaces();
            if (itInterface.hasNext()) {
                if (text.length() == 0) {
                    text.append(itInterface.next().getName());
                }
                while (itInterface.hasNext()) {
                    text.append(", ").append(itInterface.next().getName());
                }
            }
        }
        return text.toString();
    }

    public String constraintString() {
        StringBuilder text = new StringBuilder();

        if (getDerivableText() != null) {
            text.append("der ");
        }

//        if (isMutable()) {
//            text.append("mut ");
//        }
        if (isConstrainedBaseType()) {
            ConstrainedBaseType cbt = (ConstrainedBaseType) getObjectType();
            String values = cbt.getValueConstraint().valuesString();
//            if (values.length() > 8) {
//                values = cbt.getValueConstraint().getAbbreviationCode();
//            }
            text.append(values).append(" ");
        }

        if (defaultValue != null) {
            text.append(defaultValue.getValue());
        }

        if (ot != null) {
            if (ot.isMutable()) {
                text.append(" mut");
            }
        }

        return text.toString();
    }

    /**
     *
     * @return the OTE and if not available, the FTE of this facttype in
     * String-format
     */
    public String getTypeExpression() {
        if (isObjectType()) {
            return ot.getOTE().toString();
        } else {
            return fte.toString();
        }
    }

    /**
     *
     * @return in general:</br> true if the smallest uniqueness constraint
     * covers at least (count of roles -1) non-qualifying roles, else false
     * </br> in case of an objecttype:</br> also all roles must be covered by
     * any uniqueness constraint
     *
     */
    public boolean isElementary() {
        if (roles.isEmpty() || (ot != null && ot.isValueType())) {
            return true;
        }

        int count_of_qualifying_roles = 0;
        int smallest = roles.size();
        boolean allCovered = true;
        boolean noUc = true;
        for (Role role : roles) {
            if (role.isQualifier()) {
                count_of_qualifying_roles++;
                if (!role.containsUniqueness()) {
                    allCovered = false;
                }
            } else {
                if (role.containsUniqueness()) {
                    noUc = false;
                } else {
                    allCovered = false;
                }
                int sizeUC = role.sizeOfSmallestUC();
                if (sizeUC < smallest) {
                    smallest = sizeUC;
                }
            }
        }
        // every facttype must have at least one uniqueness constraint
        if (noUc) {
            return false;
        }

        // (n-1) rule:
        if (smallest < roles.size() - count_of_qualifying_roles - 1) {
            return false;
        } else if (ot == null) {
            return true;
        } else { // an objecttype with an uncovered role is not elementary
            return allCovered;
        }
    }

    /**
     *
     * @return true if this facttype behaves like a class, otherwise false
     */
    public boolean isClass() {
        return ot != null || factTypeClass != null;
    }

    /**
     * isElementary() check if this facttype is a class (see return spec); if
     * !isObjectType() and this facttype satisfies the rules of a class, a
     * artificial objecttype with an artificial objecttype-expression similar to
     * the present facttype expression will be generated
     *
     * @return true if the facttype is elementary and in case of a facttype:
     * more than two non qualifying roles, else false
     */
    public boolean setFactTypeClassIfNeeded() {

        if (ot != null) {
            factTypeClass = null;
            return isElementary();
        }
        if (roles.size() <= 2) {
            return false;
        }

        int nonQualifyingRoles = 0;
        for (Role role : roles) {
            if (!role.isQualifier()) {
                nonQualifyingRoles++;
            }
        }

        // only elementary facttypes with 3 or more non-qualifying roles
        // are suitable as class
        if (nonQualifyingRoles > 2) {
            factTypeClass = new FactTypeClass(this);
            return true;
        } else {
            factTypeClass = null;
            return false;
        }
    }

    /**
     * checkIfClass() for this facttype
     *
     * @return if checkIsClass() all relations (c.q. roles where information
     * about the relation can be called up) which each refers to one attribute
     * or outgoing navigable association based on this fact type
     */
    public List<Relation> relations() {

        List<Relation> list = identifyingRelations();

        if (ot != null) {
            // this facttype plays roles; list gets expanded if possible
            List<Relation> playingRoles = ot.playsRoleRelations();
            list.addAll(playingRoles);
        }

        return list;

    }

    /**
     *
     * @return a list with all identifying relations of this fact type
     */
    public List<Relation> identifyingRelations() {
        Set<Relation> set = new HashSet<>();
        if (ot != null) {
            Iterator<ObjectType> it = ot.supertypes();
            while (it.hasNext()) {
                ObjectType superType = it.next();
                set.addAll(superType.identifyingRelations());
            }
        }
        if (set.isEmpty()) {
            //roles of fact type which are not inherited
            if (isCollectionType()) {
                CollectionType ct = (CollectionType) getObjectType();
                set.add(new CollectionIdRelation(ct, ct.getElementRole()));
            } else {
                for (Role role : roles) {
                    List<Role> qualifiers = detectQualifiers(role);
                    if (qualifiers.isEmpty()) {
                        set.add(new IdRelation(getObjectType(), role));
                    } else {
                        set.add(new QualifiedIdRelation(getObjectType(), role, qualifiers));
                    }

                }
            }
        }
        return new ArrayList<>(set);
    }

    public List<Role> detectQualifiers(Role role) {
        ArrayList<Role> qualifiers = new ArrayList<>();
        for (Role candidateQualifier : roles) {
            if (candidateQualifier.qualified == role) {
                qualifiers.add(candidateQualifier);
            }
        }
        return qualifiers;
    }

    /**
     *
     * @return true if this facttype behaves like an objecttype otherwise false
     */
    public boolean isObjectType() {
        return ot != null;
    }

    void disconnectRelations() throws ChangeNotAllowedException {

        for (Role role : roles) {
            role.disconnect();
        }

        if (ot != null) {
            try {
                ot.checkActivity();
            } catch (ChangeNotAllowedException exc) {
                for (Role role : roles) {
                    role.reconnect();
                }
                throw exc;
            }
        }
        if (isObjectType()) {
            Iterator<ObjectType> it = ot.supertypes();
            while (it.hasNext()) {
                it.next().removeSubType(ot);
            }
        }

    }

    /**
     *
     * @return the corresponding objectttype if existing otherwise null
     */
    public ObjectType getObjectType() {
        if (ot != null) {
            return ot;
        } else {
            return factTypeClass;
        }
    }

    void checkMatch(List<SubstitutionType> types, boolean exactTypeMatch)
            throws MismatchException {

        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            if (exactTypeMatch) {
                if (!role.getSubstitutionType().equals(types.get(i))) {
                    throw new MismatchException(getTE(), "TYPE " + i
                            + " (" + role.getSubstitutionType().getName()
                            + "," + types.get(i).getName() + ")"
                            + " DOESN'T MATCH EXACTLY.");
                }
            } else {

                if (!role.getSubstitutionType().isEqualOrSupertypeOf(types.get(i))) {

                    if (types.get(i) instanceof ObjectType && role.getSubstitutionType() instanceof ObjectType) {
                        ObjectType otDefined = (ObjectType) role.getSubstitutionType();
                        ObjectType otConcrete = (ObjectType) types.get(i);
                        ObjectType common;
                        if (otConcrete.isEqualOrSupertypeOf(otDefined)) {
                            common = otConcrete;
                        } else {
                            common = otDefined.detectCommonSupertype(otConcrete);
                        }
                        if (common != null) {
                            role.expandSubstitutionType(common);
                        } else {
                            try {
                                otConcrete.addSuperType(otDefined);
                            } catch (ChangeNotAllowedException ex) {
                                Logger.getLogger(FactType.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    } else {

                        throw new MismatchException(getTE(), "type " + i
                                + " (" + role.getSubstitutionType().getName()
                                + "," + types.get(i).getName() + ")"
                                + " doesn't match.");
                    }
                }
            }
        }
    }

    void checkMatch(List<SubstitutionType> types, List<String> roleNames,
            boolean exactTypeMatch) throws MismatchException {

        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            if (exactTypeMatch) {
                if (!role.getSubstitutionType().equals(types.get(i))) {
                    throw new MismatchException(getTE(), "TYPE " + i + (" DOESN'T MATCH."));
                }
            } else if (!role.getSubstitutionType().isEqualOrSupertypeOf(types.get(i))) {
                throw new MismatchException(getTE(), ("TYPE ") + i + (" DOESN'T MATCH."));
            }
            if (!role.getRoleName().equalsIgnoreCase(roleNames.get(i))) {
                throw new MismatchException(getTE(), ("ROLENAME ") + i + role.getRoleName()
                        + (" DOESN'T MATCH WITH ") + roleNames.get(i));
            }
        }
    }

    public boolean hasAbstractRoles() {
        for (Role role : roles) {
            if (role.getSubstitutionType().hasAbstractRoles()) {
                return true;
            }
        }
        return false;
    }

    public List<Role> qualifiersOf(Role qualified) {
        ArrayList<Role> qualifiers = new ArrayList<>();
        for (Role role : roles) {
            if (role.isQualifier() && role.getQualified().equals(qualified)) {
                qualifiers.add(role);
            }
        }
        return qualifiers;
    }

    @Override
    public int getSize() {
        return size();
    }

    @Override
    public Role getElementAt(int index) {
        Iterator<Role> it = roles();

        int i = 0;
        while (i < index && it.hasNext()) {
            i++;
            it.next();
        }
        if (i == index && it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public void fireListChanged() {
        EventListener[] listeners = listenerList.getListeners(ListDataListener.class);

        for (EventListener l : listeners) {
            ((ListDataListener) l).contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
        }
    }

    void correctQualifyingRoles(Role qualified) {
        for (Role role : roles) {
            if (role.getQualified() == qualified) {
                role.setQualifier(null);
            }
        }
    }

    /**
     *
     * @param role is a non-qualifier-role
     * @return the other non-qualifier role of this facttype, if possible else
     * null will be returned
     */
    public Role counterpart(Role role) {
        if (role.isQualifier() || role.getParent().isObjectType()) {
            return null;
        }
        Role counterpart = null;
        for (Role r : roles) {
            if (!r.isQualifier() && r != role) {
                if (counterpart != null) {
                    return null;
                } else {
                    counterpart = r;
                }
            }
        }
        return counterpart;
    }

    /**
     *
     * @param roles
     * @return true if roles includes id of this facttype, else false
     */
    boolean isId(List<Role> roles) {
        if (isValueType()) {

            return roles.size() == this.roles.size();

        }
        Set<UniquenessConstraint> ucs = ucs();
        for (UniquenessConstraint uc : ucs) {
            Iterator<Role> ucRoles = uc.roles();
            boolean includes = true;
            while (includes && ucRoles.hasNext()) {
                if (!roles.contains(ucRoles.next())) {
                    includes = false;
                }
            }
            if (includes == true) {
                return true;
            }
        }
        return false;
    }

    public Set<UniquenessConstraint> ucs() {
        Set<UniquenessConstraint> ucs = new HashSet<>();
        for (Role role : roles) {
            ucs.addAll(role.ucs());
        }
        return ucs;
    }

    /**
     *
     * @return the description of the derivability of this facttype; if this
     * facttype isn't derivable null will be returned
     */
    public String getDerivableText() {
        if (derivableConstraint == null) {
            return null;
        }
        return derivableConstraint.getText();
    }

    /**
     *
     * @return true if facts of this facttype could be derived from other facts,
     * otherwise false
     */
    public boolean isDerivable() {
        if (derivableConstraint != null) {
            return true;
        } else {
            if (ot != null) {
                Iterator<ObjectType> itSupertypes = ot.supertypes();
                while (itSupertypes.hasNext()) {
                    if (itSupertypes.next().getFactType().isDerivable()) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    /**
     *
     * @return true if another role of this facttype with common uniqueness with
     * role is modifiable
     */
    public boolean isIndirectModifiable(Role role) {
        List<Role> roles1 = new ArrayList<>();
        roles1.add(role);
        for (Role otherRole : roles) {

            if (otherRole != role && otherRole.hasCommonUniquenessWith(roles1)
                    && otherRole.isSettable()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param role
     * @return true if this facttype contaibs another role with the same
     * substitutiontype, otherwise false
     */
    public boolean hasRoleWithSameType(Role role) {
        for (Role otherRole : roles) {
            if (otherRole != role && otherRole.getSubstitutionType() == role.getSubstitutionType()) {
                return true;
            }
        }
        return false;
    }

    /**
     * duplication of FTE and OTE
     */
    public void duplicateTE() {
        if (fte == null) {

            fte = new TypeExpression(ot.getOTE());

        } else if (ot != null) {
            ot.setOTE(new TypeExpression(fte));
        }
    }

    private static List<UniquenessConstraint> commonUniqueness(List<Role> ftRoles) {
        List<UniquenessConstraint> ucs = ftRoles.get(0).ucs();
        for (UniquenessConstraint uc : ftRoles.get(0).ucs()) {
            boolean common = true;
            for (Role role : ftRoles) {
                if (!role.contains(uc)) {
                    common = false;
                }
            }
            if (common) {
                if (!ucs.contains(uc)) {
                    ucs.add(uc);
                }
            }
        }
        return ucs;
    }

    boolean hasCompositionRole() {
        for (Role role : roles) {
            if (role.isNavigable() && role.isComposition()) {
                return true;
            }
        }
        return false;
    }

    Role getResponsibleRole() {
        for (Role role : roles) {
            if (role.isResponsible()) {
                return role;
            }
        }
        return null;
    }
    
    ObjectRole getNavigableRole() {
        ObjectRole navigable = null;
        for (Role role : roles) {
            if (role.isNavigable()) {
                if (navigable != null) {
                    return null;
                } else {
                    navigable = (ObjectRole) role;
                }
            }
        }
        return navigable;
    }

    /**
     *
     * @param accessibles
     * @return if this fact type is accessible through other objecttype which
     * isn't a subordinate to this object type
     */
    boolean isAccessible() {
        /*
         if an accessible or candidate plays a navigable identifying role then
         this objecttype is indirect accessible
         */
        for (Role role : roles) {
            if (role.isNavigable()) {
                ObjectType st = (ObjectType) role.getSubstitutionType();
                Set<ObjectType> responsibles = st.getResponsibles();
                if (responsibles.isEmpty() || !responsibles.contains(this)) {
                    return true;
                }
            }
        }

        return ot.isAccessible();
    }

    @Override
    public void addListener(PropertyListener listener, String property) {
        publisher.addListener(listener, property);
    }

    @Override
    public void removeListener(PropertyListener listener, String property) {
        publisher.removeListener(listener, property);
    }

    /**
     *
     * @return true if this objecttyep behaves lik a basetype with supplementary
     * valueconstraints, otherwise false
     */
    public boolean isConstrainedBaseType() {
        return ot != null && ot instanceof ConstrainedBaseType;
    }

    boolean hasSuperType() {
        return ot != null && ot.supertypes().hasNext();
    }

    boolean isAddable() {
        for (Role role : roles) {
            if (role.isAddable()) {
                return true;
            }
        }
        if (ot != null) {
            return ot.relatedToAddableRole();
        }
        return false;
    }

    boolean isRemovable() {
        for (Role role : roles) {
            if (role.isRemovable()) {
                return true;
            }
        }
        if (ot != null) {
            return ot.relatedToRemovableRole();
        }
        return false;
    }

    boolean isSettable() {
        for (Role role : roles) {
            if (role.isSettable()) {
                return true;
            }
        }
        if (ot != null) {
            return ot.relatedToSettableRole();
        }
        return false;
    }

    boolean isAdjustable() {

        for (Role role : roles) {
            if (role.isAdjustable()) {
                return true;
            }
        }
        if (ot != null) {
            return ot.relatedToAdjustableRole();
        }

        return false;
    }

    boolean isInsertable() {
        for (Role role : roles) {
            if (role.isInsertable()) {
                return true;
            }
        }
        if (ot != null) {
            return ot.relatedToInsertableRole();
        }
        return false;
    }

    boolean hasToManyResponsibleRoles() {
        int responsibleRoles = 0;
        for (Role role : roles) {
            if (role.isResponsible()) {
                responsibleRoles++;
            }
        }

        return responsibleRoles > 1;
    }

    boolean hasNavigableRoles() {
        int navigableRoles = 0;
        for (Role role : roles) {
            if (role.isNavigable()) {
                navigableRoles++;
            }
        }

        return navigableRoles > 0;
    }

    public boolean hasAutoIncr() {
        for (Role role : roles) {
            if (role.isAutoIncr()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return true if this fact type has a role which could be responsible with
     * respect to the adding, removing or inserting of the concerning facts
     * otherwise false
     */
    public boolean isMissingResponsibleRole() {
        if (isDerivable()) {
            return false;
        }
        boolean result = false;
        for (Role role : roles) {
            if (role.isResponsible()
                    || (role.isNavigable() && role.isMandatory())) {
                return false;
            } else if (role.isNavigable()) {
                if (role.isMultiple()) {
                    result = true;
                }
            } else {
                if (role.isAutoIncr()) {
                    return false;
                }
            }
        }
        return result;
    }

    @Override
    public String getPlural() {
        if (ot == null || plural == null || plural.isEmpty()) {
            if (name.endsWith("y")) {
                return name.substring(0, name.length() - 1) + "ies";
            } else {
                return name + "s";
            }
        } else {
            return plural;
        }
    }

    @Override
    public void setPlural(String plural) {
        this.plural = plural;
    }

    @Override
    public Iterator<String> getSynonyms() {
        return new ArrayList<String>().iterator();
    }

    @Override
    public void addSynonym(String synonym) {
    }

    @Override
    public void removeSynonym(String synonym) {
    }

    @Override
    public String getDocumentation() {
        return documentation;
    }

    @Override
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public Iterator<ITerm> getReferenceTerms() {
        return referencedTerms.iterator();
    }

    @Override
    public void addReferenceTerm(ITerm term) {
        referencedTerms.add(term);
    }

    @Override
    public void removeReferenceTerm(ITerm term) {
        referencedTerms.remove(term);
    }

    @Override
    public void remove() {

        if (ot != null) {
            try {
                ot.checkActivity();
                ot.remove();
            } catch (ChangeNotAllowedException exc) {
                return;
            }
        } else if (factTypeClass != null) {
            factTypeClass.remove();
        }

        removeYourself();

    }

    public void removeYourself() {
        try {
            disconnectRelations();
            if (derivableConstraint != null) {
                derivableConstraint.remove();
//                for (Source source : derivableConstraint.sources()) {
//                    if (source instanceof Requirement) {
//                        ((Requirement) source).remove();
//                    }
//                }
            }
            if (defaultValue != null) {
                defaultValue.remove();
//                for (Source source : defaultValue.sources()) {
//                    if (source instanceof Requirement) {
//                        ((Requirement) source).remove();
//                    }
//                }
            }

            population.remove();
            for (Role role : new ArrayList<Role>(roles)) {
                role.remove();
            }
            roles.clear();
            getParent().remove(this);
            super.remove();

        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(FactType.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param st
     * @return how many times is st used as substitutiontype of one of the roles
     * of this facttype
     */
    private int usageOfSubstitutionType(SubstitutionType st) {
        int count = 0;
        for (Role role : roles) {
            if (role.getSubstitutionType().equals(st)) {
                count++;
            }
        }
        return count;
    }

    boolean looksLike(List<SubstitutionType> selected_types) {
        for (SubstitutionType st : selected_types) {
            if (usageOfSubstitutionType(st) != count(selected_types, st)) {
                return false;
            }
        }
        return true;
    }

    private static int count(List<SubstitutionType> selected_types, SubstitutionType st) {
        int c = 0;
        for (SubstitutionType t : selected_types) {
            if (t.equals(st)) {
                c++;
            }
        }
        return c;
    }

    /**
     *
     * @return true if every or no role of facttype contains a uniqueness
     * constraint otherwise false
     */
    public boolean isObjectifiable() {

        for (Role role : roles) {
            if (role.ucs().isEmpty()) {
                return ucs().isEmpty();
            }
        }
        return true;
    }

    /**
     *
     * @return true if !isObjectifiable() and count of non qualifier roles is
     * more than 2, otherwise false
     */
    boolean isSuspiciousCandidateClass() {
        int countNonQualifyingRoles = 0;
        boolean objectifiable = true;
        for (Role role : roles) {
            if (role.ucs().isEmpty()) {
                objectifiable = false;
            }
            if (!role.isQualifier()) {
                countNonQualifyingRoles++;
            }
        }
        return !objectifiable && countNonQualifyingRoles > 2;
    }

    @Override
    public void remove(ModelElement member) {
        if (member instanceof DerivableConstraint) {
            deleteDerivableConstraint();
        } else if (member instanceof DefaultValueConstraint) {
            deleteDefaultValue();
        } else if (member instanceof Role) {
            roles.remove(member);
        }
    }

    public final String getFactTypeString() {
        if (fte == null) {
            return "<" + name + ">";
        } else {
            return "\"" + fte.toString() + "\"";
        }
    }

    /**
     *
     * @return the objecttype who controls facts of this facttype during the
     * whole lifetime ; if this composition-objecttype doesn't exist, null will
     * be returned
     */
    ObjectRole creationalRole() {
        for (Role role : roles) {
            if (role.isCreational()) {
                return (ObjectRole) role;
            }
        }
        return null;
    }

    /**
     * constraints of otherRoles which don't refer to outside roles are added to
     * the corresponding roles of this fact type
     *
     * @param otherRoles substitution types of roles are the same as those of
     * roles of this fact type
     *
     */
    public void injectLocalConstraints(List<Role> otherRoles) throws ChangeNotAllowedException {

        for (int i = 0; i < otherRoles.size(); i++) {
            Role otherRole = otherRoles.get(i);
            Role thisRole = this.roles.get(i);
            thisRole.injectConstraints(otherRole);
        }
    }

    public boolean isGenerated() {
        return name.startsWith("_");
    }

    private List<Role> idRoles() {
        if (isConstrainedBaseType()) {
            return new ArrayList<Role>(roles);
        }
        List<Role> idRoles = new ArrayList<>();
        Role idRole = roles.get(0);
        UniquenessConstraint uc = idRole.ucs().get(0);
        Iterator<Role> itRoles = uc.roles();
        while (itRoles.hasNext()) {
            Role role = itRoles.next();
            idRoles.add(role);
        }
        return idRoles;
    }

    public void setFTE(TypeExpression oTE) {
        fte = oTE;
    }

    public boolean isEnum() {
        if (isObjectType()) {
            if (ot instanceof ConstrainedBaseType) {
                return ((ConstrainedBaseType) ot).getValueConstraint().isEnum();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isValueType() {
        return ot != null && ot.isValueType();
    }

    public boolean isSingleton() {
        return (ot != null && ot.isSingleton());
    }

    void refreshFactRequirements() throws ChangeNotAllowedException {
        if (isObjectType()) {
            ot.refreshFactRequirements();
        }
    }

    protected String idString() {
        StringBuilder id = new StringBuilder(roles.get(0).detectRoleName());
        for (int i = 1; i < roles.size(); i++) {
            id.append(",").append(roles.get(i).detectRoleName());
        }
        if (roles.size() > 1) {
            return "(" + id.toString() + ")";
        } else {
            return id.toString();
        }
    }

    boolean checkIsQualified(Role r) {
        for (Role role : roles) {
            if (role.qualified == r) {
                return true;
            }
        }
        return false;

    }

    public boolean isSeqRole(Role r) {
        for (Role role : roles) {
            if (role.isSeqNr() && role.qualified == r) {
                return true;
            }
        }
        return false;
    }

    public Category getCategory() {
        FactRequirement fact = getFirstFactRequirement();
        if (fact == null) {
            return null;
        }
        Category cat = getFirstFactRequirement().getCategory();
        if (cat == null) {
            return ((ObjectModel) getParent()).getProject().getDefaultCategory();
        } else {
            return cat;
        }

    }

    public FactRequirement getFirstFactRequirement() {
        for (Source source : sources()) {
            if (source instanceof FactRequirement) {
                return ((FactRequirement) source);
            }
        }
        if (ot != null) {
            Iterator<ObjectType> it = ot.subtypes();
            while (it.hasNext()) {
                FactRequirement fr = it.next().getFactType().getFirstFactRequirement();
                if (fr != null) {
                    return fr;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof FactType) {
            return name.equals(((FactType) object).name);
        } else {
            return false;
        }
    }

    @Override
    public boolean isPureFactType() {
        return fte != null;
    }

    public boolean isCollectionType() {
        return ot != null && ot instanceof CollectionType;
    }

    boolean creatableElementsIn(Set<ObjectType> todo) {
        for (Relation relation : relations()) {
            if (relation.isResponsible() && relation.isMandatory() && todo.contains(relation.targetType())) {
                return true;
            }
        }
        return false;
    }

    private void fillOTEConstants(List<String> constants) {
        if (ot.isAbstract()) {
            return;
        }
        TypeExpression ote = getObjectType().getOTE();
        String constant = ote.constant(0);
        if (!constant.isEmpty()) {
            constants.add(constant);
        }
        for (int i = 0; i < size(); i++) {
            SubstitutionType st = roles.get(ote.getRoleNumber(i)).getSubstitutionType();
            if (st instanceof ObjectType) {
                ObjectType objectType = (ObjectType) st;
                objectType.getFactType().fillOTEConstants(constants);
            }
            constant = ote.constant(i + 1);
            if (!constant.isEmpty()) {
                constants.add(constant);
            }

        }
    }

    private void fillFTEConstants(List<String> constants) {
        if (fte == null) {
            return;
        }

        String constant = fte.constant(0);
        if (!constant.isEmpty()) {
            constants.add(constant);
        }
        for (int i = 0; i < size(); i++) {
            SubstitutionType st = roles.get(fte.getRoleNumber(i)).getSubstitutionType();
            if (st instanceof ObjectType) {
                ObjectType objectType = (ObjectType) st;
                objectType.getFactType().fillOTEConstants(constants);
            }
            constant = fte.constant(i + 1);
            if (!constant.isEmpty()) {
                constants.add(constant);
            }
        }
    }

    public int matchTypeExpression(String expression) {
        expression = expression.toLowerCase();
        List<String> constants = new ArrayList<>();
        if (ot == null) {
            fillFTEConstants(constants);
        } else {
            fillOTEConstants(constants);
        }
        if (!constants.isEmpty()) {
            int index;
            String constant = constants.get(0).toLowerCase();
            index = expression.indexOf(constant);
            if (index == -1) {
                return -1;
            }

            for (int i = 1; i < constants.size(); i++) {
                constant = constants.get(i).toLowerCase();
                index = expression.indexOf(constant, index + constants.get(i - 1).length());
                if (index == -1) {
                    return -1;
                }
            }
            return constants.size();
        } else {
            return -1;
        }
    }

    boolean hasOptionalImmutableRoles() {
        for (Role role : roles) {
            if (role.isNavigable() && !role.isMandatory()) {
                Role responsibleRole = getResponsibleRole();
                if (responsibleRole == null) {
                    if (!withMandatorialRoles()) {
                        return true;
                    }
                } else if (!responsibleRole.isSettable() && !responsibleRole.isAddable()
                        && !responsibleRole.isInsertable() && !withMandatorialRoles()) {
                    Role counterpart = counterpart(responsibleRole);
                    return counterpart == null
                            || (!responsibleRole.isAdjustable() || !counterpart.hasDefaultValue());
                }
            }
        }
        return false;
    }

    boolean withMandatorialRoles() {
        for (Role role : roles) {
            if (role.isMandatory()) {
                return true;
            }
        }
        return false;
    }

    boolean withMoreMandarialConstraints() {
        int c = 0;
        for (Role role : roles) {
            if (role.isMandatory()) {
                c++;
            }
        }
        return c > 1;
    }

    boolean withMoreUniquenessConstraints() {
        Set<UniquenessConstraint> ucs = new HashSet<>();
        for (Role role : roles) {
            ucs.addAll(role.ucs());
        }
        return ucs.size() > 1;
    }

    int nonQualifierSize() {
        int size = roles.size();
        for (Role role : roles) {
            if (role.isQualifier()) {
                size--;
            }
        }
        return size;
    }

    boolean doesHaveMoreParentRoles() {
        boolean parentFound = false;
        for (Role role : roles) {
            if (role.isCreational()) {
                if (parentFound) {
                    return true;
                } else {
                    parentFound = true;
                }
            }

        }

        for (Role role : ot.plays) {
            if (!role.getParent().isObjectType()) {
                Role counterpart = role.getParent().counterpart(role);
                if (counterpart != null && counterpart.isCreational()) {
                    if (parentFound) {
                        return true;
                    } else {
                        parentFound = true;
                    }
                }
            }
        }

        return false;

    }

    public String getAutoIncr() {
        for (Role role : roles) {
            if (role.isAutoIncr()) {
                return role.detectRoleName();
            }
        }
        return null;
    }
}
