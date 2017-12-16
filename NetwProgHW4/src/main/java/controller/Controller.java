/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import integration.ExchangeDAO;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import model.ExchangeRate;
import model.Exchanger;

/**
 *
 * @author ADMIN
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class Controller {

    @EJB
    ExchangeDAO exchangeDB;

    public ArrayList<String> getSupportedCurrencies() {
        return exchangeDB.getSupportedCurrencies();
    }

    public double Convert(String fromCurrency, String targetCurrency, Double conversionAmount) {
        ExchangeRate fromCurr = exchangeDB.getExchangeRate(fromCurrency);
        ExchangeRate targetCurr = exchangeDB.getExchangeRate(targetCurrency);
        return Exchanger.Exchange(fromCurr, targetCurr, conversionAmount);
    }
}
