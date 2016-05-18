package com.kiwihealthcare.bpppgcollector;

import android.content.Context;
import android.widget.Toast;

public class ToastManager {
	private static Toast toast;
	private static long lastTime=0;
	private static String lastContent="";
	public static void show(Context context,String content){
		long currentTime=System.currentTimeMillis();
		if(Math.abs(currentTime-lastTime)>3000){
			toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
			toast.show();
			lastTime=currentTime;
		}else{
			if(!lastContent.equals(content)){
				if(toast!=null)toast.cancel();
				toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
				toast.show();
				lastTime=currentTime;
			}
		}
		lastContent=content;
	}
	public static void cancel(){
		if(toast!=null)toast.cancel();
	}
}
