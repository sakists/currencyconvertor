package gr.tsiriath_android.currencyconvertor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences mySharedPreferences;
    private Context myContext;
    private ArrayAdapter<String> currenciesListAdapter;
    private Spinner spnCur1,spnCur2;
    private static String pref_baseCur;
    private static String[] masterData = {
            "EUR - 1.0000"};
    RatesDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button btnSwitch,btnCalc,btnClear;
        ArrayList<ItemData> spnCurList;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new RatesDbHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create Both spinners with images and text.
        spnCurList = fillspnCurList();  //Create parametrical spinner table
        spnCur1=(Spinner)findViewById(R.id.spin_cur_1);
        spnCur2=(Spinner)findViewById(R.id.spin_cur_2);
        SpinnerAdapter adapter=new SpinnerAdapter(this, spnCurList);
        spnCur1.setAdapter(adapter);
        spnCur2.setAdapter(adapter);

        btnSwitch = (Button) findViewById(R.id.btn_switch); //σύνδεση του btnSwitch με το Button Switch
        btnCalc = (Button) findViewById(R.id.btn_calc);     //σύνδεση του btnCalc με το Button Calc
        btnClear = (Button) findViewById(R.id.btn_clear);   //σύνδεση του btnClear με το Button Clear

        btnSwitch.setOnClickListener(switchOnClickListener);//κλήση δημιουργίας listener για το Button Switch
        btnCalc.setOnClickListener(calcOnClickListener);    //κλήση δημιουργίας listener για το Button Calc
        btnClear.setOnClickListener(clearOnClickListener);  //κλήση δημιουργίας listener για το Button Clear

        createMyArrayAdapter(this, masterData);
        setMyListViewAdapter();

        myContext = getApplicationContext();
        mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
        boolean updFlag = mySharedPreferences.getBoolean(getString(R.string.pref_autoUpdate_key),Boolean.valueOf(getString(R.string.pref_autoUpdate_def)));
        int countRec = db.numberOfRows();

        if (updFlag) {  // Check if update flag is TRUE
            // If network IS available show message and connect
            if (FetchCurrenciesTask.isNetworkAvailable(getApplicationContext())) {
                refreshData();
            }else { // If network IS NOT available show message
                String NoConnectionTXT = getString(R.string.No_connection);
                showToast(getApplicationContext(),NoConnectionTXT,Toast.LENGTH_LONG);
                changeWelcomeMessage(NoConnectionTXT,countRec);
                if (countRec>0) {   // Fetch data only if data exists.
                    DisplayData.renderMainScreen(readDbData(), this, "DB"); // Read data from DB and render main screen
                }
            }
        }else{
            String NoAutoUpdTXT = getString(R.string.No_Auto_update);
            showToast(myContext,NoAutoUpdTXT,Toast.LENGTH_LONG);
            changeWelcomeMessage(NoAutoUpdTXT + "\n" + getString(R.string.InternetCon_not_checked), countRec);
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean BCupdFlag = mySharedPreferences.getBoolean(getString(R.string.pref_BaseCurAutoUpdate_key),Boolean.valueOf(getString(R.string.pref_BaseCurAutoUpdate_key)));
        String newBaseCur = mySharedPreferences.getString(getString(R.string.pref_selectBase_key),getString(R.string.pref_selectBase_def));

        if (BCupdFlag) {    // Check if auto update on change base currency is true
            if (newBaseCur != pref_baseCur) {   // Compare current Currency with new base currency
                // If network IS available show message and connect
                if (FetchCurrenciesTask.isNetworkAvailable(getApplicationContext())) {
                    refreshData();
                } else { // If network IS NOT available show message
                    showToast(getApplicationContext(), getString(R.string.No_connection), Toast.LENGTH_LONG);
                }
            }else{
                showToast(getApplicationContext(), getString(R.string.No_Need_to_update), Toast.LENGTH_LONG);
            }
        }
    }

    private ArrayList<ItemData> fillspnCurList() {

        String[][] XMLCurTable;
        Integer curImg;

        ArrayList<ItemData> result =new ArrayList<>();
        XMLCurTable = (new LibCurrenciesXML(this)).getDetailXMLCurTable();   //Get full detail from XMLCurTable
        for(String[] rowXMLCurTable:XMLCurTable ){
            curImg = getResources().getIdentifier(rowXMLCurTable[1], "drawable", getPackageName());   // Convert image name to images's ID
            result.add(new ItemData(" - " + rowXMLCurTable[0]," - " + rowXMLCurTable[2], curImg));    // Create a new line for spinner

        }
        return result;
    }

    private void createMyArrayAdapter(Activity myActivity, String[] newMasterData){

        currenciesListAdapter = new ArrayAdapter<>(
                myActivity,
                R.layout.list_item_currencies,
                R.id.list_item_currencies_textview,
                Arrays.asList(newMasterData));
    }

    private void setMyListViewAdapter(){
        ListView currenciesListView;

        currenciesListView =  (ListView)findViewById(R.id.listview_currencies);
        currenciesListView.setAdapter(currenciesListAdapter);
        currenciesListView.setOnItemClickListener(listItemOnClickListener);
    }

    private final View.OnClickListener switchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnSwitchClicked();  //κλήση της btnClearClicked
        }
    };

    private final View.OnClickListener calcOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnCalcClicked();  //κλήση της btnCalcClicked
        }
    };

    private final View.OnClickListener clearOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnClearClicked();  //κλήση της btnClearClicked
        }
    };

    private final AdapterView.OnItemClickListener listItemOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?>  parent, View v, int position, long id) {
            listItemClicked(position);  //κλήση της listItemClicked
        }
    };

    private void listItemClicked(int position) {    // Υλοποίηση της listItemClicked

        Context context = getApplicationContext();
        String text = toastCalcCurrency(position);
        int duration = Toast.LENGTH_LONG;
        showToast(context, text, duration);
    }

    private void btnSwitchClicked() {    // Υλοποίηση της btnSwitchClicked

        spnCur1=(Spinner)findViewById(R.id.spin_cur_1);
        spnCur2=(Spinner)findViewById(R.id.spin_cur_2);
        Integer oldSp1 =spnCur1.getSelectedItemPosition();          // Save old sp1 item position
        spnCur1.setSelection(spnCur2.getSelectedItemPosition());    // Change sp1 itemPos with sp2 itemPos
        spnCur2.setSelection(oldSp1);                               // Change sp2 with old sp1 itemPos
    }

    private void btnCalcClicked() {    // Υλοποίηση της btnCalcClicked
        Double value;
        String cur1,cur2,resTxt;
        EditText edTxtCur1,edTxtCur2;

        edTxtCur1=(EditText) findViewById(R.id.edt_cur_1);
        if (!edTxtCur1.getText().toString().equals("")) {
            value = Double.valueOf(edTxtCur1.getText().toString());
            spnCur1=(Spinner)findViewById(R.id.spin_cur_1);     //Get 1st spinner object
            spnCur2=(Spinner)findViewById(R.id.spin_cur_2);     //Get 2bd spinner object
            TextView textView1 = spnCur1.getSelectedView().findViewById(R.id.spin_txt);     //Get TextView from 1st spinner object
            cur1 = textView1.getText().toString().substring(3);                             //Get string from TextView of 1st spinner
            TextView textView2 = spnCur2.getSelectedView().findViewById(R.id.spin_txt);     //Get TextView from 2nd spinner object
            cur2 = textView2.getText().toString().substring(3);                             //Get string from TextView of 2nd spinner
            resTxt = calcCurrency(cur1,cur2,value);
            edTxtCur2=(EditText) findViewById(R.id.edt_cur_2);
            edTxtCur2.setText(resTxt);
        }
    }

    private void btnClearClicked() {    // Υλοποίηση της btnClearClicked
        EditText edTxtCur1,edTxtCur2;

        edTxtCur1=(EditText) findViewById(R.id.edt_cur_1);
        edTxtCur1.setText("");
        edTxtCur2=(EditText) findViewById(R.id.edt_cur_2);
        edTxtCur2.setText("");
    }

    private Double findCurrency(String curToSearch){
        Double result = 0.00001;

        for(String rowMasterData: masterData){
            //Log.i("findCurrency-Calc", curToSearch + " = > " + rowMasterData);
            if (curToSearch.equals(rowMasterData.substring(0,3)))
                return Double.valueOf(rowMasterData.substring(6));
        }
        return result;
    }

    private String calcCurrency(String cur1, String cur2, Double intValue){
        Double conv1,conv2,result;
        Double calculation = 0.00000;

        conv1 = findCurrency(cur1);
        conv2 = findCurrency(cur2);
        result = calculation + (intValue*conv2)/conv1;  //calculate result

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);   //Get US number formatter (thousands separator)
        formatter.setMaximumFractionDigits(2);                          //Set 2 decimal digits
        return  formatter.format(result);                               //Apply format
    }

    private String toastCalcCurrency(int position){
        String cur1,cur2;

        EditText edTxtCur1=(EditText) findViewById(R.id.edt_cur_1);
        if (!edTxtCur1.getText().toString().equals("")) {
            Double value = Double.valueOf(edTxtCur1.getText().toString());
            spnCur1 = (Spinner) findViewById(R.id.spin_cur_1);     //Get 1st spinner object
            TextView textView1 = spnCur1.getSelectedView().findViewById(R.id.spin_txt);     //Get TextView from 1st spinner object
            cur1 = textView1.getText().toString().substring(3);                             //Get string from TextView of 1st spinner
            cur2 = masterData[position].substring(0, 3);                                    //Get 3char currency string from masterData
            if (masterData[position].substring(4, 5).equals("-")) {                         //Check for - in the 4th position
                return ( cur1 + " " + value.toString() + " => " + calcCurrency(cur1, cur2, value) + " " + cur2);
            }
        }
        return "Nothing to calculate.!!!";
    }

    public static void showToast(Context context, String text,int duration){

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public  void changeWelcomeMessage(String extraText, int countRec){

        TextView newWelcomeMessage;
        //Update welcome message.
        newWelcomeMessage= (TextView) findViewById(R.id.welcomeMessage);
        if (countRec==0){
            newWelcomeMessage.setText(extraText + "\n No records found on local DB.");
        }else{
            newWelcomeMessage.setText(extraText + "\n" + countRec + " records found on local DB.");
        }
    }

    public void refreshData(){
        showToast(myContext, getString(R.string.Try_to_update), Toast.LENGTH_LONG);
        pref_baseCur = mySharedPreferences.getString(getString(R.string.pref_selectBase_key),getString(R.string.pref_selectBase_def));
        FetchCurrenciesTask task = new FetchCurrenciesTask();
        task.execute(this);
    }

    public String[] readDbData(){
        Cursor cur = db.getLastData();
        cur.moveToNext();       //Read 1st record
        List<String> currenciesList = CurrenciesJsonParser.getCurrenciesFromJson(cur.getString(2), "DB");
        int strSize = currenciesList.size();    //Get size of list
        String[] tmp = new String[strSize];     //create string array with that size
        return currenciesList.toArray(tmp);
    }

    public static void newMasterData(String[] newMasterData){  //Insert new data from listMasterData

        masterData = new String[newMasterData.length];         //Clear  data from MasterData
        masterData = Arrays.copyOf(newMasterData, newMasterData.length);
    }

    public static String pref_baseCur(){
        return  pref_baseCur;
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

        if (id == R.id.m21_settings) {
            Intent intentSettings = new Intent(this, SettingsActivity.class);   // Create settings intent
            startActivity(intentSettings);                                      // Start Settings Activity
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.m22_refresh) {
            // If network IS available show message and connect
            if (FetchCurrenciesTask.isNetworkAvailable(getApplicationContext())) {
                refreshData();
            } else { // If network IS NOT available show message
                showToast(getApplicationContext(), getString(R.string.No_connection), Toast.LENGTH_LONG);
            }
            return true;
        }
        if (id == R.id.m23_fb_share_button) {

            // Sharing the content to facebook
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                Log.i("FB - Share content", "ShareDialog.canShow(ShareLinkContent.class)=TRUE");
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://github.com/sakists/CurrencyConvertor")).build();
                ShareDialog.show(this,content);  // Show facebook ShareDialog
            }
            return true;
        }
        if (id == R.id.m24_delAllDbData) {
            new AlertDialog.Builder(this)       // Show confirmation.DialogBOX
                    .setTitle("Confirmation Message")
                    .setMessage("All local database records will be deleted. Are you sure.?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(MainActivity.this, "Records have been deleted.", Toast.LENGTH_SHORT).show();
                            db.deleteAllData();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
