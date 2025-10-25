package com.ecommerce.kco_ecom.ServiceImplementation;

import com.ecommerce.kco_ecom.Exceptions.ResourceNotFoundException;
import com.ecommerce.kco_ecom.Model.Category;
import com.ecommerce.kco_ecom.Repository.CategoryRepository;
import com.ecommerce.kco_ecom.Service.CategoryService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService
{

    private List<Category> categories = new ArrayList<>();
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category categoryList = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "Category Id", categoryId));
        categoryRepository.delete(categoryList);
        return "Category with ID " + categoryId + " deleted successfully.";
    }



    @Override
    public void updateCategory(Long categoryId, Category category) {
        Optional<Category> savedCategoryOptional = categoryRepository.findById(categoryId);
        savedCategoryOptional.orElseThrow( () -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Category not found") );
        category.setCategoryId(categoryId);

        categoryRepository.save(category);
    }


}
