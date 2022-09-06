package com.app.personas_social.utlis

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class RecycleViewTouchListener(
    context: Context?, recyclerView: RecyclerView,
    private val mOnTouchActionListener: OnTouchActionListener?
) : RecyclerView.OnItemTouchListener {
    private val mGestureDetector: GestureDetectorCompat = GestureDetectorCompat(context!!, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean { // Find the item view that was swiped based on the coordinates
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                val childPosition = recyclerView.getChildAdapterPosition(child!!)
                mOnTouchActionListener!!.onClick(child, childPosition)
            return false
        }

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent,
            velocityX: Float, velocityY: Float
        ): Boolean {
            try {
                if (Math.abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) {
                    return false
                }
                // Find the item view that was swiped based on the coordinates
                val child =
                    recyclerView.findChildViewUnder(e1.x, e1.y)
                val childPosition = recyclerView.getChildAdapterPosition(child!!)
                Log.e("onFling", "onFling: ${e1.x - e2.x}  >>>> ${abs(velocityX)}  >>SWIPE_MIN_DISTANCE $SWIPE_MIN_DISTANCE  SWIPE_THRESHOLD_VELOCITY $SWIPE_THRESHOLD_VELOCITY" )
                // right to left swipe
                if (e1.x - e2.x > SWIPE_MIN_DISTANCE && abs(
                        velocityX
                    ) > SWIPE_THRESHOLD_VELOCITY
                ) {
                    if (mOnTouchActionListener != null && child != null) {
                        mOnTouchActionListener.onLeftSwipe(child, childPosition)
                    }
                } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && abs(
                        velocityX
                    ) > SWIPE_THRESHOLD_VELOCITY
                ) {
                    if (mOnTouchActionListener != null && child != null) {
                        mOnTouchActionListener.onRightSwipe(child, childPosition)
                    }
                }
            } catch (e: Exception) { // nothing
            }
            return false
        }
    })

    interface OnTouchActionListener {
        fun onLeftSwipe(view: View?, position: Int)
        fun onRightSwipe(view: View?, position: Int)
        fun onClick(view: View?, position: Int)
    }


    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) { // do nothing
    }

    companion object {
        /*Change these as per your need*/
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
        private const val SWIPE_MAX_OFF_PATH = 250
    }

}