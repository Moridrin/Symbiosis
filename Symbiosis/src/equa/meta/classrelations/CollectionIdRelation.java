/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.meta.objectmodel.Constraint;
import equa.meta.objectmodel.FrequencyConstraint;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.objectmodel.UniquenessConstraint;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class CollectionIdRelation extends IdRelation {

    private static final long serialVersionUID = 1L;
    private Role collectionRole;

    public CollectionIdRelation(ObjectType owner, Role elementRole) {
        super(owner, elementRole);
        collectionRole = role.getParent().counterpart(role);
    }

    @Override
    public SubstitutionType targetType() {
        return role.getSubstitutionType();
    }

    @Override
    public String asAttribute() {
        return "- " + name() + " : " + new CT(CollectionKind.COLL, targetType());
    }

    @Override
    public Relation inverse() {

        return new FactTypeRelation(getOwner(), collectionRole);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isQualifier() {
        return false;
    }

    /**
     *
     * @return see {@link Role#isSeqNr()}
     */
    @Override
    public boolean isSeqNr() {
        return false;
    }

    @Override
    public String multiplicity() {
        return collectionRole.getMultiplicity();
    }

    @Override
    public int getMinFreq() {
        FrequencyConstraint constraint = collectionRole.getFrequencyConstraint();
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

    @Override
    public Constraint getLowerConstraint() {
        Constraint constraint = collectionRole.getFrequencyConstraint();
        if (constraint == null) {
            return collectionRole.getMandatoryConstraint();
        } else {
            return constraint;
        }
    }

    @Override
    public int getMaxFreq() {
        FrequencyConstraint constraint = collectionRole.getFrequencyConstraint();
        if (constraint == null) {
            List<UniquenessConstraint> ucs = collectionRole.ucs();
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

    @Override
    public Constraint getUpperConstraint() {
        Constraint constraint = collectionRole.getFrequencyConstraint();
        if (constraint == null) {
            List<UniquenessConstraint> ucs = collectionRole.ucs();
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

    @Override
    public boolean isMandatory() {
        return collectionRole.isMandatory();
    }

    @Override
    public boolean hasMultipleTarget() {
        return !collectionRole.hasSingleTarget();
    }

    @Override
    public String fieldName() {
        return role.getPluralName();
    }

}
