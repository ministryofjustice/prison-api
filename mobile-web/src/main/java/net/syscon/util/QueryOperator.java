package net.syscon.util;
/**
 * 
 * @author om.pandey
 *
 */
public enum QueryOperator {
	
	CONNECTOR_AND("and","and"),
	CONNECTOR_OR("or", "or"),
	EQUAL("eq","="),
	NOTEQUAL("neq","!="),
	GREATER("gt", ">"),
	GREATER_EQ("gteq",">="),
	LESSER("lt", "<"),
	LESSER_EQ("lteq","<="),
	LIKE("like","like"),
	IN("in","IN");
	
	private String jsonOperator;
	private String sqlOperator;
	
	private QueryOperator(String jsonOperator, String sqlOperator) {
		this.jsonOperator = jsonOperator;
		this.sqlOperator = sqlOperator;
	}
	
	public String getJsonOperator() {
		return jsonOperator;
	}

	public String getSqlOperator() {
		return sqlOperator;
	}

}
