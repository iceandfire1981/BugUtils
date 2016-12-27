package com.tools.BugUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.tools.BugUtils.config.ConfigFileUtils;
import com.tools.BugUtils.data.ConfigFileData;

public class WebSiteAnalysis {

    private String mBaseAddress;
    private ConfigFileData mConfigFileData;
    
    public WebSiteAnalysis(String base_address) {
        
        // TODO Auto-generated constructor stub
        if (null == base_address) {
            mBaseAddress = ConfigFileUtils.BASE_ADDRESS;
        } else {
            mBaseAddress = base_address;
        }
        
        mConfigFileData = ConfigFileUtils.analysisConfigFile();
        System.out.println("WebSiteAnalysis::address= " + mBaseAddress + " info= " + mConfigFileData);
    }

    
    
    /**
     * 第一步： 获取包括当天在内的所有比赛日期内的所有比赛ID
     * 
     * @return HashMap<MatchDate, ArrayList<match_id>> 比赛日期与比赛ID列表的映射
     */
    private HashMap<String, ArrayList<String>> getAllMatchIds() {
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setUseInsecureSSL(true);
        wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        wc.getOptions().setCssEnabled(false); // 禁用css支持
        wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        wc.getOptions().setTimeout(5000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        wc.getOptions().setDoNotTrackEnabled(false);
        try {
            HtmlPage current_page = wc.getPage(mBaseAddress);
            HtmlSelect target_select_view = getSelectView(current_page);
            if (null == target_select_view) {
                return null;
            }
            int select_option_size = target_select_view.getOptionSize();
            HashMap<String, ArrayList<String>> all_match = new HashMap<>();
            for (int index = 0; index < select_option_size; index++) {
                target_select_view = getSelectView(current_page);
                System.out.println("getAllMatchIds:target_select_view= " + target_select_view.asText());
                HtmlOption current_option = target_select_view.getOption(index);
                String current_date = current_option.getValueAttribute();
                System.out.println("getAllMatchIds:current_option= " + current_option.asText());
                target_select_view.setSelectedAttribute(current_option, true);
                current_page = current_page.getPage();
                ArrayList<String> current_match_ids = getAllMatchIDFromDate(current_page);
                all_match.put(current_date, current_match_ids);
            }
            return all_match;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != wc) {
                wc.close();
            }
        }
        return null;
    }
    
    /**
     * 选择比赛日期
     * @param current_page
     * @return
     */
    private HtmlSelect getSelectView(HtmlPage current_page) {
        List<DomElement> all_selects = current_page.getElementsByTagName("select");
        HtmlSelect target_select_view = null;
        if (null != all_selects && all_selects.size() > 0) {
            for (DomElement current_element : all_selects) {
                HtmlSelect select_view = (HtmlSelect) current_element;
                String chage_mothod = select_view.getAttribute("onchange");
                if (null != chage_mothod && !"".equals(chage_mothod) && chage_mothod.startsWith("SetDate")) {
                    target_select_view = select_view;
                    break;
                }
            }
        }
        return target_select_view;
    }
    
    /**
     * 从当前比赛页面中获取比赛ID列表
     * 
     * @param current_page
     * @param date
     * @return
     */
    private static ArrayList<String> getAllMatchIDFromDate(HtmlPage current_page) {
        List<DomElement> list_tr = current_page.getElementsByTagName("tr");
        ArrayList<String> ids = new ArrayList<String>();

        for (DomElement element : list_tr) {
            String element_id = element.getAttribute("id");
            String element_index = element.getAttribute("index");
            if (null != element_id && "" != element_id && null != element_index && "" != element_index) {
                System.out.println(
                        "getAllMatchIDFromDate::current_id= " + element_id + " element_index= " + element_index);
                if (element_id.startsWith("tr_") && !element_id.contains("ad")) {
                    String tmp_id = element_id.substring(3);
                    System.out.println("getMatchID::current_id1= " + tmp_id);
                    ids.add(tmp_id);
                }
            }
        }
        return ids;
    }
}
