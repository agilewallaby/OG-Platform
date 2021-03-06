/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.volatility.cube.AggregatingVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.BloombergVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.ConfigDBVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.rest.DataVolatilityCubeDefinitionSourceResource;

/**
 * Component factory providing the {@code VolatilityCubeDefinitionSource}.
 */
@BeanDefinition
public class VolatilityCubeDefinitionSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The config source to wrap.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;
  /**
   * The flag to include the Bloomberg source.
   */
  @PropertyDefinition
  private boolean _bloomberg;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    ComponentInfo info = new ComponentInfo(VolatilityCubeDefinitionSource.class, getClassifier());
    VolatilityCubeDefinitionSource base = new ConfigDBVolatilityCubeDefinitionSource(getConfigSource());
    if (isBloomberg()) {
      VolatilityCubeDefinitionSource bbg = new BloombergVolatilityCubeDefinitionSource();
      VolatilityCubeDefinitionSource combined = new AggregatingVolatilityCubeDefinitionSource(Arrays.asList(bbg, base));
      repo.registerComponent(info, combined);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataVolatilityCubeDefinitionSourceResource(combined));
      }
    } else {
      repo.registerComponent(info, base);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataVolatilityCubeDefinitionSourceResource(base));
      }
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilityCubeDefinitionSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static VolatilityCubeDefinitionSourceComponentFactory.Meta meta() {
    return VolatilityCubeDefinitionSourceComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(VolatilityCubeDefinitionSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public VolatilityCubeDefinitionSourceComponentFactory.Meta metaBean() {
    return VolatilityCubeDefinitionSourceComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -614707837:  // publishRest
        return isPublishRest();
      case 195157501:  // configSource
        return getConfigSource();
      case 1218257467:  // bloomberg
        return isBloomberg();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
      case 195157501:  // configSource
        setConfigSource((ConfigSource) newValue);
        return;
      case 1218257467:  // bloomberg
        setBloomberg((Boolean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_configSource, "configSource");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      VolatilityCubeDefinitionSourceComponentFactory other = (VolatilityCubeDefinitionSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(isBloomberg(), other.isBloomberg()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(isBloomberg());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source to wrap.
   * @return the value of the property, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source to wrap.
   * @param configSource  the new value of the property, not null
   */
  public void setConfigSource(ConfigSource configSource) {
    JodaBeanUtils.notNull(configSource, "configSource");
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag to include the Bloomberg source.
   * @return the value of the property
   */
  public boolean isBloomberg() {
    return _bloomberg;
  }

  /**
   * Sets the flag to include the Bloomberg source.
   * @param bloomberg  the new value of the property
   */
  public void setBloomberg(boolean bloomberg) {
    this._bloomberg = bloomberg;
  }

  /**
   * Gets the the {@code bloomberg} property.
   * @return the property, not null
   */
  public final Property<Boolean> bloomberg() {
    return metaBean().bloomberg().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilityCubeDefinitionSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", VolatilityCubeDefinitionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", VolatilityCubeDefinitionSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", VolatilityCubeDefinitionSourceComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code bloomberg} property.
     */
    private final MetaProperty<Boolean> _bloomberg = DirectMetaProperty.ofReadWrite(
        this, "bloomberg", VolatilityCubeDefinitionSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "configSource",
        "bloomberg");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case 195157501:  // configSource
          return _configSource;
        case 1218257467:  // bloomberg
          return _bloomberg;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends VolatilityCubeDefinitionSourceComponentFactory> builder() {
      return new DirectBeanBuilder<VolatilityCubeDefinitionSourceComponentFactory>(new VolatilityCubeDefinitionSourceComponentFactory());
    }

    @Override
    public Class<? extends VolatilityCubeDefinitionSourceComponentFactory> beanType() {
      return VolatilityCubeDefinitionSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    /**
     * The meta-property for the {@code bloomberg} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> bloomberg() {
      return _bloomberg;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
