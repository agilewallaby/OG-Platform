/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.ClientBayeuxListener;
import org.cometd.Message;
import org.cometd.server.BayeuxService;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.financial.aggregation.PortfolioAggregationFunctions;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * The core of the back-end to the web client, providing the implementation of the Bayeux protocol.
 */
public class LiveResultsService extends BayeuxService implements ClientBayeuxListener {
  
  private static final Logger s_logger = LoggerFactory.getLogger(LiveResultsService.class);
  private static final Pattern s_guidPattern = Pattern.compile("(\\{?([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}?)");
  
  private final Map<String, WebView> _clientViews = new HashMap<String, WebView>();
  
  /**
   * The executor service used to call web clients back asynchronously.
   */
  private final ExecutorService _executorService;
    
  private final ViewProcessor _viewProcessor;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;
  private final NamedMarketDataSpecificationRepository _namedMarketDataSpecificationRepository;
  private final AggregatedViewDefinitionManager _aggregatedViewDefinitionManager;
  
  public LiveResultsService(final Bayeux bayeux, final ViewProcessor viewProcessor,
      final PositionSource positionSource, final SecuritySource securitySource,
      final PortfolioMaster userPortfolioMaster, final PositionMaster userPositionMaster,
      final ManageableViewDefinitionRepository userViewDefinitionRepository,
      final MarketDataSnapshotMaster snapshotMaster, final UserPrincipal user, final ExecutorService executorService,
      final FudgeContext fudgeContext, final NamedMarketDataSpecificationRepository namedMarketDataSpecificationRepository,
      final PortfolioAggregationFunctions portfolioAggregators) {
    super(bayeux, "processPortfolioRequest");
    ArgumentChecker.notNull(bayeux, "bayeux");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(userPortfolioMaster, "userPortfolioMaster");
    ArgumentChecker.notNull(userPositionMaster, "userPositionMaster");
    ArgumentChecker.notNull(userViewDefinitionRepository, "userViewDefinitionRepository");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(executorService, "executorService");
    ArgumentChecker.notNull(namedMarketDataSpecificationRepository, "namedMarketDataSpecificationRepository");
    ArgumentChecker.notNull(portfolioAggregators, "portfolioAggregators");
    
    _viewProcessor = viewProcessor;
    _snapshotMaster = snapshotMaster;
    _user = user;
    _executorService = executorService;
    _resultConverterCache = new ResultConverterCache(fudgeContext);
    _namedMarketDataSpecificationRepository = namedMarketDataSpecificationRepository;
    _aggregatedViewDefinitionManager = new AggregatedViewDefinitionManager(positionSource, securitySource,
        viewProcessor.getViewDefinitionRepository(), userViewDefinitionRepository, userPortfolioMaster, userPositionMaster,
        portfolioAggregators.getMappedFunctions());
    
    viewProcessor.getViewDefinitionRepository().changeManager().addChangeListener(new ChangeListener() {

      @Override
      public void entityChanged(ChangeEvent event) {
        sendInitData(false);
      }
      
    });
    
    s_logger.info("Subscribing to services");
    subscribe("/service/getInitData", "processInitDataRequest");
    subscribe("/service/changeView", "processChangeViewRequest");
    subscribe("/service/updates", "processUpdateRequest");
    subscribe("/service/updates/mode", "processUpdateModeRequest");
    subscribe("/service/updates/depgraph", "processDepGraphRequest");
    subscribe("/service/currentview/pause", "processPauseRequest");
    subscribe("/service/currentview/resume", "processResumeRequest");
    getBayeux().addListener(this);
    s_logger.info("Finished subscribing to services");
  }

  @Override
  public void clientAdded(Client client) {
    s_logger.debug("Client " + client.getId() + " connected");
  }

  @Override
  public void clientRemoved(Client client) {
    // Tidy up
    s_logger.debug("Client " + client.getId() + " disconnected");
    if (_clientViews.containsKey(client.getId())) {
      WebView view = _clientViews.remove(client.getId());
      shutDownWebView(view);
    }
  }
  
  public WebView getClientView(String clientId) {
    synchronized (_clientViews) {
      return _clientViews.get(clientId);
    }
  }

  private void initializeClientView(final Client remote, final UniqueId baseViewDefinitionId, final String aggregatorName, final ViewExecutionOptions executionOptions, final UserPrincipal user) {
    synchronized (_clientViews) {
      WebView webView = _clientViews.get(remote.getId());
      
      if (webView != null) {
        if (webView.matches(baseViewDefinitionId, aggregatorName, executionOptions)) {
          // Already initialized
          webView.reconnected();
          return;
        }
        // Existing view is different - client is switching views
        shutDownWebView(webView);
        _clientViews.remove(remote.getId());
      }
      
      ViewClient viewClient = getViewProcessor().createViewClient(user);
      try {
        UniqueId viewDefinitionId = _aggregatedViewDefinitionManager.getViewDefinitionId(baseViewDefinitionId, aggregatorName);
        webView = new WebView(getClient(), remote, viewClient, baseViewDefinitionId, aggregatorName, viewDefinitionId,
            executionOptions, user, getExecutorService(), getResultConverterCache());
      } catch (Exception e) {
        _aggregatedViewDefinitionManager.releaseViewDefinition(baseViewDefinitionId, aggregatorName);
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + baseViewDefinitionId + "'", e);
      }
      _clientViews.put(remote.getId(), webView);
    }
  }

  private void shutDownWebView(WebView webView) {
    webView.shutdown();
    _aggregatedViewDefinitionManager.releaseViewDefinition(webView.getBaseViewDefinitionId(), webView.getAggregatorName());
  }

  private UserPrincipal getUser(Client remote) {
    return _user;
  }
  
  private ExecutorService getExecutorService() {
    return _executorService;
  }

  private ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  private ResultConverterCache getResultConverterCache() {
    return _resultConverterCache;
  }
  
  public void processUpdateRequest(Client remote, Message message) {
    s_logger.info("Received portfolio data request from {}, getting client view...", remote);
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      // Disconnected client has come back to life
      return;
    }
    webView.triggerUpdate(message);
  }
  
  @SuppressWarnings("unchecked")
  public void processUpdateModeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    String gridName = (String) dataMap.get("gridName");
    long jsRowId = (Long) dataMap.get("rowId");
    long jsColId = (Long) dataMap.get("colId");
    ConversionMode mode = ConversionMode.valueOf((String) dataMap.get("mode"));
    WebViewGrid grid = webView.getGridByName(gridName);
    if (grid == null) {
      s_logger.warn("Request to change update mode for cell in unknown grid '{}'", gridName);
    }
    grid.setConversionMode(WebGridCell.of((int) jsRowId, (int) jsColId), mode);
  }
  
  @SuppressWarnings("unchecked")
  public void processDepGraphRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    String gridName = (String) dataMap.get("gridName");
    long jsRowId = (Long) dataMap.get("rowId");
    long jsColId = (Long) dataMap.get("colId");
    boolean includeDepGraph = (Boolean) dataMap.get("includeDepGraph");
    webView.setIncludeDepGraph(gridName, WebGridCell.of((int) jsRowId, (int) jsColId), includeDepGraph);
  }

  public void processInitDataRequest(Client remote, Message message) {
    s_logger.info("processInitDataRequest");
    sendInitData(true);
  }
  
  private void sendInitData(boolean includeSnapshots) {
    Map<String, Object> reply = new HashMap<String, Object>();
    
    Object availableViewDefinitions = getViewDefinitions();
    reply.put("viewDefinitions", availableViewDefinitions);
    
    List<String> aggregatorNames = getAggregatorNames();
    reply.put("aggregatorNames", aggregatorNames);
    
    if (includeSnapshots) {
      List<String> marketDataSpecificationNames = getMarketDataSpecificationNames();
      reply.put("specifications", marketDataSpecificationNames);
      Map<String, Map<String, String>> snapshotDetails = getSnapshotDetails();
      reply.put("snapshots", snapshotDetails);
    }
    
    getBayeux().getChannel("/initData", true).publish(getClient(), reply, null);
  }

  private List<Map<String, String>> getViewDefinitions() {
    List<Map<String, String>> result = new ArrayList<Map<String, String>>();
    Map<UniqueId, String> availableViewEntries = _viewProcessor.getViewDefinitionRepository().getDefinitionEntries();
    s_logger.debug("Available view entries: " + availableViewEntries);
    for (Map.Entry<UniqueId, String> entry : availableViewEntries.entrySet()) {
      if (s_guidPattern.matcher(entry.getValue()).find()) {
        s_logger.debug("Ignoring view definition which appears to have an auto-generated name: {}", entry.getValue());
        continue;
      }
      Map<String, String> resultEntry = new HashMap<String, String>();
      resultEntry.put("id", entry.getKey().toLatest().toString());
      resultEntry.put("name", entry.getValue());
      result.add(resultEntry);
    }
    Collections.sort(result, new Comparator<Map<String, String>>() {

      @Override
      public int compare(Map<String, String> o1, Map<String, String> o2) {
        return o1.get("name").compareTo(o2.get("name"));
      }
      
    });
    return result;
  }
  
  private List<String> getAggregatorNames() {
    List<String> result = new ArrayList<String>();
    result.addAll(_aggregatedViewDefinitionManager.getAggregatorNames());
    Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
    return result;
  }

  private List<String> getMarketDataSpecificationNames() {
    return _namedMarketDataSpecificationRepository.getNames();
  }

  private Map<String, Map<String, String>> getSnapshotDetails() {
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult snapshotSearchResult = _snapshotMaster.search(snapshotSearchRequest);
    List<ManageableMarketDataSnapshot> snapshots = snapshotSearchResult.getSnapshots();
    
    Map<String, Map<String, String>> snapshotsByBasisView = new HashMap<String, Map<String, String>>();
    for (ManageableMarketDataSnapshot snapshot : snapshots) {
      if (snapshot.getUniqueId() == null) {
        s_logger.warn("Ignoring snapshot with null unique identifier {}", snapshot.getName());
        continue;
      }
      if (StringUtils.isBlank(snapshot.getName())) {
        s_logger.warn("Ignoring snapshot {} with no name", snapshot.getUniqueId());
        continue;
      }
      if (s_guidPattern.matcher(snapshot.getName()).find()) {
        s_logger.debug("Ignoring snapshot which appears to have an auto-generated name: {}", snapshot.getName());
        continue;
      }
      String basisViewName = snapshot.getBasisViewName() != null ? snapshot.getBasisViewName() : "unknown";
      Map<String, String> snapshotsForBasisView = snapshotsByBasisView.get(basisViewName);
      if (snapshotsForBasisView == null) {
        snapshotsForBasisView = new HashMap<String, String>();
        snapshotsByBasisView.put(basisViewName, snapshotsForBasisView);
      }
      snapshotsForBasisView.put(snapshot.getUniqueId().toString(), snapshot.getName());
    }
    return snapshotsByBasisView;
  }

  @SuppressWarnings("unchecked")
  public void processChangeViewRequest(Client remote, Message message) {
    try {
      Map<String, Object> data = (Map<String, Object>) message.getData();
      
      String viewIdString = (String) data.get("viewId");
      UniqueId baseViewDefinitionId;
      try {
        baseViewDefinitionId = UniqueId.parse(viewIdString);
      } catch (IllegalArgumentException e) {
        sendChangeViewError(remote, "Invalid view definition identifier format: '" + viewIdString);
        return;
      }
      if (!validateViewDefinitionId(baseViewDefinitionId)) {
        sendChangeViewError(remote, "No view definition with identifier " + baseViewDefinitionId + " could be found");
        return;
      }
      String aggregatorName = (String) data.get("aggregatorName");
      String marketDataType = (String) data.get("marketDataType");
      
      MarketDataSpecification marketDataSpec;
      EnumSet<ViewExecutionFlags> flags;
      if ("snapshot".equals(marketDataType)) {
        String snapshotIdString = (String) data.get("snapshotId");
        if (StringUtils.isBlank(snapshotIdString)) {
          sendChangeViewError(remote, "Unknown snapshot");
          return;
        }
        UniqueId snapshotId = UniqueId.parse(snapshotIdString);
        marketDataSpec = MarketData.user(snapshotId.toLatest());
        flags = ExecutionFlags.none().triggerOnMarketData().get();
      } else if ("live".equals(marketDataType)) {
        String liveMarketDataProvider = (String) data.get("provider");
        marketDataSpec = _namedMarketDataSpecificationRepository.getSpecification(liveMarketDataProvider);
        flags = ExecutionFlags.triggersEnabled().get();
      } else {
        throw new OpenGammaRuntimeException("Unknown market data type: " + marketDataType);
      }
      ViewExecutionOptions executionOptions = ExecutionOptions.infinite(marketDataSpec, flags);
      s_logger.info("Initializing view '{}', aggregated by '{}' with execution options '{}' for client '{}'", new Object[] {baseViewDefinitionId, aggregatorName, executionOptions, remote});
      initializeClientView(remote, baseViewDefinitionId, aggregatorName, executionOptions, getUser(remote));
    } catch (Exception e) {
      s_logger.error("Exception propagated to client while changing view", e);
      sendChangeViewError(remote, "Unexpected error with message: " + e.getMessage());
    }
  }

  private void sendChangeViewError(Client remote, String errorMessage) {
    s_logger.info("Notifying client of error changing view: " + errorMessage);
    Map<String, String> reply = new HashMap<String, String>();
    reply.put("isError", "true");
    reply.put("message", "Unable to change view. " + errorMessage + ".");
    remote.deliver(getClient(), "/changeView", reply, null);
  }
  
  private boolean validateViewDefinitionId(UniqueId viewDefinitionId) {
    try {
      return _viewProcessor.getViewDefinitionRepository().getDefinition(viewDefinitionId) != null;
    } catch (Exception e) {
      s_logger.warn("Error validating view definition ID " + viewDefinitionId, e);
      return false;
    }
  }
  
  public void processPauseRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    webView.pause();
  }
  
  public void processResumeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    webView.resume();
  }
  
}
