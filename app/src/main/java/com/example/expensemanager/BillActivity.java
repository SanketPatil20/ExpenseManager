package com.example.expensemanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class BillActivity extends AppCompatActivity {

    private String tripId;
    private ListView lvList;
    private BillAdapter billAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        initViews();


        Button btnAddBill = findViewById(R.id.btnAddBill);
        btnAddBill.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddBillActivity.class);
            intent.putExtra("tripId", tripId);
            startActivity(intent);
        });

        Button btnSplit = findViewById(R.id.btnSplit);
        btnSplit.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SplitActivity.class);
            intent.putExtra("tripId", tripId);
            startActivity(intent);
        });

        Cursor result = billAdapter.getBill(Integer.parseInt(tripId));
        String[] columns = new String[]{Helper.COLUMN_BILL_NAME, Helper.COLUMN_BILL_AMOUNT, Helper.COLUMN_BILL_SHARE, Helper.COLUMN_BILL_MEMBERS};
        int[] to = new int[]{R.id.tvBillName, R.id.tvBillAmount, R.id.tvBillShare, R.id.tvBillMembers};
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.list_item_bill, result, columns, to, 0);
        lvList.setAdapter(dataAdapter);
    }

    private void initViews() {
        tripId = getIntent().getStringExtra("tripId");
        this.billAdapter = new BillAdapter(this);
        this.lvList = findViewById(R.id.lvBillList);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        billAdapter.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
