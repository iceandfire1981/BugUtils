package com.tools.BugUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class App 
{
    public static void main( String[] args ) {
        
        HashMap<String, ArrayList<String>> all_support_matchs = WebsiteUtils.getAllMatchIds();
        WebsiteUtils.writeRecordIdFile(all_support_matchs);
        HashMap<String, ArrayList<OddData>> all_match_datas = WebsiteUtils.getAllMatchData(all_support_matchs);
        System.out.println("main::size= " + (null == all_match_datas ? "null" : String.valueOf(all_match_datas.size())));
        WebsiteUtils.writeRecordfile(all_match_datas);

        while(WebsiteUtils.changeFlag()){
            //all_support_matchs = WebsiteUtils.getAllMatchIds();
            all_match_datas = WebsiteUtils.getAllMatchData(all_support_matchs);
            System.out.println("main::size= " + (null == all_match_datas ? "null" : String.valueOf(all_match_datas.size())));
            WebsiteUtils.writeRecordfile(all_match_datas);
        }
        
        return;
    	
    }
}
