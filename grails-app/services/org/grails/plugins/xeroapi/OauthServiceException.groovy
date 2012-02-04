package org.grails.plugins.xeroapi

/**
 *
 * @author steph
 */
class OauthServiceException extends Exception {
	// Declare exception properties
    final String message

    /**
    * Constructor.
    *
    * @param message the exception message.
    */
    public OauthServiceException(String message) {
        super(message)
        this.message = message
    }

    /**
    * Constructor.
    *
    * @param message the exception message.
    * @param exception the wrapped exception.
    */
    public OauthServiceException(String message, Exception exception) {
        super(message, exception)
        this.message = message
    }

    /**
    * Return the exception error message.
    *
    * @return the exception error message.
    */
    public String getMessage() {
       return(this.message)
    }
}

