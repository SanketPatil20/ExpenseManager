package com.example.expensemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expensemanager.databinding.ActivityMainBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnItemsClick {

    ActivityMainBinding binding;
    private ExpensesAdapter expensesAdapter;
    private long income = 0, expense = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expensesAdapter = new ExpensesAdapter(this, this);
        binding.recycler.setAdapter(expensesAdapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        binding.addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                intent.putExtra("type", "Income");
                startActivity(intent);
            }
        });

        binding.addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                intent.putExtra("type", "Expense");
                startActivity(intent);
            }
        });

        binding.viewChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BarChartActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog.show();
            FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.cancel();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.cancel();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        income = 0;
        expense = 0;
        getData();
    }

    private void getData() {
        FirebaseFirestore.getInstance()
                .collection("expenses")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        expensesAdapter.clear();
                        List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();
                        income = 0;
                        expense = 0;

                        Map<String, Long> expenseCategoryMap = new HashMap<>();

                        for (DocumentSnapshot ds : dsList) {
                            ExpenseModel expenseModel = ds.toObject(ExpenseModel.class);
                            expensesAdapter.add(expenseModel);

                            if (expenseModel.getType().equals("Income")) {
                                income += expenseModel.getAmount();
                            } else {
                                expense += expenseModel.getAmount();
                                String category = expenseModel.getCategory();
                                long amount = expenseModel.getAmount();
                                expenseCategoryMap.put(category, expenseCategoryMap.getOrDefault(category, 0L) + amount);
                            }
                        }

                        Log.d("MainActivity", "Income: " + income + ", Expense: " + expense);
                        setUpGraph(expenseCategoryMap);
                        updateOverview();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MainActivity", "Error fetching data: " + e.getMessage());
                    }
                });
    }

    private void setUpGraph(Map<String, Long> expenseCategoryMap) {
        List<PieEntry> pieEntryList = new ArrayList<>();
        List<Integer> colorsList = new ArrayList<>();

        if (income != 0) {
            pieEntryList.add(new PieEntry(income, "Income"));
            colorsList.add(getResources().getColor(R.color.teal_700));
        }

        for (Map.Entry<String, Long> entry : expenseCategoryMap.entrySet()) {
            String category = entry.getKey();
            long amount = entry.getValue();
            pieEntryList.add(new PieEntry(amount, category));
            colorsList.add(getColorForCategory(category));
        }

        if (pieEntryList.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.invalidate();
            return;
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, String.valueOf(income - expense));
        pieDataSet.setColors(colorsList);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.white));
        PieData pieData = new PieData(pieDataSet);

        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(getResources().getColor(android.R.color.transparent));
        binding.pieChart.setTransparentCircleRadius(61f);

        binding.pieChart.setData(pieData);
        binding.pieChart.invalidate();
    }

    private void updateOverview() {
        String currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
        binding.overviewMonth.setText(currentMonth);
        binding.overviewIncome.setText(String.format(Locale.getDefault(), "₹%,d", income));
        binding.overviewExpenses.setText(String.format(Locale.getDefault(), "₹%,d", expense));
    }

    private int getColorForCategory(String category) {
        switch (category.toLowerCase()) {
            case "food":
                return getResources().getColor(R.color.orange);
            case "transport":
                return getResources().getColor(R.color.blue);
            case "entertainment":
                return getResources().getColor(R.color.purple);
            case "shopping":
                return getResources().getColor(R.color.green);
            case "health":
                return getResources().getColor(R.color.red);
            default:
                return getResources().getColor(R.color.gray);
        }
    }

    @Override
    public void onCLick(ExpenseModel expenseModel) {
        Log.d("MainActivity", "Clicked on item: " + expenseModel.getCategory());
        Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
        intent.putExtra("model", expenseModel);
        startActivity(intent);
    }
}

