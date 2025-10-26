package com.ecommerce.kco_ecom.ServiceImplementation;

import com.ecommerce.kco_ecom.Exceptions.ResourceNotFoundException;
import com.ecommerce.kco_ecom.Model.Category;
import com.ecommerce.kco_ecom.Payload.CategoryDTO;
import com.ecommerce.kco_ecom.Payload.CategoryResponse;
import com.ecommerce.kco_ecom.Repository.CategoryRepository;
import com.ecommerce.kco_ecom.Service.CategoryService;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService
{

    private List<Category> categories = new ArrayList<>();
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Category> pageCategories = categoryRepository.findAll(pageable);

         List<Category> categories = pageCategories.getContent();

         List<CategoryDTO> categoryDTOS = categories.stream()
                 .map(category -> modelMapper.map(category, CategoryDTO.class))
                 .toList();


         CategoryResponse categoryResponse = new CategoryResponse();
         categoryResponse.setContent(categoryDTOS);

         categoryResponse.setPageNumber(pageCategories.getNumber());
            categoryResponse.setPageSize(pageCategories.getSize());
            categoryResponse.setTotalElements(pageCategories.getTotalElements());
            categoryResponse.setTotalPages(pageCategories.getTotalPages());
            categoryResponse.setLastPage(pageCategories.isLast());


         return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO,Category.class);
        Category savedCategory = categoryRepository.save(category);
        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);
        return savedCategoryDTO;
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "Category Id", categoryId));


        categoryRepository.delete(category);
        return modelMapper.map(category, CategoryDTO.class);
    }



    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {

        Category savedCategoryOptional = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "Category Id", categoryId));

        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setCategoryId(categoryId);
        savedCategoryOptional = categoryRepository.save(category);

        return modelMapper.map(savedCategoryOptional, CategoryDTO.class);
    }


}
