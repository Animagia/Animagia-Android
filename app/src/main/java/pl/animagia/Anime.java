package pl.animagia;

import android.os.Parcel;
import android.os.Parcelable;

public class Anime implements Parcelable {

    static final String NAME_OF_INTENT_EXTRA = "video data";

    private final String title;
    private final String thumbnailAsssetUri;
    private final String posterAssetUri;
    private final String videoUrl;
    private final int episodes;
    private final String timeStamps;
    private final String price;
    private final String genres;
    private final String subtitle;
    private final String duration;
    private final String description;
    private final int previewMillis;
    private final String polishAudio;
    private final String sku;


    Anime(String title, String thumbnailAssetUri, String videoUrl, int episodes,
          String posterAssetUri, String timeStamps, String price, String genres,
          String subtitle, String duration, String description, int previewMillis,
          String polishAudio, String sku) {
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
        this.previewMillis = previewMillis;
        this.polishAudio = polishAudio;
        this.sku = sku;
    }


    protected Anime(Parcel in) {
        title = in.readString();
        thumbnailAsssetUri = in.readString();
        posterAssetUri = in.readString();
        videoUrl = in.readString();
        episodes = in.readInt();
        timeStamps = in.readString();
        price = in.readString();
        genres = in.readString();
        subtitle = in.readString();
        duration = in.readString();
        description = in.readString();
        previewMillis = in.readInt();
        polishAudio = in.readString();
        sku = in.readString();
    }


    public static final Creator<Anime> CREATOR = new Creator<Anime>() {
        @Override
        public Anime createFromParcel(Parcel in) {
            return new Anime(in);
        }


        @Override
        public Anime[] newArray(int size) {
            return new Anime[size];
        }
    };


    @Override
    public String toString() {
        return getTitle();
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

    public int getEpisodeCount() {
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

    public int getPreviewMillis() {
        return previewMillis;
    }

    public String getPolishAudio() {
        return polishAudio;
    }

    public String getSku() {
        return sku;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(thumbnailAsssetUri);
        parcel.writeString(posterAssetUri);
        parcel.writeString(videoUrl);
        parcel.writeInt(episodes);
        parcel.writeString(timeStamps);
        parcel.writeString(price);
        parcel.writeString(genres);
        parcel.writeString(subtitle);
        parcel.writeString(duration);
        parcel.writeString(description);
        parcel.writeInt(previewMillis);
        parcel.writeString(polishAudio);
        parcel.writeString(sku);
    }
}
