package uk.gov.justice.hmpps.prison.service.xtag;

import lombok.Builder;
import lombok.Value;
import oracle.sql.STRUCT;

import java.sql.Timestamp;

@Value
@Builder(toBuilder = true)
public class XtagEventNonJpa {
    private String msgId;
    private String qName;
    private String corrID;
    private Long priority;
    private Long state;
    private Timestamp delay;
    private Long expiration;
    private Timestamp timeManagerInfo;
    private Long localOrderNo;
    private Long chainNo;
    private Long cscn;
    private Long dscn;
    private Timestamp enqTime;
    private String enqUID;
    private String enqTID;
    private Timestamp deqTime;
    private String deqUID;
    private String deqTID;
    private Long retryCount;
    private String exceptionQSchema;
    private String exceptionQueue;
    private Long stepNo;
    private Long recipientKey;
    private String dequeueMsgId;
    private String senderName;
    private String senderAddress;
    private Long senderProtocol;
    private STRUCT userData;
    private STRUCT userProp;
}
