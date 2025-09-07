package com.example.expensemanager;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.expensemanager.databinding.ActivityAddExpenseBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity {

    ActivityAddExpenseBinding binding;  // ViewBinding for activity
    private String type;
    private ExpenseModel expenseModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // Set content view with ViewBinding

        // Get the type (Income or Expense) from the intent
        type = getIntent().getStringExtra("type");
        expenseModel = (ExpenseModel) getIntent().getSerializableExtra("model");

        // Set up the Spinner for category selection
        String[] categories = new String[]{"Food", "Transport", "Entertainment", "Health", "Shopping","House","Personal","Saving and Investment","Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(adapter);

        if (expenseModel != null) {
            // Pre-fill fields if updating an existing expense
            type = expenseModel.getType();
            binding.amount.setText(String.valueOf(expenseModel.getAmount()));
            binding.note.setText(expenseModel.getNote());
            // Set the selected item in the spinner based on the category
            int position = adapter.getPosition(expenseModel.getCategory());
            binding.categorySpinner.setSelection(position);
            binding.deleteButton.setVisibility(View.VISIBLE);  // Make delete button visible
        }

        // Pre-select radio button based on the type
        if ("Income".equals(type)) {
            binding.incomeRadio.setChecked(true);
        } else {
            binding.expenseRadio.setChecked(true);
        }

        // Set click listeners for the radio buttons to update 'type'
        binding.incomeRadio.setOnClickListener(view -> type = "Income");
        binding.expenseRadio.setOnClickListener(view -> type = "Expense");

        // Save button logic
        binding.saveButton.setOnClickListener(view -> {
            if (expenseModel != null) {
                updateExpense();  // Update existing entry
            } else {
                createExpense();  // Create a new entry
            }
        });

        // Delete button logic
        binding.deleteButton.setOnClickListener(view -> {
            if (expenseModel != null) {
                deleteExpense();  // Delete existing entry
            } else {
                Toast.makeText(AddExpenseActivity.this, "No expense to delete", Toast.LENGTH_SHORT).show();
            }
        });

        // Apply edge-to-edge support (optional, for display purposes)
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void createExpense() {
        String expenseId = UUID.randomUUID().toString();
        String amount = binding.amount.getText().toString();
        String note = binding.note.getText().toString();
        String category = binding.categorySpinner.getSelectedItem().toString();

        // Check for empty amount input and show error if empty
        if (amount.trim().isEmpty()) {
            binding.amount.setError("Empty");
            return;
        }

        // Create an ExpenseModel object and save it to Firestore
        ExpenseModel expenseModel = new ExpenseModel(expenseId, note, category, type, Long.parseLong(amount),
                Calendar.getInstance().getTimeInMillis(), FirebaseAuth.getInstance().getUid());

        FirebaseFirestore.getInstance().collection("expenses").document(expenseId).set(expenseModel);
        finish();  // Close the activity after saving
    }

    private void updateExpense() {
        String expenseId = expenseModel.getExpenseId();
        String amount = binding.amount.getText().toString();
        String note = binding.note.getText().toString();
        String category = binding.categorySpinner.getSelectedItem().toString();

        // Check for empty amount input and show error if empty
        if (amount.trim().isEmpty()) {
            binding.amount.setError("Empty");
            return;
        }

        // Create an updated ExpenseModel object and save it to Firestore
        ExpenseModel model = new ExpenseModel(expenseId, note, category, type, Long.parseLong(amount),
                expenseModel.getTime(), FirebaseAuth.getInstance().getUid());

        FirebaseFirestore.getInstance().collection("expenses").document(expenseId).set(model);
        finish();  // Close the activity after updating
    }

    private void deleteExpense() {
        FirebaseFirestore.getInstance().collection("expenses").document(expenseModel.getExpenseId()).delete();
        finish();  // Close the activity after deletion
    }
}
