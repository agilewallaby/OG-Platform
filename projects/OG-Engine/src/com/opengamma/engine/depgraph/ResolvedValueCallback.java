/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Callback interface to receive the results of a resolution.
 */
/* package */interface ResolvedValueCallback {

  /**
   * Notifies the implementer of a successful resolution.
   * 
   * @param context graph building context
   * @param valueRequirement requirement resolved
   * @param resolvedValue the resolved specification
   * @param pump a pump callback for providing the next possible resolution (or a failure)
   */
  void resolved(GraphBuildingContext context, ValueRequirement valueRequirement, ResolvedValue resolvedValue, ResolutionPump pump);

  /**
   * Notifies the implementer of a failed resolution (or no more successful ones).
   * 
   * @param context graph building context
   * @param value requirement that couldn't be resolved
   */
  void failed(GraphBuildingContext context, ValueRequirement value);

  /**
   * {@link ContextRunnable} form of the {@link #resolved} method.
   */
  class Resolved implements ContextRunnable {

    private final ResolvedValueCallback _instance;
    private final ValueRequirement _valueRequirement;
    private final ResolvedValue _resolvedValue;
    private final ResolutionPump _pump;

    public Resolved(final ResolvedValueCallback instance, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
      _instance = instance;
      _valueRequirement = valueRequirement;
      _resolvedValue = resolvedValue;
      _pump = pump;
    }

    @Override
    public void run(final GraphBuildingContext context) {
      _instance.resolved(context, _valueRequirement, _resolvedValue, _pump);
    }

  }

  /**
   * {@link ContextRunnable} form of the {@link #failed} method.
   */
  class Failed implements ContextRunnable {

    private final ResolvedValueCallback _instance;
    private final ValueRequirement _value;

    public Failed(final ResolvedValueCallback instance, final ValueRequirement value) {
      _instance = instance;
      _value = value;
    }

    @Override
    public void run(final GraphBuildingContext context) {
      _instance.failed(context, _value);
    }

  }

}