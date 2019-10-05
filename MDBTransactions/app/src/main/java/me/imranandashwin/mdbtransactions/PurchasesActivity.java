package me.imranandashwin.mdbtransactions;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class PurchasesActivity extends AppCompatActivity implements TextWatcher, View.OnTouchListener {

    RecyclerView purchasesView;
    FloatingActionButton addPurchaseButton;
    SwipeRefreshLayout swipeRefreshLayout;

    EditText searchEntry;
    Button logoutButton;
    TextView totalSpendingTracker;

    PurchasesListAdapter purchasesAdapter;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchases);

        purchasesAdapter = new PurchasesListAdapter(new ArrayList<Purchase>(), getApplicationContext());

        pref = getApplicationContext().getSharedPreferences("MDBTransactions", 0);
        editor = pref.edit();

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                purchasesAdapter.clear();
                FirebaseUtils.getPurchases(swipeRefreshLayout);
            }
        });

        purchasesView = findViewById(R.id.recyclerView);

        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseUtils.startFireBase();

        FirebaseUtils.setListAdapter(purchasesAdapter);
        FirebaseUtils.getPurchases(swipeRefreshLayout);

        totalSpendingTracker = findViewById(R.id.spendingTotal);
        totalSpendingTracker.setText(FirebaseUtils.getTotalSpending(totalSpendingTracker));

        addPurchaseButton = findViewById(R.id.addPurchaseButton);
        addPurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddPurchaseActivity.class);
                startActivity(intent);
            }
        });

        ArrayList<Purchase> purchases = new ArrayList<>();

        purchasesAdapter.setData(purchases);

        // Set adapter for result recycler view
        purchasesView.setAdapter(purchasesAdapter);
        purchasesView.setLayoutManager(new LinearLayoutManager(this));
        purchasesView.setMinimumHeight(purchasesView.getHeight());

        // Set separator between items in result view.
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(purchasesView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getApplication(), R.drawable.separator_horizontal);
        horizontalDecoration.setDrawable(horizontalDivider);
        purchasesView.addItemDecoration(horizontalDecoration);

        searchEntry = findViewById(R.id.searchBarEntry);
        searchEntry.addTextChangedListener(this);
        searchEntry.setText(pref.getString("searchBar", ""));

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PurchasesActivity.this)
                        .setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                FirebaseUtils.getFirebase().signOut();
                                editor.remove("searchBar");
                                editor.commit();
                                searchEntry.setText("");
                                Intent setupIntent = new Intent(getBaseContext(), LoginActivity.class);
                                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(setupIntent);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        enableSwipeToDeleteAndUndo();
        enableSwipeToEdit();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                purchasesAdapter.removeItem(position);
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(purchasesView);
    }

    private void enableSwipeToEdit() {
        final SwipeToEditCallback swipeToEditCallback = new SwipeToEditCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                Intent intent = new Intent(PurchasesActivity.this, EditPurchaseActivity.class);
                Bundle b = new Bundle();
                b.putInt("purchaseNumber", position);
                intent.putExtras(b);
                startActivity(intent);
                purchasesAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToEditCallback);
        itemTouchhelper.attachToRecyclerView(purchasesView);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        FirebaseUtils.filterPurchases(searchEntry.getText().toString());
        editor.putString("searchBar", searchEntry.getText().toString());
        editor.commit();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    // Function to hide the soft-keyboard.
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Focus for closing keyboard if tapped outside of text box.
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideKeyboard(v);
        return false;
    }
}
