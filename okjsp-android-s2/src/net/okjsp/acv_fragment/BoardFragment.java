package net.okjsp.acv_fragment;

import java.util.ArrayList;
import java.util.List;

import net.okjsp.Const;
import net.okjsp.MainActivity;
import net.okjsp.R;
import net.okjsp.ViewPostActivity;
import net.okjsp.data.Post;
import net.okjsp.imageloader.ImageWorker;
import net.okjsp.provider.CacheProvider;
import net.okjsp.provider.DbConst;
import net.okjsp.thread.ParseBoardPageThread;
import net.okjsp.thread.ParseMainPageThread;
import net.okjsp.util.Log;
import net.okjsp.util.Utils;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.handmark.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.PullToRefreshListView;

public class BoardFragment extends SherlockFragment implements Const, DbConst {
    public static final String TAG = BoardFragment.class.getSimpleName();
    public static final boolean DEBUG_LOG = true;
    public static final boolean DEBUG_LOG_VERBOSE = false;
    
    // board://board
    protected static final String SCHEME = "board";
    protected static final String AUTHORITY = "recent";
    public static final Uri DEFAULT_URI = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).build();
    public static final Uri URI = DEFAULT_URI;
    
    protected static final int ANIMATION_FADEOUT_DURATION = 600;

    protected View mView;
    protected PullToRefreshListView mPtrView;
    protected ArrayList<Post> mPostList = new ArrayList<Post>();
    protected PostAdapter mPostAdapter;
    protected ImageWorker mImageWorker = MainActivity.getImageWorker();
    protected Thread mParseThread;
    
    protected boolean mShowSplash = true;
    
    public static BoardFragment newInstance(CharSequence uri_host) {
    	BoardFragment f = new BoardFragment();
        Bundle b = new Bundle();
        b.putCharSequence("board_uri_host", uri_host);
		if (DEBUG_LOG) Log.d("uri_host: " + uri_host);
        
        f.setArguments(b);
        return f;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.fragment_main, container, false);
    	mPostAdapter = new PostAdapter(getBaseContext(), R.layout.fragment_main_list_item, mPostList);
    	//setListAdapter(mPostAdapter);

    	if (!mShowSplash) {
    		mView.findViewById(R.id.iv_splash).setVisibility(View.GONE);
    	}
    	
    	mPtrView = (PullToRefreshListView)mView.findViewById(R.id.listview);
		final ListView actualListView = mPtrView.getRefreshableView();

		ViewCompat.setOverScrollMode(actualListView, ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS);

		registerForContextMenu(actualListView);
		actualListView.setFadingEdgeLength(2);
		actualListView.setCacheColorHint(0x00000000);
		actualListView.setDividerHeight(0);
		actualListView.setLongClickable(true);
		actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				position--; // position start from '1' not '0' at this method because of pulltorefresh header?
				if (DEBUG_LOG) Log.i(TAG, "onListItemClick: " + position + ", " + mPostList.get(position).getUrl());
				
				setAsRead(mPostList.get(position).getId());
				mPostList.get(position).setAsRead(true);
				mPostAdapter.notifyDataSetChanged();
				
		        Intent intent = new Intent(getActivity(), ViewPostActivity.class);
		        intent.putExtra("post", mPostList.get(position));
		        startActivity(intent);
			}
		});

		mPtrView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				//mThread = new ParsePageThread();
				//mThread.start();
			}

			@Override
			public void onUpdate() {

			}
		});
		actualListView.setAdapter(mPostAdapter);

		if (DEBUG_LOG) Log.d("uri_host: " + getUriHost());
		
		String uri_host = getUriHost();
		if ("recent".equals(uri_host)) {
			loadPostList();
			mParseThread = new ParseMainPageThread(Uri.parse(BOARD_URI_SCHEME + uri_host), mHandler);
			mParseThread.start();
		} else {
			mView.findViewById(R.id.iv_splash).setVisibility(View.GONE);
			mParseThread = new ParseBoardPageThread(Uri.parse(BOARD_URI_SCHEME + uri_host), mHandler);
			mParseThread.start();
		}
    	
		setHasOptionsMenu(true);
		
        return mView;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuinfo) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.context_menu_post, menu);
        menu.setHeaderTitle("Select");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		;
		switch (item.getItemId()) {
		case R.id.action_share:
			Intent i=new Intent(android.content.Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, mPostList.get(info.position).getTitle());
			i.putExtra(Intent.EXTRA_TEXT, mPostList.get(info.position).getUrl());
			startActivity(Intent.createChooser(i, getBaseContext().getString(R.string.action_share)));
			return true;
		}
		return false;
    }    

    public String getUriHost() {
    	Bundle bundle = getArguments();
    	return bundle.getString("board_uri_host");
    }
    
    public void setSplash(boolean show) {
    	mShowSplash = show;
    }
    
    protected Context getBaseContext() {
    	return getActivity();
    }
    
    @SuppressWarnings("unused")
	protected void loadPostList() {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	Cursor c = cr.query(CacheProvider.POST_URI, CacheProvider.TablePost.PROJECTION_ALL, 
    			null, null, FIELD_CREATED_AT + " DESC");
    	
    	if (DEBUG_LOG) Log.d("loadPostList(): " + c.getCount());
    	
		mPostList.clear();
    	while(c.moveToNext()) {
    		Post post = new Post();
    		post.setId(c.getInt(c.getColumnIndex(FIELD_POST_ID)));
    		post.setUrl(c.getString(c.getColumnIndex(FIELD_POST_URL)));
    		post.setTitle(c.getString(c.getColumnIndex(FIELD_POST_TITLE)));
    		post.setBoardName(c.getString(c.getColumnIndex(FIELD_BOARD_DISPLAY_NAME)));
    		post.setBoardUrl(c.getString(c.getColumnIndex(FIELD_BOARD_URI_HOST)));
    		post.setWriterName(c.getString(c.getColumnIndex(FIELD_POST_WRITER_NAME)));
    		post.setProfileImageUrl(c.getString(c.getColumnIndex(FIELD_POST_WRITER_PHOTO_URL)));
    		post.setTimeStamp(c.getString(c.getColumnIndex(FIELD_POST_TIMESTAMP)));
    		post.setReadCount(c.getInt(c.getColumnIndex(FIELD_POST_CLICK_COUNT)));
    		post.setAsRead(c.getInt(c.getColumnIndex(FIELD_POST_ISREAD)) > 0 ? true : false);
    		
    		if (DEBUG_LOG && DEBUG_LOG_VERBOSE) {
    			Log.d("[" + post.getId() + "]: " + post.toString());
    		}
    		mPostList.add(post);
    	}
    	c.close();
    }
    
    protected void savePostList(ArrayList<Post> post_list) {
        for(Post post : post_list) {
        	savePost(post);
        }
    }
    
    @SuppressWarnings("unused")
	protected void savePost(Post post) {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	
    	if (isExist(post.getId())) {
    		if (DEBUG_LOG && DEBUG_LOG_VERBOSE) Log.e("[" + post.getId() + "] " + post.getUrl() + ", " + Uri.parse(post.getUrl()).getLastPathSegment() + " exist!!!");
    		return;
    	}

    	ContentValues values = new ContentValues();
        values.clear();
        values.put(FIELD_POST_ID, post.getId());
        values.put(FIELD_POST_URL, post.getUrl());
        values.put(FIELD_POST_TITLE, post.getTitle());
        values.put(FIELD_BOARD_URI_HOST,post.getBoardUri());
        values.put(FIELD_BOARD_DISPLAY_NAME, post.getBoardName());
        values.put(FIELD_POST_CLICK_COUNT, post.getReadCount());
        values.put(FIELD_POST_WRITER_NAME, post.getWriterName());
        values.put(FIELD_POST_WRITER_PHOTO_URL, post.getProfileImageUrl());
        values.put(FIELD_POST_TIMESTAMP, post.getTimeStamp());
        values.put(FIELD_POST_ISREAD, post.isRead() ? 1 : 0);
        values.put(FIELD_CREATED_AT, post.getTime());
        values.put(FIELD_UPDATED_AT, System.currentTimeMillis());
        Uri uri = cr.insert(CacheProvider.POST_URI, values);
        
        if (DEBUG_LOG && DEBUG_LOG_VERBOSE) {
        	Log.v("savePost():" + uri.toString());
        }
    }
    
    protected long getMaxPostId() {
    	long max_id = 0;

    	ContentResolver cr = getBaseContext().getContentResolver();
		Cursor c = cr.query(CacheProvider.POST_URI,	new String[] { "MAX(" + FIELD_POST_ID + ")" }, null, null, null);
		try {
			c.moveToFirst();
			max_id = c.getInt(0);
		} finally {
			c.close();
		}
		
		if (DEBUG_LOG) {
			Log.d("max post id: " + max_id);
		}
		
    	return max_id;
    }
    
    protected boolean isExist(int post_id) {
    	boolean is_exist = false;

    	ContentResolver cr = getBaseContext().getContentResolver();
    	String where = FIELD_POST_ID + " = " + post_id;
    	Cursor c = cr.query(CacheProvider.POST_URI, CacheProvider.TablePost.PROJECTION_ALL, 
    			where, null, null);
    	
    	is_exist = (c.getCount() > 0) ? true : false;
    	
    	c.close();
    	return is_exist;
    }
    
    protected void setAsRead(int post_id) {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	String where = FIELD_POST_ID + " = " + post_id;
    	ContentValues cv = new ContentValues();
    	cv.put(FIELD_POST_ISREAD, 1);
    	cr.update(CacheProvider.POST_URI, cv, where, null);
    }
    
    protected Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case MSG_PARSE_MAIN_PAGE_DONE:
    			loadPostList();
    			mPtrView.onRefreshComplete();
    			mPostAdapter.notifyDataSetChanged();
    			Animation fadeOut = new AlphaAnimation(1, 0);
    		    fadeOut.setDuration(ANIMATION_FADEOUT_DURATION);
    		    fadeOut.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						mView.findViewById(R.id.iv_splash).setVisibility(View.GONE);
					}
				});
    		    
    		    if (mShowSplash) {
       		    	mView.findViewById(R.id.iv_splash).startAnimation(fadeOut);
       		    	mShowSplash = false;
    		    }
    			break;
    		case MSG_PARSE_BOARD_PAGE_DONE:
    			if (msg.obj instanceof ArrayList<?>) {
    				mPostList.clear();
        			mPostList.addAll((ArrayList<Post>)msg.obj);
    			}
    			mPtrView.onRefreshComplete();
    			mPostAdapter.notifyDataSetChanged();
    			break;
    		}
    	}
    };
    
	protected class PostAdapter extends ArrayAdapter<Post> {
		public PostAdapter(Context context, int textViewResourceId, List<Post> objects) {
			super(context, textViewResourceId, objects);
		}

		@SuppressLint("NewApi") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = LayoutInflater.from(getBaseContext());
				rowView = inflater.inflate(R.layout.fragment_main_list_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.tv_writer = (TextView) rowView.findViewById(R.id.tv_writer);
				viewHolder.tv_title = (TextView) rowView.findViewById(R.id.tv_title);
				viewHolder.tv_timestamp = (TextView) rowView.findViewById(R.id.tv_timestamp);
				viewHolder.tv_board = (TextView) rowView.findViewById(R.id.tv_board);
				viewHolder.iv_profile = (ImageView) rowView.findViewById(R.id.iv_profile);
				rowView.setTag(viewHolder);
			}

			Post post = mPostList.get(position);
			ViewHolder holder = (ViewHolder) rowView.getTag();
			holder.tv_writer.setText(post.getWriterName());
			holder.tv_title.setText(post.getTitle());
			holder.tv_timestamp.setText(post.getTimeStamp());
			if (!TextUtils.isEmpty(post.getBoardName())) {
				holder.tv_board.setText("[" + post.getBoardName() + "]");
			} else {
				holder.tv_board.setText("");
			}
			if (!TextUtils.isEmpty(post.getProfileImageUrl())) {
				mImageWorker.loadImage(post.getProfileImageUrl(), holder.iv_profile);
			} else {
				holder.iv_profile.setImageResource(0);
			}
			if (post.isRead()) {
				holder.tv_title.setTextColor(Color.DKGRAY);
				//holder.tv_title.setTypeface(null, Typeface.NORMAL);
				holder.tv_writer.setTextColor(Color.DKGRAY);
				holder.tv_timestamp.setTextColor(Color.DKGRAY);
				holder.tv_board.setTextColor(Color.DKGRAY);
				if (Utils.checkApiLevel(11)) {
					holder.iv_profile.setAlpha(0.3f);
				} else {
					holder.iv_profile.setAlpha(80);
				}
			} else {
				holder.tv_title.setTextColor(Color.WHITE);
				//holder.tv_title.setTypeface(null, Typeface.BOLD);
				holder.tv_writer.setTextColor(Color.LTGRAY);
				holder.tv_timestamp.setTextColor(Color.LTGRAY);
				holder.tv_board.setTextColor(Color.LTGRAY);
				if (Utils.checkApiLevel(11)) {
					holder.iv_profile.setAlpha(1.0f);
				} else {
					holder.iv_profile.setAlpha(255);
				}
			}

			return rowView;
		}
		
		class ViewHolder {
		    public TextView tv_writer;
		    public TextView tv_title;
		    public TextView tv_timestamp;
		    public TextView tv_board;
		    public ImageView iv_profile;
		}		
	}
}
