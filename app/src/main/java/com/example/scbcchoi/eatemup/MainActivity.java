package com.example.scbcchoi.eatemup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BadParcelableException;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.content.Intent;
import java.util.Calendar;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scbcchoi.eatemup.inventory.InventoryAdapter;
import com.example.scbcchoi.eatemup.inventory.InventoryListItem;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private Dialog addDialog;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallBack;
    private Dialog datePickerDialog;

    public static int pickID;

    static String channelIDStr = "whateverEatEmUo";
    String channel_name = "Eatemup";
    String channel_description = "notification channel for eatemup";

    // to test recylerView, should be removed later
    List<InventoryListItem> InventoryList;

    private void createNotificationChannel() {
        //calculae expiry date
//        BackgroundService bgs = new BackgroundService();
        BackgroundService.oneDayHasPassed(this);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelIDStr, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init back ground service
        createNotificationChannel();
        //Settings.backgroundInit(this);

        //init Lists Model
        ListsModel lm = new ListsModel(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.menu_icon);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.about:
                        menuItem.setChecked(true);
                        showAbout();
                        drawerLayout.closeDrawers();
                        return true;
                    case R.id.recipe:
                        menuItem.setChecked(true);
                        showRecipe();
                        drawerLayout.closeDrawers();
                        return true;
                    case R.id.settings:
                        menuItem.setChecked(true);
                        showSettings();
                        drawerLayout.closeDrawers();
                        return true;
                }
                return false;
            }
        });

        //set up recyclerView and floating action button
        recyclerView = findViewById(R.id.rv_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "add", Toast.LENGTH_SHORT).show();
                addDialog = new Dialog(view.getContext());
                addDialog.setContentView(R.layout.dialog_add);
                addDialog.show();
            }
        });

        InventoryList = lm.getInventoryList();

        //setup adapter for RecyclerView
        actionModeCallBack = new ActionModeCallBack();

        adapter = new InventoryAdapter(InventoryList);
        recyclerView.setAdapter(adapter);
        initAdapter();
    }

    public void enableActionMode(){
        if (actionMode == null) {
            actionMode = this.startActionMode(actionModeCallBack);
        }
    }

    public void initAdapter(){
        adapter.setOnClickListener(new InventoryAdapter.onClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(adapter.getSelectionCount() > 0){
                    actionMode.setTitle(String.valueOf(adapter.getSelectionCount()));
                }
                else{
                    actionMode.finish();
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                enableActionMode();
                actionMode.setTitle(String.valueOf(adapter.getSelectionCount()));
            }
        });
    }

    private class ActionModeCallBack implements ActionMode.Callback{

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                multiDelete();
                mode.finish();
                return true;
            } else if(id == R.id.action_add_cart){
                multiAddShopping();
                mode.finish();
                return true;
            }

            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.multi,menu);
            //mode.setTitle();
            //navigationView.setVisibility(View.GONE);
            //toolbar.setVisibility(View.GONE);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapter.clearSelection();
            //navigationView.setVisibility(View.VISIBLE);
            //toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void showRecipe() {
        Intent intent = new Intent(this, RecipeActivity.class);
        startActivity(intent);
    }
    public void showSettings() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void cameraClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        //finish();
    }

    public void shopClick(View view) {
        Intent intent = new Intent(this, ShoppingActivity.class);
        startActivity(intent);
        //finish();
    }



    //insert an item to InventoryList
    //assuming the List has been sorted
    private int insertItem(InventoryListItem item){
        int j = item.getDateInt();
        boolean flag = false;
        int pos = 0;
        for(int i=0; i<InventoryList.size(); i++) {
            if(InventoryList.get(i).getName().equals(item.getName())) {
                flag = true;
                pos = i;
                break;
            }
        }
        if(flag == true) {
            InventoryList.remove(pos);
        }
        for (int i = 0; i < InventoryList.size(); i++) {
            if (InventoryList.get(i).getDateInt() >= j) {
                InventoryList.add(i, item);
                return i;
            }
        }
        InventoryList.add(item);
        return InventoryList.size() - 1;
    }

    public void addCancel(View view){
        addDialog.dismiss();
    }

    //add an item to inventory list
    public void addInventory(View view){
        EditText nameText = addDialog.findViewById(R.id.dialog_name_add);
        EditText dateText = addDialog.findViewById(R.id.dialog_date_add);
        //Check for empty input string
        if(dateText.getText().toString().equals("") || nameText.getText().toString().equals("")) {
            Toast.makeText(view.getContext(), "Input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(dateText.getText().toString().length() >= 6){
            Toast.makeText(view.getContext(), "Invalid intput", Toast.LENGTH_SHORT).show();
            return;
        }

        int date = Integer.parseInt(dateText.getText().toString());
        String name = nameText.getText().toString();
        InventoryListItem item = new InventoryListItem(name, date);

        //store in shared preferences
        ListsModel lm = new ListsModel(this);
        lm.addToList("inventory", name, date);
        lm.addToList("common", name, date);

        int pos = insertItem(item);
        recyclerView.smoothScrollToPosition(pos);
        adapter.notifyDataSetChanged();
        addDialog.dismiss();
    }

    public void deleteInventory(View view){
        InventoryListItem item =  adapter.updateItem();
        ListsModel lm = new ListsModel(this);
        lm.removeFromList("inventory",InventoryList.get(adapter.getPos()).getName());
        InventoryList.remove(adapter.getPos());
        adapter.notifyDataSetChanged();
    }

    public void  updateInventory(View view){
        InventoryListItem item =  adapter.updateItem();
        //check for empty input
        if(item.getName().equals("")) {
            Toast.makeText(view.getContext(), "Input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ListsModel lm = new ListsModel(this);

        String itemToDelete = InventoryList.get(adapter.getPos()).getName();
        String alias = lm.getAlias(itemToDelete);
        if (alias != null){
            lm.addToList("alias", item.getName().toLowerCase(), alias);
        }

        lm.removeFromList("inventory", itemToDelete);
        InventoryList.remove(adapter.getPos());//index of item changed
        int pos = insertItem(item);
        lm.addToList("inventory", item.getName(), item.getDateInt());
        recyclerView.smoothScrollToPosition(pos);
        adapter.notifyDataSetChanged();

    }

    public void  multiDelete (){
        List<Integer>  selection = adapter.getSelection();
        ListsModel lm = new ListsModel(this);

        for(Integer i: selection){
            lm.removeFromList("inventory",InventoryList.get(i).getName());
        }

        InventoryList = lm.getInventoryList();
        adapter = new InventoryAdapter(InventoryList);
        recyclerView.setAdapter(adapter);
        initAdapter();
        adapter.notifyDataSetChanged();
    }

    public void multiAddShopping(){
        List<Integer>  selection = adapter.getSelection();
        ListsModel lm = new ListsModel(this);
        for(Integer i: selection){
            lm.addToList("shopping",InventoryList.get(i).getName(),"");
            //Is this correct?
        }

        InventoryList = lm.getInventoryList();
        adapter = new InventoryAdapter(InventoryList);
        recyclerView.setAdapter(adapter);
        initAdapter();
        adapter.notifyDataSetChanged();
    }

    public void noUpdate(View view){
        adapter.noUpdate();
    }

    public void clearAll(View v){
        ListsModel lm = new ListsModel(this);
        //lm.clearList("alias");
        lm.clearList("inventory");
        //lm.clearList("shopping");
        InventoryList = lm.getInventoryList();
        adapter = new InventoryAdapter(InventoryList);
        initAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }



    public void pickDate(View view){
        String tag = String.valueOf(view.getTag());
        System.out.println("view's tag is " + tag);
        if(tag.equals("addDatePick")) pickID = R.id.dialog_date_add;
        else if(tag.equals("inventoryDatePick")) pickID = R.id.dialog_date;
        else if(tag.equals("addScanPick")) pickID = R.id.rv_scandate;
        datePickerDialog = new Dialog(view.getContext());
        datePickerDialog.setContentView(R.layout.dialog_date_picker);
        datePickerDialog.show();
    }


    public void dateCancel(View view){
        datePickerDialog.dismiss();
    }

    private void dateSelectHelper(View view, EditText editText){
        DatePicker datePicker = datePickerDialog.findViewById(R.id.date_picker);
        Calendar tempCalendar = Calendar.getInstance();
        long millis1 = tempCalendar.getTimeInMillis();

        tempCalendar.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth(),0,0);

        long millis2 = tempCalendar.getTimeInMillis();

        editText.setText((String.valueOf(1+(millis2 - millis1)/(1000*3600*24))));
        datePickerDialog.dismiss();
    }

    public void dateSelect(View view){
        EditText editText;
        if(pickID == R.id.dialog_date) editText = adapter.inventoryDialog.findViewById(pickID);
        else if(pickID == R.id.dialog_date_add) editText = addDialog.findViewById(pickID);
        else editText = findViewById(pickID);
        dateSelectHelper(view, editText);
    }



}
