package com.cr4sh.nhlanucher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private MainUtils mainUtils;
    private MyPreferences myPreferences;
    public String buttonCategory;
    public String buttonName;
    public String buttonDescription;

    public String buttonCmd;
    public int buttonUsage;

    public static boolean disableMenu = false;

    public SQLiteDatabase mDatabase;

    // Access methods from DialogUtils
    DialogUtils dialogUtils = new DialogUtils(this.getSupportFragmentManager());

    private RecyclerView recyclerView;
    public static List<Item> itemList;

    @SuppressLint("Recycle")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHandler mDbHandler = DBHandler.getInstance(this);
        mDatabase = mDbHandler.getDatabase();
        myPreferences = new MyPreferences(this);

        itemList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        // Set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the adapter
        MyAdapter adapter = new MyAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // Get functions from this class
//        new MainUtils(this);
        mainUtils = new MainUtils(this);

        // Setup colors and settings

        mainUtils.changeLanguage(myPreferences.languageLocale());

        // Get the dialog and set it to not be cancelable
        this.setFinishOnTouchOutside(false);

        // Check for nethunter and terminal apps
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.offsec.nethunter", PackageManager.GET_ACTIVITIES);
            pm.getPackageInfo("com.offsec.nhterm", PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            dialogUtils.openAppsDialog();
            return;
        }

        // Set content view before dialogs below, so they wont appear twice!!
        // Check if setup has been completed

        if (!myPreferences.isSetupCompleted()) {
            dialogUtils.openFirstSetupDialog();
        }

        // Check for permissions
        if (!PermissionUtils.isPermissionsGranted(this)) {
            dialogUtils.openPermissionsDialog();
        }

        // Setting up spinner
        Spinner spinner = findViewById(R.id.categoriesSpinner);
        mainUtils.restartSpinner();

        // Check if there is any favourite tool in db, and open Favourite Tools category by default
        int isFavourite = 0;

            Cursor cursor = mDatabase.rawQuery("SELECT Count(FAVOURITE) FROM TOOLS WHERE FAVOURITE=1;", null);
            if (cursor.moveToFirst()) {
                isFavourite = cursor.getInt(0);
            }
            // Close connection
            cursor.close();

            // Set category, so code below can run
            if (isFavourite == 0) {
                spinner.setSelection(1);
            } else {
                spinner.setSelection(0);
            }

        // Add onclick listener for toolbar
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        mainUtils.refreshFrame();

        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                return false;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onQueryTextChange(String newText) {
                itemList.clear();

                // Limit text input to 25 characters
                InputFilter[] filters = new InputFilter[] { new InputFilter.LengthFilter(25) };
                EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                searchEditText.setFilters(filters);

                mainUtils.deleteButtons();

                TextView noToolsText = findViewById(R.id.messagebox);
                    Cursor cursor;

                        String[] projection = {"CATEGORY", "NAME", myPreferences.language(), "CMD", "ICON", "USAGE"};

                        // Add search filter to query
                        String selection = "NAME LIKE ?";

                        String[] selectionArgs = {newText + "%"};

                        if(newText.length() > 0){

                            recyclerView.setVisibility(View.VISIBLE);

//                            menuItem.setEnabled(false);
                            disableMenu = true;

                            cursor = mDatabase.query("TOOLS", projection, selection, selectionArgs, null, null, "NAME ASC", "15");

                            // Run OnUiThread to edit layout!
                            if (cursor.getCount() == 0) {
                                runOnUiThread(() -> noToolsText.setText(getResources().getString(R.string.cant_found) + newText + "\n" + getResources().getString(R.string.check_your_query)));
                            }

                            while (cursor.moveToNext()) {
                                noToolsText.setText(null);
                                String toolCategory = cursor.getString(0);
                                String toolName = cursor.getString(1);
                                String toolDescription = cursor.getString(2);
                                String toolCmd = cursor.getString(3);
                                String toolIcon = cursor.getString(4);
                                int toolUsage = cursor.getInt(5);

                                Item item = new Item(toolCategory, toolName, toolDescription, toolCmd, toolIcon, toolUsage);
                                // Add the item to the itemList
                                itemList.add(item);
                            }
                            cursor.close();
                            return true;
                        } else {
                            runOnUiThread(() -> {
                                disableMenu = false;
                                noToolsText.setText(getResources().getString(R.string.no_newtext_entry));
                                // Clear the list of items
                                recyclerView.setVisibility(View.GONE);

                            });
                            return false;
                        }
            }
        });

        searchView.setOnCloseListener(() -> {
            if (!searchView.getQuery().toString().isEmpty()) {
                return false;
            }
            spinner.setEnabled(true);
            toolbar.setEnabled(true);
            spinner.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            mainUtils.restartSpinner();
            return false;
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            // If the search view has focus, disable and hide the spinner
            if (hasFocus) {
                spinner.setEnabled(false);
                toolbar.setEnabled(false);
                spinner.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
            }
        });

    }

    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (requestCode == 101) {
                boolean readExt = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!readExt) {
                    mainUtils.takePermissions();
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String text = adapterView.getItemAtPosition(position).toString();
        mainUtils.spinnerChanger(text);
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Set the color of the menu title
        SpannableString s = new SpannableString(getResources().getString(R.string.choose_option));
        s.setSpan(new ForegroundColorSpan(Color.parseColor(myPreferences.nameColor())), 0, s.length(), 0);
        Objects.requireNonNull(menu.setHeaderTitle(s));
        getMenuInflater().inflate(R.menu.options_menu, menu);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.option_1:
                dialogUtils.openEditableDialog(buttonName, buttonCmd);
                return true;
            case R.id.option_2:
                mainUtils.addFavourite();
                return true;
            case R.id.option_3:
                if (!disableMenu){
                    dialogUtils.openNewToolDialog(buttonCategory);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.get_out), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.option_4:
                dialogUtils.openDeleteToolDialog(buttonName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_1:
                dialogUtils.openSettingsDialog();
                return true;
            case R.id.menu_item_2:
                closeOptionsMenu();
                dialogUtils.openCustomThemesDialog();
                return true;
            case R.id.menu_item_3:
                dialogUtils.openStatisticsDialog();
                return true;
            case R.id.menu_item_4:
                // Just open link in browser
                String url = "https://github.com/cr4sh-me/NHLauncher";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

