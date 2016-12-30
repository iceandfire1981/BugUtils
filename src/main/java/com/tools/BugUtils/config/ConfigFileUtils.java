package com.tools.BugUtils.config;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.tools.BugUtils.data.ConfigFileData;
import com.tools.BugUtils.data.ConfigItem;

public final class ConfigFileUtils {
    
    public static final String BASE_ADDRESS = "http://zhishu.165.tip55.com:869/oldOdds.aspx?t=4";
    public static final String BASE_MATCH_URI = "http://op1.win007.com/oddslist/";

    private static final boolean IS_DEBUG = true;
    private static final String CONFIG_FILE_NAME = "BugUtilConfig.xml";
    private static final String TAG_CONFIG_GET_TIMES = "get_times";
    private static final String TAG_CONFIG_LIST = "config_list";
    private static final String TAG_CONFIG_COS = "comp_list";
    private static final String TAG_CONFIG_FILE_PRE = "file_name_pre";
    
    @SuppressWarnings("unchecked")
    public static final ConfigFileData analysisConfigFile(){
        SAXReader xml_reader = new SAXReader();
        File config_file = new File(CONFIG_FILE_NAME);
        try {
            Document xml_doc = xml_reader.read(config_file);
            Element root_element = xml_doc.getRootElement();
            List<Element> all_children_list = root_element.elements();
            ConfigFileData file_data = new ConfigFileData();
            
            for (Element current_element : all_children_list) {
                System.out.println("current_element= " + current_element);
                if (TAG_CONFIG_GET_TIMES.equals(current_element.getName())) {
                    String repeat_times = current_element.getText();
                    file_data.setRepeatTimes(Integer.valueOf(repeat_times));
                }
                
                if (TAG_CONFIG_LIST.equals(current_element.getName())) {
                    ArrayList<ConfigItem> all_config_items = getAllConfigItems(current_element);
                    file_data.setAllConfigItems(all_config_items);
                }
            }
            return file_data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static final ArrayList<ConfigItem> getAllConfigItems(Element root_element) {
        System.out.println("getAllConfigItems::info= " + root_element);
        List<Element> all_config_items = root_element.elements();
        ArrayList<ConfigItem> all_config_item_datas = new ArrayList<ConfigItem>();
        for (Element current_item : all_config_items) {
            System.out.println("getAllConfigItems::item= " + current_item);
            ConfigItem current_item_data = new ConfigItem();
            List<Element> config_items = current_item.elements();
            for (Element config_item : config_items) {
                if (TAG_CONFIG_COS.equals(config_item.getName())) {
                    String[] cos = config_item.getText().split("|");
                    current_item_data.setCompanyList(cos);
                }
                
                if (TAG_CONFIG_FILE_PRE.equals(config_item.getName())){
                    String file_name_pre = config_item.getText();
                    current_item_data.setOutputFileName(file_name_pre);
                }
            }
            all_config_item_datas.add(current_item_data);
        }
        return all_config_item_datas;
    }
    
    public static void writeMatchIdFile(HashMap<String, ArrayList<String>> all_match_ids) {
        if (IS_DEBUG) {
            try {
                File record_file = new File("match_ids.txt");
                record_file.createNewFile();
                FileOutputStream out = new FileOutputStream(record_file, true);
                StringBuffer result_buffer = new StringBuffer();
                Iterator<Entry<String, ArrayList<String>>> map_iterator = all_match_ids.entrySet().iterator();
                if (null == all_match_ids || all_match_ids.isEmpty()) {
                    result_buffer.append("No matchs here!");
                } else {
                    while (map_iterator.hasNext()) {
                        Entry<String, ArrayList<String>> record_entry = map_iterator.next();
                        result_buffer.append(record_entry.getKey() + " : ");
                        ArrayList<String> current_day_match_ids = record_entry.getValue();
                        for (String current_id : current_day_match_ids) {
                            result_buffer.append(current_id + " , ");
                        }
                        result_buffer.append("=======[" + String.valueOf(current_day_match_ids.size()) + " ]========");
                        result_buffer.append("\n");
                    }
                    out.write(result_buffer.toString().getBytes("utf-8"));
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

    }
}
