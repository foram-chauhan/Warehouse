package com.example.dell.warehouse;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.dell.warehouse.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;

import static android.net.Uri.*;
import static com.example.dell.warehouse.data.ProductProvider.LOG_TAG;
import static java.security.AccessController.getContext;

/**
 * Created by DELL on 09-02-2017.
 */

public class ProductCursorAdapter extends CursorAdapter {
    private ImageView image;
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {

        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.name_text_view);
        TextView tvPrice = (TextView) view.findViewById(R.id.price_text_view);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity_text_view);
        image = (ImageView)view.findViewById(R.id.image);

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        String summary  = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        String filepath = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE));

        tvName.setText(name);
        tvPrice.setText("Price : "+summary);
        tvQuantity.setText("Quantity : "+quantity);
        image.setImageURI(Uri.parse(filepath));
    }
}
