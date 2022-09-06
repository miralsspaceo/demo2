package com.app.personas_social.stickerview.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.app.personas_social.R
import com.app.personas_social.databinding.FragmentTextEditingBinding
import com.app.personas_social.stickerview.callbacks.TextEditorOperation


class TextEditingFragment : DialogFragment() {
    private var textSize: Int? = 0
    private var lineSpacing: Float? = 0f
    private var letterSpacing: Float? = 0f
    private var inputText: String = ""

    private var colorPosition: Int = 0
    private var fontPosition: Int = 0
    private var startTime: Long = 0L
    private var endTime: Long = 0L
    private var strTag: String = ""


    private var gravity = Gravity.CENTER
    private var yPos = 0f
    private var preYpos = 0f
    private lateinit var binding :FragmentTextEditingBinding


    fun newInstance(): TextEditingFragment {
        return TextEditingFragment()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         binding = FragmentTextEditingBinding.inflate(inflater,container,false)

        return binding.root

    }

    private var isReached = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTextValue()
        binding.tvDone.setOnClickListener {
            onDoneClickListener?.onDoneClicked(
                binding.etPhotoText.text.toString().trim(),
                    colorPosition,
                    fontPosition,
                    convertSpToPx(binding.etPhotoText.textSize).toInt(),
                    lineSpacing!!,
                    letterSpacing!!,
                    gravity,
                    yPos,
                   startTime,
                endTime,
                strTag
            )
            dismiss()
        }

        binding.tvClose.setOnClickListener {
            onDoneClickListener!!.onCloseClicked()
            dismiss()
        }

    }

    fun setColorPos(yPos: Float) {
        preYpos = yPos
        this.yPos = yPos
    }

    private fun loadTextValue() {

     //   etPhotoText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        inputText = this.requireArguments().getString("text")!!
        textSize = this.requireArguments().getInt("text_size")

        lineSpacing = requireArguments().getFloat("line_spacing")
        letterSpacing = requireArguments().getFloat("letter_spacing")

        colorPosition = requireArguments().getInt("color")
        fontPosition = requireArguments().getInt("font")

        startTime = requireArguments().getLong("start_time")
        endTime = requireArguments().getLong("end_time")
        strTag = requireArguments().getString("str_tag").toString()

        gravity = requireArguments().getInt("gravity")

        if (inputText.isEmpty())
            binding.etPhotoText.setText(getString(R.string.enter_the_text))
        else
            binding.etPhotoText.setText(inputText)
        if (context != null)
            binding.etPhotoText.setSelection(binding.etPhotoText.text!!.length)
        binding.etPhotoText.setTextColor( Color.WHITE)


    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog

        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        }
    }

    private var onDoneClickListener: TextEditorOperation? = null

    fun OnDone(onDoneClickListener: TextEditorOperation) {
        this.onDoneClickListener = onDoneClickListener
    }

    private fun convertSpToPx(scaledPixels: Float): Float {
        return scaledPixels / requireContext().getResources().displayMetrics.scaledDensity
    }

}