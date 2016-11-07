package com.beloo.widget.chipslayoutmanager.layouter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.cache.IViewCacheStorage;
import com.beloo.widget.chipslayoutmanager.gravity.IChildGravityResolver;
import com.beloo.widget.chipslayoutmanager.layouter.criteria.IFinishingCriteria;
import com.beloo.widget.chipslayoutmanager.layouter.placer.IPlacer;

class RTLUpLayouter extends AbstractLayouter implements ILayouter {
    private static final String TAG = RTLUpLayouter.class.getSimpleName();

    private int viewLeft;

    private RTLUpLayouter(Builder builder) {
        super(builder);
        viewLeft = builder.viewLeft;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    void onPreLayout() {
        int leftOffsetOfRow = -(getCanvasRightBorder() - viewLeft);

        for (Pair<Rect, View> rowViewRectPair : rowViews) {
            Rect viewRect = rowViewRectPair.first;

            viewRect.left = viewRect.left - leftOffsetOfRow;
            viewRect.right = viewRect.right - leftOffsetOfRow;

            rowTop = Math.min(rowTop, viewRect.top);
            rowBottom = Math.max(rowBottom, viewRect.bottom);
        }
    }

    @Override
    void onAfterLayout() {
        //go to next row, increase top coordinate, reset left
        viewLeft = getCanvasLeftBorder();
        rowBottom = rowTop;
    }

    @Override
    Rect createViewRect(View view) {
        int right = viewLeft + currentViewWidth;
        int viewTop = rowBottom - currentViewHeight;
        Rect viewRect = new Rect(viewLeft, viewTop, right, rowBottom);
        viewLeft = viewRect.right;
        return viewRect;
    }

    @Override
    public boolean onAttachView(View view) {

        if (viewLeft != getCanvasLeftBorder() && viewLeft + getLayoutManager().getDecoratedMeasuredWidth(view) > getCanvasRightBorder()) {
            viewLeft = getCanvasLeftBorder();
            rowBottom = rowTop;
        } else {
            viewLeft = getLayoutManager().getDecoratedRight(view);
        }

        rowTop = Math.min(rowTop, getLayoutManager().getDecoratedTop(view));

        return super.onAttachView(view);
    }

    @Override
    public boolean canNotBePlacedInCurrentRow() {
        //when go up, check cache to layout according previous down algorithm
        boolean stopDueToCache = getCacheStorage().isPositionEndsRow(getCurrentViewPosition());
        if (stopDueToCache) return true;

        int bufRight = viewLeft + currentViewWidth;
        return super.canNotBePlacedInCurrentRow() || (bufRight > getCanvasRightBorder() && viewLeft > getCanvasLeftBorder());
    }

    @Override
    public AbstractPositionIterator positionIterator() {
        return new DecrementalPositionIterator();
    }


    public static final class Builder extends AbstractLayouter.Builder {
        private int viewLeft;

        private Builder() {
        }

        @NonNull
        @Override
        public AbstractLayouter.Builder offsetRect(@NonNull Rect offsetRect) {
            this.viewLeft = offsetRect.left;
            return super.offsetRect(offsetRect);
        }

        @NonNull
        public RTLUpLayouter build() {
            return new RTLUpLayouter(this);
        }
    }
}
