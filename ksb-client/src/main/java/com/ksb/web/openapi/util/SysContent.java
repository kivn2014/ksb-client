package com.ksb.web.openapi.util;

public class SysContent {

	static public final int minNodeKey = 1;// 节点的最小值
	static public final int maxNodeKey = 32;// 节点的最大值
	
	static private int myid = 0;//

	public static int getMyid() {
		return myid;
	}

	public static boolean setMyid(int myid) {
		if (myid >= minNodeKey && myid <= maxNodeKey) {
			SysContent.myid = myid;
			return true;
		}
		return false;
	}
	
}
