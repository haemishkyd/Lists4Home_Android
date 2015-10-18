package com.sppp.lists4home;

import java.util.ArrayList;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    public Weather weather_data[] = null;
    ListView listView1;
    AutoCompleteTextView auto;
    Spinner autoSpinner;
    TextView currentTextView;
    Boolean refresh;
    Boolean headerAdded = false;

    View header;
    boolean UserDetailsCorrectOnLogin;
    public FormInfo returnInfoClass;
    WeatherAdapter mainListAdapter;
    private static final int STOPSPLASH = 0;
    private static final int SPLASHTIME = 3000;

    private Handler splashHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOPSPLASH:
                    int numUsers;

                    DatabaseHandler myCheck = new DatabaseHandler(MainActivity.this);
                    numUsers = myCheck.getNumberUsers();
                    refresh = false;
                    UserDetailsCorrectOnLogin = false;
                    if (numUsers == 0) {
                        setContentView(R.layout.login);
                    } else {
                        setContentView(R.layout.main);
                        headerAdded = false;
                        getItemsLocal();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Message msg = new Message();
        returnInfoClass = new FormInfo();
        msg.what = STOPSPLASH;
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);

        setContentView(R.layout.splash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DatabaseHandler myUpdateStatusDB = new DatabaseHandler(MainActivity.this);
        DatabaseMetrics local_metrics = new DatabaseMetrics();


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (myUpdateStatusDB.getUpdateStatus(local_metrics)) {
            menu.findItem(R.id.sync_button).setIcon(R.drawable.ic_action_refresh_new);
        } else {
            menu.findItem(R.id.sync_button).setIcon(R.drawable.ic_action_refresh);
        }
        return true;//return true so to menu pop up is opens
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String versionName = "";
        String DataString;
        DatabaseHandler mySubmitDB = new DatabaseHandler(MainActivity.this);
        DatabaseMetrics local_metrics = new DatabaseMetrics();

        switch (item.getItemId()) {
            case R.id.sync_button:
                startSync();
                return true;
            case R.id.item2:
                setContentView(R.layout.login);
                mySubmitDB.truncateTables();
                mySubmitDB.truncateUserTable();
                return true;
            case R.id.item3:
                setContentView(R.layout.about);
                try {
                    PackageInfo packageinfo = getPackageManager().getPackageInfo(
                            getPackageName(), 0);
                    versionName = packageinfo.versionName;
                } catch (NameNotFoundException e) {
                }
                TextView aboutdata = (TextView) findViewById(R.id.aboutTextView1);
                DataString = "Lists4Home\n\nVersion: "
                        + versionName
                        + "\nFor support contact: support@lists4home.com\nWebsite: www.lists4home.com\nDeveloped by: SPPP\nProudly South African";
                aboutdata.setText(DataString);
                break;
            case R.id.item4:
                setContentView(R.layout.about);
                try {
                    PackageInfo packageinfo = getPackageManager().getPackageInfo(
                            getPackageName(), 0);
                    versionName = packageinfo.versionName;
                } catch (NameNotFoundException e) {
                }
                mySubmitDB.getUpdateStatus(local_metrics);
                TextView detailsdata = (TextView) findViewById(R.id.aboutTextView1);
                DataString = "Local Database Data"
                        + "\n\rLocal Add DB: " + local_metrics.num_local_add_list
                        + "\n\rLocal Remove DB: " + local_metrics.num_local_remove_list
                        + "\n\rLocal New Items DB: " + local_metrics.num_local_new_items_list;
                detailsdata.setText(DataString);
                break;
        }
        return false;
    }

    /* Write toast from worker threads */
    public void writeToast(final String text_for_toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), text_for_toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getLoginInfo() {

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                EditText username = (EditText) findViewById(R.id.username);
                EditText password = (EditText) findViewById(R.id.password);

                returnInfoClass.stored_username = username.getText().toString();
                returnInfoClass.stored_password = password.getText().toString();

                synchronized(this)
                {
                    this.notify();
                }
            }
        };

        synchronized (myRunnable){
            runOnUiThread(myRunnable);

            try {
                myRunnable.wait();
            }catch (InterruptedException e){}
        }
    }

    public void getRegisterInfo() {

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                EditText username = (EditText) findViewById(R.id.registerusername);
                EditText password = (EditText) findViewById(R.id.registerpassword);
                EditText repeatpassword = (EditText) findViewById(R.id.repeatpassword);
                EditText registername = (EditText) findViewById(R.id.actualname);
                EditText registersurname = (EditText) findViewById(R.id.registersurname);
                EditText registeremail = (EditText) findViewById(R.id.registeremail);

                returnInfoClass.stored_username = username.getText().toString();
                returnInfoClass.stored_password = password.getText().toString();
                returnInfoClass.stored_repeatpassword = repeatpassword.getText().toString();
                returnInfoClass.stored_registername = registername.getText().toString();
                returnInfoClass.stored_registersurname = registersurname.getText().toString();
                returnInfoClass.stored_registeremail = registeremail.getText().toString();

                synchronized(this)
                {
                    this.notify();
                }
            }
        };

        synchronized (myRunnable){
            runOnUiThread(myRunnable);

            try {
                myRunnable.wait();
            }catch (InterruptedException e){}
        }
    }

    /* Button action listeners ====================================================*/
    /* Button to go to the add view */
    public void AddClick(View view) {
        setContentView(R.layout.add_item);
        addItemstoAutocomplete();
        addCategoriestoSpinner();
    }

    public void CompleteClick(View view) {
        setContentView(R.layout.main);
        headerAdded = false;
        getItemsLocal();
    }

    public void SubmitItemtoList(View view) {
        DatabaseHandler mySubmitDB = new DatabaseHandler(MainActivity.this);
        EditText localitem = (EditText) findViewById(R.id.autoCompleteTextView1);
        EditText comment_item = (EditText) findViewById(R.id.comment);
        List<String> myStoredItems = new ArrayList<String>();
        myStoredItems = mySubmitDB.getAllItems();
        autoSpinner = (Spinner) findViewById(R.id.spinner1);
        currentTextView = (TextView) findViewById(R.id.textView1);

		/* if the category selector is visible we need to now add the new item to the list */
        if (autoSpinner.getVisibility() == View.VISIBLE) {
            mySubmitDB.addLocalNewItem(localitem.getText().toString(), autoSpinner.getSelectedItem().toString());
        }
        /* If the item is already in the list of known items or the catergory selector has been made visible:
         * add to the local list. If not then make the category selector visible. */
        if ((myStoredItems.contains((String) localitem.getText().toString())) || (autoSpinner.getVisibility() == View.VISIBLE)) {
            mySubmitDB.addLocalItemToList(localitem.getText().toString(), comment_item.getText().toString());
            setContentView(R.layout.main);
            headerAdded = false;
            getItemsLocal();
        } else {
            autoSpinner.setVisibility(View.VISIBLE);
            currentTextView.setVisibility(View.VISIBLE);
        }
    }

    /* Cancel button on the add view */
    public void CancelSubmit(View view) {
        setContentView(R.layout.main);
        headerAdded = false;
        getItemsLocal();
    }

    public void PasswordReset(View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                new ResetUserPasswordTask().execute();
            }
        });
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder1.setMessage("Are you sure you want to reset your password.");
        AlertDialog alert1 = builder1.create();
        alert1.show();
    }

    /* Sync Button */
    public void startSync() {
        new RetreiveListTask().execute();
    }

    public void LoginSubmit(View view) {
        Button myButton = (Button) findViewById(R.id.LoginBut);
        myButton.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        DatabaseHandler mySubmitDB = new DatabaseHandler(MainActivity.this);
        new CheckUserTask().execute();
        submitLoginDetails();
        mySubmitDB.truncateTables();
    }

    public void SignUpSubmit(View view) {
        setContentView(R.layout.register);
    }

    public void RegisterSubmit(View view) {
        new CreateNewUserTask().execute();
    }
    /* ========================================================================== */

    private void submitLoginDetails() {
        DatabaseHandler mySubmitDB = new DatabaseHandler(MainActivity.this);

        EditText username = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);

        mySubmitDB.addLoginDetail(username.getText().toString(), password.getText().toString());
    }

    public void checkUpdateStatusAndAct() {
        DatabaseHandler myUpdateStatusDB = new DatabaseHandler(MainActivity.this);
        DatabaseMetrics local_metrics = new DatabaseMetrics();

        if (myUpdateStatusDB.getUpdateStatus(local_metrics)) {
            TextView myHeaderColourChange = (TextView) findViewById(R.id.txtHeader);
            TextView myHeader2ColourChange = (TextView) findViewById(R.id.txtHeader2);
            TableRow myRow = (TableRow) findViewById(R.id.tableRow1);
            myHeaderColourChange.setBackgroundColor(Color.RED);
            myHeader2ColourChange.setBackgroundColor(Color.RED);
            myRow.setBackgroundColor(Color.RED);
        } else {
            TextView myHeaderColourChange = (TextView) findViewById(R.id.txtHeader);
            TextView myHeader2ColourChange = (TextView) findViewById(R.id.txtHeader2);
            TableRow myRow = (TableRow) findViewById(R.id.tableRow1);
            myHeaderColourChange.setBackgroundColor(Color.BLUE);
            myHeader2ColourChange.setBackgroundColor(Color.BLUE);
            myRow.setBackgroundColor(Color.BLUE);
        }
        invalidateOptionsMenu();
    }

    private void addItemstoAutocomplete() {
        DatabaseHandler myFetchDB = new DatabaseHandler(MainActivity.this);
        List<String> myStoredItems = new ArrayList<String>();
        myStoredItems = myFetchDB.getAllItems();

        auto = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, myStoredItems);
        adp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        auto.setThreshold(2);
        auto.setAdapter(adp);
    }

    private void addCategoriestoSpinner() {
        DatabaseHandler myFetchDB = new DatabaseHandler(MainActivity.this);
        List<String> myStoredItems = new ArrayList<String>();
        myStoredItems = myFetchDB.getAllCats();

        autoSpinner = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myStoredItems);
        adp.setDropDownViewResource(android.R.layout.simple_list_item_1);
        autoSpinner.setAdapter(adp);
    }

    public void setUpListAdapter() {
        TextView username;
        UserData currentUserData;

        DatabaseHandler myFetchDB2 = new DatabaseHandler(MainActivity.this);
		/* Add the header to the list */
        listView1 = (ListView) findViewById(R.id.listView1);
        header = (View) getLayoutInflater().inflate(R.layout.listview_header_row, null);
        if (!headerAdded) {
            listView1.addHeaderView(header);
        }
        headerAdded = true;

        listView1.setAdapter(mainListAdapter);

        listView1.setOnItemClickListener(new OnItemClickListener() {
            DatabaseHandler myTickDB = new DatabaseHandler(MainActivity.this);

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = listView1.getAdapter().getItem(position).toString();
                String[] separated = item.split(":");
                myTickDB.tickListItem(separated[0]);
                getItemsLocal();
            	/*String item2 = "Long Press to Delete!";
                Toast.makeText(getBaseContext(), item2, Toast.LENGTH_LONG).show();*/
            }
        });

        listView1.setOnItemLongClickListener(new OnItemLongClickListener() {
            DatabaseHandler myRemoveDB = new DatabaseHandler(MainActivity.this);

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String item = listView1.getAdapter().getItem(position).toString();
                String[] separated = item.split(":");
                myRemoveDB.removeItemFromList(separated[0]);
                getItemsLocal();
                Toast.makeText(getBaseContext(), item + " Removed!", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        listView1.setBackgroundColor(0x464646);
        checkUpdateStatusAndAct();

        currentUserData = myFetchDB2.getUser();
        username = (TextView) findViewById(R.id.txtHeader2);
        username.setText(currentUserData.name);
    }

    /* Retrieve all the items that are on the list */
    public void getItemsLocal() {
        ArrayList<Weather> list = new ArrayList<Weather>();
        DatabaseHandler myFetchDB = new DatabaseHandler(MainActivity.this);

        List<ShowListItem> myStoredItems = new ArrayList<ShowListItem>();
        myStoredItems = myFetchDB.getAllListItems(); /* SELECT * FROM sl_list */
        ShowListItem myItems[];
        myItems = myStoredItems.toArray(new ShowListItem[myStoredItems.size()]);
					
		/* If there is something in the list */
        if (myStoredItems.size() > 0) {
            for (int i = 0; i < myStoredItems.size(); i++) {
                if (myItems[i].isTicked) {
                    list.add(new Weather(R.drawable.hand_basket_nb_bw, myItems[i].listText, myItems[i].isTicked, myItems[i].Category));
                } else {
                    list.add(new Weather(R.drawable.hand_basket_nb, myItems[i].listText, myItems[i].isTicked, myItems[i].Category));
                }
            }
        } else {
            list.add(new Weather(R.drawable.attention, "Add Items.....", false, ""));
            list.add(new Weather(R.drawable.attention, "or", false, ""));
            list.add(new Weather(R.drawable.attention, "Sync Online...", false, ""));
        }
        weather_data = list.toArray(new Weather[list.size()]);

        mainListAdapter = new WeatherAdapter(MainActivity.this, R.layout.listview_item_row, weather_data);
        mainListAdapter.notifyDataSetChanged();
        setUpListAdapter();

        removeFocusFromKeyboard();
    }

    public void removeFocusFromKeyboard() {
		/* Intended to remove keyboard but this doesn't seem to work. */
        listView1.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void UserCreationFeedback(boolean status) {
        if (status == true) {
            Toast.makeText(getBaseContext(), "User successfully created!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Username not available!", Toast.LENGTH_LONG).show();
        }
    }

    public void ResetPasswordFeedback(boolean status) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setCancelable(false);
        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        if (status == true) {
            builder1.setMessage("Password Successfully Reset. You will receive an email with your new password shortly.");
        } else {
            builder1.setMessage("Password Could Not Be Reset.");
        }
        AlertDialog alert1 = builder1.create();
        alert1.show();
    }

    public void UserDetailsCheckFeedback(boolean status) {
        UserDetailsCorrectOnLogin = status;
        if (status == false) {
            Toast.makeText(getBaseContext(), "User details incorrect!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.login);
        } else {
            setContentView(R.layout.main);
            headerAdded = false;
            getItemsLocal();
        }
    }

    class RetreiveListTask extends AsyncTask<String, String, String> {
        private String url_all_products = "http://www.lists4home.com/android_connect/get_all_products.php";
        private String url_all_items = "http://www.lists4home.com/android_connect/get_all_items.php";
        private String url_all_cats = "http://www.lists4home.com/android_connect/get_all_categories.php";
        private String url_add_items_to_list = "http://www.lists4home.com/android_connect/add_item_to_list.php";
        private String url_remove_items_from_list = "http://www.lists4home.com/android_connect/remove_item_from_list.php";
        private String url_add_items_to_db = "http://www.lists4home.com/android_connect/add_new_item_to_db.php";
        JSONParser jParser = new JSONParser();
        private ProgressDialog pDialog;
        JSONArray products = null;

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Syncing Data. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... urls) {
            List<NameValuePair> params1 = new ArrayList<NameValuePair>();
            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            List<NameValuePair> params3 = new ArrayList<NameValuePair>();
            List<NameValuePair> params4 = new ArrayList<NameValuePair>();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            DatabaseHandler myDB = new DatabaseHandler(MainActivity.this);
            JSONObject json;

            if (!connesso()) {
                writeToast("No Internet Connection!");
                return null;
            }

            myDB.truncateTables();
            UserData tempUser = myDB.getUser();


                /* 1. Add new items to the remote database ============================================ */
            params4.add(new BasicNameValuePair("name", tempUser.getName().toString()));
            params4.add(new BasicNameValuePair("pass", tempUser.getPass().toString()));
            List<NewDBItem> myNewDatabaseItems = new ArrayList<NewDBItem>();
            myNewDatabaseItems = myDB.getAllItemsToAddToRemoteDB();
            NewDBItem NewDBItemsArray[];
            NewDBItemsArray = myNewDatabaseItems.toArray(new NewDBItem[myNewDatabaseItems.size()]);
            params4.add(new BasicNameValuePair("numitems", Integer.toString(myNewDatabaseItems.size())));
            if (myNewDatabaseItems.size() > 0) {
                for (int i = 0; i < myNewDatabaseItems.size(); i++) {
                    String temp = NewDBItemsArray[i].Item;
                    params4.add(new BasicNameValuePair("item" + Integer.toString(i), temp));
                    temp = NewDBItemsArray[i].Category;
                    params4.add(new BasicNameValuePair("cat" + Integer.toString(i), temp));
                }

                json = jParser.makeHttpRequest(url_add_items_to_db, "GET", params4);
                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                    if (success == 1) {
                        myDB.truncateNewItemsToDB();
                    } else {
                        Toast.makeText(getBaseContext(), "Sync Failure! Please try again.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

                /*2. Add locally added items to the remote list ====================================== */
            params2.add(new BasicNameValuePair("name", tempUser.getName().toString()));
            params2.add(new BasicNameValuePair("pass", tempUser.getPass().toString()));
            List<ItemCommentPair> myLocallyAddedItems = new ArrayList<ItemCommentPair>();
            myLocallyAddedItems = myDB.getLocalAddedListItems();
            ItemCommentPair myLocalAddItems[];
            myLocalAddItems = myLocallyAddedItems.toArray(new ItemCommentPair[myLocallyAddedItems.size()]);
            params2.add(new BasicNameValuePair("numitems", Integer.toString(myLocallyAddedItems.size())));
            if (myLocallyAddedItems.size() > 0) {
                for (int i = 0; i < myLocallyAddedItems.size(); i++) {
                    String temp = myLocalAddItems[i].Item;
                    params2.add(new BasicNameValuePair("product" + Integer.toString(i), temp));
                    temp = myLocalAddItems[i].Comment;
                    params2.add(new BasicNameValuePair("comment" + Integer.toString(i), temp));
                }

                json = jParser.makeHttpRequest(url_add_items_to_list, "GET", params2);
                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                    if (success == 1) {
                        myDB.truncateAddRemoveTable(1);
                    } else {
                        Toast.makeText(getBaseContext(), "Sync Failure! Please try again.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

                /*3. Remove locally removed items from the remote list =============================== */
            params3.add(new BasicNameValuePair("name", tempUser.getName().toString()));
            params3.add(new BasicNameValuePair("pass", tempUser.getPass().toString()));
            List<String> myLocallyRemovedItems = new ArrayList<String>();
            myLocallyRemovedItems = myDB.getLocalRemovedListItems();
            String myLocalRemoveItems[];
            myLocalRemoveItems = myLocallyRemovedItems.toArray(new String[myLocallyRemovedItems.size()]);
            params3.add(new BasicNameValuePair("numitems", Integer.toString(myLocallyRemovedItems.size())));
            if (myLocallyRemovedItems.size() > 0) {
                for (int i = 0; i < myLocallyRemovedItems.size(); i++) {
                    String temp = myLocalRemoveItems[i];
                    params3.add(new BasicNameValuePair("product" + Integer.toString(i), temp));
                }

                json = jParser.makeHttpRequest(url_remove_items_from_list, "GET", params3);
                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                    if (success == 1) {
                        myDB.truncateAddRemoveTable(2);
                    } else {
                        Toast.makeText(getBaseContext(), "Sync Failure! Please try again.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            params1.add(new BasicNameValuePair("name", tempUser.getName().toString()));
            params1.add(new BasicNameValuePair("pass", tempUser.getPass().toString()));
                /*4. Get all of the items on the list ====================================== */
            json = jParser.makeHttpRequest(url_all_products, "GET", params1);
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");

                if (success == 1) {
                    products = json.getJSONArray("products");

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString("item");
                        String comment = c.getString("comment");
                        myDB.addListItem(id, comment);
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Sync Failure! Please try again.", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            params.add(new BasicNameValuePair("name", tempUser.getName().toString()));
            params.add(new BasicNameValuePair("pass", tempUser.getPass().toString()));
                /*5. Get all of the items available ====================================== */
            json = jParser.makeHttpRequest(url_all_items, "GET", params);
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");

                if (success == 1) {
                    products = json.getJSONArray("products");

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString("item");
                        String cat = c.getString("cat");

                        myDB.addItem(id, cat);
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Sync Failure! Please try again.", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

                /*6. Get all of the categories ====================================== */
            json = jParser.makeHttpRequest(url_all_cats, "GET", params);
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");

                if (success == 1) {
                    products = json.getJSONArray("products");

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString("cat");
                        String cat = c.getString("cat_id");

                        myDB.addCategory(id, cat);
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Sync Failure! Please try again.", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            headerAdded = false;
            setContentView(R.layout.main);
            getItemsLocal();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
					/* Nothing to do here. */
                }
            });

        }

    }

    class CreateNewUserTask extends AsyncTask<String, String, String> {
        boolean UserCreated;
        private ProgressDialog pDialog;
        JSONParser LocaljParser = new JSONParser();
        private String url_register_new_user = "http://www.lists4home.com/android_connect/register_new_user.php";

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Creating new user. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            UserCreated = false;
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject json;
            getRegisterInfo();

            List<NameValuePair> paramsusername = new ArrayList<NameValuePair>();

            if (!connesso()) {
                writeToast("No Internet Connection!");
                return null;
            }

            if (!returnInfoClass.stored_password.equals(returnInfoClass.stored_repeatpassword)) {
                writeToast("Passwords Don't Match!");
            } else if (returnInfoClass.stored_registeremail.contains("@") != true) {
                writeToast("Email Address Invalid!");
            }
            paramsusername.add(new BasicNameValuePair("registerusername", returnInfoClass.stored_username));
            paramsusername.add(new BasicNameValuePair("registerpassword", returnInfoClass.stored_password));
            paramsusername.add(new BasicNameValuePair("registerrepeatpassword", returnInfoClass.stored_repeatpassword));
            paramsusername.add(new BasicNameValuePair("registeractualname", returnInfoClass.stored_registername));
            paramsusername.add(new BasicNameValuePair("registeractualsurname", returnInfoClass.stored_registersurname));
            paramsusername.add(new BasicNameValuePair("registeremail", returnInfoClass.stored_registeremail));
            json = LocaljParser.makeHttpRequest(url_register_new_user, "GET", paramsusername);

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");
                String text = json.getString("message");

                if (success == 1) {
                    UserCreated = true;
                } else {
                    UserCreated = false;

                }
            } catch (JSONException e) {
                UserCreated = false;
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (UserCreated) {
                setContentView(R.layout.login);
            } else {
                setContentView(R.layout.register);
            }
            UserCreationFeedback(UserCreated);
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
					/* Nothing to do here. */
                }
            });

        }
    }

    class ResetUserPasswordTask extends AsyncTask<String, String, String> {
        boolean PasswordChanged;
        private ProgressDialog pDialog;
        JSONParser LocaljParser = new JSONParser();
        private String url_reset_user_password = "http://www.lists4home.com/android_connect/reset_password.php";

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Resetting Password. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            PasswordChanged = false;
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject json;
            getLoginInfo();
            List<NameValuePair> paramsusername = new ArrayList<NameValuePair>();

            if (!connesso()) {
                writeToast("No Internet Connection!");
                return null;
            }

            paramsusername.add(new BasicNameValuePair("name", returnInfoClass.stored_username));

            json = LocaljParser.makeHttpRequest(url_reset_user_password, "GET", paramsusername);

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");
                String text = json.getString("message");

                if (success == 1) {
                    PasswordChanged = true;
                } else {
                    PasswordChanged = false;
                }
            } catch (JSONException e) {
                PasswordChanged = false;
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (PasswordChanged) {
                setContentView(R.layout.login);
                ResetPasswordFeedback(true);
            } else {
                setContentView(R.layout.login);
                ResetPasswordFeedback(false);
            }
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
					/* Nothing to do here. */
                }
            });

        }
    }

    class CheckUserTask extends AsyncTask<String, String, String> {
        boolean userDetailsCorrect;
        private ProgressDialog pDialog;
        JSONParser LocaljParser = new JSONParser();
        private String url_check_user_detail = "http://www.lists4home.com/android_connect/check_user_details.php";

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Checking details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            userDetailsCorrect = false;
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            getLoginInfo();
            JSONObject json;
            List<NameValuePair> paramsusername = new ArrayList<NameValuePair>();

            if (!connesso()) {
                writeToast("No Internet Connection!");
                return null;
            }

            paramsusername.add(new BasicNameValuePair("name", returnInfoClass.stored_username));
            paramsusername.add(new BasicNameValuePair("pass", DatabaseHandler.md5(returnInfoClass.stored_password)));

            json = LocaljParser.makeHttpRequest(url_check_user_detail, "GET", paramsusername);

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");
                String text = json.getString("message");

                if (success == 1) {
                    userDetailsCorrect = true;
                } else {
                    userDetailsCorrect = false;

                }
            } catch (JSONException e) {
                userDetailsCorrect = false;
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (!userDetailsCorrect) {
                setContentView(R.layout.login);
            } else {
                setContentView(R.layout.register);
            }
            UserDetailsCheckFeedback(userDetailsCorrect);
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
					/* Nothing to do here. */
                }
            });

        }
    }

    class FormInfo {
        String stored_username;
        String stored_password;
        String stored_repeatpassword;
        String stored_registername;
        String stored_registersurname;
        String stored_registeremail;
    }

    public boolean connesso() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}