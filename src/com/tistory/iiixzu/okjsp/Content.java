package com.tistory.iiixzu.okjsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tistory.iiixzu.common.AdamAdViewDelegater;
import com.tistory.iiixzu.common.CommonActivity;
import com.tistory.iiixzu.common.IRequestObserver;
import com.tistory.iiixzu.common.OkjspShare;
import com.tistory.iiixzu.common.RequestAsyncHandler;
import com.tistory.iiixzu.common.Requester;
import com.tistory.iiixzu.common.StringHelper;
import com.tistory.iiixzu.common.SwipeDetector;
/**
 * 게시물.
 * @author iiixzu
 *
 */
public class Content extends CommonActivity implements IRequestObserver{
	
	private Requester requester;
	private Requester requester2;
	private Requester requester3;
	private Requester requester6;
	
	private ImageButton 갱신버튼;
	private boolean isRefresh;
	
	private Adapter 아답터;
	private boolean isWritable;
	private HtmlCleaner xpathParser;
	
	private String htmlData;
	private String commentId;
	
	//뒤로가기제스쳐위한방법..
	private SwipeDetector swipeDetector;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);
        
        Object obj = getLastNonConfigurationInstance();
		if (obj != null) {
		    htmlData=(String)obj;
		    
		    갱신버튼=(ImageButton)this.findViewById(R.id.갱신);
	        
	        Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("boardData");
	        Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
	        TextView tv게시판제목=(TextView)this.findViewById(R.id.게시판제목);
	        TextView tv게시물제목=(TextView)this.findViewById(R.id.prompt);
	        
	        tv게시물제목.setText(contentData.get("subject"));
	        tv게시판제목.setText(boardData.get("menuName"));
	        
	        ListView lv게시물목록=(ListView)this.findViewById(R.id.게시물);
	        
	        if(OkjspShare.getInstance(this).isGoBackGesture()){
	        	swipeDetector=new SwipeDetector(this);
	        }
	        
	        아답터=new Adapter();
	        lv게시물목록.setAdapter(아답터);
	        xpathParser=new HtmlCleaner();
	        
	        아답터.notifyDataSetChanged();
		}else{
	        갱신버튼=(ImageButton)this.findViewById(R.id.갱신);
	        
	        Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("boardData");
	        Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
	        TextView tv게시판제목=(TextView)this.findViewById(R.id.게시판제목);
	        TextView tv게시물제목=(TextView)this.findViewById(R.id.prompt);
	        
	        tv게시물제목.setText(contentData.get("subject"));
	        tv게시판제목.setText(boardData.get("menuName"));
	        
	        ListView lv게시물목록=(ListView)this.findViewById(R.id.게시물);
	        
	        if(OkjspShare.getInstance(this).isGoBackGesture()){
	        	swipeDetector=new SwipeDetector(this);
	        }
	        
	        아답터=new Adapter();
	        lv게시물목록.setAdapter(아답터);
	        xpathParser=new HtmlCleaner();
	        
	        this.refresh(갱신버튼);
		}
	}
	@Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		if(isModified){ //데이터가 수정된적이 있으면 목록에서 처리위해서 이렇게..
    			this.setResult(100);
    		}
    		this.finish();
    		return false;
    	}
    	return false;
    }
	@Override
	public Object onRetainNonConfigurationInstance() {
	    return htmlData;
	}
	private Configuration config;
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        config=newConfig;
        
        Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("boardData");
        Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
        TextView tv게시판제목=(TextView)this.findViewById(R.id.게시판제목);
        TextView tv게시물제목=(TextView)this.findViewById(R.id.prompt);
        
        //가로일때랑 세로일때 제목바를 달리한다.
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	tv게시물제목.setVisibility(View.GONE);
        	tv게시판제목.setText(contentData.get("subject"));
        	
        }else if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
        	tv게시물제목.setVisibility(View.VISIBLE);
        	tv게시판제목.setText(boardData.get("menuName"));
        }
    }
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "글수정");
			menu.add(android.view.Menu.NONE,2, 0, "글삭제");
			menu.add(android.view.Menu.NONE,3, 0, "댓글쓰기");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,4, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	@Override
	public boolean onPrepareOptionsMenu (android.view.Menu menu){
			
		menu.clear();
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "글수정");
			menu.add(android.view.Menu.NONE,2, 0, "글삭제");
			menu.add(android.view.Menu.NONE,3, 0, "댓글쓰기");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,4, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId()==1){//글수정..
			글수정();
		}else if(item.getItemId()==2){//글삭제..
			글삭제();
		}else if(item.getItemId()==3){//댓글쓰기
			Intent intent=new Intent(Content.this, WriteReply.class);
			intent.putExtra("isModify",true);
			startActivityForResult(intent,100);
			overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
		}
		return (super.onOptionsItemSelected(item));
	}
	
	private boolean isModified; //글이 수정되었는지여부..
	@Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode==100&&resultCode==100){ //글수정창에서 넘겨진경우..
    	    
    	    String subject=(String)OkjspShare.getInstance(this).getParameterDic().get("tmpSubject");
	        String content=(String)OkjspShare.getInstance(this).getParameterDic().get("tmpContent");
    	    content=content.replaceAll("\n","<br>");
	        
    	    Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
    	    contentData.put("subject",subject);
    	    contentData.put("content",content);
    	    
    	    TextView tv게시판제목=(TextView)this.findViewById(R.id.게시판제목);
            TextView tv게시물제목=(TextView)this.findViewById(R.id.prompt);
            
    	    if(config==null||config.orientation==Configuration.ORIENTATION_PORTRAIT){
    	    	tv게시물제목.setText(contentData.get("subject"));
            	
            }else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            	tv게시판제목.setText(contentData.get("subject"));
            	
            }
    	    
    	    isModified=true;
    	    
    		this.refresh(갱신버튼);
    	}else if(requestCode==100&&resultCode==200){ //댓글창에서 넘겨진경우..
    		this.refresh(갱신버튼);
    	}
    }
	private void 글수정(){
		LayoutInflater inflater=getLayoutInflater();
		View 암호뷰=(View)inflater.inflate(R.layout.password,null);
        tv암호=(TextView)암호뷰.findViewById(R.id.암호);
		AlertDialog.Builder aDialog = new AlertDialog.Builder(Content.this);
		aDialog.setTitle("게시물을 수정하시겠습니까?");
		aDialog.setMessage("암호를 입력하십시오.");
		aDialog.setView(암호뷰);
		aDialog.setNegativeButton("아니오",null);
		aDialog.setPositiveButton("수정", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
	            
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
				
				String password=tv암호.getText().toString();
	            password=password.trim();
	            
	            if(password.equals("")){
	            	
	            	Toast.makeText(Content.this,"암호를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
	            	
	            	return;
	            }
				
	            Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(Content.this).getParameterDic().get("boardData");
				Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(Content.this).getParameterDic().get("contentData");
		        String contentNo=contentData.get("link");
		        contentNo=StringHelper.tokenize(contentNo,"/")[2];
		        
		        //seq/%@?act=MODIFY&bbs=%@&pact=MODIFY&password=%@
		        Map<String,String>parameter=new HashMap<String,String>();
		        parameter.put("act","MODIFY");
		        parameter.put("bbs",boardData.get("boardId"));
		        parameter.put("pact","MODIFY");
		        parameter.put("password",password);
	            
		        OkjspShare.getInstance(Content.this).getParameterDic().put("modifyPassword",password);
		        
		        Content.this.startLoadingDialog("권한확인중...");
		        
		        requester2=new Requester(OkjspShare.getInstance(Content.this),Content.this);
		        requester2.setAsync(new RequestAsyncHandler());
				requester2.submitWithParameters("seq/"+contentNo,parameter);
			}
		});
		aDialog.show();
	}
	private void 글삭제(){
		LayoutInflater inflater=getLayoutInflater();
		View 암호뷰=(View)inflater.inflate(R.layout.password,null);
        tv암호=(TextView)암호뷰.findViewById(R.id.암호);
		AlertDialog.Builder aDialog = new AlertDialog.Builder(Content.this);
		aDialog.setTitle("게시물을 삭제하시겠습니까?");
		aDialog.setMessage("암호를 입력하십시오.");
		aDialog.setView(암호뷰);
		aDialog.setNegativeButton("아니오",null);
		aDialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
	            
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
				
				String password=tv암호.getText().toString();
	            password=password.trim();
	            
	            if(password.equals("")){
	            	
	            	Toast.makeText(Content.this,"암호를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
	            	
	            	return;
	            }
				
	            Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(Content.this).getParameterDic().get("boardData");
				Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(Content.this).getParameterDic().get("contentData");
		        String contentNo=contentData.get("link");
		        contentNo=StringHelper.tokenize(contentNo,"/")[2];
		        
		        //seq/%@?pact=MEMO&bbs=%@&seq=%@&writer=%@&memopass=%@&send=Memo&bcomment=%@&doublecheck=okjsp
		        Map<String,String>parameter=new HashMap<String,String>();
		        parameter.put("act","DELETE");
		        parameter.put("bbs",boardData.get("boardId"));
		        parameter.put("pact","DELETE");
		        parameter.put("password",password);
	            
		        Content.this.startLoadingDialog("글삭제중...");
		        
		        requester3=new Requester(OkjspShare.getInstance(Content.this),Content.this);
		        requester3.setAsync(new RequestAsyncHandler());
				requester3.submitWithParameters("seq/"+contentNo,parameter);
			}
		});
		aDialog.show();
	}
	//refresh
	public void refresh(View sender){
		//버튼이 터치된상태라면 작업이 완료되기전까지
		//재실행이 금지된다.--연속터치시 에러발생...
		갱신버튼.setEnabled(false);
		
		isRefresh=true;
	    
		//no정보를 얻음..
		Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
        String contentNo=contentData.get("link");
        contentNo=StringHelper.tokenize(contentNo,"/")[2];
        
        //http://www.okjsp.pe.kr/html5/bbs/viewMemo.jsp?seq=182933&startCount=0
		Map<String,String>parameter=new HashMap<String,String>();
        parameter.put("seq",contentNo);
        parameter.put("startCount","0");
        
        this.startLoadingDialog("로드중...");
        
        requester=new Requester(OkjspShare.getInstance(this),this);
        requester.setEndPage(false);
        requester.setAsync(new RequestAsyncHandler());
        requester.setCheckingLogin(false);
        requester.submitWithParameters("html5/bbs/viewMemo.jsp",parameter,0);
	}
	@Override
	public void finish(Requester request, String result) {
		
		this.stopLoadingDialog();
		
		if (request==requester6) { //댓글삭제시..
	        //화면을 재갱신한다..
			getHandler().post(
					new Runnable(){
						public void run(){
							Content.this.refresh(갱신버튼);
						}
					});
	    }else if (request==requester3) { //삭제시..
	    	
	        int range1=result.indexOf("java.lang.Exception");
	        int range2=result.indexOf("WRONG PASSWORD");
	        
	        //이경우에는 패스워드가 틀렸다.
	        if (range1!=-1&&range2!=-1) {
	    		
	    		this.getHandler().post(
						new Runnable(){
							public void run(){
						        AlertDialog.Builder ab=new AlertDialog.Builder(Content.this);
							    ab.setMessage("암호가 틀렸습니다.");
						    	ab.setPositiveButton("확인",null);
						    	//뒤로가기버튼 안먹게수정..
						     	//ab.setCancelable(false);	
						    	ab.show();
							}
						});
	            return;
	        }
	        
	        Toast.makeText(Content.this,"게시물이 삭제되었습니다.",Toast.LENGTH_SHORT).show();
	        
	        //완료로 판단하여..
	        //되돌아감..
	        this.setResult(200);
	        this.finish();
	        
	    }else if (request==requester2) { //수정시.. 
	    	
	        int range1=result.indexOf("java.lang.Exception");
	        int range2=result.indexOf("WRONG PASSWORD");
	        
	        //이경우에는 패스워드가 틀렸다.
	        if (range1!=-1&&range2!=-1) {
	        	this.getHandler().post(
						new Runnable(){
							public void run(){
						        AlertDialog.Builder ab=new AlertDialog.Builder(Content.this);
							    ab.setMessage("암호가 틀렸습니다.");
						    	ab.setPositiveButton("확인",null);
						    	//뒤로가기버튼 안먹게수정..
						     	//ab.setCancelable(false);	
						    	ab.show();
							}
						});
	            return;
	        }
	        
	        Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
	        
	        String modifySubject=contentData.get("subject");
	        String modifyContent=contentData.get("content");
	        modifyContent=modifyContent.trim();
	        
	        String[] contentArray=StringHelper.tokenize(modifyContent, "</p>");
	        String contentData1;
	        String contentMakeData="";
	        for (int i=0;i<contentArray.length;i++) {
	            contentData1=contentArray[i];
	            contentData1=contentData1.replace("<p>", "");
	            contentData1=contentData1.trim();
	            
	            contentMakeData+=contentData1;
	            contentMakeData+="\n";
	        }
	        modifyContent=contentMakeData;
	        modifyContent=modifyContent.replace("&nbsp;"," ");
	        modifyContent=modifyContent.trim();
	        
	        //옥희 가 있다면 이를 없앤다.
	        if (modifyContent.lastIndexOf("-from OkJsP-<br />", modifyContent.length()-18)!=-1) {
	            modifyContent=modifyContent.substring(0,modifyContent.length()-18);
	        }
	        if (modifyContent.endsWith("-from OkJsP-")) {
	            modifyContent=modifyContent.substring(0,modifyContent.length()-12);
	        }
	        
	        Map<String,String> modifyContentData=new HashMap<String,String>();
	        modifyContentData.put("subject",modifySubject);
	        modifyContentData.put("content",modifyContent);
	        OkjspShare.getInstance(this).getParameterDic().put("modifyContentData",modifyContentData);
	        
	        Intent intent=new Intent(this, WriteContent.class);
			intent.putExtra("isModify",true);
			startActivityForResult(intent,100);
			overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
	        
	    }else{
	        
	        //댓글파싱용정보를 얻어낸다.
	        List<String> commentSourceArray=checkReplys(result);
	        
	        /////////////////
	        //댓글파싱
	        /////////////////
	        
	        List<Map<String,String>> commentArray=new ArrayList<Map<String,String>>();
	        
	        String commentName=null;
	        String commentImage=null;
	        String commentSource=null;
	        String commentTime=null;
	        String commenttmpId=null;
	        
	        Map<String,String> commentDic=null;
	        TagNode tagNode=null;
	        
	        for(int i=0;i<commentSourceArray.size();i++){
	            commentDic=new HashMap<String,String>();
	            commentSource=commentSourceArray.get(i);
	            commentSource="<div class='column1-unit'>"+commentSource;
	            
	            tagNode=xpathParser.clean(commentSource);
	            
	            //댓글작성자정보..(시간과 같이 포함되어있다)
	            commentName=this.getTagData(tagNode,"//div[@class='column1-unit']/p[2]/text()");
	            int index=commentName.length()-19;
	            commentTime=commentName.substring(index,commentName.length());
	            commentName=commentName.substring(0,index);
	            
	            commentDic.put("commentName", commentName);
	            commentDic.put("commentTime", "("+commentTime+")");
	            
	            //댓글작성자이미지..
	            commentImage=this.getTagData(tagNode,"//div[@class='column1-unit']/p[1]/img/@src");
	            commentDic.put("commentImage", commentImage);
	            
	            //댓글내용..
	            int range=commentSource.indexOf("<br />");
	            commentSource=commentSource.substring(range+6,commentSource.length());
	            range=commentSource.indexOf("</p>");
	            commentSource=commentSource.substring(0, range);
	            commentDic.put("commentMemo", commentSource);
	            
	            //댓글아이디..
	            commenttmpId=this.getTagData(tagNode,"//div[@class='column1-unit']/input[@name='mseq']/@value");
	            commentDic.put("commentId",commenttmpId);
	            
	            commentArray.add(commentDic);
	        }
	        
	        Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("contentData");
	        
	        //이미지 크기를 CSS제어하기위한 처리..빈공백이 들어간 값은 제대로 CSS인식을 못함..
	        result=result.replace("class=\"ct lh\"", "class=\"imageWidth\"");
	        //유투브 실행관련하여 문제가 발생하여 이로직은 추가함..
	        result=result.replace("<object ", "<object width='100%' height='' ");
	        result=result.replace("<embed ", "<embed width='100%' height='' ");
	        
	        htmlData="<html>\n";
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
	        htmlData+="<td class=a200 width='40%'><img src='"+contentData.get("nickImage")+"' width='40'><span class='xeComment_name'><b>"+contentData.get("nick")+"</b></span></td>\n";
	        htmlData+="<td class=a300 width='60%' align='right' valign='bottom'><span class='xe_viewDate'>("+contentData.get("readCount")+","+contentData.get("writeDate")+")</span></td>\n";
	        htmlData+="</table>\n";
	        
	        htmlData+="<div style='padding:4px;border-bottom:1px dashed #202020;background-color:f8f8ff'>"+contentData.get("content")+"</div>\n";
	        
	        htmlData+=this.makeCommentHtml(commentArray)+"\n"; //코멘트정보..
	        
	        htmlData+="</body>\n";
	        htmlData+="</html>\n";
	        
	        //처리된 item을 다시이용가능하게한다.
	        갱신버튼.setEnabled(true);
	        
	        아답터.notifyDataSetChanged();
	        
	        //메모삭제시 에 대한 처리..
	        if (this.commentId!=null) {
	            
	            //삭제하려는 것과 동일한 아이디의 메모가 존재한다면 삭제안된것으로 간주한다.
	            for(Map<String,String> tmpData : commentArray){
	            	if(tmpData.get("commentId").equals(this.commentId)){
	                	AlertDialog.Builder ab=new AlertDialog.Builder(this);
	        		    ab.setMessage("암호가 틀렸습니다.");
	        	    	ab.setPositiveButton("확인",null);
	        	    	//뒤로가기버튼 안먹게수정..
	        	     	//ab.setCancelable(false);	
	        	    	ab.show();
	                    
	                    break;
	                }
	            }
	            
	            this.commentId=null;
	        }
	    }
	}
	@Override
	public void fail(Requester request) {
		
		this.stopLoadingDialog();
		
		this.getHandler().post(new Runnable(){
        	public void run(){
        		
        		//처리된 item을 다시이용가능하게한다.
        		갱신버튼.setEnabled(true);
        		
        		아답터.notifyDataSetChanged();
        		
        		AlertDialog.Builder ab=new AlertDialog.Builder(Content.this);
        		ab.setTitle("오류");
        	    ab.setMessage("연결상태를 확인후 재시도 바랍니다.");
            	ab.setPositiveButton("확인",null);
            	//뒤로가기버튼 안먹게수정..
             	//ab.setCancelable(false);	
            	ab.show();
        	}
        });
	}
	
	/**
	 * 태그의 내용정보를 추출하여 리턴한다.
	 * @param tagNode
	 * @param xPath
	 * @return
	 */
	private String getTagData(TagNode tagNode,String xPath){
		try{
			Object[] ttt=tagNode.evaluateXPath(xPath);
			if(ttt==null){
				return "";
			}else if(ttt.length>0){
				return ttt[0].toString();
			}else{
				return "";
			}
			
		}catch(Exception e){
			return "";
		}
	}
	//댓글정보를 html로 생성한다.
	private String makeCommentHtml(List<Map<String,String>> commentArray){
		String commentData="";
		
		String strName=null;
		String strMemo=null;
		String strTime=null;
		String strCommentId=null;
	    String strImage=null;
	    
	    String commentWriterId=null;
	    String commentWriterNick=null;
	    
	    Map<String,String> data=null;
	    
		for (int i=0;i<commentArray.size();i++) {
			data=commentArray.get(i);
	        
			strName=data.get("commentName");
	        strTime=data.get("commentTime");
	        strMemo=data.get("commentMemo");
	        strCommentId=data.get("commentId");
	        strImage=data.get("commentImage");
	        
	        commentWriterId=data.get("commentWriterId");
	        commentWriterNick=data.get("commentWriterNick");
	        
	        commentData+="<table width='100%' style='background-color:#c2d3fc;'>\n";
	            commentData+="<tr>\n";
	            	commentData+="<td colspan='2'><span style='font:15px 굴림;color:#000000'>"+strName+"</span><span style='font:12px 굴림; color:#696969'>"+strTime+"</span></td>\n";
	            commentData+="</tr>\n";
	            commentData+="<tr>\n";
	                commentData+="<td width='30' valign='top'>\n";
	                    if (!strImage.equals("")) {
	                    	commentData+="<img src='"+strImage+"'  width='30'/>\n";
	                    }else {
	                        commentData+="&nbsp;";
	                    }
	                commentData+="</td>\n";
	                commentData+="<td>\n";
	                	commentData+=strMemo+"\n";
	                commentData+="</td>\n";
	            commentData+="</tr>\n";
	        if (OkjspShare.getInstance(this).isLogin()) {
	            commentData+="<tr>\n";
	                commentData+="<td colspan='2' align='right' class='xeComment_memo'>\n";
	                	commentData+="<span style='font:12px 굴림;color:#696969;' onClick=\"javascript:command('DeleteComment:"+strCommentId+"')\">삭제</span>&nbsp;\n";
	                commentData+="</td>\n";
	            commentData+="</tr>\n";
	        }else {
	            commentData+="<tr>\n";
	                commentData+="<td colspan='2' align='right' class='xeComment_memo'>\n";
	                	commentData+="&nbsp;";
	                commentData+="</td>\n";
	            commentData+="</tr>\n";
	        }
			commentData+="</table>\n";
		}
	    
		return commentData;
	}
	//파싱할 댓글정보를 리스트로 얻어낸다.
	private List<String> checkReplys(String result){
	    
		String[] checkArray=StringHelper.tokenize(result,"<div class=\"column1-unit\">");
		
		if(checkArray.length>1){
			List<String> array=new ArrayList<String>(checkArray.length-1);
			//1번째는 댓글데이터가 아니므로 떼어낸다.
	        for(String gap : checkArray){
	        	array.add(gap);
	        }
	        array.remove(0);
	        return array;
		}else{
			return new ArrayList<String>(0);
		}
	}
	/**
	 * 자바스크립트관련 처리를 한다.
	 * @author iiixzu
	 *
	 */
	private TextView tv암호;
	public final class JavaScriptExtention{
		public void webCommand(final String arg) {
			if(arg.startsWith("DeleteComment")){ //댓글삭제일경우..
	            commentId=StringHelper.tokenize(arg,":")[1];
	            
	            LayoutInflater inflater=getLayoutInflater();
	            View 암호뷰=(View)inflater.inflate(R.layout.password,null);
	            tv암호=(TextView)암호뷰.findViewById(R.id.암호);
	            
	            AlertDialog.Builder aDialog = new AlertDialog.Builder(Content.this);
	    		aDialog.setTitle("댓글을 삭제하시겠습니까?");
	    		aDialog.setMessage("암호를 입력하십시오.");
	    		aDialog.setView(암호뷰);
	    		aDialog.setNegativeButton("아니오",null);
	    		aDialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {
	    				
	    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
	    				imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
	    				
	    				String password=tv암호.getText().toString();
	    	            password=password.trim();
	    	            
	    	            if(password.equals("")){
	    	            	
	    	            	Toast.makeText(Content.this,"암호를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
	    	            	
	    	            	return;
	    	            }
	    	            
	    	            Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(Content.this).getParameterDic().get("boardData");
	    	            Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(Content.this).getParameterDic().get("contentData");
	    	            String contentNo=contentData.get("link");
	    	            contentNo=StringHelper.tokenize(contentNo,"/")[2];
	    	            
	    	            Map<String,String>parameter=new HashMap<String,String>();
	    	            parameter.put("pact","MEMO");
	    	            parameter.put("seq",contentNo);
	    	            parameter.put("bbs",boardData.get("boardId"));
	    	            parameter.put("delpass",password);
	    	            parameter.put("mseq",commentId);
	    	            		
	    	            Content.this.startLoadingDialog("댓글삭제중...");
	    	            requester6=new Requester(OkjspShare.getInstance(Content.this),Content.this);
	    		        requester6.setAsync(new RequestAsyncHandler());
	    		        requester6.submitWithParameters("seq/"+contentNo,parameter);
	    			}
	    		});
	    		aDialog.show();
			}
		}	  
	}

	/**
	 * 게시판목 아답터..
	 * @author iiixzu
	 */
	public class Adapter extends BaseAdapter {
			
        public Adapter(){
        	
        }
        
        public int getCount(){
        	if(htmlData==null){
        		return 1;
        	}else{
        		return 2;
        	}
        }
        
        public Object getItem(int index){
        	return null;
        }
        public long getItemId(int index){
        	return index;
        }
        
        public View getView(int position,View convertView,ViewGroup parent){
        	
        	if(htmlData!=null){
        		if(position==1){//광고..	
            		LayoutInflater inflater=getLayoutInflater();
            		LinearLayout layout=(LinearLayout)inflater.inflate(R.layout.ad,null);
            		layout.addView(AdamAdViewDelegater.getInatance(Content.this).getAdView());
            		
            		return layout;
        		}else{
		        	if(convertView==null||convertView.getId() != R.layout.content_item){
		        		LayoutInflater inflater=getLayoutInflater();
		        		convertView=inflater.inflate(R.layout.content_item,null);
		        	}
		        	WebView webView=(WebView)convertView;
		        	webView.loadDataWithBaseURL(null, htmlData, "text/html", "utf-8", null);
		        	webView.getSettings().setJavaScriptEnabled(true);
		        	webView.addJavascriptInterface(new JavaScriptExtention(), "android");
		        	
		        	if(OkjspShare.getInstance(Content.this).isGoBackGesture()){
		        		webView.setOnTouchListener(swipeDetector);
		        	}
			        
			    	return convertView;
        		}
        	}else{//광고..	
        		LayoutInflater inflater=getLayoutInflater();
        		LinearLayout layout=(LinearLayout)inflater.inflate(R.layout.ad,null);
        		layout.addView(AdamAdViewDelegater.getInatance(Content.this).getAdView());
        		
        		return layout;
        	}
        }
    }
}
