/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace;

import com.appdynamics.extensions.aws.SingleNamespaceCloudwatchMonitor;
import com.appdynamics.extensions.aws.collectors.NamespaceMetricStatisticsCollector;
import com.appdynamics.extensions.aws.customnamespace.conf.CustomNamespaceConfiguration;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import org.apache.log4j.Logger;

import static com.appdynamics.extensions.aws.Constants.METRIC_PATH_SEPARATOR;

/**
 * @author Florencio Sarmiento
 */
public class CustomNamespaceMonitor extends SingleNamespaceCloudwatchMonitor<CustomNamespaceConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(CustomNamespaceMonitor.class);

    private static final String DEFAULT_METRIC_PREFIX = String.format("%s%s%s%s",
            "Custom Metrics", METRIC_PATH_SEPARATOR, "Amazon Custom Namespace", METRIC_PATH_SEPARATOR);

    public CustomNamespaceMonitor() {
        super(CustomNamespaceConfiguration.class);
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return "AWSCustomNamespaceMonitor";
    }

    @Override
    protected int getTaskCount() {
        return 3;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected NamespaceMetricStatisticsCollector getNamespaceMetricsCollector(CustomNamespaceConfiguration config) {
        MetricsProcessor metricsProcessor =
                new CustomNamespaceMetricsProcessor(config.getMetricsConfig().getIncludeMetrics(),
                        config.getDimensions(),
                        config.getNamespace());

        return new NamespaceMetricStatisticsCollector
                .Builder(config.getAccounts(),
                config.getConcurrencyConfig(),
                config.getMetricsConfig(),
                metricsProcessor, config.getMetricPrefix())
                .withCredentialsDecryptionConfig(config.getCredentialsDecryptionConfig())
                .withProxyConfig(config.getProxyConfig())
                .build();
    }
}
