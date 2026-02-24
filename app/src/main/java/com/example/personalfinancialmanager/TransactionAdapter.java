package com.example.personalfinancialmanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.List;

public class TransactionAdapter extends BaseAdapter {
    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @Override
    public int getCount() {
        return transactionList.size();
    }

    @Override
    public Object getItem(int position) {
        return transactionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        }

        TextView amountView = convertView.findViewById(R.id.amountView);
        TextView descriptionView = convertView.findViewById(R.id.descriptionView);
        TextView categoryView = convertView.findViewById(R.id.categoryView);
        TextView typeView = convertView.findViewById(R.id.typeView);
        ImageView transactionIcon = convertView.findViewById(R.id.transactionIcon);
        CardView iconBackground = convertView.findViewById(R.id.iconBackground);

        Transaction transaction = transactionList.get(position);

        amountView.setText("\u20B9" + String.format("%.2f", Math.abs(transaction.getAmount())));
        descriptionView.setText(transaction.getDescription());
        categoryView.setText(transaction.getCategory());
        typeView.setText(transaction.getType());

        // Customize appearance based on transaction type
        if (transaction.getType().equalsIgnoreCase("Expense")) {
            amountView.setTextColor(Color.parseColor("#D32F2F"));
            iconBackground.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            transactionIcon.setImageResource(R.drawable.ic_expense);
            transactionIcon.setColorFilter(Color.parseColor("#D32F2F"));
        } else {
            amountView.setTextColor(Color.parseColor("#388E3C"));
            iconBackground.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            transactionIcon.setImageResource(R.drawable.ic_income);
            transactionIcon.setColorFilter(Color.parseColor("#388E3C"));
        }

        return convertView;
    }

    public void updateList(List<Transaction> newTransactions) {
        this.transactionList = newTransactions;
        notifyDataSetChanged();
    }

    // Add this method to get the current transaction list
    public List<Transaction> getTransactionList() {
        return transactionList;
    }
}
