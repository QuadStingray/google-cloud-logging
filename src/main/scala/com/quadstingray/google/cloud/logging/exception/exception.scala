package com.quadstingray.google.cloud.logging.exception

import ch.qos.logback.core.boolex.EvaluationException

case class FileNotSet(msg: String = "no file path set in logback.xml") extends EvaluationException(msg)

case class LoginCredentialsNotSupported(msg: String = "Your Login Credentials are not supported. Use File Login or request at GitHub project.") extends EvaluationException(msg)