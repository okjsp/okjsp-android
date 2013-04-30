/*
  
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*/

package net.okjsp.acv_adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.okjsp.R;
import net.okjsp.data.BoardRank;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ActionsAdapter extends BaseAdapter {
	protected static final String TAG = "ActionsAdapter";
	protected static final boolean DEBUG_LOG = false;
	
	protected static final int VIEW_TYPE_CATEGORY = 0;
	protected static final int VIEW_TYPE_SETTINGS = 1;
	protected static final int VIEW_TYPE_SITES = 2;
	protected static final int VIEW_TYPE_PROFILE = 3;
	protected static final int VIEW_TYPES_COUNT = 4;

	protected final LayoutInflater mInflater;

	protected final String[] mTitles;
	protected final String[] mUrls;
	protected final TypedArray mIcons;
	protected ArrayList<ActionItem> mActionList = new ArrayList<ActionItem>();
	protected BoardRank mBoardRank;
	protected int mSelected = -1;

    public ActionsAdapter(Context context, BoardRank boardRank) {
        mInflater = LayoutInflater.from(context);
        mBoardRank = boardRank;

        final Resources res = context.getResources();
        mTitles = res.getStringArray(R.array.actions_names);
        mUrls = res.getStringArray(R.array.actions_links);
        mIcons = res.obtainTypedArray(R.array.actions_icons);
        
        for(int i = 0; i < mTitles.length; i++) {
        	ActionItem ai = new ActionItem(mTitles[i], mUrls[i], i);
        	if (ai.uri.getScheme().equals("board")) {
            	mActionList.add(ai);
        	}
        }
        
		Collections.sort(mActionList, comparator);
		
		if (DEBUG_LOG) {
			int count = 0;
			for(ActionItem ai : mActionList) {
				Log.d(TAG, "[" + count++ + "]:" + ai.title + ", " + mBoardRank.getClickCount(ai.uri.getHost()));
			}
		}
    }

    @Override
    public int getCount() {
        return mUrls.length;
    }

    @Override
    public Uri getItem(int position) {
    	int index = position;
        if (position >= 2 && position < (mActionList.size() + 2)) {
        	index = mActionList.get(position - 2).index;
        } 
    	
        return Uri.parse(mUrls[index]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);

        final ViewHolder holder;
        if (convertView == null) {
			switch (type) {
			case VIEW_TYPE_PROFILE:
				convertView = mInflater.inflate(R.layout.activity_main_inc_profile, parent, false);
				break;
			case VIEW_TYPE_CATEGORY:
				convertView = mInflater.inflate(R.layout.acv_category_list_item, parent, false);
				break;
			default:
				convertView = mInflater.inflate(R.layout.acv_action_list_item, parent, false);
				break;
			}

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position >= 2 && position < (mActionList.size() + 2)) {
        	holder.text.setText(mActionList.get(position - 2).title);
        } else {
            holder.text.setText(mTitles[position]);
        }
        
        if (mSelected == position) {
        	holder.text.setBackgroundColor(Color.YELLOW);
        	holder.text.setTextColor(Color.BLACK);
        	holder.text.setTypeface(null, Typeface.BOLD);
        } else {
        	holder.text.setBackgroundColor(Color.DKGRAY);
        	holder.text.setTextColor(Color.WHITE);
        	holder.text.setTypeface(null, Typeface.NORMAL);
        }
        
        switch (type) {
            case VIEW_TYPE_CATEGORY:
            case VIEW_TYPE_PROFILE:
                break;
            default:
            	int index = position;
                if (position >= 2 && position < (mActionList.size() + 2)) {
                	index = mActionList.get(position - 2).index;
                } 
                /*final Drawable icon = mIcons.getDrawable(index);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                holder.text.setCompoundDrawables(icon, null, null, null);*/
                break;
        }
        
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        final Uri uri = Uri.parse(mUrls[position]);
        final String scheme = uri.getScheme();

        if ("category".equals(scheme)) return VIEW_TYPE_CATEGORY;
        else if ("settings".equals(scheme)) return VIEW_TYPE_SETTINGS;
        else if ("profile".equals(scheme))return VIEW_TYPE_PROFILE;
        
        return VIEW_TYPE_SITES;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != VIEW_TYPE_CATEGORY;
    }
    
    public String getTitle(int position) {
    	int index = position;
        if (position >= 2 && position < (mActionList.size() + 2)) {
        	index = mActionList.get(position - 2).index;
        } 
    	
        return mTitles[index];
    }
    
    public void setSelected(int position) {
    	mSelected = position;
    }
    
    public void recycle() {
    	if (mIcons != null) mIcons.recycle();
    }

    protected static class ViewHolder {
        TextView text;
    }
    
	private final Comparator<ActionItem> comparator = new Comparator<ActionItem>() {
		@Override
		public int compare(ActionItem obj1, ActionItem obj2) {
			int result = mBoardRank.getClickCount(obj2.uri.getHost()) - mBoardRank.getClickCount(obj1.uri.getHost());
			return (result == 0) ? (int)(mBoardRank.getTime(obj2.uri.getHost()) - mBoardRank.getTime(obj1.uri.getHost())) : result;
		}
	};	

	public void setBoardRank(BoardRank boardRank) {
		mBoardRank = boardRank;
	}
	
    protected class ActionItem {
    	String title;
    	Uri uri;
    	int index;

    	public ActionItem() {
    	}
    	
    	public ActionItem(String _title, String url, int _index) {
    		title = _title;
    		uri = Uri.parse(url);
    		index = _index;
    	}
    }
}
