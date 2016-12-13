package com.tools.BugUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public final class BugConfig {

	//private static final String BASE_ADDRESS = "http://zhishu.31.tip55.com:8089/oldOdds.aspx?t=4";
    //private static final String BASE_ADDRESS = "http://zhishu.35.tip55.com:869/oldOdds.aspx?t=4";
    private static final String SCHEDULEID_JSP = "http://data.win007.com/soccer_scheduleid.js";
    //private static final String DATA_ADDRESS = "http://1x2.nowscore.com/";//.js

    private static final String getUrl(int id) {
        StringBuffer address_buffer = new StringBuffer("http://op1.win007.com/oddslist/");
        address_buffer.append(String.valueOf(id));
        address_buffer.append(String.valueOf(".htm"));
        String result = address_buffer.toString();
        System.out.println("result-address= " + result);
        return result;
    }

    public static String[] getScheduleIds() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(SCHEDULEID_JSP);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            System.out.println("GET_SCHEDULE_IDS::code= " + code);
            if (code == 200) {
                InputStream in = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, "utf-8");
                BufferedReader reader = new BufferedReader(isr);
                String line = null;
                StringBuffer result_buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    System.out.println("Line= " + line);
                    result_buffer.append(line);
                }
                String final_result=  result_buffer.toString();
                System.out.println("GET_SCHEDULE_IDS::final_result= " + final_result);
                final_result = final_result.replace("var soccer_scheduleid=\"", "");
                int flag_index = final_result.indexOf("\";");
                final_result = final_result.substring(0, flag_index);
                System.out.println("GET_SCHEDULE_IDS::final_result= " + final_result);
                String[] ids = final_result.split(",");
                return ids;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
        return null;
    }
    

    
    public static String getWebContent(int id) {
        String target_address = getUrl(id);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(target_address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            System.out.println("GET_SCHEDULE_IDS::code= " + code);
            if (code == 200) {
                InputStream in = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, "utf-8");
                BufferedReader reader = new BufferedReader(isr);
                String line = null;
                StringBuffer result_buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    System.out.println("Line= " + line);
                    result_buffer.append(line);
                }
                String final_result=  result_buffer.toString();
                
                return final_result;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
        return null;
    }
    
    public static ArrayList<String> getMatchID() {
    	String target_address = "http://zhishu.31.tip55.com:8089/oldOdds.aspx?t=4";
    	WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setUseInsecureSSL(true);  
        wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true  
        wc.getOptions().setCssEnabled(false); // 禁用css支持  
        wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常  
        wc.getOptions().setTimeout(100000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待  
        wc.getOptions().setDoNotTrackEnabled(false);  
        
        try {
        	HtmlPage current_page = wc.getPage(target_address);
        	List<DomElement> list_tr = current_page.getElementsByTagName("tr");
        	ArrayList<String> ids = new ArrayList<String>();
        	for (DomElement element : list_tr) {
				String element_id = element.getAttribute("id");
				String element_index = element.getAttribute("index");
				if (null != element_id && "" != element_id && null != element_index && "" != element_index) {
					System.out.println("getMatchID::current_id= " + element_id + " element_index= " + element_index);
					if (element_id.startsWith("tr_") && !element_id.contains("ad")) {
						String tmp_id = element_id.substring(3);
						System.out.println("getMatchID::current_id1= " + tmp_id);
						ids.add(tmp_id);
					}
				} 
			}
        	return ids;
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
			if (null != wc) 
				wc.close();
		}
    	return null;
    }

    public static OddData getWebContentFromWS(int match_id){
    	String target_uri = getUrl(match_id);
    	
    	WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setUseInsecureSSL(true);  
        wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true  
        wc.getOptions().setCssEnabled(false); // 禁用css支持  
        wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常  
        wc.getOptions().setTimeout(100000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待  
        wc.getOptions().setDoNotTrackEnabled(false);  
        try {
			HtmlPage page = wc.getPage(target_uri);
			if(selectedTargetByTb(page)){
				System.out.println("getWebContentFromWS::info= select target success");
				if(flashPage(page)){
					System.out.println("getWebContentFromWS::info= flash page success");
					HtmlPage new_page = page.getPage();
					System.out.println("getWebContentFromWS::page content=\n" + new_page.asText());
					OddData target_data = getResult(new_page);
					target_data.setTargetAddress(target_uri);
					System.out.println("getWebContentFromWS::data= " + target_data);
					return target_data;
				}
			} else {
				System.out.println("getWebContentFromWS::info= select target success");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != wc) {
				wc.close();
			}
		}
    	return null;
    }
    
    /**
     * 点击“保留选中”以刷新界面
     * @param target_page
     * @return
     */
    private static boolean flashPage(HtmlPage target_page) {
    	List<HtmlAnchor> anchors = target_page.getAnchors();
		HtmlAnchor anchor1 = null;
		for (HtmlAnchor anchor : anchors){
			String action_str = anchor.getAttribute("onclick");
			if(null != action_str && (action_str.contains("delCheck") && action_str.contains("true"))) {
				anchor1 = anchor;
				break;
			}
		}
		
		if(null != anchor1){
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
     * 选中需要的公司
     * @param target_page
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean selectedTarget(HtmlPage target_page) {
    	List<DomElement> shows = target_page.getElementsByName("Show");
		System.out.println("selectedTarget::size= " + (null == shows ? "null":String.valueOf(shows.size())));
		boolean is_success = false;
		for (DomElement element : shows){
			String result = ((HtmlCheckBoxInput)element).getAttribute("value");
			if ("0".equals(result) || "6".equals(result)) {
				try {
					((HtmlCheckBoxInput)element).click();
					is_success = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("selectedTarget::result " + is_success);
    	return is_success;
    }
    
    private static boolean selectedTargetByTb(HtmlPage target_page){
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
    		HtmlCheckBoxInput input_1060 = (HtmlCheckBoxInput) element_1060.getFirstElementChild().getFirstElementChild();
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
    		HtmlCheckBoxInput input_1129 = (HtmlCheckBoxInput) element_1129.getFirstElementChild().getFirstElementChild();
			try {
				input_1129.click();
				is_click = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	return is_click;
    }
    
    /**
     * 获取需要的数据
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
