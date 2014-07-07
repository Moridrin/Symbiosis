/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.code.operations.MapType;
import equa.meta.objectmodel.CBTRole;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.Constraint;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.FrequencyConstraint;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.objectmodel.UniquenessConstraint;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public abstract class Relation implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     *
     */
    protected ObjectType owner;
    /**
     *
     */
    protected Role role;
    private final boolean multiple;
    private final boolean mandatory;
    private final String multiplicity;

    /**
     * Constructor. Multiplicity values depend on the role.
     *
     * @param owner , that is, OT that is owner of this relation.
     * @param role that is played by the OT.
     */
    public Relation(ObjectType owner, Role role) {
        this.owner = owner;
        this.role = role;
        multiplicity = role.getMultiplicity();
        multiple = role.isMultiple();
        mandatory = role.isMandatory();
    }

    /**
     *
     * @return the object type where this relation to belongs
     */
    public ObjectType getOwner() {
        return owner;
    }

    /**
     *
     * @return the multiplicity of the corresponding association or attribute
     */
    public String multiplicity() {
        return multiplicity;
    }

    public int getMinFreq() {
        FrequencyConstraint constraint = role.getFrequencyConstraint();
        if (constraint == null) {
            if (isMandatory()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return constraint.getMin();
        }
    }

    public Constraint getLowerConstraint() {
        Constraint constraint = role.getFrequencyConstraint();
        if (constraint == null) {
            return role.getMandatoryConstraint();
        } else {
            return constraint;
        }
    }

    public int getMaxFreq() {
        FrequencyConstraint constraint = role.getFrequencyConstraint();
        if (constraint == null) {
            List<UniquenessConstraint> ucs = role.ucs();
            if (ucs.isEmpty()) {
                return Integer.MAX_VALUE;
            } else if (ucs.get(0).isSingleUniqueness()) {
                return 1;
            } else {
                return Integer.MAX_VALUE;
            }
        } else {
            return constraint.getMax();
        }
    }

    public Constraint getUpperConstraint() {
        Constraint constraint = role.getFrequencyConstraint();
        if (constraint == null) {
            List<UniquenessConstraint> ucs = role.ucs();
            if (ucs.isEmpty()) {
                return null;
            } else if (ucs.get(0).isSingleUniqueness()) {
                return ucs.get(0);
            } else {
                return null;
            }
        } else {
            return constraint;
        }
    }

    public int multiplicityLower() {
        return getMinFreq();
    }

    public int multiplicityUpper() {
        return getMaxFreq();
    }

    /**
     *
     * @return target substitution type
     */
    public abstract SubstitutionType targetType();

    /**
     *
     * @return the attribute-string: name and type of the attribute
     */
    public abstract String asAttribute();

    /**
     *
     * @return name of relation
     */
    public abstract String name();

    /**
     *
     * @return the role name of the corresponding association or attribute
     */
    public String roleName() {
        return name();
    }

    public String getPluralName() {
        return role.getPluralName();
    }

    /**
     *
     * @return true if this relation must be defined, otherwise false
     */
    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isCollectionReturnType() {
        if (isMapRelation() || (isSeqRelation() && (isSettable() || isAdjustable()))) {
            return hasMultipleQualifiedTarget();
        } else {
            return hasMultipleTarget();
        }
    }

    public boolean mustHaveValue() {
        if (isMapRelation()) {
            FrequencyConstraint fc = role.getFrequencyConstraint();
            if (fc != null) {
                List<Role> qualifiers = this.qualifierRoles();
                int count = 1;
                for (Role role : qualifiers) {
                    if (role instanceof CBTRole) {
                        CBTRole cbtRole = (CBTRole) role;
                        int c = ((ConstrainedBaseType) cbtRole.getSubstitutionType()).getValueConstraint().countAcceptedElements();
                        if (c == -1) {
                            return false;
                        }
                        count *= c;
                    }
                }
                return count == fc.getMax() - fc.getMin() + 1;
            } else {
                return false;
            }

        } else {
            return isMandatory();
        }
    }

    /**
     *
     * @return true if this relation could be manyfold, otherwise false; if this
     * relation isSeqRelation then the multiplicity is evaluated per index
     */
    public boolean hasMultipleTarget() {
        return multiple;
    }

    public boolean hasMultipleQualifiedTarget() {
        return false;
    }

    /**
     *
     * @return true if this could be derived on base of other relations
     */
    public boolean isDerivable() {
        return role.isDerivable();
    }

    /**
     *
     * @return
     */
    public boolean isQualifier() {
        return role.isQualifier();
    }

    public boolean isSetRelation() {
        return hasMultipleTarget() && qualifierRoles().isEmpty();
    }

    public abstract boolean isSeqRelation();

    public boolean isMapRelation() {
        return role.isMappingRole();
    }

    /**
     *
     * @return derivable text (if any) of the role's parent.
     */
    public String getDerivableText() {
        if (isDerivable()) {
            return role.getParent().getDerivableText();
        } else {
            return null;
        }
    }

    /**
     *
     * @return true if this relation is a composition ( origin of this relation
     * is parent in a lifetime dependency with the target of the relation)
     */
    public boolean isComposition() {
        return role.isComposition();
    }

    /**
     *
     * @return true if the inverse relation is mandatory, otherwise false
     */
    //public abstract boolean isAnchorRelation();
    /**
     *
     * @return true if information about this relation isn't published by the
     * concerning class, else false
     */
    public boolean isHidden() {
        return role.isHidden();
    }

    /**
     *
     * @return true if the value of the link(s) cannot be changed, else false;
     */
    public abstract boolean isFinal();

    /**
     *
     * @param uc uniqueness constraint
     * @return true if uc is used by this relation otherwise false
     */
    public boolean restrictedBy(UniquenessConstraint uc) {
        return role.contains(uc);
    }

    /**
     *
     * @param idRole to be compared with the role of this relation.
     * @return true if underlying role equals idRole, otherwise false
     */
    public boolean refersTo(Role idRole) {
        return idRole == role;
    }

    /**
     *
     * @return true if the class of this relation knows about this relation
     */
    public abstract boolean isNavigable();

    /**
     * @return the inverse relation, null if inverse relation comes from a
     * basetype
     */
    public abstract Relation inverse();

    /**
     *
     * @return true if this relation must result in setter, otherwise false
     */
    public abstract boolean isSettable();

    /**
     *
     * @return true if this relation must result in adjust method, otherwise
     * false
     */
    public abstract boolean isAdjustable();

    /**
     * False is automatically returned if object is not a Relation instance.
     *
     * @param object to compare with this relation.
     * @return true if the role of object is == to the role of this relation.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Relation) {
            Relation relation = (Relation) object;
            return relation.role.equals(role);
        }
        return false;
    }

    /**
     *
     * @return true if this relation must result in an add-method, otherwise
     * false
     */
    public boolean isAddable() {
        return role.isAddable();
    }

    /**
     *
     * @return
     */
    public boolean isInsertable() {
        return role.isInsertable();
    }

    /**
     *
     * @return true if this relation must result in a remove-method, otherwise
     * false
     */
    public boolean isRemovable() {
        return role.isRemovable();
    }

    /**
     *
     * @return compositional responsible, if exists, otherwise null,
     */
    public ObjectType compositionalRemovable() {

        Relation responsible = getOwner().getResponsibleRelation();
        if (responsible == null) {
            return null;
        }
        Relation responsibleInverse = responsible.inverse();
        if (responsibleInverse.isComposition() && responsibleInverse.isRemovable()) {
            return (ObjectType) responsible.targetType();
        }
        return null;

    }

    /**
     *
     * @return true if this relation is responsible for changes, otherwise false
     */
    public boolean isResponsible() {
        return isSettable() || isAdjustable() || isAddable() || isRemovable() || isInsertable() || isComposition();
    }

    public boolean isCreational() {
        if (isResponsible()) {
            Relation inverse = inverse();
            if (inverse != null && inverse.isMandatory()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     *
     * @return fact type parent of the role of this relation.
     */
    public FactType getParent() {
        return role.getParent();
    }

    /**
     *
     * @return true if the role of this relation has default name.
     */
    public boolean hasDefaultName() {
        return role.hasDefaultName();
    }

    /**
     *
     * @return negation of {@link Role#hasDefaultName() }
     */
    public abstract boolean hasNoDefaultValue();

    public abstract String getDefaultValue();

    public boolean isAutoIncr() {
        return role.isAutoIncr();
    }

    /**
     *
     * @return see {@link Role#isSeqNr()}
     */
    public boolean isSeqNr() {
        return role.isSeqNr();
    }

    /**
     *
     * @return
     */
    public List<Role> qualifierRoles() {
        return role.getParent().qualifiersOf(role);
    }

    public abstract boolean isPartOfId();

    /**
     * pre: relation.isMultiple()
     *
     * @return the prescribed collection type of this relation
     */
    public CT collectionType() {
        List<Role> qualifiers = qualifierRoles();
        if (qualifiers.isEmpty()) {
            return new CT(CollectionKind.SET, targetType());
        } else {
            if (qualifiers.size() > 1) {
                return hashmap(qualifiers, 0);
            } else {
                Role qualifier = qualifiers.get(0);
                if (qualifier.isSeqNr()) {
                    return new CT(CollectionKind.LIST, targetType());

                } else {
                    return new MapType(qualifier.getSubstitutionType(), targetType());
                }
            }
        }
    }

    MapType hashmap(List<Role> qualifiers, int fromIndex) {
        Role qualifier = qualifiers.get(fromIndex);
        if (fromIndex == qualifiers.size() - 1) {
            return new MapType(qualifier.getSubstitutionType(), targetType());
        } else {
            return new MapType(qualifier.getSubstitutionType(), hashmap(qualifiers, fromIndex + 1));
        }
    }

    public abstract String fieldName();
    
    
    public String getAutoIncrField(){
        return null;
    }

}
