package net.okjsp.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.okjsp.Const;
import net.okjsp.util.Log;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Post implements Const, Parcelable {
	protected static final boolean DEBUG_LOG = false;
	
	protected int Id;
	protected String BoardName;
	protected String BoardUrl;
	protected String Title;
	protected String Url;
	protected String WriterName;
	protected String ProfileImageUrl;
	protected String TimeStamp;
	protected boolean IsEmpty = true;
	protected boolean IsRead = false;
	protected boolean IsPinned = false;
	protected int ReadCount;
	protected int CommentNum;
	
	public int getId() {
		if (Id < 1 && !TextUtils.isEmpty(Url)) {
			Id = extractPostId(Url);
		}
		
		return Id;
	}

	public Post setId(int postId) {
		IsEmpty = false;
		Id = postId;
		return this;
	}

	public String getBoardName() {
		return BoardName;
	}

	public Post setBoardName(String boardName) {
		if (!TextUtils.isEmpty(boardName)) IsEmpty = false;
		BoardName = boardName;
		return this;
	}

	public String getBoardUrl() {
		return TextUtils.isEmpty(BoardUrl) ? null : 
			Url.startsWith(BASE_URL) ? BoardUrl : (BASE_URL + BoardUrl);
	}

	public Post setBoardUrl(String boardUrl) {
		if (!TextUtils.isEmpty(boardUrl)) IsEmpty = false;
		BoardUrl = boardUrl;
		return this;
	}

	public String getBoardUri() {
		return TextUtils.isEmpty(BoardUrl) ? null : 
			Url.startsWith(BASE_URL) ? Uri.parse(BoardUrl).getHost() : (BoardUrl);
	}

	public Post setBoardUri(String boardUri) {
		if (!TextUtils.isEmpty(boardUri)) IsEmpty = false;
		else {
			BoardUrl = boardUri;
			return this;
		}
		
		BoardUrl = boardUri.startsWith(BASE_URL) ? boardUri : (BASE_URL + boardUri);
		return this;
	}
	
	public String getTitle() {
		return Title;
	}

	public Post setTitle(String title) {
		if (!TextUtils.isEmpty(title)) IsEmpty = false;
		Title = title;
		return this;
	}

	public String getUrl() {
		return TextUtils.isEmpty(Url) ? null : 
			Url.startsWith(BASE_URL) ? Url : (BASE_URL + Url);
	}

	public Post setUrl(String postUrl) {
		if (!TextUtils.isEmpty(postUrl)) IsEmpty = false;
		Url = postUrl;
		return this;
	}

	public String getWriterName() {
		return WriterName;
	}

	public Post setWriterName(String writerName) {
		if (!TextUtils.isEmpty(writerName)) IsEmpty = false;
		WriterName = writerName;
		return this;
	}

	public String getProfileImageUrl() {
		return TextUtils.isEmpty(ProfileImageUrl) ? null : 
			Url.startsWith(BASE_URL) ? ProfileImageUrl : (BASE_URL + ProfileImageUrl);
	}

	public Post setProfileImageUrl(String profileImageUrl) {
		if (!TextUtils.isEmpty(profileImageUrl)) IsEmpty = false;
		ProfileImageUrl = profileImageUrl;
		return this;
	}

	public int getReadCount() {
		return ReadCount;
	}

	public Post setReadCount(int readCount) {
		ReadCount = readCount;
		return this;
	}

	public String getTimeStamp() {
		return TimeStamp;
	}

	public Post setTimeStamp(String timeStamp) {
		if (!TextUtils.isEmpty(timeStamp)) IsEmpty = false;
		TimeStamp = timeStamp;
		return this;
	}
	
	public long getTime() {
		long time = 0;
		
		if (TextUtils.isEmpty(getTimeStamp())) return 0;
		else if (getTimeStamp().length() == "2013-04-16 15:46:02.0".length()) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss'.0'", Locale.getDefault());  
			try {  
			    Date date = format.parse(getTimeStamp());  
			    time = date.getTime(); 
			    if (DEBUG_LOG) Log.e("Converted Time: " + date.toString());
			} catch (ParseException e) {  
			    e.printStackTrace();  
			}			
		} else if (getTimeStamp().length() == "2013-04-16 15:46:02".length()) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault());  
			try {  
			    Date date = format.parse(getTimeStamp());  
			    time = date.getTime(); 
			    if (DEBUG_LOG) Log.e("Converted Time: " + date.toString());
			} catch (ParseException e) {  
			    e.printStackTrace();  
			}			
		}
		
		return time;
	}
	
	public Post setAsRead(boolean is_read) {
		IsRead = is_read;
		return this;
	}

	public boolean isRead() {
		return IsRead;
	}
	
	public boolean isEmpty() {
		return IsEmpty;
	}
	
	public Post setPinned(boolean pin) {
		IsPinned = pin;
		return this;
	}

	public boolean isPinned() {
		return IsPinned;
	}
	
	public boolean isValid() {
		boolean valid = false;
		
		valid = !TextUtils.isEmpty(getWriterName());
		
		return valid;
	}
	
	public int extractPostId(String url) {
		int post_id = -1;
		
		if ("Sponsored".equals(url)) {
			this.setPinned(true);
		} else {
			int index = url.indexOf("?"); // /f.jsp?/seq/219160
			if (index >= 0) {
				url = url.substring(index + 1);
			}
			try {
				this.setId(Integer.valueOf(Uri.parse(url).getLastPathSegment()));
			} catch (NumberFormatException e) {
				Log.w("NumberFormatException: " + url + ", " + Uri.parse(url).getLastPathSegment());
			}
		}
		
		return post_id;
	}

	public Post() {
		// do nothing
	}
	
	public Post(Parcel in){
		Id = in.readInt();
		BoardName = in.readString();
		BoardUrl = in.readString();
		Title = in.readString();
		Url = in.readString();
		WriterName = in.readString();
		ProfileImageUrl = in.readString();
		ReadCount = in.readInt();
		TimeStamp = in.readString();
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(Id);
    	dest.writeString(BoardName);
    	dest.writeString(BoardUrl);
    	dest.writeString(Title);
    	dest.writeString(Url);
    	dest.writeString(WriterName);
    	dest.writeString(ProfileImageUrl);
    	dest.writeInt(ReadCount);
    	dest.writeString(TimeStamp);
    }
    
    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        public Post createFromParcel(Parcel in) {
            return new Post(in); 
        }

        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
    
    public String toString() {
    	String result = "";
    	
    	result +=   "Writer     : " + getWriterName();
    	result += "\n    ImageUrl   : " + getProfileImageUrl();
    	result += "\n    Post Id    : " + getId();
    	result += "\n    Board Name : " + getBoardName();
    	result += "\n    Board URL  : " + getBoardUrl();
    	result += "\n    Title      : " + getTitle();
    	result += "\n    Post URL   : " + getUrl();
    	result += "\n    Read Count : " + getReadCount();
    	result += "\n    Time stamp : " + getTimeStamp();
    	
    	return result;
    }
}
