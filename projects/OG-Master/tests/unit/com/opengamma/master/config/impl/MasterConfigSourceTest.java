/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;

/**
 * Test {@link MasterConfigSource}.
 */
@Test
public class MasterConfigSourceTest {

  private static final ConfigDocument<ExternalId> DOC;
  static {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("Test");
    doc.setValue(ExternalId.of("A", "B"));
    DOC = doc;
  }
  
  private MasterConfigSource _configSource;

  @BeforeMethod
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    ConfigDocument<?> added = configMaster.add(DOC);
    DOC.setUniqueId(added.getUniqueId());
    _configSource = new MasterConfigSource(configMaster);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _configSource = null;
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterConfigSource(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterConfigSource(null, null);
  }

  public void search() throws Exception {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setName("Test");
    request.setType(ExternalId.class);
    List<ExternalId> searchResult = _configSource.search(request);
    assertTrue(searchResult.size() == 1);
    assertEquals(ExternalId.of("A", "B"), searchResult.get(0));
  }

  public void get() throws Exception {
    ExternalId test = _configSource.getConfig(ExternalId.class, DOC.getUniqueId());
    assertEquals(ExternalId.of("A", "B"), test);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void accessInvalidDocument() throws Exception {
    _configSource.getConfig(UniqueId.class, UniqueId.of("U", "1"));
  }

}
