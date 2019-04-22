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
    private String timeStamps;
    private final String price;
    private final String genres;
    private final String subtitle;
    private final String duration;
    private final String description;

    public VideoData(String title, String thumbnailAssetUri, String videoUrl, int episodes,
                     String posterAssetUri, String timeStamps, String price, String genres,
                     String subtitle, String duration, String description) {
        this.title = title;
        this.thumbnailAsssetUri = thumbnailAssetUri;
        this.videoUrl = videoUrl;
        this.episodes = episodes;
        this.posterAssetUri = posterAssetUri;
        this.timeStamps = timeStamps;
        this.genres = genres;
        this.price = price;
        this.subtitle = subtitle;
        this.duration = duration;
        this.description = description;
    }

    public String formatFullTitle() {
        return subtitle.isEmpty() ? title : subtitle + " " + title;
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

    public String getTimeStamps() {
        return timeStamps;
    }

    public String getPrice() {
        return price;
    }

    public String getGenres() {
        return genres;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
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
        this.timeStamps = in.readString();
        this.price = in.readString();
        this.genres = in.readString();
        this.subtitle = in.readString();
        this.duration = in.readString();
        this.description = in.readString();
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
        dest.writeString(timeStamps);
        dest.writeString(price);
        dest.writeString(genres);
        dest.writeString(subtitle);
        dest.writeString(duration);
        dest.writeString(description);
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
