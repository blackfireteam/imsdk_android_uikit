package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.masonsoft.imsdk.uikit.MSIMSessionUserIdChangedHelper;

@Deprecated
public class MSIMSessionUserInfoGenderView extends MSIMUserInfoGenderView {

    @SuppressWarnings("FieldCanBeLocal")
    private MSIMSessionUserIdChangedHelper mMSIMSessionUserIdChangedHelper;

    public MSIMSessionUserInfoGenderView(Context context) {
        this(context, null);
    }

    public MSIMSessionUserInfoGenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMSessionUserInfoGenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMSessionUserInfoGenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mMSIMSessionUserIdChangedHelper = new MSIMSessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                MSIMSessionUserInfoGenderView.this.setUserInfo(sessionUserId, null);
            }
        };

        MSIMSessionUserInfoGenderView.this.setUserInfo(mMSIMSessionUserIdChangedHelper.getSessionUserId(), null);
    }

}
