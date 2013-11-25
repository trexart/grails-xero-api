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

    final static def servicePropertyMap = [:]

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

        def xi = new XeroInvoice()
        String url = API_URL

        if(name.startsWith("findAllBy")) {

            String instructions = name.replaceAll("findAllBy", "")

            Date modifiedSince = null

            if(instructions.contains("And")) {

            } else {

                String propertyName = GrailsNameUtils.getPropertyName(instructions)

                if(xi.metaClass.hasProperty(xi, propertyName)) {
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
            }

            log.debug("url: ${url}")

            // this caches the method, not quite sure how to get it to work though
            // don't think it is exactly correct, needs adjustments
            // doesn't seem to be working
            //this.metaClass."$name" = {-> getListResult(url, headers) }

            return getListResult(url, modifiedSince)

        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

    private def fillInvoice(def it, boolean listResult) {
        log.debug(it)

        def inFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )

        def invoice = new XeroInvoice()
        invoice.id = it.InvoiceID
        invoice.invoiceNumber = it.InvoiceNumber
        invoice.reference = it.Reference
        if(it.DateString != null) {
            invoice.date = inFormat.parse(it.DateString)
        }
        if(it.DueDateString != null) {
            invoice.dueDate = inFormat.parse(it.DueDateString)
        }
        invoice.sentToContact = it.SentToContact
        invoice.currencyCode = it.CurrencyCode
        invoice.subTotal = it.SubTotal
        invoice.totalTax = it.TotalTax
        invoice.total = it.Total
        invoice.amountPaid = it.AmountPaid
        invoice.amountDue = it.AmountDue
        invoice.discounted = it.IsDiscounted
        invoice.lineAmountTypes = it.LineAmountTypes
        invoice.status = it.Status
        invoice.brandingThemeId = it.BrandingThemeID

        def contact = new XeroContact()
        contact.id = it.Contact.ContactID
        contact.name = it.Contact.Name
        if(!listResult) {
            contact.email = it.Contact.EmailAddress
            contact.status = it.Contact.ContactStatus
            //contact.supplier = it.Contact.IsSupplier
            //contact.customer = it.Contact.IsCustomer
        }

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
                invoice = fillInvoice(it, true)

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