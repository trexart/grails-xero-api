package org.grails.plugins.xeroapi

import grails.converters.*
import grails.util.GrailsNameUtils

import uk.co.desirableobjects.oauth.scribe.OauthService

import org.scribe.model.Token

class XeroContactService {
	final static String API_URL = "https://api.xero.com/api.xro/2.0/Contacts"

    final static def servicePropertyMap = [
        'customer'  : 'IsCustomer',
        'supplier'  : 'IsSupplier',
        'email'     : 'EmailAddress',
        'status'    : 'ContactStatus'
    ]
    
    boolean transactional = false
    
    OauthService oauthService
    Token oauthToken

    XeroContactService() {
        // not sure if I have to do this
        //def mc = new ExpandoMetaClass(XeroContactService, false, true)
        //mc.initialize()
        //this.metaClass = mc
    }
    
    def setAuth(Token token) {
        oauthToken = token
    }

    def get(String identifier) throws XeroUnauthorizedException, XeroException {
        def contact
        def headers = ['Content-Type':'application/json', 'Accept':'application/json']

        def url = API_URL + "/${identifier}"

        def resp = oauthService.getXeroResource(oauthToken, url, null, headers)
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            log.debug(resp.getBody())
            def json = JSON.parse(resp.getBody())
            contact = fillContact(json.Contacts[0])

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

        return contact
    }

    private def fillContact(def it) {
        def contact = new XeroContact()
        contact.id = it.ContactID
        contact.number = it.ContactNumber
        contact.name = it.Name
        contact.firstName = it.FirstName
        contact.lastName = it.LastName
        contact.email = it.EmailAddress
        contact.supplier = it.IsSupplier
        contact.customer = it.IsCustomer
        contact.status = it.ContactStatus
        contact.skypeUserName = it.SkypeUserName
        contact.bankAccountDetails = it.BankAccountDetails
        contact.taxNumber = it.TaxNumber
        contact.accountsReceivableTaxType = it.AccountsReceivableTaxType
        contact.accountsPayableTaxType = it.AccountsPayableTaxType
        contact.defaultCurrency = it.DefaultCurrency
        //contact.updatedDateUTC = it.UpdatedDateUTC

        // TODO add addresses
        // TODO add phone numbers
        // TODO add contact groups

        return contact
    }

    def findAll(Date modifiedSince = null) throws XeroUnauthorizedException, XeroException {
        def contacts = []

        def headers = ['Content-Type':'application/json', 'Accept':'application/json']

        if(modifiedSince != null) {
            def outFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
            outFormat.timeZone = java.util.TimeZone.getTimeZone( 'GMT' )
            log.debug 'modified date in local time: ' + modifiedSince.format("yyyy-MM-dd'T'HH:mm:ss")
            log.debug 'modified date in GMT: ' + outFormat.format(modifiedSince)
            headers["If-Modified-Since"] = outFormat.format(modifiedSince)
        }

        return getListResult(API_URL, headers)
    }

    private String getServiceProperty(String propertyName) {
        if(servicePropertyMap.containsKey(propertyName)) {
            servicePropertyMap[propertyName]
        } else {
            propertyName
        }
    }

    def methodMissing(String name, args) throws XeroUnauthorizedException, XeroException, MissingMethodException {
        log.debug("methodMissing name: ${name}")
        log.debug("methodMissing args: ${args}")

        def xc = new XeroContact()
        String url = API_URL
        
        if(name.startsWith("findAllBy")) {

            String instructions = name.replaceAll("findAllBy", "")

            Date modifiedSince = null

            String propertyName = GrailsNameUtils.getPropertyName(instructions)

            if(xc.metaClass.hasProperty(xc, propertyName)) {
                String where = getServiceProperty(propertyName) + '=='
                if(args[0] instanceof String) {
                    where += "\"${args[0]}\""
                } else {
                    where += args[0]
                }
                 
                url += "?where=" + where.encodeAsURL()

                if(args.size() > 1 && args[1] instanceof Date) {
                    modifiedSince = args[1]
                }
            } else {
                throw new MissingMethodException(name, this.class, args)
            }

            log.debug("url: ${url}")

            def headers = ['Content-Type':'application/json', 'Accept':'application/json']

            if(modifiedSince != null) {
                def outFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
                outFormat.timeZone = java.util.TimeZone.getTimeZone( 'GMT' )
                log.debug 'modified date in local time: ' + modifiedSince.format("yyyy-MM-dd'T'HH:mm:ss")
                log.debug 'modified date in GMT: ' + outFormat.format(modifiedSince)
                headers["If-Modified-Since"] = outFormat.format(modifiedSince)
            }

            // this caches the method, not quite sure how to get it to work though
            // don't think it is exactly correct, needs adjustments
            // doesn't seem to be working
            //this.metaClass."$name" = {-> getListResult(url, headers) }

            return getListResult(url, headers)

        } else {
            throw new MissingMethodException(name, this.class, args)       
        }
    }

    private def getListResult(String url, def headers) throws XeroUnauthorizedException, XeroException {
        def contacts = []

        def resp = oauthService.getXeroResource(oauthToken, url, null, headers)
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = JSON.parse(resp.getBody())

            def contact

            json.Contacts.each {  // iterate over JSON 'status' object in the response:
                //println(it)
                contact = fillContact(it)
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