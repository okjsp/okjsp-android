package com.tistory.iiixzu.okjsp;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.tistory.iiixzu.common.CommonActivity;
import com.tistory.iiixzu.common.IRequestObserver;
import com.tistory.iiixzu.common.OkjspShare;
import com.tistory.iiixzu.common.RequestAsyncHandler;
import com.tistory.iiixzu.common.Requester;
import com.tistory.iiixzu.common.StringHelper;
/**
 * 댓글쓰기.
 * @author iiixzu
 *
 */
public class WriteReply extends CommonActivity implements IRequestObserver{
	
	private TextView tv댓글;
	private Requester requester;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_reply);
        
        tv댓글=(TextView)this.findViewById(R.id.댓글);
        
        requester=new Requester(OkjspShare.getInstance(this),this);
        requester.setAsync(new RequestAsyncHandler());
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "댓글쓰기완료");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,2, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	@Override
	public boolean onPrepareOptionsMenu (android.view.Menu menu){
			
		menu.clear();
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "댓글쓰기완료");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,2, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	private TextView tv암호;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId()==1){//글쓰기
			String 댓글=tv댓글.getText().toString().trim();
			if(댓글.equals("")){
	    		
				tv댓글.requestFocus();
	    		Toast.makeText(WriteReply.this,"내용을 입력하십시오.",Toast.LENGTH_SHORT).show();
	    		
			}else{
				
				LayoutInflater inflater=getLayoutInflater();
				View 암호뷰=(View)inflater.inflate(R.layout.password,null);
	            tv암호=(TextView)암호뷰.findViewById(R.id.암호);
	            tv암호.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							
							InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
							imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
							
							return true;
						}
						return false;
					}
				});
				AlertDialog.Builder aDialog = new AlertDialog.Builder(WriteReply.this);
	    		aDialog.setTitle("댓글을 등록하시겠습니까?");
	    		aDialog.setMessage("암호를 입력하십시오.");
	    		aDialog.setView(암호뷰);
	    		aDialog.setNegativeButton("아니오",null);
	    		aDialog.setPositiveButton("등록", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {
	    	            
	    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
						
	    				String password=tv암호.getText().toString();
	    	            password=password.trim();
	    	            
	    	            if(password.equals("")){
	    	            	
	    	            	Toast.makeText(WriteReply.this,"암호를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
	    	            	
	    	            	return;
	    	            }
	    	            
	    	            Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(WriteReply.this).getParameterDic().get("boardData");
	    				Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(WriteReply.this).getParameterDic().get("contentData");
	    		        String contentNo=contentData.get("link");
	    		        contentNo=StringHelper.tokenize(contentNo,"/")[2];
	    		        
	    		        String writer="";
	    		        String 댓글="";
	    		        try{writer=new String(OkjspShare.getInstance(WriteReply.this).getNickName().getBytes("euc-kr"),"iso-8859-1");}catch(Exception e){}
	    		        try{댓글=new String(tv댓글.getText().toString().trim().getBytes("euc-kr"),"iso-8859-1");}catch(Exception e){}
	    		        댓글+="\n\n-from OkJsP-\n";
	    		        
	    		        //seq/%@?pact=MEMO&bbs=%@&seq=%@&writer=%@&memopass=%@&send=Memo&bcomment=%@&doublecheck=okjsp
	    		        Map<String,String>parameter=new HashMap<String,String>();
	    		        parameter.put("pact","MEMO");
	    		        parameter.put("bbs",boardData.get("boardId"));
	    		        parameter.put("seq",contentNo);
	    		        parameter.put("writer",writer);
	    		        parameter.put("memopass",password);
	    		        parameter.put("send","Memo");
	    		        parameter.put("bcomment",댓글);
	    		        parameter.put("doublecheck","okjsp");
	    		        
	    		        WriteReply.this.startLoadingDialog("댓글저장중...");
	    		        
	    				requester.submitWithParameters("seq/"+contentNo,parameter);
	    			}
	    		});
	    		aDialog.show();
			}
		}
		return (super.onOptionsItemSelected(item));
	}
	@Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		this.finish();
    		overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    		return false;
    	}
    	return false;
    }
	
	private String message;
	@Override
	public void finish(Requester request, String result) {
		
		this.stopLoadingDialog();
		
		int index=result.indexOf("history.go(-1)");
	    
	    if (index!=-1) {
	        
	    	message=StringHelper.tokenize(result,"alert('")[1];
	    	message=StringHelper.tokenize(message,"');")[0];
	        
	    	this.getHandler().post(
					new Runnable(){
						public void run(){
					        AlertDialog.Builder ab=new AlertDialog.Builder(WriteReply.this);
						    ab.setMessage("댓글등록에 실패했습니다.\n확인후 재시도 바랍니다.");
					    	ab.setPositiveButton("확인",null);
					    	//뒤로가기버튼 안먹게수정..
					     	//ab.setCancelable(false);	
					    	ab.show();
						}
					});
	    }else{
	        tv댓글.setText("");
	        
	        //임시작성내용을 초기화시킨다.
	        OkjspShare.getInstance(this).getParameterDic().put("tmpReplyContent","");
	        
	        this.setResult(200);
			this.finish();
			overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	    }
	}
	@Override
	public void fail(Requester request) {
        
		this.stopLoadingDialog();
		
		this.getHandler().post(
				new Runnable(){
					public void run(){
				        AlertDialog.Builder ab=new AlertDialog.Builder(WriteReply.this);
						ab.setTitle("오류");
					    ab.setMessage("댓글등록에 실패했습니다.\n확인후 재시도 바랍니다.");
				    	ab.setPositiveButton("확인",null);
				    	//뒤로가기버튼 안먹게수정..
				     	//ab.setCancelable(false);	
				    	ab.show();
					}
				});
	}
}
