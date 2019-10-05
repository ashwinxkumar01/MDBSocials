package me.imranandashwin.mdbtransactions;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FirebaseUtils {

    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDB;
    private static StorageReference mStorageRef;

    private static SwipeRefreshLayout refreshLayout;

    private static PurchasesListAdapter listAdapter;
    final private static ArrayList<Purchase> purchases = new ArrayList<>();
    private static String currentFilter = "";
    private static TextView spendingTracker;

    public static FirebaseAuth getFirebase() {
        return mAuth;
    }

    public static void startFireBase() {
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public static boolean isLoggedIn() { return mAuth.getCurrentUser() != null; }

    public static void addPurchase(Purchase p) {
        refreshLayout.setRefreshing(true);
        displayPurchase(p);
        DatabaseReference purchaseTitleRef = mDB.getReference().child("purchase_titles").push();
        String purchaseKey = purchaseTitleRef.getKey();
        purchaseTitleRef.child("title").setValue(p.getItemName());

        DatabaseReference purchaseCostRef = mDB.getReference().child("purchase_costs");
        purchaseCostRef.child(purchaseKey).child("cost").setValue(p.getCost()).toString();

        DatabaseReference purchaseSupplier = mDB.getReference().child("purchase_suppliers");
        purchaseSupplier.child(purchaseKey).child("suppliers").setValue(p.getSuppliers()).toString();

        DatabaseReference purchaseDateRef = mDB.getReference().child("purchase_dates");
        purchaseDateRef.child(purchaseKey).child("month").setValue(p.getDatePurchased().get(Calendar.MONTH));
        purchaseDateRef.child(purchaseKey).child("day").setValue(p.getDatePurchased().get(Calendar.DAY_OF_MONTH));
        purchaseDateRef.child(purchaseKey).child("year").setValue(p.getDatePurchased().get(Calendar.YEAR));

        DatabaseReference purchaseDescRef = mDB.getReference().child("purchase_desc");
        purchaseDescRef.child(purchaseKey).child("desc").setValue(p.getDescription());

        DatabaseReference purchasePurchaser = mDB.getReference().child("purchase_purchaser");
        purchasePurchaser.child(purchaseKey).child("name").setValue(p.getPurchaser());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        p.getImage().compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final StorageReference imageLocation = mStorageRef.child("purchase_images/" + purchaseKey + ".png");
        UploadTask uploadTask = imageLocation.putBytes(data);
        p.setImageUrl(imageLocation);

        spendingTracker.setText(getTotalSpending(spendingTracker));

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageLocation.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    getPurchases(refreshLayout);
                    Uri downloadUri = task.getResult();
                    if (downloadUri == null) {
                        return;
                    }
                }
            }});
    }

    public static void editPurchase(int pos, Purchase p) {
        refreshLayout.setRefreshing(true);
        String purchaseKey = purchases.get(pos).getID();
        filterPurchases(currentFilter).set(pos, p);
        DatabaseReference purchaseTitleRef = mDB.getReference().child("purchase_titles").child(purchaseKey);
        purchaseTitleRef.child("title").setValue(p.getItemName());

        DatabaseReference purchaseCostRef = mDB.getReference().child("purchase_costs");
        purchaseCostRef.child(purchaseKey).child("cost").setValue(p.getCost()).toString();

        DatabaseReference purchaseSuppliersRef = mDB.getReference().child("purchase_suppliers");
        purchaseSuppliersRef.child(purchaseKey).child("suppliers").setValue(p.getSuppliers()).toString();

        DatabaseReference purchaseDateRef = mDB.getReference().child("purchase_dates");
        purchaseDateRef.child(purchaseKey).child("month").setValue(p.getDatePurchased().get(Calendar.MONTH));
        purchaseDateRef.child(purchaseKey).child("day").setValue(p.getDatePurchased().get(Calendar.DAY_OF_MONTH));
        purchaseDateRef.child(purchaseKey).child("year").setValue(p.getDatePurchased().get(Calendar.YEAR));

        DatabaseReference purchaseDescRef = mDB.getReference().child("purchase_desc");
        purchaseDescRef.child(purchaseKey).child("desc").setValue(p.getDescription());

        DatabaseReference purchasePurchaser = mDB.getReference().child("purchase_purchaser");
        purchasePurchaser.child(purchaseKey).child("name").setValue(p.getPurchaser());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        p.getImage().compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final StorageReference imageLocation = mStorageRef.child("purchase_images/" + purchaseKey + ".png");
        UploadTask uploadTask = imageLocation.putBytes(data);
        p.setImageUrl(imageLocation);

        spendingTracker.setText(getTotalSpending(spendingTracker));

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageLocation.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    getPurchases(refreshLayout);
                    Uri downloadUri = task.getResult();
                    if (downloadUri == null) {
                        return;
                    }
                }
            }});
    }

    public static ArrayList<Purchase> getPurchases(SwipeRefreshLayout rl) {
        refreshLayout = rl;
        mDB.getReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        purchases.clear();
                        ArrayList<ArrayList<Object>> info = new ArrayList<>();

                        HashSet<String> ids = new HashSet<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            info.add(new ArrayList<Object>());
                            HashMap<String, Object> data = (HashMap<String, Object>)snapshot.getValue();
                            ids.addAll(data.keySet());
                            for (Object o: data.values()) {
                                info.get(info.size() - 1).add(o);
                            }
                        }

                        if (info.size() == 0) {
                            refreshLayout.setRefreshing(false);
                            spendingTracker.setText("Total Spending - $0.00");
                            return;
                        }
                        for (int i = 0; i < info.get(0).size(); i++) {
                            Purchase p = new Purchase();
                            HashMap<String, Object> hashCost = (HashMap<String, Object>) info.get(0).get(i);

                            try {
                                p.setCost((double)(hashCost.get("cost")));
                            } catch (ClassCastException e) {
                                System.err.println(e);
                                p.setCost(new Double((long)(hashCost.get("cost"))));
                            }

                            HashMap<String, String> hashSuppliers = (HashMap<String, String>) info.get(4).get(i);
                            p.setSuppliers(hashSuppliers.get("suppliers"));

                            HashMap<String, String> hashDesc = (HashMap<String, String>) info.get(2).get(i);
                            p.setDescription(hashDesc.get("desc"));

                            HashMap<String, String> hashPurchaser = (HashMap<String, String>) info.get(3).get(i);
                            p.setPurchaser(hashPurchaser.get("name"));

                            HashMap<String, String> hashTitles = (HashMap<String, String>) info.get(5).get(i);
                            p.setItemName(hashTitles.get("title"));

                            HashMap<String, Long> hashDate = (HashMap<String, Long>) info.get(1).get(i);
                            int year = Math.toIntExact(hashDate.get("year"));
                            int month = Math.toIntExact(hashDate.get("month"));
                            int day = Math.toIntExact(hashDate.get("day"));
                            Calendar c = Calendar.getInstance();
                            c.set(year, month, day);
                            p.setDatePurchased(c);

                            p.setImageUrl(getReference((String)ids.toArray()[i]));

                            p.setID((String)ids.toArray()[i]);

                            purchases.add(p);
                        }

                        Collections.sort(purchases, new Comparator<Purchase>() {
                            public int compare(Purchase o1, Purchase o2) {
                                if (o1.getDatePurchased() == null || o2.getDatePurchased() == null)
                                    return 0;
                                return o1.getDatePurchased().compareTo(o2.getDatePurchased());
                            }
                        });

                        Collections.reverse(purchases);

                        listAdapter.setData(purchases);
                        refreshLayout.setRefreshing(false);
                        FirebaseUtils.filterPurchases(currentFilter);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        return purchases;
    }

    public static void setDisplayName(String name) {
        FirebaseUser user = mAuth.getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name).build();

        user.updateProfile(profileUpdates);
    }

    public static void setListAdapter(PurchasesListAdapter listAdapter) {
        FirebaseUtils.listAdapter = listAdapter;
    }

    public static PurchasesListAdapter getListAdapter() {
        return listAdapter;
    }

    public static void displayPurchase(Purchase p) {
        purchases.add(p);
        Collections.sort(purchases, new Comparator<Purchase>() {
            public int compare(Purchase o1, Purchase o2) {
                if (o1.getDatePurchased() == null || o2.getDatePurchased() == null)
                    return 0;
                return o1.getDatePurchased().compareTo(o2.getDatePurchased());
            }
        });

        Collections.reverse(purchases);
        listAdapter.setData(purchases);
    }

    public static StorageReference getReference(String id) {
        return mStorageRef.child("purchase_images/" + id + ".png");
    }

    public static ArrayList<Purchase> filterPurchases(String descFilter) {
        currentFilter = descFilter;
        ArrayList<Purchase> copyPurchases = new ArrayList<>();
        for (int i = 0; i < purchases.size(); i++) {
            copyPurchases.add(purchases.get(i));
        }
        int i = 0;
        while (i < copyPurchases.size()) {
            Purchase currentPurchase = copyPurchases.get(i);
            if (!currentPurchase.getDescription().toLowerCase().contains(currentFilter.trim().toLowerCase())) {
                copyPurchases.remove(i);
                i--;
            }
            i++;
        }
        double total = 0.0;
        for (Purchase p: copyPurchases) {
            total += p.getCost();
        }
        if (total == 0) {
            spendingTracker.setText("Total Spending - $0.00");
        } else {
            String money = formatDecimal(String.valueOf(total));
            while (money.substring(money.indexOf(".") + 1).length() < 2) {
                money = money + "0";
            }
            spendingTracker.setText("Total Spending - $" + money);
        }
        listAdapter.setData(copyPurchases);
        listAdapter.notifyDataSetChanged();
        return copyPurchases;
    }

    public static void deletePurchase(int position) {
        Purchase p = filterPurchases(currentFilter).remove(position);
        String id = p.getID();

        DatabaseReference purchaseTitleRef = mDB.getReference().child("purchase_titles").child(id);
        purchaseTitleRef.removeValue();

        DatabaseReference purchaseSuppliersRef = mDB.getReference().child("purchase_suppliers").child(id);
        purchaseSuppliersRef.removeValue();

        DatabaseReference purchaseCostRef = mDB.getReference().child("purchase_costs").child(id);
        purchaseCostRef.removeValue();

        DatabaseReference purchaseDateRef = mDB.getReference().child("purchase_dates").child(id);
        purchaseDateRef.removeValue();

        DatabaseReference purchaseDescRef = mDB.getReference().child("purchase_desc").child(id);
        purchaseDescRef.removeValue();

        DatabaseReference purchasePurchaser = mDB.getReference().child("purchase_purchaser").child(id);
        purchasePurchaser.removeValue();

        getPurchases(refreshLayout);
    }

    public static String getTotalSpending(TextView v) {
        spendingTracker = v;
        double total = 0.0;
        for (Purchase p: filterPurchases(currentFilter)) {
            total += p.getCost();
        }
        if (total == 0) {
            return "Total Spending - $0.00";
        } else {
            String money = formatDecimal(String.valueOf(total));
            while (money.substring(money.indexOf(".") + 1).length() < 2) {
                money = money + "0";
            }
            return "Total Spending - $" + money;
        }
    }

    private static String formatDecimal(String str) {
        BigDecimal parsed = new BigDecimal(str);
        DecimalFormat formatter = new DecimalFormat("#,###." + getDecimalPattern(str),
                new DecimalFormatSymbols(Locale.US));
        formatter.setRoundingMode(RoundingMode.DOWN);
        return formatter.format(parsed);
    }

    private static String getDecimalPattern(String str) {
        int decimalCount = str.length() - str.indexOf(".") - 1;
        StringBuilder decimalPattern = new StringBuilder();
        for (int i = 0; i < 2 - decimalCount; i++) {
            decimalPattern.append("0");
        }
        return decimalPattern.toString();
    }
}
