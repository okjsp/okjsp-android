package net.okjsp;

import net.okjsp.acv_adapter.ActionsAdapter;
import net.okjsp.acv_fragment.BoardFragment;
import net.okjsp.acv_fragment.MainFragment;
import net.okjsp.acv_fragment.ProfileFragment;
import net.okjsp.data.BoardManager;
import net.okjsp.imageloader.ImageCache;
import net.okjsp.imageloader.ImageFetcher;
import net.okjsp.imageloader.ImageResizer;
import net.okjsp.imageloader.ImageWorker;
import shared.ui.actionscontentview.ActionsContentView;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

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
    protected boolean mRunOnce = false;
    protected boolean mShowSplash = true;
    protected Handler mHandler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getPreferences();
        
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
        mActionsAdapter.setSelected(3);
        mActionListView.setAdapter(mActionsAdapter);
        mActionListView.setOnItemClickListener(this);

        findViewById(R.id.iv_menu).setOnClickListener(this);

        if (savedInstanceState != null) {
            currentUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            currentContentFragmentTag = savedInstanceState.getString(STATE_FRAGMENT_TAG);
        }

        updateContent(currentUri);
        
        if (!mRunOnce) {
            mHandler.postDelayed(mMenuDrawerOpenRunnable, 1000);
            mRunOnce = true;
        }
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
        savePreferences();
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
        mActionsAdapter.setSelected(position);
        mActionsAdapter.notifyDataSetChanged();
        updateTitle(mActionsAdapter.getTitle(position));
        updateContent(uri);
        mMenuDrawer.showContent();
	}
	
	@Override
    public void onBackPressed() {
		if (mMenuDrawer.isActionsShown()) {
        	mMenuDrawer.showContent();
		} else {
        	super.onBackPressed();
        }
    }
	
    public void onActionsButtonClick(View view) {
        if (mMenuDrawer.isActionsShown())
        	mMenuDrawer.showContent();
        else
        	mMenuDrawer.showActions();
    }
    
    protected void updateTitle(String title) {
    	((TextView)findViewById(R.id.tv_header_title)).setText(title);
    }

    protected void updateContent(Uri uri) {
        final Fragment fragment;
        final String tag;

        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction tr = fm.beginTransaction();

        if (currentContentFragmentTag != null) {
            final Fragment currentFragment = fm.findFragmentByTag(currentContentFragmentTag);
            if (currentFragment != null)
                tr.hide(currentFragment);
        }

        if (MainFragment.URI.equals(uri)) {
            tag = MainFragment.TAG;
            final Fragment foundFragment = fm.findFragmentByTag(tag);
            if (foundFragment != null) {
                fragment = foundFragment;
            } else {
                fragment = new MainFragment();
                ((MainFragment)fragment).setSplash(mShowSplash);
                mShowSplash = false;
            }
        } else if (ProfileFragment.URI.equals(uri)) {
            tag = ProfileFragment.TAG;
            final Fragment foundFragment = fm.findFragmentByTag(tag);
            if (foundFragment != null) {
                fragment = foundFragment;
            } else {
                fragment = new ProfileFragment();
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
            BoardManager.getInstance(getBaseContext()).onBoadClicked(uri.getHost());
        }

        if (fragment.isAdded()) {
            tr.show(fragment);
        } else {
            tr.replace(R.id.content, fragment, tag);
        }

        tr.commit();

        currentUri = uri;
        currentContentFragmentTag = tag;
    }
    
    public static ImageWorker getImageWorker() {
        return mImageWorker;
    }
    
    protected void getPreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        mRunOnce = pref.getBoolean("run_once", false);
    }
     
    protected void savePreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("run_once", mRunOnce);
        editor.commit();
    }
     
    protected void removePreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("run_once");
        editor.commit();
    }
     
    protected void removeAllPreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
    
    protected Runnable mMenuDrawerOpenRunnable = new Runnable() {
		@Override
		public void run() {
			mMenuDrawer.showActions();
			mHandler.postDelayed(mMenuDrawerCloseRunnable, 2000);
		}
    };
    
    protected Runnable mMenuDrawerCloseRunnable = new Runnable() {
		@Override
		public void run() {
			mMenuDrawer.showContent();
		}
    };
    
}