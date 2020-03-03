package pl.animagia;

public enum Anime {

    CHUUNIBYOU("Take On Me",
            "https://static.animagia.pl/Chuu_poster.jpg",
            "https://animagia.pl/chuunibyou-demo-koi-ga-shitai-take-on-me/", 1,
            "https://animagia.pl/wp-content/uploads/2018/07/umbrella_for_store_page.png",
            "00:00:29.500;00:33:03.115;01:05:03.115",
            "25,00zł", "Przygoda", "Chuunibyou demo Koi ga Shitai!",
            "93 min.", "Dorastanie jest zbyt mainstreamowe.", 430000),
    AMAGI("Amagi Brilliant Park",
            "https://static.animagia.pl/Amagi4.jpg",
            "https://animagia.pl/amagi-brilliant-park-odc-1/", 13,
            "https://animagia.pl/wp-content/uploads/2018/05/kv-for-store-page.png",
            "00:03:03.115;00:08:03.115",
            "34,90zł", "Przygoda", "",
            "13 × 24 min.", "Najbardziej magiczne miejse na Ziemi.", Integer.MAX_VALUE),
    HANAIRO("Home Sweet Home",
            "https://static.animagia.pl/Hana_poster.jpg",
            "https://animagia.pl", 1,
            "https://animagia.pl/wp-content/uploads/2019/02/HanaIro_store_page.png",
            "00:04:03.115;00:11:03.115",
            "24,90zł", "Obyczajowy", "Hanasaku Iroha:",
            "66 min.", "Chcę lśnić! Ale czy tu mogę zabłysnąć?", 913000),
    KNK_PAST("Przeszłość",
            "https://static.animagia.pl/Past_poster.jpg",
            "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przeszlosc/", 1,
            "https://animagia.pl/wp-content/uploads/2019/03/knk_past_store_page.png",
            "00:03:03.115;00:04:03.115",
            "19,90zł", "Akcja, dramat", "Kyoukai no Kanata –",
            "86 min.", "Początek historii Mirai i Akihito.", 772000),
    KNK_FUTURE("Przyszłość",
            "https://static.animagia.pl/Future_poster.jpg",
            "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przyszlosc/", 1,
            "https://animagia.pl/wp-content/uploads/2019/03/future_store_page.png",
            "00:00:34.019;01:25:31.738;01:28:36.465",
            "19,90zł", "Akcja, dramat", "Kyoukai no Kanata –",
            "89 min.", "Mirai i Akihito walczą o lepszą przyszłość, ale ich moce mają swoją cenę.",
            768000),
    TAMAKO("Tamako Love Story",
            "https://static.animagia.pl/Tamako_poster.jpg",
            "https://animagia.pl/tamako-love-story/", 1,
            "https://animagia.pl/wp-content/uploads/2019/04/Tamako_store_page.png",
            "00:05:03.115;00:08:03.115",
            "19,90zł", "Obyczajowy", "",
            "83 min.", "Czy to miłość? Tak.", 645000);



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

    Anime(String title, String thumbnailAssetUri, String videoUrl, int episodes,
                 String posterAssetUri, String timeStamps, String price, String genres,
                 String subtitle, String duration, String description, int previewMillis) {
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


    @Override
    public String toString() {
        return formatFullTitle();
    }


    static Anime forSku(String sku) {
        for (Anime anime : values()) {
            if(anime.name().equalsIgnoreCase(sku)) {
                return anime;
            }
        }

        throw new IllegalArgumentException("No anime exists with SKU: " + sku);
    }


    static final String NAME_OF_INTENT_EXTRA = "video data";

}
