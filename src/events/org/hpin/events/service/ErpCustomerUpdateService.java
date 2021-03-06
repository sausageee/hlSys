package org.hpin.events.service;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hpin.common.core.orm.BaseService;
import org.hpin.common.util.DateUtils;
import org.hpin.events.dao.ErpCustomerDao;
import org.hpin.events.entity.ErpCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(value = "org.hpin.events.service.ErpCustomerUpdateService")
@Transactional()
public class ErpCustomerUpdateService extends BaseService {

	private Logger log = Logger.getLogger(ErpCustomerUpdateService.class);
	
	@Autowired
	ErpCustomerDao dao;
	
	/**
	 * 读取xlsx后缀的excel
	 * @param file
	 * @return
	 * @throws Exception
	 * @author tangxing
	 * @date 2016-7-13下午7:17:42
	 */
	public void saveCustomerXlsx(File file) throws Exception{
		List<Map<String, String>> result = xlsxExcel(file);
		
		if(result.size()>0){
			String codeNum = "";
			String dateNum = "";
			Map<String, String> map = result.get(0);
				String valueStr = map.toString();
				if(valueStr.indexOf("条形码")!=-1&&valueStr.indexOf("送检日期")!=-1){
					for(int i=0;i<map.keySet().size();++i){
						String s = String.valueOf(i);
						String key = map.get(s);
						if(key.equals("条形码")){
							codeNum = s;
						}
						if(key.equals("送检日期")){
							dateNum = s;
						}
					}
				}
			if(!StringUtils.isEmpty(codeNum)&&!StringUtils.isEmpty(dateNum)){
				int count = 0;
				for(int j = 1; j<result.size(); ++j){
					/*System.out.println("code---"+result.get(j).get(codeNum));	
					System.out.println("date---"+result.get(j).get(dateNum));*/
					log.info(file.toString()+": result---"+result.get(j).toString());
					List<ErpCustomer> listTemp = dao.getCustomerByCode(result.get(j).get(codeNum));		//根据code获取customer
					
					if(listTemp.size()>0){
						count = 0;
						for (ErpCustomer erpCustomer : listTemp) {
							if(erpCustomer.getCheckTime()==null){
								Date date =formatString(result.get(j).get(dateNum));
								erpCustomer.setCheckTime(date);
								erpCustomer.setUpdateTime(new Date());
								log.info(erpCustomer.getCode()+": name ---"+erpCustomer.getName()+","+erpCustomer.getCheckTime());
								dao.update(erpCustomer);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 读取xls后缀的excel
	 * @param file
	 * @return
	 * @throws Exception
	 * @author tangxing
	 * @date 2016-7-13下午7:17:42
	 */
	public void saveCustomerXls(File file) throws Exception{
		List<Map<String, String>> result = xlsExcel(file);
		log.info(file.toString()+": result---"+result.toString());
		if(result.size()>0){
			String codeNum = "";
			String dateNum = "";
			Map<String, String> map = result.get(0);
				String valueStr = map.toString();
				if(valueStr.indexOf("条形码")!=-1&&valueStr.indexOf("送检日期")!=-1){
					for(int i=0;i<map.keySet().size();++i){
						String s = String.valueOf(i);
						String key = map.get(s);
						if(key.equals("条形码")){
							codeNum = s;
						}
						if(key.equals("送检日期")){
							dateNum = s;
						}
					}
				}
			if(!StringUtils.isEmpty(codeNum)&&!StringUtils.isEmpty(dateNum)){
				int count = 0;
				for(int j = 1; j<result.size(); ++j){
					/*System.out.println("code---"+result.get(j).get(codeNum));	
					System.out.println("date---"+result.get(j).get(dateNum));*/
					log.info(file.toString()+": result---"+result.get(j).toString());
					List<ErpCustomer> listTemp = dao.getCustomerByCode(result.get(j).get(codeNum));		//根据code获取customer
					
					if(listTemp.size()>0){
						count = 0;
						for (ErpCustomer erpCustomer : listTemp) {
							if(erpCustomer.getCheckTime()==null){
								Date date =formatString(result.get(j).get(dateNum));
								erpCustomer.setCheckTime(date);
								erpCustomer.setUpdateTime(new Date());
								dao.update(erpCustomer);
							}
						}
						//list.add(customerTemp);
					}else{
						count++;
					}
					
					if(count>=50){	//连续50次null，跳出循环
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 读取用户的PDF
	 * @param stratTime
	 * @param endTime
	 * @throws Exception
	 * @author tangxing
	 * @date 2016-7-22上午10:11:18
	 */
	public void readPdfCustomer(Date stratTime,Date endTime) throws Exception{
		/*List<ErpCustomer> customerList = dao.getCustomerByCreateTime(stratTime,endTime);
		for (ErpCustomer erpCustomer : customerList) {
			String tempStr = erpCustomer.getPdffilepath();
			int num = tempStr.indexOf("gene/");
			tempStr = tempStr.substring(num+5,tempStr.length());
			
			String path = "Z://"+tempStr;
			
			String code = erpCustomer.getCode();
			if(!StringUtils.isEmpty(path)){
				this.readPdf(path,code);
			}
		}*/
		readPdf("D:/readPdf/171228.pdf","");
	}
	
	@SuppressWarnings("finally")
	public List<Map<String, String>> xlsxExcel(File file) throws Exception{
		InputStream is = null;
		// Workbook wb = null;
		XSSFWorkbook wb = null;
		// id--值集合
		List<Map<String, String>> valueList = new ArrayList<Map<String, String>>();
		try {
			is = new FileInputStream(file);
			// wb = Workbook.getWorkbook(is);
			wb = new XSSFWorkbook(is);

			XSSFSheet st = wb.getSheetAt(0);// 得到工作薄中的第一个工作表
			// 第一行是条码,第二行是姓名,第三行性别,第四行年龄,第五行联系方式,第六行支公司,
			// 第七行检测项目,第八行样本类型,第九行送检医生,第十行送检单位,
			// 第十一行提交者,第十二行送检日期,第十三行收样日期,第十四行状态,第十五行报告
			int bb = st.getLastRowNum();
			for (int i = 0; i <= bb; i++) {
				Map<String, String> valueMap = new HashMap<String, String>();
				XSSFRow row = st.getRow(i);
				int aa = row.getLastCellNum();
				for (int j = 0; j < aa; j++) {
					String cellValue = getValue(row.getCell(j));// 得到字段id
					valueMap.put(String.valueOf(j), cellValue);
				}
				valueList.add(valueMap);// 把第一条记录加入到list中去
			}
		} catch (IOException e) {
			//log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			try {
				if (wb != null) {
					//wb.close();// 关闭工作薄
					wb = null;
				}
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return valueList;
		}
	}
	
	@SuppressWarnings("finally")
	public List<Map<String, String>> xlsExcel(File file) throws Exception{
		Workbook wb = null;
		InputStream is=null;
		Sheet st=null;
		// id--值集合
		List<Map<String, String>> valueList = new ArrayList<Map<String, String>>();
		try {
			is = new FileInputStream(file);
			// wb = Workbook.getWorkbook(is);
			wb = Workbook.getWorkbook(is);

			st = wb.getSheet(0);// 得到工作薄中的第一个工作表
			// 第一行是条码,第二行是姓名,第三行性别,第四行年龄,第五行联系方式,第六行支公司,
			// 第七行检测项目,第八行样本类型,第九行送检医生,第十行送检单位,
			// 第十一行提交者,第十二行送检日期,第十三行收样日期,第十四行状态,第十五行报告
			int bb = st.getRows();//总行数
			int rsCols=st.getColumns();//获取Sheet表中所包含的总列数
			for (int i = 0; i <= bb; i++) {
				Map<String, String> valueMap = new HashMap<String, String>();
				
				for (int j = 0; j < rsCols; j++) {
					Cell coo = st.getCell(j,i);// 得到字段id
					String cellValue=coo.getContents();
					valueMap.put(String.valueOf(j), cellValue);
				}
				valueList.add(valueMap);// 把第一条记录加入到list中去
			}
		} catch (IOException e) {
			//log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			try {
				if (wb != null) {
					wb.close();// 关闭工作薄
					wb = null;
				}
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return valueList;
		}
	}
	
	private String getValue(org.apache.poi.ss.usermodel.Cell cell) {
		String value = "";
		if (cell == null) {
			return value;
		}
		switch (cell.getCellType()) {
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:// boolean类型
			value = String.valueOf(cell.getBooleanCellValue()).trim();
			break;
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC: // 数字或日期类型

			if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否是日期类型
				value = DateUtils.DateToStr(cell.getDateCellValue(),
						DateUtils.DATE_FORMAT); // 把Date转换成本地格式的字符串
			} else {
				short format = cell.getCellStyle().getDataFormat();
				SimpleDateFormat sdf = null;
				if (format == 14 || format == 31 || format == 57
						|| format == 58) {
					// 日期
					sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date date = org.apache.poi.ss.usermodel.DateUtil
							.getJavaDate(cell.getNumericCellValue());
					value = sdf.format(date);
				} else if (format == 20 || format == 32) {
					// 时间
					sdf = new SimpleDateFormat("HH:mm");
					Date date = org.apache.poi.ss.usermodel.DateUtil
							.getJavaDate(cell.getNumericCellValue());
					value = sdf.format(date);
				} else {
					DecimalFormat df = new DecimalFormat("0");
					value = String.valueOf(
							df.format(cell.getNumericCellValue())).trim();
				}
			}
			break;
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:// 字符类型
			value = String.valueOf(cell.getStringCellValue()).trim();
			break;
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA:
			value = cell.getCellFormula();
			break;
		case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK:// 空白单元格
			break;
		default:
			value = String.valueOf(cell.getStringCellValue()).trim();
			break;
		}
		return value;
	}
	
	/**
	 * 读取PDF文件
	 * @param filePath
	 * @return
	 * @author tangxing
	 * @throws Exception 
	 * @date 2016-7-22上午10:16:14
	 */
	public ErpCustomer readPdf(String filePath,String code) throws Exception{
		Map<String,String> resultMap = new HashMap<String,String>();
		ErpCustomer customer = new ErpCustomer();
		String result = null;
		FileInputStream is = null;
		PDDocument document = null;
		String content = null;
		try {
			is = new FileInputStream(filePath);
			log.info("readPDF path : "+ filePath);
			document = PDDocument.load(is);
			int nums = document.getNumberOfPages();
			//从每一页读取内容
			for(int i = 1;i<nums+1;i++){
				PDFTextStripper stripper = new PDFTextStripper();
				stripper.setStartPage(i);
				stripper.setEndPage(i);
				result = stripper.getText(document);
				Pattern p = Pattern.compile("\\s*|\t|\r|\n|\r\n");
				Matcher m = p.matcher(result);
				content = m.replaceAll("");
				content = content.replaceAll(" ", "");
				resultMap = isMatch(content);
				
				if(resultMap!=null&&resultMap.size()>0){
					this.updateErpCustomer(resultMap,code);
					break;
				}
				
			}
		} catch (FileNotFoundException e) {
			log.error("DealWithPdfUtil readPdfContent ERROR ", e);
		} catch (IOException e) {
			log.error("DealWithPdfUtil readPdfContent ERROR ", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("DealWithPdfUtil readPdfContent ERROR ", e);
				}
			}
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					log.error("DealWithPdfUtil readPdfContent ERROR ", e);
				}
			}
		}
		
		if(customer!=null){
			return customer;
		}else{
			return null;
		}
	}
	
	/**
	 * 截取报告日期
	 * @param content
	 * @return
	 * @author tangxing
	 * @date 2016-7-22下午4:08:28
	 */
	public Map<String,String> isMatch(String content){
		HashMap<String,String> formatMap = new HashMap<String, String>();
		log.info("pdf content---"+content);
		if(content.indexOf("报告日期")!=-1){
			int num = content.indexOf("报告日期");
			String timeStr = "";
			if(content.indexOf("-10-")!=-1||content.indexOf("-11-")!=-1||content.indexOf("-12-")!=-1
					||content.indexOf("/10/")!=-1||content.indexOf("/11/")!=-1||content.indexOf("/12/")!=-1){
				int temp = num+4;
				timeStr = content.substring(temp, temp+10);
			}else{
				int temp = num+4;
				timeStr = content.substring(temp, temp+9);
			}
			log.info("reportTime---"+timeStr);
			formatMap.put("reportTime", timeStr);
		}
		return formatMap;
	}
	
	/**
	 * 修改当前code的ErpCustomer
	 * @param resultMap
	 * @param code
	 * @author tangxing
	 * @throws Exception 
	 * @date 2016-7-22下午2:59:05
	 */
	public void updateErpCustomer(Map<String,String> resultMap,String code) throws Exception{
		List<ErpCustomer> listTemp = dao.getCustomerByCode(code);		//根据code获取customer
		log.info(code+" is format map --- "+resultMap.toString());
		String str = "";
		if(resultMap.size()>0){
			str = resultMap.get("reportTime");
		}
		Date date = formatString(str);
		for (ErpCustomer erpCustomer : listTemp) {
			erpCustomer.setCheckTime(date);
		}
		log.info("update list size --- "+listTemp.size());
		if(listTemp.size()>0){
			updateCustomer(listTemp);
		}
	}
	
	/**
	 * String转换Date
	 * @param s
	 * @return
	 * @author tangxing
	 * @date 2016-7-22下午3:02:02
	 */
	public Date formatString(String s){
		if(!StringUtils.isEmpty(s)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			try {
				date = sdf.parse(s);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}
		return null;
	}
	
	/**
	 * 集合修改
	 * @param customers
	 * @throws Exception
	 * @author tangxing
	 * @date 2016-7-22下午3:02:23
	 */
	public void updateCustomer(List<ErpCustomer> customers) throws Exception{
		for (ErpCustomer erpCustomer : customers) {
			dao.update(erpCustomer);
		}
	}
	
}
