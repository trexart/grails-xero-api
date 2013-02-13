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
class XeroOrganisationService {
    final static String API_URL = "https://api.xero.com/api.xro/2.0/Organisation"
    final static String REQUEST = "Organisation"
    
    boolean transactional = false
    
    String oauthKey
    String oauthSecret
    
    def setAuth(String key, String secret) {
        oauthKey = key
        oauthSecret = secret
    }
	
    def get() throws XeroUnauthorizedException, XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def org = new XeroOrganisation()
        
        RESTClient restClient = new RESTClient( API_URL )
        restClient.auth.oauth key, secret, oauthKey, oauthSecret
        
        try {
            def resp = restClient.get(requestContentType: 'application/json',
                contentType: 'application/json' ){ resp, json ->

                if( resp.status == 200 ) {
                    println(json)

                    /*json.each {  // iterate over JSON 'status' object in the response:
                        println it.created_at
                        println '  ' + it.text
                    }*/

                    org.name = json.Organisations[0].Name
                    org.legalName = json.Organisations[0].LegalName
                    org.paysTax = json.Organisations[0].PaysTax
                    org.taxNumber = json.Organisations[0].TaxNumber
                    org.registrationNumber = json.Organisations[0].RegistrationNumber
                    org.version = json.Organisations[0].Version
                    org.organisationType = json.Organisations[0].OrganisationType
                    org.status = json.Organisations[0].OrganisationStatus
                    org.financialYearEndDay = json.Organisations[0].FinancialYearEndDay
                    org.financialYearEndMonth = json.Organisations[0].FinancialYearEndMonth
                    org.baseCurrency = json.Organisations[0].BaseCurrency
                    org.countryCode = json.Organisations[0].CountryCode
                    org.demoCompany = json.Organisations[0].IsDemoCompany
                    /*org.periodLockDate = json.Organisations[0].PeriodLockDate
                    org.createdDateUTC = json.Organisations[0].CreatedDateUTC*/
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
        
        return org
    }
}

