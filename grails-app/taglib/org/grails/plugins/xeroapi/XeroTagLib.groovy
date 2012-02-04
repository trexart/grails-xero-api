package org.grails.plugins.xeroapi

@Mixin(XeroOauthState)
class XeroTagLib {
    static namespace = 'xero'
    
    def link = { attrs, body ->
        attrs.url = xero.url(attrs)
        out << g.link(attrs, body)
    }
    
    def url = { attrs ->
        attrs.url = [controller:'xero', action:'auth', params:[:]]

        final def returnTo = attrs.remove('returnTo')
        final def controller = returnTo?.controller ?: controllerName
        final def action = returnTo?.action ?: actionName
        final def id = returnTo?.id ?: ""
        attrs.url.params["return_controller"] = controller
        attrs.url.params["return_action"] = action
        attrs.url.params["return_id"] = id

        final def error = attrs.remove('error')
        final def errorController = error?.controller ?: controller
                final def errorAction = error?.action ?: action
                final def errorId = error?.id ?: ""
        attrs.url.params['error_controller'] = errorController
        attrs.url.params['error_action'] = errorAction
        attrs.url.params['error_id'] = errorId

        out << g.createLink(attrs)
    }
    
    def hasError = { attrs, body ->
        if (flash.oauthError) {
            out << body()
        }
    }


    def renderError = { attrs ->
        if (flash.oauthError) {
            out << message(code: flash.oauthError)
        }
    }

    def connected = { attrs, body ->

        if (session.xeroOauthToken) {
            out << body()
        }

     }

     def disconnected = { attrs, body ->

        if (!session.xeroOauthToken) {

            out << body()
        }

     }
}
