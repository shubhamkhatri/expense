package smart.budget.expense.ui.main.history;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;

import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import smart.budget.expense.R;
import smart.budget.expense.firebase.FirebaseElement;
import smart.budget.expense.firebase.FirebaseObserver;
import smart.budget.expense.firebase.ListDataSet;
import smart.budget.expense.firebase.viewmodel_factories.UserProfileViewModelFactory;
import smart.budget.expense.firebase.viewmodel_factories.WalletEntriesHistoryViewModelFactory;
import smart.budget.expense.firebase.models.User;
import smart.budget.expense.firebase.models.WalletEntry;
import smart.budget.expense.util.CategoriesHelper;
import smart.budget.expense.models.Category;
import smart.budget.expense.ui.main.history.edit_entry.EditWalletEntryActivity;
import smart.budget.expense.util.CurrencyHelper;

public class WalletEntriesRecyclerViewAdapter extends RecyclerView.Adapter<WalletEntryHolder> {

    private final String uid;
    private final FragmentActivity fragmentActivity;
    private ListDataSet<WalletEntry> walletEntries;

    private String city;
    private User user;
    private boolean firstUserSync = false;
    String listText="";


    public WalletEntriesRecyclerViewAdapter(FragmentActivity fragmentActivity, String uid, String city) {
        this.fragmentActivity = fragmentActivity;
        this.uid = uid;
        this.city = city;

        UserProfileViewModelFactory.getModel(uid, fragmentActivity).observe(fragmentActivity, new FirebaseObserver<FirebaseElement<User>>() {
            @Override
            public void onChanged(FirebaseElement<User> element) {
                if (!element.hasNoError()) return;
                WalletEntriesRecyclerViewAdapter.this.user = element.getElement();
                if (!firstUserSync) {
                    WalletEntriesHistoryViewModelFactory.getModel(uid, fragmentActivity).observe(fragmentActivity, new FirebaseObserver<FirebaseElement<ListDataSet<WalletEntry>>>() {
                        @Override
                        public void onChanged(FirebaseElement<ListDataSet<WalletEntry>> element) {
                            if (element.hasNoError()) {
                                walletEntries = element.getElement();
                                //checkCity();
                                generateText(walletEntries.getList());
                                element.getElement().notifyRecycler(WalletEntriesRecyclerViewAdapter.this);

                            }
                        }
                    });
                }
                notifyDataSetChanged();
                firstUserSync = true;
            }
        });

    }


    public String sendListText(){
        return listText;
    }

    private void generateText(List<WalletEntry> walletEntryList) {
        if(walletEntryList.size()>0) {
            WalletEntry w = walletEntryList.get((walletEntryList.size()) - 1);
            long s = w.balanceDifference;
            String state = "";
            if (s < 0)
                state = "withdraw";
            else
                state = "deposited";

            listText = listText + "Name: " + w.name
                    + "\nCity: " + w.village
                    + "\nAmount: " + CurrencyHelper.formatCurrency(user.currency, w.balanceDifference)
                    + "\nState: " + state
                    + "\n\n";

        }
    }


    private void checkCity() {
        if (!city.equals("default")) {
            int i = 0;
            for (WalletEntry w : walletEntries.getList()) {
                String s="City is : "+city
                        +"\n Village is : "+w.village;
                //TODO Extract the Complete list and compare to the walletEntries.village field and use the updated list in recycler view
                //System.out.println("city................." + city + "..............village is........" + w.village);
                if (!city.equals(w.village)) {
                    walletEntries.getList().remove(i);
                }
                i++;
            }
        }
    }

    @Override
    public WalletEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(fragmentActivity);
        View view = inflater.inflate(R.layout.history_listview_row, parent, false);
        return new WalletEntryHolder(view);
    }

    @Override
    public void onBindViewHolder(WalletEntryHolder holder, int position) {
        String id = walletEntries.getIDList().get(position);
        WalletEntry walletEntry = walletEntries.getList().get(position);
        Category category = CategoriesHelper.searchCategory(user, walletEntry.categoryID);
        holder.iconImageView.setImageResource(category.getIconResourceID());
        holder.iconImageView.setBackgroundTintList(ColorStateList.valueOf(category.getIconColor()));
        holder.categoryTextView.setText(category.getCategoryVisibleName(fragmentActivity));
        holder.nameTextView.setText(walletEntry.name);

        Date date = new Date(-walletEntry.timestamp);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        holder.dateTextView.setText(dateFormat.format(date));
        holder.moneyTextView.setText(CurrencyHelper.formatCurrency(user.currency, walletEntry.balanceDifference));
        holder.moneyTextView.setTextColor(ContextCompat.getColor(fragmentActivity,
                walletEntry.balanceDifference < 0 ? R.color.primary_text_expense : R.color.primary_text_income));

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createDeleteDialog(id, uid, walletEntry.balanceDifference, fragmentActivity);
                return false;
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fragmentActivity, EditWalletEntryActivity.class);
                intent.putExtra("wallet-entry-id", id);
                fragmentActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (walletEntries == null) return 0;
        return walletEntries.getList().size();
    }

    private void createDeleteDialog(String id, String uid, long balanceDifference, Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Do you want to delete?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("wallet-entries").child(uid).child("default").child(id).removeValue();
                        user.wallet.sum -= balanceDifference;
                        UserProfileViewModelFactory.saveModel(uid, user);
                        dialog.dismiss();
                    }

                })

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create().show();

    }

    public void setDateRange(Calendar calendarStart, Calendar calendarEnd) {
        WalletEntriesHistoryViewModelFactory.getModel(uid, fragmentActivity).setDateFilter(calendarStart, calendarEnd);
    }


}