package com.glassdoorbi.glassdoorbi.controller;

import com.glassdoorbi.glassdoorbi.mongo.MongoDocumentUtils;
import com.glassdoorbi.glassdoorbi.mongo.MongoUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecordController {

  private MongoUtils mongoUtils;

  public RecordController(@Autowired MongoUtils mongoUtils) {
    this.mongoUtils = mongoUtils;
  }

  @RequestMapping("/records")
  public ResponseEntity<List<String>> getRecords(@RequestParam(value = "collection", required = true) String collection,@RequestParam(value = "identifier", required = true) String identifier) {
    return new ResponseEntity<List<String>>(MongoDocumentUtils.getRecords(mongoUtils.getdocuments(collection), identifier),
        HttpStatus.OK);
  }

//  @RequestMapping(value = "/records", method = RequestMethod.POST)
//  public ResponseEntity addRecords() {
//
//  }
}
