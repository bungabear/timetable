package com.bungabear.sche;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

/**
 * Created by Minjae on 2017-02-28.
 */

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "Sche.AppWidgetProvider";
    private SharedPreferences data;
    private String packageName;
    private boolean showSat;
    private String ACTION_CALL_ACTIVITY = "com.bungabear.sche.CALL_ACTIVITY";


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

    }
    enum WeekDays{
        sun,mon,tue,wed,thu,fri,sat
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            // Create an Intent to launch ExampleActivity
//            Intent intent = new Intent(context, WidgetProvider.class);
            RemoteViews views = makeTable(context,appWidgetManager, appWidgetId);
            Intent activityIntent 			= new Intent(ACTION_CALL_ACTIVITY);
            PendingIntent activityPIntent 		= PendingIntent.getBroadcast(context, 0, activityIntent	, 0);
            views.setOnClickPendingIntent(R.id.widget_vertical_linearlayout    , activityPIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        String str = "max : " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) + " " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
                + " min : " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) + " " + newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        Log.d(TAG, "onAppWidgetOptionsChanged: " + str );
        RemoteViews views = makeTable(context,appWidgetManager,appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(ACTION_CALL_ACTIVITY.equals(intent.getAction())){
            Intent newIntent = new Intent("com.bungabear.sche.CALL_ACTIVITY");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }
    }

    private RemoteViews makeTable(Context context, AppWidgetManager appWidgetManager, int appWidgetId){
        packageName = context.getPackageName();
        data = context.getSharedPreferences("data", 0);
        RemoteViews views = new RemoteViews(packageName, R.layout.widget_vertical_linearlayout);
        views.removeAllViews(R.id.widget_vertical_linearlayout);
        Resources resource = context.getResources();
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) ,height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        boolean clearCellInWidget = data.getBoolean("clearCellInWidget", true);
        boolean showWidgetWeekRow = data.getBoolean("showWidgetWeekRow", true);
        boolean showWidgetTimeColumn = data.getBoolean("showWidgetTimeColumn", true);
        int limitRows = data.getInt("limitRows",15);
        int limitColimns = (showSat)? 7 : 6;

        int startRow = 0;
        if(!showWidgetWeekRow){
            startRow = 1;
        }
        int startColumn = 0;
        if(!showWidgetTimeColumn){
            startColumn = 1;
        }
        showSat = data.getBoolean("showSat", true);

        String[][] timetableValue = new String[limitRows][7];
        for(int i = startRow ; i < limitRows ; i++){
            for(int j = startColumn ; j < limitColimns ; j++){
                if(i==0){
                    if(j!=0){
                        //요일을 넣어준다.
                        timetableValue[i][j] = resource.getString(resource.getIdentifier(WeekDays.values()[j].name(),"string", context.getPackageName()));
                    }
                    //저장된 시간부 값이 비어있지않으면 넣어줌.
                } else if(j==0 && data.getString(""+i+j,"").equals("")) {
                    timetableValue[i][0] = (i<10)? ""+i : "N"+(i-9);

                } else {
                    timetableValue[i][j] = data.getString(""+i+j, "");
                }
            }
        }
        int fontsize = 20;
        if(width >550 && height > 550) {
            fontsize = 20;
        } else if(width >450 && height > 450) {
            fontsize = 20;
        } else if(width >330 && height > 330){
            fontsize = 15;
        } else if(width >200 && height > 200){
            fontsize = 10;
        } else if(width >90 && height > 90){
            fontsize = 5;
        }
        Log.d(TAG, "makeTable: setFontSize" + fontsize);

        //cell생성
        for (int j = startRow; j < limitRows; j++) {
            RemoteViews horizentalLinearLayout = new RemoteViews(packageName, R.layout.widget_horizontal_linearlayout);
            for (int k = startColumn; k < limitColimns; k++) {
                int id;
//                if (j == 0 && k == 0) {
//                    이미지 넣고싶은데 안들어감..
//                    RemoteViews imageView = new RemoteViews(packageName, R.layout.widget_mode_cell);
//                    imageView.setImageViewResource(R.id.widget_textview,R.drawable.edit);
//                    horizentalLinearLayout.addView(R.id.widget_horizoontal_linearlayout, imageView);
//                    continue;
//                } else

                // Choose Cell Backbround Color
                if (j == 0 || k == 0) {
                    id = R.layout.widget_top_cell;
                } else if(timetableValue[j][k].equals("") && clearCellInWidget){
                    id = R.layout.widget_empty_cell;
                } else {
                    id = R.layout.widget_cell;
                }
                RemoteViews textView = new RemoteViews(packageName, id);
                textView.setTextViewTextSize(R.id.widget_textview, TypedValue.COMPLEX_UNIT_DIP,fontsize);
                textView.setTextViewText(R.id.widget_textview, timetableValue[j][k]);
                horizentalLinearLayout.addView(R.id.widget_horizoontal_linearlayout, textView);
            }
            views.addView(R.id.widget_vertical_linearlayout, horizentalLinearLayout);
        }
        return views;
    }
}
