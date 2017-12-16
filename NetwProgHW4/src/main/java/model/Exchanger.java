/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author ADMIN
 */
public class Exchanger {

    private static final String BASECURRENCY = "EUR";

    public static Double Exchange(ExchangeRate fromCurrency, ExchangeRate targetCurrency, Double amount) {
        if (fromCurrency.getExchangeRate() > 1) {
            amount /= fromCurrency.getExchangeRate();
        } else {
            amount *= fromCurrency.getExchangeRate();
        }
        if (targetCurrency.getExchangeRate() < 1) {
            amount /= targetCurrency.getExchangeRate();
        } else {
            amount *= targetCurrency.getExchangeRate();
        }
        return amount;
    }

}
