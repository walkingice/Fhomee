package org.zeroxlab.Fhome;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

/**
 * Specific {@link AppWidgetHost} that creates our {@link LauncherAppWidgetHostView}
 * which correctly captures all long-press events. This ensures that users can
 * always pick up and move widgets.
 */
public class FhomeAppWidgetHost extends AppWidgetHost {
    public FhomeAppWidgetHost(Context context, int hostId) {
        super(context, hostId);
    }
    
    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
            AppWidgetProviderInfo appWidget) {
        return new FhomeAppWidget(context);
    }
}
