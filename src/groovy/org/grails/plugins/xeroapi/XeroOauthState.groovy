/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

/**
 *
 * @author steph
 */
class XeroOauthState {
	static final String OAUTH_SESSION_KEY = 'xeroOauthToken'

    Map getOauthCredentials() {
        return session ? session[OAUTH_SESSION_KEY] : null
    }

    boolean isOauthSession() {
        return oauthCredentials ? true : false
    }
}

