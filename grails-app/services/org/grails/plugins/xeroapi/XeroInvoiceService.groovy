/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import grails.converters.*

import uk.co.desirableobjects.oauth.scribe.OauthService

import org.scribe.model.Token

/**
 *
 * @author steph
 */
class XeroInvoiceService {
    final static String API_URL = "https://api.xero.com/api.xro/2.0/Invoices"
    final static String REQUEST = "Invoices"
    
    boolean transactional = false
    
    OauthService oauthService
    Token oauthToken
    
    def setAuth(Token token) {
        oauthToken = token
    }

    def getAll() throws XeroUnauthorizedException, XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def invoices = []

        def resp = oauthService.getXeroResource(oauthToken, API_URL, null, ['Content-Type':'application/json', 'Accept':'application/json'])
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = JSON.parse(resp.getBody())

            def invoice

            json.Invoices.each {  // iterate over JSON 'status' object in the response:
                //println(it)
                invoice = new XeroInvoice()
                invoice.id = it.InvoiceID
                invoice.invoiceNumber = it.InvoiceNumber

                invoices.add(invoice)
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
        
        return invoices
    }

    def getAllByType(String type, String status = null, Date modifiedSince = null) throws XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def invoices = []
        
        String where = 'Type=="' + type + '"'

        if(status) {
        	where += 'AND STATUS=="' + status + '"'
        }

        String url = API_URL + "?where=" + where.encodeAsURL()
        log.debug 'url: ' + url



        /*if(modifiedSince != null) {
            def outFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
            outFormat.timeZone = java.util.TimeZone.getTimeZone( 'GMT' )
            log.debug 'modified date in local time: ' + modifiedSince.format("yyyy-MM-dd'T'HH:mm:ss")
            log.debug 'modified date in GMT: ' + outFormat.format(modifiedSince)
            restClient.headers["If-Modified-Since"] = outFormat.format(modifiedSince)
        }*/

        def resp = oauthService.getXeroResource(oauthToken, url, null, ['Content-Type':'application/json', 'Accept':'application/json'])
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = JSON.parse(resp.getBody())

            def invoice
            def contact

            json.Invoices.each {  // iterate over JSON 'status' object in the response:
                //println(it)
                invoice = new XeroInvoice()
                invoice.id = it.InvoiceID
                invoice.invoiceNumber = it.InvoiceNumber
                invoice.status = it.Status

                contact = new XeroContact()
                contact.id = it.Contact.ContactID
                contact.name = it.Contact.Name
                contact.email = it.Contact.EmailAddress
                contact.status = it.Contact.ContactStatus
                //contact.supplier = it.Contact.IsSupplier
                //contact.customer = it.Contact.IsCustomer

                invoice.contact = contact

                invoices.add(invoice)
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
        
        return invoices
    }
}