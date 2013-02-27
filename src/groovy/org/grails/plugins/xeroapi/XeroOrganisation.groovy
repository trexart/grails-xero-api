/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

/**
 *
 * @author steph
 */
class XeroOrganisation {
    String apiKey
	String name
    String legalName
    String organisationType
    Boolean paysTax
    String taxNumber
    String registrationNumber
    String version
    String status
    Integer financialYearEndDay
    Integer financialYearEndMonth
    String baseCurrency
    String countryCode
    Boolean demoCompany
    Date periodLockDate
    Date createdDateUTC
}

