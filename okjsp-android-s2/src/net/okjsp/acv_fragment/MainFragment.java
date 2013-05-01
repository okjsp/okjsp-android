package net.okjsp.acv_fragment;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.okjsp.Const;
import net.okjsp.MainActivity;
import net.okjsp.R;
import net.okjsp.ViewPostActivity;
import net.okjsp.data.Post;
import net.okjsp.imageloader.ImageWorker;
import net.okjsp.provider.DbConst;
import net.okjsp.provider.OkjspProvider;
import net.okjsp.util.Log;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.PullToRefreshListView;

public class MainFragment extends Fragment implements Const, DbConst {
    public static final String TAG = MainFragment.class.getSimpleName();
    public static final boolean DEBUG_LOG = true;
    
    protected static final int MSG_PARSE_PAGE_DONE = 1;
    protected static final int ANIMATION_FADEOUT_DURATION = 600;
    
    protected static final int POST_TYPE_NOTICE = 1;
    protected static final int POST_TYPE_RECENT = 2;
    
    protected static final String SCHEME = "board";
    protected static final String AUTHORITY = "recent";
    public static final Uri URI = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).build();

    protected Uri mUri;
    protected View mView;
    protected PullToRefreshListView mPtrView;
    protected ArrayList<Post> mNoticeList = new ArrayList<Post>();
    protected ArrayList<Post> mRecentPostList = new ArrayList<Post>();
    protected PostAdapter mPostAdapter;
    protected ImageWorker mImageWorker = MainActivity.getImageWorker();
    protected ParsePageThread mMainThread;
    
    protected boolean mShowSplash = true;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.fragment_main, container, false);
    	mPostAdapter = new PostAdapter(getBaseContext(), R.layout.fragment_main_list_item, mRecentPostList);
    	//setListAdapter(mPostAdapter);

    	if (!mShowSplash) {
    		mView.findViewById(R.id.iv_splash).setVisibility(View.GONE);
    	}
    	
    	mPtrView = (PullToRefreshListView)mView.findViewById(R.id.listview);
		final ListView actualListView = mPtrView.getRefreshableView();

		ViewCompat.setOverScrollMode(actualListView, ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS);

		actualListView.setFadingEdgeLength(2);
		actualListView.setCacheColorHint(0x00000000);
		actualListView.setDividerHeight(0);
		actualListView.setLongClickable(true);
		actualListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});
		actualListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				boolean consumed = true;
				return consumed;
			}
		});
		actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				position--; // position start from '1' not '0' at this method because of pulltorefresh header?
				Log.i(TAG, "onListItemClick: " + position + ", " + mRecentPostList.get(position).getUrl());
		        Intent intent = new Intent(getActivity(), ViewPostActivity.class);
		        intent.putExtra("post", mRecentPostList.get(position));
		        startActivity(intent);
			}
		});

		mPtrView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				mRecentPostList.clear();
				mMainThread = new ParsePageThread();
				mMainThread.start();
			}

			@Override
			public void onUpdate() {

			}
		});
		actualListView.setAdapter(mPostAdapter);

		mRecentPostList.clear();
		loadPostList();
		mMainThread = new ParsePageThread();
		mMainThread.start();
    	
        return mView;
    }

   /* @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(TAG, "onListItemClick: " + position + ", " + mRecentPostList.get(position).getPostUrl());
        Intent intent = new Intent(getActivity(), ViewPostActivity.class);
        intent.putExtra("post", mRecentPostList.get(position));
        startActivity(intent);
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mRecentPostList.get(position).getPostUrl())));
    }*/
    
    public void setUri(Uri uri) {
    	mUri = uri;
    	
		Log.e(TAG, "getLastPathSegment(): " + mUri.getLastPathSegment() 
			   + "\ngetEncodedPath()    : " + mUri.getEncodedPath()
			   + "\ngetHost()           : " + mUri.getHost());
		
    }
    
    public void setSplash(boolean show) {
    	mShowSplash = show;
    }
    
    protected Context getBaseContext() {
    	return getActivity();
    }
    
    protected void loadPostList() {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	Cursor c = cr.query(OkjspProvider.POST_URI, OkjspProvider.TablePost.PROJECTION_ALL, 
    			null, null, FIELD_CREATED_AT + " DESC");
    	
    	if (DEBUG_LOG) Log.d("loadPostList(): " + c.getCount());
    	
    	while(c.moveToNext()) {
    		Post post = new Post();
    		post.setId(c.getInt(c.getColumnIndex(FIELD_POST_ID)));
    		post.setUrl(c.getString(c.getColumnIndex(FIELD_POST_URL)));
    		post.setTitle(c.getString(c.getColumnIndex(FIELD_POST_TITLE)));
    		post.setBoardName(c.getString(c.getColumnIndex(FIELD_BOARD_NAME)));
    		post.setWriterName(c.getString(c.getColumnIndex(FIELD_POST_WRITER_NAME)));
    		post.setProfileImageUrl(c.getString(c.getColumnIndex(FIELD_POST_WRITER_PHOTO_URL)));
    		post.setTimeStamp(c.getString(c.getColumnIndex(FIELD_POST_TIMESTAMP)));
    		if (DEBUG_LOG) {
    			Log.d("[" + post.getId() + "]: " + post.toString());
    		}
    		mRecentPostList.add(post);
    	}
    	c.close();
    }
    
    protected void savePostList(ArrayList<Post> post_list) {
        for(Post post : post_list) {
        	savePost(post);
        }
    }
    
    protected void savePost(Post post) {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	
    	if (isExist(post.getId())) {
    		if (DEBUG_LOG) Log.e("[" + post.getId() + "] " + post.getUrl() + ", " + Uri.parse(post.getUrl()).getLastPathSegment() + " exist!!!");
    		return;
    	}

    	ContentValues values = new ContentValues();
        values.clear();
        values.put(FIELD_POST_ID, post.getId());
        values.put(FIELD_POST_URL, post.getUrl());
        values.put(FIELD_POST_TITLE, post.getTitle());
        if (!TextUtils.isEmpty(post.getBoardUrl())) {
            values.put(FIELD_BOARD_NAME, Uri.parse(post.getBoardUrl()).getHost());
        } else {
        	
        }
        values.put(FIELD_POST_READ_COUNT, post.getReadCount());
        values.put(FIELD_POST_WRITER_NAME, post.getWriterName());
        values.put(FIELD_POST_WRITER_PHOTO_URL, post.getProfileImageUrl());
        values.put(FIELD_POST_TIMESTAMP, post.getTimeStamp());
        values.put(FIELD_CREATED_AT, post.getTime());
        values.put(FIELD_UPDATED_AT, System.currentTimeMillis());
        Uri uri = cr.insert(OkjspProvider.POST_URI, values);
    }
    
    protected long getMaxPostId() {
    	long max_id = 0;

    	ContentResolver cr = getBaseContext().getContentResolver();
		Cursor c = cr.query(OkjspProvider.POST_URI,	new String[] { "MAX(" + FIELD_POST_ID + ")" }, null, null, null);
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
    	Cursor c = cr.query(OkjspProvider.POST_URI, OkjspProvider.TablePost.PROJECTION_ALL, 
    			where, null, null);
    	
    	is_exist = (c.getCount() > 0) ? true : false;
    	
    	c.close();
    	return is_exist;
    }
    
    protected Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case MSG_PARSE_PAGE_DONE:
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
    			mMainThread = null;
    			break;
    		}
    	}
    };
    
    protected class ParsePageThread extends Thread {
		@Override
		public void run() {
			int post_type = -1;
			ArrayList<Post> post_list = new ArrayList<Post>();

			try {
				String url = MAIN_BOARD_URL;
				Source source = new Source(new URL(url));
				source.fullSequentialParse();
				
				Element table = source.getAllElements(HTMLElementName.TABLE).get(0);
				Element sub_table = table.getAllElements(HTMLElementName.TABLE).get(1);
				Element target_table = sub_table.getAllElements(HTMLElementName.TABLE).get(1);
				// Log.e(TAG, "target_table: " + target_table.toString());
				/*for(int i = 0; i < sub_tables.size(); i++) {
					Element e = sub_tables.get(i);
					Log.e(TAG, "[" + i + "]: " + e.toString());
				}*/
				
				List<Element> tr_list = target_table.getAllElements(HTMLElementName.TR);
				for(Element tr : tr_list) {
					Post post = new Post();
					// Log.d(TAG, "[" + "]:" + tr.toString());
					List<Element> td_list = tr.getAllElements(HTMLElementName.TD);
					for (Element td : td_list) {
						String attr_value = td.getAttributeValue("class");
						if (!TextUtils.isEmpty(attr_value)) {
							String value = td.getTextExtractor().toString();
							// Log.d(TAG, "[" + mRecentPostList.size() + "]:" + attr_value + " - " + value);
							if ("ref tiny".equalsIgnoreCase(attr_value)) {
								try {
									post.setId(Integer.valueOf(value));
								} catch (NumberFormatException e) {
									post.setId(-1);
								}
							} else if ("when tiny".equalsIgnoreCase(attr_value)) {
								post.setTimeStamp(td.getAttributeValue("title"));
								//Log.d(TAG, "     ----" + td.getAttributeValue("title"));
							} else if ("subject".equalsIgnoreCase(attr_value)) {
								post.setTitle(value);
								List<Element> el_list = td.getAllElements(HTMLElementName.A);
								if (el_list != null && el_list.size() > 0) {
									Element href = el_list.get(0);
									//Log.d(TAG, "     ----" + href.getAttributeValue("href"));
									post.setUrl(href.getAttributeValue("href"));
								}
							} else if ("id".equalsIgnoreCase(attr_value)) {
								List<Element> el_list = td.getAllElements(HTMLElementName.IMG);
								if (el_list != null && el_list.size() > 0) {
									Element href = el_list.get(0);
									//Log.d(TAG, "     ----" + href.getAttributeValue("src"));
									post.setProfileImageUrl(href.getAttributeValue("src"));
								}
							} else if ("writer".equalsIgnoreCase(attr_value)) {
								if (TextUtils.isEmpty(post.getWriterName())) {
									post.setWriterName(value);
								} else {
									Log.w(TAG, "Writer field is NOT EMPTY!! " + post.getWriterName());
								}
							} else if ("read tiny".equalsIgnoreCase(attr_value)) {
								post.setReadCount(Integer.valueOf(value));
							} else if ("th".equalsIgnoreCase(attr_value)) {
								// Log.e(TAG, value);
								if ("공지사항".equals(value)) post_type = POST_TYPE_NOTICE;
								else if ("전체 게시판".equals(value)) post_type = POST_TYPE_RECENT;
							}
						} else {
							List<Element> el_list = td.getAllElements(HTMLElementName.A);
							if (el_list != null && el_list.size() > 0) {
								//Log.e(TAG, "[" + i + "]:" + attr_value + " - " + td.getTextExtractor().toString());
								Element href = el_list.get(0);
								//Log.d(TAG, "     ----" + href.getAttributeValue("href") + ", " + td.getTextExtractor().toString());
								post.setBoardName(td.getTextExtractor().toString()).setBoardUrl(href.getAttributeValue("href"));
							}
						}
					}
					
					if (post.isValid()) {
						switch(post_type) {
						case POST_TYPE_NOTICE:
							//mNoticeList.add(post);
							break;
						case POST_TYPE_RECENT:
							post_list.add(post);
							break;
						}
					} else if (!post.isEmpty()) {
						Log.w(TAG, "INVALID POST:" + post.toString());
					}
				}

				Log.d(TAG, "Post Count:" + post_list.size());
				savePostList(post_list);
				/*for(Post p : mRecentPostList) {
					Log.d(TAG, "--------------------------------------------------");
					Log.i(TAG, "Post Id    :" + p.getPostId());
					Log.i(TAG, "Board Name :" + p.getBoardName());
					Log.i(TAG, "Board URL  :" + p.getBoardUrl());
					Log.i(TAG, "Title      :" + p.getTitle());
					Log.i(TAG, "Post URL   :" + p.getPostUrl());
					Log.i(TAG, "Writer Name:" + p.getWriterName());
					Log.i(TAG, "Profile URL:" + p.getWriterProfileUrl());
					Log.i(TAG, "Read Count :" + p.getReadCount());
					Log.i(TAG, "Time stamp :" + p.getTimeStamp());
				}*/
				
				Message.obtain(mHandler, MSG_PARSE_PAGE_DONE).sendToTarget();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	protected class PostAdapter extends ArrayAdapter<Post> {
		public PostAdapter(Context context, int textViewResourceId, List<Post> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
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

			Post post = mRecentPostList.get(position);
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
