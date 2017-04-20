package net.syscon.elite.persistence.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.Referencecode;

@Repository
public class ReferenceCodeRepositoryImpl extends RepositoryBase implements ReferenceCodeRepository {
	
	private final Map<String, FieldMapper> referenceCodeMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("DESCRIPTION", 		new FieldMapper("description"))
			.put("CODE", 					new FieldMapper("code"))
			.build();

	@Override
	public List<Referencecode> getCnotetypesByCaseLoad(String caseLoad) {
		final String sql = getQuery("FIND_CNOTE_TYPES_BY_CASE_LOAD");
		final RowMapper<Referencecode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, Referencecode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseLoad", caseLoad), referenceCodeRowMapper);
	}

	@Override
	public List<Referencecode> getCnoteSubtypesByCaseNoteType(String caseNotetype) {
		final String sql = getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE");
		final RowMapper<Referencecode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, Referencecode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseNoteType", caseNotetype), referenceCodeRowMapper);
	}

	@Override
	public List<Referencecode> getReferencecodesForDomain(String domain) {
		final String sql = getQuery("FIND_REF_CODES");
		final RowMapper<Referencecode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, Referencecode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain), referenceCodeRowMapper);
	}

	@Override
	public Referencecode getReferencecodeDescriptionForCode(String domain, String code) {
		final String sql = getQuery("FIND_REF_CODE_DESC");
		final RowMapper<Referencecode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, Referencecode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", code), referenceCodeRowMapper);
	}

	

}
