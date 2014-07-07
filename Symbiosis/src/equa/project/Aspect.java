/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

/**
 *
 * @author frankpeeters
 */
public enum Aspect {

    ActionToDo("Action requirement manually todo"),
    ActionRedundant("Action requirement manually redundant"),
    ActionGenerated("Action requirement generated"),
    Fact("Fact requirement manually"),
    RuleToDo("Rule requirement manually todo"),
    RuleRedundant("Rule requirement manually redundant"),
    RuleGenerated("Rule requirement generated"),
    FactTypes("Fact Types (not Object Type)"),
    ObjectTypes("Normal Object Types"),
    Registries("Registries"),
    AbstractObjectTypes("Abstract Object Types"),
    ValueTypes("Value Types (incl enums)"),
    Associations("Associations with Object Types (not Value Type)"),
    Attributes("Attributes: Base Type or Value Type"),
    Operations("Generated operations"),
    Inheritance("Number of inheritance relations");

    private final String description;

    Aspect(String descr) {
        this.description = descr;
    }
    
    @Override
    public String toString() {
        return description;
    }

}
