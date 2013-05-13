package net.okjsp.thread;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.okjsp.Const;
import net.okjsp.data.Post;

import org.apache.http.client.ClientProtocolException;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class ParseBoardPageThread extends Thread implements Const {
	protected static final String TAG = "ParseBoardPageThread";
	protected static final boolean DEBUG_LOG = false;
	
	protected final Handler mHandler;
	protected final Uri mUri;
	protected ArrayList<Post> mPostList = new ArrayList<Post>();
	
	public ParseBoardPageThread(Uri uri, Handler handler) {
		mHandler = handler;
		mUri = uri;
	}
	
	@Override
	public void run() {
		try {
			String url = MAIN_BOARD_URL;
			if (mUri != null && !"main".equals(mUri.getHost())) {
				url = BBS_BOARD_URL + mUri.getHost();
				if (DEBUG_LOG) Log.d(TAG, "" + url);
			}
			Source source = new Source(new URL(url));
			source.fullSequentialParse();
			
			Element table = source.getAllElements(HTMLElementName.TABLE).get(0);
			List<Element> tr_list = table.getAllElements(HTMLElementName.TR);
			for(int i = 0; i < tr_list.size(); i++) {
				Post post = new Post();
				
				Element tr = tr_list.get(i);
				//Log.e(TAG, "[" + i + "]:" + tr.toString());
				List<Element> td_list = tr.getAllElements(HTMLElementName.TD);
				for (Element td : td_list) {
					String attr_value = td.getAttributeValue("class");
					if (!TextUtils.isEmpty(attr_value)) {
						String value = td.getTextExtractor().toString();
						if (DEBUG_LOG)  Log.e(TAG, "[" + i + "]:" + attr_value + " - " + td.getTextExtractor().toString());
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
							List<Element> el_list = td.getAllElements(HTMLElementName.IMG);
							if (el_list != null && el_list.size() > 0) {
								Element href = el_list.get(0);
								//Log.d(TAG, "     ----" + href.getAttributeValue("src"));
								post.setProfileImageUrl(href.getAttributeValue("src"));
							} else {
								post.setWriterName(value);
							}
						} else if ("read tiny".equalsIgnoreCase(attr_value)) {
							post.setReadCount(Integer.valueOf(value));
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
				if (post.isValid()) mPostList.add(post);
			}

			if (DEBUG_LOG) Log.d(TAG, "Post Count:" + mPostList.size());
			/*for(Post p : mPostList) {
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
			
			Message.obtain(mHandler, MSG_PARSE_BOARD_PAGE_DONE, mPostList).sendToTarget();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
