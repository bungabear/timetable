package com.bungabear.sche;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TimeTableActivity extends Activity implements View.OnClickListener, TextView.OnEditorActionListener, View.OnFocusChangeListener {
	public static final String TAG = "ScheTest";
	private EditText editTexts[][] = new EditText[15][7];
	//설정 저장객체 생성
	private SharedPreferences data;
	private boolean showSat = true;
	private int limitColumns;
	private Drawable save, edit;

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.d(TAG, "onEditorAction: " + actionId);

		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		int[] tags = (int[])v.getTag();
		String text = ((EditText)v).getText().toString();
//		Log.d(TAG, "onEditorAction: " + tags[0] + tags[1] + text + hasFocus);
		//포커스를 잃은 칸이 둘쨋줄 이후고 값이 period이면 값복사
		if(!hasFocus && tags[0] >= 2 && text.equals(".")){
			((EditText)v).setText(editTexts[tags[0]-1][tags[1]].getText().toString());
		}
	}


	enum WeekDays{
		sun,mon,tue,wed,thu,fri,sat
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		setContentView(R.layout.view_mode);
		LinearLayout linearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		linearLayout.setLayoutParams(lp);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(16, 16, 16, 16);
		linearLayout.setBackgroundResource(R.drawable.base);

		TableLayout tableLayout = new TableLayout(this);
		TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT,1);
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT,1);
		tableLayout.setLayoutParams(tableParams);
		TableRow[] tableRows = new TableRow[15];

		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
		scrollView.addView(tableLayout);
		linearLayout.addView(scrollView);

		data = getSharedPreferences("data",0);
		showSat = data.getBoolean("showSat", true);
		if(showSat) {
			limitColumns = 7;
		}
		else {
			limitColumns = 6;
		}
		//뷰 생성 및 초기화
		for(int i = 0; i < 15; i++){
			tableRows[i] = new TableRow(this);
			tableRows[i].setLayoutParams(tableParams);
			tableLayout.addView(tableRows[i]);
			for(int j = 0 ; j < limitColumns ; j++){
				editTexts[i][j] = new EditText(this);
				editTexts[i][j].setLayoutParams(rowParams);
				editTexts[i][j].setBackgroundResource(R.drawable.topcell);
				editTexts[i][j].setGravity(Gravity.CENTER);
				editTexts[i][j].setFocusable(false);
				editTexts[i][j].setCursorVisible(false);
				editTexts[i][j].setSingleLine();
				editTexts[i][j].setMovementMethod(null);
				editTexts[i][j].setPadding(0,0,0,0);
				editTexts[i][j].setHeight(113);
				editTexts[i][j].setId((i*10)+j);
				editTexts[i][j].setTag(new int[]{i,j});
				editTexts[i][j].setOnEditorActionListener(this);
				editTexts[i][j].setOnFocusChangeListener(this);
                tableRows[i].addView(editTexts[i][j]);
				// Todo 커서를 보이게 해줘야함.
				// 연속 입력을 위한 포커스 재설정. EditorActionListener를 설정하면 등록순서인 오른쪽으로 가버림.
				if(i !=0  &&j != 0){
					int k = i, l = j;
					if(k == 14){
						k = 1;
						l++;
					} else {
						k++;
					}
					editTexts[i][j].setNextFocusForwardId((k*10)+l);
					editTexts[i][j].setNextFocusDownId((k*10)+l);
				}
			}
		}
		//요일 입력
		for(int i = 1 ; i < limitColumns ; i++){
			editTexts[0][i].setText(getString(getResources().getIdentifier(WeekDays.values()[i].name(),"string", getPackageName())));
		}

		//수업교시 입력
		for(int i = 1; i < 15; i++){
			String time = data.getString(""+i+0,"");
			if(time.equals("")){
				editTexts[i][0].setText((i<10)? ""+i : "N" + (i-9));
			} else {
				editTexts[i][0].setText(time);
			}
		}

		//저장된 값 불러오기.
		String subject;
		for(int i = 1; i < 15; i++){
			for(int j = 1 ; j < limitColumns ; j++){
				subject = data.getString(""+i+j, "");
				editTexts[i][j].setText(subject);
				editTexts[i][j].setBackgroundResource(R.drawable.leftcell);
			}
		}

		editTexts[0][0].setText(getImageSpan(70,70,getResources().getDrawable(R.drawable.edit)));
		editTexts[0][0].setTag("edit");
		editTexts[0][0].setOnClickListener(this);

		setContentView(linearLayout);
		requestWidgetUpdate();
	}

	@Override
	public void onClick(View v) {
		if(v.getTag().equals("edit")){
			//입력 활성화
			for(int i = 1 ; i < 15 ; i++){
				for(int j = 0 ; j < limitColumns ; j++){
					editTexts[i][j].setFocusable(true);
					editTexts[i][j].setFocusableInTouchMode(true);
					editTexts[i][j].setCursorVisible(true);
				}
			}

			//버튼 변경
			((TextView)v).setText(getImageSpan(70,70,getResources().getDrawable(R.drawable.save)));
			v.setTag("save");

		} else if(v.getTag().equals("save")){

			//입력 비활성화
			for(int i = 1 ; i < 15 ; i++){
				for(int j = 0 ; j < limitColumns ; j++){
					editTexts[i][j].setFocusable(false);
					editTexts[i][j].setCursorVisible(false);
				}
			}

			//시간표 값 저장
			SharedPreferences.Editor editor = data.edit();
			String value;
			for(int i = 1 ; i < 15 ; i++){
				for(int j = 0 ; j < limitColumns ; j++){
					//시간명에 변경이 있는지 확인
					if(j==0){
						value = editTexts[i][0].getText().toString();
						//기본값이면 비움.
						if(value.equals(String.valueOf(i)) || value.equals("N" + String.valueOf(i-9))){
							editor.putString(""+i+j,"");
							continue;
						}
					} else {
						value = editTexts[i][j].getText().toString();
					}
					editor.putString(""+i+j, value);
				}
			}

			editor.commit();

			//버튼 변경
			((TextView)v).setText(getImageSpan(70,70,getResources().getDrawable(R.drawable.edit)));
			v.setTag("edit");


			//키보드 숨기기
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

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
		switch(item.getItemId()){
		case R.id.item1:
			showSat = !(item.isChecked());
			item.setChecked(showSat);
			editor.putBoolean("showSat",showSat);
			editor.commit();
			recreate();
			requestWidgetUpdate();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	//위젯 업데이트를 요청.
	public void requestWidgetUpdate(){
		int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
		Intent intent = new Intent(this, WidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,widgetIDs);
		sendBroadcast(intent);
	}

	public Spanned getImageSpan(final int width, final int height, final Drawable draw){

		Html.ImageGetter imageGetter = new Html.ImageGetter() {

			@Override

			public Drawable getDrawable(String source) {
				if ( source.equals( "icon" ) ){
					Drawable drawable = draw;
					drawable.setBounds( 0, 0, width, height );
					return drawable;
				}
				return null;
			}
		};
		Spanned htmlText = Html.fromHtml( "<img src=\"icon\" width=50 height=50>", imageGetter, null );
		return htmlText;
	}
}