# Grails Xero-Api Plugin

This plugin is for integration with the Xero Api. It is in very early stages, so lots of functionality doesn't exist. If you wish to help with it, please let me know.

## Dependencies

Oauth Plugin (Scribe)

## Getting started

To use this plugin, it requires some configuration that follows the standard configuration for the Oauth Plugin.

	oauth {
        providers {
            xero {
            	api = org.grails.plugins.xeroapi.XeroPublicApi    // This is required, it is how the oauth plugin knows the api
                key = 'YOUR KEY'
                secret = 'YOUR SECRET'
                successUri = "${grails.serverURL}/xeroSave"  // The url that is called after successfully going through the callback. Good to save token info to db, or whatever here.
                failureUri = "/unauthorized"							// Any url you wish to go to on Oauth failure
                callback = "${grails.serverURL}/oauth/xero/callback"   // This is required as it saves the oauth token to the session.
            }
        }
    }

Now you can use this tag somewhere in your app, to get started with the Oauth process:

	<oauth:connect provider="xero">Connect to Xero</oauth:connect>

## Querying Xero

There are 3 services available at this time.

### Organisation

To query an organisation, first do the following to get the service injected:

	def xeroOrganisationService

Then to get an organisation:

	String sessionKey = oauthService.findSessionKeyForAccessToken('xero')
    Token oauthToken = session[sessionKey]
	xeroOrganisationService.setAuth(oauthToken)
    def xeroOrg = xeroOrganisationService.get()

### Contact

To query contacts, inject the correct service:

	def xeroContactService

Available methods:

	get(ID OR NUMBER)
	findAll()
	findAllBy dynamic method

The findAllBy dynamic method only has the capability at the moment of doing '==' queries with a single property. In the future will be adding muliple property searches and '<=', '>=', etc.

### Invoice

... documention to come