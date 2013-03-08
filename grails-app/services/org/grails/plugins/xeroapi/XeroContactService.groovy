package org.grails.plugins.xeroapi

import grails.converters.*

import uk.co.desirableobjects.oauth.scribe.OauthService

import org.scribe.model.Token

class XeroContactService {
	final static String API_URL = "https://api.xero.com/api.xro/2.0/Contacts"
    
    boolean transactional = false
    
    OauthService oauthService
    Token oauthToken
    
    def setAuth(Token token) {
        oauthToken = token
    }

    def getAll(Date modifiedSince = null) throws XeroUnauthorizedException, XeroException {
        def contacts = []

        def headers = ['Content-Type':'application/json', 'Accept':'application/json']

        if(modifiedSince != null) {
            def outFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
            outFormat.timeZone = java.util.TimeZone.getTimeZone( 'GMT' )
            log.debug 'modified date in local time: ' + modifiedSince.format("yyyy-MM-dd'T'HH:mm:ss")
            log.debug 'modified date in GMT: ' + outFormat.format(modifiedSince)
            headers["If-Modified-Since"] = outFormat.format(modifiedSince)
        }

        def resp = oauthService.getXeroResource(oauthToken, API_URL, null, headers)
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = JSON.parse(resp.getBody())

            def contact

            json.Contacts.each {  // iterate over JSON 'status' object in the response:
                //println(it)
                contact = new XeroContact()
                contact.id = it.ContactID
                contact.number = it.ContactNumber
                contact.name = it.Name
                contact.email = it.EmailAddress
                contact.supplier = it.IsSupplier
                contact.customer = it.IsCustomer
                contact.status = it.Status

                contacts.add(contact)
            }
        } else {
            log.debug(resp.getBody())

            switch (resp.getCode()) {
                case 400: 
                    throw new XeroBadRequestException()
                    break
                case 401:
                    throw new XeroUnauthorizedException()
                    break
                case 503:
                    throw new XeroNotAvailableException()
                    break
                default:
                    throw new XeroException()
                
            }
        }
        
        return contacts
    }

    def getAllCustomers(Date modifiedSince = null) throws XeroUnauthorizedException, XeroException {
        def contacts = []

        String where = 'IsCustomer==true'

        String url = API_URL + "?where=" + where.encodeAsURL()
        log.debug 'url: ' + url

        def headers = ['Content-Type':'application/json', 'Accept':'application/json']

        if(modifiedSince != null) {
            def outFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
            outFormat.timeZone = java.util.TimeZone.getTimeZone( 'GMT' )
            log.debug 'modified date in local time: ' + modifiedSince.format("yyyy-MM-dd'T'HH:mm:ss")
            log.debug 'modified date in GMT: ' + outFormat.format(modifiedSince)
            headers["If-Modified-Since"] = outFormat.format(modifiedSince)
        }

        def resp = oauthService.getXeroResource(oauthToken, url, null, headers)
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = JSON.parse(resp.getBody())

            def contact

            json.Contacts.each {  // iterate over JSON 'status' object in the response:
                //println(it)
                contact = new XeroContact()
                contact.id = it.ContactID
                contact.number = it.ContactNumber
                contact.name = it.Name
                contact.email = it.EmailAddress
                contact.supplier = it.IsSupplier
                contact.customer = it.IsCustomer
                contact.status = it.Status

                contacts.add(contact)
            }
        } else {
            log.debug(resp.getBody())

            switch (resp.getCode()) {
                case 400: 
                    throw new XeroBadRequestException()
                    break
                case 401:
                    throw new XeroUnauthorizedException()
                    break
                case 503:
                    throw new XeroNotAvailableException()
                    break
                default:
                    throw new XeroException()
                
            }
        }
        
        return contacts
    }
}
