package vn.iotstar.repository;
import vn.iotstar.entity.Product;
import org.springframework.data.jpa.repository.*;
public interface ProductRepository extends JpaRepository<Product,Long>,JpaSpecificationExecutor<Product> {
 boolean existsByCategoryCategoryId(Long categoryId);
}
