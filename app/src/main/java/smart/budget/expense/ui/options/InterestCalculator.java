package smart.budget.expense.ui.options;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import smart.budget.expense.R;

public class InterestCalculator extends AppCompatActivity {

    EditText princy , interest , months ; // , intPercentage , totalwithInterest;
    TextView intPercentage , totalwithInterest;
    Button cal ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_calculator);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Interest Calculator");

        princy = (EditText)findViewById(R.id.principalAmount);
        interest = (EditText)findViewById(R.id.interestPercentage);
        intPercentage = (TextView)findViewById(R.id.onlyInterest);
        totalwithInterest = (TextView)findViewById(R.id.principalWithInterest);
        months = (EditText) findViewById(R.id.monthsRange);
        cal = (Button) findViewById(R.id.calculate);

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String princygetText , interestgetText , monthsgetText;

                princygetText = princy.getText().toString();
                interestgetText = interest.getText().toString();
                monthsgetText = months.getText().toString();

                Integer convertedP = Integer.parseInt(princygetText);
                Integer convertedI = Integer.parseInt(interestgetText);
                Integer convertedM = Integer.parseInt(monthsgetText);


                Integer interestcal = convertedP * convertedI / 100 * convertedM;
                Integer totalCal = interestcal + convertedP ;

                totalwithInterest.setText("Principle with Interest: "+ "\t" + totalCal);
                intPercentage.setText("Interest Amount: "+ "\t" + interestcal);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }
}