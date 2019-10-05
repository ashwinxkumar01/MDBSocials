package me.imranandashwin.mdbtransactions;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class AddPurchaseActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final int PICK_IMAGE_ID = 234;

    private DatePickerDialog datePickerDialog;
    private final Calendar today = Calendar.getInstance();
    private Calendar currentDate;

    private EditText purchaseName;
    private EditText datePurchased;
    private EditText purchaseDescription;
    private EditText purchaseSupplier;
    private CurrencyEditText purchaseCost;

    private ImageView purchasedItemPhoto;
    private Bitmap currBitmap;

    private ImageButton addPhotoButton;
    private Button cancelButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        datePickerDialog = new DatePickerDialog(this, this, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        currentDate = Calendar.getInstance();
        currentDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        datePurchased = findViewById(R.id.addPurchaseDate);
        datePurchased.setClickable(true);
        datePurchased.setKeyListener(null);
        datePurchased.setFocusable(false);
        datePurchased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        purchaseName = findViewById(R.id.addPurchaseName);
        purchaseCost = findViewById(R.id.addPurchaseCost);
        purchaseSupplier = findViewById(R.id.addPurchaseSupplier);
        purchaseDescription = findViewById(R.id.addPurchaseDesc);

        purchasedItemPhoto = findViewById(R.id.addPurchasePhoto);
        currBitmap = Utils.drawableToBitmap(purchasedItemPhoto.getDrawable());

        addPhotoButton = findViewById(R.id.addPhotoButton);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton = findViewById(R.id.addPurchaseSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (purchaseName.getText().toString().toLowerCase().isEmpty() ||
                    purchaseCost.getText().toString().substring(8).trim().isEmpty() ||
                    datePurchased.getText().toString().trim().isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                Purchase purchase = new Purchase();

                purchase.setItemName(purchaseName.getText().toString());

                double cost = Double.parseDouble(purchaseCost.getText().toString().substring(8).trim().replace(",", ""));
                purchase.setCost(cost);

                purchase.setPurchaser(FirebaseUtils.getFirebase().getCurrentUser().getDisplayName());

                purchase.setDatePurchased(currentDate);

                purchase.setSuppliers(purchaseSupplier.getText().toString());

                purchase.setDescription(purchaseDescription.getText().toString());

                purchase.setImage(currBitmap);

                FirebaseUtils.addPurchase(purchase);
                finish();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar newDate = Calendar.getInstance();
        newDate.set(year, monthOfYear, dayOfMonth);
        currentDate = newDate;
        datePurchased.setText(Utils.formatDate(newDate.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                currBitmap = bitmap;
                purchasedItemPhoto.setImageBitmap(bitmap);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
