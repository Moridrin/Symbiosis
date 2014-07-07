package equa.meta.objectmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import equa.meta.requirements.Requirement;
import equa.meta.traceability.ParentElement;

/**
 *
 * @author FrankP
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class StaticConstraint extends Constraint implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @Column
    protected String id;

    public StaticConstraint() {
    }

    /**
     *
     * @param source
     */
    StaticConstraint(ParentElement parent, Requirement source) {
        super(parent, source);
        initId(parent);
    }

    private void initId(ParentElement parent) {
        ParentElement pe = parent.getParent();
        while (!(pe instanceof ObjectModel)) {
            pe = pe.getParent();
        }
        this.id = getAbbreviationCode()
                + ((ObjectModel) pe).getConstraintNumberIssue().nextNumber(getAbbreviationCode());
    }

    /**
     * every constraint has his abbreviation code followed by a number;
     * numbering is done per subtype
     *
     * @return the unique constraint code of this constraint
     */
    public String getId() {
        return this.id;
    }

    /**
     *
     * @param uc
     * @return true if this constraint is in conflict with uc, otherwise false
     */
    public boolean clashesWith(UniquenessConstraint uc) {
        return false;
    }

    /**
     *
     * @param uc
     * @return true if this constraint is in conflict with mc, otherwise false
     */
    public boolean clashesWith(MandatoryConstraint mc) {
        return false;
    }

    /**
     *
     * @param fc
     * @return true if this constraint is in conflict with fc, otherwise false
     */
    public boolean clashesWith(FrequencyConstraint fc) {
        return false;
    }

    /**
     *
     * @param sc
     * @return true if this constraint is in conflict with sc, otherwise false
     */
    public boolean clashesWith(SubsetConstraint sc) {
        return false;
    }

    /**
     *
     * @param ec
     * @return true if this constraint is in conflict with ec, otherwise false
     */
    public boolean clashesWith(ExclusionConstraint ec) {
        return false;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public boolean equals(Object member) {
        if (member instanceof StaticConstraint) {
            return id.equals(((StaticConstraint) member).id);
        } else {
            return false;
        }
    }
}
