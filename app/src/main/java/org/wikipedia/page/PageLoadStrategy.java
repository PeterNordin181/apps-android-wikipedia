package org.wikipedia.page;

import org.wikipedia.bridge.CommunicationBridge;
import org.wikipedia.editing.EditHandler;
import org.wikipedia.page.leadimages.LeadImagesHandler;
import org.wikipedia.search.SearchBarHideHandler;
import org.wikipedia.views.ObservableWebView;
import org.wikipedia.views.SwipeRefreshLayoutWithScroll;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Defines interaction between PageFragment and an implementation that loads a page
 * for viewing.
 */
public interface PageLoadStrategy {
    void setup(PageViewModel model, PageFragment fragment,
               SwipeRefreshLayoutWithScroll refreshView, ObservableWebView webView,
               CommunicationBridge bridge, SearchBarHideHandler searchBarHideHandler,
               LeadImagesHandler leadImagesHandler);

    void onActivityCreated(@NonNull List<PageBackStackItem> backStack);

    void backFromEditing(Intent data);

    void onDisplayNewPage(boolean pushBackStack, boolean tryFromCache, int stagedScrollY);

    boolean isLoading();

    void onHidePageContent();

    boolean onBackPressed();

    void setEditHandler(EditHandler editHandler);

    void setBackStack(@NonNull List<PageBackStackItem> backStack);

    void updateCurrentBackStackItem();

    void loadPageFromBackStack();
}
