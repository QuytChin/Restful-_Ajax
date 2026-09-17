package vn.iotstar.repository;
import vn.iotstar.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
public interface CategoryRepository extends JpaRepository<Category,Long> {
 Page<Category> findByCategoryNameContainingIgnoreCase(String keyword,Pageable pageable);
 boolean existsByCategoryNameIgnoreCase(String name);
 boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(String name,Long id);
}
