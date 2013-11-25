/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.plugins.xeroapi

/**
 *
 * @author steph
 */
class XeroInvoice {
	String id
    String invoiceNumber
    String reference
    String type
    Date date
    Date dueDate
    String lineAmountTypes
    BigDecimal subTotal
    BigDecimal totalTax
    BigDecimal total
    BigDecimal amountDue
    BigDecimal amountPaid
    BigDecimal amountCredited
    String currencyCode
    String status
    Boolean discounted
    Boolean sentToContact
    Date updatedDateUTC

    String brandingThemeId
    XeroContact contact
}

