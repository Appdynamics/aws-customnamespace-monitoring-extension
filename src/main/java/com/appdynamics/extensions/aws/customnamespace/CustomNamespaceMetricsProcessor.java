/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace;

import com.appdynamics.extensions.aws.config.Dimension;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.NamespaceMetricStatistics;
import com.appdynamics.extensions.aws.metric.StatisticType;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.aws.predicate.MultiDimensionPredicate;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.metrics.Metric;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Prashant Mehta
 */
public class CustomNamespaceMetricsProcessor implements MetricsProcessor {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(CustomNamespaceMetricsProcessor.class);

    private List<IncludeMetric> includeMetrics;
    private List<Dimension> dimensions;
    private String namespace;

    //private List<IncludeMetric> accountIncludeMetrics = Lists.newArrayList();

    public CustomNamespaceMetricsProcessor(List<IncludeMetric> includeMetrics, List<Dimension> dimensions, String namespace) {
        this.includeMetrics = includeMetrics;
        this.dimensions = dimensions;
        this.namespace = namespace;
        
        LOGGER.info("Initialized CustomNamespaceMetricsProcessor for namespace: {} with {} include metrics and {} dimensions", 
                namespace, includeMetrics.size(), dimensions.size());
        
        LOGGER.info("Include metrics: {}", 
                    includeMetrics.stream().map(IncludeMetric::getName).toArray());
        LOGGER.info("Dimensions: {}", 
                dimensions.stream().map(d -> d.getName() + " (" + d.getDisplayName() + ")").toArray());
        
    }

    @Override
    public synchronized List<AWSMetric> getMetrics(CloudWatchClient awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        LOGGER.info("Starting metric collection for account: {} in namespace: {}", accountName, namespace);
        
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        LOGGER.info("Created {} dimension filters: {}", dimensionFilters.size(), 
                dimensionFilters.stream().map(df -> df.name()).toArray());
        
        MultiDimensionPredicate predicate = new MultiDimensionPredicate(dimensions);
        List<AWSMetric> metrics = MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestsCounter,
                namespace, includeMetrics, dimensionFilters, predicate);
        
        LOGGER.info("Collected {} metrics for account: {} in namespace: {}", metrics.size(), accountName, namespace);
        
        for (AWSMetric awsMetric : metrics) {
            LOGGER.info("Collected metric: {}", awsMetric.toString());
        }
        
        
        return metrics;
    }

    @Override
    public synchronized StatisticType getStatisticType(AWSMetric awsMetric) {
        return MetricsProcessorHelper.getStatisticType(awsMetric.getIncludeMetric(), includeMetrics);
    }

    private List<DimensionFilter> getDimensionFilters() {
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();
        for (Dimension dimension : dimensions) {
            DimensionFilter dimensionFilter = DimensionFilter.builder().name(dimension.getName()).build();
            dimensionFilters.add(dimensionFilter);
        }
        return dimensionFilters;
    }

    public List<Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        LOGGER.info("Creating metric stats map for upload from namespace: {}", namespace);
        
        Map<String, String> dimensionToMetricPathNameDictionary = new HashMap<String, String>();
        for (Dimension dimension : dimensions) {
            dimensionToMetricPathNameDictionary.put(dimension.getName(), dimension.getDisplayName());
        }

        List<Metric> metrics = MetricsProcessorHelper.createMetricStatsMapForUpload(namespaceMetricStats,
                dimensionToMetricPathNameDictionary, true);
        
        LOGGER.info("Created {} metrics for upload to AppDynamics Controller", metrics.size());
        
        for (Metric metric : metrics) {
            LOGGER.info("Uploading metric: {} = {} ({})", 
                    metric.getMetricPath(), 
                    metric.getMetricValue(), 
                    metric.getAggregationType());
        }
        
        return metrics;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }
}