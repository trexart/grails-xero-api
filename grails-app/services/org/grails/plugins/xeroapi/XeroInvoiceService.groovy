/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

import grails.converters.*
import grails.util.GrailsNameUtils

import uk.co.desirableobjects.oauth.scribe.OauthService

import org.scribe.model.Token

/**
 *
 * @author steph
 */
class XeroInvoiceService {
    final static String API_URL = "https://api.xero.com/api.xro/2.0/Invoices"
    
    boolean transactional = false
    
    OauthService oauthService
    Token oauthToken
    
    def setAuth(Token token) {
        oauthToken = token
    }

    def getAll(Date modifiedSince = null) throws XeroUnauthorizedException, XeroException {

        return getListResult(API_URL, modifiedSince)
    }

    def getAllByType(String type, String status = null, Date modifiedSince = null) throws XeroException {
        
        String where = 'Type=="' + type + '"'

        if(status) {
        	where += 'AND STATUS=="' + status + '"'
        }

        String url = API_URL + "?where=" + where.encodeAsURL()
        log.debug 'url: ' + url

        return getListResult(url, modifiedSince)
    }

    def getAllPaidSince(Date paidSince, Date modifiedSince = null) throws XeroException {

        String where = 'FullyPaidOnDate >= DateTime(' + (paidSince.year+1900) + ', ' + (paidSince.month+1) + ', ' + paidSince.date + ')'
        log.debug 'where: ' + where

        String url = API_URL + "?where=" + where.encodeAsURL()
        log.debug 'url: ' + url

        return getListResult(url, modifiedSince)
    }

    private def fillInvoice(def it) {
        def invoice = new XeroInvoice()
        invoice.id = it.InvoiceID
        invoice.invoiceNumber = it.InvoiceNumber
        invoice.status = it.Status

        def contact = new XeroContact()
        contact.id = it.Contact.ContactID
        contact.name = it.Contact.Name
        contact.email = it.Contact.EmailAddress
        contact.status = it.Contact.ContactStatus
        //contact.supplier = it.Contact.IsSupplier
        //contact.customer = it.Contact.IsCustomer

        invoice.contact = contact

        return invoice
    }

    private def getListResult(String url, Date modifiedSince = null) throws XeroUnauthorizedException, XeroException {
        def invoices = []

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

            def invoice

            json.Invoices.each {  // iterate over JSON 'status' object in the response:
                //println(it)
                invoice = fillInvoice(it)

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
                case 503:
                    throw new XeroNotAvailableException()
                    break
                default:
                    throw new XeroException()
                
            }
        }
        
        return invoices
    }
}