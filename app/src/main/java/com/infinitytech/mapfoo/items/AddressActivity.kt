package com.infinitytech.mapfoo.items

import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import com.infinitytech.mapfoo.BaseActivity
import com.infinitytech.mapfoo.R
import com.infinitytech.mapfoo.utils.*
import kotlinx.android.synthetic.main.activity_address.*

class AddressActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        searchBar.loadLayoutDescription(R.xml.searchbar_states)

        searchBar.setState(R.id.searchBarNormal, 1080, 1920)

        backBtn.onClick {
            d("back button clicked")
            finish()
        }

        cancelBtn.onTouch {
            d("cancel button clicked (${it.action})")

            if (it.action == MotionEvent.ACTION_UP) {
                addressEtv.setText("")
            }
            cancelBtn.onTouchEvent(it)
        }


        searchBtn.onClick {
            d("search button clicked")
            addressEtv.clearFocus()
        }

        rootView.onClick {
            d("rootView clicked")
            addressEtv.clearFocus()
        }

        addressEtv.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchBtn.performClick()
            }
            true
        }

        addressEtv.onFocus { focus ->
            val set = TransitionSet()
            set.ordering = TransitionSet.ORDERING_TOGETHER
            set.addTransition(Fade(Fade.OUT).apply { duration = 120 })
                    .addTransition(ChangeBounds()).apply { duration = 200 }
                    .addTransition(Fade(Fade.IN).apply { duration = 120 })
            TransitionManager.beginDelayedTransition(searchBar, set)
            searchBar.setState(if (focus) R.id.searchBarFocused else R.id.searchBarNormal, 1080, 1920)
            if (!focus) addressEtv.hideKeyBoard()
        }


    }

}