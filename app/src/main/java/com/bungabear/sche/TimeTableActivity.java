package com.bungabear.sche;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private EditText editTexts[][] = new EditText[15][7];
    private ImageView editButton;
    private SharedPreferences data;
    private int iconsize;
    private int limitColumns;

    private boolean showSat = true;
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

        data = getSharedPreferences("data", 0);
        showSat = data.getBoolean("showSat", true);
        if (showSat) {
            limitColumns = 7;
        } else {
            limitColumns = 6;
        }


        initTable(false);
        requestWidgetUpdate();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void onClick(View v) {
        if (v.getTag().equals("edit")) {
            initTable(true);
//            //입력 활성화
//            for (int i = 1; i < 15; i++) {
//                for (int j = 0; j < limitColumns; j++) {
//                    editTexts[i][j].setFocusable(true);
//                    editTexts[i][j].setFocusableInTouchMode(true);
//                    editTexts[i][j].setCursorVisible(true);
//                }
//            }
//
//            //버튼 변경
//            ((ImageView) v).setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.save),iconsize,iconsize,false));
//            v.setTag("save");

        } else if (v.getTag().equals("save")) {

            //키보드 숨기기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

//            //입력 비활성화
//            for (int i = 1; i < 15; i++) {
//                for (int j = 0; j < limitColumns; j++) {
//                    editTexts[i][j].setFocusable(false);
//                    editTexts[i][j].setCursorVisible(false);
//                }
//            }

            //시간표 값 저장
            SharedPreferences.Editor editor = data.edit();
            String value;
            for (int i = 1; i < 15; i++) {
                for (int j = 0; j < limitColumns; j++) {
                    //시간명에 변경이 있는지 확인
                    if (j == 0) {
                        value = editTexts[i][0].getText().toString();
                        //기본값이면 비움.
                        if (value.equals(String.valueOf(i)) || value.equals("N" + String.valueOf(i - 9))) {
                            editor.putString("" + i + j, "");
                            continue;
                        }
                    } else {
                        value = editTexts[i][j].getText().toString();
                    }
                    editor.putString("" + i + j, value);
                }
            }

            editor.commit();

//            //버튼 변경
//            ((ImageView) v).setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.edit),iconsize,iconsize,false));
//            v.setTag("edit");
//            onCreate(new Bundle());
            initTable(false);
            requestWidgetUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//		String subject;
//		for(int i = 0 ; i < 9 ; i++){
//			for(int j = 0 ; j < 5 ; j++){
//				subject = data.getString(""+i+j, "");
//				textViews[i][j].setText(subject);
//			}
//		}
//		Log.d(TAG, "onResume called.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setChecked(showSat);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = data.edit();
        switch (item.getItemId()) {
            case R.id.item1:
                showSat = !(item.isChecked());
                item.setChecked(showSat);
                editor.putBoolean("showSat", showSat);
                editor.commit();
                recreate();
                requestWidgetUpdate();
                break;
            //파일에 백업한다
            case R.id.item2:

                //마시멜로우 이상이고 권한이 없으면 권한 요청
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(!STORAGE_WRITE_PERMISSION){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }
                }
                File fileDestination = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "timetable.backup");
                saveSharedPreferencesToFile(fileDestination);

                break;
            //파일에서 복구한다.
            case R.id.item3:
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
        }
        return super.onOptionsItemSelected(item);
    }

    //위젯 업데이트를 요청.
    public void requestWidgetUpdate() {
        int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs);
        sendBroadcast(intent);
    }

    //텍스트뷰의 텍스트 중간에 이미지 삽입기능.
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

    //SharedPreference를 Map데이터 파일로 내보냄.
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

    //Map데이터파일을 SharedPreference로 불러옴
    @SuppressWarnings({"unchecked"})
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
        CellValue[][] value = new CellValue[15][limitColumns];
        for(int i = 0 ; i < 15 ; i++){
            for(int j = 0 ; j < limitColumns ; j++){
                value[i][j] = new CellValue();
                if(i==0){
                    if(j!=0){
                        //요일을 넣어준다.
                        value[i][j].cell_value = getString(getResources().getIdentifier(WeekDays.values()[j].name(), "string", getPackageName()));
                        value[i][j].cell_color = R.drawable.topcell;
                    }
                    //저장된 시간부 값이 비어있지않으면 넣어줌.
                } else if(j==0 && data.getString(""+i+j,"").equals("")) {
                    value[i][0].cell_value = (i<10)? ""+i : "N"+(i-9);
                    value[i][j].cell_color = R.drawable.topcell;
                } else {
                    value[i][j].cell_value = data.getString(""+i+j, "");
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
        } else {
            mainLinearLayout.removeAllViews();
            mainLinearLayout.setLayoutParams(lp);
            mainLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainLinearLayout.setBackgroundResource(R.drawable.base);
        }

        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setLayoutParams(lp);
        gridLayout.setRowCount(15);
        gridLayout.setColumnCount(limitColumns);
        gridLayout.setOrientation(GridLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        scrollView.addView(gridLayout);
        mainLinearLayout.addView(scrollView);
        setContentView(mainLinearLayout);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);


        int cellWidth = (size.x)/limitColumns , cellHeight = (size.y-180)/15;
        int squreWidth = (cellWidth>cellHeight)? cellHeight : cellWidth ;
        iconsize = squreWidth-45;

        CellValue[][] timetableValue = readPreferences();



        //뷰 생성 및 초기화
        for (int j = 0; j < limitColumns; j++) {
            for (int i = 0; i < 15; i++) {
                //0,0에 버튼생성
                if (i==0 && j==0){
                    int iconID = R.drawable.edit;
                    String tag = "edit";
                    if(editMode){
                        iconID = R.drawable.save;
                        tag = "save";
                    }
                    Bitmap icon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),iconID),iconsize , iconsize,false);
                    editButton = new ImageView(this);
                    editButton.setImageBitmap(icon);
                    editButton.setScaleType(ImageView.ScaleType.CENTER);
                    editButton.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    editButton.setBackgroundResource(R.drawable.topcell);
                    editButton.setOnClickListener(this);
                    editButton.setMaxHeight(cellHeight);
                    editButton.setMaxWidth(cellWidth);
                    editButton.setTag(tag);
                    gridLayout.addView(editButton,cellWidth, cellHeight);
                    continue;
                }
                editTexts[i][j] = new EditText(this);
                editTexts[i][j].setText(timetableValue[i][j].cell_value);

                editTexts[i][j].setLayoutParams(lp);
                editTexts[i][j].setHeight(cellHeight);
                editTexts[i][j].setWidth(cellWidth);
                editTexts[i][j].setBackgroundResource(timetableValue[i][j].cell_color);
                editTexts[i][j].setGravity(Gravity.CENTER);
                editTexts[i][j].setSingleLine();
                editTexts[i][j].setPadding(0,0,0,0);
                editTexts[i][j].setId((i * 10) + j);
                editTexts[i][j].setOnEditorActionListener(this);
                editTexts[i][j].setOnFocusChangeListener(this);
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                    editTexts[i][j].setElevation(2);
//                }
                if(!editMode){
                    editTexts[i][j].setFocusable(false);
                    editTexts[i][j].setCursorVisible(false);
                    editTexts[i][j].setMovementMethod(null);
                    if(i > 1 &&
                            !(timetableValue[i-1][j].cell_value.equals("")) &&
                            timetableValue[i-1][j].cell_value.equals(timetableValue[i][j].cell_value)){
                        int spansize = timetableValue[i-1][j].spansize + 1;
                        timetableValue[i-1][j].spansize = spansize;
                        timetableValue[i][j].spansize = spansize;

                        Log.d(TAG, "onCreate: " + i + j + " " + spansize);
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
                gridLayout.addView(editTexts[i][j],cellWidth, cellHeight);

                // Todo 커서를 보이게 해줘야함.
                // 연속 입력을 위한 포커스 재설정. EditorActionListener를 설정하면 등록순서인 오른쪽으로 가버림.
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
    }
}
