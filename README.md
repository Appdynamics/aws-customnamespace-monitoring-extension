# AWS Custom Namespace Monitoring Extension

## Use Case
Captures Custom Namespace statistics from Amazon CloudWatch and displays them in the AppDynamics Metric Browser.

Before you configure this extension, you can take a look [here](https://developer.cisco.com/codeexchange/explore/#products=AppDynamics%20Extension) to see if any specific extension is available for your AWS services which you are planning to monitor. Using specific extension provides more configurability and better monitoring.

## Prerequisites
1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.


2. Please give the following permissions to the account being used to with the extension. 
```
        cloudwatch:ListMetrics
        cloudwatch:GetMetricStatistics
```

## Installation

1. Run `mvn clean install` from aws-customnamespace-monitoring-extension
2. Copy and unzip AWSCustomNamespaceMonitor-\<version\>.zip from 'target' directory into \<MachineAgent_Home\>/monitors/ directory.
3. Edit the config.yml file located at MachineAgent_Home/monitors/AWSCustomNamespaceMonitor and provide the required configuration (see Configuration section)
4. The metricPrefix of the extension has to be configured as specified [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695#Configuring%20an%20Extension). Please make sure that the right metricPrefix is chosen based on your machine agent deployment, otherwise this could lead to metrics not being visible in the controller.
5. Restart the Machine Agent.

Please place the extension in the **"monitors"** directory of your **Machine Agent** installation directory. Do not place the extension in the **"extensions"** directory of your **Machine Agent** installation directory.

## Configuration
In order to use the extension, you need to update the config.yml file that is present in the extension folder. The following is an explanation of the configurable fields that are present in the config.yml file.

1. If SIM is enabled, then use the following metricPrefix `metricPrefix: "Custom Metrics|AWS DynamoDB"` else configure the `COMPONENT_ID` under which the metrics need to be reported. This can be done by changing the value of <COMPONENT_ID> in `metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|AWS DynamoDB|"`.
   
   For example,
   
   `metricPrefix: "Server|Component:100|Custom Metrics|Amazon Custom Namespace|"`
   
More details around metric prefix can be found [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695).

2. Provide accessKey(required) and secretKey(required) of AWS account(s), also provide displayAccountName(any name that represents your account) and regions(required). If you are running this extension inside an EC2 instance which has IAM profile configured then awsAccessKey and awsSecretKey can be kept empty, extension will use IAM profile to authenticate.
  ``` 
   accounts:
         - awsAccessKey: "XXXXXXXX1"
           awsSecretKey: "XXXXXXXXXX1"
           displayAccountName: "TestAccount_1"
           regions: ["us-east-1","us-west-1","us-west-2"]
       
         - awsAccessKey: "XXXXXXXX2"
           awsSecretKey: "XXXXXXXXXX2"
           displayAccountName: "TestAccount_2"
           regions: ["eu-central-1","eu-west-1"]
   ```
   
3. Please specify the AWS Namespace that you would like to monitor. Please note that only 1 AWS Namespace can be monitored using this extension. Inorder to monitor multiple Namespaces, please have multiple copies of this extension. However, multiple accounts can be configured under the `accounts` to monitor the namespace.
   Example is `namespace: "AWS/DynamoDB"`

4. If you want to encrypt the "awsAccessKey" and "awsSecretKey" then follow the "Credentials Encryption" section and provide the encrypted values in "awsAccessKey" and "awsSecretKey". Configure "enableDecryption" of "credentialsDecryptionConfig" to true and provide the encryption key in "encryptionKey" For example,
  ``` 
           #Encryption key for Encrypted password.
           credentialsDecryptionConfig:
               enableDecryption: "true"
               encryptionKey: "XXXXXXXX"
   ```
5. Configure proxy details if there is a proxy between MachineAgent and AWS.
   ``` 
        proxyConfig:
            host:
            port:
            username:
            password:
    ```
6. To report metrics from specific dimension values, configure the `dimesions` section. The dimensions varies with AWS Namespace. Please refer to AWS [doc](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_concepts.html#Dimension) for details on dimensions.
   Example below:
   ```
   dimensions:
     - name: "TableName"
       displayName: "Table Name"
       values: ["ABC", "DEF"]
     - name: "AvailabilityZone"
       displayName: "Availability Zone"
       values: [".*"]
    ```
   name is the dimension name and values are comma separated values of the dimension. displayName is the name with which it appears on the AppDynamics Metric Browser.
   If these fields are left empty, none of your instances will be monitored. In order to monitor everything under a dimension, you can simply use ".*" to pull everything from your AWS Environment.
   
7. Configure the metrics section.
   
For configuring the metrics, the following properties can be used:
   
|     Property      |   Default value |         Possible values         |                                              Description                                                                                                |
| :---------------- | :-------------- | :------------------------------ | :------------------------------------------------------------------------------------------------------------- |
| alias             | metric name     | Any string                      | The substitute name to be used in the metric browser instead of metric name.                                   |
| statType          | "ave"           | "AVERAGE", "SUM", "MIN", "MAX"  | AWS configured values as returned by API                                                                       |
| aggregationType   | "AVERAGE"       | "AVERAGE", "SUM", "OBSERVATION" | [Aggregation qualifier](https://docs.appdynamics.com/display/latest/Build+a+Monitoring+Extension+Using+Java)    |
| timeRollUpType    | "AVERAGE"       | "AVERAGE", "SUM", "CURRENT"     | [Time roll-up qualifier](https://docs.appdynamics.com/display/latest/Build+a+Monitoring+Extension+Using+Java)   |
| clusterRollUpType | "INDIVIDUAL"    | "INDIVIDUAL", "COLLECTIVE"      | [Cluster roll-up qualifier](https://docs.appdynamics.com/display/latest/Build+a+Monitoring+Extension+Using+Java)|
| multiplier        | 1               | Any number                      | Value with which the metric needs to be multiplied.|
| convert           | null            | Any key value map               | Set of key value pairs that indicates the value to which the metrics need to be transformed. eg: UP:0, DOWN:1  |
| delta             | false           | true, false                     | If enabled, gives the delta values of metrics instead of actual values.|
   
For example,
```
       - name: "ConsumedReadCapacityUnits"
         alias: "Consumed Read Capacity Units"
         statType: "ave"
         delta: false
         multiplier: 1
         aggregationType: "AVERAGE"
         timeRollUpType: "AVERAGE"
         clusterRollUpType: "INDIVIDUAL"
```
       
**All these metric properties are optional, and the default value shown in the table is applied to the metric(if a property has not been specified) by default.**
       
8. For several services AWS CloudWatch does not instantly update the metrics but it goes back in time to update that information. 
   This delay sometimes can take upto 5 minutes. The extension runs every minute(Detailed) or every 5 minutes (Basic) and gets the latest value at that time.
   There may be a case where the extension may miss the value before CloudWatch updates it. In order to make sure we don't do that, 
   the extension has the ability to look for metrics during a certain interval, where we already have set it to default at 5 minutes but you can 
   change it as per your requirements. 
```
       metricsTimeRange:
         startTimeInMinsBeforeNow: 10
         endTimeInMinsBeforeNow: 5
```

9. Configure the default period for metrics collection and individual metric periods.
   
   The `defaultPeriod` field sets the default time period (in seconds) for all metrics retrieved from CloudWatch. This value must be a multiple of 60 seconds. Common values include 60 (1 minute), 300 (5 minutes), or 3600 (1 hour). Individual metrics can override this default by specifying their own `period` field within the metric configuration.
   
   ```
   # Default period for all metrics (in seconds). Must be a multiple of 60.
   # Valid values: 60, 300, 3600, etc.
   defaultPeriod: 300
   ```
   
   For individual metrics, you can override the default period by adding a `period` field to the specific metric configuration:
   
   ```
   - name: "ConsumedReadCapacityUnits"
     alias: "Consumed Read Capacity Units"
     statType: "ave"
     period: 60  # Override default period for this specific metric
     delta: false
     multiplier: 1
     aggregationType: "AVERAGE"
     timeRollUpType: "AVERAGE"
     clusterRollUpType: "INDIVIDUAL"
   ```
   
10. This field is set as per the defaults suggested by AWS. You can change this if your limit is different.
 ```    
       getMetricStatisticsRateLimit: 400
```
11. The maximum number of retry attempts for failed requests that can be retried. 
```
        maxErrorRetrySize: 3
```
12. CloudWatch can be used in two formats, Basic and Detailed. You can specify how you would like to run the extension by specifying the chosen format here.
    By default, the extension is set to Basic, which makes the extension run every 5 minutes.
    Refer https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-cloudwatch-new.html for more information.
    
```
        #Allowed values are Basic and Detailed. 
        # Basic will fire CloudWatch API calls every 5 minutes
        # Detailed will fire CloudWatch API calls every 1 minutes
        cloudWatchMonitoring: "Basic"
```

Please avoid using tab (\t) when editing yaml files. Please copy all the contents of the config.yml file and go to [Yaml Validator](https://jsonformatter.org/yaml-validator) . On reaching the website, paste the contents and press the “Validate YAML” button.                                                       
If you get a valid output, that means your formatting is correct and you may move on to the next step.

## Metrics
Typical metric path: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Amazon Custom Namespace|\<NameSpace\>|\<Account Name\>|Region|Dimension Name|Dimension Value|** followed by the AWS Namespace configured metrics 

## Credentials Encryption
Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting
Please follow the steps listed in this [troubleshooting document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension.
   
## Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/aws-customnamespace-monitoring-extension).


## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.0.7       |
|Last Update               |05/11/2025|
|Changes list|[ChangeLog]((https://github.com/Appdynamics/aws-customnamespace-monitoring-extension/blob/master/CHANGELOG.md))|

**Note**: While extensions are maintained and supported by customers under the open-source licensing model, they interact with agents and Controllers that are subject to [AppDynamics’ maintenance and support policy](https://docs.appdynamics.com/latest/en/product-and-release-announcements/maintenance-support-for-software-versions). Some extensions have been tested with AppDynamics 4.5.13+ artifacts, but you are strongly recommended against using versions that are no longer supported.
