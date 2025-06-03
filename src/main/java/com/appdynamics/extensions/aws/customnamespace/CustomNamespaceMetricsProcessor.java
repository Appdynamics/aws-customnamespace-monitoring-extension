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
import com.google.common.collect.Lists;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.metrics.Metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Prashant Mehta
 */
public class CustomNamespaceMetricsProcessor implements MetricsProcessor {

    private List<IncludeMetric> includeMetrics;
    private List<Dimension> dimensions;
    private String namespace;

    //private List<IncludeMetric> accountIncludeMetrics = Lists.newArrayList();

    public CustomNamespaceMetricsProcessor(List<IncludeMetric> includeMetrics, List<Dimension> dimensions, String namespace) {
        this.includeMetrics = includeMetrics;
        this.dimensions = dimensions;
        this.namespace = namespace;
    }

    @Override
    public synchronized List<AWSMetric> getMetrics(CloudWatchClient awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        MultiDimensionPredicate predicate = new MultiDimensionPredicate(dimensions);
        return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestsCounter,
                namespace, includeMetrics, dimensionFilters, predicate);
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
        Map<String, String> dimensionToMetricPathNameDictionary = new HashMap<String, String>();
        for (Dimension dimension : dimensions) {
            dimensionToMetricPathNameDictionary.put(dimension.getName(), dimension.getDisplayName());
        }

        return MetricsProcessorHelper.createMetricStatsMapForUpload(namespaceMetricStats,
                dimensionToMetricPathNameDictionary, true);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }
}