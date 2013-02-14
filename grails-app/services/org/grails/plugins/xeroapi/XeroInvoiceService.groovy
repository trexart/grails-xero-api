/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import grails.converters.*

import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException

/**
 *
 * @author steph
 */
class XeroInvoiceService {
    final static String API_URL = "https://api.xero.com/api.xro/2.0/Invoices"
    final static String REQUEST = "Invoices"
    
    boolean transactional = false
    
    String oauthKey
    String oauthSecret
    
    def setAuth(String key, String secret) {
        oauthKey = key
        oauthSecret = secret
    }

    def getAll() throws XeroUnauthorizedException, XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def invoices = []
        
        RESTClient restClient = new RESTClient( API_URL )
        restClient.auth.oauth key, secret, oauthKey, oauthSecret
        
        try {
            def resp = restClient.get(requestContentType: 'application/json',
                contentType: 'application/json' ){ resp, json ->

                if( resp.status == 200 ) {
                    //println(json)

                    def invoice

                    json.Invoices.each {  // iterate over JSON 'status' object in the response:
                    	//println(it)
                        invoice = new XeroInvoice()
                        invoice.id = it.InvoiceID
                        invoice.invoiceNumber = it.InvoiceNumber

                        invoices.add(invoice)
                    }
                } else {
                    println resp.dump()
                }
            }
        }
        catch( HttpResponseException ex ) { 
            println ex.response.dump()
            switch (ex.response.status) {
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
        RESTClient restClient = new RESTClient( url )
        restClient.auth.oauth key, secret, oauthKey, oauthSecret

        if(modifiedSince != null) {
        	def outFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
        	outFormat.timeZone = java.util.TimeZone.getTimeZone( 'GMT' )
        	log.debug 'modified date in local time: ' + modifiedSince.format("yyyy-MM-dd'T'HH:mm:ss")
        	log.debug 'modified date in GMT: ' + outFormat.format(modifiedSince)
        	restClient.headers["If-Modified-Since"] = outFormat.format(modifiedSince)
        }
        
        try {
            def resp = restClient.get(requestContentType: 'application/json',
                contentType: 'application/json' ){ resp, json ->

                if( resp.status == 200 ) {
                    //println(json)

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
                        contact.status = it.Contact.ContactStatus
                        //contact.supplier = it.Contact.IsSupplier
                        //contact.customer = it.Contact.IsCustomer

                        invoice.contact = contact

                        invoices.add(invoice)
                    }
                } else {
                    println resp.dump()
                }
            }
        }
        catch( HttpResponseException ex ) { 
            println ex.response.dump()
            switch (ex.response.status) {
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