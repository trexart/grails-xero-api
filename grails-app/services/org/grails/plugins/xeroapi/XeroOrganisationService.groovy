/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

import grails.converters.*

import uk.co.desirableobjects.oauth.scribe.OauthService

import org.scribe.model.Token

/**
 *
 * @author steph
 */
class XeroOrganisationService {
    final static String API_URL = "https://api.xero.com/api.xro/2.0/Organisation"
    
    boolean transactional = false

    OauthService oauthService
    Token oauthToken
    
    def setAuth(Token token) {
        oauthToken = token
    }
	
    def get() throws XeroUnauthorizedException, XeroException {
        def org = new XeroOrganisation()
        def dateFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
        def dateFormatUTC = new java.text.SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.sss" )
        
        //def resp = oauthService.getXeroResource(oauthToken, API_URL, null, ['Content-Type':'application/json', 'Accept':'application/json'])
        def resp = oauthService.getXeroResource(oauthToken, API_URL)
        //log.debug(resp.getCode())

        if( resp.getCode() == 200 ) {
            //log.debug(resp.getBody())

            def json = XML.parse(resp.getBody())
            //org.apiKey = json.Organisations[0].APIKey
            org.apiKey = json.Organisations.Organisation.APIKey
            org.name = json.Organisations.Organisation.Name
            org.legalName = json.Organisations.Organisation.LegalName
            org.paysTax = json.Organisations.Organisation.PaysTax
            org.taxNumber = json.Organisations.Organisation.TaxNumber
            org.registrationNumber = json.Organisations.Organisation.RegistrationNumber
            org.version = json.Organisations.Organisation.Version
            org.organisationType = json.Organisations.Organisation.OrganisationType
            org.status = json.Organisations.Organisation.OrganisationStatus
            org.financialYearEndDay = ((json.Organisations.Organisation.FinancialYearEndDay as String) as Integer)
            org.financialYearEndMonth = ((json.Organisations.Organisation.FinancialYearEndMonth as String) as Integer)
            org.baseCurrency = json.Organisations.Organisation.BaseCurrency
            org.countryCode = json.Organisations.Organisation.CountryCode
            org.demoCompany = json.Organisations.Organisation.IsDemoCompany

            String dateString = json.Organisations.Organisation.PeriodLockDate
            org.periodLockDate = dateFormat.parse(dateString)

            dateString = json.Organisations.Organisation.CreatedDateUTC
            org.createdDateUTC = dateFormatUTC.parse(dateString)
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
        
        return org
    }
}

