package net.okjsp.data;

import net.okjsp.Const;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.BoringLayout;
import android.text.TextUtils;

public class Post implements Const, Parcelable {
	protected int PostId;
	protected String BoardName;
	protected String BoardUrl;
	protected String Title;
	protected String PostUrl;
	protected String WriterName;
	protected String ProfileImageUrl;
	protected int ReadCount;
	protected String TimeStamp;
	protected boolean IsEmpty = true;;
	
	public int getPostId() {
		return PostId;
	}

	public Post setPostId(int postId) {
		IsEmpty = false;
		PostId = postId;
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
		return BoardUrl;
	}

	public Post setBoardUrl(String boardUrl) {
		if (!TextUtils.isEmpty(boardUrl)) IsEmpty = false;
		BoardUrl = boardUrl;
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

	public String getPostUrl() {
		return TextUtils.isEmpty(PostUrl) ? null : BASE_URL + PostUrl;
	}

	public Post setPostUrl(String postUrl) {
		if (!TextUtils.isEmpty(postUrl)) IsEmpty = false;
		PostUrl = postUrl;
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
		return TextUtils.isEmpty(ProfileImageUrl) ? null : (BASE_URL + ProfileImageUrl);
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
	
	public boolean isEmpty() {
		return IsEmpty;
	}
	
	public boolean isValid() {
		boolean valid = false;
		
		valid = !TextUtils.isEmpty(getWriterName());
		
		return valid;
	}

	public Post() {
		// do nothing
	}
	
	public Post(Parcel in){
		PostId = in.readInt();
		BoardName = in.readString();
		BoardUrl = in.readString();
		Title = in.readString();
		PostUrl = in.readString();
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
    	dest.writeInt(PostId);
    	dest.writeString(BoardName);
    	dest.writeString(BoardUrl);
    	dest.writeString(Title);
    	dest.writeString(PostUrl);
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
    	result += "\n    Post Id    : " + getPostId();
    	result += "\n    Board Name : " + getBoardName();
    	result += "\n    Board URL  : " + getBoardUrl();
    	result += "\n    Title      : " + getTitle();
    	result += "\n    Post URL   : " + getPostUrl();
    	result += "\n    Read Count : " + getReadCount();
    	result += "\n    Time stamp : " + getTimeStamp();
    	
    	return result;
    }
}
