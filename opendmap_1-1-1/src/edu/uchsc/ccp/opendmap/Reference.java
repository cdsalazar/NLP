/**
 * The OpenDMAP for Protege Project
 * July 2005
 */
package edu.uchsc.ccp.opendmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;

/**
 * A <code>Reference</code> is a description of something that DMAP has recognized in the input text. Typically, a reference will be either a word
 * or a Protege Frame with slot fillers.
 * <p>
 * A reference to a word (or a regex) is a simple record of the fact that the encapsulated word was spotted at the given point in the input text.
 * <p>
 * A reference to a Protege Frame describes a frame that the input text mentions and includes the slot fillers for that frame that the text mentions.
 * There may not be an actual Protege Instance of that frame with those slot fillers in the knowledge base.
 * 
 * @author Will Fitzgerald
 * @author R. James Firby
 * @author William A. Baumgartner, Jr.
 */

/*
 * Changes (most recent first)
 * 
 * 11-29-06 wab - Added reference to the Prediction that was responsible for the creation of this Reference - this is used primarily for debugging
 * ---------------purposes when tracing back to find the pattern that was originally responsible for a Prediction or Reference ---------------------
 * 02-01-06 rjf - Added property lists -------------------------------------------------------------------------------------------------------------
 * 02-01-06 rjf - Changed subsumption to include properties and strictness -------------------------------------------------------------------------
 * 12-21-05 rjf - Added separate denotation concept ------------------------------------------------------------------------------------------------
 * 10-12-05 rjf - Added the character start and end positions. -------------------------------------------------------------------------------------
 * 09-30-05 rjf - Added equal method. --------------------------------------------------------------------------------------------------------------
 * 09-29-05 rjf - Added duplicate method. ----------------------------------------------------------------------------------------------------------
 * 09-26-05 rjf - Added subsumes method. -----------------------------------------------------------------------------------------------------------
 * 09-13-05 rjf - Added getSlotValues for retrieving multiple values for the same slot name. -------------------------------------------------------
 */

public class Reference implements Cloneable {

    /* The DMAP item encapsulated by this reference */
    private DMAPItem item;

    /* Other DMAP items this reference encapsulates if it is a set */
    // private Vector<DMAPItem> members = null;
    /* The start and end token positions of the constituents of this reference */
    private int start;

    private int end;

    /* The number of tokens ignored between the start and end */
    private int missing;

    /* The start and end character positions of this reference */
    private int cStart = -1;

    private int cEnd = -1;

    /* The subconcepts recognized while building this reference */
    private Vector<InfoPacket> information;

    /* The 'valence' properties attached to this reference */
    private Vector<String> properties = null;

    /* The tokens seen to recognize this reference */
    private Vector<DMAPToken> tokens = new Vector<DMAPToken>();

    /* The prediction that resulted in creation of this reference */
    private Prediction prediction = null;

    /**
     * Create a new reference for the supplied item stating it was recognized by the input tokens spanning the range from start to end. While parsing
     * this range the additional items in information were also referenced.
     * 
     * @param item
     *            The item referenced.
     * @param start
     *            The input token position of the start of the reference.
     * @param end
     *            The input token position of the end of the reference.
     * @param missing
     *            The number of tokens within the span of this reference that are not accounted for by this reference.
     * @param information
     *            Additional references encountered while parsing this reference.
     * @param cStart
     *            The input character position of the start of the reference.
     * @param cEnd
     *            The input character position of the end of the reference.
     */
    Reference(DMAPItem item, int start, int end, int missing, Vector<InfoPacket> information, int cStart, int cEnd, Prediction prediction) {
        super();
        this.item = item;
        this.start = start;
        this.end = end;
        this.missing = missing;
        this.information = information;
        this.cStart = cStart;
        this.cEnd = cEnd;
        this.prediction = prediction;
    }

    /**
     * Create a copy of this Reference with new slot information.
     * 
     * @param information
     *            Additional references for this reference.
     * @return The new Reference.
     */
    @SuppressWarnings("unchecked")
    public Reference duplicate(Vector<InfoPacket> information) {
        Reference dup = new Reference(item, start, end, missing, information, cStart, cEnd, prediction);
        if (properties != null)
            dup.properties = (Vector<String>) properties.clone();
        if (tokens != null) {
            for (DMAPToken token : tokens)
                dup.addToken(token);
        }
        return dup;
    }

    @SuppressWarnings("unchecked")
    public Reference clone() {
        if (information != null) {
            return duplicate((Vector<InfoPacket>) information.clone());
        } else {
            return duplicate(null);
        }
    }

    /**
     * Get the item referenced.
     * 
     * @return The item referenced.
     */
    public DMAPItem getItem() {
        return item;
    }

    public Vector<InfoPacket> getInfo(){
        return this.information;
    }

    /**
     * Get the input token position of the start of this reference.
     * 
     * @return The input token position of the start of this reference.
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the input token position of the end of this reference.
     * 
     * @return The input token position of the end of this reference.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Get the number of input tokens ignored by this reference between the start and end token positions.
     * 
     * @return The number of ignored tokens.
     */
    public int getMissing() {
        return missing;
    }

    /**
     * Get the list of tokens used to recognize this reference.
     * 
     * @return The list of tokens used.
     */
    public List<DMAPToken> getTokens() {
        return tokens;
    }

    /**
     * Add a new token to the list of tokens used to recognize this reference. Keep the list in order sorted by token start position.
     * 
     * @param newToken
     *            The new token to add to the list.
     */
    public void addToken(DMAPToken newToken) {
        boolean found = false;
        for (int i = 0; i < tokens.size(); i++) {
            if (newToken.getStart() < tokens.get(i).getStart()) {
                tokens.add(i, newToken);
                found = true;
                break;
            } else if (newToken.equals(tokens.get(i))) {
                found = true;
                break;
            }
        }
        if (!found)
            tokens.add(newToken);
    }

    /**
     * Get the additional reference information found while recognizing this reference.
     * 
     * @return The additional references found within this reference.
     */
    public Vector<InfoPacket> getInformation() {
        return information;
    }
    
    public void addInfoPacket (InfoPacket info) {
    	information.add(info);
    }

    /**
     * Get the input character position of the start of this reference.
     * 
     * @return The input character position of the start of this reference.
     */
    public int getCharacterStart() {
        return cStart;
    }

    /**
     * Get the input character position of the end of this reference.
     * 
     * @return The character token position of the end of this reference.
     */
    public int getCharacterEnd() {
        return cEnd;
    }

    /**
     * Check whether this reference is tagged with any properties.
     * 
     * @return True if there is at least one tag attached to this reference.
     */
    public boolean hasProperties() {
        return ((properties != null) && !properties.isEmpty());
    }

    /**
     * Check whether this reference has a particular tag.
     * 
     * @param name
     *            The name of the tag
     * @return True if this reference has a tag with the given name
     */
    public boolean hasProperty(String name) {
        // Check the property list first
        if (properties != null) {
            for (String property : properties) {
                if (property.equalsIgnoreCase(name))
                    return true;
            }
        }
        // Now, try isa
        if (item != null) {
            if (item.isa(name))
                return true;
        }
        return false;
    }

    /**
     * Get the item that this reference 'denotes'.
     * 
     * @return The item this reference 'denotes'
     */
    public DMAPItem getDenotation() {
        // TODO - Get this to work with multiple denotations
        if ((information == null) || information.isEmpty()) {
            return null;
        } else {
            for (InfoPacket info : information) {
                DMAPItem key = info.getKey();
                if (key instanceof ProtegeSlotItem) {
                    if (((ProtegeSlotItem) key).isDenotation()) {
                        return info.getValue().getItem();
                    }
                }
            }
            return null;
        }
    }

    /**
     * Massage this reference into a valid filler for the specified slot. This may return this reference if it is a valid filler directly, or it might
     * return one of the slot fillers of this reference pulled up as the denotation of this reference. If the denotation slot is pulled up, it gets
     * the slot fillers for the this reference attached and it gets this reference type as a property tag.
     * 
     * @param slot
     *            The slot to be filled
     * @return This reference or its denotation
     */
    Reference invert(ProtegeSlotItem slot) {
        if (slot.validFiller(item)) {
            // This reference is a valid filler of the slot, no inversion necessary
            return this;
        } else if ((information == null) || information.isEmpty()) {
            // No slots, no inversion possible
            return this;
        } else {
            // Look for the denotation slot
            InfoPacket denotation = null;
            for (InfoPacket info : information) {
                DMAPItem key = info.getKey();
                if (key instanceof ProtegeSlotItem) {
                    if (((ProtegeSlotItem) key).isDenotation()) {
                        denotation = info;
                        break;
                    }
                }
            }
            // If there is no denotation slot, no inversion possible
            if (denotation == null)
                return this;
            // Pull up the denotation reference
            Reference denotationRef = denotation.getValue();
            // See if it is a valid filler for the slot
            if (slot.validFiller(denotationRef.item)) {
                // Okay, this is what we want to return, first clone it
                Reference result = denotationRef.clone();
                // Expand the "span" of this reference
                result.start = start;
                result.end = end;
                result.cStart = cStart;
                result.cEnd = cEnd;
                result.missing = missing;
                result.prediction = prediction.clone();
                for (DMAPToken token : tokens)
                    result.addToken(token);
                // Add any slots at this level
                for (InfoPacket info : information) {
                    if (info != denotation)
                        result.information.add(info);
                }
                // Add this level as a 'property'
                if (item instanceof ProtegeFrameItem) {
                    // TODO: How do we accumulate properties sometimes and not other times?
                    // if (result.properties == null)
                    result.properties = new Vector<String>();
                    result.properties.add(((ProtegeFrameItem) item).getFrame().getBrowserText());
                }
                return result;
            } else {
                // Not really a valid slot filler, this should never happen!
                return null;
            }
        }
    }

    /**
     * Return a string appropriate for debugging output.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append(item.toString());
        // sb.append(":");
        // sb.append(this.hashCode());
        sb.append(" from ");
        sb.append(start);
        sb.append(" to ");
        sb.append(end);
        sb.append(" ignore ");
        sb.append(missing);
        if ((information != null) && (information.size() > 0)) {
            sb.append(" : ");
            sb.append(information.toString());
        }
        if ((properties != null) && (properties.size() > 0)) {
            sb.append(" | ");
            sb.append(properties.toString());
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Get a descriptive string for this reference that includes the information gathered during its recognition.
     * 
     * @return A descriptive string for this reference.
     */
    public String getReferenceString() {
        return item.getReferenceString(information, properties);
    }

    /**
     * Get a reference that was recognized as the filler of a Protege slot within this reference.
     * 
     * @param slotName
     *            The Protege slot name.
     * @return The reference to the filler of the named slot.
     */
    public Reference getSlotValue(String slotName) {
        if (slotName == null)
            return null;
        Reference result = null;
        if (information != null) {
            for (InfoPacket pair : information) {
                DMAPItem key = pair.getKey();
                if (key instanceof ProtegeSlotItem) {
                    Slot slot = ((ProtegeSlotItem) key).getSlot();
                    if (slot != null) {
                        if (slotName.equals(slot.getName())) {
                            result = pair.getValue();
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the names of all of the slots defined within this reference. These names can then be used with getSlotValue and getSlotValues to get the
     * values of a slot.
     * 
     * @return The slot names.
     */
    public Collection<String> getSlotNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (InfoPacket pair : information) {
            DMAPItem key = pair.getKey();
            if (key instanceof ProtegeSlotItem) {
                Slot slot = ((ProtegeSlotItem) key).getSlot();
                if (slot != null) {
                    names.add(slot.getName());
                }
            }
        }
        return names;
    }

    /**
     * Get a DMAPItem that represents the default filler of a Protege slot for this reference, if a default is specified in the Protege knowledge
     * base.
     * 
     * @param slotName
     *            The Protege slot name.
     * @return The DMAPItem holding the default slot filler.
     */
    public DMAPItem getSlotDefault(String slotName) {
        if (item == null) {
            return null;
        } else {
            return item.getSlotValue(slotName);
        }
    }

    public InfoPacket getSlot(String slotName) {
        if (slotName == null)
            return null;
        InfoPacket result = null;
        if (information != null) {
            for (InfoPacket pair : information) {
                DMAPItem key = pair.getKey();
                if (key instanceof ProtegeSlotItem) {
                    Slot slot = ((ProtegeSlotItem) key).getSlot();
                    if (slot != null) {
                        if (slotName.equals(slot.getName())) {
                            result = pair;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all the references that were recognized as the fillers of a Protege slot within this reference.
     * 
     * @param slotName
     *            The Protege slot name.
     * @return The references to the fillers of the named slot.
     */
    public Collection<Reference> getSlotValues(String slotName) {
        if (slotName == null)
            return null;
        Vector<Reference> result = null;
        if (information != null) {
            for (InfoPacket pair : information) {
                DMAPItem key = pair.getKey();
                if (key instanceof ProtegeSlotItem) {
                    Slot slot = ((ProtegeSlotItem) key).getSlot();
                    if (slot != null) {
                        if (slotName.equals(slot.getName())) {
                            if (result == null)
                                result = new Vector<Reference>();
                            result.add(pair.getValue());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all the references that were recognized as the fillers of a Protege slot within this reference.
     * 
     * @param item
     *            A DMAPItem holding a Protege slot.
     * @return The references to the fillers of that slot.
     */
    private Collection<Reference> getSlotValues(DMAPItem item) {
        if (item == null)
            return null;
        if (!(item instanceof ProtegeSlotItem))
            return null;
        Slot pSlot = ((ProtegeSlotItem) item).getSlot();
        if (pSlot == null)
            return null;
        Vector<Reference> result = null;
        if (information != null) {
            for (InfoPacket pair : information) {
                DMAPItem key = pair.getKey();
                if (key instanceof ProtegeSlotItem) {
                    if (pSlot == ((ProtegeSlotItem) key).getSlot()) {
                        if (result == null)
                            result = new Vector<Reference>();
                        result.add(pair.getValue());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Check if a reference was recognized as the filler of a Protege slot within this reference.
     * 
     * @param slotName
     *            The Protege slot name.
     * @return True if a filler was recognized for the slot within this reference.
     */
    public boolean hasSlotValue(String slotName) {
        if (slotName == null)
            return false;
        boolean result = false;
        if (information != null) {
            for (InfoPacket pair : information) {
                DMAPItem key = pair.getKey();
                if (key instanceof ProtegeSlotItem) {
                    Slot slot = ((ProtegeSlotItem) key).getSlot();
                    if (slot != null) {
                        if (slotName.equals(slot.getName())) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all instances of the DMAPItem encapsulated by this reference that are consistent with the slot fillers contained in this reference. Ie. get
     * all of the instances that are described by this reference.
     * 
     * @return All of the consistent instances.
     */
    public Collection<DMAPItem> getConsistentInstances() {
        return item.findInstances(information);
    }

    /**
     * Check whether the DMAPItem encapsulated by this reference is an instantiated instance.
     * 
     * @return True if this reference corresponds to an instance.
     */
    public boolean isInstance() {
        if (item == null) {
            return false;
        } else {
            return item.isInstance();
        }
    }

    /**
     * Check whether the DMAPItem encapsulated by this reference <i>isa</i> member of the Protege class with the given name.
     * 
     * @param ancestor
     *            The name of the ancestor class to be <i>isa</i>.
     * @return True if this reference <i>isa</i> the given class name.
     */
    public boolean isa(String ancestor) {
        if (item == null) {
            return false;
        } else {
            return item.isa(ancestor);
        }
    }

    /**
     * Check whether the DMAPItem encapsulated by this reference <i>isa</i> child of the DMAPItem encapulated by the supplied argument reference.
     * 
     * @param ancestor
     *            The ancestor reference to be <i>isa</i>.
     * @return True if this reference <i>isa</i> child (or the same as) the given reference.
     */
    public boolean isa(Reference ancestor) {
        if ((item == null) || (ancestor.item == null)) {
            return false;
        } else {
            return item.isa(ancestor.item);
        }
    }

    /**
     * Check whether this reference is identical to another.
     */
    public boolean equals(Object other) {
        if (other instanceof Reference) {
            Reference ref = (Reference) other;
            if (ref == this) {
                return true;
            } else if (item == null) {
                return (ref.item == null);
            } else if (!item.equals(ref.item)) {
                return false;
                // } else if ((start != ref.start) || (end != ref.end)) {
                // return false;
            } else if ((information == null) && (ref.information == null)) {
                return true;
            } else if ((information == null) || (ref.information == null)) {
                return false;
            } else if (information.size() != ref.information.size()) {
                return false;
            } else {
                // Check that all slot fillers are equal
                for (InfoPacket slot : information) {
                    if (!memberOf(slot, ref.information))
                        return false;
                }
                for (InfoPacket slot : ref.information) {
                    if (!memberOf(slot, information))
                        return false;
                }
                // Check all the properties
                if (properties != null) {
                    for (String prop : properties) {
                        if (!ref.hasProperty(prop))
                            return false;
                    }
                }
                if (ref.properties != null) {
                    for (String prop : ref.properties) {
                        if (!hasProperty(prop))
                            return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean memberOf(InfoPacket slot, Vector<InfoPacket> slots) {
        if (slot == null)
            return false;
        DMAPItem key = slot.getKey();
        Reference val = slot.getValue();
        for (InfoPacket pair : slots) {
            if (pair == slot)
                return true;
            if (key.equals(pair.getKey())) {
                if (val.equals(pair.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get a natural language string that describes the DMAPItem encapsulated by this reference. Ie. a string that is suitable for 'speaking' this
     * item.
     * 
     * @return A string suitable for using as part of an output sentence.
     */
    public String getText() {
        if (item == null) {
            return "";
        } else {
            return item.getText();
        }
    }

    /**
     * Check whether this reference subsumes the given reference. One reference subsumes another if it has the other as a slot filler, or if the other
     * is a child type and all of its slots are subsumed.
     * 
     * @param r
     *            The reference that might be subsumed.
     * @return True if this reference subsumes the given one.
     */
    public boolean subsumes(Reference r) {
        return subsumes(r, false);
    }

    /**
     * Check whether this reference subsumes the given reference. One reference subsums another if it has the other as a slot filler, or if the other
     * is a child type and all of its slots are subsumed. If strict is true, then all the tokens in r must fall within the span of this reference as
     * well.
     * 
     * @param r
     *            The reference that might be subsumed.
     * @param strict
     *            True if this reference must 'span' r to subsume it.
     * @return True if this reference subsumes the given one.
     */
    public boolean subsumes(Reference r, boolean strict) {
        if (this.equals(r)) {
            // Don't say that a reference subsumes itself
            return false;
        } else {
            return subsumes(r, this, strict);
        }
    }

    /**
     * Check whether s satisfies the strictness constraint of subsuming r.
     * 
     * @param r
     *            The reference that might be subsumed
     * @param s
     *            The reference that might subsume r
     * @param strict
     *            True if this subsumption must be strict
     * @return True if subsumption with the specified strictness is possible
     */
    private boolean checkStrictness(Reference r, Reference s, boolean strict) {
        // If we are being strict, then s must "span" r to subsume it
        if (!strict) {
            return true;
        } else {
            return ((s.getStart() <= r.getStart()) && (r.getEnd() <= s.getEnd()));
        }
    }

    /**
     * Check whether reference r is subsumed by reference s.
     * 
     * @param r
     *            The reference that might be subsumed.
     * @param s
     *            The reference that might be subsuming.
     * @param strict
     *            True if the subsumption must be strict.
     * @return True if s subsumes r.
     */
    private boolean subsumes(Reference r, Reference s, boolean strict) {
        if (!checkStrictness(r, s, strict)) {
            // r must be spanned by s and it isn't
            return false;
        } else if (isSlotFiller(r, s)) {
            // r fills a slot, subsumed
            return true;
        } else if (r.isa(s)) {
            // r is a subsumed type (ie. the same or a child)
            boolean isSubsumed = true;
            // See if slots match
            Collection<InfoPacket> information = r.getInformation();
            if ((information == null) || information.isEmpty()) {
                // r has no slots, it may be subsumed by s
                information = s.getInformation();
                if ((information != null) && !information.isEmpty()) {
                    // s has some slots, r doesn't, r is subsumed
                    return true;
                } else {
                    if (subsumesProperties(r, s)) {
                        // s has not slots, r has not slots, s subsumes r's properties, r is subsumed
                        return true;
                    } else {
                        // s has not slots, r has not slots, s doe not subsumes r's properties, r is not subsumed
                        return false;
                    }
                }
            } else {
                // check each slot in r to see if it subsumed by the corresponding slot in s
                for (InfoPacket pair : information) {
                    DMAPItem rSlot = pair.getKey();
                    Reference rFiller = pair.getValue();
                    // get the corresponding slot in s
                    if (rSlot instanceof ProtegeSlotItem) {
                        Collection<Reference> sFillers = s.getSlotValues(rSlot);
                        if (sFillers == null) {
                            // no corresponding slot in s, not subsumed
                            isSubsumed = false;
                            break;
                        } else {
                            // check if any slot filler subsumes r slot filler
                            boolean isSubSubsumed = false;
                            for (Reference sf : sFillers) {
                                if (subsumes(rFiller, sf, strict)) {
                                    // r slot is subsumed by slot in s
                                    isSubSubsumed = true;
                                    break;
                                }
                            }
                            if (!isSubSubsumed) {
                                isSubsumed = false;
                                break;
                            }
                        }
                    }
                }
                if (isSubsumed) {
                    // If we got to here, every slot in r is subsumed by a slot in s, check properties
                    if (subsumesProperties(r, s)) {
                        // s subsumes r's properties, r is subsumed
                        return true;
                    } else {
                        // s does not subsumes r's properties, r is not subsumed
                        return false;
                    }
                } else {
                    // Properties of s do not subsume properties of r
                    return false;
                }
            }
        } else {
            // r is not a subsumed type
            return false;
        }
    }

    /**
     * Check whether the properties of s subsume the properties of r
     * 
     * @param r
     *            The reference that might be subsumed
     * @param s
     *            The reference that might subsume r
     * @return True if the properties in s subsume the properties in r
     */
    private boolean subsumesProperties(Reference r, Reference s) {
        boolean isSubsumed = true;
        // If we got to here, every slot in r is subsumed by a slot in s, check our properties
        if ((r.properties != null) && !r.properties.isEmpty()) {
            if ((s.properties != null) && !s.properties.isEmpty()) {
                if (s.properties.size() < r.properties.size()) {
                    // r has more properties than s
                    isSubsumed = false;
                } else {
                    // see if s has every property in r
                    for (String rprop : r.properties) {
                        boolean sHasProperty = false;
                        for (String sprop : s.properties) {
                            if (rprop.equalsIgnoreCase(sprop)) {
                                sHasProperty = true;
                                break;
                            }
                        }
                        if (!sHasProperty) {
                            isSubsumed = false;
                            break;
                        }
                    }
                }
            } else {
                // r has properties and s doesn't
                isSubsumed = false;
            }
        }
        return isSubsumed;
    }

    /**
     * Determine if a reference is a slot filler within another reference.
     * 
     * @param r
     *            The reference to check
     * @param s
     *            The reference with the slots it might fill
     * @value True if r is a slot filler in s
     */
    private boolean isSlotFiller(Reference r, Reference s) {
        Collection<InfoPacket> information = s.getInformation();
        if (information == null) {
            return false;
        } else {
            for (InfoPacket pair : information) {
                Reference filler = pair.getValue();
                if ((filler == r) || isSlotFiller(r, filler)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Get the prediction that corresponds to this Reference. This is used primarily for debugging purposes when tracing back from a Reference to the
     * Pattern that was used to create it.
     * 
     * @return the Prediction corresponding to this reference
     */
    public Prediction getPrediction() {
        return prediction;
    }

    /**
     * Set the prediction that corresponds to this Reference. This is usually done in the constructor to the Reference, so this method is seldom used.
     * 
     * @param prediction
     */
    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }
    
    /**
     * Find any unfilled slots in this Reference.
     * @return
     */
	@SuppressWarnings("unchecked")
	public Collection<String> missingSlots() {
		ArrayList<String> missingSlots = new ArrayList<String>();
		DMAPItem refItem = getItem();
		if ( refItem instanceof ProtegeFrameItem ) {
			// test for "empty" slots
			Frame frame = ((ProtegeFrameItem)refItem).getFrame();
			if ( frame instanceof Cls ) {
				Cls cls = (Cls)frame;
				//Collection<Slot> slots = cls.getDirectTemplateSlots();
				Collection<Slot> slots = cls.getTemplateSlots();
				for (Slot slot : slots) {
					// check for a filler in the Reference for each slot
					//Collection values = frame.getOwnSlotValue(slot);
					//for (Object val : values) {
					String slotName = slot.getName();
					if ( !slotName.startsWith(":") &&  !hasSlotValue(slotName)) {
						missingSlots.add(slotName);
					}
					//}
				}
			}
		}

		// other DMAPItems are fully specified by definition (no slots to fill)
		//TODO unless we need to deal with subslots in slots?!
		return missingSlots;
	}
	
    /**
     * Return the proportion of slots filled
     * @return
     */
	@SuppressWarnings("unchecked")
	public double filledSlotScore() {
		int countSlots = 0;
		int countMissing = 0;
		double score = 1.0d;
		
		ArrayList<String> missingSlots = new ArrayList<String>();
		DMAPItem refItem = getItem();
		if ( refItem instanceof ProtegeFrameItem ) {
			// test for "empty" slots
			Frame frame = ((ProtegeFrameItem)refItem).getFrame();
			if ( frame instanceof Cls ) {
				Cls cls = (Cls)frame;
				//Collection<Slot> slots = cls.getDirectTemplateSlots();
				Collection<Slot> slots = cls.getTemplateSlots();
				for (Slot slot : slots) {
					// check for a filler in the Reference for each slot
					//Collection values = frame.getOwnSlotValue(slot);
					//for (Object val : values) {
					countSlots++;
					String slotName = slot.getName();
					if ( !slotName.startsWith(":") &&  !hasSlotValue(slotName)) {
						countMissing++;
						missingSlots.add(slotName);
					}
					//}
				}
			}

			if ( countSlots != 0 ) {
				score = ((double)(countSlots - countMissing + 1)/(countSlots + 1));
			}
		} else {
			// other DMAPItems are fully specified by definition (no slots to fill)
			//TODO unless we need to deal with subslots in slots?!
			return 1.0d;
		}
		return score;
	}
    
    /** 
     * Check whether this Reference unifies with another Reference.
     * They must match on type, and each slot filler must be compatible.
     * @param r
     * @return
     */
    public boolean unifies(Reference r) {
    	// check DMAPItem type
    	if ( this.getItem().getClass().equals(r.getItem().getClass())) {
    		// check semantic type
    		//TODO
    	}
    	
    	return false;
    }

}
