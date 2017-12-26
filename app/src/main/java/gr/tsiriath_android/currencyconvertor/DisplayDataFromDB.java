package gr.tsiriath_android.currencyconvertor;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tsiriath on 26/12/2017.
 */

public class DisplayDataFromDB {

    static void renderMainScreen(String[] strings, Activity parentActivity) {
        ArrayAdapter<String> currenciesListAdapter;
        ArrayList<ItemData> MySpnCurList;
        ListView currenciesListView;
        TextView newWelcomeMessage;
        Context myContext = parentActivity.getApplicationContext();

        if (strings != null) {
            //Update welcome message. Its the array's last element
            newWelcomeMessage = parentActivity.findViewById(R.id.welcomeMessage);
            String newMessage = "OFFLINE MODE. Data source is local DB. \n" + strings[strings.length - 1];
            newWelcomeMessage.setText(newMessage);
            //Update listview with new values. Welcome message not included
            strings = Arrays.copyOfRange(strings, 0, strings.length - 1);

            MainActivity.newMasterData(strings); //Update MasterData in MainActivity with new values

            currenciesListAdapter = new ArrayAdapter<>(
                    parentActivity,
                    R.layout.list_item_currencies,
                    R.id.list_item_currencies_textview,
                    Arrays.asList(strings));
            currenciesListView = parentActivity.findViewById(R.id.listview_currencies);
            currenciesListView.setAdapter(currenciesListAdapter);

            //Update spinners with new data from data form JSON string

            MySpnCurList = FetchCurrenciesTask.updSpnCurList(strings, myContext);  //Create parametrical spinner table
            Spinner spnCur1 = parentActivity.findViewById(R.id.spin_cur_1);   //Link spnCur1 variable with spin_cur_1
            Spinner spnCur2 = parentActivity.findViewById(R.id.spin_cur_2);   //Link spnCur1 variable with spin_cur_2
            Integer oldSp1 = spnCur1.getSelectedItemPosition();      // Save old spnCur1 selected item position
            Integer oldSp2 = spnCur2.getSelectedItemPosition();      // Save old spnCur2 selected item position
            SpinnerAdapter adapter = new SpinnerAdapter(parentActivity, MySpnCurList);
            spnCur1.setAdapter(adapter);    // Update spnCur1's Adapter
            spnCur2.setAdapter(adapter);    // Update spnCur2's Adapter
            spnCur1.setSelection(oldSp1);   // Restore sp1 itemPos
            spnCur2.setSelection(oldSp2);   // Restore sp2 itemPos
        }
    }

}
