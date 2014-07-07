package equa.meta.objectmodel;

import equa.code.IndentedList;
import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.code.operations.Param;
import equa.code.operations.STorCT;
import equa.meta.ChangeNotAllowedException;
import equa.meta.classrelations.FactTypeRelation;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ExternalInput;
import equa.util.Naming;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class DerivableConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private String text;
    private OperationHeader oh;

    public DerivableConstraint() {
    }

    DerivableConstraint(FactType parent, RuleRequirement rule, String text) {
        super(parent, rule);
        
        this.text = text;
        checkOH(); 
    }

    public void setText(ExternalInput source, String text) throws ChangeNotAllowedException {
        if (text.equals(this.text)) {
        } else {
            getFactType().getDerivableConstraint().setText(source, text);
        }
    }

    public String getText() {
        return text;
    }

    public OperationHeader getOperationHeader() {
        checkOH();
        return oh;
    }

    private void checkOH() {
        FactType ft = getFactType();

        ObjectRole role = ft.getNavigableRole();
        if (role == null) {
            if (ft.isObjectType()) {
                throw new UnsupportedOperationException("Factory is still missing");
            } else {
                // if role = null then there are no or too many navigable roles 
                throw new RuntimeException("there are no or too many navigable roles within derivable fact type "+ ft.getName() );
            }
        }
        ObjectType ot = role.getSubstitutionType();
        List<Role> qualifiers = ft.qualifiersOf(role);
        List<Param> params = new ArrayList<>();
        if (role.isQualified()) {
            for (Role r : qualifiers) {
                params.add(new Param(r.detectRoleName(), r.getSubstitutionType(), new FactTypeRelation(ot, r)));
            }
        }
        String operationName;
        STorCT returnType;
        if (ft.size() - qualifiers.size() == 1) {
            // boolean operation     
            operationName = Naming.withoutCapital(ft.getName());
            returnType = BaseType.BOOLEAN;
        } else {
            Role counterpart = ft.counterpart(role);
            if (counterpart != null) {
                if (role.hasSingleTarget()) {
                    returnType = counterpart.getSubstitutionType();
                    operationName = counterpart.detectRoleName();
                } else {
                    returnType = new FactTypeRelation(ot, role).collectionType();
                    operationName = counterpart.getPluralName();
                }
            } else {
                return;
            }
        }

        if (oh != null) 
        {
            OperationHeader oh2 = new MetaOH(operationName, params.isEmpty(), false, returnType, params);
            if (!oh.equals(oh2)) 
            {
                ot.removeAlgorithm(oh);
                oh = ot.addAlgorithm(operationName, params.isEmpty(), false, returnType,
                        params, new IndentedList());
            }
        } 
        else 
        {
            oh = ot.addAlgorithm(operationName, params.isEmpty(), false, returnType,
                    params, new IndentedList());
        }

    }

    @Override
    public String getAbbreviationCode() {
        return "der";
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        FactType ft = getFactType();
        if (ft.getDerivableConstraint() != null) {
            ft.deleteDerivableConstraint();
            super.remove();
        }
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }

    @Override
    public String getRequirementText() {
        return text;
    }

    @Override
    public boolean isRealized() {
        Role responsible = getFactType().getResponsibleRole();
        if (responsible == null || oh == null) {
            return false;
        } else {
            ObjectType ot = (ObjectType) responsible.getSubstitutionType();
            return !ot.getAlgorithm(oh).isEditable();
        }
    }
}
