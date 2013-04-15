package net.okjsp;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.okjsp.data.Post;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.BaseAdapter;

public class ViewPostActivity extends ListActivity {
	protected static final String TAG = "ViewPostActivity";
	protected static final int MSG_PARSE_PAGE_DONE = 1;
	
	protected Post mPostInfo;
	protected String mPostBody;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpost);
        
        if (getIntent() == null || !getIntent().hasExtra("post")) {
        	Log.e(TAG, "Intent or Extra parameters are NULL!!");
        	finish();
        }
        
        setListAdapter(new PostAdapter());
        
        mPostInfo = getIntent().getExtras().getParcelable("post");
        Log.d(TAG, "title:" + mPostInfo.getTitle());
        mMainThread.start();
    }
	
	protected String getHtmlBody() {
        String htmlData="<html>\n";
        htmlData+="<header>\n";
        htmlData+="<script language='javascript'>\n";
        htmlData+="function command(command){\n";
        htmlData+="   window.android.webCommand(command);\n";
        htmlData+="}\n";
        htmlData+="</script>\n";
        htmlData+="<title>\n";
        htmlData+="i'm title";
        htmlData+="</title>\n";
        htmlData+="<style>\n";
        htmlData+="body {margin:2px;padding:2px;}\n";
        htmlData+="#table {margin-top:0px;padding-left:0px;padding-right:0px;}\n";
        htmlData+="#table_a {border-bottom :1px solid #000000;}\n";
        htmlData+="#table_a td {font:15px \"고딕\",arial;}\n";
        htmlData+="#table_a td.a200 {font-weight:bold}\n";
        htmlData+="#table_a td.a300 {text-align:right;}\n";
        htmlData+="#table_b {background-color:#c2d3fc;}\n";
        htmlData+="#table_b td {font:15px \"고딕\",arial;}\n";
        htmlData+="#table_b td.a200 {font-weight:bold}\n";
        htmlData+="#table_b td.a300 {text-align:right;}\n";
        htmlData+="#table_c {background-color:#f8f8ff;}\n";
        htmlData+="#table_c td {font:15px \"고딕\",arial;}\n";
        htmlData+="#table_c td.a200 {font-weight:bold}\n";
        htmlData+="#table_c td.a300 {text-align:right;}\n";
        htmlData+=".xe_viewMemo {font:14px 굴림; color:#000; word-break:break-all;}\n";
        htmlData+=".xeComment_name {font:14px 굴림; color:#000000;}\n";
        htmlData+=".xeComment_memo {font:15px 굴림; color:#000;word-break:break-all;line-height:140%;vertical-align:top; border-bottom:1px dashed #202020;}\n";
        htmlData+=".xe_viewDate {font:bold 11px tahoma; color:#696969;}\n";
        
        //이미지 크기 자동조절..(최고!!! ㅋ) 
        htmlData+=".resContents      img { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        htmlData+=".commentContents  img { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        htmlData+=".attachedImage    img { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        htmlData+=".imageWidth       img { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        htmlData+=".imageWidth       object { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        htmlData+=".imageWidth       embed { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        htmlData+=".imageWidth       iframe { max-width:100%; width: expression(this.width > 290 ? 100%: true); }\n";
        
        htmlData+="</style>\n";
        htmlData+="</header>\n";
        
        htmlData+="<body>\n";
        htmlData+="<table id=table_b width=100%>\n";
        htmlData+="<td class=a200 width='40%'><img src='" + mPostInfo.getProfileImageUrl()
        		+"' width='40'><span class='xeComment_name'><b>" + mPostInfo.getWriterName() + "</b></span></td>\n";
        htmlData+="<td class=a300 width='60%' align='right' valign='bottom'><span class='xe_viewDate'>("
        		+ mPostInfo.getReadCount() + "," + mPostInfo.getTimeStamp() + ")</span></td>\n";
        htmlData+="</table>\n";
        
        htmlData+="<div style='padding:4px;border-bottom:1px dashed #202020;background-color:f8f8ff'>" + mPostBody + "</div>\n";
        
        //htmlData+=this.makeCommentHtml(commentArray)+"\n"; //코멘트정보..
        
        htmlData+="</body>\n";
        htmlData+="</html>\n";
        
        return htmlData;
	}
	
    protected Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case MSG_PARSE_PAGE_DONE:
    			((BaseAdapter)getListAdapter()).notifyDataSetChanged();
    			break;
    		}
    	}
    };
    
    protected Thread mMainThread = new Thread() {
		@Override
		public void run() {
			try {
				Source source = new Source(new URL(mPostInfo.getPostUrl()));
				source.fullSequentialParse();
				
				Element table = source.getAllElements(HTMLElementName.TABLE).get(0);
				List<Element> tr_list = table.getAllElements(HTMLElementName.TR);
				for(int i = 0; i < tr_list.size(); i++) {
					//Log.e(TAG, "[" + i + "]:" + tr_list.get(i).toString());
				}
				
				if (tr_list.size() > 4) {
					mPostBody = tr_list.get(4).toString();
					int start = mPostBody.indexOf("</script>") + "</script>".length();
					int end = mPostBody.indexOf("<iframe");
					mPostBody = mPostBody.substring(start, end);
					
					Log.e(TAG, mPostBody);
				}
				
				Message.obtain(mHandler, MSG_PARSE_PAGE_DONE).sendToTarget();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	public final class JavaScriptExtention{
		public void webCommand(final String cmd) {
			Log.d(TAG, "webCommand(" + cmd + ")");
			if (cmd.startsWith("DeleteComment")) {
			}
		}	  
	}
	
	protected class PostAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return TextUtils.isEmpty(mPostBody) ? 0 : 4;
		}

		@Override
		public Object getItem(int position) {
			return mPostBody;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (!TextUtils.isEmpty(mPostBody)) {
				LayoutInflater inflater = getLayoutInflater();
				if (position == 0) {
					if (convertView == null || convertView.getId() != R.layout.activity_viewpost_content_item) {
						convertView = inflater.inflate(R.layout.activity_viewpost_content_item, null);
					}
					WebView webView = (WebView) convertView;
					webView.loadDataWithBaseURL(null, mPostBody, "text/html", "utf-8", null);
					webView.getSettings().setJavaScriptEnabled(true);
					webView.addJavascriptInterface(new JavaScriptExtention(), "android");
					webView.setWebChromeClient(new WebChromeClient() {
						public void onProgressChanged(WebView view, int progress) {
							Log.e(TAG, "onProgressChanged(" + progress + ")");
						}
					 });
				} else {
					convertView = inflater.inflate(R.layout.fragment_main_list_item, null);
				}
				return convertView;
			}
			
			return convertView;
		}
		
	}
}
