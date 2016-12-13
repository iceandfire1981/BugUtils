package com.tools.BugUtils;

public final class OddData {
	private String mTargetAddress;
	private float mX, mY, mZ, mResult;

	public OddData(float X, float Y, float Z) {
		super();
		this.mX = X;
		this.mY = Y;
		this.mZ = Z;
		this.mResult = 0.0f;
		this.mTargetAddress = "";
	}

	
	public float getmX() {
		return mX;
	}



	public float getmY() {
		return mY;
	}



	public float getmZ() {
		return mZ;
	}

	public float getmResult(){
		return mResult;
	}

	public void setmResult(float result) {
		mResult = result;
	}
	
	public String getTargetAddress(){
		return mTargetAddress;
	}
	
	public void setTargetAddress(String target_address) {
		mTargetAddress = target_address;
	}

	@Override
	public String toString() {
		return "OddData [getmX()=" + getmX() + ", getmY()=" + getmY() + ", getmZ()=" + getmZ() + ", getmResult()="
				+ getmResult() + ", getTargetAddress()=" + getTargetAddress() + "]";
	}



	
}
