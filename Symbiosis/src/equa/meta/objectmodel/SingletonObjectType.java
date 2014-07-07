/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import equa.code.Field;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.FactRequirement;
import equa.meta.traceability.Source;
import equa.util.Naming;

/**
 *
 * @author FrankP
 */
@Entity
public class SingletonObjectType extends ObjectType {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Singleton singleton;

    public SingletonObjectType() {
    }

    /**
     * creation of a singleton objecttype which refers to parent and contains
     * one object with the name objectname, based on source
     *
     * @param parent
     * @param objectname is not empty
     * @param source
     */
    public SingletonObjectType(FactType parent, String objectname,
            Source source) {
        super(parent, objectname);
        singleton = new Singleton(this, source);
        getFactType().getPopulation().addSingleton(singleton);
    }

    /**
     *
     * @return the singleton object belonging to this objecttype
     */
    public Singleton getSingleton() {
        return singleton;
    }

    @Override
    public Value parse(String expression, boolean otherOptions, FactRequirement source) throws MismatchException {
        if (expression.trim().equalsIgnoreCase(getOTE().makeExpression(singleton))) {
            singleton.addSource(source);
            return singleton;
        } else {
            throw new MismatchException(getOTE(), "[" + expression + "] differs from [" + singleton.toString() + "]");
        }
    }

    @Override
    public String makeExpression(Value value) {
        return getOTE().makeExpression(singleton);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    boolean isDirectAccessible() {
        return true;
    }

    @Override
    void generateMethods() {
        try {
            List<Relation> relations = codeClass.getRelations();
            generateSingletonField();

            generateMethods(relations);
            generateToStringMethod();
            if (!isLight()) {
                generatePropertiesMethod();
            }
        } catch (DuplicateException ex) {
            Logger.getLogger(SingletonObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void generateSingletonField() {
        Field f = new Field(this, Naming.singletonName(getName()));
        f.setClassField(true);
        codeClass.addField(f);
    }

}
