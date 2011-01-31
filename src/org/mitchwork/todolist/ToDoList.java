package org.mitchwork.todolist;

import java.util.ArrayList;

import org.mitchwork.todolist.R;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class ToDoList extends Activity {
	
  static final private int ADD_NEW_TODO = Menu.FIRST;
  static final private int REMOVE_TODO = Menu.FIRST + 1;
  
  private boolean addingNew = false;
  private ArrayList<ToDoItem> todoItems;
  private ListView myListView;
  private EditText myEditText;
  private ToDoItemAdapter aa;
  
  private static final String TEXT_ENTRY_KEY = "TEXT_ENTRY_KEY";
  private static final String ADDING_ITEM_KEY = "ADDING_ITEM_KEY";
  private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";

  /** Called when the activity is first created. */
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate your view
    setContentView(R.layout.main);
      
    // Get references to UI widgets
    myListView = (ListView)findViewById(R.id.myListView);
    myEditText = (EditText)findViewById(R.id.myEditText);

    todoItems = new ArrayList<ToDoItem>();
    int resID = R.layout.todolist_item;
    aa = new ToDoItemAdapter(this, resID, todoItems);
    
    myListView.setAdapter(aa);
        
    myEditText.setOnKeyListener(new OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            ToDoItem newItem = new ToDoItem(myEditText.getText().toString());
            todoItems.add(0, newItem);
            myEditText.setText("");
            aa.notifyDataSetChanged();
            cancelAdd();
            return true; 
          }
        return false;
      }
    });
    
    registerForContextMenu(myListView);
    restoreUIState();
  }
  
  private void restoreUIState() {
	 // Get the activity preferences object.
	  SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
	  
	  // Read the UI state values, specifying default values.
	  String text = settings.getString(TEXT_ENTRY_KEY, "");
	  Boolean adding = settings.getBoolean(ADDING_ITEM_KEY, false);
	  
	  // Restore the UI to the prvious state.
	  if (adding) {
		  addNewItem();
		  myEditText.setText(text);
	  }
	
  }
  
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
	  savedInstanceState.putInt(SELECTED_INDEX_KEY, myListView.getSelectedItemPosition());
	  
	  super.onSaveInstanceState(savedInstanceState);
  }
  
  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
	  int pos = -1;
	  
	  if (savedInstanceState != null)
		  if (savedInstanceState.containsKey(SELECTED_INDEX_KEY))
			  pos = savedInstanceState.getInt(SELECTED_INDEX_KEY, -1);
	  
	  myListView.setSelection(pos);
  }

  @Override
  protected void onPause() {
	  super.onPause();
	  
	  // Get the activity preferences object.
	  SharedPreferences uiState = getPreferences(0);
	  // Get the preferences editor
	  SharedPreferences.Editor editor = uiState.edit();
	  
	  // Add the UI state preference values.
	  editor.putString(TEXT_ENTRY_KEY, myEditText.getText().toString());
	  editor.putBoolean(ADDING_ITEM_KEY, addingNew);
	  // Commit the preferences.
	  editor.commit();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Create and add new menu items.
    MenuItem itemAdd = menu.add(0, ADD_NEW_TODO, Menu.NONE,
                                R.string.add_new);
    MenuItem itemRem = menu.add(0, REMOVE_TODO, Menu.NONE,
                                R.string.remove);

    // Assign icons
    itemAdd.setIcon(R.drawable.add_new_item);
    itemRem.setIcon(R.drawable.remove_item);

    // Allocate shortcuts to each of them.
    itemAdd.setShortcut('0', 'a');
    itemRem.setShortcut('1', 'r');

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    int idx = myListView.getSelectedItemPosition();

    String removeTitle = getString(addingNew ? 
                                   R.string.cancel : R.string.remove);

    MenuItem removeItem = menu.findItem(REMOVE_TODO);
    removeItem.setTitle(removeTitle);
    removeItem.setVisible(addingNew || idx > -1);

    return true;
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, 
                                  View v, 
                                  ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    menu.setHeaderTitle("Selected To Do Item");
    menu.add(0, REMOVE_TODO, Menu.NONE, R.string.remove);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    int index = myListView.getSelectedItemPosition();

    switch (item.getItemId()) {
      case (REMOVE_TODO): {
        if (addingNew) {
          cancelAdd();
        } 
        else {
          removeItem(index);
        }
        return true;
      }
      case (ADD_NEW_TODO): { 
        addNewItem();
        return true; 
      }
    }

    return false;
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item) {  
    super.onContextItemSelected(item);
    switch (item.getItemId()) {
      case (REMOVE_TODO): {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = menuInfo.position;

        removeItem(index);
        return true;
      }
    }
    return false;
  }
  
  private void cancelAdd() {
    addingNew = false;
    myEditText.setVisibility(View.GONE);
  }

  private void addNewItem() {
    addingNew = true;
    myEditText.setVisibility(View.VISIBLE);
    myEditText.requestFocus(); 
  }

  private void removeItem(int _index) {
    todoItems.remove(_index);
    aa.notifyDataSetChanged();  
  }
}