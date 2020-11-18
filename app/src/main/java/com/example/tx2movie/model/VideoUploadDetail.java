package com.example.tx2movie.model;

public class VideoUploadDetail {
    String video_silde, video_type, video_thum, video_url, video_name, video_description, video_category;

    public VideoUploadDetail(String video_silde, String video_type, String video_thum,
                             String video_url, String video_name, String video_description, String video_category) {
        this.video_silde = video_silde;
        this.video_type = video_type;
        this.video_thum = video_thum;
        this.video_url = video_url;
        this.video_name = video_name;
        this.video_description = video_description;
        this.video_category = video_category;
    }

    public String getVideo_silde() {
        return video_silde;
    }

    public void setVideo_silde(String video_silde) {
        this.video_silde = video_silde;
    }

    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(String video_type) {
        this.video_type = video_type;
    }

    public String getVideo_thum() {
        return video_thum;
    }

    public void setVideo_thum(String video_thum) {
        this.video_thum = video_thum;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getVideo_description() {
        return video_description;
    }

    public void setVideo_description(String video_description) {
        this.video_description = video_description;
    }

    public String getVideo_category() {
        return video_category;
    }

    public void setVideo_category(String video_category) {
        this.video_category = video_category;
    }

    public VideoUploadDetail() {
    }
}
