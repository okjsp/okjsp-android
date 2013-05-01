/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh;

import net.okjsp.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView> {

	private LoadingLayout mHeaderLoadingView;
	private LoadingLayout mFooterLoadingView;

	private FrameLayout mLvFooterLoadingFrame;
	private boolean mAddedLvFooter = false;

	class InternalListView extends ListView implements EmptyViewMethodAccessor {

		public InternalListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setAdapter(ListAdapter adapter) {
			// Add the Footer View at the last possible moment
			if (!mAddedLvFooter && null != mLvFooterLoadingFrame) {
				addFooterView(mLvFooterLoadingFrame, null, false);
				mAddedLvFooter = true;
			}

			super.setAdapter(adapter);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}
	}

	public PullToRefreshListView(Context context) {
		super(context);
		setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, int mode) {
		super(context, mode);
		setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDisableScrollingWhileRefreshing(false);
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalListView) getRefreshableView()).getContextMenuInfo();
	}

	public void setReleaseLabel(String releaseLabel) {
		super.setReleaseLabel(releaseLabel);

		if (null != mHeaderLoadingView) {
			mHeaderLoadingView.setReleaseLabel(releaseLabel);
		}
		if (null != mFooterLoadingView) {
			mFooterLoadingView.setReleaseLabel(releaseLabel);
		}
	}

	public void setPullLabel(String pullLabel) {
		super.setPullLabel(pullLabel);

		if (null != mHeaderLoadingView) {
			mHeaderLoadingView.setPullLabel(pullLabel);
		}
		if (null != mFooterLoadingView) {
			mFooterLoadingView.setPullLabel(pullLabel);
		}
	}

	public void setRefreshingLabel(String refreshingLabel) {
		super.setRefreshingLabel(refreshingLabel);

		if (null != mHeaderLoadingView) {
			mHeaderLoadingView.setRefreshingLabel(refreshingLabel);
		}
		if (null != mFooterLoadingView) {
			mFooterLoadingView.setRefreshingLabel(refreshingLabel);
		}
	}

	@Override
	protected final ListView createRefreshableView(Context context, AttributeSet attrs) {
		ListView lv = new InternalListView(context, attrs);

		final int mode = getMode();

		// Loading View Strings
		String pullLabel = context.getString(R.string.pull_to_refresh_pull_label);
		String refreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
		String releaseLabel = context.getString(R.string.pull_to_refresh_release_label);

		// Get Styles from attrs
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

		// Add Loading Views
		if (mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) {
			FrameLayout frame = new FrameLayout(context);
			mHeaderLoadingView = new LoadingLayout(context, MODE_PULL_DOWN_TO_REFRESH, releaseLabel, pullLabel,
					refreshingLabel, a);
			frame.addView(mHeaderLoadingView, FrameLayout.LayoutParams.FILL_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			mHeaderLoadingView.setVisibility(View.GONE);
			lv.addHeaderView(frame, null, false);
		}
		if (mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) {
			mLvFooterLoadingFrame = new FrameLayout(context);
			mFooterLoadingView = new LoadingLayout(context, MODE_PULL_UP_TO_REFRESH, releaseLabel, pullLabel,
					refreshingLabel, a);
			mLvFooterLoadingFrame.addView(mFooterLoadingView, FrameLayout.LayoutParams.FILL_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			mFooterLoadingView.setVisibility(View.GONE);
		}

		a.recycle();

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return lv;
	}

	@Override
	protected void setRefreshingInternal(boolean doScroll) {

		// If we're not showing the Refreshing view, or the list is empty, then
		// the header/footer views won't show so we use the
		// normal method
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
			super.setRefreshingInternal(doScroll);
			return;
		}

		super.setRefreshingInternal(false);

		final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
		final int selection, scrollToY;

		switch (getCurrentMode()) {
			case MODE_PULL_UP_TO_REFRESH:
				originalLoadingLayout = getFooterLayout();
				listViewLoadingLayout = mFooterLoadingView;
				selection = mRefreshableView.getCount() - 1;
				scrollToY = getScrollY() - getHeaderHeight();
				break;
			case MODE_PULL_DOWN_TO_REFRESH:
			default:
				originalLoadingLayout = getHeaderLayout();
				listViewLoadingLayout = mHeaderLoadingView;
				selection = 0;
				scrollToY = getScrollY() + getHeaderHeight();
				break;
		}

		if (doScroll) {
			// We scroll slightly so that the ListView's header/footer is at the
			// same Y position as our normal header/footer
			setHeaderScroll(scrollToY);
		}

		// Hide our original Loading View
		originalLoadingLayout.setVisibility(View.INVISIBLE);

		// Show the ListView Loading View and set it to refresh
		listViewLoadingLayout.setVisibility(View.VISIBLE);
		listViewLoadingLayout.refreshing();

		if (doScroll) {
			// Make sure the ListView is scrolled to show the loading
			// header/footer
			mRefreshableView.setSelection(selection);

			// Smooth scroll as normal
			smoothScrollTo(0);
		}
	}

	@Override
	protected void resetHeader() {

		// If we're not showing the Refreshing view, or the list is empty, then
		// the header/footer views won't show so we use the
		// normal method
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
			super.resetHeader();
			return;
		}

		LoadingLayout originalLoadingLayout;
		LoadingLayout listViewLoadingLayout;

//		int scrollToHeight = getHeaderHeight();
		int selection;

		switch (getCurrentMode()) {
			case MODE_PULL_UP_TO_REFRESH:
				originalLoadingLayout = getFooterLayout();
				listViewLoadingLayout = mFooterLoadingView;

				selection = mRefreshableView.getCount() - 1;
				break;
			case MODE_PULL_DOWN_TO_REFRESH:
			default:
				originalLoadingLayout = getHeaderLayout();
				listViewLoadingLayout = mHeaderLoadingView;
//				scrollToHeight *= -1;
				selection = 0;
				break;
		}

		originalLoadingLayout.setVisibility(View.VISIBLE);
		

		mRefreshableView.dispatchTouchEvent(MotionEvent.obtain(0, 0,
		        MotionEvent.ACTION_CANCEL, 100.0f, 100.0f, 0));
		if (getState() != MANUAL_REFRESHING && mRefreshableView.getFirstVisiblePosition() == 0) {
			mRefreshableView.setSelection(selection);
			setHeaderScroll(-mRefreshableView.getChildAt(0).getBottom());
		}else if(mRefreshableView.getFirstVisiblePosition() != 0){
		    mRefreshableView.setSelection(selection);
		}

		// Hide the ListView Header/Footer
		listViewLoadingLayout.setVisibility(View.GONE);

		super.resetHeader();
	}

	protected int getNumberInternalHeaderViews() {
		return null != mHeaderLoadingView ? 1 : 0;
	}

	protected int getNumberInternalFooterViews() {
		return null != mFooterLoadingView ? 1 : 0;
	}

}
