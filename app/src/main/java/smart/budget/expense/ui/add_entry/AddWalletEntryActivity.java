package smart.budget.expense.ui.add_entry;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import smart.budget.expense.activities.CircularRevealActivity;
import smart.budget.expense.exceptions.EmptyStringException;
import smart.budget.expense.exceptions.ZeroBalanceDifferenceException;
import smart.budget.expense.firebase.FirebaseElement;
import smart.budget.expense.firebase.FirebaseObserver;
import smart.budget.expense.firebase.viewmodel_factories.UserProfileViewModelFactory;
import smart.budget.expense.firebase.models.User;
import smart.budget.expense.util.CategoriesHelper;
import smart.budget.expense.models.Category;
import smart.budget.expense.util.CurrencyHelper;
import smart.budget.expense.R;
import smart.budget.expense.firebase.models.WalletEntry;

public class AddWalletEntryActivity extends CircularRevealActivity {

    private Spinner selectCategorySpinner;
    private TextInputEditText selectNameEditText;
    private TextInputEditText selectMobileEditText;
    private Calendar chosenDate;
    private TextInputEditText selectAmountEditText;
    private TextInputEditText selectVillageEditText;
    private TextInputEditText selectDescriptionEditText;
    private TextView chooseDayTextView;
    private TextView chooseTimeTextView;
    private Spinner selectTypeSpinner;
    private User user;
    private TextInputLayout selectAmountInputLayout;
    private TextInputLayout selectNameInputLayout;
    private TextInputLayout selectMobileInputLayout;
    private TextInputLayout selectVillageInputLayout;
    private TextInputLayout selectDescriptionInputLayout;

    public AddWalletEntryActivity() {
        super(R.layout.activity_add_wallet_entry, R.id.activity_contact_fab, R.id.root_layout, R.id.root_layout2);
    }

    @Override
    public void onInitialized(Bundle savedInstanceState) {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add wallet entry");

        selectCategorySpinner = findViewById(R.id.select_category_spinner);
        selectNameEditText = findViewById(R.id.select_name_edittext);
        selectMobileEditText = findViewById(R.id.select_mobile_edittext);
        selectVillageEditText = findViewById(R.id.select_village_edittext);
        selectDescriptionEditText = findViewById(R.id.select_description_edittext);
        selectNameInputLayout = findViewById(R.id.select_name_inputlayout);
        selectNameInputLayout = findViewById(R.id.select_name_inputlayout);
        selectTypeSpinner = findViewById(R.id.select_type_spinner);
        Button addEntryButton = findViewById(R.id.add_entry_button);
        chooseTimeTextView = findViewById(R.id.choose_time_textview);
        chooseDayTextView = findViewById(R.id.choose_day_textview);
        selectAmountEditText = findViewById(R.id.select_amount_edittext);
        selectAmountInputLayout = findViewById(R.id.select_amount_inputlayout);
        selectMobileInputLayout = findViewById(R.id.select_mobile_inputlayout);
        selectVillageInputLayout = findViewById(R.id.select_village_inputlayout);
        selectDescriptionInputLayout = findViewById(R.id.select_description_inputlayout);
        chosenDate = Calendar.getInstance();

        UserProfileViewModelFactory.getModel(getUid(), this).observe(this, new FirebaseObserver<FirebaseElement<User>>() {
            @Override
            public void onChanged(FirebaseElement<User> firebaseElement) {
                if (firebaseElement.hasNoError()) {
                    user = firebaseElement.getElement();
                    dateUpdated();
                }
            }
        });


        EntryTypesAdapter typeAdapter = new EntryTypesAdapter(this,
                R.layout.new_entry_type_spinner_row, Arrays.asList(
                new EntryTypeListViewModel("Expense", Color.parseColor("#ef5350"),
                        R.drawable.money_icon),
                new EntryTypeListViewModel("Income", Color.parseColor("#66bb6a"),
                        R.drawable.money_icon)));

        selectTypeSpinner.setAdapter(typeAdapter);

        updateDate();
        chooseDayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
        chooseTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime();
            }
        });


        addEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToWallet(((selectTypeSpinner.getSelectedItemPosition() * 2) - 1) *
                                    CurrencyHelper.convertAmountStringToLong(selectAmountEditText.getText().toString()),
                            chosenDate.getTime(),
                            ((Category) selectCategorySpinner.getSelectedItem()).getCategoryID(),
                            selectNameEditText.getText().toString(),
                            selectMobileEditText.getText().toString(),
                            selectVillageEditText.getText().toString(),
                            selectDescriptionEditText.getText().toString()
                    );
                } catch (EmptyStringException e) {
                    selectNameInputLayout.setError(e.getMessage());
                    selectMobileInputLayout.setError(e.getMessage());
                } catch (ZeroBalanceDifferenceException e) {
                    selectAmountInputLayout.setError(e.getMessage());
                }
            }
        });


    }

    private void dateUpdated() {
        if (user == null) return;

        final List<Category> categories = CategoriesHelper.getCategories(user);
        EntryCategoriesAdapter categoryAdapter = new EntryCategoriesAdapter(this,
                R.layout.new_entry_type_spinner_row, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectCategorySpinner.setAdapter(categoryAdapter);

        CurrencyHelper.setupAmountEditText(selectAmountEditText, user);

    }


    private void updateDate() {
        SimpleDateFormat dataFormatter = new SimpleDateFormat("yyyy-MM-dd");
        chooseDayTextView.setText(dataFormatter.format(chosenDate.getTime()));

        SimpleDateFormat dataFormatter2 = new SimpleDateFormat("HH:mm");
        chooseTimeTextView.setText(dataFormatter2.format(chosenDate.getTime()));
    }

    public void addToWallet(long balanceDifference, Date entryDate, String entryCategory, String entryName , String mobile , String village , String description) throws ZeroBalanceDifferenceException, EmptyStringException {
        if (balanceDifference == 0) {
            throw new ZeroBalanceDifferenceException("Balance difference should not be 0");
        }

        if (entryName == null || entryName.length() == 0) {
            throw new EmptyStringException("Entry name length should be > 0");
        }

        FirebaseDatabase.getInstance().getReference().child("wallet-entries").child(getUid())
                .child("default").push().setValue(new WalletEntry(entryCategory, entryName, entryDate.getTime(), balanceDifference, mobile , village , description));
        user.wallet.sum += balanceDifference;
        UserProfileViewModelFactory.saveModel(getUid(), user);
        finishWithAnimation();
    }

    private void pickTime() {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                chosenDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                chosenDate.set(Calendar.MINUTE, minute);
                updateDate();

            }
        }, chosenDate.get(Calendar.HOUR_OF_DAY), chosenDate.get(Calendar.MINUTE), true).show();
    }

    private void pickDate() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        chosenDate.set(year, monthOfYear, dayOfMonth);
                        updateDate();

                    }
                }, year, month, day).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }


}
