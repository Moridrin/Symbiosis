package equa.meta.objectmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ParentElement;
import equa.util.Naming;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;

/**
 *
 * @author FrankP
 */
@Entity
public abstract class Role extends ParentElement implements Serializable, Publisher {

    private static final long serialVersionUID = 1L;
    @Id
    private long id;
    @Column
    private String name;
    @OneToMany(cascade = CascadeType.PERSIST)
    private final List<UniquenessConstraint> ucs;
    @OneToOne
    private MandatoryConstraint mc;
    @OneToMany(cascade = CascadeType.PERSIST)
    private final List<StaticConstraint> otherConstraints;
    @ManyToOne(cascade = CascadeType.PERSIST)
    protected Role qualified;
    @Column
    private boolean hidden;
    @Transient
    protected BasicPublisher publisher;

    public Role() {
        ucs = new ArrayList<>(1);
        otherConstraints = new ArrayList<>(1);
        publisher = new BasicPublisher(new String[]{"name", "nr", "ucsIterator",
            "mandatory", "otherConstraintsIterator", "substitutionType", "composition",
            "frozenId", "settable", "addable", "removable", "navigable", "autoincr", "qualifier",
            "hidden", "frozen", "hiddenId", "frozenId"});

    }

    /**
     *
     * @param type
     */
    Role(FactType parent) {
        super(parent);
        name = "";
        ucs = new ArrayList<>(1);
        mc = null;
        otherConstraints = new ArrayList<>(1);
        this.qualified = null;
        this.hidden = false;
        publisher = new BasicPublisher(new String[]{"name", "nr", "ucsIterator",
            "mandatory", "otherConstraintsIterator", "substitutionType", "composition",
            "frozenId", "settable", "addable", "removable", "navigable", "autoincr", "qualifier",
            "hidden", "frozen", "hiddenId", "frozenId"});
    }

    /**
     *
     * @return the substitutiontype of this role
     */
    public abstract SubstitutionType getSubstitutionType();

    public abstract boolean isAutoIncr();

    public abstract boolean isCandidateAutoIncr();

    /**
     *
     * @return name of role; in case of an unknown rolename: an empty string
     * will be returned
     */
    public String getRoleName() {
        return name;
    }

    public String getPluralName() {
        String roleName = detectRoleName();
        return Naming.plural(roleName);
    }

    @Override
    public String getName() {
        return detectRoleName();
    }

    public abstract String getConstraintString();

    public boolean hasDefaultName() {
        return name.isEmpty() || name.equalsIgnoreCase(getSubstitutionType().getName());
    }

    /**
     *
     * @return if role name is empty the role number will be returned else the
     * name
     */
    public String getRoleNameOrNr() {
        if (name.isEmpty()) {
            return getNr() + ("");
        } else {
            return name;
        }
    }

    /**
     *
     * @return the number of the role
     */
    public int getNr() {
        return getParent().searchRoleNr(this);
    }

    /**
     *
     * @return a list with all uniqueness constraints belonging to this role
     */
    public List<UniquenessConstraint> ucs() {
        ArrayList<UniquenessConstraint> copy = new ArrayList<>(ucs);
        return copy;
    }

    @Override
    public String toString() {
        String str = getConstraintString();
        if (str.isEmpty()) {
            return getNamePlusType();
        } else {
            return getNamePlusType() + " {" + str + "}";
        }
    }

    /**
     *
     * @return the multiplicity of this role
     */
    public String getMultiplicity() {
        // multiplicity must be calculated on base of constraints
        // start firstly with the most wide ranged multiplicity bounds
        String lower = ("0");
        String upper = ("*");

        // check single uniqueness constraint
        for (UniquenessConstraint uc : ucs) {
            if (uc.isSingleUniqueness()) {
                upper = ("1");
            }
        }

        // check mandatory constraint
        if (mc != null) {
            lower = ("1");
        }

        // check frequency constraint
        for (StaticConstraint constraint : otherConstraints) {
            if (constraint instanceof FrequencyConstraint) {
                FrequencyConstraint frequency = (FrequencyConstraint) constraint;
                lower = frequency.getMin() + ("");
                upper = frequency.getMax() + ("");
            }
        }

        if (lower.equals(upper)) {
            return lower;
        } else {
            return lower + ("..") + upper;
        }
    }

    public boolean isMultiple() {
        for (UniquenessConstraint uc : ucs) {
            if (uc.isSingleUniqueness()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSingleTarget() {
        return !isMultiple() || !isMultipleQualified();
    }

    public boolean isMultipleQualified() {
        for (UniquenessConstraint uc : ucs) {
            if (uc.isSingleQualifiedUniqueness()) {
                return false;
            }
        }
        return true;

    }

    /**
     *
     * @param name
     */
    void setRoleName(String name) {
        this.name = Naming.withoutCapital(name);
        getParent().fireListChanged();
        publisher.inform(this, "name", null, name);
    }

    /**
     *
     * @return true if the substitutiontype is not interested about his
     * involvement in this role otherwise false
     */
    public abstract boolean isNavigable();

    /**
     *
     * @return true if the substitutiontype of this role has a
     * composition-responsibility, otherwise false
     */
    public abstract boolean isComposition();

    /**
     *
     * @return true if isComposition() or the role offers the oppoprtunity to
     * create an object of the role otherwise false
     */
    public abstract boolean isCreational();

    /**
     * the substitutaiontype of this role gets a composition-responsibility (if
     * composition = true)
     *
     * @param composition
     * @return true if setting is executed, false if setting is rejected
     */
    public abstract boolean setComposition(boolean composition);

    /**
     * if this role is played by an objecttype then the change will be executed
     * otherwise the setting will be neglected
     *
     * @param navigable
     */
    public abstract void setNavigable(boolean navigable);

    /**
     * if (qualified is null) the qualifier will be removed
     *
     * @param qualified != this role && qualified must have the same parent as
     * this role and qualified.isNavigable() && qualified must possess common
     * uniqueness with this role && !isComposition() && parent of qualified is
     * not an ElementsFactType; qualified null is allowed
     */
    public void setQualifier(ObjectRole qualified) {
        if (getParent() instanceof ElementsSequenceFactType) {
            throw new RuntimeException(("QUALIFIED ROLE of a role of an elements factype related to a sequence cannot be changed"));
        }
        if (this.qualified == qualified) {
            return;
        }

        if (qualified == null) {
            this.qualified = null;
            getParent().fireListChanged();
            publisher.inform(this, "qualifier", null, false);
            return;
        }

        if (this == qualified) {
            throw new RuntimeException(("QUALIFIED ROLE MUST BE ANOTHER ROLE AS THIS ROLE"));
        }
        if (getParent() != qualified.getParent()) {
            throw new RuntimeException(("QUALIFIED ROLE MUST BELONG TO THE SAME PARENT"));
        }
        if (!qualified.isNavigable()) {
            throw new RuntimeException(("QUALIFIED ROLE MUST BE NAVIGABLE"));
        }
        if (!qualified.hasCommonUniquenessWith(this)) {
            throw new RuntimeException(("QUALIFIED ROLE MUST HAVE COMMON "
                    + "UNIQUENESS WITH CONCERNING QUALIFIER ROLE"));
        }
        if (isComposition()) {
            throw new RuntimeException(("QUALIFIER ROLE CANNOT BE a "
                    + "COMPOSITION ROLE "));
        }

        this.qualified = qualified;
        getParent().fireListChanged();
        publisher.inform(this, "qualifier", null, true);
    }

    public boolean isCandidateQualifier() {
        return isMultiple() && !isComposition() && !getParent().isValueType()
                && !(getParent() instanceof ElementsSetFactType);
    }

    /**
     * constraint will be added to role, every uniqueness constraint which is
     * implied by the added constraint will be removed firstly
     *
     * @param constraint
     * @throws ChangeNotAllowedException if the adding is not allowed conform
     * the rules of the concerning constraints
     */
    void addConstraint(UniquenessConstraint constraint) throws ChangeNotAllowedException {
        ArrayList<UniquenessConstraint> toRemove = new ArrayList<>();
        for (UniquenessConstraint uc : ucs) {
            if (uc.implies(constraint)) {
                throw new ChangeNotAllowedException(("ADDED UNIQUENESS ")
                        + ("CONSTRAINT IS REDUNDANT"));
            }
            if (constraint.implies(uc)) {
                // uc will come redundant
                toRemove.add(uc);
            }
        }
        for (StaticConstraint oc : otherConstraints) {
            if (oc.clashesWith(constraint)) {
                throw new ChangeNotAllowedException(("CONSTRAINT ")
                        + constraint.getId()
                        + (" IS IN CONFLICT WITH EXISTING CONSTRAINT ")
                        + oc.getId());
            }
        }

        for (UniquenessConstraint uc : toRemove) {
            uc.remove();
        }
        ucs.add(constraint);
        getParent().fireListChanged();
        publisher.inform(this, "ucsIterator", null, ucs.iterator());

    }

    /**
     * constraint will be added to role, if there exists already a mandatory
     * constraint an exception will be raised;
     *
     * @param constraint
     * @throws ChangeNotAllowedException if the adding is not allowed conform
     * the rules of the concerning constraints
     */
    void addConstraint(MandatoryConstraint constraint) throws ChangeNotAllowedException {
        if (mc != null) {
            throw new ChangeNotAllowedException(("CONSTRAINT ")
                    + constraint.getId() + (" IS IN CONFLICT WITH EXISTING CONSTRAINT ")
                    + mc.getId());
        }

        for (StaticConstraint oc : otherConstraints) {
            if (oc.clashesWith(constraint)) {
                throw new ChangeNotAllowedException(("CONSTRAINT ")
                        + constraint.getId()
                        + (" IS IN CONFLICT WITH EXISTING CONSTRAINT ")
                        + oc.getId());
            }
        }

        mc = constraint;
        getParent().fireListChanged();
        publisher.inform(this, "mandatory", null, isMandatory());

    }

    /**
     * constraint will be added to role
     *
     * @param constraint
     * @throws ChangeNotAllowedException if the adding is not allowed conform
     * the rules of the concerning constraints
     */
    void addConstraint(FrequencyConstraint constraint) throws ChangeNotAllowedException {
        for (UniquenessConstraint uc : ucs) {
            if (uc.clashesWith(constraint)) {
                throw new ChangeNotAllowedException(("CONSTRAINT ")
                        + constraint.getId()
                        + (" IS IN CONFLICT WITH EXISTING CONSTRAINT ")
                        + uc.getId());
            }
        }

        for (StaticConstraint oc : otherConstraints) {
            if (oc.clashesWith(constraint)) {
                throw new ChangeNotAllowedException(("CONSTRAINT ")
                        + constraint.getId()
                        + (" IS IN CONFLICT WITH EXISTING CONSTRAINT ")
                        + oc.getId());
            }
        }
        deleteMandatoryConstraint();
        otherConstraints.add(constraint);
        getParent().fireListChanged();

    }

    public FrequencyConstraint getFrequencyConstraint() {
        for (Constraint constraint : otherConstraints) {
            if (constraint instanceof FrequencyConstraint) {
                return (FrequencyConstraint) constraint;
            }
        }
        return null;
    }

    public void setFrequencyConstraint(int min, int max, RuleRequirement rule) throws ChangeNotAllowedException {
        if (min < 0 || max < 2 || max < min) {
            throw new RuntimeException("frequency constraint with invalid range: "
                    + min + ".." + max);
        }

        FrequencyConstraint fc = null;
        for (Constraint constraint : otherConstraints) {
            if (constraint instanceof FrequencyConstraint) {
                fc = (FrequencyConstraint) constraint;
            }
        }
        if (fc != null) {
            fc.remove();
            otherConstraints.remove(fc);
        }
        fc = new FrequencyConstraint(this, min, max, rule);
        addConstraint(fc);
    }

    /**
     * constraint will be added to role
     *
     * @param constraint
     * @throws ChangeNotAllowedException if the adding is not allowed conform
     * the rules of the concerning constraints
     */
    void addConstraint(SetConstraint constraint) throws ChangeNotAllowedException {
        otherConstraints.add(constraint);
        getParent().fireListChanged();

    }

    /**
     * constraint will be added to role
     *
     * @param constraint
     * @throws ChangeNotAllowedException if the adding is not allowed conform
     * the rules of the concerning constraints
     */
    void addConstraint(TupleConstraint constraint) throws ChangeNotAllowedException {
        otherConstraints.add(constraint);
        getParent().fireListChanged();

    }

    /**
     *
     * @param constraint
     */
    void removeUniquenessConstraint(UniquenessConstraint constraint) throws ChangeNotAllowedException {
//        if (this.isQualified() || this.isQualifier()) {
//            throw new ChangeNotAllowedException("uniqueness constraint can not be removed because of presence "
//                    + "of qualifier role");
//        }
        ucs.remove(constraint);
        getParent().fireListChanged();
        publisher.inform(this, "ucsIterator", null, ucs.iterator());
    }

    /**
     * deletion of the mandatory constraint
     */
    public void deleteMandatoryConstraint() {
        if (mc != null) {
            mc = null;
            publisher.inform(this, "mandatory", null, false);
        }
    }

    /**
     *
     * @return true if all values of the substitutiontype of this role must play
     * a role in the concerning facttype, else false (a basetype as
     * substitutiontype doesn't possess a mandatory constraint)
     */
    public boolean isMandatory() {
        if (getSubstitutionType().isValueType() && isNavigable()) {
            return true;
        }
        String mult = getMultiplicity();
        if (mult.startsWith("0") || mult.startsWith("*")) {
            return false;
        } else {
            return true;
        }
    }

    void removeOtherConstraint(StaticConstraint constraint) {
        otherConstraints.remove(constraint);
        publisher.inform(this, "otherConstraintsIterator", null, otherConstraints.iterator());
    }

    /**
     *
     * @return the facttype who is parent of this role
     */
    @Override
    public FactType getParent() {
        return (FactType) super.getParent();
    }

    /**
     *
     * @param ot
     * @return the counterpart role of this role, but if this role is part of an
     * objectified fact type or a fact type with one non-qualifier role null
     * will be returned
     */
    public Role relatedRole(ObjectType ot) {
        if (inside(ot)) {
            return null;
        } else if (getParent().isObjectType()) {
            return null;
        } else if (getParent().size() == 1) {
            return null;
        } else {
            return getParent().counterpart(this);
        }
    }

    /**
     *
     * @param ot
     * @return if this object cannot refer to a relation with ot an exception
     * will be raised else the related substitution type of the relation
     * starting at ot will be returned
     */
    public String relatedRoleName(ObjectType ot) {
        if (inside(ot)) {
            if (name.isEmpty()) {
                return Naming.withoutCapital(getSubstitutionType().getName());
            } else {
                return name;
            }
        }

        String roleName;
        if (getParent().isObjectType()) {
            if (name.isEmpty()) {
                roleName = getParent().getName();
            } else {
                roleName = name + getParent().getName();
            }
        } else if (getParent().size() == 1) {
            roleName = getParent().getName();
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart.name.isEmpty()) {
                roleName = Naming.withoutCapital(counterpart.getSubstitutionType().getName());
            } else {
                roleName = counterpart.name;
            }
        }
        return Naming.withoutCapital(roleName);
    }

    /**
     *
     * @param ot
     * @return if this object cannot refer to a relation call up with ot an
     * exception will be raised else the multiplicity of the referred relation
     * starting at ot conform UML-format will be returned
     */
    public String relatedMultiplicity(ObjectType ot) {
        if (inside(ot)) {
            return ("1");
        } else {
            return getMultiplicity();
        }
    }

    boolean inside(ObjectType ot) {
        if (getParent().equals(ot.getFactType())) {
            // this role is an identifying role of ot
            return true;
        }
        return false;
    }

    boolean withUniquenessConstraint() {
        return !ucs.isEmpty();
    }

    /**
     *
     * @return the size of the smallest uc; if uniqueness is missing then size
     * of parent + 1 will be returned
     */
    int sizeOfSmallestUC() {
        int smallest = getParent().size() + 1;
        for (UniquenessConstraint uc : ucs) {
            if (uc.countNonQualifyingRoles() < smallest) {
                smallest = uc.countNonQualifyingRoles();
            }
        }
        return smallest;
    }

    /**
     *
     * @return an iterator over all constraints of this role
     */
    public Iterator<StaticConstraint> constraints() {
        List<StaticConstraint> list = new ArrayList<StaticConstraint>(ucs);
        if (mc != null) {
            list.add(mc);
        }
        list.addAll(otherConstraints);
        return list.iterator();
    }

    /**
     *
     * @return true if the parent objecttype of this role is abstract, else
     * false (if parent is not an objecttype, always false will be returned)
     */
    public abstract boolean isAbstract();

    /**
     * setting of the substitutiontype of this role
     *
     * @param ot is a supertype of the current substitutionType
     */
    public abstract void expandSubstitutionType(ObjectType ot);

    abstract SubstitutionType disconnect();

    abstract void reconnect();

    String getParamName() {
        if (name == null && name.isEmpty()) {
            return Naming.withoutCapital(getSubstitutionType().getName());
        } else {
            return name;
        }
    }

    public String getNamePlusType() {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase(getSubstitutionType().getName())) {
            return getSubstitutionType().getName();
        }
        return getRoleName() + " : " + getSubstitutionType().getName();
    }

    /**
     *
     * @param otherRole
     * @return if this role and otherRole do possess a common uniqueness
     * constraint then return true else return false
     */
    public boolean hasCommonUniquenessWith(List<Role> otherRoles) {
        ArrayList<UniquenessConstraint> relevantUcs = new ArrayList<>(ucs);
        for (int i = 0; i < otherRoles.size(); i++) {
            relevantUcs.retainAll(otherRoles.get(i).ucs);
        }

        return !relevantUcs.isEmpty();
    }

    public boolean hasCommonUniquenessWith(Role otherRole) {
        ArrayList<UniquenessConstraint> relevantUcs = new ArrayList<>(ucs);
        relevantUcs.retainAll(otherRole.ucs);
        return !relevantUcs.isEmpty();
    }

    /**
     *
     * @return if this role plays a qualifying role with respect to one or more
     * other roles of this parent facttype return true else return false
     */
    public boolean isQualifier() {
        if (qualified == null) {
            return false;
        } else {
            return qualified.isNavigable() && qualified.hasCommonUniquenessWith(this);
        }
    }

    public boolean isQualified() {
        return getParent().checkIsQualified(this);
    }

    /**
     * deletion of all uniquenesse constraints
     */
    void deleteUniquenessConstraints() throws ChangeNotAllowedException {
        if (ucs.isEmpty()) {
            return;
        }

        if (getParent().hasCompositionRole() && !isComposition()) {
            return;
        }

        for (int i = 0; i < ucs.size(); i++) {
            UniquenessConstraint uc = ucs.get(i);
            uc.remove();
        }
        ucs.clear();
        publisher.inform(this, "ucs", null, ucs.iterator());
    }

    /**
     *
     * @return if this role is a qualifier role then the qualified role will be
     * returned otherwise null
     */
    public ObjectRole getQualified() {
        return (ObjectRole) qualified;
    }

    /**
     *
     * @return in case of unknown rolename default-name else rolename
     */
    public String detectRoleName() {
        if (name.isEmpty()) {
            if (!getParent().isObjectType() && getParent().size() == 1) {
                return Naming.withoutCapital(getParent().getName());
            } else {
                SubstitutionType st = getSubstitutionType();
                if (st instanceof ObjectType) {
                    ObjectType ot = (ObjectType) st;
                    if (ot.isAbstract()) {
                        ObjectType concreteSubType = ot.concreteSubType();
                        if (concreteSubType != null) {
                            st = concreteSubType;
                        }
                    }
                }
                return Naming.withoutCapital(st.getName());
            }
        } else {
            return name;
        }
    }

    /**
     *
     * @param uc
     * @return true if uc is within the uniqueness constraints of this role,
     * otherwise false
     */
    public boolean contains(UniquenessConstraint uc) {
        for (StaticConstraint constraint : ucs) {
            if (constraint == uc) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return role is derivable if the whole facttype is derivable
     */
    public boolean isDerivable() {
        return getParent().getDerivableText() != null || (isQualifier() && getParent().isObjectType());
    }

    boolean containsUniqueness() {
        return ucs.size() > 0;
    }

    /**
     *
     * @return true, if the value of this role is not published (= only internal
     * known), otherwise false
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * if (hidden=true) the value of this role will not be published, otherwise
     * the value of this role will be published
     *
     * @param hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        getParent().fireListChanged();
        publisher.inform(this, "hidden", null, isHidden());
    }

    /**
     *
     * @return true, if the value of this role cannot be changed after initial
     * setting otherwise false
     */
    public boolean isFinal() {
        return !getParent().isMutable();
    }

    /**
     *
     * @return true if this role needs a setter-operation, otherwise false
     */
    public abstract boolean isSettable();

    /**
     *
     * @return true if this role needs an adjust-operation, otherwise false
     */
    public abstract boolean isAdjustable();

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
     * @return true if this role is a qualifier, which qualifies by number,
     * otherwise false;
     */
    public abstract boolean isSeqNr();

    /**
     *
     * @return true if the substitutiontype of this role has the opportunity to
     * add values, else false
     */
    public abstract boolean isAddable();

    /**
     *
     * @return true if the substitutiontype of this role has the opportunity to
     * remove values, else false
     */
    public abstract boolean isRemovable();

    /**
     *
     * @return if substitutiontype of role is responsible for changing fact
     * true, else false
     */
    public boolean isResponsible() {
        return !getParent().isValueType() && (isAddable() || isRemovable() || isSettable() || isInsertable() || isAdjustable() || isComposition());
    }

    abstract boolean isResponsibleForNonVT();

    public boolean isCandidateResponsible() {
        Role reponsibleRole = getParent().getResponsibleRole();
        return reponsibleRole == null || reponsibleRole == this;
    }

    public abstract boolean isCandidateAddable();

    public abstract boolean isCandidateAdjustable();

    public abstract boolean isCandidateSettable();

    public abstract boolean isCandidateInsertable();

    public abstract boolean isCandidateRemovable();

    public abstract boolean isCandidateComposition();

    @Override
    public void remove() throws ChangeNotAllowedException {
        qualified = null;
        for (UniquenessConstraint uc : new ArrayList<>(ucs)) {
            uc.remove();
        }
        ucs.clear();
        if (mc != null) {
            mc.remove();
            mc = null;
        }
        for (StaticConstraint constraint : new ArrayList<>(otherConstraints)) {
            constraint.remove();
        }
        otherConstraints.clear();
        super.remove();
    }

    public abstract boolean hasDefaultValue();

    public abstract String getDefaultValue();

    public MandatoryConstraint getMandatoryConstraint() {
        return this.mc;
    }

    public ObjectRole parentOfProperty() {
        if (getParent().isObjectType()) {
            return null;
        } else {
            if (getParent().size() == 1) {
                return null;
            } else {
                Role counterpart = getParent().counterpart(this);
                if (counterpart == null) {
                    return null;
                } else {
                    ((ObjectType) counterpart.getSubstitutionType()).getFactType();
                }
            }
        }
        return null;
    }

    public abstract boolean isInsertable();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isRestrictedByFrequencyConstraint() {
        for (Constraint constraint : otherConstraints) {
            if (constraint instanceof FrequencyConstraint) {
                return true;
            }
        }
        return false;
    }

    void injectConstraints(Role otherRole) {
        // mandatorial
        if (otherRole.isMandatory()) {
            try {
                new MandatoryConstraint(this,
                        (RuleRequirement) otherRole.getMandatoryConstraint().sources().get(0));
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(Role.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // seqnr
//            if (otherRole.isQualifier()) {
//                thisRole.setQualifier((ObjectRole) this.roles.get(otherRole.getQualified().getNr()));
//            }
        // composition, addable, removeable, settable
//            if (otherRole.isComposition()) {
//                setComposition(true);
//            }
//
//            if (otherRole.isAddable()) {
//                ActionRequirement rule = (ActionRequirement) ((ObjectRole) otherRole).getAddable().sources().get(0);
//                ((ObjectRole) thisRole).addAddable(rule);
//            }
//
//            if (otherRole.isRemovable()) {
//                ActionRequirement rule = (ActionRequirement) ((ObjectRole) otherRole).getRemovable().sources().get(0);
//                ((ObjectRole) thisRole).addRemovable(rule);
//            }
//            if (otherRole.isSettable()) {
//                ActionRequirement rule = (ActionRequirement) ((ObjectRole) otherRole).getSettable().sources().get(0);
//                ((ObjectRole) thisRole).addSettable(rule);
//            }
    }

    public abstract boolean isMappingRole();
    
    public SubstitutionType targetSubstitutionType() {
        if (getParent().isObjectType()) return getParent().getObjectType();
        else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart==null) return null;
            else {
                return counterpart.getSubstitutionType();
            }
        }
    }

}
