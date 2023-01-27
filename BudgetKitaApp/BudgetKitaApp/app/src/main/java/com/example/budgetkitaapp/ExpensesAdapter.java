package com.example.budgetkitaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ViewHolder> {

    private ArrayList<Expenses> expensesArrayList;
    private Context context;

    public ExpensesAdapter(ArrayList<Expenses> expensesArrayList, Context context){
        this.expensesArrayList = expensesArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ExpensesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpensesAdapter.ViewHolder holder, int position) {

        Expenses expenses = expensesArrayList.get(position);
        //Bind data to the view in viewHolder
        holder.nameE.setText("Expense name: " + expenses.getExpensesName());
        holder.totalE.setText("RM"+ expenses.getExpensesTotal());
        holder.dateE.setText("Date: " + expenses.getExpensesDate());

    }

    @Override
    public int getItemCount() {
        return expensesArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        // creating variables for our text views.
        private final TextView nameE;
        private final TextView totalE;
        private final TextView dateE;


        public ViewHolder(View inflate) {
            super(inflate);
            // initializing our text views.
            nameE = itemView.findViewById(R.id.tv2);
            //Set text colour
            totalE = itemView.findViewById(R.id.tv4);
            totalE.setTextColor(Color.RED);
            dateE = itemView.findViewById(R.id.tv5);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // after clicking of the item of recycler view.
                    // we are passing our course object to the new activity.
                    Expenses lah = expensesArrayList.get(getAdapterPosition());

                    // below line is creating a new intent.
                    Intent i = new Intent(context, UpdateExpenses.class);

                    // below line is for putting our course object to our next activity.
                    i.putExtra("expenses", lah);

                    // after passing the data we are starting our activity.
                    context.startActivity(i);
                }
            });
        }
    }
}
