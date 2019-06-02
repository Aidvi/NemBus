package com.example.myproject.app.services

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val s = spacing / 2

        outRect.left = s
        outRect.right = s
        outRect.top = s
        outRect.bottom = s
    }
}