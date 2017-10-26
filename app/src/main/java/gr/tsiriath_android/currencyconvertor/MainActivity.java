package gr.tsiriath_android.currencyconvertor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Button btnClear;
    private EditText edTxtCur1,edTxtCur2;
    private String[] sampleData = {
            "AUD - 1.5282",
            "BGN - 1.9558",
            "BRL - 3.8134",
            "CAD - 1.4963",
            "CHF - 1.169",
            "CNY - 7.8317",
            "CZK - 25.589",
            "DKK - 7.4429"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnClear = (Button) findViewById(R.id.btn_clear);   //σύνδεση του btnClear με το Button Clear
        btnClear.setOnClickListener(clearOnClickListener);  //κλήση δημιουργίας listener για το Button Clear

        List<String> dummyList = Arrays.asList(sampleData);

        ArrayAdapter<String> currenciesListAdapter =
                new ArrayAdapter<String>(
                        this,
                        R.layout.list_item_currencies,
                        R.id.list_item_currencies_textview,
                        dummyList);

        ListView currenciesListView = (ListView)findViewById(R.id.listview_currencies);
        currenciesListView.setAdapter(currenciesListAdapter);
    }

    private View.OnClickListener clearOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnClearClicked();  //κλήση της btnClearClicked
        }
    };

    private void btnClearClicked() {    // Υλοποίηση της btnClearClicked
        edTxtCur1=(EditText) findViewById(R.id.edt_cur_1);
        edTxtCur1.setText("");
        edTxtCur2=(EditText) findViewById(R.id.edt_cur_2);
        edTxtCur2.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

       // menu.getItem(0).getIcon();
       // return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.m20_options) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}