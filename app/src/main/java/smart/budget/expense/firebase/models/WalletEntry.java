package smart.budget.expense.firebase.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class WalletEntry {

    public String categoryID;
    public String name;
    public String mobile;
    public String village;
    public String description;
    public long timestamp;
    public long balanceDifference;
    public WalletEntry() {

    }

    public WalletEntry(String categoryID, String name, long timestamp, long balanceDifference, String mobile , String village , String description ) {
        this.categoryID = categoryID;
        this.name = name;
        this.mobile = mobile;
        this.village = village;
        this.description = description;
        this.timestamp = -timestamp;
        this.balanceDifference = balanceDifference;
    }

}