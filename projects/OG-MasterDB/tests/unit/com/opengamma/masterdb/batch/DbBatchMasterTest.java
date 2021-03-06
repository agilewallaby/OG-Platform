/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ViewCycleMetadata;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.tuple.Pair;

public class DbBatchMasterTest extends DbTest {

  private DbBatchMaster _batchMaster;
  private ViewCycleMetadata _cycleMetadataStub;
  private ComputationTargetSpecification _compTargetSpec;
  private ValueRequirement _requirement;
  private ValueSpecification _specification;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchMasterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();

    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _batchMaster = (DbBatchMaster) context.getBean(getDatabaseType() + "DbBatchMaster");

    final String calculationConfigName = "config_1";

    _compTargetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL"));
    final Security security = new SimpleSecurity(_compTargetSpec.getUniqueId(), ExternalIdBundle.of("Sec", "APPL"), "equity", "APPL");                            

    _requirement = new ValueRequirement("FAIR_VALUE", security);
    _specification = new ValueSpecification(_requirement, "IDENTITY_FUNCTION");

    _cycleMetadataStub = new ViewCycleMetadata() {

      @Override
      public UniqueId getViewCycleId() {
        return UniqueId.of("viewcycle", "viewcycle", "viewcycle");
      }
      
      @Override
      public Collection<String> getAllCalculationConfigurationNames() {
        return newArrayList(calculationConfigName);
      }

      @Override
      public Collection<com.opengamma.engine.ComputationTarget> getComputationTargets(String configurationName) {
        if (configurationName.equals(calculationConfigName)) {
          return newArrayList(
            new com.opengamma.engine.ComputationTarget(ComputationTargetType.PRIMITIVE, new UniqueIdentifiable() {
              @Override
              public UniqueId getUniqueId() {
                return UniqueId.of("Primitive", "Value");
              }
            }),
            new com.opengamma.engine.ComputationTarget(
              _compTargetSpec.getType(),
              security
            )
          );
        } else {
          return emptyList();
        }
      }

      @Override
      public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs(String configurationName) {
        return new HashMap<ValueSpecification, Set<ValueRequirement>>() {{
          put(_specification, new HashSet<ValueRequirement>() {{
            add(_requirement);
          }});
        }};
      }

      @Override
      public UniqueId getMarketDataSnapshotId() {
        return UniqueId.of("snapshot", "snapshot", "snapshot");
      }

      @Override
      public Instant getValuationTime() {
        return Instant.now();
      }

      @Override
      public VersionCorrection getVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public UniqueId getViewDefinitionId() {
        return UniqueId.of("viewdef", "viewdef", "viewdef");
      }

    };

  }

  @Test
  public void searchAllBatches() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();                
    _batchMaster.createMarketData(marketDataUid);            
    RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    BatchRunSearchRequest request = new BatchRunSearchRequest();

    Pair<List<RiskRun>, Paging> result = _batchMaster.searchRiskRun(request);
    assertNotNull(result);

    assertEquals(1, result.getFirst().size());
    RiskRun item = result.getFirst().get(0);
    assertNotNull(item.getObjectId());
    assertEquals(item.getValuationTime(), run.getValuationTime());
    assertEquals(false, item.isComplete());

    _batchMaster.endRiskRun(item.getObjectId());
    
    result = _batchMaster.searchRiskRun(request);
    assertEquals(1, result.getFirst().size());
    item = result.getFirst().get(0);
    assertNotNull(item.getObjectId());
    assertEquals(item.getValuationTime(), run.getValuationTime());
    assertEquals(true, item.isComplete());
  }


  @Test
  public void searchOneBatch() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();                
    _batchMaster.createMarketData(marketDataUid);            
    RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    BatchRunSearchRequest request = new BatchRunSearchRequest();
    request.setValuationTime(run.getValuationTime());

    Pair<List<RiskRun>, Paging> result = _batchMaster.searchRiskRun(request);
    assertNotNull(result);

    assertEquals(1, result.getFirst().size());
    RiskRun item = result.getFirst().get(0);
    assertNotNull(item.getObjectId());
    assertEquals(item.getValuationTime(), run.getValuationTime());
    assertEquals(false, item.isComplete());
  }

}
