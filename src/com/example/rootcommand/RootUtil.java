/*
Easy root exec util , by darkk6
*/
package com.example.rootcommand;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Scanner;

import android.util.Log;

public class RootUtil
{
	private static RootUtil instance=null;
	private static String TAG="RootUtil";
	private static Runtime runtime=null;
	
	public static RootUtil getInstance(){
		if(instance==null) instance=new RootUtil();
		return instance;
	}
	
	private String suPath="[su not found]";
	private String busyboxPath="[busybox not found]";
	
	private RootUtil(){
		if(runtime==null) runtime=Runtime.getRuntime();
		hasRootAccess();
		hasBusybox();
	}
	
	public String whereIsSu(){return suPath;}
	public String whereIsBusybox(){return busyboxPath;}
	
	
	/* �����su ��bin ��� xbin 摨��嚗�蒂蝣箄�����潛�
	 * SecurityException (鋡�superuser ���銋����
	 * 撠梯”蝷箏�隞交����敺�Root 甈��
	 */
	public boolean hasRootAccess(){
		try{
			if (!new File("/system/bin/su").exists()){
				if(!new File("/system/xbin/su").exists())
					return false;
				else suPath="/system/xbin/su";
			}else suPath="/system/bin/su";
		}catch (SecurityException e){
			Log.d(TAG, "Check has root access: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/* �����busybox ��xbin ��� bin 摨��嚗�蒂蝣箄�����潛�
	 * SecurityException (鋡�superuser ���銋����
	 * 撠梯”蝷箸�摰��憟�busybox
	 */
	public boolean hasBusybox(){
		try{
			if (!new File("/system/xbin/busybox").exists()){
				if(!new File("/system/bin/busybox").exists())
					return false;
				else busyboxPath="/system/bin/busybox";
			}else busyboxPath="/system/xbin/busybox";
		}catch (SecurityException e){
			Log.d(TAG, "Check has busybox : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	/* 隞�root 頨怠��瑁��賭誘嚗�����銵����撓�箏�銝�	 * 	���嚗�	 * 		cmds - 摮�葡���嚗��靘���瑁����隞�	 * 
	 * 	瘜冽�嚗�	 * 		�粹����摨����������ipt.hasNextLine() , �����炎��	 * 		�喳�鞈��銝剜��血���"exit" , �交�����芸����甇文�隞�	*/
	public String rootExec(String...cmds){
		return rootExec(true,cmds);
	}
//============== private ================
	private String rootExec(boolean autoEnter,String...cmds){
		//撠�� exit ��誘������  rootExecInputStream 銝�		
		InputStream tmpIpt=rootExecInputStream(autoEnter,cmds);
		if(tmpIpt==null) return null;
		String result;
		try {
			result = convertStreamToString(tmpIpt);
			
			Log.d("UtilLog", result);
			Log.d("UtilLog", "Size:"+result.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuilder str=new StringBuilder();
		Scanner ipt=new Scanner(tmpIpt);
		while(ipt.hasNextLine())
			str.append(ipt.nextLine()+"\n");
		return str.toString();
	}
	/* rootExecInputStream
	 * 	隞�root 頨怠��瑁���誘銝血���InputStream
	*/
	private InputStream rootExecInputStream(boolean autoEnter,String...cmds){
		try{
			//Process proc = new ProcessBuilder().command("su").redirectErrorStream(true).start();
			
			Process proc=runtime.exec("su");
			DataOutputStream opt = new DataOutputStream(proc.getOutputStream());
			InputStream ipt=proc.getInputStream();
			boolean exitCmdFound=false;
			//靘���喳�����訾�銵��銵��銵��隞�			
			for(String cmd:cmds){
				opt.writeBytes(cmd);
				// NOTE 甇�exit 撠�����銝�������autoEnter=false ���瘜�				if(!exitCmdFound && cmd.equals("exit")) exitCmdFound=true;
				if(autoEnter) opt.writeBytes("\n");
			}
			if(!exitCmdFound) opt.writeBytes("exit\n");
			opt.flush();
			try {
				int retValue = proc.waitFor();
				Log.d("Process", "Return code:"+retValue);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ipt;
		}catch(IOException e){
			Log.d(TAG, "Root execute : " + e.getMessage());
		}
		return null;
	}
	
	private String convertStreamToString(InputStream is)
            throws IOException {
        //
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        //
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {        
            return "";
        }
    }
}
