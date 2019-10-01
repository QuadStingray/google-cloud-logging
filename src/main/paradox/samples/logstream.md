# Sample Logging to Log Stream

## Logback.xml

```xml
    <appender name="CLOUD" class="com.quadstingray.logging.logback.google.cloud.LoggingAppender">
        <!-- Optional : filter logs at or above a level -->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
        <projectId>MyProjectId</projectId> 
        <credentialType>FILE</credentialType> 
        <file>path/to/my/login/json/MyProjectName-glog.json</file> 
        <log>application.log</log> 
        <flushLevel>WARN</flushLevel>
        
        <!--  Special Setting for Push Logging to Google Cloud -->
        <resourceType>logging_log</resourceType> 
        <projectName>my-project</projectName> 
    </appender>

```