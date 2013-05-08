package net.okjsp;

import net.okjsp.acv_adapter.ActionsAdapter;
import net.okjsp.acv_fragment.BoardFragment;
import net.okjsp.acv_fragment.ProfileFragment;
import net.okjsp.imageloader.ImageCache;
import net.okjsp.imageloader.ImageFetcher;
import net.okjsp.imageloader.ImageResizer;
import net.okjsp.imageloader.ImageWorker;
import net.okjsp.manager.BoardManager;
import net.okjsp.util.Log;
import shared.ui.actionscontentview.ActionsContentView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class MainActivity extends SherlockFragmentActivity implements OnClickListener, AdapterView.OnItemClickListener, Const {
	protected static final String TAG = "MainActivity";
	protected static boolean DEBUG_LOG = true;

	protected static final String STATE_URI = "state:uri";
	protected static final String STATE_FRAGMENT_TAG = "state:fragment_tag";
	protected static final String DEFAULT_URI = "board://recent";

	protected ActionsContentView mMenuDrawer;
	protected ActionsAdapter mActionsAdapter;
	protected ListView mActionListView;

	protected Uri currentUri = BoardFragment.URI;
	protected String currentContentFragmentTag = null;

    protected static ImageResizer mImageWorker;
    protected boolean mRunOnce = false;
    protected boolean mShowSplash = true;
    protected Handler mHandler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.activity_main);
        getSherlock().getActionBar().setHomeButtonEnabled(true);
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
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

        if (savedInstanceState != null) {
            currentUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            currentContentFragmentTag = savedInstanceState.getString(STATE_FRAGMENT_TAG);
        }

        updateContent(currentUri);
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(false);
        
        if (!mRunOnce) {
            mHandler.postDelayed(mMenuDrawerOpenRunnable, 1000);
            mRunOnce = true;
        }
    }

    /**{@inheritDoc}*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getSherlock().getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URI, currentUri.toString());
        outState.putString(STATE_FRAGMENT_TAG, currentContentFragmentTag);

        super.onSaveInstanceState(outState);
    }

	@Override
	public void onClick(View v) {
		/*switch(v.getId()) {
		case R.id.iv_menu:
            onActionsButtonClick(v);
			break;
		}*/
	}
    
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long flags) {
        final Uri uri = mActionsAdapter.getItem(position);
        Log.i(TAG, "position:" + position + ", " + uri.toString());
        mActionsAdapter.setSelected(position);
        mActionsAdapter.notifyDataSetChanged();
        updateContent(uri);
        mMenuDrawer.showContent();
	}
	
	@Override
    public void onBackPressed() {
		if (mMenuDrawer.isActionsShown()) {
        	getSherlock().getActionBar().setDisplayHomeAsUpEnabled(false);
        	mMenuDrawer.showContent();
		} else {
        	super.onBackPressed();
        }
    }
	
	/**
	 * Let's the user tap the activity icon to go 'home'.
	 * Requires setHomeButtonEnabled() in onCreate().
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	getSherlock().getActionBar().setDisplayHomeAsUpEnabled(!mMenuDrawer.isActionsShown());
        		if (mMenuDrawer.isActionsShown()) {
                	mMenuDrawer.showContent();
        		} else {
        			mMenuDrawer.showActions();
        		}
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
    public void onActionsButtonClick(View view) {
        if (mMenuDrawer.isActionsShown())
        	mMenuDrawer.showContent();
        else
        	mMenuDrawer.showActions();
    }
    
    @SuppressLint("NewApi") 
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

    	if (DEBUG_LOG) Log.i(TAG, "updateContent:" + uri.toString());

        if (ProfileFragment.URI.equals(uri)) {
            tag = ProfileFragment.TAG;
            final Fragment foundFragment = fm.findFragmentByTag(tag);
            if (foundFragment != null) {
                fragment = foundFragment;
            } else {
                fragment = ProfileFragment.newInstance(uri.getHost());
            }
        } else {
            tag = uri.getHost();
            final Fragment foundFragment = fm.findFragmentByTag(tag);
            if (foundFragment != null) {
                fragment = foundFragment;
            } else {
                fragment = BoardFragment.newInstance(uri.getHost());
            }
            BoardManager.getInstance(getBaseContext()).onBoadClicked(uri.getHost());
        }

        if (fragment.isAdded()) {
            tr.show(fragment);
        } else {
            tr.replace(R.id.content, fragment, tag);
        }

        tr.commit();
        
        String title = BoardManager.getInstance(getBaseContext()).getBoardName(uri.getHost());
    	getActionBar().setTitle(title);

        currentUri = uri;
        currentContentFragmentTag = tag;
    }
    
    public static ImageWorker getImageWorker() {
        return mImageWorker;
    }
    
    public void showProgressBarIndeterminateVisibility(boolean visible){
    	getSherlock().setProgressBarIndeterminate(true);
    	getSherlock().setProgressBarIndeterminateVisibility(visible);
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