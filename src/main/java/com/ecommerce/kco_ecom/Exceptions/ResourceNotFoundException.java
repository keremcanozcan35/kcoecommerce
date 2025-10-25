package com.ecommerce.kco_ecom.Exceptions;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ResourceNotFoundException  extends  RuntimeException{

    String resourceName;
    String field;
    String fieldName;
    Long fieldId;

    public ResourceNotFoundException(String category, String categoryId, Long categoryId1) {
        super(String.format("%s not found with %s : '%s'", category, categoryId, categoryId1));
        this.resourceName = category;
        this.field = categoryId;
        this.fieldId = categoryId1;
    }
}
