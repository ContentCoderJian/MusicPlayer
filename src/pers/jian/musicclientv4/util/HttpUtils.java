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
 * ����Http����(get��post)�Ĺ�����
 */
public class HttpUtils {

	/**
	 * ͨ��Url·��,����Http get����, ��ȡ��Ӧ�ֽ��� 
	 * ������в����� ��Ҫ��path������?ƴ��
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
	 * ͨ��Url·��,����Http post����, ��ȡ��Ӧ�ֽ��� 
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
		
		// ����map, �Ѽ�ֵ��ƴ��Ϊ�����ַ���
		StringBuffer sb = new StringBuffer();
		Set<String> keys = params.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String val = params.get(key);
			sb.append(key + "=" + val + "&");
		}
		
		// �����һ��&�ַ�ȥ����
		sb.deleteCharAt(sb.length() - 1);
		String param = sb.toString();
		os.write(param.getBytes("UTF-8"));
		os.flush();
		
		// 5. InputStream
		InputStream is = conn.getInputStream();
		return is;
	} 
	
	/**
	 * ������������ UTF-8 ��ʽת��Ϊ�ַ���
	 * @param is ��Ӧ���ص�������
	 * @return string �ַ���
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
