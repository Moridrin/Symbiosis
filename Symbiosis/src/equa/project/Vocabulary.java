/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;

import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;

/**
 *
 * @author frankpeeters
 */
@Entity
public class Vocabulary implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long vocabularyId;
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "term")
    private Map<String, Term> termTable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private ObjectModel om;

    /**
     * Constructor. Initializes a map of vocabulary.
     */
    public Vocabulary() {
        termTable = new TreeMap<>();
    }

    /**
     * Constructor. Initializes a map of vocabulary and sets the object model
     * for this vocabulary.
     *
     * @param om
     */
    Vocabulary(ObjectModel om) {
        termTable = new TreeMap<>();
        this.om = om;
    }

    /**
     *
     * @return new {@link TermIterator}.
     */
    public Iterator<ITerm> terms() {
        TermIterator iterator = new TermIterator();
        return iterator;
    }

    /**
     * Reviews if the termName (a) is member of the map of this vocabulary or if
     * termName (b) is the name of the object model's fact-type in this
     * vocabulary.
     *
     * @param termName to review
     * @return true if termName satisfies (a) or (b)
     */
    public boolean isMember(String termName) {
        return termTable.containsKey(termName)
                || om.getFactType(termName) != null;
    }

    /**
     *
     * @return map size plus size of the object model of this vocabulary
     */
    public int size() {
        return termTable.size() + om.getSize();
    }

    /**
     *
     * @param termName should be found in this vocabulary
     * @return term from the map or the object model's fact-type
     */
    public ITerm getTerm(String termName) {
        ITerm term = termTable.get(termName);
        if (term == null) {
            term = om.getFactType(termName);
        }
        return term;
    }

    /**
     *
     * @param term to add in the map of this vocabulary.
     */
    public void addTerm(Term term) {
        if (!isMember(term.getName())) {
            termTable.put(term.getName(), term);
        }
    }

    /**
     *
     * @param term to remove from the map of this vocabulary.
     */
    public void removeTerm(Term term) {
        termTable.remove(term.getName());
    }

    /**
     *
     * @return id of this vocabulary.
     */
    public long getVocabularyId() {
        return vocabularyId;
    }

    /**
     *
     * @param vocabularyId for this vocabulary
     */
    public void setVocabularyId(long vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    /**
     * ******** inner class **********************************************
     */
    private class TermIterator implements Iterator<ITerm> {

        private Iterator<String> itVocabulary;
        private String nextTerm;
        private Iterator<FactType> itObjectModel;
        private FactType nextFactType;

        /**
         * Constructor. Prepares the collection of terms in the vocabulary and
         * of fact-types in the object-type of the vocabulary.
         */
        public TermIterator() {
            itVocabulary = termTable.keySet().iterator();
            nextTerm();

            itObjectModel = om.typesIterator();
            nextFactType();

        }

        private void nextTerm() {
            if (itVocabulary.hasNext()) {
                nextTerm = itVocabulary.next();
            } else {
                nextTerm = null;
            }
        }

        private void nextFactType() {
            if (itObjectModel.hasNext()) {
                nextFactType = itObjectModel.next();
            } else {
                nextFactType = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextTerm != null || nextFactType != null;
        }

        @Override
        public ITerm next() {
            ITerm next;
            if (nextTerm == null) {
                next = nextFactType;
                nextFactType();
            } else if (nextFactType == null) {
                next = termTable.get(nextTerm);
                nextTerm();
            } else {
                if (nextTerm.compareTo(nextFactType.getName()) < 0) {
                    next = termTable.get(nextTerm);
                    nextTerm();
                } else {
                    next = nextFactType;
                    nextFactType();
                }
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove isn't allowed");
        }
    }
}
