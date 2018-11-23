package net.syscon.util;
/**
 * 
 * @author om.pandey
 *
 */
public enum QueryOperator {
	
	CONNECTOR_AND("and","AND"),
	CONNECTOR_OR("or", "OR"),
	EQUAL("eq","="),
	NOTEQUAL("neq","!="),
	IS("is","IS"),
	GREATER("gt", ">"),
	GREATER_EQ("gteq",">="),
	LESSER("lt", "<"),
	LESSER_EQ("lteq","<="),
	LIKE("like","like"),
	IN("in","IN");
	
	private String jsonOperator;
	private String sqlOperator;
	
	QueryOperator(String jsonOperator, String sqlOperator) {
		this.jsonOperator = jsonOperator;
		this.sqlOperator = sqlOperator;
	}
	
	public String getJsonOperator() {
		return jsonOperator;
	}

	public String getSqlOperator() {
		return sqlOperator.toUpperCase();
	}

}
