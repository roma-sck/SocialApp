package net.kultprosvet.androidcourse.socialapp.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class FabScrollBehavior extends FloatingActionButton.Behavior {

    public FabScrollBehavior(Context context, AttributeSet attributeSet){
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        //child -> Floating Action Button
        if(dyConsumed > 0 && child.getVisibility() == View.VISIBLE){
            // User scrolled down and the FAB is currently visible -> hide the FAB
            child.hide();
        } else if(dyConsumed < 0 && child.getVisibility() == View.GONE){
            // User scrolled up and the FAB is currently not visible -> show the FAB
            child.show();
        }
    }
}
