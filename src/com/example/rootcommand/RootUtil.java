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
	
	
	/* 有找到 su 在 bin 或者 xbin 底下，並確認時沒發生
	 * SecurityException (被 superuser 擋住之類的)
	 * 就表示可以成功取得 Root 權限
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
	
	/* 有找到 busybox 在 xbin 或者 bin 底下，並確認時沒發生
	 * SecurityException (被 superuser 擋住之類的)
	 * 就表示有安裝好 busybox
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
	
	
	/* 以 root 身分執行命令，傳回執行後的輸出字串
	 * 	參數：
	 * 		cmds - 字串陣列，將依序執行的指令
	 * 
	 * 	注意：
	 * 		為避免程序沒有結束卡在 ipt.hasNextLine() , 會自動檢查
	 * 		傳入資料中是否包含 "exit" , 若沒有會自動加入此命令
	*/
	public String rootExec(String...cmds){
		return rootExec(true,cmds);
	}
//============== private ================
	private String rootExec(boolean autoEnter,String...cmds){
		//尋找 exit 指令的部分在  rootExecInputStream 中
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
	 * 	以 root 身分執行指令並傳回 InputStream
	*/
	private InputStream rootExecInputStream(boolean autoEnter,String...cmds){
		try{
			//Process proc = new ProcessBuilder().command("su").redirectErrorStream(true).start();
			
			Process proc=runtime.exec("su");
			DataOutputStream opt = new DataOutputStream(proc.getOutputStream());
			InputStream ipt=proc.getInputStream();
			boolean exitCmdFound=false;
			//依照傳入的參數一行一行執行指令
			for(String cmd:cmds){
				opt.writeBytes(cmd);
				// NOTE 此 exit 尋找情況不會考慮到 autoEnter=false 的情況
				if(!exitCmdFound && cmd.equals("exit")) exitCmdFound=true;
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
