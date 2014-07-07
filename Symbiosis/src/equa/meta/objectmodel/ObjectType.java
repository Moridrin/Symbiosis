package equa.meta.objectmodel;

import static equa.code.CodeNames.AUTO_INCR;
import static equa.code.CodeNames.TEMPLATE;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import equa.code.CodeClass;
import equa.code.Field;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.LanguageOH;
import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.code.Util;
import equa.code.operations.AccessModifier;
import equa.code.operations.ActualParam;
import equa.code.operations.AddBaseTypeMethod;
import equa.code.operations.AddObjectTypeMethod;
import equa.code.operations.AdjustMethod;
import equa.code.operations.ChangeIdMethod;
import equa.code.operations.CompareToMethod;
import equa.code.operations.Constructor;
import equa.code.operations.ContainsMethod;
import equa.code.operations.CountMethod;
import equa.code.operations.EqualsMethod;
import equa.code.operations.GetSingletonMethod;
import equa.code.operations.HashCodeMethod;
import equa.code.operations.IdentifyingPropertiesMethod;
import equa.code.operations.IndexMethod;
import equa.code.operations.IndexOfMethod;
import equa.code.operations.IndexedProperty;
import equa.code.operations.InsertMethod;
import equa.code.operations.IsDefinedMethod;
import equa.code.operations.IsRemovableMethod;
import equa.code.operations.MaxCountMethod;
import equa.code.operations.Method;
import equa.code.operations.MinCountMethod;
import equa.code.operations.MoveMethod;
import equa.code.operations.Null;
import equa.code.operations.Operation;
import equa.code.operations.Param;
import equa.code.operations.PropertiesMethod;
import equa.code.operations.Property;
import equa.code.operations.PutMethod;
import equa.code.operations.RegisterMethod;
import equa.code.operations.RemoveAllMethod;
import equa.code.operations.RemoveAtMethod;
import equa.code.operations.RemoveMethod;
import equa.code.operations.STorCT;
import equa.code.operations.SearchCollectionMethod;
import equa.code.operations.SearchLexicalMethod;
import equa.code.operations.SearchMethod;
import equa.code.operations.SubParam;
import equa.code.operations.SubsequenceMethod;
import equa.code.operations.ToStringMethod;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.CollectionIdRelation;
import equa.meta.classrelations.FactTypeRelation;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.ObjectTypeRelation;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.project.ProjectRole;
import equa.util.Naming;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;
import java.util.Collections;

/**
 *
 * @author FrankP
 */
@Entity
public class ObjectType extends ParentElement implements SubstitutionType, Serializable, Publisher, ActualParam {

    private static final long serialVersionUID = 1L;
    public static final int CONSTRAINED_BASETYPE = 1;
    public static final int ABSTRACT_OBJECTTYPE = 0;
    public static final int UNIDENTIFIED_OBJECTTYPE = -1;
    /**
     * **********************************************************************
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    private TypeExpression ote;
    @Column
    private boolean _abstract;
    @Column
    private boolean comparable;
    private MutablePermission mutable;
    @Column
    protected boolean valueType;
    @Column
    private boolean hiddenId;
    @OneToMany
    protected Collection<Role> plays;
    @OneToMany
    private Collection<ObjectType> supertypes;
    @OneToMany
    private Collection<Interface> interfaces;
    @OneToMany
    private Collection<ObjectType> subtypes;
    @Transient
    protected CodeClass codeClass;
    @Transient
    protected BasicPublisher publisher;
    @Transient
    protected List<Relation> relations;

    private final Map<OperationHeader, Algorithm> algorithms = new HashMap<>();
    private Set<String> imports = new HashSet<>();

    public ObjectType() {
    }

    /**
     * objecttype with OTE based on constants will be created; objecttype will
     * be not abstract; it plays no where a role; it has no super- and subtype
     *
     * @param parent parent corresponds with this objecttype (they have the same
     * name)
     * @param constants of the OTE (constants.size() = number of roles +1)
     * @param the ranking of the roles of the OTE in relation to the ranking of
     * the roles at the parent-facttype
     */
    ObjectType(FactType parent, List<String> constants, List<Integer> roleNumbers) {
        super(parent, parent);
        init(parent, constants, roleNumbers);
    }

    /**
     * constructor needed in behalf of constructor of a singleton objecttype
     *
     * @param parent
     * @param constant
     */
    ObjectType(FactType parent, String constant) {
        super(parent, parent);
        ArrayList<String> constants = new ArrayList<>();
        constants.add(Naming.withoutCapital(constant.trim()));
        init(parent, constants, null);
    }

    ObjectType(FactType parent, int kind) {
        super(parent, parent);
        ArrayList<String> constants;
        constants = new ArrayList<>();
        if (kind == CONSTRAINED_BASETYPE) {
            constants.add("");
            constants.add("");
        }
        init(parent, constants, null);
    }

    ObjectType(FactType parent) {
        super(parent, parent);
        ote = new TypeExpression(parent.getFTE());
        _abstract = false;
        comparable = false;
        mutable = null;
        valueType = false;
        hiddenId = false;
        plays = new ArrayList<>();
        supertypes = new ArrayList<>(1);
        interfaces = new ArrayList<>(1);
        subtypes = new ArrayList<>(1);
        codeClass = new CodeClass(this, null);
        publisher = new BasicPublisher(new String[]{"subtypesIterator", "supertypesIterator", "behavior", "ote", "abstract", "ordered",
            "valueType"});
    }

    ObjectType(FactType parent, CollectionTypeExpression ote) {
        super(parent, parent);
        this.ote = ote;
        _abstract = false;
        comparable = false;
        mutable = null;
        valueType = false;
        hiddenId = true;
        plays = new ArrayList<>();
        supertypes = new ArrayList<>(1);
        interfaces = new ArrayList<>(1);
        subtypes = new ArrayList<>(1);
        codeClass = new CodeClass(this, null);
        publisher = new BasicPublisher(new String[]{"subtypesIterator", "supertypesIterator", "behavior", "ote", "abstract", "ordered",
            "valueType"});
    }

    public void addMutable(String justification) {
        if (isCandidateMutable()) {
            if (mutable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement action = rm.addActionRequirement(getFactType().getCategory(), "It is allowed to change the id of <"
                        + getName() + ">.", new ExternalInput(justification, projectRole));
                this.mutable = new MutablePermission(this, action);
            }
        }
    }

    void deleteMutable() {
        if (mutable != null) {
            mutable = null;
            publisher.inform(this, "mutable", null, isMutable());
        }
    }

    @Override
    public boolean isMutable() {
        return !isValueType() && mutable != null;
    }

    public boolean isCandidateMutable() {
        return !isValueType();

    }

    public MutablePermission getMutablePermission() {
        return mutable;
    }

    public void sealOperations(File f) {
        String file = Util.getString(f);
        Language l = ((ObjectModel) getParent().getParent()).getProject().getLastUsedLanguage();
        Map<OperationHeader, IndentedList> map = l.getOperations(file);
        addImports(l.getImports(file));
        removeLanguageAlgorithms();
        for (Entry<OperationHeader, IndentedList> e : map.entrySet()) {
            if (!e.getKey().getName(l).equals(getName() + TEMPLATE)) {
                addAlgorithm(e.getKey(), e.getValue());
            }
        }
    }

    public Set<String> getImports() {
        return imports;
    }

    public void addImports(List<String> imports) {
        this.imports.addAll(imports);
    }

    public Map<OperationHeader, Algorithm> algorithms() {
        return algorithms;
    }

    public Algorithm getAlgorithm(OperationHeader header) {
        return algorithms.get(header);
    }

    public Algorithm removeAlgorithm(OperationHeader header) {
        return algorithms.remove(header);
    }

    public void addEmptyAlgorithm(OperationHeader oh) {
        algorithms.put(oh, new Algorithm());
    }

    public void removeLanguageAlgorithms() {
        Iterator<OperationHeader> algos = algorithms.keySet().iterator();
        while (algos.hasNext()) {
            if (algos.next() instanceof LanguageOH) {
                algos.remove();
            }
        }
    }

    void addAlgorithm(OperationHeader oh, IndentedList code) {
        if (algorithms.get(oh) == null) {
            Algorithm a = new Algorithm();
            a.setCode(code);
            a.setSealed(true);
            algorithms.put(oh, a);
        } else {
            Algorithm a = algorithms.get(oh);
            a.setCode(code);
            a.setSealed(true);
            a.setEditable(false);
        }
    }

    public OperationHeader addAlgorithm(String name, boolean property, boolean classMethod, STorCT returnType, List<Param> params, IndentedList code) {
        OperationHeader oh = new MetaOH(name, property, classMethod, returnType, params);
        if (algorithms.containsKey(oh)) {
            return null;
        } else {
            Algorithm a = new Algorithm();
            a.setCode(code);
            a.setSealed(false);
            a.setEditable(code.isEmpty());
            algorithms.put(oh, a);
            return oh;
        }
    }

    boolean isLight() {
        return ((ObjectModel) this.getFactType().getParent()).requiresLightBehavior();
    }

    final void init(FactType parent, List<String> constants, List<Integer> roleNumbers) {
        if (!constants.isEmpty()) {
            String firstConstant = constants.get(0);
            if (!firstConstant.isEmpty()) {
                if (firstConstant.length() > 1) {
                    char secondChar = firstConstant.charAt(1);
                    if (!Character.isUpperCase(secondChar)) {
                        constants.set(0, Naming.withoutCapital(firstConstant));
                    }
                } else {
                    constants.set(0, Naming.withoutCapital(firstConstant));
                }
            }
        }

        if (roleNumbers == null) {
            ote = new TypeExpression(parent, constants);
        } else {
            ote = new TypeExpression(parent, constants, roleNumbers);
        }
        _abstract = false;
        comparable = false;
        mutable = null;
        valueType = false;
        hiddenId = false;
        plays = new ArrayList<>();
        supertypes = new ArrayList<>(1);
        interfaces = new ArrayList<>(2);
        subtypes = new ArrayList<>(2);
        codeClass = new CodeClass(this, null);
        publisher = new BasicPublisher(new String[]{"subtypesIterator", "supertypesIterator", "behavior", "ote", "abstract", "ordered",
            "valueType"});
    }

    /**
     *
     * @return true is this objecttype is abstract, otherwise false
     */
    public boolean isAbstract() {
        return _abstract;
    }

    /**
     *
     * @return true, if this objecttype is a value type (only the value of this
     * objecttype is relevant; replacement by a copy has no influence on the
     * state of the system)
     */
    @Override
    public boolean isValueType() {
        if (valueType) {
            return true;
        } else {
            for (ObjectType supertype : supertypes) {
                if (supertype.isValueType()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * valuetype = true --> this objecttype will be marked as a value type
     * valuetype = false --> this objecttype is not a valuetype anymore
     *
     * @param valueType
     */
    public void setValueType(boolean valueType) {
        if (this.valueType == valueType) {
            return;
        }

        if (valueType) {
            this.valueType = true;
            for (Role role : plays) {
                if (!role.isDerivable()) {
                    role.setNavigable(false);
                }
                // if (!role.isMandatory()) {
                // try {
                // new MandatoryConstraint(role, new OtherInput("a valuetype "
                // + "is always used ones", "System"));
                // } catch (ChangeNotAllowedException ex) {
                // Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE,
                // null, ex);
                // }
                // }
                // }
            }
        } else {
            this.valueType = false;
        }

        for (ObjectType subtype : subtypes) {
            subtype.setValueType(valueType);
        }

        getFactType().fireListChanged();

        publisher.inform(this, "valueType", null, isValueType());
    }

    /**
     *
     * @return true if objects of this objecttype are comparable, else false
     */
    public boolean isComparable() {
        return comparable;
    }

    @Override
    public boolean isParsable() {
        return ote.isParsable() && !isSuperType();
    }

    public boolean isSuperType() {
        return isAbstract() || subtypes().hasNext();
    }

    /**
     * if (ordered = true) then this objecttype fullfills the
     * Comparable-interface else this objecttype doesn't fullfill the Comparable
     * interface
     *
     * @param comparable
     */
    public void setComparable(boolean comparable) {
        if (this.comparable == comparable) {
            return;
        }

        if (comparable == true) {

            this.comparable = true;
            Interface comparableInterface = new Interface("Comparable");
            if (!interfaces.contains(comparableInterface)) {
                interfaces.add(comparableInterface);
            }
        } else {
            Interface comparableInterface = null;
            for (Interface i : interfaces) {
                if (i.getName().equals("Comparable")) {
                    comparableInterface = i;
                }
            }
            interfaces.remove(comparableInterface);
        }
        publisher.inform(this, "ordered", null, isComparable());
    }

    /**
     * setting of the abstractness of this objecttype
     *
     * @param isAbstract
     * @throws ChangeNotAllowedException population of this objecttype is not
     * empty
     */
    public void setAbstract(boolean isAbstract) throws ChangeNotAllowedException {
        if (isAbstract) {
            if (getFactType().getPopulation().tuples().hasNext()) {
                throw new ChangeNotAllowedException(("POPULATION OF ") + getName() + (" IS NOT EMPTY"));
            }
        }
        this._abstract = isAbstract;
        publisher.inform(this, "abstract", null, isAbstract());
    }

    /**
     *
     * @return the object type expresssion of this objecttype
     */
    public TypeExpression getOTE() {
        return ote;
    }

    /**
     *
     * @return the objectrole who controls objects of this objecttype during the
     * whole lifetime ; if this composition-objecttype doesn't exist, null will
     * be returned
     */
    public ObjectRole getCreationalRole() {
        ObjectRole role = getFactType().creationalRole();
        if (role == null) {
            for (ObjectType supertype : supertypes) {
                role = supertype.getCreationalRole();
                if (role != null) {
                    return role;
                }
            }
        }
        return role;
    }

    public List<Role> getPlaysRoles() {
        return Collections.unmodifiableList((List<Role>) plays);
    }

    /**
     * supertype will be a supertype of this objecttype this objecttype is as
     * subtype registered at supertype;
     *
     * @param supertype
     * @throws ChangeNotAllowedException if supertype is by accident a subtype
     * of this objecttype or vica versa
     */
    public void addSuperType(ObjectType supertype) throws ChangeNotAllowedException {
        if (supertype == null) {
            throw new RuntimeException(("NULL NOT ALLOWED"));
        }
        if (hasSuperType(supertype)) {
            return;
        }
        if (hasSubType(supertype)) {
            throw new ChangeNotAllowedException(("SUPERTYPE ") + supertype.getName() + (" IS A SUBTYPE OF") + (" THIS OBJECTTYPE ")
                    + getName());
        }

        supertype.addSubType(this);
        this.supertypes.add(supertype);
        publisher.inform(this, "supertypesIterator", null, supertypes.iterator());
    }

    /**
     * @param type will be added as subtype
     */
    void addSubType(ObjectType type) {
        subtypes.add(type);
        publisher.inform(this, "subtypesIterator", null, subtypes.iterator());
    }

    void removeSubType(ObjectType type) {
        boolean removed = subtypes.remove(type);
        if (!removed) {
            throw new RuntimeException(("REMOVING OF SUBTYPE HAS NOT SUCCEEDED"));
        }
        publisher.inform(this, "subtypesIterator", null, subtypes.iterator());
    }

    /**
     * the supertype of this objecttype will be removed
     *
     * @throws ChangeNotAllowedException if any tuple in the population of this
     * objecttype refers to an object which plays a role in some facttype of
     * supertype (or his ancestors)
     */
    public void removeSupertype(ObjectType supertype) throws ChangeNotAllowedException {
        if (!supertypes.contains(supertype)) {
            throw new RuntimeException(("THIS OBJECTTYPE " + getName() + " HAS NO SUPERTYPE " + supertype.getName()));
        }

        // Tuple tuple = supertypesAreUsingTuplesOf(this);
        // if (tuple != null) {
        // throw new
        // ChangeNotAllowedException(("THIS OBJECTTYPE IS USED AS SUPERTYPE IN ")
        // + tuple.toString());
        // }
        supertype.removeSubType(this);
        supertypes.remove(supertype);
        publisher.inform(this, "supertypesIterator", null, supertypes.iterator());
    }

    void deleteSupertype(ObjectType supertype) {
        if (!supertypes.contains(supertype)) {
            throw new RuntimeException(("THIS OBJECTTYPE " + getName() + " HAS NO SUPERTYPE " + supertype.getName()));
        }

        supertype.removeSubType(this);
        supertypes.remove(supertype);
        publisher.inform(this, "supertypesIterator", null, supertypes.iterator());
    }

    /**
     * all existing subtypes of this objecttypes will be a direct subtype of ot;
     * ot will be the only subtype of this objecttype
     *
     * @param ot not equal to this objecttype
     * @throws ChangeNotAllowedException if ot is a super- or subtype of this
     * objecttype
     */
    public void insertSubtype(ObjectType ot) throws ChangeNotAllowedException {
        if (ot.equals(this)) {
            throw new RuntimeException("inserting " + ot.getName() + " as subtype at the same objecttype is not allowed.");
        }

        if (hasSubType(ot)) {
            throw new ChangeNotAllowedException(getName() + " is already supertype of " + ot.getName());
        } else if (hasSuperType(ot)) {
            throw new ChangeNotAllowedException(getName() + " is already subtype of " + ot.getName());
        } else {
            for (ObjectType subtype : subtypes) {
                subtype.addSuperType(ot);
            }
            ot.addSuperType(this);
            for (ObjectType subtype : subtypes) {
                if (!subtype.equals(ot)) {
                    subtype.removeSupertype(this);
                }
            }
        }
    }

    Tuple supertypesAreUsingTuplesOf(ObjectType ot) {
        Tuple tuple;
        for (ObjectType supertype : supertypes) {
            tuple = supertype.getFactType().getPopulation().usesTuplesOf(ot.getFactType().getPopulation());
            if (tuple != null) {
                return tuple;
            }
            tuple = supertype.supertypesAreUsingTuplesOf(ot);
            if (tuple != null) {
                return tuple;
            }
        }
        return null;
    }

    /**
     *
     * @return an iterator over all direct supertypes of this objecttype
     */
    public Iterator<ObjectType> supertypes() {
        return supertypes.iterator();
    }

    public int countSupertypes() {
        return supertypes.size();
    }

    public Iterator<Interface> interfaces() {
        return interfaces.iterator();
    }

    /**
     *
     * @return an iterator over all direct subtypes of this objecttype
     */
    public Iterator<ObjectType> subtypes() {
        return subtypes.iterator();
    }

    /**
     *
     * @return a list with all (direct and indirect) non-abstract subtypes
     */
    public List<ObjectType> concreteSubTypes() {
        List<ObjectType> types = new ArrayList<>();

        for (ObjectType subtype : subtypes) {
            if (!subtype.isAbstract()) {
                types.add(subtype);
            }
            types.addAll(subtype.concreteSubTypes());
        }

        return types;
    }

    // /**
    // * this objecttype is reduced to a facttype;<br> every involved role will
    // be
    // * replaced by the identifying roles of the former objecttype;<br> the
    // * type-expressions of the roletypes where a involved role belongs to will
    // * be adjusted with the help of the OTE of the former objecttype the
    // * population of the roletype where a involved rol belongs to will be
    // * adjusted to the new situation.
    // *
    // * @throws ChangeNotAllowedException if facttype doesn't possesses a
    // * facttype expression or objecttype is involved in inheritance relation
    // */
    // public void deobjectify() throws ChangeNotAllowedException {
    // // via parent regelen
    // throw new UnsupportedOperationException();
    // }
    /**
     *
     * @param type
     * @return true if type is direct or indirect a subtype of this objecttype,
     * else false
     */
    public boolean hasSubType(ObjectType type) {
        for (ObjectType ot : subtypes) {
            if (ot == type) {
                return true;
            }
            if (ot.hasSubType(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param type
     * @return true if type is a (indirect) supertype of this objecttype, else
     * false
     */
    public boolean hasSuperType(ObjectType type) {
        for (ObjectType supertype : supertypes) {
            if (supertype.equals(type)) {
                return true;
            }
            if (supertype.hasSuperType(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * warning: in theory it could be possible that there exist more than one
     * mandatorial relation
     *
     * @param target
     * @return
     */
    public Relation retrieveMandatorialRelationTo(ObjectType target) {
        FactType ft = this.getFactType();
        for (Role role : ft.roles) {
            if (role.getSubstitutionType().equals(target)) {
                return new IdRelation(this, role);
            }
        }

        for (Role role : plays) {
            FactType parentOfRole = role.getParent();
            if (parentOfRole.isClass() && role.isMandatory()) {
                return new ObjectTypeRelation(this, role);
            } else {
                Role counterpart = parentOfRole.counterpart(role);
                if (counterpart != null && counterpart.getSubstitutionType().equals(target) && role.isMandatory()) {
                    return new FactTypeRelation(this, role);
                }
            }
        }
        return null;
    }

    /**
     * makes an complete expression based on the value and the OTE of this
     * objecttype
     *
     * @param value
     */
    @Override
    public String makeExpression(Value value) {
        if (value instanceof AbstractValue) {
            return value.toString();
        }
        return ote.makeExpression((Tuple) value);
    }

    /**
     * parses the substitutionvalue out of expression. If expression is not
     * parsable null will be returned
     *
     * @param expression
     * @param otherOptions
     * @param source the source of expression
     * @return the constructed value based on expression and source; if creation
     * is rejected null will be returned
     * @throws equa.meta.MismatchException
     */
    @Override
    public Value parse(String expression, boolean otherOptions, FactRequirement source) throws MismatchException {
        if (isAbstract()) {
            return new AbstractValue(this, source, this, expression);
        }
        if (!ote.isParsable()) {
            throw new NotParsableException(getOTE(), ("OTE OF " + getName() + " NOT PARSABLE"));
        }

        // delegating parsing to facttype, he knows the roles
        return getFactType().parse(expression.trim(), ote, otherOptions, source);
    }

    /**
     * parses the substitutionvalue on base of expressionParts. If
     * expressionParts is not parsable null will be returned
     *
     * @param expressionParts
     * @param source the source of expression
     * @return the constructed value based on expressionParts and source; if
     * creation is rejected null will be returned
     */
    public Value parse(List<String> expressionParts, FactRequirement source) throws MismatchException {
        return getFactType().parse(expressionParts, ote, source);
    }

    /**
     * this objecttype gets involved in role
     *
     * @param role this objecttype doesn't play a role in given role yet
     */
    @Override
    public void involvedIn(Role role) {
        if (plays.contains(role)) {
            throw new RuntimeException(("OBJECTTYPE IS ALREADY INVOLVED IN ROLE "));
        }
        plays.add(role);
    }

    /**
     * this substitutiontype will stop playing a role in given role
     *
     * @param role this objecttype isn't the substitutiontype of this role
     * anymore
     */
    @Override
    public void resignFrom(Role role) {
        if (!role.getSubstitutionType().equals(this)) {
            throw new RuntimeException(("CHANGE FIRST THE SUBSTITUTIONTYPE OF ") + ("THIS ROLE"));
        }
        plays.remove(role);
    }

    void resignFrom(ObjectType ot) {
        List<Role> toRemove = new ArrayList<>();
        for (Role role : plays) {
            if (role.getSubstitutionType() == ot) {
                toRemove.add(role);
            }
        }
        plays.removeAll(toRemove);
    }

    /**
     *
     * @return the name of this objecttype
     */
    @Override
    public String getName() {
        return getFactType().getName();
    }

    public String getPluralName() {
        return Naming.plural(getName());
    }

    void resignTuplesFromAllRoles() {
        for (Role role : plays) {
            role.getParent().getPopulation().clearPopulation();
        }
    }

    Tuple searchForUseOf(Tuple t) {
        for (Role r : plays) {
            Tuple tuple = r.getParent().getPopulation().anyTupleWhichUses(t, r.getNr());
            if (tuple != null) {
                return tuple;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Type o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjectType) {
            return this.compareTo((ObjectType) o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.getName().hashCode();
        return hash;
    }

    /**
     * every objecttype is a facttype too
     *
     * @return the corresponding facttype of this objecttype
     */
    public FactType getFactType() {
        return getOTE().getParent();
    }

    List<Relation> playsRoleRelations() {
        ArrayList<Relation> list = new ArrayList<>();

        for (Role role : plays) {
            if (role.isNavigable() && !role.isQualifier()) {
                Relation relation;
                if (role.getParent().isClass()) {
                    relation = new ObjectTypeRelation(this, role);
                } else if (role.getParent().nonQualifierSize() == 1) {
                    relation = new BooleanRelation(this, role);
                } else {
                    relation = new FactTypeRelation(this, role);
                }
                list.add(relation);
            }
        }
        return list;
    }

    /**
     *
     * @return true if this objecttype, or a supertype, plays a role somewhere,
     * otherwise false
     */
    public boolean isSolitary() {
        if (plays.isEmpty()) {
            if (supertypes.isEmpty()) {
                return true;
            } else {
                for (ObjectType supertype : supertypes) {
                    if (!supertype.isSolitary()) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    void checkActivity() throws ChangeNotAllowedException {
        if (!plays.isEmpty()) {
            throw new equa.meta.ChangeNotAllowedException(("OBJECTTYPE ") + getName() + (" PLAYS ROLES"));
        }

        if (!subtypes.isEmpty()) {
            throw new equa.meta.ChangeNotAllowedException(("OBJECTTYPE ") + getName() + (" HAS SUBTYPES"));
        }

        Tuple tuple = supertypesAreUsingTuplesOf(this);
        if (tuple != null) {
            throw new equa.meta.ChangeNotAllowedException(("OBJECTTYPE ") + getName() + (" IS IN USE IN TUPLE ") + tuple.toString());
        }

    }

    @Override
    public boolean isEqualOrSupertypeOf(SubstitutionType st) {
        if (equals(st)) {
            return true;
        }
        if (st instanceof ObjectType) {
            return ((ObjectType) st).hasSuperType(this);
        } else {
            return false;
        }
    }

    /**
     *
     * @param ot is not a super or subtype of this objecttype or equal
     * @return a type t whereby t is supertype of this type and o t and there
     * exists no subtype of t with the same property; if such an objecttype
     * doesn't exist a null will be returned
     */
    public ObjectType detectCommonSupertype(ObjectType ot) {
        Set<ObjectType> set1 = allSupertypes();
        Set<ObjectType> set2 = ot.allSupertypes();
        Set<ObjectType> common = new HashSet<>();
        for (ObjectType supertype : set1) {
            if (set2.contains(supertype)) {
                common.add(supertype);
            }
        }

        if (common.isEmpty()) {
            return null;
        } else {
            Iterator<ObjectType> it = common.iterator();
            ObjectType candidate = it.next();
            while (it.hasNext()) {
                ObjectType otherCommon = it.next();
                if (candidate.hasSubType(otherCommon)) {
                    candidate = otherCommon;
                }
            }
            return candidate;
        }

    }

    /**
     *
     * @return a set with all (direct and indirect) supertypes of this
     * objecttype
     */
    public Set<ObjectType> allSupertypes() {
        Set<ObjectType> allSupertypes = new HashSet<>();
        for (ObjectType supertype : supertypes) {
            allSupertypes.add(supertype);
            allSupertypes.addAll(supertype.allSupertypes());
        }
        return allSupertypes;
    }

    /**
     *
     * @return a set with all (direct and indirect) subtypes of this objecttype
     */
    public Set<ObjectType> allSubtypes() {
        Set<ObjectType> allSubtypes = new HashSet<>();
        for (ObjectType subtype : subtypes) {
            allSubtypes.add(subtype);
            allSubtypes.addAll(subtype.allSubtypes());
        }
        return allSubtypes;
    }

    /**
     *
     * @return true if this objecttype is a singleton (there allways exists
     * exactly one object of this type) otherwise false
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * @return the resulting class of this objecttype, could be undefined = null
     */
    public CodeClass getCodeClass() {
        return codeClass;
    }

    public void generateClass() {
        relations = getFactType().relations();
        codeClass = new CodeClass(this, relations);
        generateFields(relations);
        generateProperties(relations);
    }

    void generateMethods() {
        List<Relation> relations = codeClass.getRelations();
        try {
            generateMethods(relations);
            generateToStringMethod();
            generateEqualsMethod();
            if (!isLight()) {
                if (!isAbstract()) {
                    generatePropertiesMethod();
                    if (!isValueType()) {
                        generateIdentifierMethod();
                    }
                }
                generateCompareToMethod();
            }
        } catch (DuplicateException exc) {
            exc.printStackTrace();
        }
    }

    void generateFields(List<Relation> relations) {
        for (Relation r : relations) {
            if (r.isNavigable() && !r.isDerivable() && (r.targetType() == null || !r.targetType().isSingleton())) {
                codeClass.addField(new Field(r));
            }
            if (!r.isMandatory() && !r.hasMultipleTarget() && r.targetType().getUndefinedString() == null
                    && !r.targetType().equals(BaseType.BOOLEAN)) {
                codeClass.addField(new Field(BaseType.BOOLEAN, r.fieldName() + "Defined"));
            }
            if (r.getAutoIncrField() != null) {
                codeClass.addField(new Field(BaseType.NATURAL, r.getAutoIncrField() + AUTO_INCR));
            }
        }
    }

    void generateConstructor() {
        try {
            generateConstructor(codeClass.getRelations());
        } catch (DuplicateException ex) {
            Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void generateConstructor(List<Relation> relations) throws DuplicateException {
        Constructor constructor = new Constructor(relations, this, this.getFactType());
        codeClass.addOperation(constructor);

        if (isSingleton()) {
            constructor.setAccessModifier(AccessModifier.PRIVATE);
            Method singleton = new GetSingletonMethod(this, this.getFactType());
            codeClass.addOperation(singleton);
        }
    }

    void generateProperties(List<Relation> relations) {
        for (Relation relation : relations) {
            if (relation.isNavigable() && !relation.isHidden() && !relation.targetType().isSingleton()) {
                if (relation.getMinFreq() < relation.getMaxFreq()) {
                    if (relation.isCollectionReturnType() || relation.isSeqRelation()) {
                        generateCountMethod(relation);
                    }
                }

              //  if (relation.isDerivable() && !relation.isQualifier()) {
                // MetaOH metaOH;
                // if (relation.isCollectionReturnType()) {
                // metaOH = new MetaOH(relation.getPluralName(), true,
                // relation.collectionType(), new ArrayList<>());
                // } else {
                // metaOH = new MetaOH(relation.name(), true,
                // relation.targetType(), new ArrayList<>());
                // }
                // addAlgorithm(metaOH);
                //  } else 
                {

                    Property property;
                    if (relation.isMapRelation() || (relation.isSeqRelation() && (relation.isSettable() || relation.isAdjustable()))) {
                        property = new IndexedProperty(relation, this);
                    } else {
                        property = new Property(relation, this);
                    }

                    codeClass.addOperation(property);

                    if (!relation.isMandatory() && relation.targetType().getUndefinedString() == null) {
                        // unknown basevalues are risky, that's why we add a
                        // supplementary
                        // boolean property which indicates if the property is
                        // defined properly

                        if (!relation.targetType().equals(BaseType.BOOLEAN)) {
                            Method defined = new IsDefinedMethod(relation, this);
                            codeClass.addOperation(defined);
                        }
                    }
                }
            }
        }
    }

    void generateMethods(List<Relation> relations) throws DuplicateException {
        for (Relation relation : relations) {
            SubstitutionType st = relation.targetType();

            if (relation.hasMultipleTarget()) {

                if (!relation.isComposition()) {
                    generateContainsMethod(relation, st);
                    generateMoveMethod(relation);
                }

                if (st instanceof ObjectType) {
                    if (relation.isResponsible()) {
                        generateSearchMethodObjectType(relation, (ObjectType) st);

                    }
                }

                generateAddMethod(relation);
                generateChangeWithIndexMethods(relation);

            }

            if (relation.isCreational()) {
                ObjectType ot = (ObjectType) st;
                ot.encapsulateConstructor();

                if (ot.getFactType().isMutable()) {
                    generateChangeIdMethod(relation, ot);
                }
            }

            if (!relation.multiplicity().equals("1") && !relation.isDerivable() && relation.targetType() != BaseType.BOOLEAN) {
                generateRemoveMethods(relation);
            }

            if (relation instanceof FactTypeRelation) {
                generateAdjustMethod((FactTypeRelation) relation);
            }

        }

        codeClass.eliminateDuplicateSignatures();
    }

    void generatePropertiesMethod() throws DuplicateException {
        Method propertiesMethod = new PropertiesMethod(this, this.getFactType());
        codeClass.addOperation(propertiesMethod);

    }

    void generateIdentifierMethod() throws DuplicateException {
        Method identifierMethod = new IdentifyingPropertiesMethod(this, this.getFactType());
        codeClass.addOperation(identifierMethod);
    }

    void generateChangeIdMethod(Relation relation, ObjectType ot) throws DuplicateException {
        List<Role> otherRoles = ot.getRolesPlayedByOtherSubstitutionTypes(this);
        if (!otherRoles.isEmpty()) {
            codeClass.addOperation(new ChangeIdMethod(relation, ot, otherRoles));
        }
    }

    void generateContainsMethod(Relation relation, SubstitutionType st) throws DuplicateException {
        if (!relation.isMapRelation() || relation.hasMultipleQualifiedTarget()) {
            Method containsMethod = new ContainsMethod(relation, this, st);
            codeClass.addOperation(containsMethod);
        }
    }

    void generateSearchMethodObjectType(Relation relation, ObjectType target) throws DuplicateException {
        if (target.isAbstract()) {
            // concrete subtypes ...
            List<ObjectType> concreteSubTypes = target.concreteSubTypes();
            for (ObjectType concreteSubType : concreteSubTypes) {
                if (concreteSubType.isUnderControlOf(target) && !concreteSubType.isSingleton()) {
                    if (relation instanceof ObjectTypeRelation) {
                        generateSearchMethodObjectTypeRelation(relation, concreteSubType);
                    } else {
                        if (relation instanceof FactTypeRelation) {
                            generateSearchMethodFactTypeRelation((FactTypeRelation) relation, concreteSubType);
                        }
                    }
                }
            }
        } else {
            if (relation instanceof ObjectTypeRelation || relation instanceof CollectionIdRelation) {
                generateSearchMethodObjectTypeRelation(relation, target);
            } else {
                if (relation instanceof FactTypeRelation) {
                    generateSearchMethodFactTypeRelation((FactTypeRelation) relation, target);
                }
            }
        }
    }

    void generateChangeWithIndexMethods(Relation relation) throws DuplicateException {
        if (relation.isSeqRelation()) {
            if (relation.isInsertable()) {
                Method insertMethod = new InsertMethod(relation, this);
                codeClass.addOperation(insertMethod);
            }

            if (relation.isRemovable()) {
                Method removeMethod = new RemoveAtMethod(relation, this);
                codeClass.addOperation(removeMethod);
            }

            if (relation.targetType() instanceof ObjectType) {
                Method indexOfMethod = new IndexOfMethod(relation, this);
                codeClass.addOperation(indexOfMethod);
            }

            Method subseqMethod = new SubsequenceMethod(relation, this);
            codeClass.addOperation(subseqMethod);
        }
    }

    void generateAdjustMethod(FactTypeRelation relation) throws DuplicateException {
        if (relation.isAdjustable()) {
            Method adjustMethod = new AdjustMethod(relation, this);
            codeClass.addOperation(adjustMethod);
        }
    }

    void generateAddMethod(Relation relation) throws DuplicateException {
        if (relation.isDerivable()) {
            return;
        }

        SubstitutionType st = relation.targetType();

        if (st instanceof BaseType) {
            if (!relation.isAddable()) {
                return;
            }
            Method addMethod;
            if (relation.isMapRelation()) {
                addMethod = new PutMethod(relation, this, relation.targetType());
            } else {
                addMethod = new AddBaseTypeMethod(relation, this);
            }
            codeClass.addOperation(addMethod);

        } else {
            ObjectType target = (ObjectType) st;
            if (!target.isAbstract() && !target.isSingleton()) {
                generateAddMethodConcreteObjectType(target, relation);
            }

            List<ObjectType> concreteSubTypes = target.concreteSubTypes();
            for (ObjectType concreteSubType : concreteSubTypes) {
                if (concreteSubType.isUnderControlOf(target) && !concreteSubType.isSingleton()) {
                    generateAddMethodConcreteObjectType(concreteSubType, relation);
                }
            }
        }
    }

    void generateAddMethodConcreteObjectType(ObjectType concreteObjectType, Relation relation) throws DuplicateException {

        if (!relation.isNavigable()) {
            return;
        }

        Method addMethod;
        if (relation.isAddable()) {
            if (relation.isCreational()) {
                concreteObjectType.encapsulateConstructor();
                if (relation.isMapRelation()) {
                    addMethod = new PutMethod(relation, this, concreteObjectType);
                } else {
                    addMethod = new AddObjectTypeMethod(relation, concreteObjectType, this);
                }
            } else {
                if (relation.isMapRelation()) {
                    addMethod = new PutMethod(relation, this, concreteObjectType);
                } else {
                    addMethod = new RegisterMethod(relation, this);
                }
            }
        } else if (relation instanceof IdRelation) {
            return;
        } else {
            Relation inverse = relation.inverse();
            if (concreteObjectType.isAddable() && (inverse != null && inverse.isNavigable())) {
                if (relation.isMapRelation()) {
                    addMethod = new PutMethod(relation, this, concreteObjectType);
                } else {
                    addMethod = new RegisterMethod(relation, this);
                }
                addMethod.setAccessModifier(AccessModifier.NAMESPACE);
            } else {
                return;
            }
        }

        codeClass.addOperation(addMethod);

    }

    void generateCountMethod(Relation relation) {

        int lower = relation.multiplicityLower();
        int upper = relation.multiplicityUpper();

        if (lower == upper) {
            return;
        }

        if (lower > 0) {
            Operation minCount = new MinCountMethod(relation, this, lower, relation.getLowerConstraint());
            codeClass.addOperation(minCount);
        }
        if (upper > 0 && upper < Integer.MAX_VALUE) {
            Operation maxCount = new MaxCountMethod(relation, this, upper, relation.getUpperConstraint());
            codeClass.addOperation(maxCount);
        }

        Method countMethod = new CountMethod(relation, this);
        codeClass.addOperation(countMethod);

    }

    void generateMoveMethod(Relation relation) throws DuplicateException {

        if (relation.isPartOfId()) {
            return;
        }

        Relation inverse = relation.inverse();
        if (inverse != null && inverse.multiplicity().equals("1") && relation.getParent().isMutable() && !relation.isComposition()) {
            // multiplicity = 1; no composition-relation; relation and inverse
            // is modifiable
            Method moveMethod = new MoveMethod(relation, this);
            codeClass.addOperation(moveMethod);
        }
    }

    void generateRemoveMethods(Relation relation) throws DuplicateException {

        if (relation.isPartOfId()) {
            return;
        }

        boolean withRemove = false;

        if (!isLight() && relation.isNavigable() && relation.hasMultipleTarget() && relation.isRemovable()) {
            Method removeAllMethod = new RemoveAllMethod(relation, this);
            codeClass.addOperation(removeAllMethod);
            withRemove = true;
        }
        Relation inverse = relation.inverse();
        if (relation.isRemovable()) {
            Method removeMethod = new RemoveMethod(relation, this);
            codeClass.addOperation(removeMethod);
            withRemove = true;
        } else if (relation.isNavigable() && inverse != null && !inverse.isComposition()
                && (relation.hasMultipleTarget() || !relation.isMandatory())) {
            ObjectType target = (ObjectType) relation.targetType();
            ObjectType responsible = getResponsible();
            if (inverse.isRemovable() || inverse.isSettable() || (!target.equals(responsible) && target.isRemovable())) {
                Method removeMethod = new RemoveMethod(relation, this);
                codeClass.addOperation(removeMethod);
                withRemove = true;
            }
        }

        if (withRemove && relation.isComposition()) {
            SubstitutionType target = relation.targetType();
            if (target instanceof ObjectType) {
                ObjectType targetObjectType = (ObjectType) target;
                Set<Relation> contacts = targetObjectType.fans();

                contacts.remove(inverse);
                if (!contacts.isEmpty()) {
                    targetObjectType.codeClass.addOperation(new IsRemovableMethod(relation, targetObjectType));
                }
            }
        }
    }

    // final boolean hasCompositionalRemovable(ObjectType
    // compositionalResponsible) {
    // if (compositionalResponsible == null) {
    // return false;
    // }
    // return !compositionalResponsible.equals(this);
    // }
    void generateSearchMethodFactTypeRelation(FactTypeRelation relation, ObjectType concreteObjectType) throws DuplicateException {

        // if (relation.getParent().size() > 2) {
        // // elementary fact type contains qualifier roles
        // Operation indexedProperty = new IndexedProperty(relation, this);
        // codeClass.addOperation(indexedProperty);
        // } else
        {
            // elementary binary fact type
            boolean indexMethodNeeded = true;

            if (relation.targetType() instanceof CollectionType) {
                Method searchMethod = new SearchCollectionMethod(relation, this);
                codeClass.addOperation(searchMethod);
            } else {
                Method searchMethod;

                searchMethod = new SearchMethod(relation, concreteObjectType, this);
                Iterator<Param> itParams = searchMethod.getParams().iterator();
                Param param1 = itParams.next();
                if (param1.getType().equals(BaseType.NATURAL) && !itParams.hasNext()) {
                    indexMethodNeeded = false;
                }
                codeClass.addOperation(searchMethod);
                if (searchLexicalNeeded(searchMethod.getParams())) {
                    codeClass.addOperation(new SearchLexicalMethod(relation, concreteObjectType, this));
                }

            }
            // if (relation.isSeqRelation() && (indexMethodNeeded ||
            // relation.targetType() instanceof BaseType)) {
            // Method indexMethod = new IndexMethod(relation, this);
            // codeClass.addOperation(indexMethod);
            //
            // }
        }
    }

    void generateSearchMethodObjectTypeRelation(Relation relation, ObjectType concreteObjectType) throws DuplicateException {

        Method searchMethod = new SearchMethod(relation, concreteObjectType, this);
        codeClass.addOperation(searchMethod);
        if (searchLexicalNeeded(searchMethod.getParams())) {
            codeClass.addOperation(new SearchLexicalMethod(relation, concreteObjectType, this));
        }

        Iterator<Param> itParams = searchMethod.getParams().iterator();
        Param param1 = itParams.next();
        if (!relation.isSetRelation() && (!param1.getType().equals(BaseType.NATURAL) || itParams.hasNext())) {
            Method indexMethod = new IndexMethod(relation, this);
            codeClass.addOperation(indexMethod);
        }

    }

    static boolean searchLexicalNeeded(List<Param> params) {
        for (Param param : params) {
            if (param.getType() instanceof BaseType) {
            } else if (param.getType() instanceof ObjectType) {
                ObjectType ot = (ObjectType) param.getType();
                if (!ot.isAbstract()) {
                    return true;
                }
            } else {
                // CT ct = (CT) param.getType();
                // todo
            }
        }
        return false;
    }

    void generateToStringMethod() throws DuplicateException {
        if (!_abstract) {
            Method toStringMethod = new ToStringMethod(this, this.getFactType());
            codeClass.addOperation(toStringMethod);
        }
    }

    void generateEqualsMethod() throws DuplicateException {
        if ((!isLight() || this.isValueType()) && !this.isSingleton() && !_abstract) {
            Method equalsMethod = new EqualsMethod(this, this.getFactType());
            codeClass.addOperation(equalsMethod);
            Method hashCodeMethod = new HashCodeMethod(this, this.getFactType());
            codeClass.addOperation(hashCodeMethod);
        }
    }

    void generateCompareToMethod() throws DuplicateException {
        if (comparable) {
            Method compareToMethod = new CompareToMethod(this, this.getFactType());
            codeClass.addOperation(compareToMethod);
        }
    }

    void encapsulateConstructor() {
        if (!isValueType()) {
            codeClass.encapsulateConstructor();
            for (ObjectType subtype : subtypes) {
                subtype.encapsulateConstructor();
            }
        }
    }

    /**
     *
     * @return a set with the names of all public properties with
     * getter-functionality of this objecttype; properties of the supertype(s)
     * are included
     */
    public Set<String> properties() {
        Set<String> properties = codeClass.publicPropertyNames();
        for (ObjectType supertype : supertypes) {
            properties.addAll(supertype.properties());
        }
        return properties;
    }

    /**
     *
     * @return a set with all identifying properties of this object type
     */
    public List<Relation> identifyingRelations() {
        return getFactType().identifyingRelations();
    }

    public List<STorCT> identifierTypes() {
        List<STorCT> identifierTypes = new ArrayList<>();
        for (Relation relation : getFactType().identifyingRelations()) {
            identifierTypes.add(relation.targetType());
        }
        return identifierTypes;
    }

    // private static String detectUniqueName(String roleName, List<String>
    // names) {
    // String name = roleName;
    // if (names.contains(name)) {
    // int nr = 1;
    // while (names.contains(name + "_" + nr)) {
    // nr++;
    // }
    // name = name + "_" + nr;
    // }
    // names.add(name);
    // return name;
    // }
    @Override
    public String toString() {
        return getName();
    }

    void setOTE(FactType parent, List<String> constants, int[] rolenrs) {
        throw new UnsupportedOperationException();
    }

    void setOTE(TypeExpression ote) {
        this.ote = ote;
    }

    private List<Role> getRolesPlayedByOtherSubstitutionTypes(ObjectType ot) {
        FactType ft = getFactType();
        List<Role> otherRoles = new ArrayList<>();
        Iterator<Role> itRoles = ft.roles();
        while (itRoles.hasNext()) {
            Role role = itRoles.next();
            if (role.getSubstitutionType() != ot && !role.isQualifier()) {
                otherRoles.add(role);
            }
        }
        return otherRoles;
    }

    /**
     *
     * @param accessibles
     * @return if this object type is accessible
     */
    boolean isAccessible() {
        // if this objecttype is responsible then he cannot be made accessible
        // as a
        // consequence of a mandatorial role he plays in a non-objectified fact
        // type
        for (Role role : plays) {
            if (role.isMandatory() && !role.isResponsible()) {
                FactType parentOfRole = role.getParent();
                // if this object type plays role in non-objectified fact
                // type
                if (!parentOfRole.isObjectType()) {
                    Role counterpart = parentOfRole.counterpart(role);
                    if (counterpart != null && counterpart.isNavigable()) {
                        ObjectType st = (ObjectType) counterpart.getSubstitutionType();
                        Set<ObjectType> responsibles = st.getResponsibles();
                        if (responsibles.isEmpty() || !responsibles.contains(this)) {
                            return true;
                        }
                    }
                }
            }
        }
        if (isAbstract()) {
            for (ObjectType subtype : subtypes) {
                if (!subtype.getFactType().isAccessible()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @return true if this object type plays a responsible role with respect to
     * a substitutiontype which isn't base type or value type
     */
    boolean isResponsibleForNonVT() {
        for (Role role : plays) {
            if (role.isResponsibleForNonVT()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCompositionOf(SubstitutionType st) {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.getSubstitutionType() == st && role.isComposition()) {
                return true;
            }
        }
        return false;
    }

    boolean isDirectAccessible() {
        return true;
    }

    public boolean isDoubleRelatedWith(ObjectType ot) {
        int count = 0;
        for (Role role : plays) {
            if (role.targetSubstitutionType().equals(ot)) {
                count++;
            }
        }
        return count > 1;
    }

    @Override
    public void addListener(PropertyListener listener, String property) {
        publisher.addListener(listener, property);
    }

    @Override
    public void removeListener(PropertyListener listener, String property) {
        publisher.removeListener(listener, property);
    }

    public TypeExpression getTypeExpression() {
        return ote;
    }

    public List<Relation> relations() {
        if (relations == null) {
            relations = getFactType().relations();
        }
        return relations;
    }

    public Set<Relation> fans() {
        Set<Relation> fans = new HashSet<>();
        for (Relation relation : relations()) {
            Relation inverse = relation.inverse();
            if (inverse != null && inverse.isNavigable()) {
                if (inverse.getOwner().getResponsible() != this && getResponsible() != inverse.getOwner()) {
                    fans.add(inverse);
                }
            }
        }
        return fans;
    }

    List<String> getPath(ObjectType ot) {
        List<String> path = new ArrayList<>();
        for (ObjectType subtype : subtypes) {
            if (subtype.equals(ot)) {
                return path;
            }
            List<String> subpath = subtype.getPath(ot);
            if (subpath != null) {
                path.addAll(subpath);
                return path;
            }
        }
        return null;
    }

    @Override
    public boolean hasAbstractRoles() {
        return isAbstract() || getFactType().hasAbstractRoles();
    }

    @Override
    public String getUndefinedString() {
        return "null";
    }

    @Override
    public boolean isRemovable() {
        return getFactType().isRemovable();
    }

    @Override
    public void remove() {
        try {
            checkActivity();
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }

        removeBehavior();

        if (mutable != null) {
            try {
                mutable.remove();
                // for (Source source : mutable.sources()) {
                // if (source instanceof Requirement) {
                // ((Requirement) source).remove();
                // }
                // }
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
            }
            mutable = null;
        }

        for (ObjectType supertype : supertypes) {
            supertype.removeSubType(this);
        }
        supertypes.clear();
        try {
            super.remove();
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }
        ((FactType) getParent()).removeYourself();

        for (ObjectType supertype : supertypes) {
            supertype.removeSubType(this);
        }
        supertypes.clear();
        try {
            super.remove();
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }

        ((FactType) getParent()).removeYourself();
    }

    boolean relatedToAddableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isAddable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToSettableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isSettable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToAdjustableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isAdjustable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToRemovableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isRemovable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToInsertableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isInsertable()) {
                return true;
            }
        }
        return false;
    }

    void removeBehavior() {
        codeClass = null;
    }

    @Override
    public boolean isSuitableAsIndex() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    boolean isSuperTypeResponsible() {
        for (ObjectType supertype : supertypes) {
            if (supertype.isResponsibleForNonVT()) {
                return true;
            }
        }
        return false;
    }

    boolean isReflexiveCreational() {
        if (isReflexiveCreational(this)) {
            return true;
        }

        return false;
    }

    boolean isReflexiveCreational(ObjectType forbiddenSubordinate) {
        for (Role role : plays) {
            if (role.isCreational()) {
                ObjectType subordinate = null;
                if (role.getParent().isObjectType()) {
                    subordinate = role.getParent().getObjectType();
                } else if (role.getParent().size() == 1) {
                } else {
                    Role counterpart = role.getParent().counterpart(role);
                    if (counterpart == null) {
                        // fact type with 3 or more significant roles
                    } else if (counterpart instanceof ObjectRole) {
                        subordinate = (ObjectType) counterpart.getSubstitutionType();
                    }
                }
                if (subordinate != null) {
                    if (forbiddenSubordinate.equals(subordinate)) {
                        return true;
                    }
                    if (subordinate.isReflexiveCreational(forbiddenSubordinate)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    boolean doesHaveMoreParents() {
        return getFactType().doesHaveMoreParentRoles();
    }

    void refreshFactRequirements() throws ChangeNotAllowedException {
        for (Role role : plays) {
            role.getParent().refreshFactRequirements();
        }
    }

    @Override
    public void remove(ModelElement member) {
        if (mutable != null) {
            mutable = null;
        }
    }

    @Override
    public boolean isPureFactType() {
        return false;
    }

    boolean isMinimalAT() {
        if (!isAbstract()) {
            return false;
        }
        List<ObjectType> concreteSubTypes = concreteSubTypes();
        int count = 0;
        for (ObjectType ot : concreteSubTypes) {
            if (!ot.isSingleton()) {
                count++;
            }
        }
        return count <= 1;
    }

    List<ObjectType> nonAccessibleConcreteSubTypes(Set<ObjectType> accessibles) {
        List<ObjectType> concreteSubTypes = concreteSubTypes();
        List<ObjectType> nonAccessibleSubTypes = new ArrayList<>();
        for (ObjectType ot : concreteSubTypes) {
            if (!accessibles.contains(ot)) {
                nonAccessibleSubTypes.add(ot);
            }
        }
        return nonAccessibleSubTypes;
    }

    public ObjectType getResponsible() {
        if (isValueType()) {
            return null;
        }

        for (Role role : getFactType().roles) {
            if (role.isResponsible()) {
                return (ObjectType) role.getSubstitutionType();
            }
        }
        for (Role role : plays) {
            if (role.isMandatory()) {
                Role inverse = role.relatedRole(this);
                if (inverse != null && inverse.isResponsible()) {
                    return (ObjectType) inverse.getSubstitutionType();
                }
            }
        }
        return null;
    }

    public Set<ObjectType> getResponsibles() {
        Set<ObjectType> responsibles = new TreeSet<>();
        ObjectType responsible = getResponsible();
        while (responsible != null) {
            responsibles.add(responsible);
            responsible = responsible.getResponsible();
        }
        return responsibles;

    }

    public Relation getResponsibleRelation() {
        // if (isSuperTypeResponsible()) {
        // for (ObjectType ot : supertypes) {
        // if (ot.getResponsibleRelation() != null) {
        // return ot.getResponsibleRelation();
        // }
        // }
        // }
        for (Relation r : relations()) {
            Relation inverse = r.inverse();
            if (inverse != null && inverse.isResponsible()) {
                return inverse;
            }
        }
        return null;
    }

    ObjectType getUnmanagedSubType() {
        int countUnmanagedSubTypes = 0;
        ObjectType unmanagedSubType = null;
        for (ObjectType subType : concreteSubTypes()) {
            if (subType.getResponsible() == null) {
                countUnmanagedSubTypes++;
                unmanagedSubType = subType;
            }
        }
        if (countUnmanagedSubTypes == 1) {
            return unmanagedSubType;

        } else {
            return null;
        }
    }

    public ObjectType concreteSubType() {
        List<ObjectType> concreteSubTypes = concreteSubTypes();
        if (concreteSubTypes.size() == 1) {
            return concreteSubTypes.get(0);
        } else {
            return null;
        }

    }

    boolean isUnderControlOf(ObjectType supertype) {
        ObjectType responsible = getResponsible();
        return responsible == null || responsible == supertype;
    }

    void clearRelations() {
        relations = null;
    }

    @Override
    public List<Param> transformToBaseTypes(Param param) {
        List<Param> params = new ArrayList<>();
        if (isAbstract()) {
            params.add(param);
        } else {
            for (Relation relation : identifyingRelations()) {
                if (relation.targetType() instanceof ObjectType
                        && Objects.equals(((ObjectType) relation.targetType()).getResponsible(), this)) {
                    params.addAll(relation.targetType().transformToBaseTypes(param));
                } else {
                    SubParam subParam = new SubParam(relation.fieldName(), relation.targetType(), relation, param);
                    params.addAll(relation.targetType().transformToBaseTypes(subParam));
                }
            }
        }
        return params;
    }

    @Override
    public ActualParam getUndefined() {
        return Null.NULL;
    }

    @Override
    public boolean isAddable() {
        return getFactType().isAddable();
    }

    @Override
    public boolean isSettable() {
        return getFactType().isSettable();
    }

    @Override
    public boolean isAdjustable() {
        return getFactType().isAdjustable();
    }

    @Override
    public boolean isInsertable() {
        return getFactType().isInsertable();
    }

    @Override
    public String expressIn(Language l) {
        return getName();
    }

    @Override
    public String callString() {
        return getName();
    }

    public String getKind() {
        if (isAbstract()) {
            return "AT";
        } else if (isValueType()) {
            return "VT";
        } else {
            return "OT";
        }
    }

}
