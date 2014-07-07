/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;

/**
 *
 * @author frankpeeters
 */
public class QualifierRelation extends Relation {

    private static final long serialVersionUID = 1L;
   

    public QualifierRelation(ObjectType owner, Role role) {
        super(owner, role);
       
    }

    @Override
    public SubstitutionType targetType() {
        return role.getQualified().getSubstitutionType();
    }

    @Override
    public FactTypeRelation inverse() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return true;
    }
    
    @Override
    public boolean isCreational() {
        return false;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public boolean isNavigable() {
        return false;
    }

    @Override
    public String asAttribute() {
        return "unknown";
    }

    @Override
    public boolean hasNoDefaultValue() {
        return role.hasDefaultValue();
    }

    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isPartOfId() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        if (hasNoDefaultValue()) {
            return null;
        } else {
            return role.getDefaultValue();
        }
    }

    @Override
    public boolean isSeqRelation() {
        return false;
    }

    @Override
    public boolean isSetRelation() {
        return false;
    }

    @Override
    public boolean hasMultipleQualifiedTarget() {
        return false;
    }

    @Override
    public boolean isMapRelation() {
        return false;
    }

    @Override
    public String fieldName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String name() {
        return role.detectRoleName();
    }

}
