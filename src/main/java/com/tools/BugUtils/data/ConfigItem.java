package com.tools.BugUtils.data;

import java.util.ArrayList;

public class ConfigItem {
    
    private ArrayList<String> mCompanyList;
    private String mOutputFileName;
    
    public ConfigItem() {
        // TODO Auto-generated constructor stub
        mCompanyList = new ArrayList<String>();
        mOutputFileName = "";
    }

    public ArrayList<String> getCompanyList() {
        return mCompanyList;
    }

    public void setCompanyList(String[] input_list) {
        
    }

    public String getOutputFileName() {
        return mOutputFileName;
    }

    public void setOutputFileName(String output_file_name) {
        mOutputFileName = output_file_name;
    }

    @Override
    public String toString() {
        return "ConfigItem [getCompanyList()=" + getCompanyList() + ", getOutputFileName()=" + getOutputFileName()
                + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString()
                + "]";
    }

    
}
