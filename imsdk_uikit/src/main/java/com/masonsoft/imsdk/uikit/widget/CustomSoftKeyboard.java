package com.masonsoft.imsdk.uikit.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.widget.EmojiTextView;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationPickerDialog;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaPickerDialog;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaSelector;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiViewHolderBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerMoreViewHolderBinding;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.util.List;

import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;

public class CustomSoftKeyboard extends FrameLayout {

    public CustomSoftKeyboard(@NonNull Context context) {
        super(context);
        initFromAttributes(context, null, 0, 0);
    }

    public CustomSoftKeyboard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs, 0, 0);
    }

    public CustomSoftKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomSoftKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private ImsdkUikitWidgetCustomSoftKeyboardBinding mBinding;
    // 音视频通话
    private boolean mShowRtc = true;
    // 位置
    private boolean mShowLocation = true;
    // 闪照
    private boolean mShowFlashImage = true;
    // 阅后即焚
    private boolean mShowSnapchat = true;

    private static final String[] MEDIA_PICKER_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String[] FLASH_IMAGE_PICKER_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String[] LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private void initFromAttributes(
            Context context,
            AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {

        mBinding = ImsdkUikitWidgetCustomSoftKeyboardBinding.inflate(
                LayoutInflater.from(context),
                this,
                true);

        initLayerEmoji();
        initLayerMore();

        showLayerEmoji();
    }

    public void showLayerEmoji() {
        ViewUtil.setVisibilityIfChanged(mBinding.layerEmoji, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.layerMore, View.GONE);
    }

    public void showLayerMore() {
        ViewUtil.setVisibilityIfChanged(mBinding.layerEmoji, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.layerMore, View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowRtc(boolean showRtc) {
        if (mShowRtc != showRtc) {
            mShowRtc = showRtc;
            final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowLocation(boolean showLocation) {
        if (mShowLocation != showLocation) {
            mShowLocation = showLocation;
            final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowFlashImage(boolean showFlashImage) {
        if (mShowFlashImage != showFlashImage) {
            mShowFlashImage = showFlashImage;
            final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowSnapchat(boolean showSnapchat) {
        if (mShowSnapchat != showSnapchat) {
            mShowSnapchat = showSnapchat;
            final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public boolean isLayerEmojiShown() {
        return mBinding.layerEmoji.getVisibility() == View.VISIBLE;
    }

    public boolean isLayerMoreShown() {
        return mBinding.layerMore.getVisibility() == View.VISIBLE;
    }

    /**
     * 硬编码自定义键盘上的数据
     */
    private final static class CustomKeyboardDataBuiltin {

        // 1F600-1F64F
        static final int EMOJI_START = 0x1F600;
        static final int EMOJI_END = 0x1F64F;
        static final int EMOJI_COUNT = EMOJI_END - EMOJI_START + 1;
        static final String[] EMOJI = new String[EMOJI_COUNT];

        static {
            for (int i = 0; i < EMOJI_COUNT; i++) {
                EMOJI[i] = new String(Character.toChars(i + EMOJI_START));
            }
        }
    }

    /**
     * 自定义键盘：表情
     */
    private void initLayerEmoji() {
        mBinding.layerEmojiPager.setAdapter(new LayerEmojiPagerAdapter());
    }

    /**
     * 自定义键盘：更多
     */
    private void initLayerMore() {
        mBinding.layerMorePager.setAdapter(new LayerMorePagerAdapter());
    }

    public interface OnInputListener {
        void onInputText(CharSequence text);

        void onDeleteOne();

        void onMediaPicked(@NonNull List<MediaData.MediaInfo> mediaInfoList);

        void onFlashImagePicked(@NonNull List<MediaData.MediaInfo> mediaInfoList);

        void onClickRtcAudio();

        void onClickRtcVideo();

        void onLocationPicked(@NonNull LocationInfo locationInfo, long zoom);

        void onClickSnapchatMode();
    }

    private OnInputListener mOnInputListener;

    public void setOnInputListener(OnInputListener onInputListener) {
        mOnInputListener = onInputListener;
    }

    private class LayerEmojiPagerAdapter extends RecyclerView.Adapter<LayerEmojiViewHolder> {

        @NonNull
        @Override
        public LayerEmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            return new LayerEmojiViewHolder(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_view_holder, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull LayerEmojiViewHolder holder, int position) {
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    private class LayerEmojiViewHolder extends RecyclerView.ViewHolder {

        private final ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiViewHolderBinding mBinding;

        public LayerEmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            mBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiViewHolderBinding.bind(itemView);
            final Context context = getContext();
            final String[] allEmoji = CustomKeyboardDataBuiltin.EMOJI;
            final int columns = context.getResources().getInteger(R.integer.imsdk_uikit_widget_custom_soft_keyboard_emoji_columns);
            final int size = allEmoji.length;
            final int itemViewWidth = DimenUtil.dp2px(30);
            final int itemViewHeight = DimenUtil.dp2px(30);
            mBinding.gridLayout.setColumnCount(columns);
            mBinding.gridLayout.setUseDefaultMargins(true);
            ViewUtil.onClick(mBinding.actionDelete, 100, v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onDeleteOne();
                }
            });

            for (String emoji : allEmoji) {
                inflateEmojiItemView(context, itemViewWidth, itemViewHeight, emoji);
            }
        }

        private void inflateEmojiItemView(Context context, int itemViewWidth, int itemViewHeight, final String emojiText) {
            final EmojiTextView itemView = new EmojiTextView(context);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = itemViewWidth;
            lp.height = itemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            itemView.setLayoutParams(lp);

            itemView.setIncludeFontPadding(false);
            itemView.setGravity(Gravity.CENTER);
            itemView.setText(emojiText);
            itemView.setTextColor(0xFF333333);
            itemView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);

            mBinding.gridLayout.addView(itemView);

            ViewUtil.onClick(itemView, 100, v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onInputText(emojiText);
                }
            });
        }

    }

    private class LayerMorePagerAdapter extends RecyclerView.Adapter<LayerMoreViewHolder> {

        @NonNull
        @Override
        public LayerMoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            return new LayerMoreViewHolder(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_more_view_holder, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull LayerMoreViewHolder holder, int position) {
            holder.onBind(position);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    private class LayerMoreViewHolder extends RecyclerView.ViewHolder {

        private final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreViewHolderBinding mBinding;
        private final int mItemViewWidth = DimenUtil.dp2px(50);
        private final int mItemViewHeight = DimenUtil.dp2px(50);

        public LayerMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            mBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerMoreViewHolderBinding.bind(itemView);
            mBinding.gridLayout.setColumnCount(4);
            mBinding.gridLayout.setRowCount(2);
        }

        public void onBind(int position) {
            final Context context = getContext();
            mBinding.gridLayout.removeAllViews();

            final int count = 4 * 2;
            int start = 0;
            {
                start++;
                inflateMediaItemView(context);
            }
            {
                if (mShowFlashImage) {
                    start++;
                    inflateFlashImageItemView(context);
                }
            }
            {
                if (mShowRtc) {
                    start++;
                    inflateRtcAudioItemView(context);
                }
            }
            {
                if (mShowRtc) {
                    start++;
                    inflateRtcVideoItemView(context);
                }
            }
            {
                if (mShowLocation) {
                    start++;
                    inflateLocationItemView(context);
                }
            }
            {
                if (mShowSnapchat) {
                    start++;
                    inflateSnapchatItemView(context);
                }
            }
            for (int i = start; i < count; i++) {
                inflateMoreEmptyItemView(context);
            }
        }

        private void inflateMediaItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_media);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_media);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                requestMediaPickerPermission();
            });
        }

        private void inflateFlashImageItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_media);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_flash_image);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                requestFlashImagePickerPermission();
            });
        }

        private void inflateRtcAudioItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_voice_call);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_rtc_audio);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onClickRtcAudio();
                }
            });
        }

        private void inflateRtcVideoItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_video_call);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_rtc_video);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onClickRtcVideo();
                }
            });
        }

        private void inflateLocationItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_location);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_location);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                requestLocationPermission();
            });
        }

        private void inflateSnapchatItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_snapchat);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_snapchat);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onClickSnapchatMode();
                }
            });
        }

        private void inflateMoreEmptyItemView(Context context) {
            final Space itemView = new Space(context);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            itemView.setLayoutParams(lp);

            mBinding.gridLayout.addView(itemView);
        }

    }

    private void requestMediaPickerPermission() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final RxPermissions rxPermissions = new RxPermissions(activity);
        mPermissionRequest.set(
                rxPermissions.request(MEDIA_PICKER_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onMediaPickerPermissionGranted();
                            } else {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onMediaPickerPermissionGranted() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final MediaPickerDialog mediaPickerDialog = new MediaPickerDialog(activity, activity.findViewById(Window.ID_ANDROID_CONTENT));
        mediaPickerDialog.setOnMediaPickListener(mediaInfoList -> {
            if (mediaInfoList.isEmpty()) {
                return false;
            }

            for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
                if (!mediaInfo.isImageMimeType()
                        && !mediaInfo.isVideoMimeType()) {
                    Throwable e = new Throwable("unknown mime type:" + mediaInfo.mimeType + ", uri:" + mediaInfo.uri);
                    MSIMUikitLog.e(e);
                    return false;
                }
            }

            if (mOnInputListener != null) {
                mOnInputListener.onMediaPicked(mediaInfoList);
            }

            return true;
        });
        mediaPickerDialog.show();
    }

    private void requestFlashImagePickerPermission() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final RxPermissions rxPermissions = new RxPermissions(activity);
        mPermissionRequest.set(
                rxPermissions.request(FLASH_IMAGE_PICKER_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onFlashImagePickerPermissionGranted();
                            } else {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onFlashImagePickerPermissionGranted() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final MediaPickerDialog mediaPickerDialog = new MediaPickerDialog(activity, activity.findViewById(Window.ID_ANDROID_CONTENT));
        mediaPickerDialog.setMediaSelector(new MediaSelector.SimpleMediaSelector() {
            @Override
            public boolean accept(@NonNull MediaData.MediaInfo info) {
                return info.isImageMimeType();
            }
        });
        mediaPickerDialog.setOnMediaPickListener(mediaInfoList -> {
            if (mediaInfoList.isEmpty()) {
                return false;
            }

            for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
                if (!mediaInfo.isImageMimeType()) {
                    Throwable e = new Throwable("unexpected mime type:" + mediaInfo.mimeType + ", uri:" + mediaInfo.uri);
                    MSIMUikitLog.e(e);
                    return false;
                }
            }

            if (mOnInputListener != null) {
                mOnInputListener.onFlashImagePicked(mediaInfoList);
            }

            return true;
        });
        mediaPickerDialog.show();
    }

    private void requestLocationPermission() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final RxPermissions rxPermissions = new RxPermissions(activity);
        mPermissionRequest.set(
                rxPermissions.request(LOCATION_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onLocationPermissionGranted();
                            } else {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onLocationPermissionGranted() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final LocationPickerDialog locationPickerDialog = new LocationPickerDialog(
                activity, activity.findViewById(Window.ID_ANDROID_CONTENT)
        );
        locationPickerDialog.setOnLocationPickListener((locationInfo, zoom) -> {
            if (mOnInputListener != null) {
                mOnInputListener.onLocationPicked(locationInfo, zoom);
            }
            return true;
        });
        locationPickerDialog.show();
    }

}
