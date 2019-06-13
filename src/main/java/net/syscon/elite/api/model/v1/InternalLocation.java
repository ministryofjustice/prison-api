package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"description", "levels"})
public class InternalLocation {

   final static int MAX_LEVELS = 4;

   @JsonProperty("description")
   private String description;
   @JsonProperty("levels")
   private List<TypeValue> levels;

   public InternalLocation(String description, String levelStr) {
      this.description = description;
      if (levelStr != null) {
         this.levels = new ArrayList<>();
         for (String level :levelStr.split("\\|", MAX_LEVELS)) {
            String[] tv = level.split(",");
            if (tv.length == 2) {
               this.levels.add(new TypeValue(tv[0],tv[1]));
            } else {
               throw new RuntimeException("Badly formed levelStr:" + levelStr);
            }
         }
      }
   }



}
