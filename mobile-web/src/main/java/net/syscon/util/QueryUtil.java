package net.syscon.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.syscon.elite.persistence.mapping.FieldMapper;
/**
 * 
 * @author om.pandey
 *
 */
public class QueryUtil {
	public static final Logger LOG = LoggerFactory.getLogger(QueryUtil.class);
	
	private QueryUtil() {
	}
	
	public static String getSqlFieldName(Map<String,FieldMapper> fieldMap, String jsonField) {
		String sqlFieldName = fieldMap.entrySet()
										.stream()
										.filter(entry -> {return entry.getValue().getName().equals(jsonField);})
										.map(Map.Entry::getKey)
										.findAny()
										.orElse(null);
		return sqlFieldName;
	}
	
	public static String getSqlOperator(String jsonOperator) {
		String  sqlOperator = Arrays.stream(QueryOperator.values())
									.filter(queryOperator-> queryOperator.getJsonOperator().equalsIgnoreCase(jsonOperator))
									.map(queryOperator-> queryOperator.getSqlOperator())
									.findAny().orElse(null);
		//filteredQueryOperator.
		//QueryOperator.valueOf(jsonOperator).getSqlOperator();
		
		return sqlOperator;
	}
	
	public static List<String> checkPrecdencyAndSplit(String queryInput, List<String> queryBreak) {
		if(queryInput.contains("(") && queryInput.contains(")")) {
			String beforePrecedence = queryInput.substring(0,queryInput.indexOf('(')); //It will never have precedence.
			queryBreak.add(beforePrecedence);
			String precedenceString = queryInput.substring(queryInput.indexOf('(') ,queryInput.indexOf(')')+1);// It has and identified.
			queryBreak.add(precedenceString);
			String afterPrecedence = queryInput.substring(queryInput.indexOf(')')+1, queryInput.length());// Check it again.
			checkPrecdencyAndSplit(afterPrecedence, queryBreak);
		} else {
			queryBreak.add(queryInput);
		}
		return queryBreak;
	}
	
	
	public static String prepareQuery(String queryItem, boolean isPrecedence, Map<String, FieldMapper> fieldMap) {
		StringBuilder stringBuilder = new StringBuilder();
		String[] fields =queryItem.split(",");
		Arrays.stream(fields).filter(fieldItem ->fieldItem.length()>0).forEach(fieldItem ->{
			if(fieldItem.equals(fields[0])) {
				fieldItem="and:"+fieldItem;
			}
			String[] operatorFieldValue = fieldItem.split(":");
			int size = operatorFieldValue.length;
			
			String connector = size==3?"":operatorFieldValue[0];// Get Real SQL Operator From Enum
			String fieldName = size==3?operatorFieldValue[0]:operatorFieldValue[1];// Get Field Name form Field Mapper
			String operator = size==3?operatorFieldValue[1]:operatorFieldValue[2];// Get Real SQL Operator From Enum
			String value = size==3?operatorFieldValue[2]:operatorFieldValue[3];// Supply it as it is.
			String format = size==5?operatorFieldValue[4]:"";
			if(value.contains("|")) {
				value = "("+value.replaceAll("\\|", ",")+")";
			}
			stringBuilder.append(" ")
			.append(!"".equals(connector)?getSqlOperator(connector):"")
			.append(" ")
			.append(isPrecedence && fieldItem.equals("and:"+fields[0])?"(":"")
			.append(" ")
			.append(getSqlFieldName(fieldMap, fieldName))
			.append(" ")
			.append(getSqlOperator(operator))
			.append(" ")
			.append(!"".equals(format)?"to_date("+value+","+format+")":value)
			.append(" ")
			.append(isPrecedence && fieldItem.equals(fields[fields.length-1])?")":"")
			.append(" ");
			
		});
		return stringBuilder.toString();
	}
	
	public static void main(String...strings) {
		System.out.println(getSqlOperator("eq"));
	}
}
