package com.glassdoorbi.glassdoorbi.consumer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

@Component
public class CsvParser {

  public void parse(MultipartFile multipartFile) {
    ICsvListReader listReader = null;
    File csvFile = multipartToFile(multipartFile);
    try {
      listReader = new CsvListReader(new FileReader(csvFile.getAbsolutePath()),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final String[] headers = listReader.getHeader(true);
      final CellProcessor[] processors = getProcessors(listReader.length());

      List<Object> customerList;
      while ((customerList = listReader.read(processors)) != null) {
        if (listReader.getLineNumber() != 1) {
          System.out.println(customerList);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private File multipartToFile(MultipartFile multipartFile) {
//    File convertedFile = new File(multipartFile.getOriginalFilename());
//    try {
//      multipartFile.transferTo(convertedFile);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return convertedFile;
    File convFile = new File(
        System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
    try {
      multipartFile.transferTo(convFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return convFile;
  }

  private CellProcessor[] getProcessors(int numberOfColumns) {

    final CellProcessor[] processors = new CellProcessor[numberOfColumns];
    for (int i = 0; i < numberOfColumns; i++) {
      processors[i] = new Optional();
    }
    return processors;
  }
}
