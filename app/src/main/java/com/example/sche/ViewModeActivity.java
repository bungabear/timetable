package com.example.sche;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ViewModeActivity extends Activity implements View.OnClickListener {
	public static final String TAG = "ScheTest";
	private EditText editTexts[][] = new EditText[15][7];
	//설정 저장객체 생성
	private SharedPreferences data;

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
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT,1);
		tableLayout.setLayoutParams(tableParams);
		TableRow[] tableRows = new TableRow[15];

		WeekDays[] weekDays = WeekDays.values();

		linearLayout.addView(tableLayout);

		//뷰 생성 및 초기화
		for(int i = 0 ; i < 15 ; i++){
			tableRows[i] = new TableRow(this);
			tableRows[i].setLayoutParams(tableParams);
			tableLayout.addView(tableRows[i]);
			for(int j = 0 ; j < 7 ; j++){
				editTexts[i][j] = new EditText(this);
				editTexts[i][j].setLayoutParams(rowParams);
				editTexts[i][j].setBackgroundResource(R.drawable.topline);
				editTexts[i][j].setGravity(Gravity.CENTER);
				editTexts[i][j].setFocusable(false);
				editTexts[i][j].setCursorVisible(false);
				editTexts[i][j].setSingleLine();
				editTexts[i][j].setMovementMethod(null);
				editTexts[i][j].setPadding(0,0,0,0);
				tableRows[i].addView(editTexts[i][j]);
			}
		}
		//요일 입력
		for(int i = 1 ; i < 7 ; i++){
			//스트링을 직접 받아오면 로케일 구분이 안되는듯함.
			editTexts[0][i].setText(getString(getResources().getIdentifier(weekDays[i].name(),"string", getPackageName())));
		}

		//수업교시 입력
		for(int i = 1 ; i < 15 ; i++){
			editTexts[i][0].setText((i<10)? ""+i : "N" + (i-9));
		}

		//설정 저장소에 data.xml파일에 접근 읽기/쓰기가능
        data = getSharedPreferences("data",0);

		//저장된 값 불러오기.
		String subject;
		for(int i = 1 ; i < 15 ; i++){
			for(int j = 1 ; j < 7 ; j++){
				subject = data.getString(""+i+j, "");
				editTexts[i][j].setText(subject);
				editTexts[i][j].setTag(new int[]{i,j});
				editTexts[i][j].setBackgroundResource(R.drawable.leftline);
			}
		}
		editTexts[0][0].setText(R.string.edit);
		editTexts[0][0].setTag("edit");
		editTexts[0][0].setOnClickListener(this);
		setContentView(linearLayout);
	}

	@Override
	public void onClick(View v) {
		if(v.getTag().equals("edit")){
			for(int i = 1 ; i < 15 ; i++){
				for(int j = 1 ; j < 7 ; j++){
					editTexts[i][j].setFocusable(true);
					editTexts[i][j].setFocusableInTouchMode(true);
					editTexts[i][j].setCursorVisible(true);
				}
			}
			((TextView)v).setText(R.string.save);
			v.setTag("save");
		} else if(v.getTag().equals("save")){
			for(int i = 1 ; i < 15 ; i++){
				for(int j = 1 ; j < 7 ; j++){
					editTexts[i][j].setFocusable(false);
					editTexts[i][j].setCursorVisible(false);
				}
			}
			SharedPreferences.Editor editor = data.edit();
			String subject;
			for(int i = 1 ; i < 15 ; i++){
				for(int j = 1 ; j < 7 ; j++){
					subject = editTexts[i][j].getText().toString();
					editor.putString(""+i+j, subject);
				}
			}
			editor.commit();
			((TextView)v).setText(R.string.edit);
			v.setTag("edit");
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
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		menu.add(0,1,0, R.string.EditMode);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch(item.getItemId()){
//		case 1:
//			Intent editmode = new Intent(this,EditModeAcitivity.class);
//			startActivity(editmode);
//			break;
//
//		}
//		return super.onOptionsItemSelected(item);
//	}


}
