package com.app.personas_social.utlis

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SliderLayoutManager(
    private val context: Context?,
    private val recycleView: RecyclerView,
    private val callbackDelete: (view: Int) -> Unit
) : LinearLayoutManager(context) {


    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        // When scroll stops we notify on the selected item
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            try {
                // Find the closest child to the recyclerView center --> this is the selected item.
                val recyclerViewCenterX = getRecyclerViewCenterX()
                var minDistance = recycleView.width
                var position = -1
                for (i in 0 until recycleView.childCount) {
                    val child = recycleView.getChildAt(i)
                    val childCenterX =
                        getDecoratedLeft(child) + (getDecoratedRight(child) - getDecoratedLeft(child)) / 2
                    val childDistanceFromCenter = Math.abs(childCenterX - recyclerViewCenterX)
                    if (childDistanceFromCenter < minDistance) {
                        minDistance = childDistanceFromCenter
                        position = recycleView.getChildLayoutPosition(child)
                    }
                }
                callbackDelete(position)

            } catch (e: IndexOutOfBoundsException) {
                Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens")
            }

        }
    }

    override fun setOrientation(orientation: Int) {
        super.setOrientation(RecyclerView.HORIZONTAL)
    }

    private fun getRecyclerViewCenterX(): Int {
        return (recycleView.right - recycleView.left) / 2 + recycleView.left
    }


}