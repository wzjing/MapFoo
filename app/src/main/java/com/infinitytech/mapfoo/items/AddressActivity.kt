package com.infinitytech.mapfoo.items

import android.graphics.Path
import android.os.Bundle
import android.transition.*
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
                    .addTransition(ChangeBounds().apply {
                        duration = 200
                        pathMotion = object : PathMotion() {
                            override fun getPath(startX: Float, startY: Float,
                                                 endX: Float, endY: Float) = Path().apply {
                                d("PathMotion: ($startX, $startY) -> ($endX, $endY)")
                                moveTo(startX, startY)
                                lineTo(endX, endY)
//                                rCubicTo(startX, (endY - startY) * 0.5445f,
//                                        (endX - startX) * 0.4555f, endY,
//                                        endX - startX, endY - startY)
                            }
                        }
                    })
                    .addTransition(Fade(Fade.IN).apply { duration = 120 })
            TransitionManager.beginDelayedTransition(searchBar, set)
            if (focus) {
                searchBar.setState(R.id.searchBarFocused, 1080, 1920)
                addressEtv.isCursorVisible = true
            } else {
                searchBar.setState(R.id.searchBarNormal, 1080, 1920)
                addressEtv.hideKeyBoard()
                addressEtv.isCursorVisible = false
            }
        }

        val map = mapView.map
        mapView.onCreate(null)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

}