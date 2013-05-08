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

import net.okjsp.R;
import net.okjsp.data.Board;
import net.okjsp.manager.BoardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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

	protected ArrayList<Board> mActionList;
	protected int mSelected = -1;

    public ActionsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mActionList = BoardManager.getInstance(context).getBoardList(); 
    }

    @Override
    public int getCount() {
        return mActionList.size();
    }

    @Override
    public Uri getItem(int position) {
        return mActionList.get(position).getUri();
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

       	holder.text.setText(mActionList.get(position).getTitle());
        
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
        final Uri uri = mActionList.get(position).getUri();
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
        return mActionList.get(position).getTitle();
    }
    
    public void setSelected(int position) {
    	mSelected = position;
    }
    
    public void recycle() {
    }

    protected static class ViewHolder {
        TextView text;
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
