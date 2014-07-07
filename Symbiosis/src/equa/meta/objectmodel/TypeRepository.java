/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.util.Naming;

/**
 * Class that functions as a repository of {@link FactType}s via a Map data
 * structure; each registry of the Map has a key and a value.<br> Each key shall
 * be the {@link FactType} name, whereas its value shall be the {@link FactType}
 * object. <br>
 * This class contains constrained/unconstrained R[etrieve] and constrained
 * U[pdate] operation(s). The behavior is not precisely of a typical DTO, as
 * this class includes constraints implemented as logic in some retrieval or
 * update operations. Thus, this behavior is proposed as Data Regulated Transfer
 * Object (DRTO).
 *
 * @author frankpeeters
 */
@Entity
public class TypeRepository implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ElementCollection
    @MapKeyColumn(name = "ft")
    private Map<String, FactType> types;

    /**
     * DEAFULT CONSTRUCTOR that prepares a new Map to store {@link FactType}s.
     */
    public TypeRepository() {
        types = new TreeMap<>();
    }

    /**
     * Unconstrained RETRIEVE operation. Retrieves if the {@link FactType} named
     * {@code name} exists in this TypeRepository.
     *
     * @param name of a {@link FactType}.
     * @return true if the {@link FactType} exists in this TypeRepository.
     */
    public boolean existsFactType(String name) {
        return types.containsKey(name.toLowerCase());
    }

    /**
     * Unconstrained RETRIEVE operation. Retrieves the {@link FactType} that has
     * the key {@code name} in this TypeRepository.
     *
     * @param name of the expected {@link FactType}.
     * @return {@link FactType}
     */
    public FactType getFactType(String name) {
        return types.get(name.toLowerCase());
    }

    /**
     * Constrained UPDATE operation. The update of the Map of {@link FactType}s
     * in this TypeRepository is constrained by inner-logic to validate if the
     * <code>candidate</code> already exists in this Map. If {@code candidate}
     * does not exist in this Map, the {@code candidate} is put in it and true
     * is returned; otherwise, false is returned and no update is done.
     *
     * @param candidate to put in this TypeRepository.
     * @return true if the {@link FactType} was put in this TypeRepository.
     */
    public boolean putFactType(FactType candidate) {
        String nameToLower = candidate.getName().toLowerCase();
        if (types.containsKey(nameToLower)) {
            return false;
        }
        types.put(nameToLower, candidate);
        return true;
    }

    /**
     * Constrained UPDATE operation. The deletion of a {@link FactType} from the
     * Map in this TypeRepository is constrained by inner-logic to validate if
     * the {@link FactType} actually exists in this Map. The validation is done
     * with the {@link name} of the {@link FactType}; if it does exist, the Map
     * of this TypeRepository is updated by the removal of the {@link FactType}
     * and true is returned; otherwise, false is returned and no update is done.
     *
     * @param name of the {@link FactType} to remove.
     * @return true if the {@link FactType} has been removed.
     */
    public boolean removeFactType(String name) {
        if (types.containsKey(name.toLowerCase())) {
            types.remove(name.toLowerCase());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Constrained UPDATE operation. The update of name of a {@link FactType} in
     * the Map of this TypeRepository is constrained by inner-logic to manage
     * the possible renaming scenarios. First, the {@code newName} is compared
     * with <code>candidate</code>'s name. If they are equal, the
     * {@link Naming#withCapital(java.lang.String)} is used and the renaming is
     * achieved. Otherwise, the name is validated in this TypeRegistry, so that
     * the renaming of <code>candidate</code> is applied if (i) the
     * {@code newName} is not already a used key in the Map of this registry and
     * (ii) the {@code newName} does not violate the {@link FactType}'s naming
     * conventions.
     *
     * @param newName for the {@link FactType}.
     * @param candidate to be renamed.
     * @throws DuplicateException if the new name already exists.
     * @throws ChangeNotAllowedException if the new name violates the registry
     * classes naming conventions.
     */
    public void renameFactType(String newName, FactType candidate)
            throws DuplicateException, ChangeNotAllowedException {
        String newNameToLower = newName.toLowerCase();
        if (newNameToLower.equals(candidate.getName().toLowerCase())) {
            candidate.rename(Naming.withCapital(newName));
        } else {
            if (types.containsKey(newNameToLower)) {
                throw new DuplicateException(newName + " is already used");
            } else {
                if (newName.startsWith("_")) {
                    if (!candidate.getName().startsWith("_")) {
                        throw new ChangeNotAllowedException("Only the name of registry classes start with an underscore (_)");
                    }
                } else {
                    if (candidate.getName().startsWith("_")) {
                        throw new ChangeNotAllowedException("The name of a registry classes should start with an underscore (_)");
                    }
                }
                removeFactType(candidate.getName());
                candidate.rename(Naming.withCapital(newName));
                types.put(newNameToLower, candidate);
            }
        }
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return collection of all {@link FactType}s in this TypeRepository.
     */
    public Collection<FactType> getFactTypeCollection() {
        return types.values();

    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return iterator with the set of names of {@link FactType}s in this
     * TypeRepository.
     */
    public Iterator<String> getFactTypeNameIterator() {
        return types.keySet().iterator();
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @param from , starting name of the {@link FactType} in the subset.
     * @param unto , ending name of the {@link FactType} in the subset.
     * @return iterator with a subset of names of {@link FactType} in this
     * TypeRepository.
     */
    public Iterator<String> getFactTypeNameIterator(String from, String unto) {
        return ((TreeMap<String, FactType>) types).subMap(from, unto).keySet().iterator();
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return amount of {@link FactType}s in the Map of this TypeRepository.
     */
    public int size() {
        return types.size();
    }

    /**
     * Constrained RETRIEVE operation. The retrieval is constrained by
     * inner-logic to match {@link FactType}s in this TypeRepository with
     * {@code factTypeNames}.
     *
     * @param factTypeNames of the requested {@link FactType}s.
     * @return collection with the {@link FactType}s that match with
     * <code>factTypeNames</code>. The names that have no {@link FactType} in
     * this TypeRepository are ignored.
     */
    Collection<FactType> getFactTypeCollection(Collection<String> factTypeNames) {
        ArrayList<FactType> collection = new ArrayList<>();
        for (String name : factTypeNames) {
            FactType ft = getFactType(name);
            if (ft != null) {
                collection.add(ft);
            }
        }
        return collection;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return id of this TypeRepository.
     */
    public long getId() {
        return id;
    }

    /**
     * Unconstrained UPDATE operation.
     *
     * @param id for this TypeRepository.
     */
    public void setId(long id) {
        this.id = id;
    }

}
