package com.app.personas_social.videoEditing

import android.content.Context
import android.graphics.Typeface
import com.app.personas_social.R
import com.app.personas_social.model.*
import com.app.personas_social.utlis.*

object VideoHelper {

    fun getColorList(): ArrayList<ColorItem> {
        val colorList = ArrayList<ColorItem>()
        colorList.clear()
        colorList.add(ColorItem(R.drawable.mutlicolor, false))
        colorList.add(ColorItem(R.color.color2, false))
        colorList.add(ColorItem(R.color.color3, false))
        colorList.add(ColorItem(R.color.color4, false))
        colorList.add(ColorItem(R.color.color5, false))
        colorList.add(ColorItem(R.color.color6, false))
        colorList.add(ColorItem(R.color.color7, false))
        return colorList
    }

    fun getGradientList(): ArrayList<GradientItem> {
        val gradientlist = ArrayList<GradientItem>()
        gradientlist.clear()
        gradientlist.add(GradientItem(R.drawable.color_blank,R.color.white,R.color.white, false))
        gradientlist.add(GradientItem(R.drawable.blue_gradient,R.color.darkblue, R.color.lightblue,false))
        gradientlist.add(GradientItem(R.drawable.red_gradient, R.color.dark_red, R.color.light_red,false))
        gradientlist.add(GradientItem(R.drawable.yellow_gradient,R.color.dark_yellow, R.color.light_yellow, false))
        gradientlist.add(GradientItem(R.drawable.orrange_gradient, R.color.dark_orrange, R.color.light_orrange,false))
        gradientlist.add(GradientItem(R.drawable.aqua_gradient,R.color.dark_aqua, R.color.light_aqua, false))
        gradientlist.add(GradientItem(R.drawable.green_gradient, R.color.dark_green, R.color.light_green,false))
        gradientlist.add(GradientItem(R.drawable.light_blue_gradient,R.color.dark_blue, R.color.light_blue, false))

        return gradientlist
    }



    fun getPatternList(): ArrayList<PatternItem> {
        val patternlist = ArrayList<PatternItem>()
        patternlist.clear()
        patternlist.add(PatternItem(R.drawable.color_blank,R.drawable.seekbar_thumb, false))
        patternlist.add(PatternItem(R.drawable.pattern_one,R.drawable.pone, false))
        patternlist.add(PatternItem(R.drawable.pattern_two,R.drawable.ptwo, false))
        patternlist.add(PatternItem(R.drawable.pattern_three,R.drawable.pthree, false))
        patternlist.add(PatternItem(R.drawable.pattern_four,R.drawable.pfour, false))
        patternlist.add(PatternItem(R.drawable.pattern_five,R.drawable.psix, false))
        patternlist.add(PatternItem(R.drawable.pattern_six,R.drawable.pseven, false))
        patternlist.add(PatternItem(R.drawable.pattern_eight,R.drawable.peight, false))
        return patternlist
    }

    fun getInvertList(): ArrayList<ColorItem> {
        val invertList = ArrayList<ColorItem>()
        invertList.clear()
        invertList.add(ColorItem(R.drawable.mutlicolor, false))
        invertList.add(ColorItem(R.color.color2, false))
        invertList.add(ColorItem(R.color.color3, false))
        invertList.add(ColorItem(R.color.color4, false))
        invertList.add(ColorItem(R.color.color5, false))
        invertList.add(ColorItem(R.color.color6, false))
        invertList.add(ColorItem(R.color.color7, false))
        return invertList
    }

    fun getBorderList(): ArrayList<BorderItem> {
        val borderlist = ArrayList<BorderItem>()
        borderlist.clear()
        borderlist.add(BorderItem(R.drawable.border_blank,R.color.white, false, BLANK_BORDER))
        borderlist.add(BorderItem(R.drawable.react_black_border,R.color.black, false,LIGHT_BORDER))
        borderlist.add(BorderItem(R.drawable.react_black_darkborder,R.color.black, false,DARK_BORDER))
        borderlist.add(BorderItem(R.drawable.react_multicolor,R.color.black, false,MULTICOLOR_BORDER))
        borderlist.add(BorderItem(R.drawable.react_dot_border,R.color.black, false,DOT_BORDER))
        borderlist.add(BorderItem(R.drawable.react_red_border,R.color.red, false,RED_BORDER))


        return borderlist
    }



    fun getBasicAnimationList(): ArrayList<AnimationItem> {
        val animationlist = ArrayList<AnimationItem>()
        animationlist.clear()
        animationlist.add(AnimationItem(R.drawable.ic_left_arrow, false))
        animationlist.add(AnimationItem(R.drawable.ic_up_arrow, false))
        animationlist.add(AnimationItem(R.drawable.ic_right_arrow, false))
        animationlist.add(AnimationItem(R.drawable.ic_down_arrow, false))
        animationlist.add(AnimationItem(R.drawable.ic_rightcorner_arrow, false))

        return animationlist
    }

    fun getBasicPinkAnimationList(): ArrayList<AnimationItem> {
        val animationlist = ArrayList<AnimationItem>()
        animationlist.clear()
        animationlist.add(AnimationItem(R.drawable.ic_left_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_up_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_right_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_down_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_right_corner_pink, false))

        return animationlist
    }



    fun getloopAnimationList(): ArrayList<AnimationItem> {
        val animationlist = ArrayList<AnimationItem>()
        animationlist.clear()
        animationlist.add(AnimationItem(R.drawable.left_rotate, false))
        animationlist.add(AnimationItem(R.drawable.right_rotate, false))
        animationlist.add(AnimationItem(R.drawable.ic_left_right, false))
        animationlist.add(AnimationItem(R.drawable.ic_parraler, false))
        animationlist.add(AnimationItem(R.drawable.ic_rotation_, false))
        return animationlist
    }


    fun getpinkloopAnimationList(): ArrayList<AnimationItem> {
        val animationlist = ArrayList<AnimationItem>()
        animationlist.clear()
        animationlist.add(AnimationItem(R.drawable.ic_leftroate_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_rightrotate_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_left_right_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_parraler_pink, false))
        animationlist.add(AnimationItem(R.drawable.ic_leftroate_pink, false))
        return animationlist
    }



    fun getFontList(context: Context): ArrayList<FontBean> {
        val fontList = ArrayList<FontBean>()
        fontList.add(
            FontBean(
                "Proxima Nova",
                Typeface.createFromAsset(context.assets, "font/proxima_nova_bold.otf")
            )
        )
        fontList.add(
            FontBean(
                "Didact Gothic",
                Typeface.createFromAsset(context.assets, "font/pt_sans_bold_italic.ttf")
            )
        )
        fontList.add(
            FontBean(
                "Disco Society",
                Typeface.createFromAsset(context.assets, "font/disco_society.otf")
            )
        )
        fontList.add(
            FontBean(
                "PT Sans",
                Typeface.createFromAsset(
                    context.assets,
                    "font/pt_sans_bold_italic.ttf"
                )
            )
        )
        fontList.add(
            FontBean(
                "Satisfy",
                Typeface.createFromAsset(context.assets, "font/satisfy_regular.ttf")
            )
        )

        fontList.add(
            FontBean(
                "Hillshort",
                Typeface.createFromAsset(context.assets, "font/hillshort.otf")
            )
        )
        fontList.add(
            FontBean(
                "Hujan",
                Typeface.createFromAsset(context.assets, "font/hujan.ttf")
            )
        )
        fontList.add(
            FontBean(
                "Newton Howard",
                Typeface.createFromAsset(context.assets, "font/newton_howard_font.ttf")
            )
        )
        fontList.add(
            FontBean(
                "Paradigma Regular",
                Typeface.createFromAsset(context.assets, "font/paradigma_regular_trial.otf")
            )
        )
        fontList.add(
            FontBean(
                "Passion of the Rose",
                Typeface.createFromAsset(context.assets, "font/passion_of_the_rose.ttf")
            )
        )
        fontList.add(
            FontBean(
                "Quilt Patches",
                Typeface.createFromAsset(context.assets, "font/quilt_patches.ttf")
            )
        )
        fontList.add(
            FontBean(
                "The Californication",
                Typeface.createFromAsset(context.assets, "font/the_californication.ttf")
            )
        )

        return fontList
    }





}