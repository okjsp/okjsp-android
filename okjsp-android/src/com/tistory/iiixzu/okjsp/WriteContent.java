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
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.tistory.iiixzu.common.CommonActivity;
import com.tistory.iiixzu.common.IRequestObserver;
import com.tistory.iiixzu.common.OkjspShare;
import com.tistory.iiixzu.common.RequestAsyncHandler;
import com.tistory.iiixzu.common.Requester;
import com.tistory.iiixzu.common.StringHelper;

public class WriteContent extends CommonActivity implements IRequestObserver{
	
	private TextView tv글제목;
	private TextView tv글내용;
	
	private Requester requester;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_content);
        
        tv글제목=(TextView)this.findViewById(R.id.글제목);
        tv글내용=(TextView)this.findViewById(R.id.글내용);
        
        if(WriteContent.this.getIntent().getBooleanExtra("isModify",false)){//수정모드일경우 제목및 내용셋팅..
        	Map<String,String> modifyContentData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("modifyContentData");
        	
        	//수정이 전용에디터같은것을 통해 이루어지기때문에 내부적으로 이런내용이 섞여있다.
            //이를 제대로 표현하기위해서 내용정보를 변경한다.
        	String modifyContent=modifyContentData.get("content");
            modifyContent=modifyContent.replaceAll("</p>","\n");
            modifyContent=modifyContent.replaceAll("&nbsp;"," ");
            modifyContent=modifyContent.replaceAll("<br />","\n");
            modifyContent=modifyContent.replaceAll("<br>","\n");
            modifyContent=modifyContent.replaceAll("&quot;","\"");
            
            modifyContent=modifyContent.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>",""); 
            modifyContent=modifyContent.trim();
            
        	tv글제목.setText(modifyContentData.get("subject").trim());
        	tv글내용.setText(modifyContent);
        	
        	TextView tv제목=(TextView)this.findViewById(R.id.제목);
        	tv제목.setText("글수정");
        }
        requester=new Requester(OkjspShare.getInstance(this),this);
        requester.setAsync(new RequestAsyncHandler());
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "글작성완료");
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
			if(WriteContent.this.getIntent().getBooleanExtra("isModify",false)){//수정모드일경우 제목및 내용셋팅..
				menu.add(android.view.Menu.NONE,1, 0, "글수정완료");
			}else{
				menu.add(android.view.Menu.NONE,1, 0, "글작성완료");
			}
			return true;
		}else{
			menu.add(android.view.Menu.NONE,4, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	private TextView tv암호;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId()==1){//글작성완
			String 글제목=tv글제목.getText().toString().trim();
			String 글내용=tv글내용.getText().toString().trim();
			if(글제목.equals("")){
	    		
				tv글제목.requestFocus();
	    		Toast.makeText(WriteContent.this,"제목을 입력하십시오.",Toast.LENGTH_SHORT).show();
			}else if(글내용.equals("")){
	    		
				tv글내용.requestFocus();
	    		Toast.makeText(WriteContent.this,"내용을 입력하십시오.",Toast.LENGTH_SHORT).show();
			}else{
				
				if(WriteContent.this.getIntent().getBooleanExtra("isModify",false)){//수정모드일경우 제목및 내용셋팅..
					AlertDialog.Builder aDialog = new AlertDialog.Builder(WriteContent.this);
		    		aDialog.setMessage("글을 수정하시겠습니까?");
		    		aDialog.setNegativeButton("아니오",null);
		    		aDialog.setPositiveButton("수정", new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int which) {
		    	            
		    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    				imm.hideSoftInputFromWindow(tv글제목.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(tv글내용.getWindowToken(), 0);
							
		    	            Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(WriteContent.this).getParameterDic().get("boardData");
		    		        
		    		        String writer="";
		    		        String 글제목="";
		    		        String 글내용="";
		    		        writer=OkjspShare.getInstance(WriteContent.this).getNickName();
		    		        글제목=tv글제목.getText().toString().trim();
		    		        글내용=tv글내용.getText().toString().trim();
		    		        글내용+="\n\n-from OkJsP-\n";
		    		        
		    		        Map<String,String>parameter=new HashMap<String,String>();
	    		        	Map<String,String> contentData=(HashMap<String,String>)OkjspShare.getInstance(WriteContent.this).getParameterDic().get("contentData");
		    		        String contentNo=contentData.get("link");
		    		        contentNo=StringHelper.tokenize(contentNo,"/")[2];
		    		        
	    		        	parameter.put("bbs",boardData.get("boardId"));
		    		        parameter.put("act","MODIFY");
		    		        parameter.put("writer",writer);
		    		        parameter.put("subject",글제목);
		    		        parameter.put("content",글내용);
		    		        parameter.put("password",(String)OkjspShare.getInstance(WriteContent.this).getParameterDic().get("modifyPassword"));
		    		        parameter.put("seq",contentNo);
		    		        
		    		        OkjspShare.getInstance(WriteContent.this).getParameterDic().put("tmpSubject",글제목);
		    		        OkjspShare.getInstance(WriteContent.this).getParameterDic().put("tmpContent",글내용);
		    		        
		    		        WriteContent.this.startLoadingDialog("글수정중...");
		    		        
		    				requester.submitWithParameters("bbs",parameter,null);
		    			}
		    		});
		    		aDialog.show();
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
					AlertDialog.Builder aDialog = new AlertDialog.Builder(WriteContent.this);
		    		aDialog.setTitle("글을 등록하시겠습니까?");
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
		    	            	
		    	            	Toast.makeText(WriteContent.this,"암호를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
		    	            	
		    	            	return;
		    	            }
		    	            
		    	            Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(WriteContent.this).getParameterDic().get("boardData");
		    		        
		    		        String writer="";
		    		        String 글제목="";
		    		        String 글내용="";
		    		        writer=OkjspShare.getInstance(WriteContent.this).getNickName();
		    		        글제목=tv글제목.getText().toString().trim();
		    		        글내용=tv글내용.getText().toString().trim();
		    		        글내용+="\n\n-from OkJsP-\n";
		    		        
		    		        Map<String,String>parameter=new HashMap<String,String>();
		    		        parameter.put("bbs",boardData.get("boardId"));
		    		        parameter.put("act","ADD");
		    		        parameter.put("writer",writer);
		    		        parameter.put("subject",글제목);
		    		        parameter.put("content",글내용);
		    		        parameter.put("password",password);
		    		        
		    		        WriteContent.this.startLoadingDialog("글저장중...");
		    		        
		    				requester.submitWithParameters("bbs",parameter,null);
		    			}
		    		});
		    		aDialog.show();
				}
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
	@Override
	public void finish(Requester request, String result) {
		
		this.stopLoadingDialog();
		
        tv글제목.setText("");
        tv글내용.setText("");
        
        //임시작성내용을 초기화시킨다.
        OkjspShare.getInstance(this).getParameterDic().remove("modifyContentData");
        
        if(WriteContent.this.getIntent().getBooleanExtra("isModify",false)){
        	Toast.makeText(this,"게시물이 수정되었습니다.",Toast.LENGTH_SHORT).show();
        }else{
        	Toast.makeText(this,"게시물이 작성되었습니다.",Toast.LENGTH_SHORT).show();
        }
        this.setResult(100);
		this.finish();
		overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	}
	@Override
	public void fail(Requester request) {
        
		this.stopLoadingDialog();
		
		this.getHandler().post(
				new Runnable(){
					public void run(){
				        AlertDialog.Builder ab=new AlertDialog.Builder(WriteContent.this);
						ab.setTitle("오류");
					    ab.setMessage("글등록에 실패했습니다.\n확인후 재시도 바랍니다.");
				    	ab.setPositiveButton("확인",null);
				    	//뒤로가기버튼 안먹게수정..
				     	//ab.setCancelable(false);	
				    	ab.show();
					}
				});
	}
}
