package equa.meta.traceability;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;

import equa.project.ProjectRole;

@Entity
public class E_Document extends Document {

    private static final long serialVersionUID = 1L;
    @Column
    private URL url;

    public E_Document() {
    }

    /**
     * creation of a reference to an electronic document with given title,
     * section, autors and url
     *
     * @param title
     * @param section
     * @param authors
     * @param url
     * @param justification
     * @param participant
     */
    public E_Document(String title, String section, String authors, URL url,
            String justification, ProjectRole participant) {
        super(title, section, authors, justification, participant);
        this.url = url;
    }

    /**
     *
     * @return the url of this document
     */
    public URL getUrl() {
        return url;
    }

    /**
     * setting of the url of thios document
     *
     * @param url
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return super.toString() + " at URL: " + url;
    }
}
