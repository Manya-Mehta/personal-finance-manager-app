package com.example.personalfinancialmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CurrencySettingsActivity extends AppCompatActivity {

    private Spinner currencySpinner;
    private TextView sampleConversionText;
    private Button saveCurrencyButton;
    private String selectedCurrency = "USD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_settings);

        // Initialize views
        currencySpinner = findViewById(R.id.currencySpinner);
        sampleConversionText = findViewById(R.id.sampleConversionText);
        saveCurrencyButton = findViewById(R.id.saveCurrencyButton);

        // Set up back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Set current currency in spinner
        String currentCurrency = CurrencyPreference.getCurrency(this);
        int currencyPosition = getCurrencyPosition(currentCurrency);
        if (currencyPosition >= 0) {
            currencySpinner.setSelection(currencyPosition);
            selectedCurrency = currentCurrency;
        }

        // Update sample conversion text
        updateSampleConversion(selectedCurrency);

        // Handle currency selection changes
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] currencies = getResources().getStringArray(R.array.currencies);
                String selected = currencies[position];
                // Extract currency code from the display string (e.g., "USD ($)" -> "USD")
                selectedCurrency = selected.substring(0, 3);
                updateSampleConversion(selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Save currency settings
        saveCurrencyButton.setOnClickListener(v -> {
            CurrencyPreference.setCurrency(CurrencySettingsActivity.this, selectedCurrency);
            Toast.makeText(CurrencySettingsActivity.this, 
                    "Currency set to " + selectedCurrency, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateSampleConversion(String targetCurrency) {
        double baseAmount = 100.0;
        double convertedAmount = CurrencyConverter.convert(baseAmount, "USD", targetCurrency);
        String symbol = CurrencyPreference.getCurrencySymbol(targetCurrency);
        
        sampleConversionText.setText(String.format("100 USD = %.2f %s (%s)", 
                convertedAmount, targetCurrency, symbol));
    }

    private int getCurrencyPosition(String currencyCode) {
        String[] currencies = getResources().getStringArray(R.array.currencies);
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].startsWith(currencyCode)) {
                return i;
            }
        }
        return 0; // Default to first position (USD)
    }
}
