package edu.uchsc.ccp.opendmap.tokenizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple class used to hold annotation information.
 * 
 * @author Bill Baumgartner
 *
 */
public class EntityAnnotation {
    int spanStart;

    int spanEnd;

    /* The entity type.  Make sure this type appears in the ontology being used by OpenDMAP, otherwise this entity will not be recognized. */
    String entityType;

    /* slots can hold sets of Objects. The objects are typically Strings or other EntityAnnotations. */
    Map<String, Set<Object>> slotName2ValuesMap;

    public EntityAnnotation(int spanStart, int spanEnd, String entityType) {
        this.spanStart = spanStart;
        this.spanEnd = spanEnd;
        this.entityType = entityType;
        this.slotName2ValuesMap = new HashMap<String, Set<Object>>();
    }

    public int getSpanEnd() {
        return spanEnd;
    }

    public void setSpanEnd(int spanEnd) {
        this.spanEnd = spanEnd;
    }

    public int getSpanStart() {
        return spanStart;
    }

    public void setSpanStart(int spanStart) {
        this.spanStart = spanStart;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Map<String, Set<Object>> getSlotName2ValuesMap() {
        return slotName2ValuesMap;
    }

    public void setSlotName2ValuesMap(Map<String, Set<Object>> slotName2ValuesMap) {
        this.slotName2ValuesMap = slotName2ValuesMap;
    }

    public void addSlotNameValuePair(String slotName, Object slotValue) {
        if (slotName2ValuesMap.containsKey(slotName)) {
            slotName2ValuesMap.get(slotName).add(slotValue);
        } else {
            Set<Object> values = new HashSet<Object>();
            slotName2ValuesMap.put(slotName, values);
        }
    }

}

