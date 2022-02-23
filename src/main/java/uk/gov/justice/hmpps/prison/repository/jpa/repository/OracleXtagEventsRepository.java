package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.STRUCT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;
import uk.gov.justice.hmpps.prison.service.xtag.XtagEventNonJpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class OracleXtagEventsRepository implements XtagEventsRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OracleXtagEventsRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Optional<XtagEventNonJpa> xtagEventOf(final ResultSet rs) {

        XtagEventNonJpa xtagEventNonJpaWithoutOracleTypes = null;

        try {
            xtagEventNonJpaWithoutOracleTypes = XtagEventNonJpa.builder()
                    .chainNo(rs.getLong("CHAIN_NO"))
                    .corrID(rs.getString("CORRID"))
                    .cscn(rs.getLong("CSCN"))
                    .delay(rs.getTimestamp("DELAY"))
                    .deqTID(rs.getString("DEQ_TID"))
                    .deqTime(rs.getTimestamp("DEQ_TIME"))
                    .dequeueMsgId(rs.getString("DEQUEUE_MSGID"))
                    .deqUID(rs.getString("DEQ_UID"))
                    .dscn(rs.getLong("DSCN"))
                    .enqTID(rs.getString("ENQ_TID"))
                    .enqTime(rs.getTimestamp("ENQ_TIME"))
                    .enqUID(rs.getString("ENQ_UID"))
                    .exceptionQSchema(rs.getString("EXCEPTION_QSCHEMA"))
                    .exceptionQueue(rs.getString("EXCEPTION_QUEUE"))
                    .expiration(rs.getLong("EXPIRATION"))
                    .localOrderNo(rs.getLong("LOCAL_ORDER_NO"))
                    .msgId(rs.getString("MSGID"))
                    .priority(rs.getLong("PRIORITY"))
                    .qName(rs.getString("Q_NAME"))
                    .recipientKey(rs.getLong("RECIPIENT_KEY"))
                    .retryCount(rs.getLong("RETRY_COUNT"))
                    .senderAddress(rs.getString("SENDER_ADDRESS"))
                    .senderName(rs.getString("SENDER_NAME"))
                    .senderProtocol(rs.getLong("SENDER_PROTOCOL"))
                    .state(rs.getLong("STATE"))
                    .stepNo(rs.getLong("STEP_NO"))
                    .timeManagerInfo(rs.getTimestamp("TIME_MANAGER_INFO"))
                    .build();


            return Optional.of(xtagEventNonJpaWithoutOracleTypes.toBuilder()
                    .userData((STRUCT) rs.getObject("USER_DATA"))
                    .userProp((STRUCT) rs.getObject("USER_PROP"))
                    .build());

        } catch (final SQLException e) {
            log.error(e.getMessage());
        } catch (final Throwable t) {
            log.error("Caught throwable building XtagEventNonJpa {}. Will return empty and continue! : {} {}",xtagEventNonJpaWithoutOracleTypes, t.getMessage(), t.getStackTrace());
        }
        return Optional.empty();
    }


    public List<XtagEventNonJpa> findAll(final OffenderEventsFilter f) {
        final var results = jdbcTemplate.query("select * from XTAG.XTAG_LISTENER_TAB where enq_time >= ? and enq_time <= ?", (rs, rowNum) -> xtagEventOf(rs), Timestamp.valueOf(f.getFrom()), Timestamp.valueOf(f.getTo()));
        return results.stream().filter(Optional::isPresent).map(Optional::get).toList();
    }


}
