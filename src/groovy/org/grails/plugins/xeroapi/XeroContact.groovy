/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

/**
 *
 * @author steph
 */
class XeroContact {
	String id
	String number
    String name
    String firstName
    String lastName
    String email
    String skypeUserName
    String bankAccountDetails
    String taxNumber
    String accountsReceivableTaxType
    String accountsPayableTaxType
    String defaultCurrency
    boolean supplier
    boolean customer
    String status
    Date updatedDateUTC
}

