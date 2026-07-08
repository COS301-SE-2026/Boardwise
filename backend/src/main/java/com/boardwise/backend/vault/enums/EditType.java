package com.boardwise.backend.vault.enums;

public enum EditType{
    INSERT("Insert"),
    UPDATE("Update"),
    DELETE("Delete");

    private final String type;
    
    EditType(String type){
        this.type = type;
    }

    @Override
    public String toString(){
        return this.type;
    }
}