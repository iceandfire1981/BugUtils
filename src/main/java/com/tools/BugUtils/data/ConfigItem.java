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
        if (null == input_list || input_list.length <= 0) {
            return;
        }
        if (null == mCompanyList || mCompanyList.isEmpty()) {
            mCompanyList = new ArrayList<String>();
        }
        
        for(int index = 0; index<input_list.length; index ++) {
            String current_company_id = input_list[index];
            if (!mCompanyList.contains(current_company_id)) {
                mCompanyList.add(current_company_id);
            }
        }
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
