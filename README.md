#AWS Custom Namespace Monitoring Extension

##Use Case
Captures Custom Namespace statistics from Amazon CloudWatch and displays them in the AppDynamics Metric Browser.

**Note : By default, the Machine agent can only send a fixed number of metrics to the controller. This extension potentially reports thousands of metrics, so to change this limit, please follow the instructions mentioned [here](https://docs.appdynamics.com/display/PRO40/Metrics+Limits).** 

##Installation

1. Run 'mvn clean install' from aws-customnamespace-monitoring-extension
2. Copy and unzip AWSCustomNamespaceMonitor-\<version\>.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yaml file in AWSCustomNamespaceMonitor/conf and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

##Configuration

###config.yaml

**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

| Section | Fields | Description | Example |
| ----- | ----- | ----- | ----- |
| **accounts** | | Fields under this section can be repeated for multiple accounts config |  |
| | awsAccessKey | AWS Access Key |  |
| | awsSecretKey | AWS Secret Key |  |
| | displayAccountName | Display name used in metric path | "MyAWSCustomNamespace" |
| | regions | Regions where CustomNamespace is registered | **Allowed values:**<br/>"ap-southeast-1",<br/>"ap-southeast-2",<br/>"ap-northeast-1",<br/>"eu-central-1",<br/>"eu-west-1",<br/>"us-east-1",<br/>"us-west-1",<br/>"us-west-2",<br/>"sa-east-1" |
| | namespaces | The custom namespaces registered for this account that you want to monitor | "MyCustomNamespace1" |
| **credentialsDecryptionConfig** | ----- | ----- | ----- |
| | enableDecryption | If set to "true", then all aws credentials provided (access key and secret key) will be decrypted - see AWS Credentials Encryption section |  |
| | decryptionKey | The key used when encypting the credentials |  |
| **proxyConfig** | ----- | ----- | ----- |
| | host | The proxy host (must also specify port) |  |
| | port | The proxy port (must also specify host) |  |
| | username | The proxy username (optional)  |  |
| | password | The proxy password (optional)  |  |
| **metricsConfig** | ----- | ----- | ----- |
| metricTypes | | Fields under this section can be repeated for multiple metric types override |  |
| | metricName | The metric name | "CPUUtilization" |
| | statType | The statistic type | **Allowed values:**<br/>"ave"<br/>"max"<br/>"min"<br/>"sum"<br/>"samplecount" |
| | ----- | ----- | ----- |
| | excludeMetrics | Metrics to exclude - supports regex | "CPUUtilization",<br/>"Swap.*" |
| metricsTimeRange |  |  |  |
| | startTimeInMinsBeforeNow | The no of mins to deduct from current time for start time of query | 5 |
| | endTimeInMinsBeforeNow | The no of mins to deduct from current time for end time of query.<br>Note, this must be less than startTimeInMinsBeforeNow | 0 |
| | ----- | ----- | ----- |
| | maxErrorRetrySize | The max number of retry attempts for failed retryable requests | 1 |
| **concurrencyConfig** |  |  |  |
| | noOfNamespaceThreads | The no of threads to process multiple namespaces concurrently | 3 |
| | noOfAccountThreads | The no of threads to process multiple accounts concurrently | 3 |
| | noOfRegionThreadsPerAccount | The no of threads to process multiple regions per account concurrently | 3 |
| | noOfMetricThreadsPerRegion | The no of threads to process multiple metrics per region concurrently | 3 |
| | ----- | ----- | ----- |
| | metricPrefix | The path prefix for viewing metrics in the metric browser. | "Custom Metrics\|Amazon Custom Namespace\|" |


**Below is an example config for monitoring multiple accounts, regions and namespaces:**

~~~
accounts:
  - awsAccessKey: "XXXXXXXX1"
    awsSecretKey: "XXXXXXXXXX1"
    displayAccountName: "TestAccount_1"
    regions: ["us-east-1","us-west-1","us-west-2"]
    namespaces: ["Custom1"]
    
  - awsAccessKey: "XXXXXXXX2"
    awsSecretKey: "XXXXXXXXXX2"
    displayAccountName: "TestAccount_2"
    regions: ["eu-central-1","eu-west-1"]
    namespaces: ["Custom2", "Custom3"]
    
credentialsDecryptionConfig:
    enableDecryption: "false"
    decryptionKey:
    
proxyConfig:
    host: 
    port:
    username:
    password:    

metricsConfig:
    metricTypes:
      - metricName: "GroupInServiceInstances"
        statType: "max"
        
      - metricName: "GroupMinSize"
        statType: "sum"        

    excludeMetrics: ["GroupMaxSize", "GroupTotal.*"]

    metricsTimeRange:
      startTimeInMinsBeforeNow: 5
      endTimeInMinsBeforeNow: 0

    maxErrorRetrySize: 0

concurrencyConfig:
  noOfNamespaceThreads: 3
  noOfAccountThreads: 3
  noOfRegionThreadsPerAccount: 3
  noOfMetricThreadsPerRegion: 3

metricPrefix: "Custom Metrics|Amazon Custom Namespace|"
~~~

###AWS Credentials Encryption
To set an encrypted awsAccessKey and awsSecretKey in config.yaml, follow the steps below:

1. Download the util jar to encrypt the AWS Credentials from [here](https://github.com/Appdynamics/maven-repo/blob/master/releases/com/appdynamics/appd-exts-commons/1.1.2/appd-exts-commons-1.1.2.jar).
2. Run command:

   	~~~   
   	java -cp appd-exts-commons-1.1.2.jar com.appdynamics.extensions.crypto.Encryptor EncryptionKey CredentialToEncrypt
   	
   	For example: 
   	java -cp "appd-exts-commons-1.1.2.jar" com.appdynamics.extensions.crypto.Encryptor test myAwsAccessKey
   	
   	java -cp "appd-exts-commons-1.1.2.jar" com.appdynamics.extensions.crypto.Encryptor test myAwsSecretKey
   	~~~
   	
3. Set the decryptionKey field in config.yaml with the encryption key used, as well as the resulting encrypted awsAccessKey and awsSecretKey in their respective fields.

###AWS Instance Profiles
When running on an EC2 instance, you can use the IAM instance profile on the instance instead of storing credentials in
your configuration file.

To use instance profiles instead of keys in the `config.yaml`, follow all the installation steps but leave the
`awsAccessKey` and `awsSecretKey` fields blank in the `config.yaml` or omit them. The extension will then get temporary
credentials from using the instance profile each time metrics are sent to your AppDynamcis controller.

##Metrics
Typical metric path: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Amazon Custom Namespace|\<Namespace\>|\<Account Name\>|\<Region\>** followed by the metrics defined by your custom namespace.

##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/aws-customnamespace-monitoring-extension).

##Community

Find out more in the [AppSphere](https://www.appdynamics.com/community/exchange/extension/aws-customnamespace-monitoring-extension) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).
