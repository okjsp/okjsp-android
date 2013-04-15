package net.okjsp;

import net.okjsp.acv_adapter.ActionsAdapter;
import net.okjsp.acv_fragment.AboutFragment;
import net.okjsp.acv_fragment.BoardFragment;
import net.okjsp.acv_fragment.MainFragment;
import net.okjsp.imageloader.ImageCache;
import net.okjsp.imageloader.ImageFetcher;
import net.okjsp.imageloader.ImageResizer;
import net.okjsp.imageloader.ImageWorker;
import shared.ui.actionscontentview.ActionsContentView;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements OnClickListener, AdapterView.OnItemClickListener, Const {
	protected static final String TAG = "MainActivity";

	protected static final String STATE_URI = "state:uri";
	protected static final String STATE_FRAGMENT_TAG = "state:fragment_tag";

	protected ActionsContentView mMenuDrawer;
	protected ActionsAdapter mActionsAdapter;
	protected ListView mActionListView;

	protected Uri currentUri = MainFragment.URI;
	protected String currentContentFragmentTag = null;

    protected static ImageResizer mImageWorker;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        // activity runs full screen
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;
        final int longest = height > width ? height : width;
        
        mImageWorker = new ImageFetcher(this, longest);
        mImageWorker.setLoadingImage(R.drawable.ic_launcher);
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(this, IMAGE_CACHE_DIR));
        mImageWorker.setImageFadeIn(false);
        
        // setup menu drawer
        mMenuDrawer = (ActionsContentView) findViewById(R.id.menu_drawer);
        mActionListView = (ListView) findViewById(R.id.actions);
        mActionsAdapter = new ActionsAdapter(this);
        mActionListView.setAdapter(mActionsAdapter);
        mActionListView.setOnItemClickListener(this);

        findViewById(R.id.iv_menu).setOnClickListener(this);

        if (savedInstanceState != null) {
            currentUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            currentContentFragmentTag = savedInstanceState.getString(STATE_FRAGMENT_TAG);
        }

        updateContent(currentUri);
    }

    @Override
    public void onPause() {
        mImageWorker.setExitTasksEarly(true);
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mImageWorker.setExitTasksEarly(false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActionsAdapter.recycle();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URI, currentUri.toString());
        outState.putString(STATE_FRAGMENT_TAG, currentContentFragmentTag);

        super.onSaveInstanceState(outState);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.iv_menu:
            onActionsButtonClick(v);
			break;
		}
	}
    
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long flags) {
        final Uri uri = mActionsAdapter.getItem(position);
        Log.i(TAG, "position:" + position + ", " + uri.toString());
        updateContent(uri);
        mMenuDrawer.showContent();
	}
	
    public void onActionsButtonClick(View view) {
        if (mMenuDrawer.isActionsShown())
        	mMenuDrawer.showContent();
        else
        	mMenuDrawer.showActions();
    }

    public void updateContent(Uri uri) {
        final Fragment fragment;
        final String tag;

        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction tr = fm.beginTransaction();

        if (currentContentFragmentTag != null) {
            final Fragment currentFragment = fm.findFragmentByTag(currentContentFragmentTag);
            if (currentFragment != null)
                tr.hide(currentFragment);
        }

        if (AboutFragment.URI.equals(uri)) {
            tag = AboutFragment.TAG;
            final Fragment foundFragment = fm.findFragmentByTag(tag);
            if (foundFragment != null) {
                fragment = foundFragment;
            } else {
                fragment = new AboutFragment();
            }
        } else if (MainFragment.URI.equals(uri)) {
                tag = MainFragment.TAG;
                final Fragment foundFragment = fm.findFragmentByTag(tag);
                if (foundFragment != null) {
                    fragment = foundFragment;
                } else {
                    fragment = new MainFragment();
                }
        } else {
        	Log.i(TAG, "updateContent:" + uri.toString());
            tag = BoardFragment.TAG;
            final Fragment foundFragment = fm.findFragmentByTag(tag);
            if (foundFragment != null) {
                fragment = foundFragment;
            } else {
                fragment = new BoardFragment();
            }
            ((BoardFragment)fragment).setUri(uri);
        }

        if (fragment.isAdded()) {
            tr.show(fragment);
        } else {
            tr.add(R.id.content, fragment, tag);
        }

        // 메인을 통해 다른 화면에 같을 경우 백버튼을 누르면 다시 메인에 오도록 변경
        if (!MainFragment.URI.equals(uri)) {
            tr.addToBackStack(null);
        }
        tr.commit();

        currentUri = uri;
        currentContentFragmentTag = tag;
    }
    
    public static ImageWorker getImageWorker() {
        return mImageWorker;
    }
}