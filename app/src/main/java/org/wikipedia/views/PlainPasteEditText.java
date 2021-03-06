package org.wikipedia.views;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.content.ClipboardManager;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import org.wikipedia.util.ApiUtil;
import org.wikipedia.util.ClipboardUtil;

public class PlainPasteEditText extends AppCompatEditText {
    public PlainPasteEditText(Context context) {
        super(context);
    }

    public PlainPasteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlainPasteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (ApiUtil.hasHoneyComb() && id == android.R.id.paste) {
            return onTextContextMenuPaste();
        }
        return super.onTextContextMenuItem(id);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean onTextContextMenuPaste() {
        // Do not allow pasting of formatted text!
        // We do this by intercepting the clipboard and temporarily replacing its
        // contents with plain text.
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData oldClipData = clipboard.getPrimaryClip();
            String lastClipText = oldClipData.getItemAt(oldClipData.getItemCount() - 1).coerceToText(getContext()).toString();
            // temporarily set the new clip data as the primary
            ClipboardUtil.setPlainText(getContext(), null, lastClipText);
            // execute the paste!
            super.onTextContextMenuItem(android.R.id.paste);
            // restore the clip data back to the old one.
            clipboard.setPrimaryClip(oldClipData);
        }
        return true;
    }

}
