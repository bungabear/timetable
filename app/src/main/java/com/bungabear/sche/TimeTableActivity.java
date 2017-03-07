package com.bungabear.sche;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;


public class TimeTableActivity extends Activity implements View.OnClickListener, TextView.OnEditorActionListener, View.OnFocusChangeListener {
    public static final String TAG = "ScheTest";
    private LinearLayout mainLinearLayout;
    private GridLayout gridLayout;
    private ScrollView scrollView;
    private EditText editTexts[][] = new EditText[20][7];
    private CellValue[][] timetableValue;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferenceEditor;
    private int limitColumns, limitRows;
    private boolean clearCellInWidget;
    private boolean showSat;
    private boolean STORAGE_WRITE_PERMISSION = false;

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "onEditorAction: " + actionId);
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int[] tags = new int[]{v.getId()/10, v.getId()%10};
        String text = ((EditText) v).getText().toString();
//		Log.d(TAG, "onEditorAction: " + tags[0] + tags[1] + text + hasFocus);
        //포커스를 잃은 칸이 둘쨋줄 이후고 값이 period이면 값복사
        if (!hasFocus && tags[0] >= 2 && text.equals(".")) {
            ((EditText) v).setText(editTexts[tags[0] - 1][tags[1]].getText().toString());
        }
    }

    enum WeekDays {
        sun, mon, tue, wed, thu, fri, sat
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //스토리지 저장 권한이 있는지 체크.
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            // 권한 없음
        }else{
            STORAGE_WRITE_PERMISSION = true;
            // 권한 있음
        }

        sharedPreferences = getSharedPreferences("data", 0);
        preferenceEditor = sharedPreferences.edit();
        showSat = sharedPreferences.getBoolean("showSat", true);
        if (showSat) {
            limitColumns = 7;
        } else {
            limitColumns = 6;
        }

        if(sharedPreferences.getInt("limitRows", 0) != 0){
            limitRows = sharedPreferences.getInt("limitRows", 0);
        } else {
            limitRows = 15;
        }

        clearCellInWidget = sharedPreferences.getBoolean("clearCellInWidget", true);



        initTable(false);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void onClick(final View v) {
        // Todo 항목을 선택하여 연속적인 과목을 등록 할 수 있도록 추가.
        // Todo 특정 시간 간격을 조정하고, 특정 시간대를 삭제하는 기능 추가
        final int id = v.getId();
        if(id > limitColumns) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("시간표 수정");

            final boolean[] spaned = new boolean[]{ false };
            final int spansize = timetableValue[id/10][id%10].spansize;
            // Is time name cell
            if(id%10 == 0){
                alert.setMessage( id/10 + "번째 시간이름 수정");

                // Is data cell
            } else {
                String message;
                if(spansize > 1){
                    message = getResources().getString(getResources().getIdentifier(WeekDays.values()[id%10].name(),"string",getPackageName())) + "요일 "
                            + (id/10 - spansize + 1) + " ~ " + id/10 + "교시";
                    spaned[0] = true;
                } else {
                    message = getResources().getString(getResources().getIdentifier(WeekDays.values()[id%10].name(),"string",getPackageName())) + "요일 " + id/10 + "교시";
                }
                alert.setMessage(message);
            }

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setText(((EditText)v).getText());
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    for(int i = id/10 ; i > id/10 - spansize; i--){
                        editTexts[i][id%10].setText(input.getText().toString());
                        preferenceEditor.putString(""+i+id%10,input.getText().toString());
                    }
                    preferenceEditor.apply();
                    initTable(false);
                }
            });

            alert.setNeutralButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
            alert.setNegativeButton("Delete",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    for(int i = id/10 ; i > id/10 - spansize; i--){
                        editTexts[i][id%10].setText("");
                        preferenceEditor.putString(""+i+(id%10),"");
                    }
                    preferenceEditor.apply();
                    initTable(false);
                }
            });
            alert.show();

            // Todo Show keyboard automatically
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, 0);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(2).setChecked(clearCellInWidget);
        menu.getItem(3).setChecked(sharedPreferences.getBoolean("showWidgetWeekRow", true));
        menu.getItem(4).setChecked(sharedPreferences.getBoolean("showWidgetTimeColumn", true));
        menu.getItem(6).setChecked(showSat);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Todo 테이블 색상 변경 기능 추가
        // Todo 시간 간격을 조정 할 수 있는 메뉴 추가
        boolean check;
        switch (item.getItemId()) {

            // Backup preference data.
            case R.id.item0:
                // 마시멜로우 이상이고 권한이 없으면 권한 요청
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(!STORAGE_WRITE_PERMISSION){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }
                }
                File fileDestination = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "timetable.backup");
                saveSharedPreferencesToFile(fileDestination);
                break;

            // Restore preference data.
            case R.id.item1:
                //마시멜로우 이상이고 권한이 없으면 권한 요청
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(!STORAGE_WRITE_PERMISSION){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }
                }
                File fileSource = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "timetable.backup");
                loadSharedPreferencesFromFile(fileSource);
                initTable(false);
                break;

            // Make widget's empty cell trasnparent
            case R.id.item2:

                // User uncheck -> check trasnparent option.
                if(!item.isChecked()){
                    check = true;
                    // User check -> uncheck trasnparent option.
                } else {
                    check = false;
                }
                preferenceEditor.putBoolean("clearCellInWidget",check);
                preferenceEditor.apply();
                item.setChecked(check);
                requestWidgetUpdate();
                break;

            // User can select to show widget's weekend row
            case R.id.item3:

                // User uncheck -> check show widget's weekend row
                if(!item.isChecked()){
                    check = true;
                    // User uncheck -> check show widget's weekend row
                } else {
                    check = false;
                }
                preferenceEditor.putBoolean("showWidgetWeekRow",check);
                preferenceEditor.apply();
                item.setChecked(check);
                requestWidgetUpdate();

                break;

            // User can select to show widget's timename row
            case R.id.item4:

                // User uncheck -> check widget's timename row
                if(!item.isChecked()){
                    check = true;
                    // User uncheck -> check widget's timename row
                } else {
                    check = false;
                }
                preferenceEditor.putBoolean("showWidgetTimeColumn",check);
                preferenceEditor.apply();
                item.setChecked(check);
                requestWidgetUpdate();

                break;

            // User select table row.
            case R.id.item5:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("시간표 행 수정");
                final NumberPicker numberPicker = new NumberPicker(this);
                numberPicker.setMinValue(9);
                numberPicker.setMaxValue(19);
                numberPicker.setValue(sharedPreferences.getInt("limitRows",15)-1);
                // Set an EditText view to get user input
                alert.setView(numberPicker);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        limitRows = numberPicker.getValue()+1;
                        preferenceEditor.putInt("limitRows", limitRows);
                        preferenceEditor.apply();
                        initTable(false);
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();

                break;

            // Show Saturday option.
            case R.id.item6:
                // User uncheck -> check Show Saturday.
                if(!item.isChecked()){
                    check = true;
                    limitColumns = 7;
                    // User check -> uncheck Show Saturday.
                } else {
                    check = false;
                    limitColumns = 6;
                }
                preferenceEditor.putBoolean("showSat", check);
                preferenceEditor.apply();
                showSat = check;
                item.setChecked(check);
                initTable(false);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 위젯 업데이트를 요청.
    public void requestWidgetUpdate() {
        Log.d(TAG, "requestWidgetUpdate Called");
        int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs);
        sendBroadcast(intent);
    }

    // 텍스트뷰의 텍스트 중간에 이미지 삽입기능.
    public Spanned getImageSpan(final int width, final int height, final Drawable draw) {

        Html.ImageGetter imageGetter = new Html.ImageGetter() {

            @Override

            public Drawable getDrawable(String source) {
                if (source.equals("icon")) {
                    Drawable drawable = draw;
                    drawable.setBounds(0, 0, width, height);
                    return drawable;
                }
                return null;
            }
        };
        Spanned htmlText = Html.fromHtml("<img src=\"icon\" width="+ width + "height=" + height + ">", imageGetter, null);
        return htmlText;
    }

    // SharedPreference를 Map데이터 파일로 내보냄.
    private boolean saveSharedPreferencesToFile(File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref =
                    getSharedPreferences("data", MODE_PRIVATE);
            output.writeObject(pref.getAll());

            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    // Map데이터파일을 SharedPreference로 불러옴
    private boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = getSharedPreferences("data", MODE_PRIVATE).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.apply();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    private CellValue[][] readPreferences(){
        CellValue[][] value = new CellValue[limitRows][limitColumns];
        for(int i = 0 ; i < limitRows ; i++){
            for(int j = 0 ; j < limitColumns ; j++){
                value[i][j] = new CellValue();
                if(i==0){
                    //요일을 넣어준다.
                    if(j!=0){
                        value[i][j].cell_value = getString(getResources().getIdentifier(WeekDays.values()[j].name(), "string", getPackageName()));
                    }
                    value[i][j].cell_color = R.drawable.topcell;

                    //저장된 시간명이 비어있지않으면 넣어줌.
                } else if(j==0) {
                    if(sharedPreferences.getString(""+i+j,"").equals("")){
                        value[i][0].cell_value = (i<10)? ""+i : "N"+(i-9);
                    } else {
                        value[i][0].cell_value = sharedPreferences.getString(""+i+j, "");
                    }
                    value[i][j].cell_color = R.drawable.topcell;
                } else {
                    value[i][j].cell_value = sharedPreferences.getString(""+i+j, "");
                    value[i][j].cell_color = R.drawable.leftcell;
                }
            }
        }
        return value;
    }

    private void initTable(boolean editMode){

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


        if(mainLinearLayout == null){
            mainLinearLayout =  new LinearLayout(this);
            gridLayout = new GridLayout(this);
            scrollView = new ScrollView(this);
            gridLayout.setOrientation(GridLayout.VERTICAL);
        } else {
            mainLinearLayout.removeAllViews();
            mainLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainLinearLayout.setBackgroundResource(R.drawable.base);
            mainLinearLayout.setLayoutParams(lp);
            gridLayout.removeAllViews();
            gridLayout.setLayoutParams(lp);
            scrollView.removeAllViews();
            scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        }


        gridLayout.setColumnCount(limitColumns);
        gridLayout.setRowCount(limitRows);
        scrollView.addView(gridLayout);
        mainLinearLayout.addView(scrollView);
        setContentView(mainLinearLayout);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        TypedValue tv = new TypedValue();

        int actionBarHeight = getActionBar().getHeight();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

//        Rect rectangle = new Rect();
//        Window window = getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
//        int statusBarHeight = rectangle.top;

        Log.d(TAG, "initTable: action bar size " + actionBarHeight + result);

        // Fix Weekday row's Height
        int weekdayHeight = 100;
        int cellWidth = (size.x)/limitColumns , cellHeight = (size.y-actionBarHeight - result - weekdayHeight)/(limitRows-1);

        timetableValue = readPreferences();

        // 테이블 생성 및 초기화
        for (int j = 0; j < limitColumns; j++) {
            for (int i = 0; i < limitRows; i++) {

                editTexts[i][j] = new EditText(this);
                editTexts[i][j].setText(timetableValue[i][j].cell_value);

                editTexts[i][j].setLayoutParams(lp);
                editTexts[i][j].setHeight((i==0)? weekdayHeight : cellHeight);
                editTexts[i][j].setWidth(cellWidth);
                editTexts[i][j].setBackgroundResource(timetableValue[i][j].cell_color);
                editTexts[i][j].setGravity(Gravity.CENTER);
                editTexts[i][j].setSingleLine();
                editTexts[i][j].setPadding(0,0,0,0);
                editTexts[i][j].setId((i * 10) + j);
                editTexts[i][j].setOnEditorActionListener(this);
                editTexts[i][j].setOnFocusChangeListener(this);
                editTexts[i][j].setOnClickListener(this);
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                    editTexts[i][j].setElevation(2);
//                }
                if(!editMode){
                    editTexts[i][j].setFocusable(false);
                    editTexts[i][j].setCursorVisible(false);
                    editTexts[i][j].setMovementMethod(null);

                    // Span cell, if same strings be nearby.

                    // Row is not weekrow && before cell is not empty && before cell's string equal current cell.
                    if(i > 1 &&
                            !(timetableValue[i-1][j].cell_value.equals("")) &&
                            timetableValue[i-1][j].cell_value.equals(timetableValue[i][j].cell_value)){
                        int spansize = timetableValue[i-1][j].spansize + 1;
                        // Set spansize data to spaned cell's data
                        for(int m = i; m > i - spansize ; m--){
                            timetableValue[m][j].spansize = spansize;
                        }

                        editTexts[i-1][j].setVisibility(View.INVISIBLE);
                        gridLayout.removeView(editTexts[i-1][j]);

                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.rowSpec = GridLayout.spec(i - spansize + 1, spansize, GridLayout.FILL);
                        params.columnSpec = GridLayout.spec(j,1);
                        params.width = cellWidth;
                        params.height = cellHeight*spansize;
                        params.setGravity(Gravity.FILL|Gravity.CENTER);
                        editTexts[i][j].setHeight(cellHeight*spansize);
                        gridLayout.addView(editTexts[i][j], params);
                        continue;
                    }
                }
                gridLayout.addView(editTexts[i][j],cellWidth, (i==0)? weekdayHeight : cellHeight);

                // 연속 입력을 위한 포커스 재설정. EditorActionListener를 설정하면 기본 포커스가 오른쪽으로 가버림.
                if (i != 0 && j != 0) {
                    int k = i, l = j;
                    if (k == 14) {
                        k = 1;
                        l++;
                    } else {
                        k++;
                    }
                    editTexts[i][j].setNextFocusForwardId((k * 10) + l);
                    editTexts[i][j].setNextFocusDownId((k * 10) + l);
                }
            }
        }
        requestWidgetUpdate();
    }
}
