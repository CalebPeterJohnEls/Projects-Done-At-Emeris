package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

/** One page inside the tutorial ViewPager2 — content comes from arguments. */
class TutorialSlideFragment : Fragment(R.layout.item_tutorial_slide) {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_BODY = "arg_body"
        private const val ARG_ICON_RES = "arg_icon_res"

        fun newInstance(page: TutorialPage): TutorialSlideFragment =
            TutorialSlideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, page.title)
                    putString(ARG_BODY, page.body)
                    putInt(ARG_ICON_RES, page.iconRes)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        view.findViewById<TextView>(R.id.tvTutTitle).text = args.getString(ARG_TITLE)
        view.findViewById<TextView>(R.id.tvTutBody).text = args.getString(ARG_BODY)
        view.findViewById<ImageView>(R.id.imgIcon).setImageResource(args.getInt(ARG_ICON_RES))
    }
}

// Reference:
// Android Developers (2026) Fragments. Available at: https://developer.android.com/guide/fragments (Accessed: 16 August 2026).
