/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.spark.flint.operation;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.spark.asyncquery.model.AsyncQueryRequestContext;
import org.opensearch.sql.spark.client.EMRServerlessClientFactory;
import org.opensearch.sql.spark.dispatcher.model.FlintIndexOptions;
import org.opensearch.sql.spark.flint.FlintIndexMetadata;
import org.opensearch.sql.spark.flint.FlintIndexMetadataService;
import org.opensearch.sql.spark.flint.FlintIndexState;
import org.opensearch.sql.spark.flint.FlintIndexStateModel;
import org.opensearch.sql.spark.flint.FlintIndexStateModelService;
import org.opensearch.sql.spark.scheduler.AsyncQueryScheduler;

/**
 * Index Operation for Altering the flint index. Only handles alter operation when
 * auto_refresh=false.
 */
public class FlintIndexOpAlter extends FlintIndexOp {
  private static final Logger LOG = LogManager.getLogger(FlintIndexOpAlter.class);
  private final FlintIndexMetadataService flintIndexMetadataService;
  private final FlintIndexOptions flintIndexOptions;
  private final AsyncQueryScheduler asyncQueryScheduler;

  public FlintIndexOpAlter(
      FlintIndexOptions flintIndexOptions,
      FlintIndexStateModelService flintIndexStateModelService,
      String datasourceName,
      EMRServerlessClientFactory emrServerlessClientFactory,
      FlintIndexMetadataService flintIndexMetadataService,
      AsyncQueryScheduler asyncQueryScheduler) {
    super(flintIndexStateModelService, datasourceName, emrServerlessClientFactory);
    this.flintIndexMetadataService = flintIndexMetadataService;
    this.flintIndexOptions = flintIndexOptions;
    this.asyncQueryScheduler = asyncQueryScheduler;
  }

  @Override
  protected boolean validate(FlintIndexState state) {
    return state == FlintIndexState.ACTIVE || state == FlintIndexState.REFRESHING;
  }

  @Override
  FlintIndexState transitioningState() {
    return FlintIndexState.UPDATING;
  }

  @SneakyThrows
  @Override
  void runOp(
      FlintIndexMetadata flintIndexMetadata,
      FlintIndexStateModel flintIndexStateModel,
      AsyncQueryRequestContext asyncQueryRequestContext) {
    LOG.debug(
        "Running alter index operation for index: {}", flintIndexMetadata.getOpensearchIndexName());
    this.flintIndexMetadataService.updateIndexToManualRefresh(
        flintIndexMetadata.getOpensearchIndexName(), flintIndexOptions, asyncQueryRequestContext);
    if (flintIndexMetadata.getFlintIndexOptions().isExternalScheduler()) {
      asyncQueryScheduler.unscheduleJob(
          flintIndexMetadata.getOpensearchIndexName(), asyncQueryRequestContext);
    } else {
      cancelStreamingJob(flintIndexStateModel);
    }
  }

  @Override
  FlintIndexState stableState() {
    return FlintIndexState.ACTIVE;
  }
}
