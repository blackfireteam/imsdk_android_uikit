package com.masonsoft.imsdk.uikit.drawee;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.masonsoft.imsdk.uikit.widget.AlignImageSpan;
import com.masonsoft.imsdk.uikit.widget.CustomSoftKeyboard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.Preconditions;

public class ViewDraweeSpan extends AlignImageSpan {

    private final DraweeHolder<?> mDraweeHolder;

    public ViewDraweeSpan(
            DraweeHolder<?> draweeHolder, int verticalAlignment) {
        super(draweeHolder.getTopLevelDrawable(), verticalAlignment);
        mDraweeHolder = draweeHolder;
        mDraweeHolder.onAttach();
    }

    public void setTargetView(View targetView) {
        mDraweeHolder.getTopLevelDrawable().setCallback(targetView);
        mDraweeHolder.getTopLevelDrawable().invalidateSelf();
    }

    public static ViewDraweeSpan create(final String assetFilename, int sizePx) {
        final GenericDraweeHierarchy hierarchy =
                GenericDraweeHierarchyBuilder.newInstance(ContextUtil.getContext().getResources())
                        .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .setPlaceholderImage(new ColorDrawable(Color.TRANSPARENT))
                        .setPlaceholderImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .setFailureImage(new ColorDrawable(Color.TRANSPARENT))
                        .setFailureImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .build();
        final DraweeHolder<GenericDraweeHierarchy> draweeHolder = new DraweeHolder<>(hierarchy);
        final PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
        final ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(assetFilename))
                .setCacheChoice(ImageRequest.CacheChoice.SMALL)
                .build();
        builder.setImageRequest(imageRequest);
        builder.setAutoPlayAnimations(false);
        draweeHolder.setController(builder.build());

        final Drawable topLevelDrawable = draweeHolder.getTopLevelDrawable();
        Preconditions.checkNotNull(topLevelDrawable);
        topLevelDrawable.setBounds(0, 0, sizePx, sizePx);

        final ViewDraweeSpan draweeSpan = new ViewDraweeSpan(draweeHolder, ALIGN_CENTER);
        return draweeSpan;
    }

    public static Spannable rebuildTargetViewText(CharSequence text, TextView targetView) {
        final Spannable spannableText;
        if (!(text instanceof Spannable)) {
            spannableText = new SpannableString(text);
        } else {
            spannableText = (Spannable) text;
        }

        spannableText.removeSpan(ViewDraweeSpan.class);
        final Matcher matcher = EMOTION_PATTERN.matcher(spannableText);

        while (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();
            final String emotionName = spannableText.subSequence(start, end).toString();
            if (CustomSoftKeyboard.EmotionLoader.contains(emotionName)) {
                final String assetFilename = CustomSoftKeyboard.EmotionLoader.getAssetValue(emotionName);
                final Paint.FontMetrics fontMetrics = targetView.getPaint().getFontMetrics();
                final int size = (int) ((fontMetrics.descent - fontMetrics.ascent) * 0.95f);
                final ViewDraweeSpan viewDraweeSpan = ViewDraweeSpan.create(assetFilename, size);
                viewDraweeSpan.setTargetView(targetView);
                spannableText.setSpan(
                        viewDraweeSpan,
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        return spannableText;
    }

    public static void rebuildTargetViewText(TextView targetView) {
        final CharSequence text = targetView.getText();
        final Spannable spannable = rebuildTargetViewText(text, targetView);
        if (text != spannable) {
            targetView.setText(spannable);
        }
    }

    private static final Pattern EMOTION_PATTERN = Pattern.compile("\\[[A-Za-z0-9_-]+\\]");

}