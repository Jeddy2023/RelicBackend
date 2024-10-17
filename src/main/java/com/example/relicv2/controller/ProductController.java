package com.example.relicv2.controller;

import com.example.relicv2.dto.*;
import com.example.relicv2.enums.Condition;
import com.example.relicv2.service.CategoryService;
import com.example.relicv2.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> createAuction(@ModelAttribute @Valid CreateProductDto createProductDto) throws IOException {
        String createdProduct = productService.createProduct(createProductDto);
        return ResponseEntity.ok().body(createdProduct);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "false") boolean totalWonProducts,
            @RequestParam(defaultValue = "false") boolean totalActiveAuctions,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long minBid,
            @RequestParam(required = false) Long maxBid,
            @RequestParam(required = false) Double minimumBid,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "Default Sorting") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ProductDto> products = productService.getAllProducts(totalWonProducts, totalActiveAuctions,categoryId, condition, keyword, minBid, maxBid, minimumBid, rating, sortBy);

        int start = Math.min(page * size, products.size());
        int end = Math.min((page + 1) * size, products.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = new PageImpl<>(products.subList(start, end), pageable, products.size());

        return new ResponseEntity<>(productPage, HttpStatus.OK);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminProductDto>> getAllProductsForAdmin(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isBiddingClosed,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(required = false) String biddingTimeLimitStart,
            @RequestParam(required = false) String biddingTimeLimitEnd,
            @RequestParam(required = false) Long minBid,
            @RequestParam(required = false) Long maxBid,
            @RequestParam(required = false, defaultValue = "Default Sorting") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AdminProductDto> products = productService.getAllProductsForAdmin(
                categoryId,
                condition,
                keyword,
                isBiddingClosed,
                isBlocked,
                biddingTimeLimitStart,
                biddingTimeLimitEnd,
                minBid,
                maxBid,
                sortBy
        );

        int start = Math.min(page * size, products.size());
        int end = Math.min((page + 1) * size, products.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminProductDto> productPage = new PageImpl<>(products.subList(start, end), pageable, products.size());

        return new ResponseEntity<>(productPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        String response = productService.deleteProduct(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<UserProductDto>> getUserProducts(
            @RequestParam(defaultValue = "false") boolean pendingReviews,
            @RequestParam(defaultValue = "false") boolean activeProducts,
            @RequestParam(defaultValue = "false") boolean offlineBid,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId
    ) {
        List<UserProductDto> products = productService.getUserProducts(pendingReviews, activeProducts, offlineBid , keyword, userId);

        int start = Math.min(page * size, products.size());
        int end = Math.min((page + 1) * size, products.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProductDto> productPage = new PageImpl<>(products.subList(start, end), pageable, products.size());

        return new ResponseEntity<>(productPage, HttpStatus.OK);
    }

    @GetMapping("/totals")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<TotalProductsCountDto> getTotalProductCounts() {
        TotalProductsCountDto countsDto = productService.getTotalProductCounts();
        return new ResponseEntity<>(countsDto, HttpStatus.OK);
    }

    @PostMapping(value = "/username/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createAuctionWithUsername(@ModelAttribute @Valid CreateProductDto createProductDto) throws IOException {
        String createdProduct = productService.createProductWithUsername(createProductDto);
        return ResponseEntity.ok().body(createdProduct);
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<String> blockProduct(@PathVariable Long id) {
        String response = productService.blockProduct(id);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<String> unblockProduct(@PathVariable Long id) {
        String response = productService.unblockProduct(id);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<String> endProduct(@PathVariable Long id) {
        String response = productService.endProduct(id);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<String> reopenProduct(@PathVariable Long id) {
        String response = productService.reopenProduct(id);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/reset-winner")
    public ResponseEntity<String> resetWinningBidder(@PathVariable Long id) {
        String response = productService.resetWinningBidder(id);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/trophy")
    public void winningBidderTrophy(@PathVariable Long id) {
        productService.winningBidderTrophy(id);
    }

}
