package com.glassdoorbi.glassdoorbi.model.mongo;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
public class Record {

  @SerializedName("_id")
  @Id
  private String identifer;

  private List<Object> records;

}
