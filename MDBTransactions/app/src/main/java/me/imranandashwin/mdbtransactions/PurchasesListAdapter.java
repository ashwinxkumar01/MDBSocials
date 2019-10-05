package me.imranandashwin.mdbtransactions;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class PurchasesListAdapter extends RecyclerView.Adapter<PurchasesListAdapter.ViewHolder> {

    private List<Purchase> purchasesList;

    private Context context;

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView purchaseName;
        TextView purchaserName;
        TextView purchaseCost;
        TextView purchaseDate;
        TextView purchaseSupplier;
        TextView purchaseDescription;
        ImageView purchasePhoto;

        ConstraintLayout purchaseCard;

        ViewHolder(View itemView) {
            super (itemView);

            purchaseName = itemView.findViewById(R.id.purchaseName);
            purchaserName = itemView.findViewById(R.id.purchaserName);
            purchaseCost = itemView.findViewById(R.id.purchaseCost);
            purchaseDate = itemView.findViewById(R.id.purchaseDate);
            purchaseSupplier = itemView.findViewById(R.id.rowSupplierName);
            purchasePhoto = itemView.findViewById(R.id.purchasePhoto);
            purchaseDescription = itemView.findViewById(R.id.purchaseDescription);

            purchaseCard = itemView.findViewById(R.id.purchaseCard);
        }
    }

    PurchasesListAdapter(List<Purchase> purchases, Context c) {
        this.purchasesList = purchases;
        this.context = c;
    }

    @Override
    public PurchasesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View purchasesListView = inflater.inflate(R.layout.purchase_row, parent,false);
        ViewHolder viewHolder = new ViewHolder(purchasesListView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PurchasesListAdapter.ViewHolder viewHolder, int position) {
        final Purchase purchase = this.purchasesList.get(position);

        viewHolder.purchaseName.setText(purchase.getItemName());
        viewHolder.purchaserName.setText(purchase.getPurchaser());
        viewHolder.purchaseCost.setText("$" + Utils.formatPrice(purchase.getCost()));
        viewHolder.purchaseDate.setText(Utils.formatDate((purchase.getDatePurchased().getTime())));
        viewHolder.purchaseDescription.setText(purchase.getDescription());
        viewHolder.purchaseSupplier.setText(purchase.getSuppliers());

        if (purchase.getImageUrl() != null) {
            purchase.getImageUrl().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageURL = uri.toString();
                    Glide.with(context.getApplicationContext())
                            .load(imageURL)
                            .placeholder(R.drawable.placeholder)
                            .override(viewHolder.purchasePhoto.getWidth(), viewHolder.purchasePhoto.getHeight())
                            .transform(new CenterCrop(), new RoundedCorners(32))
                            .into(viewHolder.purchasePhoto);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        } else {

            Glide.with(context.getApplicationContext())
                    .load(R.drawable.placeholder)
                    .override(viewHolder.purchasePhoto.getWidth(), viewHolder.purchasePhoto.getHeight())
                    .transform(new CenterCrop(), new RoundedCorners(32)).into(viewHolder.purchasePhoto);
        }
    }

    @Override
    public int getItemCount() {
        return this.purchasesList.size();
    }

    void setData(List<Purchase> purchases) {
        this.purchasesList = purchases;
        notifyDataSetChanged();
    }

    List<Purchase> getData() {
        return purchasesList;
    }

    // Clean all elements of the recycler
    void clear() {
        purchasesList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Purchase> list) {
        purchasesList.addAll(list);
        notifyDataSetChanged();
    }

    void removeItem(int position) {
        purchasesList.remove(position);
        notifyDataSetChanged();
        FirebaseUtils.deletePurchase(position);
    }
}
