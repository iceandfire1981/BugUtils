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

public class WebsiteUtils {
    private static final String BASE_ADDRESS = "http://zhishu.31.tip55.com:8089/oldOdds.aspx?t=4";
    
    /**
     * 0：获取3家数据，1：获取9家数据， 2：获取13家数据
     */
    private static int GET_COUNT = 0;
    
    /**
     * 第一步： 获取包括当天在内的所有比赛日期内的所有比赛ID
     * @return HashMap<MatchDate, ArrayList<match_id>>
     */
    public static HashMap<String, ArrayList<String>> getAllMatchIds() {
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setUseInsecureSSL(true);
        wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        wc.getOptions().setCssEnabled(false); // 禁用css支持
        wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        wc.getOptions().setTimeout(5000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        wc.getOptions().setDoNotTrackEnabled(false);
        try {
            HtmlPage current_page = wc.getPage(BASE_ADDRESS);
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
     * 第二步：分析所有比赛，生成数据字典
     * @param all_match_ids
     * @return
     */
    public static HashMap<String, ArrayList<OddData>> getAllMatchData(
            HashMap<String, ArrayList<String>> all_match_ids) {
        Iterator<Entry<String, ArrayList<String>>> match_iterator = all_match_ids.entrySet().iterator();
        HashMap<String, ArrayList<OddData>> all_math_data = new HashMap<>();
        while (match_iterator.hasNext()) {
            Entry<String, ArrayList<String>> current_entry = (Entry<String, ArrayList<String>>) match_iterator.next();
            String current_date = current_entry.getKey();
            ArrayList<String> ids = current_entry.getValue();
            System.out.println("getAllMatchData::current_key= " + current_date + " ids= "
                    + (null == ids ? "null" : String.valueOf(ids.size())));
            
            ArrayList<OddData> one_day_data = new ArrayList<>();
            for (String id : ids) {
                int int_id = Integer.valueOf(id);
                OddData current_data = WebsiteUtils.getMatchData(int_id);
                one_day_data.add(current_data);
            }
            all_math_data.put(current_date, one_day_data);
        }

        return all_math_data;
    }

    /**
     * 第三步：写数据文件
     * @param all_match_data
     */
    public static void writeRecordfile(HashMap<String, ArrayList<OddData>> all_match_data){
        System.out.print("writeRecordfile::flag= " + GET_COUNT);
        if (GET_COUNT == 0) {
            File current_dir =  new File("");
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
        
        Iterator<Entry<String, ArrayList<OddData>>> data_iterator = all_match_data.entrySet().iterator();
        while(data_iterator.hasNext()) {
            Entry<String, ArrayList<OddData>> current_entry = data_iterator.next();
            String current_date = current_entry.getKey();
            ArrayList<OddData> current_datas = current_entry.getValue();
            for(OddData current_data : current_datas) {
                float x = current_data.getmX();
                float y = current_data.getmY();
                float z = current_data.getmZ();
                
                float result_tmp = (y/x) + 1 + (y/z);
                float result = y - result_tmp;
                current_data.setmResult(result);
            }
            
            StringBuffer file_name_buffer = new StringBuffer("record_file_");
            file_name_buffer.append(current_date);
            switch(GET_COUNT) {
            case 0:
                file_name_buffer.append("_THREE");
                break;
            case 1:
                file_name_buffer.append("_TEN");
                break;
            case 2:
                file_name_buffer.append("_ALL");
                break;
            default:
                file_name_buffer.append("_ALL");
                break;
            }
            file_name_buffer.append(".txt");
            String record_file_name = file_name_buffer.toString();
            
            File record_file = new File(record_file_name);
            if (record_file.exists()) {
                record_file.delete();
            }
            
            try{
            record_file.createNewFile();
            FileOutputStream out=new FileOutputStream(record_file,true);
            StringBuffer result_buffer = new StringBuffer();
            for (OddData current_data : current_datas) {
                result_buffer.append(current_data.getTargetAddress() + " : ");
                result_buffer.append("x= " + current_data.getmX() + " : ");
                result_buffer.append("y= " + current_data.getmY() + " : ");
                result_buffer.append("z= " + current_data.getmZ() + " : ");
                result_buffer.append("result= " + current_data.getmResult());
                result_buffer.append(current_data.getmResult() >= 0.01 ? " (Yes) " : " (No) ");
                result_buffer.append("\n");
            }
            out.write(result_buffer.toString().getBytes("utf-8"));
            out.close();
            } catch(Exception e) {
                e.printStackTrace();
            } 
        }
    }
    
    public static boolean changeFlag(){
        System.out.print("Before::changeFlag::flag= " + GET_COUNT);
        boolean is_success = false;
        GET_COUNT = GET_COUNT + 1;
        if (GET_COUNT < 3) {
            is_success = true;
        } else {
            GET_COUNT = 0;
            is_success = false;
        }
        System.out.print("After::changeFlag::flag= " + GET_COUNT);
        return is_success;
    }

    /**
     * 获取某一场比赛的数据
     * @param match_id
     * @return
     */
    private static OddData getMatchData(int match_id) {
        String target_uri = getUrl(match_id);
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setUseInsecureSSL(true);
        wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        wc.getOptions().setCssEnabled(false); // 禁用css支持
        wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        wc.getOptions().setTimeout(5000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        wc.getOptions().setDoNotTrackEnabled(false);
        try {
            HtmlPage page = wc.getPage(target_uri);
            if (selectedTargetByTb(page)) {
                System.out.println("getMatchData::info= select target success");
                if (flashPage(page)) {
                    System.out.println("getMatchData::info= flash page success");
                    HtmlPage new_page = page.getPage();
                    System.out.println("getMatchData::page content=\n" + new_page.asText());
                    OddData target_data = getResult(new_page);
                    target_data.setTargetAddress(target_uri);
                    System.out.println("getMatchData::data= " + target_data);
                    return target_data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != wc)
                wc.close();
        }
        return null;
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

    /**
     * 拼装比赛详情链接
     * 
     * @param id
     * @return
     */
    private static final String getUrl(int id) {
        StringBuffer address_buffer = new StringBuffer("http://op1.win007.com/oddslist/");
        address_buffer.append(String.valueOf(id));
        address_buffer.append(String.valueOf(".htm"));
        String result = address_buffer.toString();
        System.out.println("result-address= " + result);
        return result;
    }

    private static final HtmlSelect getSelectView(HtmlPage current_page) {
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
     * 选择指定的公司
     * 
     * @param target_page
     * @return
     */
    private static boolean selectedTargetByTb(HtmlPage target_page) {
        System.out.print("selectedTargetByTb::flag= " + GET_COUNT);
        boolean is_click = false;
        switch(GET_COUNT) {
        case 0:
            is_click = getDataFrom3COs(target_page);
            break;
        case 1:
            is_click = getDataFrom10COs(target_page);
            break;
        case 2:
            is_click = getDataFrom11Cos(target_page);
            break;
        default:
             is_click = false;
             break;
        }
        return is_click;
    }
    
    /**
     * GET_COUNT=0
     * @param target_page
     * @return
     */
    private static final boolean getDataFrom3COs(HtmlPage target_page){
        boolean is_click = false;
        
        DomElement element_90 = target_page.getElementById("oddstr_90");
        if (null != element_90) {
            System.out.println("selectedTargetByTb::info::90 click ");
            HtmlCheckBoxInput input_90 = (HtmlCheckBoxInput) element_90.getFirstElementChild().getFirstElementChild();
            try {
                input_90.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_281 = target_page.getElementById("oddstr_281");
        if (null != element_281) {
            System.out.println("selectedTargetByTb::info::281 click ");
            HtmlCheckBoxInput input_281 = (HtmlCheckBoxInput) element_281.getFirstElementChild().getFirstElementChild();
            try {
                input_281.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_499 = target_page.getElementById("oddstr_499");
        if (null != element_499) {
            System.out.println("selectedTargetByTb::info::499 click ");
            HtmlCheckBoxInput input_499 = (HtmlCheckBoxInput) element_499.getFirstElementChild().getFirstElementChild();
            try {
                input_499.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return is_click;
    }
    
    /**
     * GET_COUNT=1
     * @param target_page
     * @return
     */
    private static final boolean getDataFrom10COs(HtmlPage target_page){
        boolean is_click = false;
        DomElement element_90 = target_page.getElementById("oddstr_90");
        if (null != element_90) {
            System.out.println("selectedTargetByTb::info::90 click ");
            HtmlCheckBoxInput input_90 = (HtmlCheckBoxInput) element_90.getFirstElementChild().getFirstElementChild();
            try {
                input_90.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_281 = target_page.getElementById("oddstr_281");
        if (null != element_281) {
            System.out.println("selectedTargetByTb::info::281 click ");
            HtmlCheckBoxInput input_281 = (HtmlCheckBoxInput) element_281.getFirstElementChild().getFirstElementChild();
            try {
                input_281.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_499 = target_page.getElementById("oddstr_499");
        if (null != element_499) {
            System.out.println("selectedTargetByTb::info::499 click ");
            HtmlCheckBoxInput input_499 = (HtmlCheckBoxInput) element_499.getFirstElementChild().getFirstElementChild();
            try {
                input_499.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_482 = target_page.getElementById("oddstr_482");
        if (null != element_482) {
            System.out.println("selectedTargetByTb::info::482 click ");
            HtmlCheckBoxInput input_482 = (HtmlCheckBoxInput) element_482.getFirstElementChild().getFirstElementChild();
            try {
                input_482.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_798 = target_page.getElementById("oddstr_798");
        if (null != element_798) {
            System.out.println("selectedTargetByTb::info::798 click ");
            HtmlCheckBoxInput input_798 = (HtmlCheckBoxInput) element_798.getFirstElementChild().getFirstElementChild();
            try {
                input_798.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        DomElement element_517 = target_page.getElementById("oddstr_517");
        if (null != element_517) {
            System.out.println("selectedTargetByTb::info::517 click ");
            HtmlCheckBoxInput input_517 = (HtmlCheckBoxInput) element_517.getFirstElementChild().getFirstElementChild();
            try {
                input_517.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_255 = target_page.getElementById("oddstr_255");
        if (null != element_255) {
            System.out.println("selectedTargetByTb::info::255 click ");
            HtmlCheckBoxInput input_255 = (HtmlCheckBoxInput) element_255.getFirstElementChild().getFirstElementChild();
            try {
                input_255.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        DomElement element_136 = target_page.getElementById("oddstr_136");
        if (null != element_136) {
            System.out.println("selectedTargetByTb::info::136 click ");
            HtmlCheckBoxInput input_136 = (HtmlCheckBoxInput) element_136.getFirstElementChild().getFirstElementChild();
            try {
                input_136.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        DomElement element_115 = target_page.getElementById("oddstr_115");
        if (null != element_115) {
            System.out.println("selectedTargetByTb::info::115 click ");
            HtmlCheckBoxInput input_115 = (HtmlCheckBoxInput) element_115.getFirstElementChild()
                    .getFirstElementChild();
            try {
                input_115.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        DomElement element_82 = target_page.getElementById("oddstr_82");
        if (null != element_82) {
            System.out.println("selectedTargetByTb::info::82 click ");
            HtmlCheckBoxInput input_82 = (HtmlCheckBoxInput) element_82.getFirstElementChild()
                    .getFirstElementChild();
            try {
                input_82.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return is_click;
    }
    
    /**
     * GET_COUNT=2
     * @param target_page
     * @return
     */
    private static final boolean getDataFrom11Cos(HtmlPage target_page) {
        boolean is_click = false;
        
        DomElement element_90 = target_page.getElementById("oddstr_90");
        if (null != element_90) {
            System.out.println("selectedTargetByTb::info::90 click ");
            HtmlCheckBoxInput input_90 = (HtmlCheckBoxInput) element_90.getFirstElementChild().getFirstElementChild();
            try {
                input_90.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_281 = target_page.getElementById("oddstr_281");
        if (null != element_281) {
            System.out.println("selectedTargetByTb::info::281 click ");
            HtmlCheckBoxInput input_281 = (HtmlCheckBoxInput) element_281.getFirstElementChild().getFirstElementChild();
            try {
                input_281.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_499 = target_page.getElementById("oddstr_499");
        if (null != element_499) {
            System.out.println("selectedTargetByTb::info::499 click ");
            HtmlCheckBoxInput input_499 = (HtmlCheckBoxInput) element_499.getFirstElementChild().getFirstElementChild();
            try {
                input_499.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_482 = target_page.getElementById("oddstr_482");
        if (null != element_482) {
            System.out.println("selectedTargetByTb::info::482 click ");
            HtmlCheckBoxInput input_482 = (HtmlCheckBoxInput) element_482.getFirstElementChild().getFirstElementChild();
            try {
                input_482.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_798 = target_page.getElementById("oddstr_798");
        if (null != element_798) {
            System.out.println("selectedTargetByTb::info::798 click ");
            HtmlCheckBoxInput input_798 = (HtmlCheckBoxInput) element_798.getFirstElementChild().getFirstElementChild();
            try {
                input_798.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_1060 = target_page.getElementById("oddstr_1060");
        if (null != element_1060) {
            System.out.println("selectedTargetByTb::info::1060 click ");
            HtmlCheckBoxInput input_1060 = (HtmlCheckBoxInput) element_1060.getFirstElementChild()
                    .getFirstElementChild();
            try {
                input_1060.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_517 = target_page.getElementById("oddstr_517");
        if (null != element_517) {
            System.out.println("selectedTargetByTb::info::517 click ");
            HtmlCheckBoxInput input_517 = (HtmlCheckBoxInput) element_517.getFirstElementChild().getFirstElementChild();
            try {
                input_517.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_255 = target_page.getElementById("oddstr_255");
        if (null != element_255) {
            System.out.println("selectedTargetByTb::info::255 click ");
            HtmlCheckBoxInput input_255 = (HtmlCheckBoxInput) element_255.getFirstElementChild().getFirstElementChild();
            try {
                input_255.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_772 = target_page.getElementById("oddstr_772");
        if (null != element_772) {
            System.out.println("selectedTargetByTb::info::772 click ");
            HtmlCheckBoxInput input_772 = (HtmlCheckBoxInput) element_772.getFirstElementChild().getFirstElementChild();
            try {
                input_772.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_136 = target_page.getElementById("oddstr_136");
        if (null != element_136) {
            System.out.println("selectedTargetByTb::info::136 click ");
            HtmlCheckBoxInput input_136 = (HtmlCheckBoxInput) element_136.getFirstElementChild().getFirstElementChild();
            try {
                input_136.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        DomElement element_1129 = target_page.getElementById("oddstr_1129");
        if (null != element_1129) {
            System.out.println("selectedTargetByTb::info::1129 click ");
            HtmlCheckBoxInput input_1129 = (HtmlCheckBoxInput) element_1129.getFirstElementChild()
                    .getFirstElementChild();
            try {
                input_1129.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        DomElement element_115 = target_page.getElementById("oddstr_115");
        if (null != element_115) {
            System.out.println("selectedTargetByTb::info::115 click ");
            HtmlCheckBoxInput input_115 = (HtmlCheckBoxInput) element_115.getFirstElementChild()
                    .getFirstElementChild();
            try {
                input_115.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        DomElement element_82 = target_page.getElementById("oddstr_82");
        if (null != element_82) {
            System.out.println("selectedTargetByTb::info::82 click ");
            HtmlCheckBoxInput input_82 = (HtmlCheckBoxInput) element_82.getFirstElementChild()
                    .getFirstElementChild();
            try {
                input_82.click();
                is_click = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return is_click;
    }
    
    /**
     * 点击“保留选中”以刷新界面
     * 
     * @param target_page
     * @return
     */
    private static boolean flashPage(HtmlPage target_page) {
        List<HtmlAnchor> anchors = target_page.getAnchors();
        HtmlAnchor anchor1 = null;
        for (HtmlAnchor anchor : anchors) {
            String action_str = anchor.getAttribute("onclick");
            if (null != action_str && (action_str.contains("delCheck") && action_str.contains("true"))) {
                anchor1 = anchor;
                break;
            }
        }

        if (null != anchor1) {
            System.out.println("flashPage::info= click");
            try {
                anchor1.click();
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("flashPage::info= nothing to click");
            return false;
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