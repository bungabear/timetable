package com.example.sche;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class EditModeAcitivity extends Activity {
	public static final String TAG = "ScheTest";
	private EditText e[][];
	//설정 저장객체 생성
	private SharedPreferences data;
        
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_mode);

		//EditText배열 초기화.
		e = new EditText[9][5];
		for(int i = 0 ; i < 9 ; i ++){
			for(int j = 0 ; j < 5 ; j ++){
				String viewID = "e"+(i+1)+"."+(j+1);
				int resID = getResources().getIdentifier(viewID, "id", getPackageName());
				e[i][j] = (EditText)findViewById(resID);
			}
		}

		//설정 저장소에 data.xml파일에 접근 읽기/쓰기가능
		data = getSharedPreferences("data",0);

		String subject = "";
		for(int i = 0 ; i < 9 ; i++){
			for(int j = 0 ; j < 5 ; j++){
				subject = data.getString(""+i+j, "");
				e[i][j].setText(subject);
				Log.d(TAG, "readData: " + i + j + " " + subject);
			}
		}

	}

	public void save(View v){
		SharedPreferences.Editor editor = data.edit();
		String subject = "";
		for(int i = 0 ; i < 9 ; i++){
			for(int j = 0 ; j < 5 ; j++){
				subject = e[i][j].getText().toString();
				editor.putString(""+i+j, subject);
				Log.d(TAG, "saveData: " + i + j + subject);
			}
		}
		editor.commit();

		Intent MainActivity = new Intent(this,ViewModeActivity.class);
		startActivity(MainActivity);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0,1,0, R.string.ViewMode);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case 1:
			Intent MainActivity = new Intent(this,ViewModeActivity.class);
			startActivity(MainActivity);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
