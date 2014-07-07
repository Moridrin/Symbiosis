/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import java.util.Iterator;

/**
 * terms within a vocabulary of a project; name of term is case sensitive
 *
 * @author frankpeeters
 */
public interface ITerm {

    /**
     *
     * @return the name of the term
     */
    String getName();

    /**
     *
     * @return the name of the term in plural form (may be undefined, in that
     * case null will be returned)
     */
    String getPlural();

    /**
     * setting/changing of the plural form of this term
     *
     * @param plural not empty; null is allowed (undefining the plural form)
     */
    void setPlural(String plural);

    /**
     *
     * @return an iterator over all registerd synonyms of this term
     */
    Iterator<String> getSynonyms();

    /**
     * adding of synonym to the currrent set of synonyms
     *
     * @param synonym not empty
     */
    void addSynonym(String synonym);

    /**
     * synonym is removed out of the current set with synonyms
     *
     * @param synonym
     */
    void removeSynonym(String synonym);

    /**
     *
     * @return a description of the term
     */
    String getDocumentation();

    /**
     * setting/changing of the documentation of this term
     *
     * @param documentation
     */
    void setDocumentation(String documentation);

    /**
     *
     * @return an iterator over terms which could be interesting to inspect
     */
    Iterator<ITerm> getReferenceTerms();

    /**
     * term is added to the current set with referenced terms
     *
     * @param term is a member of the currect vocabulary of the project
     */
    void addReferenceTerm(ITerm term);

    /**
     * term is removed from the current set with referenced terms
     *
     * @param term
     */
    void removeReferenceTerm(ITerm term);
}
