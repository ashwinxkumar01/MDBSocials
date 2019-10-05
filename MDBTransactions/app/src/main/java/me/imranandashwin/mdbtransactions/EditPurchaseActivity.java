package me.imranandashwin.mdbtransactions;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EditPurchaseActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final int PICK_IMAGE_ID = 234;

    private DatePickerDialog datePickerDialog;
    private final Calendar today = Calendar.getInstance();
    private Calendar currentDate;

    private EditText purchaseName;
    private EditText datePurchased;
    private EditText purchaseSupplier;
    private EditText purchaseDescription;
    private CurrencyEditText purchaseCost;

    private ImageView purchasedItemPhoto;
    private Bitmap currBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_purchase);

        Bundle b = getIntent().getExtras();
        assert b != null;
        final int pos = b.getInt("purchaseNumber");
        Purchase p = FirebaseUtils.getListAdapter().getData().get(pos);

        datePickerDialog = new DatePickerDialog(this, this, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        currentDate = Calendar.getInstance();
        currentDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        datePurchased = findViewById(R.id.editPurchaseDate);
        datePurchased.setClickable(true);
        datePurchased.setKeyListener(null);
        datePurchased.setFocusable(false);
        datePurchased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        purchaseName = findViewById(R.id.editPurchaseName);
        purchaseCost = findViewById(R.id.editPurchaseCost);
        purchaseDescription = findViewById(R.id.editPurchaseDesc);
        purchaseSupplier = findViewById(R.id.editPurchaseSupplier);

        purchasedItemPhoto = findViewById(R.id.editPurchasePhoto);
        currBitmap = Utils.drawableToBitmap(purchasedItemPhoto.getDrawable());

        ImageButton addPhotoButton = findViewById(R.id.editPhotoButton);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });

        Button cancelButton = findViewById(R.id.editCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button saveButton = findViewById(R.id.editPurchaseSave);
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

                purchase.setSuppliers(purchaseSupplier.getText().toString());

                purchase.setPurchaser(FirebaseUtils.getFirebase().getCurrentUser().getDisplayName());

                purchase.setDatePurchased(currentDate);

                purchase.setDescription(purchaseDescription.getText().toString());

                purchase.setImage(((BitmapDrawable)purchasedItemPhoto.getDrawable()).getBitmap());

                FirebaseUtils.editPurchase(pos, purchase);
                finish();
            }
        });

        purchaseName.setText(p.getItemName());
        purchaseDescription.setText(p.getDescription());
        purchaseSupplier.setText(p.getSuppliers());
        purchaseCost.setCurrencyText(String.valueOf(p.getCost()));

        Calendar dte = p.getDatePurchased();
        datePickerDialog = new DatePickerDialog(this, this, dte.get(Calendar.YEAR), dte.get(Calendar.MONTH), dte.get(Calendar.DATE));
        currentDate = Calendar.getInstance();
        currentDate.set(dte.get(Calendar.YEAR), dte.get(Calendar.MONTH), dte.get(Calendar.DATE));
        datePurchased.setText(Utils.formatDate(p.getDatePurchased().getTime()));

        if (p.getImageUrl() != null) {
            p.getImageUrl().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageURL = uri.toString();
                    Glide.with(getApplicationContext())
                            .load(imageURL)
                            .override(purchasedItemPhoto.getWidth(), purchasedItemPhoto.getHeight())
                            .transform(new CenterCrop(), new RoundedCorners(32))
                            .into(purchasedItemPhoto);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        } else {
            Glide.with(getApplicationContext())
                    .load(R.drawable.placeholder)
                    .override(purchasedItemPhoto.getWidth(), purchasedItemPhoto.getHeight())
                    .transform(new CenterCrop(), new RoundedCorners(32)).into(purchasedItemPhoto);
        }
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
        if (requestCode == PICK_IMAGE_ID) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
            currBitmap = bitmap;
            purchasedItemPhoto.setImageBitmap(bitmap);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
