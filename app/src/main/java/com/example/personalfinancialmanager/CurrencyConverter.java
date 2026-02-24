package com.example.personalfinancialmanager;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {
    // Exchange rates relative to USD (as of a recent date)
    // In a real app, you would fetch these from an API
    private static final Map<String, Double> EXCHANGE_RATES = new HashMap<>();
    
    static {
        // Base currency is USD
        EXCHANGE_RATES.put("USD", 1.0);
        EXCHANGE_RATES.put("EUR", 0.85);
        EXCHANGE_RATES.put("GBP", 0.74);
        EXCHANGE_RATES.put("JPY", 110.0);
        EXCHANGE_RATES.put("INR", 75.0);
        EXCHANGE_RATES.put("CAD", 1.25);
        EXCHANGE_RATES.put("AUD", 1.35);
        EXCHANGE_RATES.put("CNY", 6.45);
    }

    /**
     * Convert an amount from one currency to another
     * @param amount Amount to convert
     * @param fromCurrency Source currency code
     * @param toCurrency Target currency code
     * @return Converted amount
     */
    public static double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        // Convert to USD first (if not already USD)
        double amountInUSD = amount;
        if (!fromCurrency.equals("USD")) {
            amountInUSD = amount / EXCHANGE_RATES.getOrDefault(fromCurrency, 1.0);
        }
        
        // Convert from USD to target currency
        return amountInUSD * EXCHANGE_RATES.getOrDefault(toCurrency, 1.0);
    }
}
