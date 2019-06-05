package com.quadstingray.google.cloud.logging.logback

import java.io.{FileInputStream, FileNotFoundException}
import java.util.Collections

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{ILoggingEvent, IThrowableProxy, StackTraceElementProxy}
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.util.Loader
import com.google.api.core.InternalApi
import com.google.auth.Credentials
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Logging.WriteOption
import com.google.cloud.logging._
import com.quadstingray.google.cloud.logging.exception.LoginCredentialsNotSupported

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer


class LoggingAppender extends UnsynchronizedAppenderBase[ILoggingEvent] {

  private var logging: Logging = _
  private var loggingEnhancers: ArrayBuffer[LoggingEnhancer] = _
  private var loggingEventEnhancers: ArrayBuffer[LoggingEventEnhancer] = _
  private var defaultWriteOptions: Array[WriteOption] = _

  private var flushLevel: Level = _
  private var log: String = _
  private var resourceType: String = _
  private val enhancerClassNames: ArrayBuffer[String] = ArrayBuffer()
  private val loggingEventEnhancerClassNames: ArrayBuffer[String] = ArrayBuffer()

  private var projectId: String = _
  private var credentialType: String = "FILE"

  private var file: String = _

  private var clientId: String = _
  private var clientEmail: String = _

  private var scopes: ArrayBuffer[String] = ArrayBuffer()
  private var privateKeyId: String = _
  private var privateKey: String = _


  /**
    * Batched logging requests get immediately flushed for logs at or above this level.
    *
    * <p>Defaults to Error if not set.
    *
    * @param flushLevel Logback log level
    */
  def setFlushLevel(flushLevel: Level): Unit = {
    this.flushLevel = flushLevel
  }

  /**
    * Sets the log filename.
    *
    * @param log filename
    */
  def setLog(log: String): Unit = {
    this.log = log
  }

  /**
    * Sets the name of the monitored resource (Optional).
    *
    * <p>Must be a <a href="https://cloud.google.com/logging/docs/api/v2/resource-list">supported</a>
    * resource type. gae_app, gce_instance and container are auto-detected.
    *
    * <p>Defaults to "global"
    *
    * @param resourceType name of the monitored resource
    */
  def setResourceType(resourceType: String): Unit = {
    this.resourceType = resourceType
  }

  /**
    * Sets the projectId.
    *
    * @param projectId The Id of Google cloud Project
    */
  def setProjectId(projectId: String): Unit = {
    this.projectId = projectId
  }

  /**
    * Sets the credentialType.
    *
    * @param credentialType The Login Credential Typ FILE, SERVICE_ACCOUNT
    */
  def setCredentialType(credentialType: String): Unit = {
    this.credentialType = credentialType
  }

  /**
    * Sets the file.
    *
    * @param file The file Path to your Auth Json File
    */
  def setFile(file: String): Unit = {
    this.file = file
  }

  /**
    * Sets the clientId.
    *
    * @param clientId The clientId of Google cloud Project
    */
  def setClientId(clientId: String): Unit = {
    this.clientId = clientId
  }

  /**
    * Sets the projectId.
    *
    * @param clientEmail The clientEmail of Google cloud Project
    */
  def setClientEmail(clientEmail: String): Unit = {
    this.clientEmail = clientEmail
  }

  /**
    * Sets the projectId.
    *
    * @param privateKeyId The privateKeyId of Google cloud Project
    */
  def setPrivateKeyId(privateKeyId: String): Unit = {
    this.privateKeyId = privateKeyId
  }

  /**
    * Sets the privateKey.
    *
    * @param privateKey The privateKey of Google cloud Project
    */
  def setPrivateKey(privateKey: String): Unit = {
    val internalPrivateKey = privateKey.split("(\n|\r\n|\\\\r\\\\n|\\\\n)").map(string => {
      string.trim
    }).mkString("\n")
    this.privateKey = internalPrivateKey
  }

  /** Add extra labels using classes that implement {@link LoggingEnhancer}. */
  def addEnhancer(enhancerClassName: String): Unit = {
    this.enhancerClassNames.+=(enhancerClassName)
  }

  def addLoggingEventEnhancer(enhancerClassName: String): Unit = {
    this.loggingEventEnhancerClassNames.+=(enhancerClassName)
  }

  private[logback] def getFlushLevel: Level = {
    if (flushLevel != null) {
      flushLevel
    }
    else {
      Level.ERROR
    }
  }

  private[logback] def getLogName = {
    if (log != null) {
      log
    }
    else {
      "java.log"
    }
  }

  private[logback] def getMonitoredResource(projectId: String): MonitoredResource = MonitoredResourceUtil.getResource(projectId, resourceType)

  private[logback] def getLoggingEnhancers: ArrayBuffer[LoggingEnhancer] = getEnhancers(enhancerClassNames)

  private[logback] def getLoggingEventEnhancers: ArrayBuffer[LoggingEventEnhancer] = getEnhancers(loggingEventEnhancerClassNames)

  private[logback] def getEnhancers[T](classNames: ArrayBuffer[String]): ArrayBuffer[T] = {
    val loggingEnhancers = ArrayBuffer[T]()
    if (classNames != null) {
      for (enhancerClassName <- classNames) {
        if (enhancerClassName != null) {
          val enhancer = getEnhancer(enhancerClassName)
          if (enhancer.isDefined)
            loggingEnhancers.+=(enhancer.get)
        }
      }
    }
    loggingEnhancers
  }

  private def getEnhancer[T](enhancerClassName: String): scala.Option[T] = {
    try {
      val clz = Loader.loadClass(enhancerClassName.trim).asInstanceOf[Class[T]]
      return Some(clz.newInstance)
    } catch {
      case ex: Exception =>

      // If we cannot create the enhancer we fallback to null
    }
    None
  }

  /** Initialize and configure the cloud logging service. */
  override def start(): Unit = {
    if (isStarted)
      return

    val resource = getMonitoredResource(getProjectId)
    defaultWriteOptions = Array[Logging.WriteOption](WriteOption.logName(getLogName), WriteOption.resource(resource))
    getLogging.setFlushSeverity(LoggingAppender.severityFor(getFlushLevel))

    loggingEnhancers = ArrayBuffer[LoggingEnhancer]()
    val resourceEnhancers = MonitoredResourceUtil.getResourceEnhancers
    loggingEnhancers.++=(resourceEnhancers.asScala)
    loggingEnhancers.++=(getLoggingEnhancers)

    loggingEventEnhancers = ArrayBuffer[LoggingEventEnhancer]()
    loggingEventEnhancers.++=(getLoggingEventEnhancers)
    super.start()
  }

  private[logback] def getProjectId: String = projectId

  override protected def append(e: ILoggingEvent): Unit = {
    val logEntry = logEntryFor(e)
    getLogging.write(Collections.singleton(logEntry), defaultWriteOptions: _*)
  }

  override def stop(): Unit = {
    if (logging != null) try
      logging.close()
    catch {
      case ex: Exception =>

      // ignore
    }
    logging = null
    super.stop()
  }


  private[logback] def getLogging: Logging = {
    if (logging == null) {
      synchronized {
        if (logging == null) {
          val builder: LoggingOptions = {
            if ("FILE".equalsIgnoreCase(credentialType)) {
              if (file != null) {
                val fileInputStream = new FileInputStream(file)
                LoggingOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(fileInputStream)).build()
              } else {
                throw new FileNotFoundException()
              }
            }
            else {
              var credentials: Credentials = null

              if ("SERVICE_ACCOUNT".equalsIgnoreCase(credentialType)) {
                val serviceAccountBuilder = ServiceAccountCredentials.fromPkcs8(clientId, clientEmail, privateKey, privateKeyId, scopes.asJava).toBuilder
                serviceAccountBuilder.setProjectId(projectId)
                credentials = serviceAccountBuilder.build()
              } else {
                throw LoginCredentialsNotSupported()
              }

              if (credentials != null)
                LoggingOptions.newBuilder().setCredentials(credentials).build()
              else {
                throw LoginCredentialsNotSupported()
              }
            }
          }
          logging = builder.getService
        }
      }
    }
    logging
  }

  private def logEntryFor(e: ILoggingEvent) = {
    val payload = new StringBuilder(e.getFormattedMessage).append('\n')
    LoggingAppender.writeStack(e.getThrowableProxy, "", payload)
    val level = e.getLevel
    val builder = LogEntry.newBuilder(Payload.StringPayload.of(payload.toString.trim)).setTimestamp(e.getTimeStamp).setSeverity(LoggingAppender.severityFor(level))
    builder.addLabel(LoggingAppender.LEVEL_NAME_KEY, level.toString).addLabel(LoggingAppender.LEVEL_VALUE_KEY, String.valueOf(level.toInt))
    if (loggingEnhancers != null) {
      for (enhancer <- loggingEnhancers) {
        enhancer.enhanceLogEntry(builder)
      }
    }
    if (loggingEventEnhancers != null) {
      for (enhancer <- loggingEventEnhancers) {
        enhancer.enhanceLogEntry(builder, e)
      }
    }
    builder.build
  }
}


/**
  * <a href="https://logback.qos.ch/">Logback</a> appender for StackDriver Cloud Logging.
  *
  * <p>Appender configuration in logback.xml:
  *
  * <ul>
  * <li>&lt;appender name="CLOUD" class="com.google.cloud.logging.logback.LoggingAppender"&gt;
  * <li>&lt;log&gt;application.log&lt;/log&gt; (Optional, defaults to "java.log" : Stackdriver log
  * name)
  * <li>&lt;level&gt;ERROR&lt;/level&gt; (Optional, defaults to "INFO" : logs at or above this
  * level)
  * <li>&lt;flushLevel&gt;WARNING&lt;/flushLevel&gt; (Optional, defaults to "ERROR")
  * <li>&lt;resourceType&gt;&lt;/resourceType&gt; (Optional, auto detects on App Engine Flex,
  * Standard, GCE and GKE, defaults to "global". See <a
  * href="https://cloud.google.com/logging/docs/api/v2/resource-list">supported resource
  * types</a>
  * <li>(Optional) add custom labels to log entries using {@link LoggingEnhancer} classes.
  * <li>&lt;enhancer&gt;com.example.enhancer1&lt;/enhancer&gt;
  * <li>&lt;enhancer&gt;com.example.enhancer2&lt;/enhancer&gt;
  * <li>&lt;/appender&gt;
  * </ul>
  */
object LoggingAppender {
  private val LEVEL_NAME_KEY = "levelName"
  private val LEVEL_VALUE_KEY = "levelValue"

  @InternalApi("Visible for testing") private[logback] def writeStack(throwProxy: IThrowableProxy, prefix: String, payload: StringBuilder): Unit = {
    if (throwProxy == null) return
    payload.append(prefix).append(throwProxy.getClassName).append(": ").append(throwProxy.getMessage).append('\n')
    var trace = throwProxy.getStackTraceElementProxyArray
    if (trace == null) trace = new Array[StackTraceElementProxy](0)
    val commonFrames = throwProxy.getCommonFrames
    val printFrames = trace.length - commonFrames
    var i = 0
    while ( {
      i < printFrames
    }) {
      payload.append("    ").append(trace(i)).append('\n')

      {
        i += 1;
        i - 1
      }
    }
    if (commonFrames != 0) payload.append("    ... ").append(commonFrames).append(" common frames elided\n")
    writeStack(throwProxy.getCause, "caused by: ", payload)
  }

  /**
    * Transforms Logback logging levels to Cloud severity.
    *
    * @param level Logback logging level
    * @return Cloud severity level
    */
  private def severityFor(level: Level) = level.toInt match { // TRACE
    case 5000 =>
      Severity.DEBUG
    // DEBUG
    case 10000 =>
      Severity.DEBUG
    // INFO
    case 20000 =>
      Severity.INFO
    // WARNING
    case 30000 =>
      Severity.WARNING
    // ERROR
    case 40000 =>
      Severity.ERROR
    case _ =>
      Severity.DEFAULT
  }
}