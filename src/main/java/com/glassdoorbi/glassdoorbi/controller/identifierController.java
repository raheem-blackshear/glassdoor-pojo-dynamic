package com.glassdoorbi.glassdoorbi.controller;

import com.glassdoorbi.glassdoorbi.model.controller.Identifier;
import com.glassdoorbi.glassdoorbi.mongo.MongoDocumentUtils;
import com.glassdoorbi.glassdoorbi.mongo.MongoUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class identifierController {

  MongoUtils mongoUtils;

  public identifierController(@Autowired MongoUtils mongoUtils) {
    this.mongoUtils = mongoUtils;
  }

  @RequestMapping(value = "/identifiers", method = RequestMethod.GET)
  public ResponseEntity<List<String>> getIdentifier(
      @RequestParam(value = "identifier", required = true) String identifier) {
    return new ResponseEntity<>(
        MongoDocumentUtils.parseIdentifiers(mongoUtils.getdocuments(identifier)), HttpStatus.OK);
  }

}
