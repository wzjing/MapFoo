package com.infinitytech.mapfoo.items

import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.infinitytech.mapfoo.R
import com.infinitytech.mapfoo.utils.addListener
import com.infinitytech.mapfoo.utils.d
import com.infinitytech.mapfoo.utils.onFocus
import com.infinitytech.mapfoo.utils.onclick
import kotlinx.android.synthetic.main.search_bar_normal.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

class LocationSelectorFragment : SwipeBackFragment() {

    private var stateNormal = ConstraintSet()
    private var stateFocused = ConstraintSet()

    companion object {
        fun newInstance(): LocationSelectorFragment = LocationSelectorFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_location_selector, container, false))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stateNormal.clone(context, R.layout.search_bar_normal)
        stateFocused.clone(context, R.layout.search_bar_focused)

        backBtn.onclick {
            fragmentManager?.popBackStack()
        }

        addressEtv.onFocus {
            val transition = AutoTransition()
            transition.duration = 300

            if (it) {
                transition.addListener {
                    onStart {
                        d("start Transition")
                    }

                    onEnd {
                        addressEtv.isCursorVisible = true
                        d("end Transition")
                    }
                }
                TransitionManager.beginDelayedTransition(searchBar, transition)
                stateFocused.applyTo(searchBar)
            } else {
                transition.addListener {
                    onStart {
                        d("start Transition")
                        addressEtv.isCursorVisible = false
                    }

                    onEnd {
                        d("end Transition")
                    }
                }
                TransitionManager.beginDelayedTransition(searchBar, transition)
                stateNormal.applyTo(searchBar)
            }
        }
    }

}