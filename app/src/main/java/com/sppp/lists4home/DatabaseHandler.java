package com.sppp.lists4home;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.*;
import android.database.Cursor;
import android.widget.Toast;

public class DatabaseHandler extends SQLiteOpenHelper 
{	
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "lists4home";
 
    // Contacts table name
    private static final String SHOPPING_LIST = "sl_list";
    private static final String SHOPPING_ITEMS = "sl_items";
    private static final String SHOPPING_CATS = "sl_categories";
    private static final String SHOPPING_USER = "sl_local_user";
    private static final String SHOPPING_L_ADD_LIST = "sl_local_add_list";
    private static final String SHOPPING_L_REMOVE_LIST = "sl_local_remove_list";
    private static final String SHOPPING_L_NEW_ITEMS_LIST = "sl_local_new_items_list";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String ITEM_DESC = "item_desc";
    private static final String ITEM_COMM = "item_comment";
    private static final String CAT_DESC = "cat_desc";
    private static final String CAT_ID = "cat_id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String IS_TICKED = "is_ticked";
    
    UserData appUser;
    private static long databaseReturn;
    private static Context superContext;
    
	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		superContext = context; 
	}
	
	// Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        String CREATE_LIST_TABLE = "CREATE TABLE " + SHOPPING_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + ITEM_DESC + " TEXT," + ITEM_COMM + " TEXT," + IS_TICKED +" TEXT" + ")";
        db.execSQL(CREATE_LIST_TABLE);
        
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + SHOPPING_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + ITEM_DESC + " TEXT," + CAT_ID + " TEXT)";
        db.execSQL(CREATE_ITEMS_TABLE);
        
        String CREATE_CAT_TABLE = "CREATE TABLE " + SHOPPING_CATS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + CAT_DESC + " TEXT," + CAT_ID + " TEXT)";
        db.execSQL(CREATE_CAT_TABLE);
        
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + SHOPPING_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + USERNAME + " TEXT," + PASSWORD + " TEXT)";
        db.execSQL(CREATE_LOGIN_TABLE);
        
        String CREATE_L_ADD_LIST_TABLE = "CREATE TABLE " + SHOPPING_L_ADD_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + ITEM_DESC + " TEXT," + ITEM_COMM + " TEXT" + ")";
        db.execSQL(CREATE_L_ADD_LIST_TABLE);
        
        String CREATE_L_REMOVE_LIST_TABLE = "CREATE TABLE " + SHOPPING_L_REMOVE_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + ITEM_DESC + " TEXT" + ")";
        db.execSQL(CREATE_L_REMOVE_LIST_TABLE);
        
        String CREATE_L_NEW_ITEMS_LIST_TABLE = "CREATE TABLE " + SHOPPING_L_NEW_ITEMS_LIST+ "("
        		+ KEY_ID + " INTEGER PRIMARY KEY," + ITEM_DESC + " TEXT," + CAT_DESC + " TEXT" + ")";
        db.execSQL(CREATE_L_NEW_ITEMS_LIST_TABLE);
    }
    
	// Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_LIST);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_ITEMS);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_CATS);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_USER);
        
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_L_ADD_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_L_REMOVE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_L_NEW_ITEMS_LIST);
        
        // Create tables again
        onCreate(db);
    }
    
    // Downgrading database
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_LIST);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_ITEMS);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_CATS);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_USER);
        
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_L_ADD_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_L_REMOVE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_L_NEW_ITEMS_LIST);
        
        // Create tables again
        onCreate(db);
    }
    
    /* INSERT METHODS **************************************************************/
    public void addListItem(String item,String comment) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(ITEM_DESC, item); // Contact Name
        values.put(ITEM_COMM, comment);
             
        // Inserting Row
        databaseReturn = db.insert(SHOPPING_LIST, null, values);
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }
    
    public void addItem(String item,String cat) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(ITEM_DESC, item); // Contact Name
        values.put(CAT_ID, cat);
        
        // Inserting Row
        databaseReturn = db.insert(SHOPPING_ITEMS, null, values);
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }
    
    public void addLocalNewItem(String item,String cat) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(ITEM_DESC, item); // Contact Name
        values.put(CAT_DESC, cat);
        
        /* Check if the item already exists */
        String selectQuery = "SELECT * FROM " + SHOPPING_ITEMS+" WHERE item_desc='"+item+"'";        
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) 
        {
        	// Inserting Row only if there does not exist a name like this in the database
            databaseReturn = db.insert(SHOPPING_L_NEW_ITEMS_LIST, null, values);
        }
        else
        {
        	Toast.makeText(superContext, "Item already existed in the database. Adding to the list.", Toast.LENGTH_LONG).show();
        }
        
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }
    
    public void addCategory(String item,String cat) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(CAT_DESC, item); // Contact Name
        values.put(CAT_ID, cat);
        
        // Inserting Row
        databaseReturn = db.insert(SHOPPING_CATS, null, values);
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }
    
    public void addLoginDetail(String username,String password) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
        
        String s = md5(password);
        ContentValues values = new ContentValues();
        values.put(USERNAME, username); // Contact Name
        values.put(PASSWORD, s);
        
        // Inserting Row
        databaseReturn = db.insert(SHOPPING_USER, null, values);
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }
    
    public void addLocalItemToList(String localitem,String comment) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(ITEM_DESC, localitem); // Contact Name
        values.put(ITEM_COMM, comment);
        
        // Inserting Row
        databaseReturn = db.insert(SHOPPING_LIST, null, values);
        databaseReturn = db.insert(SHOPPING_L_ADD_LIST, null, values);
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }    
    
    /* TICK ITEM **************************************************************/
    public void tickListItem(String listitem)
    {
    	String item_ticked="";
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();    	
    	
    	String selectQuery = "SELECT is_ticked FROM " + SHOPPING_LIST +" WHERE item_desc='"+listitem+"'";
        
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
        	item_ticked=cursor.getString(0);
        }
    	
        if (item_ticked == null)
        {
        	values.put(IS_TICKED, "yes");
        }
        else if (item_ticked.contains("yes"))
        {
        	values.put(IS_TICKED, "");        	
        }
        else
        {
        	values.put(IS_TICKED, "yes");
        }
        
        db.update(SHOPPING_LIST, values, ITEM_DESC + "=?", new String[]{listitem});
    }
    
    /* REMOVE METHODS *********************************************************/
    public void removeItemFromList(String listitem)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	
    	values.put(ITEM_DESC, listitem);
        db.delete(SHOPPING_LIST, ITEM_DESC + "=?", new String[]{listitem});
        databaseReturn = db.insert(SHOPPING_L_REMOVE_LIST, null, values);
        if (databaseReturn == -1)
        {
        	Toast.makeText(superContext, "Could not insert item into database.", Toast.LENGTH_LONG).show();
        }
        db.close(); // Closing database connection
    }
    
    /* TRUNCATE METHODS *******************************************************/
    public void truncateTables() 
    {
        SQLiteDatabase db = this.getWritableDatabase();
            
        db.delete(SHOPPING_LIST, null, null);        
        db.delete(SHOPPING_ITEMS, null, null);
        db.delete(SHOPPING_CATS, null, null);
        db.close(); // Closing database connection
    }
    
    public void truncateUserTable() 
    {
        SQLiteDatabase db = this.getWritableDatabase();
            
        db.delete(SHOPPING_USER, null, null);        
        db.close(); // Closing database connection
    }
    
    public void truncateAddRemoveTable(int which) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
            
        if (which == 1)
        {
        	db.delete(SHOPPING_L_ADD_LIST, null, null);
        }
        if (which == 2)
        {
        	db.delete(SHOPPING_L_REMOVE_LIST, null, null);
        }        
        db.close(); // Closing database connection
    }
    
    public void truncateNewItemsToDB()
    {
    	SQLiteDatabase db = this.getWritableDatabase();
        
        db.delete(SHOPPING_L_NEW_ITEMS_LIST, null, null);
        db.close(); // Closing database connection
    }
    
    /* COUNTING METHODS *********************************************************/
    public int getNumberUsers()
    {
    	Integer myInt;
    	myInt = 0;
    	String selectQuery = "SELECT  count(*) FROM " + SHOPPING_USER;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) 
        {        	
        	myInt = Integer.parseInt(cursor.getString(0));
	        // return contact list	        
        }
        db.close(); // Closing database connection
        return (int)myInt;
    }
    
    public boolean getUpdateStatus(DatabaseMetrics populateMetrics)
    {
    	boolean return_status;
    	Integer myInt;
    	myInt = 0;
    	
    	return_status = false;
    	
    	String selectQuery = "SELECT  count(*) FROM " + SHOPPING_L_ADD_LIST;        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) 
        {        	
        	myInt = Integer.parseInt(cursor.getString(0));
        	populateMetrics.num_local_add_list = myInt;
        	if (myInt != 0)
        		return_status = true;
        }
        
        selectQuery = "SELECT  count(*) FROM " + SHOPPING_L_REMOVE_LIST;                
        cursor = db.rawQuery(selectQuery, null);        
        if (cursor.moveToFirst()) 
        {        	
        	myInt = Integer.parseInt(cursor.getString(0));
        	populateMetrics.num_local_remove_list = myInt;
        	if (myInt != 0)
        		return_status = true;
        }
        
        selectQuery = "SELECT  count(*) FROM " + SHOPPING_L_NEW_ITEMS_LIST;                
        cursor = db.rawQuery(selectQuery, null);        
        if (cursor.moveToFirst()) 
        {        	
        	myInt = Integer.parseInt(cursor.getString(0));
        	populateMetrics.num_local_new_items_list = myInt;
        	if (myInt != 0)
        		return_status = true;
        }
        db.close(); // Closing database connection
        return return_status;
    }
    
    /* RETRIEVE METHODS *********************************************************/
    public List<ShowListItem> getAllListItems() 
    {
    	String item_for_list;
    	String comment_for_list;
    	String isItemTicked;
    	String cat_for_item;
    	
        List<ShowListItem> contactList = new ArrayList<ShowListItem>();
        // Select All Query
        String selectQuery = "SELECT list.id,list.item_desc,list.item_comment,list.is_ticked,items.cat_id FROM " + SHOPPING_LIST+ " AS list INNER JOIN "+SHOPPING_ITEMS+" AS items ON list.item_desc = items.item_desc ORDER BY items.cat_id";
        
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
            do 
            {  
            	ShowListItem itemToReturn = new ShowListItem();
                // Adding contact to list
            	item_for_list=cursor.getString(1);
            	if (cursor.getColumnCount() > 2)
            	{
            		comment_for_list = cursor.getString(2);
            		isItemTicked = cursor.getString(3);
            		itemToReturn.Category = cursor.getString(4);
            		if (isItemTicked == null)
            		{
            			itemToReturn.isTicked = false;
            		}
            		else if (isItemTicked.contains("yes"))
            		{
            			itemToReturn.isTicked = true;
            		}
            		else
            		{
            			itemToReturn.isTicked = false;
            		}
            		if (comment_for_list.length() > 0)
            		{
            			itemToReturn.listText = item_for_list+":  "+comment_for_list;
            			itemToReturn.Comment = comment_for_list;            			
            		}
            		else
            		{
            			itemToReturn.listText = item_for_list;
            		}
            	}
            	else
            	{
            		comment_for_list = "";
            		itemToReturn.listText = item_for_list;
            	}
            	contactList.add(itemToReturn);
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        // return contact list
        return contactList;
    }
    
    public List<String> getAllItems() 
    {
        List<String> itemsFull = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT item_desc FROM " + SHOPPING_ITEMS;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
            do 
            {                
                // Adding contact to list
            	itemsFull.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        // return contact list
        return itemsFull;
    }
    
    public List<NewDBItem> getAllItemsToAddToRemoteDB() 
    {
        List<NewDBItem> itemsFull = new ArrayList<NewDBItem>();
        // Select All Query
        String selectQuery = "SELECT item_desc,cat_desc FROM " + SHOPPING_L_NEW_ITEMS_LIST;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
            do 
            {
            	NewDBItem temp = new NewDBItem();
            	temp.Item = cursor.getString(0);
            	temp.Category = cursor.getString(1);
                // Adding contact to list
            	itemsFull.add(temp);
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        // return contact list
        return itemsFull;
    }
    
    public List<String> getAllCats() 
    {
        List<String> itemsFull = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT cat_desc FROM " + SHOPPING_CATS;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
            do 
            {                
                // Adding contact to list
            	itemsFull.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        // return contact list
        return itemsFull;
    }
    
    public List<ItemCommentPair> getLocalAddedListItems() 
    {
        List<ItemCommentPair> local_items = new ArrayList<ItemCommentPair>();
        // Select All Query
        String selectQuery = "SELECT item_desc,item_comment FROM " + SHOPPING_L_ADD_LIST;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
            do 
            {                
                // Adding contact to list
            	ItemCommentPair item_to_add = new ItemCommentPair();
            	item_to_add.Item = cursor.getString(0);
            	item_to_add.Comment = cursor.getString(1);
            	local_items.add(item_to_add);
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        // return contact list
        return local_items;
    }
    
    public List<String> getLocalRemovedListItems() 
    {
        List<String> local_items = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT item_desc FROM " + SHOPPING_L_REMOVE_LIST;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) 
        {
            do 
            {                
                // Adding contact to list
            	local_items.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        // return contact list
        return local_items;
    }
    
    public UserData getUser()
    {
    	// Select All Query
        String selectQuery = "SELECT * FROM " + SHOPPING_USER;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);        
        
        if (cursor.moveToFirst()) 
        {
            do 
            {            
            	appUser = new UserData(cursor.getString(1),cursor.getString(2));                            	
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        return appUser;
    }
    
    /* ADMIN METHODS *******************************************************************/
    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

