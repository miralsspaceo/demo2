package com.app.personas_social.utlis

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.atan2
import kotlin.math.roundToLong

private val values = FloatArray(9)

fun Matrix.animateScaleToPoint(
    scaleFactor: Float,
    dx: Float,
    dy: Float,
    onUpdate: () -> Unit = {}
) {

    val targetMatrix = this.clone()
        .apply {
            postConcat(Matrix().apply {
                Log.e("animateScaleToPoint", "scaleFactor>>>> $scaleFactor  dx>>>> $dx  dy>>>> $dy" )
                setScale(0.5f, 0.5f, dx, dy)
//                setScale(0.5f,dy)

            })
        }

    animateToMatrix(targetMatrix, onUpdate)
}

fun Matrix.animateToMatrix(
    targetMatrix: Matrix,
    onUpdate: () -> Unit = {}
) {

    val scaleAnimator = ValueAnimator.ofFloat(this.getScaleX(), targetMatrix.getScaleX())
    val translateXAnimator =
        ValueAnimator.ofFloat(this.getTranslateX(), targetMatrix.getTranslateX())
    val translateYAnimator =
        ValueAnimator.ofFloat(this.getTranslateY(), targetMatrix.getTranslateY())

    translateYAnimator.addUpdateListener {
        reset()
        preScale(
            scaleAnimator.animatedValue as Float,
            scaleAnimator.animatedValue as Float
        )
        postTranslate(
            translateXAnimator.animatedValue as Float,
            translateYAnimator.animatedValue as Float
        )
        postRotate(45f)
        onUpdate.invoke()
    }

    AnimatorSet()
        .apply {
            playTogether(
                scaleAnimator,
                translateXAnimator,
                translateYAnimator
            )
        }
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .apply { duration = 300 }
        .start()


}


fun Matrix.animateToMatrixText(
    targetMatrix: Matrix,
    px: Float,
    py: Float,
    width: Float,
    height: Float,
    diffX: Float,
    diffY: Float,
    scaleRatio: Pair<Float, Float>,
    loopDuration: Float,
    onUpdate: () -> Unit = {},
    onEnd: () -> Unit = {}
) {


    val scaleAnimator =
        ValueAnimator.ofFloat(scaleRatio.first, scaleRatio.second)
    Log.e("animateToMatrixText", "scaleRatio.first: ${scaleRatio.first}")
    Log.e("animateToMatrixText", "scaleRatio.second: ${scaleRatio.second}")


    val translateXAnimator =
        ValueAnimator.ofFloat(this.getTranslateX(), targetMatrix.getTranslateX())
    val translateYAnimator =
        ValueAnimator.ofFloat(this.getTranslateY(), targetMatrix.getTranslateY())

    Log.e("animateToMatrixText", "translateXAnimator: ${translateXAnimator.values}")
    Log.e("animateToMatrixText", "translateYAnimator: ${translateYAnimator.values}")
    //  Log.d("TAG", "translateX${targetMatrix.getTranslateX()}")


    scaleAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            onEnd()
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }

    })
    scaleAnimator.addUpdateListener {
        Log.d("TAG", "beforeMatrixText$this")
        reset()
        postRotate(getMatrixAngle(targetMatrix))


        preScale(
            scaleAnimator.animatedValue as Float,
            scaleAnimator.animatedValue as Float,
            px, py
        )


        val transX = px - (width / 2 * scaleAnimator.animatedValue as Float)
        val transY = py - (height / 2 * scaleAnimator.animatedValue as Float)


        /* val replaceMatrix = Matrix()
         replaceMatrix.postScale(
             scaleAnimator.animatedValue as Float * 1f,
             scaleAnimator.animatedValue as Float * 1f,

             px,
             py
         )*/
        postTranslate(
            transX - getTranslateX() - diffX,
            transY - getTranslateY() - diffY
        )

//         postRotate(getMatrixAngle(targetMatrix))
        onUpdate.invoke()
    }

    AnimatorSet()
        .apply {
            playTogether(
                scaleAnimator,
                translateXAnimator,
                translateYAnimator
            )
        }
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .apply { duration = loopDuration.roundToLong() }
        .start()
}

fun Matrix.getScaleX(): Float {
    getValues(values)
    return values[Matrix.MSCALE_X]
}

fun Matrix.getScaleY(): Float {
    getValues(values)
    return values[Matrix.MSCALE_Y]
}

fun Matrix.getTranslateX(): Float {
    getValues(values)
    return values[Matrix.MTRANS_X]
}

fun Matrix.getTranslateY(): Float {
    getValues(values)
    return values[Matrix.MTRANS_Y]
}

fun Matrix.clone(): Matrix {
    getValues(values)
    return Matrix().apply {
        setValues(values)
    }
}

fun getMatrixAngle(matrix: Matrix): Float {
    return Math.toDegrees(
        -atan2(
            getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(),
            getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()
        )
    ).toFloat()
}
