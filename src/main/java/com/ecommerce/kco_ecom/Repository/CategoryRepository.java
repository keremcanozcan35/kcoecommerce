package com.ecommerce.kco_ecom.Repository;

import com.ecommerce.kco_ecom.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
