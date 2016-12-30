package com.tools.BugUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.tools.BugUtils.config.ConfigFileUtils;
import com.tools.BugUtils.data.ConfigFileData;
import com.tools.BugUtils.data.ConfigItem;
import com.tools.BugUtils.data.OddData;

public class WebSiteAnalysis {

    private String mBaseAddress;
    private ConfigFileData mConfigFileData;
    private HashMap<String, ArrayList<String>> mAllMatchIds;
    
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

    
    public void getWebsiteData(){
        System.out.println("getWebsiteData::begin::Get data from [" + mBaseAddress + "]");
        mAllMatchIds = getAllMatchIds();
        ConfigFileUtils.writeMatchIdFile(mAllMatchIds);
        if (null == mAllMatchIds || mAllMatchIds.isEmpty()) {
            System.out.println("getWebsiteData::begin::Can not get data from [" + mBaseAddress + "]");
        } else {
            HashMap<String, HashMap<String, ArrayList<OddData>>> all_data_map = new HashMap<String, HashMap<String, ArrayList<OddData>>>();
            Iterator<Entry<String, ArrayList<String>>> all_match_iterator = mAllMatchIds.entrySet().iterator();
            while (all_match_iterator.hasNext()) {
                Entry<String, ArrayList<String>> all_match_ids_entry = (Entry<String, ArrayList<String>>) all_match_iterator
                        .next();
                String current_date = all_match_ids_entry.getKey();
                ArrayList<String> current_date_matchs = all_match_ids_entry.getValue();
                System.out.println("getWebsiteData::info::date= " + current_date + " current_date_matchs= "
                        + (null == current_date_matchs ? " null " : String.valueOf(current_date_matchs.size())));
                HashMap<String, ArrayList<OddData>> one_day_matchs = getOneDayData(current_date_matchs);
                if (null != one_day_matchs && !one_day_matchs.isEmpty()) {
                    all_data_map.put(current_date, one_day_matchs);
                }
            }
            deleteHistory();
            outPutResultToFiles(all_data_map);
            
        }
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
     * 第二步 获取每一天的数据
     * @param current_page
     * @param one_day_match_ids
     * @return 
     */
    private HashMap<String, ArrayList<OddData>> getOneDayData(ArrayList<String> one_day_match_ids) {
        System.out.println("getOneDayData::info::Begin to get data=======");
        
        if (null != mConfigFileData && mConfigFileData.getRepeatTimes() > 0 && 
                null != mConfigFileData.getAllConfigItems() && 
                mConfigFileData.getAllConfigItems().size() == mConfigFileData.getRepeatTimes()) {
            if (null != one_day_match_ids && one_day_match_ids.size() > 0) {
                HashMap<String, ArrayList<OddData>> map_cos_data = new HashMap<String, ArrayList<OddData>>();
                for (int current_time = 0; current_time < mConfigFileData.getRepeatTimes(); current_time++) {
                    ArrayList<String> current_cos = mConfigFileData.getAllConfigItems().get(current_time).getCompanyList();
                    String file_pre = mConfigFileData.getAllConfigItems().get(current_time).getOutputFileName();
                    ArrayList<OddData> one_day_match_datas = new ArrayList<OddData>();
                    for(String current_match_id : one_day_match_ids) {
                        String current_match_url = getUrl(Integer.valueOf(current_match_id));
                        WebClient wc = new WebClient(BrowserVersion.CHROME);
                        wc.getOptions().setUseInsecureSSL(true);
                        wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
                        wc.getOptions().setCssEnabled(false); // 禁用css支持
                        wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
                        wc.getOptions().setTimeout(5000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
                        wc.getOptions().setDoNotTrackEnabled(false);
                        try{
                            HtmlPage current_match_page = wc.getPage(current_match_url);
                            if (selectTargetCos(current_match_page, current_cos)) {
                                if (flashPage(current_match_page)) {
                                    HtmlPage new_page = current_match_page.getPage();
                                    OddData current_data = getResult(new_page);
                                    current_data.setTargetAddress(current_match_url);
                                    if (null != current_data) {
                                        one_day_match_datas.add(current_data);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                            e.printStackTrace();
                        }
                    }
                    map_cos_data.put(file_pre, one_day_match_datas);
                }
            }
        }
        return null;
    }
    
    private void outPutResultToFiles(HashMap<String, HashMap<String, ArrayList<OddData>>> all_matchs_datas){
        if (null == all_matchs_datas || all_matchs_datas.isEmpty()) {
            return;
        }
        Iterator<Entry<String, HashMap<String, ArrayList<OddData>>>> iterator_all_matchs_datas = all_matchs_datas.entrySet().iterator();
        while(iterator_all_matchs_datas.hasNext()){
            Entry<String, HashMap<String, ArrayList<OddData>>> current_entry = iterator_all_matchs_datas.next();
            String current_date = current_entry.getKey();
            HashMap<String, ArrayList<OddData>> one_day_data = current_entry.getValue();
            StringBuffer file_name_buffer = new StringBuffer("record_file_" + current_date);
            StringBuffer file_no_name_buffer = new StringBuffer("record_file_" + current_date);
            StringBuffer yes_record_buffer = new StringBuffer();
            StringBuffer no_record_buffer = new StringBuffer();
            Iterator<Entry<String, ArrayList<OddData>>> cos_all_datas_iterator = one_day_data.entrySet().iterator();
            while (cos_all_datas_iterator.hasNext()) {
                Entry<String, ArrayList<OddData>> cos_all_datas_entry = cos_all_datas_iterator.next();
                String co_pre = cos_all_datas_entry.getKey();
                ArrayList<OddData> current_datas = cos_all_datas_entry.getValue();
                file_name_buffer.append(co_pre + "txt");
                file_no_name_buffer.append(co_pre + "_no.txt");
                for (OddData current_data : current_datas) {
                    if (current_data.getmResult() >= 0.01F) {
                        yes_record_buffer.append(current_data.getTargetAddress() + " : ");
                        yes_record_buffer.append("x= " + current_data.getmX() + " : ");
                        yes_record_buffer.append("y= " + current_data.getmY() + " : ");
                        yes_record_buffer.append("z= " + current_data.getmZ() + " : ");
                        yes_record_buffer.append("result= " + current_data.getmResult());
                        yes_record_buffer.append("\n");
                    } else {
                        no_record_buffer.append(current_data.getTargetAddress() + " : ");
                        no_record_buffer.append("x= " + current_data.getmX() + " : ");
                        no_record_buffer.append("y= " + current_data.getmY() + " : ");
                        no_record_buffer.append("z= " + current_data.getmZ() + " : ");
                        no_record_buffer.append("result= " + current_data.getmResult());
                        no_record_buffer.append("\n");
                    }
                }
                File record_file = new File(file_name_buffer.toString());
                if (record_file.exists()) {
                    record_file.delete();
                }
                File record_no_file = new File(file_no_name_buffer.toString());
                if (record_no_file.exists()) {
                    record_no_file.delete();
                }
                
                try{
                    record_file.createNewFile();
                    record_no_file.createNewFile();

                    FileOutputStream out = new FileOutputStream(record_file, true);
                    FileOutputStream out_no = new FileOutputStream(record_no_file, true);

                    out.write(yes_record_buffer.toString().getBytes("utf-8"));
                    out_no.write(no_record_buffer.toString().getBytes("utf-8"));

                    out.close();
                    out_no.close();
                }catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }
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
    private ArrayList<String> getAllMatchIDFromDate(HtmlPage current_page) {
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
    
    /**
     * 选择指定的公司
     * @param current_page
     * @param company_list
     * @return
     */
    private boolean selectTargetCos(HtmlPage current_page, ArrayList<String> company_list){
        boolean is_selected = false;
        
        if (null != current_page && null != company_list && company_list.size() > 0) {
            for(int index = 0; index < company_list.size(); index++){
                String current_company_id = "oddstr_" + company_list.get(index);
                System.out.println("selectTargetCos::id= " + current_company_id);
                DomElement current_root_element = current_page.getElementById(current_company_id);
                if (null != current_root_element) {
                    HtmlCheckBoxInput current_input = (HtmlCheckBoxInput) current_root_element.getFirstElementChild().getFirstElementChild();
                    try {
                        current_input.click();
                        is_selected = true;
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return is_selected;
    }
    
    /**
     * 确认修改
     * @param current_page
     * @return
     */
    private boolean flashPage(HtmlPage current_page) {
        boolean is_success = false;
        List<HtmlAnchor> anchors = current_page.getAnchors();
        HtmlAnchor selected_anchor = null;
        for (HtmlAnchor anchor : anchors) {
            String action_str = anchor.getAttribute("onclick");
            if (null != action_str && (action_str.contains("delCheck") && action_str.contains("true"))) {
                selected_anchor = anchor;
                break;
            }
        }
        
        if (null != selected_anchor) {
            try {
                selected_anchor.click();
                is_success = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("flashPage::info= nothing to click");
        }
        return is_success;
    }
    
    /**
     * 拼装比赛详情链接
     * 
     * @param id
     * @return
     */
    private String getUrl(int id) {
        StringBuffer address_buffer = new StringBuffer(ConfigFileUtils.BASE_MATCH_URI);
        address_buffer.append(String.valueOf(id));
        address_buffer.append(String.valueOf(".htm"));
        String result = address_buffer.toString();
        System.out.println("getUrl::result-address= " + result);
        return result;
    }
    
    private void deleteHistory(){
        File current_dir = new File("");
        current_dir = new File(current_dir.getAbsolutePath());
        String[] all_files = current_dir.list();
        if (null != all_files && all_files.length > 0) {
            for (int i = 0; i < all_files.length; i++) {
                String current_file_name = all_files[i];
                System.out.println("writeRecordfile::delete::name= " + current_file_name);
                File current_file = new File(current_file_name);
                String file_name = current_file.getName();
                if (null != file_name && file_name.startsWith("record_file")) {
                    current_file.delete();
                }
            }
        }
    }
    
    /**
     * 获取需要的数据
     * 
     * @param target_page
     * @return
     */
    private static OddData getResult(HtmlPage target_page) {
        List<DomElement> links = target_page.getElementsByIdAndOrName("highRObj");
        OddData current_data = null;
        for (DomElement element : links) {
            String result = element.asText();
            result = result.replace('\t', ',');
            String[] results = result.split(",");
            current_data = new OddData(Float.valueOf(results[1]), Float.valueOf(results[2]), Float.valueOf(results[3]));
            System.out.println("getResultcurrent_data= " + current_data);

        }
        return current_data;
    }

}
