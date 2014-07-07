package equa.meta.traceability;

import javax.persistence.Column;
import javax.persistence.Entity;

import equa.project.ProjectRole;

/**
 *
 * @author FrankP
 */
@Entity
public class Document extends ExternalInput {

    private static final long serialVersionUID = 1L;
    @Column
    private String title;
    @Column
    private String section;
    @Column
    private String authors;

    public Document() {
    }

    /**
     * creation of a document with give title, section, authors, justification
     * and the participant who is responsible for this input.
     *
     * @param title of the document
     * @param section of relevance
     * @param authors of the document
     * @param justification
     * @param participant
     */
    public Document(String title, String section, String authors,
            String justification, ProjectRole participant) {
        super(justification, participant);
        this.title = title;
        this.section = section;
        this.authors = authors;
    }

    /**
     *
     * @return the title of this document
     */
    public String getTitle() {
        return title;
    }

    /**
     * setting of an new title
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return the relevant section of this document
     */
    public String getSection() {
        return section;
    }

    /**
     * setting of the relevant section
     *
     * @param section
     */
    public void setSection(String section) {
        this.section = section;
    }

    /**
     *
     * @return the authors of this document
     */
    public String getAuthors() {
        return authors;
    }

    /**
     * setting of the authors
     *
     * @param authors
     */
    public void setAuthors(String authors) {
        this.authors = authors;
    }

    @Override
    public String toString() {
        return super.toString() + " based on document with "
                + "title: " + title
                + ", section: " + section
                + "and authors: " + authors;
    }

    /**
     * If 'obj' is not a Document class, false is automatically returned.
     *
     * @param obj that should be an instance of Document class.
     * @return true iff the title, section and authors are case insensitive
     * equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Document) {
            Document document = (Document) obj;
            return document.title.equalsIgnoreCase(title) && document.section.equalsIgnoreCase(section) && document.authors.equalsIgnoreCase(authors);
        } else {
            return false;
        }
    }
}
