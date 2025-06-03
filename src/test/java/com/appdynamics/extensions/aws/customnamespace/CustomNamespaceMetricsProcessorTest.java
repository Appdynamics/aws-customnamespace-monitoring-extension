package com.appdynamics.extensions.aws.customnamespace;


import com.appdynamics.extensions.aws.config.Dimension;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.config.MetricsConfig;
import com.appdynamics.extensions.aws.customnamespace.conf.CustomNamespaceConfiguration;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.StatisticType;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DimensionFilter;
import software.amazon.awssdk.services.cloudwatch.model.Metric;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/*
 Copyright 2019. AppDynamics LLC and its affiliates.
 All Rights Reserved.
 This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 The copyright notice above does not evidence any actual or intended publication of such source code.
*/
@RunWith(PowerMockRunner.class)
@PrepareForTest(MetricsProcessorHelper.class)
public class CustomNamespaceMetricsProcessorTest {

    @Mock
    CloudWatchClient awsCloudWatch;
    @Mock
    CustomNamespaceConfiguration config;
    @Mock
    MetricsConfig metricsConfig;

    private LongAdder counter = new LongAdder();
    private List<Metric> listMetrics = Lists.newArrayList();
    private List<DimensionFilter> dimensionFilters = Lists.newArrayList();
    private List<Dimension> dimensions;
//    private AWSAccount awsAccount;

    @Before
    public void setUp(){
        List<String> accountMetrics = Lists.newArrayList();
        accountMetrics.add(".*");
        dimensions = Lists.newArrayList();
        Dimension dimension = new Dimension();
        dimension.setName("AutoScalingGroupName");
        dimension.setDisplayName("Autoscaling Group Name");
        dimension.setValues(Sets.newHashSet(Arrays.asList(".*")));
        dimensions.add(dimension);
//        awsAccount = new AWSAccount();
//        awsAccount.setDisplayAccountName("test.account");
//        awsAccount.setNamespace("AWS/EC2");
//        awsAccount.setAccountMetrics(Sets.newHashSet(Arrays.asList(".*")));
        when(config.getMetricsConfig()).thenReturn(metricsConfig);
        when(config.getDimensions()).thenReturn(dimensions);
        when(metricsConfig.getIncludeMetrics()).thenReturn(null);
//        when(config.getAwsAccounts()).thenReturn(Arrays.asList(awsAccount));

        for (Dimension dimension1 : dimensions) {
            DimensionFilter dimensionFilter = DimensionFilter.builder().name(dimension1.getName()).build();
            dimensionFilters.add(dimensionFilter);
        }
    }

    public void setUpProcessor() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("src/test/resources/conf/listMetrics.json"));
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(br).getAsJsonArray();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        for (JsonElement ele : array)
            listMetrics.add(mapper.readValue(ele.toString(), Metric.class));
    }

    @Test
    public void testGetMetricsReturnedAWSMetrics() throws Exception {
        setUpProcessor();
        IncludeMetric includeMetric = new IncludeMetric();
        includeMetric.setName("testMetric");
        includeMetric.setStatType("ave");
        PowerMockito.mockStatic(MetricsProcessorHelper.class);
        when(MetricsProcessorHelper.getMetrics(awsCloudWatch, counter, "AWS/EC2", dimensionFilters)).thenReturn(listMetrics);
        CustomNamespaceMetricsProcessor processor = new CustomNamespaceMetricsProcessor(Arrays.asList(includeMetric), dimensions, "AWS/EC2");
        List<AWSMetric> returnedMetrics = processor.getMetrics(awsCloudWatch, "test.account", counter);
        Assert.assertNotNull(returnedMetrics);
    }

    @Test
    public void getStatisticType() {
        IncludeMetric includeMetric = new IncludeMetric();
        includeMetric.setName("testMetric");
        includeMetric.setStatType("ave");
        AWSMetric awsMetric = new AWSMetric();
        awsMetric.setIncludeMetric(includeMetric);
        List<IncludeMetric> includeMetricList = new ArrayList<>();
        includeMetricList.add(includeMetric);
        PowerMockito.mockStatic(MetricsProcessorHelper.class);
        when(MetricsProcessorHelper.getStatisticType(awsMetric.getIncludeMetric(), includeMetricList)).thenReturn(StatisticType.AVE);
        CustomNamespaceMetricsProcessor processor = new CustomNamespaceMetricsProcessor(Arrays.asList(includeMetric), dimensions, "AWS/EC2");
        StatisticType statisticType = processor.getStatisticType(awsMetric);
        Assert.assertEquals(statisticType.getTypeName(), "Average");

    }

    @Test
    public void getNamespace() {
        IncludeMetric includeMetric = new IncludeMetric();
        includeMetric.setName("testMetric");
        includeMetric.setStatType("ave");
        CustomNamespaceMetricsProcessor processor = new CustomNamespaceMetricsProcessor(Arrays.asList(includeMetric), dimensions, "AWS/EC2");
        String result = processor.getNamespace();
        Assert.assertEquals(result, "AWS/EC2");
    }
}