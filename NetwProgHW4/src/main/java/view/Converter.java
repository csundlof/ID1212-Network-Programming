/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Controller;
import java.io.Serializable;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author ADMIN
 */
@Named("converter")
@SessionScoped
public class Converter implements Serializable {

    @EJB
    private Controller controller;
    private Double conversionAmount;
    private String fromCurrency;
    private String targetCurrency;
    private String result;
    private double conversionResult;

    public ArrayList<String> getCurrencies() {
        return controller.getSupportedCurrencies();
    }

    public String getResult() {
        return result;
    }

    public void setTargetCurrency(String curr) {
        targetCurrency = curr;
    }

    public String getTargetCurrency() {
        return null;
    }

    public void setFromCurrency(String curr) {
        fromCurrency = curr;
    }

    public String getFromCurrency() {
        return null;
    }

    public void setConversionAmount(Double amt) {
        conversionAmount = amt;
    }

    public Double getConversionAmount() {
        return null;
    }

    public void convert() {
        conversionResult = controller.Convert(fromCurrency, targetCurrency, conversionAmount);
        result = conversionAmount + " " + fromCurrency + " is worth " + conversionResult + " " + targetCurrency;
    }

}
