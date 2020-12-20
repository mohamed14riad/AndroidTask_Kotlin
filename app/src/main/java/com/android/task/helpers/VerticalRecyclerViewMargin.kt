package com.android.task.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class VerticalRecyclerViewMargin(private val margin: Int, private val columns: Int) :
    ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)

        // set bottom margin to all
        outRect.bottom = margin

        // we only add top margin to the first row
        if (position < columns) {
            outRect.top = margin
        }

        // set right margin to all
        outRect.right = margin

        // add left margin only to the first column
        if (position % columns == 0) {
            outRect.left = margin
        }
    }
}