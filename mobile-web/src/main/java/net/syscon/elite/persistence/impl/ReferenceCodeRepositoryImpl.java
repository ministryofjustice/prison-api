package net.syscon.elite.persistence.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.ReferenceCode;

@Repository
public class ReferenceCodeRepositoryImpl extends RepositoryBase implements ReferenceCodeRepository {
	
	private final Map<String, FieldMapper> referenceCodeMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("DESCRIPTION", 		new FieldMapper("description"))
			.put("CODE", 				new FieldMapper("code"))
			.put("DOMAIN", 				new FieldMapper("domain"))
			.put("PARENT_DOMAIN", 		new FieldMapper("parentDomainId"))
			.put("PARENT_CODE", 		new FieldMapper("parentCode"))
			.put("ACTIVE_FLAG", 		new FieldMapper("activeFlag", value ->{ return value.equals("Y")?true:false;}))
			.build();

	@Override
	public List<ReferenceCode> getCnotetypesByCaseLoad(final String caseLoad) {
		final String sql = getQuery("FIND_CNOTE_TYPES_BY_CASE_LOAD");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseLoad", caseLoad), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(final String caseNotetype) {
		final String sql = getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseNoteType", caseNotetype), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getReferenceCodesForDomain(final String domain) {
		final String sql = getQuery("FIND_REF_CODES");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getReferenceCodeDescriptionForCode(final String domain, final String code) {
		final String sql = getQuery("FIND_REF_CODE_DESC");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", code), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getAlertTypes(int offset, int limit) {
		final String sql = getQuery("FIND_REF_CODES");
		String domain = "ALERT";
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getAlertTypesByAlertType(String alertType) {
		final String sql = getQuery("FIND_REF_CODE_DESC");
		String domain = "ALERT";
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", alertType), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, int offset, int limit) {
		final String sql = getQuery("FIND_ALERT_REF_CODES");
		String domain = "ALERT_CODE";
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain, "parentCode", alertType), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode) {
		final String sql = getQuery("FIND_ALERT_REF_CODE_DESC");
		String domain = "ALERT_CODE";
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "parentCode", alertType, "code", alertCode), referenceCodeRowMapper);
	}

	

}
