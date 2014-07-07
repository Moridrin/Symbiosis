package equa.meta.objectmodel;

import static equa.code.ImportType.Set;
import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Source;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

/**
 *
 * @author FrankP
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class UniquenessConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private ArrayList<Role> roles;
    @Column
    private boolean intra;

    public UniquenessConstraint() {
    }

    /**
     * creation of uniqueness constraint with respect to roles, based on source;
     * constraint will be registered at all concerning roles.
     *
     * @param roles not empty and different
     * @param source
     */
    public UniquenessConstraint(List<Role> roles, RuleRequirement source) throws ChangeNotAllowedException {
        super(roles.get(0).getParent(), source);

        if (roles.isEmpty()) {
            throw new RuntimeException(("UNIQUENESS CONSTRAINT ")
                    + ("CONCERNS AT LEAST ONE ROLE"));
        }

        this.roles = new ArrayList<>(2);
        this.roles.addAll(roles);
        for (Role role : roles) {
            role.addConstraint(this);
        }

        initIntra();
    }

    /**
     * creation of single uniqueness constraint with respect to role, based on
     * source constraint will be registered at concerning role.
     *
     * @param role
     * @param source
     */
    public UniquenessConstraint(Role role, RuleRequirement source) throws ChangeNotAllowedException {
        super(role.getParent(), source);
        this.roles = new ArrayList<>(1);
        this.roles.add(role);
        intra = true;
        role.addConstraint(this);
    }

    @Override
    public String getName() {
        Set<UniquenessConstraint> ucs = getFactType().ucs();
        if (ucs.size()==1){
            return getAbbreviationCode();
        }

        int nr = 1;
        for (UniquenessConstraint uc : getFactType().ucs()) {
            if (uc == this) {
                return getAbbreviationCode() + nr;
            }
            nr++;
        }
        return null;
    }

    /**
     *
     * @return an iterator over the roles of this constraint
     */
    public Iterator<Role> roles() {
        return roles.iterator();
    }

    /**
     *
     * @return true if this constraint concerns one role, otherwise false
     */
    public boolean isSingleUniqueness() {
        return roles.size() == 1;
    }

    /**
     *
     * @return true if this constraint concerns one non qualifier role,
     * otherwise false
     */
    public boolean isSingleQualifiedUniqueness() {
        int count = 0;
        for (Role role : roles) {
            if (!role.isQualifier()) {
                count++;
            }
        }

        return count == 1;
    }

    /**
     *
     * @return the count of the concerning non qualifying roles of this
     * constraint
     */
    public int countNonQualifyingRoles() {
        int size = roles.size();
        for (Role role : roles) {
            if (role.isQualifier()) {
                size--;
            }
        }
        return size;
    }

    /**
     *
     * @return the count of the concerning roles of this constraint
     */
    public int size() {
        return roles.size();
    }

    /**
     *
     * @return true if all roles covered by this constraint belong to the same
     * facttype otherwise false
     */
    public boolean isIntraFactTypeConstraint() {
        return intra;
    }

    private void initIntra() {
        Iterator<Role> it = roles();
        FactType parent = it.next().getParent();
        intra = true;
        while (it.hasNext()) {
            if (it.next().getParent() != parent) {
                intra = false;
                return;
            }
        }
    }

    /**
     *
     * @param uc
     * @return true if the set of roles of this uniqueness is a subset of the
     * set of roles of uc
     */
    public boolean implies(UniquenessConstraint uc) {
        return uc.roles.containsAll(roles);
    }

    @Override
    public boolean clashesWith(FrequencyConstraint fc) {
        Role role = fc.getRole();
        if (roles.contains(role)) {
            return isSingleUniqueness();
        } else {
            return false;
        }
    }

    /**
     *
     * @throws equa.meta.ChangeNotAllowedException
     */
    @Override
    public void remove() throws ChangeNotAllowedException {

        for (Role role : roles) {
            role.removeUniquenessConstraint(this);
        }

        super.remove();

//        for (Source source : sources()) {
//            if (source instanceof RuleRequirement) {
//                ((RuleRequirement)source).remove();
//            }
//        }
    }

    UniquenessConstraint migrateTo(List<Role> sourceRoles, List<Role> targetRoles) {

        // detect intersection of roles and sourceRoles
        List<Integer> irrelevantRoleNumbers = new ArrayList<>();
        for (int i = 0; i < sourceRoles.size(); i++) {
            if (!roles.contains(sourceRoles.get(i))) {
                irrelevantRoleNumbers.add(i);
            }
        }

        // create a new uniquenessconstraint in behalf of related and relevant roles of targetRoles
        List<Role> relevantRoles = new ArrayList<>(targetRoles);
        for (Integer integer : irrelevantRoleNumbers) {
            relevantRoles.remove(targetRoles.get(integer));
        }
        List<Source> sources = this.sources();
        try {
            UniquenessConstraint uc = new UniquenessConstraint(relevantRoles,
                    (RuleRequirement) sources.get(0));
            for (int i = 1; i < sources.size(); i++) {
                uc.addSource(sources.get(i));
            }
            return uc;
        } catch (ChangeNotAllowedException ex) {
            return null;
        }

    }

    void merge(List<Role> mergingRoles, Role newRole) throws ChangeNotAllowedException {
        for (Role role : mergingRoles) {
            role.removeUniquenessConstraint(this);
            roles.remove(role);
        }
        roles.add(newRole);
        newRole.addConstraint(this);

    }

    void replaceRole(BaseValueRole bvr, ObjectRole or) {
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i) == bvr) {
                roles.set(i, or);
                return;
            }
        }
        throw new RuntimeException(bvr.toString() + "doesn't exist");
    }

    @Override
    public String getAbbreviationCode() {
        return "u";
    }

    void split(ObjectRole role, List<Role> idRoles) throws ChangeNotAllowedException {
        role.removeUniquenessConstraint(this);
        roles.remove(role);
        for (Role idRole : idRoles) {
            roles.add(idRole);
            idRole.addConstraint(this);
        }
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }

    @Override
    public String getRequirementText() {
        StringBuilder sb = new StringBuilder();
        FactType ft = getFactType();
        if (roles.size() == ft.getSize()) {
            if (ft.isValueType()) {
                sb.append("A value of ");
            } else if (ft.isObjectType()) {
                sb.append("Every ");
            } else {
                sb.append("Every fact about ");
            }
            sb.append(ft.getFactTypeString());
            sb.append(" is uniquely determined by the ");

            if (ft.isCollectionType()) {
                CollectionType ct = (CollectionType) ft.getObjectType();
                String role = ct.getElementRole().getNamePlusType();
                FrequencyConstraint fc = ct.getFrequencyConstraint();
                String freq;
                if (fc == null) {
                    freq = "";
                } else {
                    freq = fc.range() + " ";
                }
                if (ct.isSequence()) {
                    sb.append("sequence of ").append(freq).append("<");
                    sb.append(role);
                    sb.append(">s; order of elements does matter.");
                } else {
                    sb.append("set of ").append(freq).append("<");
                    sb.append(role);
                    sb.append(">s.");
                }
                return sb.toString();
            }

            if (roles.size() == 1) {
                sb.append("value on ");
                sb.append("<").append(roles.get(0).getNamePlusType()).append(">");
            } else {
                sb.append("combined value on ");
                sb.append("<").append(roles.get(0).getNamePlusType());
                for (int i = 1; i < roles.size(); i++) {
                    sb.append(" , ").append(roles.get(i).getNamePlusType());
                }
                sb.append(">");
            }
        } else {
            sb.append("Two different facts about ");
            sb.append(ft.getFactTypeString());
            sb.append(" with an equal ");
            if (roles.size() == 1) {
                sb.append("value on ");
                sb.append("<").append(roles.get(0).getNamePlusType()).append(">");
            } else {
                sb.append("combined value on ");
                sb.append("<").append(roles.get(0).getNamePlusType());
                for (int i = 1; i < roles.size(); i++) {
                    sb.append(" , ").append(roles.get(i).getNamePlusType());
                }
                sb.append(">");
            }
            sb.append(" is not allowed");
        }

        sb.append(".");
        return sb.toString();
    }

    @Override
    public boolean isRealized() {
        return true;
    }
}
