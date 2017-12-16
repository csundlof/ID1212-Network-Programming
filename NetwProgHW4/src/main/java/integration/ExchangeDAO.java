/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import model.ExchangeRate;

/**
 *
 * @author ADMIN
 */
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@Stateless
public class ExchangeDAO {

    @PersistenceContext(unitName = "ExchangeRatesPU")
    EntityManager em;

    public ExchangeDAO() {
        /*
        // Exchange rates in database
        ExchangeRate EUR = new ExchangeRate("eur", 1.0);
        ExchangeRate USD = new ExchangeRate("usd", .85);
        ExchangeRate SEK = new ExchangeRate("sek", .10);
        ExchangeRate RMB = new ExchangeRate("rmb", .13);
        ExchangeRate JPY = new ExchangeRate("jpy", .0076);
        em.persist(JPY);
        em.persist(RMB);
        em.persist(SEK);
        em.persist(USD);
        em.persist(EUR);
         */
    }

    public ArrayList<String> getSupportedCurrencies() {
        ArrayList<String> ret = new ArrayList<String>();
        List<ExchangeRate> exchangeRates = findCurrencies();
        for (ExchangeRate ex : exchangeRates) {
            ret.add(ex.getCurrency());
        }
        return ret;
    }

    private List findCurrencies() {
        return em.createQuery("SELECT e FROM ExchangeRate AS e").getResultList();
    }

    public ExchangeRate getExchangeRate(String currency) {
        return (ExchangeRate) (em.createQuery("SELECT e FROM ExchangeRate e WHERE e.id LIKE :curr").setParameter("curr", currency).getSingleResult());
    }
}
