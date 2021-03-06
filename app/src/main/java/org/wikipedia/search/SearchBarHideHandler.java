package org.wikipedia.search;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import org.wikipedia.R;
import org.wikipedia.ViewAnimations;
import org.wikipedia.util.ArgbEvaluatorCompat;
import org.wikipedia.util.GradientUtil;
import org.wikipedia.views.ObservableWebView;
import org.wikipedia.views.ViewUtil;

public class SearchBarHideHandler implements ObservableWebView.OnScrollChangeListener,
        ObservableWebView.OnUpOrCancelMotionEventListener,
        ObservableWebView.OnDownMotionEventListener {
    private static final int HUMAN_SCROLL_THRESHOLD = 200;
    private static final int FULL_OPACITY = 255;
    @NonNull private final View quickReturnView;
    private final float displayDensity;
    @NonNull private final ArgbEvaluatorCompat colorEvaluator;

    @NonNull private final Context context;
    private ObservableWebView webview;
    private boolean fadeEnabled;
    private boolean forceNoFade;
    @NonNull private final Drawable toolbarBackground;
    @NonNull private final PaintDrawable toolbarGradient = new PaintDrawable();
    @NonNull private final Drawable toolbarShadow;
    @NonNull private final Drawable statusBar;

    public SearchBarHideHandler(@NonNull Activity activity, @NonNull View quickReturnView) {
        context = activity;
        this.quickReturnView = quickReturnView;
        this.displayDensity = getResources().getDisplayMetrics().density;

        toolbarBackground = quickReturnView.findViewById(R.id.main_toolbar).getBackground().mutate();
        toolbarShadow = quickReturnView.findViewById(R.id.main_toolbar_shadow).getBackground().mutate();
        ViewUtil.setBackgroundDrawable(quickReturnView, toolbarBackground);
        initToolbarGradient();

        colorEvaluator = new ArgbEvaluatorCompat();
        statusBar = quickReturnView.findViewById(R.id.empty_status_bar).getBackground().mutate();
    }

    /**
     * Update the WebView based on whose scroll position the search bar will hide itself.
     * @param webView The WebView against which scrolling will be tracked.
     */
    public void setScrollView(ObservableWebView webView) {
        webview = webView;
        webview.addOnScrollChangeListener(this);
        webview.addOnDownMotionEventListener(this);
        webview.addOnUpOrCancelMotionEventListener(this);
    }

    /**
     * Whether to enable fading in/out of the search bar when near the top of the article.
     * @param enabled True to enable fading, false otherwise.
     */
    public void setFadeEnabled(boolean enabled) {
        fadeEnabled = enabled;
        update();
    }

    /**
     * Whether to temporarily disable fading of the search bar, even if fading is enabled otherwise.
     * May be used when displaying a temporary UI element that requires the search bar to be shown
     * fully, e.g. when the ToC is pulled out.
     * @param force True to temporarily disable fading, false otherwise.
     */
    public void setForceNoFade(boolean force) {
        forceNoFade = force;
        update();
    }

    /**
     * Force an update of the appearance of the search bar. Usually it is updated automatically
     * when the associated WebView is scrolled, but this function may be used to manually refresh
     * the appearance of the search bar, e.g. when the WebView is first shown.
     */
    public void update() {
        if (webview == null) {
            return;
        }
        onScrollChanged(webview.getScrollY(), webview.getScrollY());
    }

    @Override
    public void onScrollChanged(int oldScrollY, int scrollY) {
        int opacity = calculateScrollOpacity(scrollY);
        toolbarBackground.setAlpha(opacity);
        toolbarShadow.setAlpha(opacity);
        toolbarGradient.setAlpha(FULL_OPACITY - opacity);

        int color = calculateScrollStatusBarColorTween(opacity);
        statusBar.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        if (scrollY <= webview.getHeight()) {
            // For the first screenful, ensure it always exists.
            ViewAnimations.ensureTranslationY(quickReturnView, 0);
            return;
        }
        int animMargin;
        if (oldScrollY > scrollY) {
            int minMargin = 0;
            int scrollDelta = oldScrollY - scrollY;
            int newMargin = (int) ViewHelper.getTranslationY(quickReturnView) + scrollDelta;
            animMargin = Math.min(minMargin, newMargin);
        } else {
            // scroll down!
            int scrollDelta = scrollY - oldScrollY;
            if (scrollDelta > (int) (HUMAN_SCROLL_THRESHOLD * displayDensity)) {
                // we've been scrolled programmatically, probably to go to
                // a specific section, so keep the toolbar shown.
                animMargin = 0;
            } else {
                int minMargin = -quickReturnView.getHeight();
                int newMargin = (int) ViewHelper.getTranslationY(quickReturnView) - scrollDelta;
                animMargin = Math.max(minMargin, newMargin);
            }
        }
        ViewHelper.setTranslationY(quickReturnView, animMargin);
    }

    @Override
    public void onUpOrCancelMotionEvent() {
        int transY = (int)ViewHelper.getTranslationY(quickReturnView);
        int height = quickReturnView.getHeight();
        if (transY != 0 && transY > -height) {
            if (transY > -height / 2) {
                // Fully display it
                ViewAnimations.ensureTranslationY(quickReturnView, 0);
            } else {
                // Fully hide it
                ViewAnimations.ensureTranslationY(quickReturnView, -height);
            }
        }
    }

    @Override
    public void onDownMotionEvent() {
        // Don't do anything for now
    }

    private void initToolbarGradient() {
        @ColorInt int baseColor = getColor(R.color.lead_gradient_start);
        GradientUtil.setCubicGradient(toolbarGradient, baseColor, Gravity.TOP);
    }

    /** @return Alpha value between 0 and 0xff. */
    private int calculateScrollOpacity(int scrollY) {
        final int fadeHeight = 256;
        int opacity = FULL_OPACITY;
        if (fadeEnabled && !forceNoFade) {
            opacity = scrollY * FULL_OPACITY / (int) (fadeHeight * displayDensity);
        }
        opacity = Math.max(0, opacity);
        opacity = Math.min(FULL_OPACITY, opacity);
        return opacity;
    }

    @ColorInt private int calculateScrollStatusBarColorTween(int opacity) {
        final int alphaMask = 0xff000000;
        return (int) colorEvaluator.evaluate((float) opacity / FULL_OPACITY,
                toolbarColor() & ~alphaMask, toolbarColor());
    }

    @ColorInt private int toolbarColor() {
        return getColor(R.color.actionbar_background);
    }

    @ColorInt private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    @NonNull private Resources getResources() {
        return context.getResources();
    }
}
