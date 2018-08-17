package com.infinitytech.mapfoo.items

import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.transition.*
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

class AddressFragment : SwipeBackFragment() {

    private var stateNormal = ConstraintSet()
    private var stateFocused = ConstraintSet()

    companion object {
        fun newInstance(): AddressFragment = AddressFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_address, container, false))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stateNormal.clone(context, R.layout.search_bar_normal)
        stateFocused.clone(context, R.layout.search_bar_focused)

        backBtn.onclick {
            fragmentManager?.popBackStack()
        }

        view.onclick {
            it.requestFocus()
        }

        addressEtv.onFocus {
            val set = TransitionSet()
            set.ordering = TransitionSet.ORDERING_TOGETHER
            set.addTransition(Fade(Fade.OUT).apply { duration = 120 })
                    .addTransition(ChangeBounds()).apply { duration = 200 }
                    .addTransition(Fade(Fade.IN).apply { duration = 120 })

            if (it) {
                set.addListener {
                    onEnd {
                        addressEtv.isCursorVisible = true
                        d("end Transition")
                    }
                }
                TransitionManager.beginDelayedTransition(searchBar, set)
                stateFocused.applyTo(searchBar)
            } else {
                set.addListener {
                    onStart {
                        d("start Transition")
                        addressEtv.isCursorVisible = false
                    }
                }
                TransitionManager.beginDelayedTransition(searchBar, set)
                stateNormal.applyTo(searchBar)
            }
        }
    }

}