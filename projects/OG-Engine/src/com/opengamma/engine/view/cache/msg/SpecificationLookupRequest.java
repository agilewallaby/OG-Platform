// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class SpecificationLookupRequest extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitSpecificationLookupRequest (this); }
  private static final long serialVersionUID = -51269628524l;
  private java.util.List<Long> _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  public SpecificationLookupRequest (java.util.Collection<? extends Long> identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<Long> (identifier);
      if (identifier.size () == 0) throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      for (java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Long fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'identifier' cannot be null");
      }
      _identifier = fudge0;
    }
  }
  protected SpecificationLookupRequest (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (IDENTIFIER_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a SpecificationLookupRequest - field 'identifier' is not present");
    _identifier = new java.util.ArrayList<Long> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        _identifier.add (fudgeMsg.getFieldValue (Long.class, fudge1));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SpecificationLookupRequest - field 'identifier' is not long", e);
      }
    }
  }
  public SpecificationLookupRequest (Long correlationId, java.util.Collection<? extends Long> identifier) {
    super (correlationId);
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<Long> (identifier);
      if (identifier.size () == 0) throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      for (java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Long fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'identifier' cannot be null");
      }
      _identifier = fudge0;
    }
  }
  protected SpecificationLookupRequest (final SpecificationLookupRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._identifier == null) _identifier = null;
    else {
      _identifier = new java.util.ArrayList<Long> (source._identifier);
    }
  }
  public SpecificationLookupRequest clone () {
    return new SpecificationLookupRequest (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_identifier != null)  {
      for (Long fudge1 : _identifier) {
        msg.add (IDENTIFIER_KEY, null, fudge1);
      }
    }
  }
  public static SpecificationLookupRequest fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.SpecificationLookupRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.SpecificationLookupRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SpecificationLookupRequest (fudgeMsg);
  }
  public java.util.List<Long> getIdentifier () {
    return java.util.Collections.unmodifiableList (_identifier);
  }
  public void setIdentifier (Long identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      _identifier = new java.util.ArrayList<Long> (1);
      addIdentifier (identifier);
    }
  }
  public void setIdentifier (java.util.Collection<? extends Long> identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<Long> (identifier);
      if (identifier.size () == 0) throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      for (java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Long fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'identifier' cannot be null");
      }
      _identifier = fudge0;
    }
  }
  public void addIdentifier (Long identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    if (_identifier == null) _identifier = new java.util.ArrayList<Long> ();
    _identifier.add (identifier);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
