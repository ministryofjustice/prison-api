package uk.gov.justice.hmpps.prison.repository.mapping

import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.beans.TypeConverter
import org.springframework.core.MethodParameter
import org.springframework.core.convert.TypeDescriptor
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.support.JdbcUtils
import org.springframework.util.StringUtils
import java.lang.reflect.Constructor
import java.sql.ResultSet

/**
 * Copy of org.springframework.jdbc.core.DataClassRowMapper and changed so as not to use rs.findColumn(name) but
 * instead convert the result set names into indexes so can get the column value directly.  This is because findColumn
 * is very slow in Oracle as it uses a number of regex to possibly quote the name of the column before looking it up.
 */
class DataClassByColumnRowMapper<T>(mappedClass: Class<T>) : BeanPropertyRowMapper<T>(mappedClass) {

  private val mappedConstructor: Constructor<T>
  private val constructorParametersAsColumnNames: List<String>
  private val constructorParameterTypes: List<TypeDescriptor>

  // fieldMap is used for building up sorting, mapping the java parameter name to the database column name
  // for example so can then pass "lastName,startTime" and will then sort by those fields
  val fieldMap: Map<String, FieldMapper>

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  init {
    mappedConstructor = BeanUtils.getResolvableConstructor(mappedClass)
    val paramCount = mappedConstructor.parameterCount
    if (paramCount > 0) {
      val parameterNames = BeanUtils.getParameterNames(mappedConstructor)
      constructorParametersAsColumnNames = parameterNames.map {
        suppressProperty(it)
        underscoreName(it)
      }
      constructorParameterTypes = (0 until paramCount).map { index ->
        TypeDescriptor(MethodParameter(mappedConstructor, index))
      }
      fieldMap = parameterNames.associate {
        underscoreName(it).uppercase() to FieldMapper(it)
      }
    } else {
      log.warn("No args constructor for {}, expecting constructor with parameters instead", mappedClass)
      constructorParametersAsColumnNames = emptyList()
      constructorParameterTypes = emptyList()
      fieldMap = emptyMap()
    }
  }

  override fun constructMappedInstance(rs: ResultSet, tc: TypeConverter): T {
    val args: Array<Any?>
    if (constructorParametersAsColumnNames.isNotEmpty() && constructorParameterTypes.isNotEmpty()) {
      val rsmd = rs.metaData
      // construct map of column names to column indexes
      val columnMap = (1..rsmd.columnCount).associateBy { index ->
        val column = JdbcUtils.lookupColumnName(rsmd, index)
        lowerCaseName(StringUtils.delete(column, " "))
      }

      args = arrayOfNulls(constructorParametersAsColumnNames.size)
      for (i in args.indices) {
        val td = constructorParameterTypes[i]
        // look up to see if we have an index in our map for the parameter
        columnMap[constructorParametersAsColumnNames[i]]?.run {
          val value = getColumnValue(rs, this, td.type)
          args[i] = tc.convertIfNecessary(value, td.type, td)
        }
      }
    } else {
      args = arrayOfNulls(0)
    }
    return BeanUtils.instantiateClass(mappedConstructor, *args)
  }

  override fun getColumnValue(rs: ResultSet, index: Int, paramType: Class<*>): Any? {
    return when (paramType) {
      Boolean::class.javaObjectType -> "Y" == rs.getObject(index) || rs.getBoolean(index)
      else -> super.getColumnValue(rs, index, paramType)
    }
  }
}
