package com.glassdoorbi.glassdoorbi.model.controller;

import lombok.Getter;

@Getter
public class Identifier {

  private String collection;
  private String name;

  @Override
  public String toString() {
    return "Collection: " + collection + "Name: " + name;
  }
}
