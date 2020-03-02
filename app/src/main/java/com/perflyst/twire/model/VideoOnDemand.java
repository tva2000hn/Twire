package com.perflyst.twire.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.perflyst.twire.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Sebastian Rask on 16-06-2016.
 */
public class VideoOnDemand implements Comparable<VideoOnDemand>, Parcelable, MainElement {
    public static final Parcelable.Creator<VideoOnDemand> CREATOR = new ClassLoaderCreator<VideoOnDemand>() {
        @Override
        public VideoOnDemand createFromParcel(Parcel source) {
            return new VideoOnDemand(source);
        }

        @Override
        public VideoOnDemand createFromParcel(Parcel source, ClassLoader loader) {
            return new VideoOnDemand(source);
        }

        @Override
        public VideoOnDemand[] newArray(int size) {
            return new VideoOnDemand[size];
        }
    };
    private String videoTitle,
            gameTitle,
            previewUrl,
            videoId,
            channelName,
            displayName,
            recordedAtString;
    private int views,
            length;
    private boolean isBroadcast;
    private Calendar recordedAt;
    private ChannelInfo channelInfo;

    public VideoOnDemand(String videoTitle, String gameTitle, String previewUrl, String videoId,
                         String channelName, String displayName, int views, int length, String recordedAt) {
        this.videoTitle = videoTitle;
        this.gameTitle = gameTitle;
        this.previewUrl = previewUrl;
        this.videoId = videoId;
        this.channelName = channelName;
        this.displayName = displayName;
        this.views = views;
        this.length = length;
        this.recordedAtString = recordedAt;

        setRecordedAtDate(recordedAt);
    }

    private VideoOnDemand(Parcel in) {
        String[] data = new String[10];
        in.readStringArray(data);

        this.videoTitle = data[0];
        this.gameTitle = data[1];
        this.previewUrl = data[2];
        this.videoId = data[3];
        this.channelName = data[4];
        this.displayName = data[5];
        this.recordedAtString = data[6];
        this.views = Integer.valueOf(data[7]);
        this.length = Integer.valueOf(data[8]);
        this.isBroadcast = Boolean.valueOf(data[9]);
        this.channelInfo = in.readParcelable(ChannelInfo.class.getClassLoader());

        setRecordedAtDate(this.recordedAtString);
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        isBroadcast = broadcast;
    }

    private void setRecordedAtDate(String recordedAtString) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            this.recordedAt = Calendar.getInstance();
            this.recordedAt.setTime(formatter.parse(recordedAtString.split("T")[0]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] toSend = new String[]{
                this.videoTitle,
                this.gameTitle,
                this.previewUrl,
                this.videoId,
                this.channelName,
                this.displayName,
                this.recordedAtString,
                String.valueOf(this.views),
                String.valueOf(this.length),
                String.valueOf(this.isBroadcast)
        };

        dest.writeStringArray(toSend);
        dest.writeParcelable(channelInfo, flags);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStreamerName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    private String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Calendar getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Calendar recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public int compareTo(@NonNull VideoOnDemand another) {
        return (int) (recordedAt.getTimeInMillis() - another.recordedAt.getTimeInMillis());
    }

    @Override
    public String getHighPreview() {
        return getPreviewUrl();
    }

    @Override
    public String getMediumPreview() {
        return getPreviewUrl();
    }

    @Override
    public String getLowPreview() {
        return getPreviewUrl();
    }

    @Override
    public int getPlaceHolder(Context context) {
        return R.drawable.template_stream;
    }
}
