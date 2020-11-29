package smart.budget.expense.ui.main.history;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import smart.budget.expense.R;

public class WalletEntryHolder extends RecyclerView.ViewHolder {

    final TextView dateTextView;
    final TextView moneyTextView;
    final TextView categoryTextView;
    final TextView nameTextView;
    final ImageView iconImageView;
    public View view;

    public WalletEntryHolder(View itemView) {
        super(itemView);
        this.view = itemView;
        moneyTextView = itemView.findViewById(R.id.money_textview);
        categoryTextView = itemView.findViewById(R.id.category_textview);
        nameTextView = itemView.findViewById(R.id.name_textview);
        dateTextView = itemView.findViewById(R.id.date_textview);
        iconImageView = itemView.findViewById(R.id.icon_imageview);

    }
}
