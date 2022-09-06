package com.app.personas_social.utlis

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class RecyclerItemClickListener(context: Context?, private val mListener: OnItemClickListener?) :
    RecyclerView.OnItemTouchListener {
    var mGestureDetector: GestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return true
        }
    })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView: View? = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        mListener!!.onTouch()
    }


    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onTouch()
    }

}