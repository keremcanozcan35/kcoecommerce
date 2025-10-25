package com.ecommerce.kco_ecom.Service;


import com.ecommerce.kco_ecom.Model.Category;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CategoryService {
    List<Category> getAllCategories();
    void createCategory(Category category);

    String deleteCategory(Long categoryId);

    void updateCategory(Long categoryId, Category category);
}
