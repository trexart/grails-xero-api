package org.grails.plugins.xeroapi

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import oauth.signpost.exception.OAuthCommunicationException
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

class XeroOauthService {
    final static String REQUEST_TOKEN_URL = "https://api.xero.com/oauth/RequestToken"
    final static String AUTHORIZE_URL = "https://api.xero.com/oauth/Authorize"
    final static String ACCESS_TOKEN_URL = "https://api.xero.com/oauth/AccessToken"
    final static String ENDPOINT = "https://api.xero.com/api.xro/2.0/"

    boolean transactional = false
    
    CommonsHttpOAuthConsumer createConsumer() {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        log?.debug "- Key: ${key}, Secret: ${secret}"
        return new CommonsHttpOAuthConsumer(key, secret)
    }
    
    CommonsHttpOAuthProvider createProvider() {
        return new CommonsHttpOAuthProvider(
            REQUEST_TOKEN_URL,
            ACCESS_TOKEN_URL,
            AUTHORIZE_URL);
    }
    
    String createCallback() {
        final String serverURL = CH.config.grails.serverURL.toString()
        if (!serverURL.endsWith('/')) {
            serverURL += '/'
        }
        return serverURL + "xero/callback"
    }
    
    /**
    * Retrieves an unauthorized request token from the OAuth service.
    *
    * @param consumerName the consumer to fetch request token from.
    * @return A map containing the token key, secret and authorisation URL.
    */
    def fetchRequestToken() {
        log.debug "Fetching request token"

        try {
            // Get consumer and provider
            final CommonsHttpOAuthConsumer consumer = createConsumer()
            final CommonsHttpOAuthProvider provider = createProvider()
            String cb = createCallback();
            log.debug "Callback: ${cb}"

            // Retrieve request token
            final def authorisationURL = provider?.retrieveRequestToken(consumer, cb)
            final def isOAuth10a = provider.isOAuth10a()

            log.debug "Request token: ${consumer?.getToken()}"
            log.debug "Token secret: ${consumer?.getTokenSecret()}"
            log.debug "Authorisation URL: ${authorisationURL}\n"
            log.debug "Is OAuth 1.0a: ${isOAuth10a}"

            return [key: consumer?.getToken(),
                    secret: consumer?.getTokenSecret(),
                    authUrl: authorisationURL,
                    isOAuth10a: isOAuth10a
                    ]

        } catch (OAuthCommunicationException ex) { 
            final def errorMessage = "Unable to fetch request token"
            
            log.info("Response Body: " + ex.getResponseBody())

            log.error(errorMessage, ex)
        } catch (Exception ex) {
            final def errorMessage = "Unable to fetch request token"

            log.error(errorMessage, ex)
            throw new OauthServiceException(errorMessage, ex)
        }
    }
    
    def fetchAccessToken(final def requestToken) {
        log.debug "Going to exchange for access token"

        try {
            final CommonsHttpOAuthConsumer consumer = createConsumer()
            final CommonsHttpOAuthProvider provider = createProvider()
            
            consumer.setTokenWithSecret(requestToken.key, requestToken.secret)
            
            /*
            * Set to OAuth 1.0a if necessary (to make signpost add 'oath_verifier'
            * from callback to request)
            */
            if (requestToken.isOAuth10a) {
                provider.setOAuth10a(true)
            }
            
            provider.retrieveAccessToken(consumer, requestToken.verifier)
            return extractAccessTokenAndSecretFromConsumer(consumer, requestToken)

        } catch (Exception ex) {
            final def errorMessage = "Unable to fetch access token! (consumerName=$consumerName, " +
                "requestToken=$requestToken)"

            log.error(errorMessage, ex)
            throw new OauthServiceException(errorMessage, ex)
        }
    }
    
    /**
    * Extracts the access token and the secret from the consumer map.
    *
    * @param consumer
    * @param consumerName
    * @param requestToken
    * @return
    */
    private Map<String, String> extractAccessTokenAndSecretFromConsumer(CommonsHttpOAuthConsumer consumer, requestToken) {
        final def accessToken = consumer?.getToken()
        final def tokenSecret = consumer?.getTokenSecret()

        log.debug "Access token: $accessToken"
        log.debug "Token secret: $tokenSecret\n"

        if (!accessToken || !tokenSecret) {
            final def errorMessage = "Unable to fetch access token, access token is missing! (" +
                    "consumerName=$consumerName, requestToken=$requestToken, " +
                    "accessToken=$accessToken, tokenSecret=$tokenSecret)"

            log.error(errorMessage, ex)
            throw new OauthServiceException(errorMessage, ex)
        }

        return [key: accessToken, secret: tokenSecret]
    }
    
    
}
