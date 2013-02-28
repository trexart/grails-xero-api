package org.grails.plugins.xeroapi

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import grails.converters.*

import uk.co.desirableobjects.oauth.scribe.OauthService

import org.scribe.model.Token

class XeroContactService {
	final static String API_URL = "https://api.xero.com/api.xro/2.0/Contacts"
    final static String REQUEST = "Contacts"
    
    boolean transactional = false
    
    OauthService oauthService
    Token oauthToken
    
    def setAuth(Token token) {
        oauthToken = token
    }

    def getAll() throws XeroUnauthorizedException, XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def contacts = []

        def resp = oauthService.getXeroResource(oauthToken, API_URL, null, ['Content-Type':'application/json', 'Accept':'application/json'])
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
                /*case 404:
                    // throw new XeroNotFoundException
                    //break
                case 501:
                    // throw new XeroNotImplementedException
                    
                case 503:
                    // throw new XeroNotAvailableException*/
                default:
                    throw new XeroException()
                
            }
        }
        
        return contacts
    }

    def getAllCustomers() throws XeroUnauthorizedException, XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def contacts = []

        String where = 'IsCustomer==true'

        String url = API_URL + "?where=" + where.encodeAsURL()
        log.debug 'url: ' + url

        def resp = oauthService.getXeroResource(oauthToken, url, null, ['Content-Type':'application/json', 'Accept':'application/json'])
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
                /*case 404:
                    // throw new XeroNotFoundException
                    //break
                case 501:
                    // throw new XeroNotImplementedException
                    
                case 503:
                    // throw new XeroNotAvailableException*/
                default:
                    throw new XeroException()
                
            }
        }
        
        return contacts
    }
}
