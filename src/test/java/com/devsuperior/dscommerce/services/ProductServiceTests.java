package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.factory.ProductFactory;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private Long existingProductId;
    private Long nonExistingProductId;
    private Long dependentId;
    private String productName;
    private Product product;
    private ProductDTO productDTO;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() throws Exception {
        existingProductId = 1L;
        nonExistingProductId = 1000L;
        productName = "Playstation 5";
        dependentId = 3L;
        product = ProductFactory.createProduct();
        productDTO = new ProductDTO(product);
        page = new PageImpl<>(List.of(product));

        Mockito.when(repository.findById(existingProductId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingProductId)).thenReturn(Optional.empty());
        Mockito.when(repository.searchByName(any(), any())).thenReturn(page);
        Mockito.when(repository.save(any())).thenReturn(product);

        Mockito.when(repository.getReferenceById(existingProductId)).thenReturn(product);
        Mockito.when(repository.getReferenceById(nonExistingProductId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(repository.existsById(existingProductId)).thenReturn(Boolean.TRUE);
        Mockito.when(repository.existsById(dependentId)).thenReturn(Boolean.TRUE);
        Mockito.when(repository.existsById(nonExistingProductId)).thenReturn(Boolean.FALSE);

        Mockito.doNothing().when(repository).deleteById(existingProductId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    @Test
    void findByIdShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = service.findById(existingProductId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingProductId);
        Assertions.assertEquals(result.getName(), product.getName());
    }

    @Test
    void findByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.findById(nonExistingProductId));
    }

    @Test
    void findAllShouldReturnPagedProductMinDTO() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProductMinDTO> result = service.findAll(productName, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getSize(), 1);
        Assertions.assertEquals(result.iterator().next().getName(), productName);
    }

    @Test
    void insertShouldReturnProductDTO() {
        ProductDTO result = service.insert(productDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), product.getId());
    }

    @Test
    void updateShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = service.update(existingProductId, productDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingProductId);
        Assertions.assertEquals(result.getName(), productDTO.getName());
    }

    @Test
    void updateShouldReturnResourceNotFoundExceptionWhenIdDoesNotExists() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.update(nonExistingProductId, productDTO));
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> service.delete(existingProductId));
    }

    @Test
    void deleteShouldThrowsResourceNotFoundExceptionWhenIdNotExists() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistingProductId));
    }

    @Test
    void deleteShouldThrowsDatabaseExceptionWhenDependentExists() {
        Assertions.assertThrows(DatabaseException.class, () -> service.delete(dependentId));
    }


}
