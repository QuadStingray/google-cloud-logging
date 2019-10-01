# Enable Google Cloud Logging 

## Add Cloud Logging Dependency to your Project
@@@ vars
@@dependency[sbt,Maven,Gradle] {
  group="com.quadstingray"
  artifact="google-cloud-logging"
  version="$project.version$"
}
@@@

## Add Logback Dependency

@@dependency[sbt,Maven,Gradle] {
  group="ch.qos.logback"
  artifact="logback-classic"
  version="1.2.3"
}


## Add your Logback.xml
Samples:

* [File Config](file-settings.md)
* [Service Account Config](service-account-settings.md)
* [Log Stream Target](logstream.md)


## Modifiy The Log Entry

### Add Setting To Logback.xml
```xml
        <loggingEventEnhancer>com.quadstingray.logging.logback.google.SpecialEnhancer</loggingEventEnhancer>
```

```scala
import com.quadstingray.logging.logback.google.cloud.LoggingEventEnhancer
import ch.qos.logback.classic.spi.ILoggingEvent

class SpecialEnhancer extends LoggingEventEnhancer {
  override def enhanceLogEntry(logEntry: LogEntry.Builder, e: ILoggingEvent): Unit = {
            logEntry.addLabel("duration", event.duration.toString)
            logEntry.addLabel("eventType", "RequestFinishedLogEvent")
  }


}
```