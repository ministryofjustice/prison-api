package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@Builder
public class CodeDescription {

	private final String code;
	private String desc;

   @JsonCreator
   public CodeDescription(@JsonProperty("code") String code, @JsonProperty("desc") String desc) {
      super();
      this.code = code;
      this.desc = desc;
   }

   public CodeDescription(String code) {
      super();
      this.code = code;
   }

}
