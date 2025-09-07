package com.example.expensemanager;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class BarChartActivity extends AppCompatActivity {

    private BarChart barChart;
    private Map<String, Long> incomeMap = new LinkedHashMap<>();
    private Map<String, Long> expenseMap = new LinkedHashMap<>();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        barChart = findViewById(R.id.bar_chart);

        // Initialize maps for the months (e.g., Jan, Feb, Mar, etc.)
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            calendar.set(Calendar.MONTH, i);
            String month = monthFormat.format(calendar.getTime());
            incomeMap.put(month, 0L);
            expenseMap.put(month, 0L);
        }

        fetchExpenses();
    }

    private void fetchExpenses() {
        FirebaseFirestore.getInstance()
                .collection("expenses")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            ExpenseModel model = document.toObject(ExpenseModel.class);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(model.getTime()));
                            String month = monthFormat.format(calendar.getTime());

                            if (incomeMap.containsKey(month)) {
                                if ("Income".equals(model.getType())) {
                                    // Accumulate income amount for the month
                                    incomeMap.put(month, incomeMap.get(month) + model.getAmount());
                                } else {
                                    // Accumulate expense amount for the month
                                    expenseMap.put(month, expenseMap.get(month) + model.getAmount());
                                }
                            }
                        }
                        plotBarGraph();
                    }
                });
    }

    private void plotBarGraph() {
        ArrayList<BarEntry> combinedEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (String month : incomeMap.keySet()) {
            labels.add(month);

            // Create a BarEntry with both income and expense for the month
            float income = incomeMap.get(month) != null ? incomeMap.get(month) : 0f;
            float expense = expenseMap.get(month) != null ? expenseMap.get(month) : 0f;
            combinedEntries.add(new BarEntry(index, new float[]{income, expense}));

            index++;
        }

        // Create a dataset for the stacked bar
        BarDataSet combinedDataSet = new BarDataSet(combinedEntries, "");
        combinedDataSet.setColors(
                getResources().getColor(R.color.teal_700), // Color for income
                getResources().getColor(R.color.Red)       // Color for expense
        );
        combinedDataSet.setStackLabels(new String[]{"Income", "Expense"}); // Labels for the stack

        // Set up the bar data and chart
        BarData barData = new BarData(combinedDataSet);
        barData.setBarWidth(0.5f); // Adjust the width of the bars
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        // Customize the X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setCenterAxisLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.setExtraOffsets(0, 0, 0, 10); // Remove additional offsets at the bottom

        // Customize the Y-axis (left)
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawLabels(true);

        // Disable the right Y-axis
        barChart.getAxisRight().setEnabled(false);

        // Remove duplicate legends
        barChart.getLegend().setEnabled(true); // Ensure legend is enabled only once
        barChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        barChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        barChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);

        barChart.setFitBars(true);
        barChart.invalidate(); // Refresh the chart
    }
}
