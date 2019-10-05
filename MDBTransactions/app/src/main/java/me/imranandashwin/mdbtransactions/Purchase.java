package me.imranandashwin.mdbtransactions;

import android.graphics.Bitmap;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Purchase {

    private String itemName;
    private double cost;
    private String purchaser;
    private String description;
    private String suppliers;
    private Calendar datePurchased;
    private StorageReference imageUrl;
    private Bitmap image;
    private String id;

    public Purchase () {
        this.itemName = "";
        this.cost = 0.0;
        this.purchaser = "";
        this.description = "";
        this.suppliers = "";
        this.datePurchased = Calendar.getInstance();
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(String suppliers) {
        this.suppliers = suppliers;
    }

    public Calendar getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(Calendar datePurchased) {
        this.datePurchased = datePurchased;
    }

    public String getItemName() { return itemName; }

    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getPurchaser() { return purchaser; }

    public void setPurchaser(String purchaser) { this.purchaser = purchaser; }

    public StorageReference getImageUrl() { return imageUrl; }

    public void setImageUrl(StorageReference imageUrl) { this.imageUrl = imageUrl; }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }
}
