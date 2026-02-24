package com.example.personalfinancialmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyPreference {
    private static final String PREF_NAME = "currency_preference";
    private static final String KEY_CURRENCY = "selected_currency";
    private static final String DEFAULT_CURRENCY = "USD";

    public static void setCurrency(Context context, String currencyCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENCY, currencyCode).apply();
    }

    public static String getCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    public static String getCurrencySymbol(String currencyCode) {
        switch (currencyCode) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "INR": return "₹";
            case "CAD": return "C$";
            case "AUD": return "A$";
            case "CNY": return "¥";
            default: return "$";
        }
    }
}
