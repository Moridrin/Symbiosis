/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.code.OperationHeader;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class Event extends Constraint {
    
    private static final long serialVersionUID = 1L;

    private final FactType eventSource;
    private final FactType condition;
    private boolean negation;
    private final ObjectRole conditionCaller;
    private OperationHeader eventHandler;
    private String description;

    Event(FactType eventSource, FactType condition, ObjectRole conditionCaller) {
        this.eventSource = eventSource;
        this.condition = condition;
        this.conditionCaller = conditionCaller;
        this.negation = false;
        this.eventHandler = null;
        this.description = "";
    }

    public FactType getEventSource() {
        return eventSource;
    }

    public FactType getCondition() {
        return condition;
    }

    public ObjectRole getConditionCaller() {
        return conditionCaller;
    }

    public OperationHeader getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(OperationHeader eventHandler, String description) {
        this.eventHandler = eventHandler;
        this.description = description;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    @Override
    public String getAbbreviationCode() {
        String code = Naming.withoutVowels(eventSource.getName() + ": " + conditionCaller.detectRoleName() + "." + condition.getName());

        if (negation) {
            code = "!" + code;
        }
        return code;
    }

    @Override
    public boolean isRealized() {
        return eventHandler != null;
    }

    @Override
    public String getName() {
        return eventSource.getName() + ": " + conditionCaller.detectRoleName() + "." + condition.getName();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Event) {
            Event ev = (Event) object;
            if (ev.eventSource.equals(eventSource) && ev.condition.equals(condition)
                    && ev.conditionCaller.equals(conditionCaller)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public FactType getFactType() {
        return eventSource;
    }

    @Override
    public String getRequirementText() {
        String holds = "holds";
        if (negation) {
            holds += " not";
        }
        String text
                = "As soon as " + eventSource.getName() + " changes and if "
                + conditionCaller.detectRoleName() + "." + condition.getName() + holds + ""
                + " then " + description + " will be executed.";
        return text;
    }

}
