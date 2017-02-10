package com.example.dell.warehouse;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import com.example.dell.warehouse.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.dell.warehouse.data.ProductProvider.LOG_TAG;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;
    private Uri currentProductUri;
    private ContentValues values;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

                /*Set an empty view if no data available*/
        // Find the ListView which will be populated with the pet data
        ListView productListView = (ListView) findViewById(R.id.listView);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        mCursorAdapter = new ProductCursorAdapter(this,null);
        productListView.setAdapter(mCursorAdapter);


        //setup listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create new activity
                Intent intent = new Intent(MainActivity.this,EditorActivity.class);

                currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
        //initialise the loader
        getLoaderManager().initLoader(PRODUCT_LOADER,null,this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about.
        String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_NAME, ProductEntry.COLUMN_PRODUCT_PRICE,
        ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductEntry.COLUMN_PRODUCT_IMAGE};
        //This loader will execute the ContentProvider's query method in a background
        return new CursorLoader(this,  //Parent activity context
                ProductEntry.CONTENT_URI,   //Provider content URI to query
                projection, //columns to include in the resulting cursor
                null,   //no selection cause
                null,   //no selectionArgs clause
                null);  //default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update {@link PetCursorAdapter} with this new cursor containing updated data
        mCursorAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

}
