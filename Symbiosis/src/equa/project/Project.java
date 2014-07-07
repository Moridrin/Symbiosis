/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import equa.code.Language;
import equa.code.NameSpace;
import equa.diagram.cd.ClassDiagram;
import equa.diagram.ord.deprecated.ObjectRoleDiagram;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.util.DataSet;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;

/**
 * Class to represent a Project, which stores the participants and models. <br>
 * This class contains constrained C[reate], constrained/unconstrained
 * R[etrieve], constrained/unconstrained U[pdate], constrained/unconstrained
 * D[elete] and unconstrained E[xecute] operations. Some operations are of more
 * than one kind, e.g., Create and Update.
 *
 * @author FrankP
 */
@Entity
public class Project implements Serializable, Publisher {

    /**
     * SerialVersion
     */
    private static final long serialVersionUID = 1L;
    @Id
    private String name;
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar createdAt;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private ProjectRole createdBy;
    @Transient
    private transient ProjectRole currentUser;
    @Column(name = "SavedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar savedAt;
    @Transient
    private File file;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private ObjectModel om;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private RequirementModel rm;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> categories;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Vocabulary vocabulary;
    @OneToOne(cascade = CascadeType.PERSIST)
    private ProjectRoles projectRoles;
    @Transient
    private ArrayList<ObjectRoleDiagram> objectRoleDiagrams;
    @OneToMany(cascade = CascadeType.ALL)
    private Collection<ClassDiagram> classDiagrams;
    @Transient
    private transient BasicPublisher publisher;
    @Column
    private NameSpace root;

    private Language lastUsedLanguage = Language.JAVA;

    /**
     * DEFAULT CONSTRUCTOR; WARNING: creates a new instance of Project without
     * the definition of its name and {@link ProjectRole} creator.
     */
    public Project() {
    }

    /**
     * CONSTRUCTOR. A Project will be created by a project-member with the name
     * <code>nameCreator</code>. The {@code name} and {@code nameCreator} should
     * not be empty Strings.
     *
     * @param name for this Project.
     * @param nameCreator is the name of the creator of this Project.
     * @param roleCreator is the role of the creator of this Project.
     * @param isProjectMember specifies if the creator is project-member.
     */
    public Project(String name, String nameCreator, String roleCreator, boolean isProjectMember) {
        if (name.isEmpty()) {
            throw new RuntimeException("project name may not be empty");
        }
        if (nameCreator.isEmpty()) {
            throw new RuntimeException("name creator may not be empty");
        }
        initProject(name, nameCreator, roleCreator, isProjectMember);
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link ProjectRole} in this Project, for the current user.
     */
    public ProjectRole getCurrentUser() {
        return currentUser;
    }

    /**
     * Unconstrained UPDATE and EXECUTE operation. The {@code participant} is
     * utilized to update the current user of this Project. A
     * {@link BasicPublisher} informs this update.
     *
     * @param participant in the Project, represented by his/her
     * {@link ProjectRole}.
     */
    public void setCurrentUserAndInform(ProjectRole participant) {
        currentUser = participant;
        if (publisher != null) {
            publisher.inform(this, "currentUser", null, currentUser);
        }
    }

    /**
     * Unconstrained UPDATE and EXECUTE operation. The current user is confirmed
     * as current used of this Project. A {@link BasicPublisher} informs this
     * update.
     */
    public void setCurrentUserAndInform() {
        currentUser = (ProjectRole) projectRoles.getSelectedItem();
        if (publisher != null) {
            publisher.inform(this, "currentUser", null, currentUser);
        }
    }

    /*
     * Private CREATE operation.
     * This method completes the logic of the {@link #Project(java.lang.String, java.lang.String, java.lang.String, boolean) Project}
     * constructor.
     */
    private void initProject(String name, String nameCreator, String roleCreator, boolean isProjectMember) {
        this.name = name;
        createdAt = new GregorianCalendar();
        savedAt = null;
        file = null;
        publisher = new BasicPublisher(new String[]{"currentUser"});
        om = new ObjectModel(this);
        rm = new RequirementModel(this);
        categories = new TreeSet<>();
        categories.add(new Category("DEF", "Default", this));
        categories.add(Category.SYSTEM);
        vocabulary = new Vocabulary(om);
        objectRoleDiagrams = new ArrayList<>();
        classDiagrams = new TreeSet<>();

        projectRoles = new ProjectRoles(this);
        if (isProjectMember) {
            createdBy = projectRoles.addProjectMember(nameCreator, roleCreator);
        } else {
            createdBy = projectRoles.addStakeholder(nameCreator, roleCreator);
        }
        setCurrentUserAndInform(createdBy);

    }

    public static Project getProject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        Project project = (Project) in.readObject();
        in.close();
        project.publisher = new BasicPublisher(new String[]{"currentUser"});
        project.objectRoleDiagrams = new ArrayList<>();
        project.getObjectModel().addSystemMethods();
        return project;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link ObjectModel} of this Project.
     */
    public ObjectModel getObjectModel() {
        return om;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link RequirementModel} of this Project.
     */
    public RequirementModel getRequirementModel() {
        return rm;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link Vocabulary} of this Project.
     */
    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link ProjectRoles} of this Project.
     */
    public ProjectRoles getParticipants() {
        return projectRoles;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the date of creation of this Project.
     */
    public String getCreatedAt() {
        String createdAtDateTime = createdAt.get(Calendar.DAY_OF_MONTH) + " "
                + createdAt.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
                + createdAt.get(Calendar.YEAR) + " "
                + createdAt.get(Calendar.HOUR) + ":"
                + createdAt.get(Calendar.MINUTE) + ":"
                + createdAt.get(Calendar.SECOND);
        return createdAtDateTime;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link ProjectRole} who created this Project.
     */
    public ProjectRole getCreatedBy() {
        return createdBy;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link File} that should store this Project.
     */
    public File getFile() {
        return file;
    }

    /**
     * Unconstrained UPDATE operation.
     *
     * @param file that should store this Project.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return name of this Project.
     */
    public String getName() {
        return name;
    }

    /**
     * Unconstrained UPDATE operation.
     *
     * @param name for this Project.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the iterator with all categories that can be used in this
     * Project.
     */
    public Iterator<Category> getCategories() {
        return categories.iterator();
    }

    /**
     * Constrained RETRIEVE operation. Retrieves the {@link Category} that
     * corresponds to the input {@code code} in this Project. The retrieval is
     * constrained by inner-logic to find this {@link Category}. If it is found,
     * then that {@link Category} is returned, otherwise, <b>null</b> is
     * returned.
     *
     * @param code of the requested {@link Category}.
     * @return {@link Category} if <code>code</code> is found, otherwise
     * <b>null</b> is returned.
     */
    public Category getCategory(String code) {
        Iterator<Category> itCategories = getCategories();
        while (itCategories.hasNext()) {
            Category category = itCategories.next();
            if (category.getCode().equalsIgnoreCase(code)
                    || category.getName().equalsIgnoreCase(code)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Constrained RETRIEVE operation. Retrieves the {@link Category} that
     * corresponds to the code <i>DEF</i>
     * in this Project. The retrieval is constrained by inner-logic to find this
     * {@link Category}. If it is found, then that {@link Category} is returned,
     * otherwise, <b>null</b> is returned.
     *
     * @return The {@link Category} that has <i>DEF</i> as code; if it is not
     * found, <b>null<\b> is returned.
     */
    public Category getDefaultCategory() {
        for (Category category : categories) {
            if (category.getCode().equalsIgnoreCase("DEF")) {
                return category;
            }
        }
        return null;
    }

    /**
     * Constrained CREATE and UPDATE operation; 'create' because the new
     * {@link Category} is returned and 'update' because the set of categories
     * in this Project is renewed. This method uses {@code code} and
     * {@code name} to create a {@link Category} for then add it into this
     * Project. As constraints, the {@code code} should not be empty and it
     * should not be a code that already exists in this Project.
     *
     * @param code for the {@link Category}; it should not be empty.
     * @param name for the {@link Category}.
     * @return the added {@link Category}. If <code>code</code> already exists,
     * then the {@link Category} with that code is returned.
     */
    public Category addCategory(String code, String name) {
        if (code.isEmpty()) {
            throw new RuntimeException("code of actegory may not be empty");
        }
        Category category = getCategory(code);
        if (category == null) {
            category = new Category(code, name, this);
            categories.add(category);
        }
        return category;
    }

    /**
     * Constrained DELETE and UPDATE operation; 'update' because the set of
     * categories in this Project is renewed with the deletion of the
     * {@code category}. Removes the <code>category</code> from this Project if
     * and only if it is no longer used in this Project.
     *
     * @param category to be removed.
     * @throws ChangeNotAllowedException if <code>category</code> is still in
     * use in this Project.
     */
    public void removeCategory(Category category) throws ChangeNotAllowedException {
        if (rm.makesUseOf(category)) {
            throw new ChangeNotAllowedException(category.getName() + " is in use at the requirements model");
        }
        categories.remove(category);
    }

    /**
     * Constrained RETRIEVE operation. Retrieves the saved at time of this
     * Project only if it is not <b>null</b>.
     *
     * @return date time to represent the saved-date of events.
     */
    public String getSavedAt() {
        String savedDateTime = null;
        if (savedAt != null) {
            savedDateTime = savedAt.get(Calendar.DAY_OF_MONTH) + " "
                    + savedAt.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
                    + savedAt.get(Calendar.YEAR) + " "
                    + savedAt.get(Calendar.HOUR) + ":"
                    + savedAt.get(Calendar.MINUTE) + ":"
                    + savedAt.get(Calendar.SECOND);
        }
        return savedDateTime;
    }

    /**
     * Constrained CREATE operation. Saves this Project into the      <code>file<\code> unless an exception occurs.
     *
     * @param file that will be used to save the project.
     * @return true if <code>file<\code> saving has been successful, otherwise false.
     * @throws FileNotFoundException if <code>file</code> is <b>null</b>.
     */
    public synchronized boolean save(File file) throws FileNotFoundException {
        if (file == null) {
            throw new FileNotFoundException("file of project is unknown");
        } else {
            Calendar savedAt_backUp = savedAt;
            try {
                save(file, this);
                return true;

            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                savedAt = savedAt_backUp;
                return false;
            }
        }
    }

    private static void save(File file, Project project) throws IOException {
        file.createNewFile();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        project.savedAt = new GregorianCalendar();
        out.writeObject(project);
        out.close();
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return name of this Project.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return iterator of {@link ClassDiagram}s of this Project.
     */
    public Iterator<ClassDiagram> getClassDiagrams() {
        return classDiagrams.iterator();
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return iterator of {@link ObjectRoleDiagram}s of this Project.
     */
    public Iterator<ObjectRoleDiagram> getObjectRoleDiagrams() {
        return objectRoleDiagrams.iterator();
    }

    /**
     * Constrained UPDATE operation. The collection of {@link ClassDiagram}s in
     * this Project is updated by adding <code>classDiagram</code> into it. This
     * update is constrained by inner-logic that prevents the duplication of
     * {@link ClassDiagram}s.
     *
     * @param classDiagram to be added into the collection of
     * {@link ClassDiagram}s in this Project.
     * @throws DuplicateException if the <code>classDiagram</code> already
     * exists.
     */
    public void addClassDiagram(ClassDiagram classDiagram) throws DuplicateException {
        if (classDiagrams.contains(classDiagram)) {
            throw new DuplicateException("Class diagram with name " + classDiagram.getName() + " already exists within current project.");
        } else {
            classDiagrams.add(classDiagram);
        }
    }

    /**
     * Unconstrained UPDATE operation. The list of {@link ObjectRoleDiagram}s in
     * this Project is updated by adding <code>ord</code> into it.
     *
     * @param ord to be added into the list of {@link ObjectRoleDiagram}s in
     * this Project.
     */
    public void addObjectRoleDiagram(ObjectRoleDiagram ord) {
        objectRoleDiagrams.add(ord);
    }

    /**
     * Constrained UPDATE operation. The collection of {@link ClassDiagram}s in
     * this Project is updated by removing <code>cd</code> from it. This update
     * is constrained by inner-logic that checks the existence of {@code cd}.
     *
     * @param cd to be removed from the collection of {@link ClassDiagram}s in
     * this Project.
     */
    public void removeClassDiagram(ClassDiagram cd) {
        if (classDiagrams.contains(cd)) {
            classDiagrams.remove(cd);
        }
    }

    /**
     * Unconstrained DELETE operation. The <code>ord</code> is removed from the
     * list of {@link ObjectRoleDiagram}s in this Project .
     *
     * @param ord to be removed from the list of {@link ObjectRoleDiagram}s in
     * this Project.
     */
    public void removeObjectRoleDiagram(ObjectRoleDiagram ord) {
        objectRoleDiagrams.remove(ord);
    }

    // TODO implementation of this method.
    public void removeParticipant(ProjectRole participant) throws ChangeNotAllowedException {
        if (participant instanceof StakeholderRole) {
        }
    }

    /**
     * Constrained UPDATE operation. First, it is checked that the {@link Category)(s) in this Project have
     * an owner. If a {@link Category} has no owner, the method ends.
     * If all owners exist, then the owner of the Default {@link Category}
     * becomes
     * <code>sh</code>.
     *
     * @param sh {@link StakeholderRole} that may become the owner of the
     *           Default {@link Category}.
     */
    void checkDefaultCategory(StakeholderRole sh) {
        for (Category category : categories) {
            if (category.getOwner() != null) {
                return;
            }
        }
        getDefaultCategory().setOwner(sh);
    }

    /**
     * Unconstrained UPDATE operation. Updates the {@link BasicPublisher} of
     * this Project by adding a new listener with {@code listener} and
     * {@code property}.
     *
     * @param listener of a property.
     * @param property contents.
     */
    @Override
    public void addListener(PropertyListener listener, String property) {
        publisher.addListener(listener, property);
    }

    /**
     * Unconstrained UPDATE operation. Updates the {@link BasicPublisher} of
     * this Project by removing a listener with {@code listener} and
     * {@code property}.
     *
     * @param listener of a property
     * @param property contents.
     */
    @Override
    public void removeListener(PropertyListener listener, String property) {
        publisher.removeListener(listener, property);
    }

    /**
     * Constrained RETRIEVE operation. Retrieves if the current user of
     * Symbiosis has a Project member role. The constraint is if current user is
     * <b>null</b> which implies that this method returns false.
     *
     * @return true if the logged user has a Project member role.
     */
    public boolean isLoggedUserProjectMember() {
        if (currentUser == null) {
            return false;
        }

        return projectRoles.isProjectMember(currentUser);
    }

    /**
     * Constrained RETRIEVE operation. Retrieves if the current user of
     * Symbiosis has a Stakeholder role. The constraint is if current user is
     * <b>null</b> which implies that this method returns false.
     *
     * @return true if the logged user has a Stakeholder role.
     */
    public boolean isLoggedUserStakeholder() {
        if (currentUser == null) {
            return false;
        }

        return projectRoles.isStakeholder(currentUser);
    }

    public void importReqs(Category cat, Map<String, List<String>> reqs) {
        if (cat == null) {
            cat = getDefaultCategory();
        }
        List<String> reqsOfKind = reqs.get("@action");

        for (String text : reqsOfKind) {
            (rm.addActionRequirement(cat,
                    text, new ExternalInput("", cat.getOwner()))).setManuallyCreated(true);
        }
        reqsOfKind = reqs.get("@fact");
        for (String text : reqsOfKind) {
            (rm.addFactRequirement(cat,
                    text, new ExternalInput("", cat.getOwner()))).setManuallyCreated(true);
        }
        reqsOfKind = reqs.get("@rule");
        for (String text : reqsOfKind) {
            (rm.addRuleRequirement(cat,
                    text, new ExternalInput("", cat.getOwner()))).setManuallyCreated(true);
        }
        reqsOfKind = reqs.get("@qa");
        for (String text : reqsOfKind) {
            (rm.addQualityAttribute(cat,
                    text, new ExternalInput("", cat.getOwner()))).setManuallyCreated(true);
        }
    }

    public NameSpace getRootNameSpace() {
        if (root == null) {
            root = new NameSpace(name);
        }
        return root;
    }

    public void setRootNameSpace(String name) {
        root.setName(name);
    }

    public Language getLastUsedLanguage() {
        return lastUsedLanguage;
    }

    public void setLastUsedLanguage(Language l) {
        lastUsedLanguage = l;
    }

    public void showStatistics() {
        java.lang.System.out.println("******************************************");
        {
            Iterator<ActionRequirement> it = this.rm.actions();
            int todo = 0;
            int generated = 0;
            while (it.hasNext()) {
                ActionRequirement action = it.next();
                {
                    if (action.isManuallyCreated()) {
                        todo++;
                    } else {
                        generated++;
                    }
                }

            }

            java.lang.System.out.println(Aspect.ActionToDo + " : " + todo);
            java.lang.System.out.println(Aspect.ActionGenerated + " : " + generated);

        }

        {
            Iterator<FactRequirement> it = rm.facts();
            int count = 0;
            while (it.hasNext()) {
                FactRequirement fr = it.next();
                if (!fr.getCategory().equals(Category.SYSTEM)) {
                    count++;
                }
            }

            java.lang.System.out.println(Aspect.Fact + " : " + count);
        }

        {
            Iterator<RuleRequirement> it = rm.rules();
            int todo = 0;
            int generated = 0;
            while (it.hasNext()) {
                RuleRequirement rule = it.next();
                if (!rule.getCategory().equals(Category.SYSTEM)) {
                    if (rule.isManuallyCreated()) {
                        todo++;
                    } else {
                        generated++;
                    }
                }
            }
            java.lang.System.out.println(Aspect.RuleToDo + " : " + todo);
            java.lang.System.out.println(Aspect.RuleGenerated + " : " + generated);

        }

        {
            int ftsize = 0;
            int otsize = 0;
            int registries = 0;
            int atsize = 0;
            int vtsize = 0;
            int inheritance = 0;
            for (FactType ft : om.getFactTypes()) {
                if (ft.isPureFactType()) {
                    if (!ft.isGenerated()) {
                        ftsize++;
                    }
                } else {
                    if (ft.isSingleton() && ft.isGenerated()) {
                        registries++;
                    } else {
                        ObjectType ot = ft.getObjectType();

                        if (ot.isAbstract()) {
                            atsize++;
                        } else if (ot.isValueType()) {
                            vtsize++;
                        } else {
                            inheritance += ot.countSupertypes();
                            otsize++;
                        }

                    }
                }
            }
            java.lang.System.out.println(Aspect.FactTypes + " : " + ftsize);
            java.lang.System.out.println(Aspect.ObjectTypes + " : " + otsize);
            java.lang.System.out.println(Aspect.AbstractObjectTypes + " : " + atsize);
            java.lang.System.out.println(Aspect.Registries + " : " + registries);
            java.lang.System.out.println(Aspect.ValueTypes + " : " + vtsize);
            java.lang.System.out.println("Classes : " + (otsize + registries + atsize + vtsize));

            java.lang.System.out.println();
            java.lang.System.out.println(Aspect.Inheritance + " : " + inheritance);
        }

        {

            List<Integer> associationCounts = new ArrayList<>();
            List<Integer> attributeCounts = new ArrayList<>();
            List<Integer> operationCounts = new ArrayList<>();
            for (FactType ft : om.getFactTypes()) {
                if (ft.isObjectType()) {
                    ObjectType ot = ft.getObjectType();
                    int attributes = 0;
                    int associations = 0;
                    for (Relation relation : ot.relations()) {
                        if (relation.isNavigable()) {
                            // work around as result of conflicting warning
//                            if (relation.targetType() != null) 
                            {
                                if (relation.targetType().isValueType()) {
                                    attributes++;
                                } else {
                                    associations++;
                                }
                            }
                        }
                    }
                    associationCounts.add(associations);
                    attributeCounts.add(attributes);
                    operationCounts.add(ot.getCodeClass().getSize());
                }
            }

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);

            DataSet<Integer> dataSet = new DataSet(associationCounts.toArray(new Integer[]{}));
            double sum = dataSet.sum();
            java.lang.System.out.println(Aspect.Associations + " total : " + sum);
            java.lang.System.out.println(Aspect.Associations + " average : " + nf.format(dataSet.average()));
            java.lang.System.out.println(Aspect.Associations + " sd : " + nf.format(dataSet.standardDeviation()));

            dataSet = new DataSet((Number[]) attributeCounts.toArray(new Integer[]{}));
            sum += dataSet.sum();
            java.lang.System.out.println(Aspect.Attributes + " total : " + dataSet.sum());
            java.lang.System.out.println(Aspect.Attributes + " average : " + nf.format(dataSet.average()));
            java.lang.System.out.println(Aspect.Attributes + " sd : " + nf.format(dataSet.standardDeviation()));

            java.lang.System.out.println("Properties : " + (int) sum);

            dataSet = new DataSet((Number[]) operationCounts.toArray(new Integer[]{}));
            java.lang.System.out.println(Aspect.Operations + " total : " + dataSet.sum());
            java.lang.System.out.println(Aspect.Operations + " average : " + nf.format(dataSet.average()));
            java.lang.System.out.println(Aspect.Operations + " sd : " + nf.format(dataSet.standardDeviation()));
        }

    }
}
