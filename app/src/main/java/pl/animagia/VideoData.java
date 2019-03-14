package pl.animagia;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoData implements Parcelable {

    public static final String NAME_OF_INTENT_EXTRA = "video data";
    public static final String NAME_OF_URL = "video url";


    private final String title;
    private final String thumbnailAsssetUri;
    private final String posterAssetUri;
    private final String videoUrl;
    private final int episodes;

    public VideoData(String title, String thumbnailAssetUri, String videoUrl, int episodes,
                     String posterAssetUri) {
        this.title = title;
        this.thumbnailAsssetUri = thumbnailAssetUri;
        this.videoUrl = videoUrl;
        this.episodes = episodes;
        this.posterAssetUri = posterAssetUri;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailAsssetUri() {
        return thumbnailAsssetUri;
    }

    public String getPosterAsssetUri() {
        return posterAssetUri;
    }

    public String getVideoUrl(){
        return videoUrl;
    }

    public int getEpisodes() {
        return episodes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VideoData) {
            return title.equals(((VideoData) obj).getTitle()) &&
                    thumbnailAsssetUri.equals(((VideoData) obj).getThumbnailAsssetUri());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return title.hashCode() ^ thumbnailAsssetUri.hashCode();
    }

    @Override
    public String toString() {
        return title;
    }

    protected VideoData(Parcel in) {
        this.thumbnailAsssetUri = in.readString();
        this.title = in.readString();
        this.videoUrl = in.readString();
        this.episodes = in.readInt();
        this.posterAssetUri = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbnailAsssetUri);
        dest.writeString(title);
        dest.writeString(videoUrl);
        dest.writeInt(episodes);
        dest.writeString(posterAssetUri);
    }

    public static final Creator<VideoData> CREATOR = new Creator<VideoData>() {
        @Override
        public VideoData createFromParcel(Parcel in) {
            return new VideoData(in);
        }

        @Override
        public VideoData[] newArray(int size) {
            return new VideoData[size];
        }
    };

}
