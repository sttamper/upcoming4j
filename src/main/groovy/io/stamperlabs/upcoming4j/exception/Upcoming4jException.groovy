package io.stamperlabs.upcoming4j.exception

import org.codehaus.groovy.GroovyException

class Upcoming4jException extends GroovyException {
    Upcoming4jException(String message) {
        super(message)
    }

    Upcoming4jException(String message, Throwable cause) {
        super(message, cause)
    }
}
