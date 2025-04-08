package org.simpleadblocker.ui.update;

import android.content.Context;

import org.simpleadblocker.R;

/**
 * This class represents the application complete update download status.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class CompleteDownloadStatus implements DownloadStatus {
    @Override
    public int getProgress() {
        return 100;
    }

    @Override
    public String format(Context context) {
        return context.getString(R.string.update_complete_label);
    }
}
