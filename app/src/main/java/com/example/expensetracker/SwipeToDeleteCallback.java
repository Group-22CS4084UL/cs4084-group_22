package com.example.expensetracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final TransactionAdapter adapter;
    private final Drawable icon;
    private final ColorDrawable background;

    public SwipeToDeleteCallback(TransactionAdapter adapter, Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
        
        // Initialize the delete icon
        icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
        
        // Initialize background
        background = new ColorDrawable(Color.RED);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        adapter.removeItem(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (icon != null) {
            int itemHeight = viewHolder.itemView.getHeight();
            
            // Calculate icon bounds
            int iconMargin = (itemHeight - icon.getIntrinsicHeight()) / 2;
            int iconTop = viewHolder.itemView.getTop() + iconMargin;
            int iconBottom = iconTop + icon.getIntrinsicHeight();
            int iconLeft = viewHolder.itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = viewHolder.itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            // Draw background
            background.setBounds(
                viewHolder.itemView.getRight() + ((int) dX),
                viewHolder.itemView.getTop(),
                viewHolder.itemView.getRight(),
                viewHolder.itemView.getBottom()
            );
            background.draw(c);

            // Draw icon
            icon.draw(c);
        }
    }
}
