package com.example.sche;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.os.Environment;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class editmode extends Activity {
	public static final String TAG = "TestFileActivity";
    public static final String STRSAVEPATH = Environment.
            getExternalStorageDirectory()+"/testfolder/";
    public static final String STRSAVEPATH2 = Environment.
            getExternalStorageDirectory()+"/testfolder2/";
    public static final String SAVEFILEPATH = "ScheText.txt";

	private EditText e[][];
        
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editmode);
		//표 값 불러오기
		File dir = makeDirectory(STRSAVEPATH);
        //파일 생성
        File file = makeFile(dir, (STRSAVEPATH+SAVEFILEPATH));
        //절대 경로
        Log.i(TAG, ""+getAbsolutePath(dir));
        Log.i(TAG, ""+getAbsolutePath(file));

		e = new EditText[9][5];
		for(int i = 0 ; i < 9 ; i ++){
			for(int j = 0 ; j < 5 ; j ++){
				String viewID = "e"+(i+1)+"."+(j+1);
				int resID = getResources().getIdentifier(viewID, "id", getPackageName());
				e[i][j] = (EditText)findViewById(resID);
				Log.d(TAG, "onCreate: " + resID);
			}
		}
		readData(readFile(file));
	}
	
	//파일 쓰기
    //String content = new String("가나다라마바다사아자차카타파하");
    //writeFile(file , content.getBytes());

	private void readData(String s){
		for(int i=0;i<s.length();i++){
			int column = 0 ;
			int row = 0;
			String subject = "";
			//꺽쇠 시작인지 확인
			if(s.charAt(i)=='<'){
				row = Character.getNumericValue(s.charAt(++i));
				column = Character.getNumericValue(s.charAt(++i));
				for(i+=2;s.charAt(i)!='>';i++){
					subject +=  s.charAt(i);
				}
			}
			e[row][column].setText(subject);
		}
	}

	public void clicksave(View v){
		String all = "";

		for(int i = 0 ; i < 9 ; i++){
			for(int  j = 0 ; j < 5 ; j++){
				all += "<" + i + j + "$" + e[i][j].getText() + ">";
			}
		}
		Log.d(TAG, "clicksave: " + all);
		
		//폴더 생성
        File dir = makeDirectory(STRSAVEPATH);
        //파일 생성
        File file = makeFile(dir, (STRSAVEPATH+SAVEFILEPATH));
        //절대 경로
        Log.i(TAG, ""+getAbsolutePath(dir));
        Log.i(TAG, ""+getAbsolutePath(file));
        //내용 저장
        writeFile(file,all.getBytes());

		
		Intent MainActivity = new Intent(this,MainActivity.class);
		startActivity(MainActivity);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0,1,0,"시간표 보기모드");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case 1:
			//표 값 불러오기
			File dir = makeDirectory(STRSAVEPATH);
	        //파일 생성
	        File file = makeFile(dir, (STRSAVEPATH+SAVEFILEPATH));
			readData(readFile(file));
			Intent MainActivity = new Intent(this,MainActivity.class);
			startActivity(MainActivity);
			break;
		
		
		
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	 private File makeDirectory(String dir_path){
	        File dir = new File(dir_path);
	        if (!dir.exists())
	        {
	            dir.mkdirs();
	            Log.i( TAG , "!dir.exists" );
	        }else{
	            Log.i( TAG , "dir.exists" );
	        }
	 
	        return dir;
	    }
	 
	    /**
	     * 파일 생성
	     * @param dir
	     * @return file 
	     */
	    private File makeFile(File dir , String file_path){
	        File file = null;
	        boolean isSuccess = false;
	        if(dir.isDirectory()){
	            file = new File(file_path);
	            if(file!=null&&!file.exists()){
	                Log.i( TAG , "!file.exists" );
	                try {
	                    isSuccess = file.createNewFile();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                } finally{
	                    Log.i(TAG, "파일생성 여부 = " + isSuccess);
	                }
	            }else{
	                Log.i( TAG , "file.exists" );
	            }
	        }
	        return file;
	    }
	 
	    /**
	     * (dir/file) 절대 경로 얻어오기
	     * @param file
	     * @return String
	     */
	    private String getAbsolutePath(File file){
	        return ""+file.getAbsolutePath();
	    }
	 
	    /**
	     * (dir/file) 삭제 하기
	     * @param file
	     */
	    private boolean deleteFile(File file){
	        boolean result;
	        if(file!=null&&file.exists()){
	            file.delete();
	            result = true;
	        }else{
	            result = false;
	        }
	        return result;
	    }
	 
	    /**
	     * 파일여부 체크 하기
	     * @param file
	     * @return
	     */
	    private boolean isFile(File file){
	        boolean result;
	        if(file!=null&&file.exists()&&file.isFile()){
	            result=true;
	        }else{
	            result=false;
	        }
	        return result;
	    }
	 
	    /**
	     * 디렉토리 여부 체크 하기
	     * @param dir
	     * @return
	     */
	    private boolean isDirectory(File dir){
	        boolean result;
	        if(dir!=null&&dir.isDirectory()){
	            result=true;
	        }else{
	            result=false;
	        }
	        return result;
	    }
	 
	    /**
	     * 파일 존재 여부 확인 하기
	     * @param file
	     * @return
	     */
	    private boolean isFileExist(File file){
	        boolean result;
	        if(file!=null&&file.exists()){
	            result=true;
	        }else{
	            result=false;
	        }
	        return result;
	    }
	     
	    /**
	     * 파일 이름 바꾸기
	     * @param file
	     */
	    private boolean reNameFile(File file , File new_name){
	        boolean result;
	        if(file!=null&&file.exists()&&file.renameTo(new_name)){
	            result=true;
	        }else{
	            result=false;
	        }
	        return result;
	    }
	     
	    /**
	     * 디렉토리에 안에 내용을 보여 준다.
	     * @param dir
	     * @return
	     */
	    private String[] getList(File dir){
	        if(dir!=null&&dir.exists())
	            return dir.list();
	        return null;
	    }
	 
	    /**
	     * 파일에 내용 쓰기
	     * @param file
	     * @param file_content
	     * @return
	     */
	    private boolean writeFile(File file , byte[] file_content){
	        boolean result;
	        FileOutputStream fos;
	        if(file!=null&&file.exists()&&file_content!=null){
	            try {
	                fos = new FileOutputStream(file);
	                try {
	                    fos.write(file_content);
	                    fos.flush();
	                    fos.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            } catch (FileNotFoundException e) {
	                e.printStackTrace();
	            }
	            result = true;
	        }else{
	            result = false;
	        }
	        return result;
	    }
	 
	    /**
	     * 파일 읽어 오기 
	     * @param file
	     */
	    private String readFile(File file){
	        String text = "";
	        String data = "";
	        if(file!=null&&file.exists()){
	            try {
	            	BufferedReader reader = new BufferedReader(new FileReader(file));
	            	while ((data = reader.readLine()) != null) {
	                    text += data;
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            
	        }
	        return text;	        
	    }
	     
	    /**
	     * 파일 복사
	     * @param file
	     * @param save_file
	     * @return
	     */
	    private boolean copyFile(File file , String save_file){
	        boolean result;
	        if(file!=null&&file.exists()){
	            try {
	                FileInputStream fis = new FileInputStream(file);
	                FileOutputStream newfos = new FileOutputStream(save_file);
	                int readcount=0;
	                byte[] buffer = new byte[1024];
	                while((readcount = fis.read(buffer,0,1024))!= -1){
	                    newfos.write(buffer,0,readcount);
	                }
	                newfos.close();
	                fis.close();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            result = true;
	        }else{
	            result = false;
	        }
	        return result;
	    }

}
