/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.project.ProjectRole;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ObjectRole extends Role {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private ObjectType ot;
    @Column
    private boolean navigable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private SettablePermission settable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private AdjustablePermission adjustable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private AddablePermission addable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private RemovablePermission removable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private InsertablePermission insertable;
    @Column
    private boolean composition;
    @Transient
    private transient ObjectType toDisconnect;

    public ObjectRole() {
    }

    public ObjectRole(ObjectType ot, FactType parent) {
        super(parent);
        this.ot = ot;
        navigable = true;
        settable = null;
        adjustable = null;
        removable = null;
        insertable = null;
        if (ot.isSingleton()) {
            composition = true;
        } else {
            composition = false;
            addable = null;
        }
        toDisconnect = null;
        ot.involvedIn(this);
    }

    public AddablePermission getAddable() {
        return addable;
    }

    public RemovablePermission getRemovable() {
        return removable;
    }

    public SettablePermission getSettable() {
        return settable;
    }

    public AdjustablePermission getAdjustable() {
        return adjustable;
    }

    public InsertablePermission getInsertable() {
        return insertable;
    }

    @Override
    public boolean isAddable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return addable != null && isNavigable() && !isDerivable() && isMultiple();
    }

    @Override
    public boolean isInsertable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return insertable != null && isNavigable() && !isDerivable() && isMultiple();
    }

    @Override
    public ObjectType getSubstitutionType() {
        return ot;
    }

    @Override
    public boolean isNavigable() {
        if (isQualifier()) {
            return false;
        }
        return navigable;
    }

    @Override
    public void setNavigable(boolean navigable) {
        if (this.navigable == navigable) {
            return;
        }

        if (navigable == false) {
            this.navigable = false;
            try {
                if (addable != null) {

                    addable.remove();

                }
                if (removable != null) {
                    removable.remove();
                }
                if (settable != null) {
                    settable.remove();
                }
                if (adjustable != null) {
                    adjustable.remove();
                }
                if (insertable != null) {
                    insertable.remove();
                }
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(ObjectRole.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.navigable = true;
        }

        getParent().correctQualifyingRoles(
                this);
        getParent().fireListChanged();

        publisher.inform(
                this, "navigable", null, navigable);
    }

    @Override
    public boolean isAbstract() {
        return ot.isAbstract();
    }

    @Override
    SubstitutionType disconnect() {
        toDisconnect = ot;
        toDisconnect.resignFrom(this);
        return toDisconnect;
    }

    @Override
    void reconnect() {
        ot = toDisconnect;
        toDisconnect = null;
        ot.involvedIn(this);
    }

    @Override
    public boolean isComposition() {
        if (composition) {
            if (!getParent().isObjectType()) {
                // fact type relation
                Role counterPart = getParent().counterpart(this);
                if (counterPart == null) {
                    // there exists no role which can be managed by the composition
                    return false;
                }
                // only object roles can be managed by composition; multiplicity should be 1
                return (counterPart instanceof BaseValueRole)
                        || (counterPart.getMultiplicity().equals("1"));
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setComposition(boolean composition) {
        if (composition == this.composition) {
            return false;
        }

        //   if (getParent().qualifiersOf(this).isEmpty()) {
        if (composition) {
            if (isCandidateComposition()) {
                this.composition = true;
                // default: relation can be added and removed
                addAddable("composition relation goes normally along with adding facilities");
                addRemovable("composition relation goes normally along with removing facilities");
                Iterator<Role> itRoles = getParent().roles();
                while (itRoles.hasNext()) {
                    Role otherRole = itRoles.next();
                    if (otherRole != this && otherRole instanceof ObjectRole) {
                        ObjectRole objectRole = (ObjectRole) otherRole;
                        objectRole.deleteSettable();
                        objectRole.deleteAdjustable();
                        objectRole.deleteInsertable();
                        objectRole.deleteAddable();
                        objectRole.deleteRemovable();
                    }
                }
                getParent().fireListChanged();
                publisher.inform(this, "composition", null, true);
                return true;
            } else {
                return false;
            }
        } else {
            this.composition = false;
            getParent().fireListChanged();
            publisher.inform(this, "composition", null, false);
            return true;
        }

    }

    @Override
    public void deleteMandatoryConstraint() {
        Role counterpart = getParent().counterpart(this);
        if (counterpart==null || !counterpart.isComposition()) {
            super.deleteMandatoryConstraint();
        }
    }

    @Override
    public boolean isSettable() {
        return settable != null && !isDerivable() && isNavigable();
    }

    @Override
    public boolean isAdjustable() {
        return adjustable != null && !isDerivable() && isNavigable();
    }

    @Override
    public boolean isRemovable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return removable != null && isNavigable() && !isDerivable()
                && (isMultiple() || !isMandatory());
    }

    /**
     * settable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     *
     */
    public void addSettable(String justification) {
        if (isCandidateSettable()) {
            if (settable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement rule = rm.addActionRequirement(getCategory(),
                        "XX",
                        new ExternalInput(justification, projectRole));

                this.settable = new SettablePermission(this, rule);
                getParent().fireListChanged();
            }
        }
    }

    private Category getCategory() {
        return getParent().getCategory();
    }

    public void addAdjustable(String justification) {
        if (isCandidateAdjustable()) {
            if (adjustable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement action = rm.addActionRequirement(getCategory(),
                        "XX",
                        new ExternalInput(justification, projectRole));

                this.adjustable = new AdjustablePermission(this, action);
                getParent().fireListChanged();
            }
        }
    }

    public void addInsertable(String justification) {
        if (isCandidateInsertable()) {
            if (insertable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement action = rm.addActionRequirement(getCategory(),
                        "Some actor of the (sub)system must get the opportunity "
                        + "to insert a fact of " + getParent().getFactTypeString()
                        + " later on.",
                        new ExternalInput(justification, projectRole));
                this.insertable = new InsertablePermission(this, action);
                getParent().fireListChanged();
            }
        }
    }

    void deleteSettable() {
        if (settable != null) {
            settable = null;
            publisher.inform(this, "settable", null, isSettable());
        }
    }

    void deleteAdjustable() {
        if (adjustable != null) {
            adjustable = null;
        }
    }

    void deleteInsertable() {
        if (insertable != null) {
            insertable = null;
        }
    }

    @Override
    public boolean isCandidateAddable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return !ot.isValueType() && isMultiple() && (this.isResponsible() || this.isCandidateResponsible());
    }

    @Override
    public boolean isCandidateAdjustable() {
        if (!hasSingleTarget() || ot.isValueType()) {
            return false;
        }
        Role counterpart = getParent().counterpart(this);
        if (counterpart == null || !counterpart.getSubstitutionType().isNumber()) {
            return false;
        } else {
            return this.isResponsible() || this.isCandidateResponsible();
        }
    }

    @Override
    public boolean isCandidateSettable() {
        if (!hasSingleTarget() || ot.isValueType()) {
            return false;
        }
        return this.isResponsible() || this.isCandidateResponsible();
    }

    @Override
    public boolean isCandidateInsertable() {
        List<Role> qualifiers = getParent().qualifiersOf(this);
        if (qualifiers.isEmpty() || ot.isValueType()) {
            return false;
        } else {
            FrequencyConstraint fc = getFrequencyConstraint();
            if (fc != null && fc.getMax() == fc.getMin()) {
                return false;
            }
            return this.isResponsible() || this.isCandidateResponsible();
        }
    }

    @Override
    public boolean isCandidateRemovable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return !ot.isValueType() && (this.isMultiple() || !this.isMandatory()) && (this.isResponsible() || this.isCandidateResponsible());
    }

    @Override
    public boolean isCandidateComposition() {
        if (isComposition()) {
            return true;
        }

        Role counterpart = getParent().counterpart(this);

        if (counterpart != null) {
            if (counterpart.isComposition()) {
                return false;
            }
            if (!counterpart.getSubstitutionType().isValueType()) {
                if (!counterpart.isMandatory() || counterpart.isMultiple()) {
                    return false;
                }
            }
            return true;

        } else {
            return getParent().isObjectType();
        }
    }

    SubstitutionType targetType() {
        if (getParent().isObjectType()) {
            return getParent().getObjectType();
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart == null) {
                return BaseType.BOOLEAN;
            } else {
                return counterpart.getSubstitutionType();
            }
        }
    }

    /**
     * addable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     */
    public final void addAddable(String justification) {
        if (isCandidateAddable()) {
            if (addable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                SubstitutionType target = targetType();
                Category cat = null;
                if (target instanceof BaseType) {
                    cat = getCategory();
                } else {
                    cat = ((ObjectType) target).getFactType().getCategory();
                }
                ActionRequirement action = rm.addActionRequirement(cat,
                        "Some actor of the system must get the opportunity to add a "
                        + target.getName(),
                        new ExternalInput(justification, projectRole));
                addable = new AddablePermission(this, action);
                publisher.inform(this, "addable", null, isAddable());
            }
        }
    }

    void deleteAddable() {
        if (addable != null) {
            addable = null;
            publisher.inform(this, "addable", null, isAddable());
        }
    }

    /**
     * removable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     *   * @param cat category of removable constraint
     */
    public void addRemovable(String justification) {
        if (isCandidateRemovable()) {
            if (removable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                SubstitutionType target = targetType();
                Category cat = null;
                if (target instanceof BaseType) {
                    cat = getCategory();
                } else {
                    cat = ((ObjectType) target).getFactType().getCategory();
                }
                ActionRequirement action = rm.addActionRequirement(cat,
                        "Some actor of the system must get the opportunity to remove a "
                        + target.getName(),
                        new ExternalInput(justification, projectRole));
                this.removable = new RemovablePermission(this, action);
                getParent().fireListChanged();
            }
        }
    }

    void deleteRemovable() {
        if (removable != null) {
            removable = null;
            publisher.inform(this, "removable", null, isRemovable());
        }
    }

    @Override
    public boolean isSeqNr() {
        return false; //ot.isSuitableAsIndex();
    }

//    @Override
//    public void setSeqNr(boolean seqNr) {
//    }
    /**
     *
     * @param at must be a supertype of actual substitutiontype of this role
     */
    void generalize(ObjectType at) {
        this.ot.resignFrom(this);
        this.ot = at;
        at.involvedIn(this);
    }

    /**
     * preconditions: a) substitutiontype must possess exactly one subtype b)
     * substitutiontype must be abstract
     */
    void specialize() {
        ObjectType subtype = ot.subtypes().next();
        this.ot.resignFrom(subtype);
        this.ot = subtype;
        subtype.involvedIn(this);
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        if (addable != null) {
            addable.remove();
            addable = null;
        }

        if (removable != null) {
            removable.remove();
            removable = null;
        }

        if (settable != null) {
            settable.remove();
            settable = null;
        }

        if (adjustable != null) {
            adjustable.remove();
            adjustable = null;
        }
        if (insertable != null) {
            insertable = null;
        }

        this.ot.resignFrom(this);
        super.remove();

    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String getConstraintString() {
        Iterator<StaticConstraint> it = constraints();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next().getDescription()).append(" ");
        }
        if (isQualifier()) {
            if (isSeqNr()) {
                sb.append("seq ");
            } else {
                sb.append("map ");
            }
        }
        if (!isNavigable()) {
            sb.append("!nav ");
        } else {
            if (isHidden()) {
                sb.append("hid ");
            }
            if (isAddable()) {
                sb.append("add ");
            }
            if (isSettable()) {
                sb.append("set ");
            }
            if (isAdjustable()) {
                sb.append("adj ");
            }
            if (isInsertable()) {
                sb.append("ins ");
            }
            if (isRemovable()) {
                sb.append("rem ");
            }
            if (isComposition()) {
                sb.append("comp ");
            }
        }
        if (isHidden()) {
            sb.append("hid ");
        }

        return sb.toString().trim();

    }

    public void setSubstitutionType(ObjectType selected) throws ChangeNotAllowedException {
        if (ot.hasSuperType(selected) || ot.hasSubType(selected)) {
            String oldName = ot.getName();
            ot.resignFrom(this);

            ot = selected;
            ot.involvedIn(this);

            // trial to improve the name of the fact type
            ObjectModel om = (ObjectModel) getParent().getParent();
            int from = getParent().getName().indexOf(oldName);
            if (from != -1) {
                String name = getParent().getName();
                String newName = name.substring(0, from) + ot.getName() + name.substring(from + oldName.length());
                try {
                    om.renameFactType(getParent(), newName);
                } catch (DuplicateException exc) {
                }
            }

        } else {
            throw new ChangeNotAllowedException("selected is not a super or subtype of " + ot.getName());
        }
    }

    @Override
    public void expandSubstitutionType(ObjectType ot) {
        try {
            setSubstitutionType(ot);
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectRole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    boolean isResponsibleForNonVT() {
        if (!isNavigable()) {
            return false;
        }

        FactType parent = getParent();
        if (parent.isObjectType()) {
            return isResponsible();
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart != null) {
                // binary fact type
                SubstitutionType st = counterpart.getSubstitutionType();
                return isResponsible() && !st.isTerminal() && !st.isValueType();
            } else {
                return false;
            }
        }
    }

    @Override
    public void remove(ModelElement member) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "Role " + getNamePlusType();
    }

    @Override
    public boolean equals(Object object) {
        return object == this;
    }

    @Override
    public boolean isCreational() {
        if (isComposition()) {
            return true;
        }

        boolean creational = (isAddable() || isInsertable() || isSettable());
        if (getParent().isObjectType()) {
            return creational;
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart == null) {
                return false;
            }
            return creational && counterpart.isMandatory();
        }
    }

    @Override
    public boolean isAutoIncr() {
        return false;
    }

    @Override
    public boolean isCandidateAutoIncr() {
        return false;
    }

    @Override
    public boolean isMappingRole() {
        if (isQualifier()) {
            return false;
        }
        List<Role> qualifiers = getParent().qualifiersOf(this);
        if (qualifiers.isEmpty()) {
            return false;
        } else {
            return qualifiers.size() > 1 || !qualifiers.get(0).isSeqNr();
        }

    }

   
}
