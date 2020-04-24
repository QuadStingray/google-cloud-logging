package com.quadstingray.logging.logback.google.cloud

import java.io.{FileInputStream, FileNotFoundException}
import java.util.Collections

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{ILoggingEvent, IThrowableProxy, StackTraceElementProxy}
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.util.Loader
import com.google.auth.Credentials
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Logging.WriteOption
import com.google.cloud.logging._
import com.quadstingray.logging.logback.google.cloud.exception.LoginCredentialsNotSupported

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class LoggingAppender extends UnsynchronizedAppenderBase[ILoggingEvent] {

  private var logging: Logging = _
  private var loggingEnhancers: ArrayBuffer[LoggingEnhancer] = _
  private var loggingEventEnhancers: ArrayBuffer[LoggingEventEnhancer] = _
  private var defaultWriteOptions: Array[WriteOption] = _

  private var flushLevel: Level = _
  private var log: String = _
  private var resourceType: String = _
  private var payLoadType: String = "JsonPayload"
  private val enhancerClassNames: ArrayBuffer[String] = ArrayBuffer()
  private val loggingEventEnhancerClassNames: ArrayBuffer[String] = ArrayBuffer()

  private var projectId: String = _
  private var credentialType: String = "FILE"

  private var credentialFile: String = _

  private var projectName: String = ""

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
  def setFlushLevel(flushLevel: Level): Unit =
    this.flushLevel = flushLevel

  /**
    * Sets the log filename.
    *
    * @param log filename
    */
  def setLog(log: String): Unit =
    this.log = log

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
  def setResourceType(resourceType: String): Unit =
    this.resourceType = resourceType

  /**
    * Sets the projectId.
    *
    * @param projectId The Id of Google cloud Project
    */
  def setProjectId(projectId: String): Unit =
    this.projectId = projectId

  /**
    * Sets the projectName.
    *
    * @param name The name for sampel of log_stream
    */
  def setProjectName(name: String): Unit =
    this.projectName = name

  /**
    * Sets the PayLoadType.
    *
    * @param payLoadType Choose your PayloadType JsonPayload or StringPayload
    */
  def setPayLoadType(payLoadType: String): Unit =
    this.payLoadType = payLoadType

  /**
    * Sets the credentialType.
    *
    * @param credentialType The Login Credential Typ FILE, SERVICE_ACCOUNT
    */
  def setCredentialType(credentialType: String): Unit =
    this.credentialType = credentialType

  /**
    * Sets the credentialFile.
    *
    * @param credentialFile The credentialFile Path to your Auth Json File
    */
  def setFile(credentialFile: String): Unit =
    this.credentialFile = credentialFile

  /**
    * Sets the clientId.
    *
    * @param clientId The clientId of Google cloud Project
    */
  def setClientId(clientId: String): Unit =
    this.clientId = clientId

  /**
    * Sets the projectId.
    *
    * @param clientEmail The clientEmail of Google cloud Project
    */
  def setClientEmail(clientEmail: String): Unit =
    this.clientEmail = clientEmail

  /**
    * Sets the projectId.
    *
    * @param privateKeyId The privateKeyId of Google cloud Project
    */
  def setPrivateKeyId(privateKeyId: String): Unit =
    this.privateKeyId = privateKeyId

  /**
    * Sets the privateKey.
    *
    * @param privateKey The privateKey of Google cloud Project
    */
  def setPrivateKey(privateKey: String): Unit = {
    val internalPrivateKey = privateKey
      .split("(\n|\r\n|\\\\r\\\\n|\\\\n)")
      .map(string => {
        string.trim
      })
      .mkString("\n")
    this.privateKey = internalPrivateKey
  }

  /** Add extra labels using classes that implement {@link com.google.cloud.logging.LoggingEnhancer}. */
  def addEnhancer(enhancerClassName: String): Unit =
    this.enhancerClassNames.+=(enhancerClassName)

  def addLoggingEventEnhancer(enhancerClassName: String): Unit =
    this.loggingEventEnhancerClassNames.+=(enhancerClassName)

  private[logback] def getFlushLevel: Level =
    if (flushLevel != null) {
      flushLevel
    }
    else {
      Level.ERROR
    }

  private[logback] def getLogName =
    if (log != null) {
      log
    }
    else {
      "java.log"
    }

  private[logback] def getMonitoredResource(projectId: String): MonitoredResource = {
    var resource = MonitoredResourceUtil.getResource(projectId, resourceType)

    if (projectName.trim != "") {
      resource = resource.toBuilder.addLabel("name", projectName).build()
    }

    resource
  }

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
    if (logging != null)
      try logging.close()
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
              if (credentialFile != null) {
                val fileInputStream = new FileInputStream(credentialFile)
                LoggingOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(fileInputStream)).build()
              }
              else {
                throw new FileNotFoundException()
              }
            }
            else {
              var credentials: Credentials = null

              if ("SERVICE_ACCOUNT".equalsIgnoreCase(credentialType)) {
                val serviceAccountBuilder = ServiceAccountCredentials.fromPkcs8(clientId, clientEmail, privateKey, privateKeyId, scopes.asJava).toBuilder
                serviceAccountBuilder.setProjectId(projectId)
                credentials = serviceAccountBuilder.build()
              }
              else {
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

  private def logEntryFor(e: ILoggingEvent): LogEntry = {

    val level = e.getLevel

    val builder: LogEntry.Builder = {
      if ("StringPayload".equalsIgnoreCase(this.payLoadType)) {
        val stringPayloadBuilder = new StringBuilder(e.getFormattedMessage).append('\n')
        createPayloadString(e.getThrowableProxy, "", stringPayloadBuilder)

        LogEntry.newBuilder(Payload.StringPayload.of(stringPayloadBuilder.toString.trim))
      }
      else {
        val jsonPayloadMap = mutable.Map[String, Any]()
        jsonPayloadMap.put("formattedMessage", e.getFormattedMessage)
        jsonPayloadMap.put("message", e.getMessage)
        jsonPayloadMap.put("callerData", e.getCallerData.map(_.toString).toList.asJava)
        jsonPayloadMap.put("loggerName", e.getLoggerName)

        if (e.getThrowableProxy != null) {
          val proxyMap = getThrowableProxyMap(e.getThrowableProxy)
          proxyMap.foreach(element => {
            jsonPayloadMap.put(element._1, element._2)
          })
        }
        LogEntry.newBuilder(Payload.JsonPayload.of(jsonPayloadMap.asJava))
      }
    }

    builder.setTimestamp(e.getTimeStamp).setSeverity(LoggingAppender.severityFor(level))
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

  private def getThrowableProxyMap(proxy: IThrowableProxy): Map[String, Any] = {
    val proxyInfos = mutable.Map[String, Any]()
    if (proxy != null) {
      proxyInfos.put("exceptionClassName", proxy.getClassName)
      proxyInfos.put("stackTrace", proxy.getStackTraceElementProxyArray.toList.splitAt(proxy.getCommonFrames)._1.map(_.toString).toList.asJava)
      if (proxy.getCause != null)
        proxyInfos.put("causedBy", getThrowableProxyMap(proxy.getCause))
    }
    proxyInfos.toMap
  }

  private[logback] def createPayloadString(throwProxy: IThrowableProxy, prefix: String, payload: StringBuilder): Unit = {
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
    createPayloadString(throwProxy.getCause, "caused by: ", payload)
  }
}

object LoggingAppender {
  private val LEVEL_NAME_KEY = "levelName"
  private val LEVEL_VALUE_KEY = "levelValue"

  /**
    * Transforms Logback logging levels to Cloud severity.
    *
    * @param level Logback logging level
    * @return Cloud severity level
    */
  private def severityFor(level: Level) = level.toInt match {
    // TRACE
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
