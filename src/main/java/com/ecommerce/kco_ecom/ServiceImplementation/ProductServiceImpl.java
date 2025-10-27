package com.ecommerce.kco_ecom.ServiceImplementation;

import com.ecommerce.kco_ecom.Exceptions.APIException;
import com.ecommerce.kco_ecom.Exceptions.FileUploadException;
import com.ecommerce.kco_ecom.Exceptions.ResourceNotFoundException;
import com.ecommerce.kco_ecom.Model.Category;
import com.ecommerce.kco_ecom.Model.Product;
import com.ecommerce.kco_ecom.Payload.ProductDTO;
import com.ecommerce.kco_ecom.Payload.ProductResponse;
import com.ecommerce.kco_ecom.Repository.CategoryRepository;
import com.ecommerce.kco_ecom.Repository.ProductRepository;
import com.ecommerce.kco_ecom.Service.FileService;
import com.ecommerce.kco_ecom.Service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Value("${project.image}")
    private String imagePath;

    private List<Product> productList = new ArrayList<>();

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Validate page number
        if (pageNumber < 0) {
            throw new APIException("Page number cannot be negative. Please provide a valid page number.");
        }

        // Validate page size
        if (pageSize <= 0) {
            throw new APIException("Page size must be greater than zero.");
        }

        if (pageSize > 100) {
            throw new APIException("Page size cannot exceed 100 items per page.");
        }

        // Validate sort order
        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            throw new APIException("Sort order must be either 'asc' or 'desc'.");
        }

        // Validate sortBy field (basic validation)
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new APIException("Sort field cannot be null or empty.");
        }

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
               Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

       Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
       Page<Product> pageProducts = productRepository.findAll(pageable);

       List<Product> products = pageProducts.getContent();

       // Check if products list is empty
       if (products.isEmpty()) {
           throw new APIException("No products found.");
       }

       List<ProductDTO> productDTOS = products.stream()
               .map(product -> modelMapper.map(product, ProductDTO.class))
               .toList();

        ProductResponse productResponse = new ProductResponse();
         productResponse.setContent(productDTOS);

            productResponse.setPageNumber(pageProducts.getNumber());
            productResponse.setPageSize(pageProducts.getSize());
            productResponse.setTotalElements(pageProducts.getTotalElements());
            productResponse.setTotalPages(pageProducts.getTotalPages());
            productResponse.setLastPage(pageProducts.isLast());
         return productResponse;
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        // Validate productDTO
        if (productDTO == null) {
            throw new APIException("Product data cannot be null.");
        }

        // Validate product name
        if (productDTO.getProductName() == null || productDTO.getProductName().trim().isEmpty()) {
            throw new APIException("Product name is required and cannot be empty.");
        }

        if (productDTO.getProductName().length() < 3) {
            throw new APIException("Product name must be at least 3 characters long.");
        }

        if (productDTO.getProductName().length() > 100) {
            throw new APIException("Product name cannot exceed 100 characters.");
        }

        // Validate price
        if (productDTO.getPrice() <= 0) {
            throw new APIException("Product price must be greater than zero.");
        }

        if (productDTO.getPrice() > 1000000) {
            throw new APIException("Product price cannot exceed 1,000,000.");
        }

        // Validate quantity
        if (productDTO.getQuantity() == null || productDTO.getQuantity() < 0) {
            throw new APIException("Product quantity must be zero or greater.");
        }

        // Validate discount
        if (productDTO.getDiscount() < 0 || productDTO.getDiscount() > 100) {
            throw new APIException("Discount must be between 0 and 100 percent.");
        }

        // Validate categoryId
        if (categoryId == null || categoryId <= 0) {
            throw new APIException("Invalid category ID.");
        }

        // Find the category by ID
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Map DTO to entity
        Product product = modelMapper.map(productDTO, Product.class);

        // Set the category
        product.setCategory(category);

        // Ensure productId is null so JPA will auto-generate it
        product.setProductId(null);

        // Calculate special price based on discount
        double discount = productDTO.getDiscount();

        double specialPrice = product.getPrice() - (product.getPrice() * discount / 100.0);
        product.setSpecialPrice(specialPrice);

        // Save the product
        Product savedProduct = productRepository.save(product);

        // Map back to DTO and return
        ProductDTO savedProductDTO = modelMapper.map(savedProduct, ProductDTO.class);

        return savedProductDTO;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Validate categoryId
        if (categoryId == null || categoryId <= 0) {
            throw new APIException("Invalid category ID. Category ID must be a positive number.");
        }

        // Validate page number
        if (pageNumber < 0) {
            throw new APIException("Page number cannot be negative. Please provide a valid page number.");
        }

        // Validate page size
        if (pageSize <= 0) {
            throw new APIException("Page size must be greater than zero.");
        }

        if (pageSize > 100) {
            throw new APIException("Page size cannot exceed 100 items per page.");
        }

        // Validate sort order
        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            throw new APIException("Sort order must be either 'asc' or 'desc'.");
        }

        // Validate sortBy field
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new APIException("Sort field cannot be null or empty.");
        }

        // Find the category by ID
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Create sort and pageable objects
        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Fetch paginated products
        Page<Product> pageProducts = productRepository.findByCategory(category, pageable);

        List<Product> products = pageProducts.getContent();

        // Check if products list is empty
        if (products.isEmpty()) {
            throw new APIException("No products found in category: " + category.getCategoryName());
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Validate keyword
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new APIException("Search keyword cannot be null or empty.");
        }

        if (keyword.trim().length() < 2) {
            throw new APIException("Search keyword must be at least 2 characters long.");
        }

        if (keyword.length() > 100) {
            throw new APIException("Search keyword cannot exceed 100 characters.");
        }

        // Validate page number
        if (pageNumber < 0) {
            throw new APIException("Page number cannot be negative. Please provide a valid page number.");
        }

        // Validate page size
        if (pageSize <= 0) {
            throw new APIException("Page size must be greater than zero.");
        }

        if (pageSize > 100) {
            throw new APIException("Page size cannot exceed 100 items per page.");
        }

        // Validate sort order
        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            throw new APIException("Sort order must be either 'asc' or 'desc'.");
        }

        // Validate sortBy field
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new APIException("Sort field cannot be null or empty.");
        }

        // Create sort and pageable objects
        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Fetch paginated products
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword.trim() + '%', pageable);

        List<Product> products = pageProducts.getContent();

        // Check if products list is empty
        if (products.isEmpty()) {
            throw new APIException("No products found matching keyword: " + keyword);
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateCategory(ProductDTO productDTO, Long productId) {
        // Validate productId
        if (productId == null || productId <= 0) {
            throw new APIException("Invalid product ID. Product ID must be a positive number.");
        }

        // Validate productDTO
        if (productDTO == null) {
            throw new APIException("Product data cannot be null.");
        }

        // Validate product name
        if (productDTO.getProductName() != null) {
            if (productDTO.getProductName().trim().isEmpty()) {
                throw new APIException("Product name cannot be empty.");
            }

            if (productDTO.getProductName().length() < 3) {
                throw new APIException("Product name must be at least 3 characters long.");
            }

            if (productDTO.getProductName().length() > 100) {
                throw new APIException("Product name cannot exceed 100 characters.");
            }
        }

        // Validate price if provided
        if (productDTO.getPrice() <= 0) {
            throw new APIException("Product price must be greater than zero.");
        }

        if (productDTO.getPrice() > 1000000) {
            throw new APIException("Product price cannot exceed 1,000,000.");
        }

        // Validate quantity if provided
        if (productDTO.getQuantity() != null && productDTO.getQuantity() < 0) {
            throw new APIException("Product quantity must be zero or greater.");
        }

        // Validate discount
        if (productDTO.getDiscount() < 0 || productDTO.getDiscount() > 100) {
            throw new APIException("Discount must be between 0 and 100 percent.");
        }

        // Check if product exists
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product Id", productId));

        // Map DTO to entity
        Product product = modelMapper.map(productDTO, Product.class);
        product.setProductId(productId);

        // Recalculate special price
        double discount = productDTO.getDiscount();
        double specialPrice = product.getPrice() - (product.getPrice() * discount / 100.0);
        product.setSpecialPrice(specialPrice);

        Product updatedProduct = productRepository.save(product);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        // Validate productId
        if (productId == null || productId <= 0) {
            throw new APIException("Invalid product ID. Product ID must be a positive number.");
        }

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product Id", productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(MultipartFile image, Long productId) throws IOException {
        // Validate productId
        if (productId == null || productId <= 0) {
            throw new APIException("Invalid product ID. Product ID must be a positive number.");
        }

        // Validate image file
        if (image == null || image.isEmpty()) {
            throw new FileUploadException("Image file cannot be null or empty.");
        }

        // Validate file size (10MB limit)
        long maxFileSize = 10 * 1024 * 1024; // 10MB in bytes
        if (image.getSize() > maxFileSize) {
            throw new FileUploadException("Image file size cannot exceed 10MB.");
        }

        // Validate file type
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("Only image files are allowed (jpg, jpeg, png, gif, etc.).");
        }

        // Validate file extension
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new FileUploadException("Invalid file name.");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
        if (!allowedExtensions.contains(fileExtension)) {
            throw new FileUploadException("Invalid file extension. Allowed types: jpg, jpeg, png, gif, bmp, webp.");
        }

        // Check if product exists
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product Id", productId));

        // Use the configured image path from application.properties
        String fileName = fileService.uploadImage(imagePath, image);
        productFromDb.setImage(fileName);

        Product updatedProduct = productRepository.save(productFromDb);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }


}
