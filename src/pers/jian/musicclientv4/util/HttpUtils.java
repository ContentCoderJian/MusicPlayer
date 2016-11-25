package pers.jian.musicclientv4.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * 发送Http请求(get和post)的工具类
 */
public class HttpUtils {

	/**
	 * 通过Url路径,发送Http get请求, 获取响应字节流 
	 * 如果含有参数， 需要在path后面用?拼接
	 * 
	 * @param url
	 * @return inputStream
	 * @throws IOException 
	 */
	public static InputStream getInputStream(String path) throws IOException {
		// 1. URL
		URL url = new URL(path);
		
		// 2. HttpURLConnection
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		// 3. InputStream
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();

		return is;
	}

	
	/**
	 * 通过Url路径,发送Http post请求, 获取响应字节流 
	 * @param path
	 * @param params
	 * @return inputStream
	 * @throws IOException
	 */
	public static InputStream postInputStream(String path, Map<String, String> params) throws IOException {
		// 1. URL
		URL url  = new URL(path);
		
		// 2. HttpURLConnection
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		// 3. setRequestMethod() 
		conn.setRequestMethod("POST");
		
		// 4. OutputStream
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		
		// 遍历map, 把键值对拼接为参数字符串
		StringBuffer sb = new StringBuffer();
		Set<String> keys = params.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String val = params.get(key);
			sb.append(key + "=" + val + "&");
		}
		
		// 把最后一个&字符去除掉
		sb.deleteCharAt(sb.length() - 1);
		String param = sb.toString();
		os.write(param.getBytes("UTF-8"));
		os.flush();
		
		// 5. InputStream
		InputStream is = conn.getInputStream();
		return is;
	} 
	
	/**
	 * 将输入流按照 UTF-8 格式转换为字符串
	 * @param is 响应返回的输入流
	 * @return string 字符串
	 * @throws IOException 
	 */
	public static String isToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
	
	
	

}
