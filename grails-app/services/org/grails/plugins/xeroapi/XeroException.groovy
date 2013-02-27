/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

/**
 *
 * @author steph
 */
class XeroException extends Exception {
	// Declare exception properties
    final String message

    public XeroException(){}

    /**
    * Constructor.
    *
    * @param message the exception message.
    */
    public XeroException(String message) {
        super(message)
        this.message = message
    }

    /**
    * Constructor.
    *
    * @param message the exception message.
    * @param exception the wrapped exception.
    */
    public XeroException(String message, Exception exception) {
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

