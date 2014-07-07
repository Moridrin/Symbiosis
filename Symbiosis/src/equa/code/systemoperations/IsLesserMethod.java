/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.systemoperations;

import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.operations.IBooleanOperation;
import equa.code.operations.Method;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ModelElement;

/**
 *
 * @author frankpeeters
 */
public class IsLesserMethod extends Method implements IBooleanOperation {

    public IsLesserMethod(ObjectModel om, ModelElement source) {
        super(om, null, null, source);
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

    @Override
    public RuleRequirement getRuleRequirement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initSpec() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ImportType> getImports() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndentedList getCode(Language l) {
        // TODO Auto-generated method stub
        return null;
    }
}
