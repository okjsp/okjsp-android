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
import net.okjsp.util.Log;

import org.apache.http.client.ClientProtocolException;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class ParseMainPageThread extends Thread implements Const {
	protected static final String TAG = "ParseMainPageThread";
	protected static final boolean DEBUG_LOG = true;
	
	protected final Handler mHandler;
	protected final Uri mUri;
	protected ArrayList<Post> mPostList = new ArrayList<Post>();
	
	public ParseMainPageThread(Uri uri, Handler handler) {
		mHandler = handler;
		mUri = uri;
	}
	
	@Override
	public void run() {
		int post_type = -1;

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
						mPostList.add(post);
						break;
					}
				} else if (!post.isEmpty()) {
					Log.w(TAG, "INVALID POST:" + post.toString());
				}
			}

			Log.d(TAG, "Post Count:" + mPostList.size());
			//savePostList(post_list);
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
			
			Message.obtain(mHandler, MSG_PARSE_MAIN_PAGE_DONE, mPostList).sendToTarget();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
