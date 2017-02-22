package com.example.sche;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewModeActivity extends Activity {
	public static final String TAG = "ScheTest";
	private TextView e[][];
	//설정 저장객체 생성
	private SharedPreferences data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_mode);

		//객체 초기화
		e = new TextView[9][5];
		for(int i = 0 ; i < 9 ; i ++){
			for(int j = 0 ; j < 5 ; j ++){
				String viewID = "t"+(i+1)+"."+(j+1);
				int resID = getResources().getIdentifier(viewID, "id", getPackageName());
				e[i][j] = (TextView) findViewById(resID);
			}
		}
		//설정 저장소에 data.xml파일에 접근 읽기/쓰기가능
        data = getSharedPreferences("data",0);

		//저장된 값 불러오기.
		String subject;
		for(int i = 0 ; i < 9 ; i++){
			for(int j = 0 ; j < 5 ; j++){
				subject = data.getString(""+i+j, "");
				e[i][j].setText(subject);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		String subject;
		for(int i = 0 ; i < 9 ; i++){
			for(int j = 0 ; j < 5 ; j++){
				subject = data.getString(""+i+j, "");
				e[i][j].setText(subject);
			}
		}
		Log.d(TAG, "onResume called.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0,1,0, R.string.EditMode);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 1:
			Intent editmode = new Intent(this,EditModeAcitivity.class);
			startActivity(editmode);
			break;

		}
		return super.onOptionsItemSelected(item);
	}


}
