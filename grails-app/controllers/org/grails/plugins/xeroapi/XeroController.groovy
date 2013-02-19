package org.grails.plugins.xeroapi


@Mixin(XeroOauthState)
class XeroController {
    
    def xeroOauthService

    def auth() { 
        log.debug "Prepare for open authorisation..."
        log.debug "Printing params: "
        log.debug params
        
        final def errorController = params?.error_controller ?: session?.cbparams?.error_controller
        final def errorAction = params?.error_action ?: session?.cbparams?.error_action
        final def errorId = params?.error_id ?: session?.cbparams?.error_id
        
        try {
            
            /*
            * Some services like FireEagle don't retain callback and params.
            * Must store the params in session.
            */
            params?.remove('controller')
            params?.remove('action')
            session.cbparams = params



            def token = xeroOauthService?.fetchRequestToken()
            log.debug "Got token: " + token
            session[OAUTH_SESSION_KEY] = token

            log.debug "Stored token to session: ${session[OAUTH_SESSION_KEY]}"
            def redir = token.authUrl

            log.debug "Going to redirect to auth url: $redir"
            redirect(url: redir)
            return

        } catch (OauthServiceException ose) {
            log.error "Unable to initialise authorisation: $ose"

            flash.oauthError = message(code: "oauth.requesttoken.missing",
                default: "Failed to retrieve the request token from the OAuth service provider. " +
                    "Please try to the authorization action again.")
            redirect(controller: errorController, action: errorAction, id: errorId)
        }
    }
    
    /**
    * This action will be called when the OAuth service returns from user authorization.
    * Do not (and no need) to call this explicitly.
    * Access token and secret are stored in session.
    * Get them by session.oauthToken.key and session.oauthToken.secret.
    */
    def callback() {
        log.debug "Callback received..."
        log.debug "Got callback params: $params"

        // List session parameters
        log.debug "Session parameters:"
        session.cbparams.each{ k, v ->
            log.debug "- $k: $v"
        }

        // Get required redirect controllers and actions
        final def returnController = params?.remove('return_controller') ?: session?.cbparams?.remove('return_controller')
        final def returnAction = params?.remove('return_action') ?: session?.cbparams?.remove('return_action')
        final def returnId = params?.remove('return_id') ?: session?.cbparams?.remove('return_id')
        final def errorController = params?.remove('error_controller') ?: session?.cbparams?.remove('error_controller')
        final def errorAction = params?.remove('error_action') ?: session?.cbparams?.remove('error_action')
        final def errorId = params?.remove('error_id') ?: session?.cbparams?.remove('error_id')

        params?.remove('controller')
        params?.remove('action')

        log.debug "Remaining parameters:"
        params.each { k, v ->
            log.debug "- $k: $v"
        }

        final def redirParams = params + session.cbparams

        log.debug "Re-direct parameters:"
        redirParams.each{ k, v ->
            log.debug "- $k: $v"
        }

        session.cbparams = null

        final def oauth_token = params?.oauth_token
        if (oauth_token && oauth_token != session[OAUTH_SESSION_KEY].key) {
            // Returned token is different from the last received request token
            flash.oauthError = message(code: "oauth.token.mismatch",
                default: "There has been an error in the OAuth request. Please try again.")
            redirect(controller: errorController, action: errorAction, id: errorId,
                params: redirParams)
            return
        }

        // OAuth 1.0a
        def oauth_verifier = params?.oauth_verifier

        try {

            Map oauthSession = session[OAUTH_SESSION_KEY] as Map ?: null

            def accessToken = xeroOauthService?.fetchAccessToken([key: oauthSession?.key, secret: oauthSession?.secret,
                    verifier: oauth_verifier, isOAuth10a: oauthSession?.isOAuth10a])
            session[OAUTH_SESSION_KEY] = accessToken

            log.debug("Got access token: ${accessToken?.key}")
            log.debug("Got token secret: ${accessToken?.secret}")
            log.debug("OAuth Verifier: ${oauth_verifier}")
            log.debug("Saved token to session: [key]${session[OAUTH_SESSION_KEY]?.key} " +
                "[secret]${session[OAUTH_SESSION_KEY]?.secret} " +
                "[verifier]${session[OAUTH_SESSION_KEY]?.verifier} " +
                "[isOAuth10a]${session[OAUTH_SESSION_KEY]?.isOAuth10a}")
            log.debug "Redirecting: [controller]$returnController, [action]$returnAction\n"

            redirect(controller: returnController, action: returnAction, id: returnId,
                params: redirParams)
            
        } catch (OauthServiceException ose) {
            log.error "Unable to fetch access token: $ose"

                flash.oauthError = message(code: "oauth.400badrequest",
                    default: "There has been an error in the OAuth request. Please try again.")
            redirect(controller: errorController, action: errorAction, params: redirParams)
        }
    }
}
