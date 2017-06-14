package net.syscon.util;

import net.syscon.elite.persistence.mapping.FieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author om.pandey
 *
 */
public class QueryUtil {
	public static final Logger LOG = LoggerFactory.getLogger(QueryUtil.class);
	
	private QueryUtil() {
	}
	
	public static String getSqlFieldName(final Map<String,FieldMapper> fieldMap, final String jsonField) {
		final String sqlFieldName = fieldMap.entrySet()
										.stream()
										.filter(entry -> {return entry.getValue().getName().equals(jsonField);})
										.map(Map.Entry::getKey)
										.findAny()
										.orElse(null);
		return sqlFieldName;
	}
	
	public static String getSqlOperator(final String jsonOperator) {
		final String  sqlOperator = Arrays.stream(QueryOperator.values())
									.filter(queryOperator-> queryOperator.getJsonOperator().equalsIgnoreCase(jsonOperator))
									.map(queryOperator-> queryOperator.getSqlOperator())
									.findAny().orElse(null);
		return sqlOperator;
	}
	
	public static List<String> checkPrecdencyAndSplit(final String queryInput, final List<String> queryBreak) {
		if(queryInput.contains("(") && queryInput.contains(")")) {
			final String beforePrecedence = queryInput.substring(0,queryInput.indexOf('(')); //It will never have precedence.
			queryBreak.add(beforePrecedence);
			final String precedenceString = queryInput.substring(queryInput.indexOf('(') ,queryInput.indexOf(')')+1);// It has and identified.
			queryBreak.add(precedenceString);
			final String afterPrecedence = queryInput.substring(queryInput.indexOf(')')+1, queryInput.length());// Check it again.
			checkPrecdencyAndSplit(afterPrecedence, queryBreak);
		} else {
			queryBreak.add(queryInput);
		}
		return queryBreak;
	}


	public static String prepareQuery(final String queryItem, final boolean isPrecedence, final Map<String, FieldMapper> fieldMap) {
		final StringBuilder stringBuilder = new StringBuilder();
		final String[] fields = queryItem.split(",");
		Arrays.stream(fields).filter(fieldItem ->fieldItem.length()>0).forEach(fieldItem ->{
			if(fieldItem.equals(fields[0])) {
				fieldItem="and:"+fieldItem;
			}
			final String[] operatorFieldValue = fieldItem.split(":");
			final int size = operatorFieldValue.length;
			
			final String connector = size == 3 ? "" : operatorFieldValue[0]; // Get Real SQL Operator From Enum
			final String fieldName = size == 3 ? operatorFieldValue[0] : operatorFieldValue[1];// Get Field Name form Field Mapper
			final String operator = size == 3 ? operatorFieldValue[1] : operatorFieldValue[2];// Get Real SQL Operator From Enum
			String value = size == 3 ? operatorFieldValue[2] : operatorFieldValue[3];// Supply it as it is.
			final String format = size == 5 ? operatorFieldValue[4] : "";
			
			if (value.contains("|")) {
				value = "(" + value.replaceAll("\\|", ",") + ")";
			}

			String sqlFieldName = getSqlFieldName(fieldMap, fieldName);
			FieldMapper fieldMapper = fieldMap.get(sqlFieldName);
			String encodedValue = fieldMapper.getEncodedValue(value);

			stringBuilder.append(" ")
				.append(!"".equals(connector)? getSqlOperator(connector): "")
				.append(" ")
				.append(isPrecedence && fieldItem.equals("and:"+fields[0])? "(": "")
				.append(" ")
				.append(sqlFieldName)
				.append(" ")
				.append(getSqlOperator(operator))
				.append(" ")
				.append(!"".equals(format)? "TO_DATE("+ encodedValue + ", " + format + ")": encodedValue)
				.append(" ")
				.append(isPrecedence && fieldItem.equals(fields[fields.length-1])? ")": "")
				.append(" ");

		});
		String result = stringBuilder.toString().trim();
		if (result.startsWith("AND")) {
			result = result.substring(3);
		} else if (result.startsWith("OR")) {
			result = result.substring(2);
		}
		return result;
	}
}
