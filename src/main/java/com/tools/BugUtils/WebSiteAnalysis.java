package com.tools.BugUtils;

import com.tools.BugUtils.config.ConfigFileUtils;

public class WebSiteAnalysis {

    private String mBaseAddress;
    
    public WebSiteAnalysis(String base_address) {
        
        // TODO Auto-generated constructor stub
        if (null == base_address) {
            mBaseAddress = ConfigFileUtils.BASE_ADDRESS;
        } else {
            mBaseAddress = base_address;
        }
        
        
        
    }

}
