/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;

/**
 *
 * @author frankpeeters
 */
@Entity
public class NumberIssue implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "nr")
    private Map<String, Integer> numberMap;

    /**
     *
     * @return map of numbers
     */
    public Map<String, Integer> getNumberMap() {
        return numberMap;
    }

    /**
     *
     * @param numberMap to replace the map of numbers
     */
    public void setNumberMap(Map<String, Integer> numberMap) {
        this.numberMap = numberMap;
    }

    /**
     * Constructor. Initializes a number-map for this NumberIssue instance.
     */
    public NumberIssue() {
        numberMap = new HashMap<>();
    }

    /**
     * if key is unknown, a new key with a number starting at 1 will be
     * registered; after the call the number is incremented with 1
     *
     * @param key
     * @return the issued number in behalf of key
     */
    public int nextNumber(String key) {
        if (!numberMap.containsKey(key)) {
            numberMap.put(key, 1);
        }

        int value = numberMap.get(key).intValue();
        numberMap.put(key, value + 1);
        return value;

    }

    /**
     * if key is known and the related number is larger then 1, the number will
     * be decremented with one; otherwise everything stays unchanged
     *
     * @param key
     */
    public void rollBack(String key) {
        if (numberMap.containsKey(key)) {
            int value = numberMap.get(key).intValue();
            if (value > 1) {
                numberMap.put(key, value - 1);
            }
        }
    }

    /**
     * the key will be set with next number nr
     *
     * @param key
     * @param nr >0
     */
    public void setNextNumber(String key, int nr) {
        if (nr <= 0) {
            throw new RuntimeException("numbers at this issueing system "
                    + "must be positive");
        }
        numberMap.put(key, nr);
    }

    /**
     *
     * @return an iterator over all keys
     */
    public Iterator<String> keyIterator() {
        return numberMap.keySet().iterator();
    }

    /**
     *
     * @param key
     * @return true if this key is known at this issueing system, otherwise
     * false
     */
    public boolean containsKey(String key) {
        return numberMap.containsKey(key);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
