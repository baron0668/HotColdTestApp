package com.example.rootcommand;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button normal = null;
	private Button root = null;
	private TextView show = null;
	private static RootUtil suUtil = RootUtil.getInstance();
	private Map<String, String[]> appPath = new HashMap<String, String[]>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initial path data
		String[] path = new String[2];
		path[0] = "/data/app/com.facebook.katana-1.apk";
		path[1] = "/data/data/com.facebook.katana";
		appPath.put("facebook", path);
		path = new String[2];
		path[0] = "/data/app/com.android.vending-1.apk";
		path[1] = "/data/data/com.android.vending";
		appPath.put("play", path);
		
		
		List<String> facebook = new ArrayList<String>();
		//listDirectory(appPath.get("facebook")[1], facebook);
		lsAllFile(appPath.get("facebook")[1], facebook);
		lsAllFile(appPath.get("play")[1], facebook);
		//Log.d("Result", "facebook file size:" + facebook.size());
		
		// File f = new File(appPath.get("facebook")[1]);
		// Log.d("File", "Is directory:"+f.isDirectory());
		normal = (Button) findViewById(R.id.button1);
		root = (Button) findViewById(R.id.button2);
		show = (TextView) findViewById(R.id.textView1);
		normal.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String ret = suUtil
						.rootExec("/data/check /data/app/com.android.vending-1.apk");
				if (ret != null) {
					show.setText(ret);
				}
			}
		});
		root.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String ret = suUtil.rootExec("ls /data/data");
				if (ret != null) {
					show.setText(ret);
				}
			}
		});
	}

	public static void lsAllFile(String path, List<String> myfile){
		String ret = suUtil.rootExec("/data/lsfile " + path);
		Log.d("Result", ret);
	}
	
	public static void listDirectory(String path, List<String> myfile) {
		Log.d("Path", path);
		String test = "Line1\nLine2\n";
		Log.d("Test", test+",char at 6:"+test.charAt(6));
		String ret = suUtil.rootExec("ls -laR " + path);
		String subFloderSearchRange = null;
		String currentPath = null;
		String fileName = null;
		//Log.d("Result", "Ret:"+ret);
		Pattern pathPattern = Pattern.compile("/.*:"); //Path pattern
		Pattern filePattern = Pattern.compile("[\\w]*\\.[\\w]*"); //File pattern
		Matcher pathMatcher = pathPattern.matcher(ret);
		Matcher fileMatcher = null;
		int dirCount=0;
		int fileCount=0;
		int startIndex=0;
		int endIndex=0;
		if(pathMatcher.find()){
			startIndex = pathMatcher.end();
			currentPath = pathMatcher.group().replace(":", "");
			dirCount++;
			Log.d("Result", "["+dirCount+"]currentPath:"+currentPath);
			while(pathMatcher.find()){
				endIndex = pathMatcher.start()-1;
				subFloderSearchRange = ret.substring(startIndex, endIndex);
				fileMatcher = filePattern.matcher(subFloderSearchRange);
				while(fileMatcher.find()){
					fileName = fileMatcher.group();
					myfile.add(currentPath+"/"+fileName);
				}
				startIndex = pathMatcher.end();
				currentPath = pathMatcher.group().replace(":", "");
				dirCount++;
				Log.d("Result", "["+dirCount+"]currentPath:"+currentPath);	
			}
		}
		
		Log.d("Result", "Total number:"+dirCount);
		Log.d("Result", "All file:");
		for(String element:myfile){
			Log.d("Result", element);
		}
	}

	private void do_exec(String cmd) {
		String s = "";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				s += line + "\n";
			}
			show.setText(s);
		} catch (IOException e) {
			Log.d("Test", e.toString());
		}
	}

	private void su_exec(String cmd) {
		String s = "";
		try {
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream opt = new DataOutputStream(p.getOutputStream());
			opt.writeBytes("ls /data/data\n");
			opt.writeBytes("exit\n");
			opt.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				s += line + "\n";
			}
			show.setText(s);
		} catch (IOException e) {
			Log.d("Test", e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
