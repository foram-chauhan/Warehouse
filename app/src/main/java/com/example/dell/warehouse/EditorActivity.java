package com.example.dell.warehouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import com.example.dell.warehouse.data.ProductContract.ProductEntry;

import static android.R.attr.name;
import static com.example.dell.warehouse.MainActivity.PRODUCT_LOADER;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_URL_REQUEST = 1;

    private static final String STATE_URI = "STATE_URI";

    private Uri mUri;

    private ContentValues values;

    /** EditText field to enter the product's name */
    private EditText mNameEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** EditText field to enter the product's quantity */
    private EditText mQuantityEditText;

    /** ImageView field to enter the product's image */
    private ImageView mImage;

    /*Button field to save data*/
    private Button mOrderButton;
    /*Button field to select Image*/
    private Button mSaveImageButton;
    /*Button field to increase the quantity*/
    private Button mIncreaseButton;
    /*Button field to decrease the quantity*/
    private Button mDecreaseButton;

    /*mPetHasChanged will be true if the user updates part of the product form*/
    private boolean mProductHasChanged = false;

    private static final int EXISTING_PET_LOADER = PRODUCT_LOADER;

    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri mCurrentProductUri;
    /*Save the URI of the file name in a String variable*/
    String filePath;

    /*imageSelectBtnNotClicked will be true if the choose to update the image of the product*/
    private boolean imageSelectBtnNotClicked = false;
    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        mNameEditText = (EditText)findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText)findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText)findViewById(R.id.edit_product_quantity);
        mImage = (ImageView)findViewById(R.id.imageView);
        mIncreaseButton = (Button)findViewById(R.id.increaseBtn);
        mDecreaseButton = (Button)findViewById(R.id.decreaseBtn);


        //Examine the intent that was used to launch this activity
        //in order to figure out if we're creating a new pet or editing
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //if the contentUri does not contain a content URI,then create new pet
        if(mCurrentProductUri == null){
            setTitle(getString(R.string.editor_activity_title_new_product));
            //set the quantity to 2 initially
            mQuantityEditText.setText("");
            mQuantityEditText.setText("2");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }else{
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
            setTitle(getString(R.string.editor_activity_title_edit_product));
        }




        mIncreaseButton.setOnClickListener(this);
        mDecreaseButton.setOnClickListener(this);

        mOrderButton = (Button)findViewById(R.id.orderMoreBtn);
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("Summary", getProductSummary());
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this

                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.email_subject_for_order));
                intent.putExtra(Intent.EXTRA_TEXT, getProductSummary());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mProductHasChanged = true;
                return false;
            }
        };

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mIncreaseButton.setOnTouchListener(mTouchListener);
        mDecreaseButton.setOnTouchListener(mTouchListener);

        mSaveImageButton = (Button)findViewById(R.id.saveImageBtn);
        mSaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelectBtnNotClicked = true;
                openImageSelector();
            }
        });
        ViewTreeObserver viewTreeObserver = mImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mImage.setImageBitmap(getBitmapFromUri(mUri));
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                filePath = mUri.toString();
                mImage.setImageBitmap(getBitmapFromUri(mUri));
            }
        } else if (requestCode == SEND_URL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // insert a pet
                saveProductInDb();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
    /**
     * Call this method to get Summary of the product in the database.
     */
    private String getProductSummary() {
        String finalStringSummary = "";
        // Only modify the summary if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the pet that we want.
            Cursor cursor = getContentResolver().query(mCurrentProductUri, null, null,null,null);
            if (cursor.moveToFirst()) {
                // Find the columns of pet attributes that we're interested in
                int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
                int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);


                // Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                String price = cursor.getString(priceColumnIndex);


                finalStringSummary = getString(R.string.summary_initial_message)+"\n"
                        +getString(R.string.hint_product_name)+ ": "+ name +"\n"
                        +getString(R.string.hint_product_price)+ ": "+price;

            }else{
                finalStringSummary = "";
            }
        }
        return finalStringSummary;
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v("onRestoreInstanceState","entered..");

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            filePath=(mUri.toString());
        }
        Log.v("onRestoreInstanceState","left..");
    }

    private void saveProductInDb(){
        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(price) &&
                TextUtils.isEmpty(quantityString) && filePath == null ) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            // you could also add a toast info here
            Toast.makeText(this, "Please fill all the required entries.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        values = new ContentValues();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,"Please enter name",Toast.LENGTH_LONG).show();
        }else{
            values.put(ProductEntry.COLUMN_PRODUCT_NAME,name);
        }
        String priceInitial = "0";
        if(!TextUtils.isEmpty(price)){
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE,price);
        }else{
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceInitial);
        }

        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY,quantity);

        if(imageSelectBtnNotClicked==true){
            if(filePath != null){
                values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, filePath);
            }else{
                values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, "");
            }
        }else{
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, "");
        }

        if (mCurrentProductUri == null) {
            // Insert a new pet into the provider, returning the content URI for the new pet.
            Uri id = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (id == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define a projection that specifies the columns from the table we care about.
        String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductEntry.COLUMN_PRODUCT_IMAGE};
        //This loader will execute the ContentProvider's query method in a background
        return new CursorLoader(this,  //Parent activity context
                mCurrentProductUri,   //Provider content URI to query
                projection, //columns to include in the resulting cursor
                null,   //no selection cause
                null,   //no selectionArgs clause
                null);  //default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity+"");
            mImage.setImageURI(Uri.parse(image));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        int quantityToBeAltered = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        if(view.equals(mIncreaseButton)){
            quantityToBeAltered = quantityToBeAltered+1;
            mQuantityEditText.setText("");
            mQuantityEditText.setText(quantityToBeAltered+"");
        }else if(view.equals(mDecreaseButton)){
            if(quantityToBeAltered == 0){
                Toast.makeText(EditorActivity.this,"Quantity can't be 0",Toast.LENGTH_LONG).show();
            }else{
                quantityToBeAltered = quantityToBeAltered-1;
                mQuantityEditText.setText("");
                mQuantityEditText.setText(quantityToBeAltered+"");
            }
        }
    }
}
