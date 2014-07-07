package equa.meta.requirements;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import equa.meta.Model;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.project.Project;
import equa.util.NumberIssue;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;

/**
 *
 * @author FrankP
 */
@Entity(name = "RM")
public class RequirementModel extends Model implements Publisher, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rmid")
    private long requirementModelId;
    @Column(name = "domainDescription")
    private String domainDescription;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model", fetch = FetchType.LAZY)
    private Set<ActionRequirement> actions;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model", fetch = FetchType.LAZY)
    private Set<FactRequirement> facts;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model", fetch = FetchType.LAZY)
    private Set<RuleRequirement> rules;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model", fetch = FetchType.LAZY)
    private Set<QualityAttribute> qualityAttributes;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private NumberIssue requirementNumberIssue;
    @Transient
    private transient BasicPublisher bp;

    public RequirementModel() {
        actions = new TreeSet<>();
        facts = new TreeSet<>();
        rules = new TreeSet<>();
        qualityAttributes = new TreeSet<>();
        requirementNumberIssue = new NumberIssue();

        if (bp == null) {
            initPublisher();
        }
    }

    /**
     *
     * @param parent of this requirements model
     */
    public RequirementModel(Project parent) {
        super(parent);
        actions = new TreeSet<>();
        facts = new TreeSet<>();
        rules = new TreeSet<>();
        qualityAttributes = new TreeSet<>();
        requirementNumberIssue = new NumberIssue();
        initPublisher();
    }

    /**
     * @return number-issue-object of this requirements model.
     */
    public NumberIssue getRequirementNumberIssue() {
        return requirementNumberIssue;
    }

    public void setRequirementNumberIssue(NumberIssue requirementNumberIssue) {
        this.requirementNumberIssue = requirementNumberIssue;
    }

    public Requirement getRequirement(String id) {
        for (FactRequirement fr : facts) {
            if (fr.getId().equalsIgnoreCase(id)) {
                return fr;
            }
        }
        for (RuleRequirement rr : rules) {
            if (rr.getId().equalsIgnoreCase(id)) {
                return rr;
            }
        }
        for (ActionRequirement ar : actions) {
            if (ar.getId().equalsIgnoreCase(id)) {
                return ar;
            }
        }
        for (QualityAttribute qa : qualityAttributes) {
            if (qa.getId().equalsIgnoreCase(id)) {
                return qa;
            }
        }
        return null;
    }

    /**
     * It should be possible to cast the IModelMember-object (i.e., member) into
     * a Requirement-object. Moreover, this Requirement-object should be an
     * instance of either FactRequirement, RuleRequirement, ActionRequirement or
     * QualityAttribute.
     *
     * @param member of the requirements model
     * @throws RuntimeException if member is not an instance of a
     * Requirement-class extension.
     */
    @Override
    public void remove(ModelElement member) {
        Requirement requirement = (Requirement) member;
        if (requirement instanceof FactRequirement) {
            facts.remove(requirement);
        } else if (requirement instanceof RuleRequirement) {
            rules.remove(requirement);
        } else if (requirement instanceof ActionRequirement) {
            actions.remove(requirement);
        } else if (requirement instanceof QualityAttribute) {
            qualityAttributes.remove(requirement);
        } else {
            throw new RuntimeException("removing of unknown model element "
                    + "at the requirementsmodel");
        }
        if (bp != null) {
            bp.inform(this, "remReq", null, member);
        }
    }
    


    /**
     * The requirement should be an instance of either FactRequirement,
     * RuleRequirement, ActionRequirement or QualityAttribute.
     *
     * @param requirement to replace.
     * @throws RuntimeException if requirement is not an instance of a
     * Requirement-class extension.
     */
    void replace(Requirement requirement) {

        if (requirement instanceof FactRequirement) {
            replace((FactRequirement) requirement, facts);
        } else if (requirement instanceof RuleRequirement) {
            replace((RuleRequirement) requirement, rules);
        } else if (requirement instanceof ActionRequirement) {
            replace((ActionRequirement) requirement, actions);
        } else if (requirement instanceof QualityAttribute) {
            replace((QualityAttribute) requirement, qualityAttributes);
        } else {
            throw new RuntimeException("replacing of unknown model element "
                    + "at the requirementsmodel");
        }
    }

    /**
     * First, the requirement is used to remove the requirement with the same id
     * from the corresponding set of requirement extensions. Finally, the
     * requirement is added to the same set of requiremen extensions.
     *
     * @param requirement to replace.
     * @param reqs set of requirements in this model
     */
    private <A extends Requirement> void replace(A requirement, Set<A> reqs) {
        // removing of requirement with same id
        reqs.remove(requirement);
        bp.inform(this, "remReq", null, requirement);
        // adding of actual requirement
        reqs.add(requirement);
        bp.inform(this, "newReq", null, requirement);
    }

    /**
     * @return domain description in this requirements model.
     */
    public String getDomainDescription() {
        return domainDescription;
    }

    /**
     * @param domainDescription in this requirements model.
     */
    public void setDomainDescription(String domainDescription) {
        this.domainDescription = domainDescription;
    }

    /**
     * adding of action to this model. The action is added if and only if the
     * action is not found in the set of actions.
     *
     * @param cat (category) of the action requirement.
     * @param text non empty.
     * @param source of the action requirement, of external input.
     * @return the created requirement
     * @throws RuntimeException if the text is empty.
     */
    public ActionRequirement addActionRequirement(Category cat, String text, ExternalInput source) {
        if (text.isEmpty()) {
            throw new RuntimeException("text of action requirement cannot be empty");
        }
        ActionRequirement action;
        action = (ActionRequirement) searchFor(text, actions);
        if (action == null) {
            action = new ActionRequirement(requirementNumberIssue.nextNumber(cat.getCode()), cat, text,
                    source, this);
            action.setModel(this);
            actions.add(action);
            if (bp != null) {
                bp.inform(this, "newReq", null, action);
            }
        }
        return action;
    }

    /**
     * adding of fact to this model. The fact is added if and only if the fact
     * is not found in the set of facts.
     *
     * @param cat (category) of the action requirement.
     * @param text non empty
     * @param source of the action requirement, of external input.
     * @return the created requirement
     * @throws RuntimeException if the text is empty.
     */
    public FactRequirement addFactRequirement(Category cat, String text, ExternalInput source) {
        if (text.isEmpty()) {
            throw new RuntimeException("text of fact requirement cannot be empty");
        }
        FactRequirement fact;
        fact = (FactRequirement) searchFor(text, facts);
        if (fact == null) {
            fact = new FactRequirement(requirementNumberIssue.nextNumber(cat.getCode()), cat, text,
                    source, this);
            fact.setModel(this);
            facts.add(fact);
            if (bp != null) {
                bp.inform(this, "newReq", null, fact);
            }
        }
        return fact;
    }

    /**
     * The text is assumed to be text of a requirement, so it is searched over
     * the texts available in reqs.
     *
     * @param text of requirement.
     * @param reqs set of requirements.
     * @return requirement-object with the text, or null if not found.
     */
    private Requirement searchFor(String text, Set<? extends Requirement> reqs) {
        Iterator<? extends Requirement> it = reqs.iterator();
        while (it.hasNext()) {
            Requirement req = it.next();
            if (req.getText().equalsIgnoreCase(text)) {
                return req;
            }
        }
        return null;
    }

    /**
     * The text is assumed to be text of a requirement, so it is searched over
     * the texts of all requirements in the model.
     *
     * @param text of requirement.
     * @return requirement-object with the text, or null if not found.
     */
    public Requirement searchFor(String text) {
        Requirement req;
        req = searchFor(text, actions);
        if (req == null) {
            req = searchFor(text, facts);
            if (req == null) {
                req = searchFor(text, rules);
                if (req == null) {
                    req = searchFor(text, qualityAttributes);
                }
            }
        }

        return req;
    }

    /**
     * adding of rule to this model. If another rule, namely rule B, already has
     * the text, then rule B is returned as the created requirement.
     *
     * @param cat (category) of the rule requirement.
     * @param text is not Empty
     * @param source of the rule requirement, of external input.
     * @return the created requirement
     * @throws RuntimeException if the text is empty.
     */
    public RuleRequirement addRuleRequirement(Category cat, String text,
            ExternalInput source) {
        if (text.isEmpty()) {
            throw new RuntimeException("text of rule may not be empty.");
        }
        for (RuleRequirement rule : rules) {
            if (rule.getText().equalsIgnoreCase(text)) {
                return rule;
            }
        }
        RuleRequirement rule = new RuleRequirement(requirementNumberIssue.nextNumber(cat.getCode()), cat, text,
                source, this);
        rule.setModel(this);
        rules.add(rule);
        if (bp != null) {
            bp.inform(this, "newReq", null, rule);
        }
        return rule;

    }

    /**
     * adding of attribute to this model. If another attribute, namely attribute
     * B, already has the text, then attribute B is returned as the created
     * requirement.
     *
     * @param cat (category) of the quality requirement.
     * @param text non empty
     * @param source of the quality requirement, of external input.
     * @return the created requirement
     * @throws RuntimeException if the text is empty.
     */
    public QualityAttribute addQualityAttribute(Category cat, String text,
            ExternalInput source) {
        if (text.isEmpty()) {
            throw new RuntimeException("text of quality attribute cannot be empty");
        }

        for (QualityAttribute qa : qualityAttributes) {
            if (qa.getText().equalsIgnoreCase(text)) {
                return qa;
            }
        }
        QualityAttribute qa = new QualityAttribute(requirementNumberIssue.nextNumber(cat.getCode()), cat, text,
                source, this);
        qa.setModel(this);
        qualityAttributes.add(qa);
        bp.inform(this, "newReq", null, qa);
        return qa;

    }

    /**
     *
     * @return an iterator over all actions of this model
     */
    public Iterator<ActionRequirement> actions() {
        return actions.iterator();
    }

    /**
     *
     * @return an iterator over all facts of this model
     */
    public Iterator<FactRequirement> facts() {
        return facts.iterator();
    }

    /**
     *
     * @return an iterator over all rules of this model
     */
    public Iterator<RuleRequirement> rules() {
        return rules.iterator();
    }

    /**
     *
     * @return an iterator over all quality attributes of this model
     */
    public Iterator<QualityAttribute> attributes() {
        return qualityAttributes.iterator();
    }

    /**
     *
     * @return an iterator over all requirements of this model
     */
    public Iterator<Requirement> requirements() {
        TreeSet<Requirement> reqs = new TreeSet<Requirement>(actions);
        reqs.addAll(facts);
        reqs.addAll(rules);
        reqs.addAll(qualityAttributes);
        return reqs.iterator();
    }

    /**
     * all requirements will be renumbered; the orderering of the requirements
     * (conform the definition in compareTo) will not be disturbed
     */
    public void renumberRequirements() {
        requirementNumberIssue = new NumberIssue();
        for (Requirement req : actions) {
            req.setNr(requirementNumberIssue.nextNumber(req.getCategory().getCode()));
        }
        for (Requirement req : facts) {
            req.setNr(requirementNumberIssue.nextNumber(req.getCategory().getCode()));
        }
        for (Requirement req : rules) {
            req.setNr(requirementNumberIssue.nextNumber(req.getCategory().getCode()));
        }
        for (Requirement req : qualityAttributes) {
            req.setNr(requirementNumberIssue.nextNumber(req.getCategory().getCode()));
        }
    }

    /**
     * @param pl
     * @param string
     * @todo document
     */
    @Override
    public void addListener(PropertyListener pl, String string) {
        if (bp == null) {
            initPublisher();
        }
        bp.addListener(pl, string);
    }

    /**
     * @param pl
     * @param string
     * @todo document
     */
    @Override
    public void removeListener(PropertyListener pl, String string) {
        bp.removeListener(pl, string);
    }

    /**
     * Initiates the BasicPublisher-object for this requirements-model-object.
     */
    private void initPublisher() {
        bp = new BasicPublisher(new String[]{"newReq", "remReq"});
    }

    /**
     * @param cat (category) of requirements.
     * @return true if at least 1 requirement in the model uses the input cat.
     */
    public boolean makesUseOf(Category cat) {

        for (Requirement req : actions) {
            if (req.getCategory().compareTo(cat) == 0) {
                return true;
            }
        }

        for (Requirement req : facts) {
            if (req.getCategory().compareTo(cat) == 0) {
                return true;
            }
        }

        for (Requirement req : rules) {
            if (req.getCategory().compareTo(cat) == 0) {
                return true;
            }
        }

        for (Requirement req : qualityAttributes) {
            if (req.getCategory().compareTo(cat) == 0) {
                return true;
            }
        }
        return false;
    }

    public long getRequirementsModelId() {
        return requirementModelId;
    }

    public void setRequirementsModelId(long requirementsModelId) {
        this.requirementModelId = requirementsModelId;
    }

    @Override
    public String getName() {
        return "RequirementModel";
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    public void removeSys() {
        Set<Requirement> toRemove = new TreeSet<>();

        for (ActionRequirement action : actions) {
            if (action.getCategory().equals(Category.SYSTEM)) {
                toRemove.add(action);
            }
        }
        actions.removeAll(toRemove);
        
        toRemove = new TreeSet<>();
        for (FactRequirement fact : facts) {
            if (fact.getCategory().equals(Category.SYSTEM)) {
                toRemove.add(fact);
            }
        }
        facts.removeAll(toRemove);
        
        toRemove = new TreeSet<>();
        for (RuleRequirement rule : rules) {
            if (rule.getCategory().equals(Category.SYSTEM)) {
                toRemove.add(rule);
            }
        }
        rules.removeAll(toRemove);
    }
}
