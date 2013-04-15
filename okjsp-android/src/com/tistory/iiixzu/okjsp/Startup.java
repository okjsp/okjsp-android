package com.tistory.iiixzu.okjsp;

import android.content.Intent;
import android.os.Bundle;

import com.tistory.iiixzu.common.CommonActivity;
/**
 * 인트로 Activity
 * @author iiixzu
 *
 */
public class Startup extends CommonActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        
        this.getHandler().postDelayed(new Runnable(){
        	public void run(){
        		startActivity(new Intent(Startup.this, Menu.class));
        		Startup.this.finish();
        	}
        },1000);
    }
}