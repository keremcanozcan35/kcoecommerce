package com.ecommerce.kco_ecom.Service;


import com.ecommerce.kco_ecom.Model.Category;
import com.ecommerce.kco_ecom.Payload.CategoryDTO;
import com.ecommerce.kco_ecom.Payload.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CategoryService {

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO deleteCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
}
