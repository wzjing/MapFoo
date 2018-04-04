package com.infinitytech.mapfoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_text.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

class TextFragment: SwipeBackFragment() {

    companion object {
        public fun newInstance(): TextFragment {
            return TextFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_text, container, false))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backBtn.setOnClickListener { fragmentManager!!.popBackStack() }
    }
}