package net.okjsp.data;

import net.okjsp.Const;
import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Const, Parcelable {
	protected int    CommentId;
	protected String Comment;
	protected int    WriterId;
	protected String WriterName;
	protected String ProfileImageUrl;
	protected String TimeStamp;
	
	public String getComment() {
		return Comment;
	}

	public void setComment(String comment) {
		Comment = comment;
	}

	public String getWriterName() {
		return WriterName;
	}

	public void setWriterName(String writerName) {
		WriterName = writerName;
	}

	public String getProfileImageUrl() {
		return ProfileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		ProfileImageUrl = profileImageUrl;
	}
	
	public int getCommentId() {
		return CommentId;
	}

	public void setCommentId(int commentId) {
		CommentId = commentId;
	}

	public int getWriterId() {
		return WriterId;
	}

	public void setWriterId(int writerId) {
		WriterId = writerId;
	}

	public String getTimeStamp() {
		return TimeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		TimeStamp = timeStamp;
	}

	public Comment() {
		// do nothing
	}
	
	public Comment(Parcel in){
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
    
    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in); 
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };	

    public String toString() {
    	String result = "";
    	
    	result +=   "Writer     : " + getWriterName();
    	result += "\nImageUrl   : " + getProfileImageUrl();
    	result += "\nComment    : " + getComment();
    	
    	return result;
    }
}
