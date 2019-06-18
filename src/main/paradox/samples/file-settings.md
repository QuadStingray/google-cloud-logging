# Sample with File Configuration

## Logback.xml

```xml
    <appender name="CLOUD" class="com.quadstingray.google.cloud.logging.logback.LoggingAppender">
        <!-- Optional : filter logs at or above a level -->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
        <projectId>MyProjectName</projectId> <!-- Optional : default java.log -->
        <credentialType>FILE</credentialType> <!-- Options: FILE, SERVICE_ACCOUNT Optional : default FILE -->
        <file>path/to/my/login/json/MyProjectName-glog.json</file> <!-- Required, when used file credentialType -->
        <log>application.log</log> <!-- Optional : default java.log -->
        <resourceType>gae_app</resourceType> <!-- Optional : default: auto-detected, fallback: global -->
        <flushLevel>WARN</flushLevel> <!-- Optional : default ERROR -->
    </appender>

```