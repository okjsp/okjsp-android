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
	
	public int getPostId() {
		return PostId;
	}

	public Post setPostId(int postId) {
		PostId = postId;
		return this;
	}

	public String getBoardName() {
		return BoardName;
	}

	public Post setBoardName(String boardName) {
		BoardName = boardName;
		return this;
	}

	public String getBoardUrl() {
		return BoardUrl;
	}

	public Post setBoardUrl(String boardUrl) {
		BoardUrl = boardUrl;
		return this;
	}

	public String getTitle() {
		return Title;
	}

	public Post setTitle(String title) {
		Title = title;
		return this;
	}

	public String getPostUrl() {
		return TextUtils.isEmpty(PostUrl) ? null : BASE_URL + PostUrl;
	}

	public Post setPostUrl(String postUrl) {
		PostUrl = postUrl;
		return this;
	}

	public String getWriterName() {
		return WriterName;
	}

	public Post setWriterName(String writerName) {
		WriterName = writerName;
		return this;
	}

	public String getProfileImageUrl() {
		return TextUtils.isEmpty(ProfileImageUrl) ? null : (BASE_URL + ProfileImageUrl);
	}

	public Post setProfileImageUrl(String rofileImageUrl) {
		ProfileImageUrl = rofileImageUrl;
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
		TimeStamp = timeStamp;
		return this;
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
}
