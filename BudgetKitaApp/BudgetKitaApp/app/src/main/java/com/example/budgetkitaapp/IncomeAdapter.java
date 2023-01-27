package com.example.budgetkitaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.ViewHolder> {

    private ArrayList<Income> incomeArrayList;
    private Context context;

    // creating constructor for our adapter class
    public IncomeAdapter(ArrayList<Income> incomeArrayList, Context context) {
        this.incomeArrayList = incomeArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public IncomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeAdapter.ViewHolder holder, int position) {

        Income income = incomeArrayList.get(position);
        //Bind data to the view in viewHolder
        holder.name.setText("Income name: " + income.getIncomeName());
        holder.total.setText("RM"+ income.getTotalIncome());
        holder.date.setText("Date: " + income.getDateIncome());
    }

    @Override
    public int getItemCount() {
        return incomeArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our text views.
        private final TextView name;
        private final TextView total;
        private final TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            name = itemView.findViewById(R.id.tv2);

            //Set text colour
            total = itemView.findViewById(R.id.tv4);
            total.setTextColor(Color.GREEN);
            date = itemView.findViewById(R.id.tv5);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // after clicking of the item of recycler view.
                    // we are passing our course object to the new activity.
                    Income income = incomeArrayList.get(getAdapterPosition());

                    // below line is creating a new intent.
                    Intent i = new Intent(context, UpdateActivity.class);

                    // below line is for putting our course object to our next activity.
                    i.putExtra("course", income);

                    // after passing the data we are starting our activity.
                    context.startActivity(i);
                }
            });
        }
    }
}