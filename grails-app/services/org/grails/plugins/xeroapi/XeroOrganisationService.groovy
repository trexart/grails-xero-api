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
class XeroOrganisationService {
    final static String API_URL = "https://api.xero.com/api.xro/2.0/Organisation"
    final static String REQUEST = "Organisation"
    
    boolean transactional = false

    OauthService oauthService
    Token oauthToken
    
    def setAuth(Token token) {
        oauthToken = token
    }
	
    def get() throws XeroUnauthorizedException, XeroException {
        String key = CH.config.xero.key.toString()
        String secret = CH.config.xero.secret.toString()
        def org = new XeroOrganisation()
        
        def resp = oauthService.getXeroResource(oauthToken, API_URL, null, ['Content-Type':'application/json', 'Accept':'application/json'])
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = JSON.parse(resp.getBody())

            org.apiKey = json.Organisations[0].APIKey
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
        
        return org
    }
}

