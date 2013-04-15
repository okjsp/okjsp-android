package com.tistory.iiixzu.common;

import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * ShortCut메뉴를 관리한다.
 * @author iiixzu
 *
 */
public class ShortcutMenu extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "okjsp.db";
	private static final int DATABASE_VERSION = 1;

	private List<Map<String,String>> menuList;
	
	public ShortcutMenu(Context context,List<Map<String,String>> list) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		menuList=list;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE okjsp (_id INTEGER PRIMARY KEY AUTOINCREMENT,shortcutindex INTEGER UNIQUE);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public void removeMenuIndex(int index){
		try{
			this.getWritableDatabase().delete("okjsp","shortcutindex=?", new String[]{index+""});
		}catch(Exception e){
			
		}finally{
			
		}
	}
	public void setMenuIndex(int index){
		try{
			ContentValues values=new ContentValues();
			values.put("shortcutindex",index+"");
			
			this.getWritableDatabase().insert("okjsp", null, values);
		}catch(Exception e){
			
		}finally{
			
		}
	}
	/**
	 * 메뉴정보를 새로 가공해서 넘긴다.
	 * @param list 메뉴정보가 들어갈 리스트객체
	 */
	public void getMenu(List<Map<String,String>> list){
		Cursor cursor=null;
		try{
			String query = "SELECT _id, shortcutindex "+
						     "FROM okjsp "+
						 "ORDER BY shortcutindex asc";
			cursor = this.getWritableDatabase().rawQuery(query, null);
			
			list.clear();
			
			if(cursor.moveToFirst()){
	    		do{
	    			list.add(menuList.get(cursor.getInt(1)));
	    		}while(cursor.moveToNext());
	    	}
		}catch(Exception e){
			
		}finally{
			try{cursor.close();}catch(Exception e){}
		}
	}
	/**
	 * 데이터가 존재하는지여부.
	 * @param index
	 * @return
	 */
	public boolean isCheck(int index){
		Cursor cursor=null;
		try{
			String query = "SELECT _id, shortcutindex "+
		                     "FROM okjsp "+
						    "WHERE shortcutindex='"+index+"'";
			cursor = this.getWritableDatabase().rawQuery(query, null);
			
			if(cursor.moveToFirst()){
	    		return true;
	    	}
			return false;
		}catch(Exception e){
			return false;
		}finally{
			try{cursor.close();}catch(Exception e){}
		}
	}
}
