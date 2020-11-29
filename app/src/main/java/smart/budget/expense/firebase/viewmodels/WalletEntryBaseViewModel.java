package smart.budget.expense.firebase.viewmodels;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.annotation.Nullable;

import com.google.firebase.database.FirebaseDatabase;

import smart.budget.expense.firebase.FirebaseElement;
import smart.budget.expense.firebase.FirebaseObserver;
import smart.budget.expense.firebase.FirebaseQueryLiveDataElement;
import smart.budget.expense.firebase.models.WalletEntry;

public class WalletEntryBaseViewModel extends ViewModel {
    protected final FirebaseQueryLiveDataElement<WalletEntry> liveData;
    protected final String uid;

    public WalletEntryBaseViewModel(String uid, String walletEntryId) {
        this.uid=uid;
        liveData = new FirebaseQueryLiveDataElement<>(WalletEntry.class, FirebaseDatabase.getInstance().getReference()
                .child("wallet-entries").child(uid).child("default").child(walletEntryId));    }

    public void observe(LifecycleOwner owner, FirebaseObserver<FirebaseElement<WalletEntry>> observer) {
        if(liveData.getValue() != null) observer.onChanged(liveData.getValue());
        liveData.observe(owner, new Observer<FirebaseElement<WalletEntry>>() {
            @Override
            public void onChanged(@Nullable FirebaseElement<WalletEntry> element) {
                if(element != null) observer.onChanged(element);
            }
        });
    }

    public void removeObserver(Observer<FirebaseElement<WalletEntry>> observer) {
        liveData.removeObserver(observer);
    }


}
