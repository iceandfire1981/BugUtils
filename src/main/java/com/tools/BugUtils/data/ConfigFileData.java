package com.tools.BugUtils.data;

import java.util.ArrayList;

public class ConfigFileData {
    
    private int mRepeatTimes, mConfigCount;
    private ArrayList<ConfigItem> mAllConfigItems;
    
    public ConfigFileData() {
        // TODO Auto-generated constructor stub
        mRepeatTimes = 0;
        mConfigCount = 0;
        mAllConfigItems = new ArrayList<ConfigItem>();
    }

    public int getRepeatTimes() {
        return mRepeatTimes;
    }

    public void setRepeatTimes(int repeat_times) {
        mRepeatTimes = repeat_times;
    }

    public int getConfigCount() {
        return mConfigCount;
    }

    public void setmConfigCount(int config_count) {
        mConfigCount = config_count;
    }

    public ArrayList<ConfigItem> getAllConfigItems() {
        return mAllConfigItems;
    }

    public void setAllConfigItems(ArrayList<ConfigItem> all_config_items) {
        mAllConfigItems = all_config_items;
    }
    
    public void addConfigItem(ConfigItem current_item) {
        System.out.println("addConfigItem::info= " + current_item);
        if (null == current_item || mAllConfigItems.contains(current_item)) {
            return;
        }
        mAllConfigItems.add(current_item);
    }

    @Override
    public String toString() {
        return "ConfigFileData [getRepeatTimes()=" + getRepeatTimes() + ", getConfigCount()=" + getConfigCount()
                + ", getAllConfigItems()=" + getAllConfigItems() + "]";
    }
    
}
