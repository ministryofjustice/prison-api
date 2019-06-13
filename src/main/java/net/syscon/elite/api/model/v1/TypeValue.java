package net.syscon.elite.api.model.v1;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TypeValue {
   private String type;
   private String value;
}
