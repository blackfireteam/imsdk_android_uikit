package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.masonsoft.imsdk.MSIMAudioElement;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayer;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayerDelegate;
import com.masonsoft.imsdk.uikit.common.microlifecycle.real.Real;

import io.github.idonans.lang.util.ViewUtil;

public class MSIMBaseMessageAudioView extends MicroLifecycleFrameLayout implements Real {

    public MSIMBaseMessageAudioView(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageAudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageAudioView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    private MSIMBaseMessage mBaseMessage;

    private AudioPlayerView mAudioPlayerView;

    @Nullable
    private MediaPlayerDelegate mMediaPlayerDelegate;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setContentView(R.layout.imsdk_uikit_widget_im_base_message_audio_view);
        mAudioPlayerView = findViewById(R.id.audio_player_view);

        ViewUtil.onClick(mAudioPlayerView, v -> {
            togglePlayer();
            performClick();
        });
        mAudioPlayerView.setOnLongClickListener(v -> {
            performLongClick();
            return true;
        });
        mMediaPlayerDelegate = new MediaPlayerDelegate();
        setDefaultManual(true);
    }

    public void setOnPlayerStateUpdateListener(@Nullable AudioPlayerView.OnPlayerStateUpdateListener listener) {
        mAudioPlayerView.setOnPlayerStateUpdateListener(listener);
    }

    @Override
    public void forcePause() {
        if (isResumed()) {
            toggleWithManual();
        }
    }

    private void togglePlayer() {
        if (isResumed()
                && mMediaPlayerDelegate != null) {
            MediaPlayer mediaPlayer = mMediaPlayerDelegate.getMediaPlayer();
            if (mediaPlayer != null) {
                SimpleExoPlayer player = mediaPlayer.getPlayer();
                if (player != null) {
                    if (!canPausePlayer(player)) {
                        // 正常播放结束或者播放失败时点击重新开始播放
                        mMediaPlayerDelegate.initPlayerIfNeed(mAudioPlayerView, getAudioUrl(), true, isResumed(), false);
                        return;
                    }
                }
            }
        }

        toggleWithManual();
    }

    private boolean canPausePlayer(SimpleExoPlayer player) {
        return player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    public void setBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        mBaseMessage = baseMessage;

        long durationMs = 0L;
        final MSIMAudioElement element = baseMessage.getAudioElement();
        if (element != null) {
            durationMs = element.getDurationMs();
        }
        mAudioPlayerView.setDurationMs(durationMs);

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.initPlayerIfNeed(mAudioPlayerView, getAudioUrl(), true, isResumed(), false);
        }
    }

    @Nullable
    private String getAudioUrl() {
        if (mBaseMessage != null) {
            final MSIMAudioElement element = mBaseMessage.getAudioElement();
            if (element != null) {
                return element.getUrl();
            }
        }
        return null;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.initPlayerIfNeed(mAudioPlayerView, getAudioUrl(), true, isResumed(), false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.pausePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.releasePlayer();
        }
    }

}
