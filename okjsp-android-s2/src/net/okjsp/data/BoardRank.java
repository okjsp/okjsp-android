package net.okjsp.data;

import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class BoardRank implements Externalizable {
	protected static final String TAG = "BoardRank";
	protected static final int DATA_VERSION = 1;
	protected static final boolean DEBUG_LOG = false;
	
	protected HashMap<String, Integer> mBoardClickMap = new HashMap<String, Integer>();
	protected HashMap<String, Long> mBoardTimeMap = new HashMap<String, Long>();
	protected ArrayList<String> mBoardList = new ArrayList<String>();
	
	protected String mFixedBoard [] = {"recent", "notice"};
	
	public void add(String board) {
		if (isFixedBoard(board)) return;
		
		mBoardClickMap.put(board, getClickCount(board) + 1);
		mBoardTimeMap.put(board, System.currentTimeMillis());
		calcRank();
	}
	
	public void set(String board, int count) {
		mBoardClickMap.put(board, count);
		mBoardTimeMap.put(board, System.currentTimeMillis());
	}
	
	public boolean isFixedBoard(String board) {
		boolean is_fixed = false;
		for(String bd : mFixedBoard) {
			if (bd.equals(board)) {
				is_fixed = true;
				break;
			}
		}
		return is_fixed;
	}
	
	public int getClickCount(String board) {
		int count = 0;
		if (mBoardClickMap.containsKey(board)) {
			count = mBoardClickMap.get(board);
		}
		return count;
	}

	public long getTime(String board) {
		long time = 0;
		if (mBoardTimeMap.containsKey(board)) {
			time = mBoardTimeMap.get(board);
		}
		return time;
	}
	
	public void calcRank() {
		mBoardList.clear();
		Iterator<String> iter = mBoardClickMap.keySet().iterator();
        while (iter.hasNext()) {
        	mBoardList.add((String)iter.next());
        } 
		Collections.sort(mBoardList, comparator);
		
		if (DEBUG_LOG) {
			int count = 0;
			for(String board : mBoardList) {
				Log.e(TAG, "[" + count++ + "]:" + board + ", " + getClickCount(board));
			}
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int ver = in.readInt();
		if (ver == DATA_VERSION) {
			mBoardClickMap = (HashMap<String, Integer>)in.readObject();
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(DATA_VERSION);
		out.writeObject(mBoardClickMap);
	}

	private final Comparator<String> comparator = new Comparator<String>() {
		@Override
		public int compare(String obj1, String obj2) {
			int result = getClickCount(obj2) - getClickCount(obj1);
			return (result == 0) ? (int)(getTime(obj2) - getTime(obj1)) : result;
		}
	};	
	
	public void saveToFile(Context context) {
		saveToFile(context, null);
	}
	
    public void saveToFile(Context context, String fileName) {
    	if (TextUtils.isEmpty(fileName)) {
    		fileName = TAG;
    	}
    	
        FileOutputStream fos;
        synchronized (TAG) {
            ConcurrentHashMap<String, Integer> saveMap = new ConcurrentHashMap<String, Integer>(mBoardClickMap);
            try {
                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(saveMap);
                os.close();        
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }	
    
    public void loadFromFile(Context context) {
    	loadFromFile(context, null);
    }
    public void loadFromFile(Context context, String fileName) {
    	if (TextUtils.isEmpty(fileName)) {
    		fileName = TAG;
    	}
    	
        FileInputStream fis;
        synchronized (TAG) {
            try {
                fis = context.openFileInput(fileName);
                ObjectInputStream is = new ObjectInputStream(fis);
                Object info_map =  is.readObject();
                if (info_map instanceof ConcurrentHashMap<?, ?>) {
                	mBoardClickMap.putAll((ConcurrentHashMap<String, Integer>)info_map);
                } else {
                    Log.e(TAG, "saved file instance mismatch!!! " + info_map.getClass().toString());
                }
                	
                is.close();
                calcRank();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }    
}
