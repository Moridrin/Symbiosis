/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import equa.code.Language;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.ObjectModel;

/**
 *
 * @author frankpeeters
 */
public class Call implements ActualParam {

    private static final long serialVersionUID = 1L;

    private final Operation operation;
    private final List<? extends ActualParam> actualParams;
    private ActualParam called;

    public Call() {
        actualParams = null;
        operation = null;
    }

    public Call(Operation operation, List<? extends ActualParam> actualParams) {
        this.operation = operation;
        this.actualParams = actualParams;
        called = null;
        if (operation.getCodeClass().getParent() instanceof ObjectModel) {
        	called = (ActualParam) operation.getCodeClass().getParent();
        }
    }

    public String returnValue() {
        return callString();
    }

    public Call(Operation operation) {
        this.operation = operation;
        if (operation instanceof OperationWithParams) {
            actualParams = ((OperationWithParams) operation).getParams();
        } else {
            actualParams = new ArrayList<>();
        }
    }

    public Call setCalled(ActualParam called) {
        this.called = called;
        return this;
    }

    public List<ActualParam> getActualParams() {
        return Collections.unmodifiableList(actualParams);
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String expressIn(Language l) {
        if (operation instanceof Property) {
            Property p = (Property) operation;
            if (p.getReturnType().getType() instanceof ConstrainedBaseType) {
                return l.getProperty(p.getName()) + l.memberOperator() + l.getProperty("value");
            } else {
                return l.getProperty(p.getName());
            }
        } else {
            return l.callMethod((called != null ? called.expressIn(l) : ""), operation.getName(), actualParams);
        }
    }

    String paramlist() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Iterator<? extends ActualParam> it = actualParams.iterator();
        if (it.hasNext()) {
            sb.append(it.next().callString());
            while (it.hasNext()) {
                sb.append(",");
                sb.append(it.next().callString());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String callString() {
        StringBuilder sb = new StringBuilder();
        if (called != null) {
            sb.append(called.callString());
            sb.append(".");
        } else if (operation.isClassMethod()) {
            sb.append(operation.getCodeClass().getName());
            sb.append(".");
        }

        if (actualParams == null) {
            sb.append(operation.callString());
        } else {
            sb.append(operation.callString(actualParams));
        }

        return sb.toString();
    }
}
