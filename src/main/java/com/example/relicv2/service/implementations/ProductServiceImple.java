package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.*;
import com.example.relicv2.enums.Condition;
import com.example.relicv2.enums.Role;
import com.example.relicv2.exception.BiddingUnauthorizedException;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.exception.MediaUploadException;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.Bid;
import com.example.relicv2.model.Category;
import com.example.relicv2.model.Product;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.BiddingRepository;
import com.example.relicv2.repository.CategoryRepository;
import com.example.relicv2.repository.ProductRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.AuditTrialService;
import com.example.relicv2.service.FileUploadService;
import com.example.relicv2.service.NotificationService;
import com.example.relicv2.service.ProductService;
import com.example.relicv2.util.UserAuthenticationHelper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImple implements ProductService {

    private ModelMapper modelMapper;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private final NotificationService notificationService;
    private BiddingRepository biddingRepository;
    private FileUploadService fileUploadService;
    private UserAuthenticationHelper userAuthenticationHelper;
    private AuditTrialService auditTrialService;

    @Override
    public String createProduct(CreateProductDto createProductDto) {
        validateCondition(createProductDto.getCondition());

        String uploadedImageUrl = uploadImage(createProductDto.getProductImage());
        String uploadedBackViewImageUrl = uploadImage(createProductDto.getBackViewImage());
        String uploadedSideViewImageUrl = uploadImage(createProductDto.getSideViewImage());
        String uploadedInnerViewImageUrl = uploadImage(createProductDto.getInnerViewImage());

        User user = userAuthenticationHelper.getLoggedInUser();
        validateUserForProductCreation(user);

        Optional<Category> category = categoryRepository.findById(createProductDto.getCategoryId());
        if (category.isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }

        Product newProduct = new Product();
        newProduct.setVendor(user);
        newProduct.setName(createProductDto.getName());
        newProduct.setShortDescription(createProductDto.getShortDescription());
        newProduct.setDescription(createProductDto.getDescription());
        newProduct.setCondition(Condition.valueOf(createProductDto.getCondition()));
        newProduct.setMinimumBid(createProductDto.getMinimumBid());
        newProduct.setCurrentBid(createProductDto.getMinimumBid());
        newProduct.setCategory(category.get());
        newProduct.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());
        newProduct.setProductImageUrl(uploadedImageUrl);
        newProduct.setImageUrls(Arrays.asList(uploadedBackViewImageUrl, uploadedSideViewImageUrl, uploadedInnerViewImageUrl));

        productRepository.save(newProduct);
        notificationService.sendAdminNotification("New Product" + newProduct.getName() + "was just created, by" + newProduct.getVendor().getShopname());
        return "Product created succesfully";
    }
    private String uploadImage(MultipartFile image) {
        try {
            return fileUploadService.uploadImage(image);
        } catch (IOException e) {
            throw new MediaUploadException("An error occurred while uploading image");
        }
    }
    private void validateCondition(String condition) {
        try {
            Condition.valueOf(condition);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid condition provided");
        }
    }
    private void validateUserForProductCreation(User user) {
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        if (user.isBlocked()) {
            throw new CustomException("You are not authorized to access this resource, you are blocked");
        }

        if (!user.isVerified()) {
            throw new CustomException("Please verify your email to access this");
        }

        if (user.getRole() != Role.SELLER) {
            throw new BiddingUnauthorizedException("You have to be a Vendor to access this resource");
        }
    }
    @Override
    public TotalProductsCountDto getTotalProductCounts() {
        User user = userAuthenticationHelper.getLoggedInUser();
        List<Product> products = productRepository.findByVendorId(user.getId());

        long totalPendingReviews = products.stream()
                .filter(product -> product.getReviews() == null || product.getReviews().isEmpty())
                .count();

        long totalActiveProducts = products.stream()
                .filter(product -> !product.isBiddingClosed())
                .count();

        long totalOfflineProducts = products.stream()
                .filter(Product::isBiddingClosed)
                .count();

        long totalUserProducts = products.size();

        TotalProductsCountDto countsDto = new TotalProductsCountDto();
        countsDto.setTotalPendingReviews(totalPendingReviews);
        countsDto.setTotalActiveProducts(totalActiveProducts);
        countsDto.setTotalOfflineProducts(totalOfflineProducts);
        countsDto.setTotalUserProducts(totalUserProducts);

        return countsDto;
    }
    @Override
    public List<UserProductDto> getUserProducts(
            boolean pendingReviews,
            boolean activeProducts,
            boolean offlineBid,
            String keyword,
            Long userId
    ) {
        User user = getUserByIdOrCurrent(userId);

        List<Product> products = productRepository.findByVendorId(user.getId());

        if (pendingReviews) {
            products = products.stream()
                    .filter(product -> product.getReviews() == null || product.getReviews().isEmpty())
                    .collect(Collectors.toList());
        }
        if (keyword != null) {
            String lowerCaseKeyword = keyword.toLowerCase();
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toList());
        }
        if (activeProducts) {
            products = products.stream()
                    .filter(product -> !product.isBiddingClosed())
                    .collect(Collectors.toList());
        }
        if (offlineBid) {
            products = products.stream()
                    .filter(Product::isBiddingClosed)
                    .collect(Collectors.toList());
        }

        products.sort(Comparator.comparing(Product::getUpdatedAt).reversed());

        return products.stream()
                .map(product -> {
                    UserProductDto userProductDto = modelMapper.map(product, UserProductDto.class);
                    userProductDto.setTotalReviews(product.getReviews() != null ? product.getReviews().size() : 0);
                    userProductDto.setTotalBids(product.getBidList() != null ? product.getBidList().size() : 0);
                    return userProductDto;
                })
                .collect(Collectors.toList());
    }
    private User getUserByIdOrCurrent(Long userId) {
        if (userId == null) {
            return userAuthenticationHelper.getLoggedInUser();
        } else {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("User not found"));
        }
    }
    @Override
    public List<ProductDto> getAllProducts(
            boolean totalWonProducts,
            boolean totalActiveAuctions,
            Long categoryId,
            Condition condition,
            String keyword,
            Long minBid,
            Long maxBid,
            Double minimumBid,
            Integer rating,
            String sortBy
    ) {
        List<Product> products = fetchProductsBasedOnFilters(totalWonProducts, totalActiveAuctions);

        if (categoryId != null) { products = filterByCategory(products, categoryId); }
        if (condition != null) { products = filterByCondition(products, condition); }
        if (keyword != null) { products = filterByKeyword(products, keyword); }
        if (minBid != null && maxBid != null) { products = filterByBidRange(products, minBid, maxBid); }
        if (minimumBid != null) { products = filterByMinimumBid(products, minimumBid); }
        if (rating != null) { products = filterByRating(products, rating); }

        switch (sortBy) {
            case "Average Rating":
                products = products.stream()
                        .sorted((p1, p2) -> Double.compare(
                                p2.getAverageRating() != null ? p2.getAverageRating() : 0,
                                p1.getAverageRating() != null ? p1.getAverageRating() : 0))
                        .collect(Collectors.toList());
                break;
            case "Sort by current bid: low to high":
                products = products.stream()
                        .sorted(Comparator.comparingLong(p -> p.getCurrentBid() != null ? p.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Sort by current bid: high to low":
                products = products.stream()
                        .sorted((p1, p2) -> Long.compare(
                                p2.getCurrentBid() != null ? p2.getCurrentBid() : 0,
                                p1.getCurrentBid() != null ? p1.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Sort auction by ending soonest":
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getBiddingTimeLimit))
                        .collect(Collectors.toList());
                break;
            case "Sort auction by recently started":
                products = products.stream()
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .collect(Collectors.toList());
                break;
            case "Sort auction by most active":
                products = products.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getNumberOfBids(), p1.getNumberOfBids()))
                        .collect(Collectors.toList());
                break;
        }

        return products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }
    private List<Product> fetchProductsBasedOnFilters(boolean totalWonProducts, boolean totalActiveAuctions) {
        User user;
        if (totalWonProducts) {
            user = userAuthenticationHelper.getLoggedInUser();
            return productRepository.findByWinningBidderId(user.getId());
        } else if (totalActiveAuctions) {
            user = userAuthenticationHelper.getLoggedInUser();
            return productRepository.findByVendorIdAndIsBiddingClosedFalse(user.getId());
        } else {
            return productRepository.findByIsBiddingClosedFalseAndIsBlockedFalse();
        }
    }
    private List<Product> filterByCategory(List<Product> products, Long categoryId) {
        return products.stream()
                .filter(product -> product.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());
    }
    private List<Product> filterByCondition(List<Product> products, Condition condition) {
        return products.stream()
                .filter(product -> product.getCondition() == condition)
                .collect(Collectors.toList());
    }
    private List<Product> filterByKeyword(List<Product> products, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return products.stream()
                .filter(product -> product.getName().toLowerCase().contains(lowerCaseKeyword))
                .collect(Collectors.toList());
    }
    private List<Product> filterByBidRange(List<Product> products, Long minBid, Long maxBid) {
        return products.stream()
                .filter(product -> product.getCurrentBid() != null &&
                        product.getCurrentBid() >= minBid && product.getCurrentBid() <= maxBid)
                .collect(Collectors.toList());
    }
    private List<Product> filterByMinimumBid(List<Product> products, Double minimumBid) {
        return products.stream()
                .filter(product -> product.getMinimumBid() != null && product.getMinimumBid() >= minimumBid)
                .collect(Collectors.toList());
    }
    private List<Product> filterByRating(List<Product> products, Integer rating) {
        return products.stream()
                .filter(product -> product.getAverageRating() != null && product.getAverageRating().intValue() == rating)
                .collect(Collectors.toList());
    }
    @Override
    public List<AdminProductDto> getAllProductsForAdmin(
            Long categoryId,
            Condition condition,
            String keyword,
            Boolean isBiddingClosed,
            Boolean isBlocked,
            String biddingTimeLimitStart,
            String biddingTimeLimitEnd,
            Long minBid,
            Long maxBid,
            String sortBy
    ) {
        List<Product> products = productRepository.findAll();

        if (categoryId != null) {
            products = products.stream()
                    .filter(product -> product.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (condition != null) {
            products = products.stream()
                    .filter(product -> product.getCondition() == condition)
                    .collect(Collectors.toList());
        }

        if (keyword != null) {
            String lowerCaseKeyword = keyword.toLowerCase();
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(lowerCaseKeyword) ||
                            product.getShortDescription().toLowerCase().contains(lowerCaseKeyword) ||
                            product.getVendor().getShopname().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toList());
        }

        if (isBiddingClosed != null) {
            products = products.stream()
                    .filter(product -> product.isBiddingClosed() == isBiddingClosed)
                    .collect(Collectors.toList());
        }

        if (isBlocked != null) {
            products = products.stream()
                    .filter(product -> product.isBlocked() == isBlocked)
                    .collect(Collectors.toList());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            if (biddingTimeLimitStart != null && biddingTimeLimitEnd != null) {
                Date startDate = dateFormat.parse(biddingTimeLimitStart);
                Date endDate = dateFormat.parse(biddingTimeLimitEnd);
                products = products.stream()
                        .filter(product -> {
                            try {
                                Date biddingTimeLimit = dateFormat.parse(product.getBiddingTimeLimit());
                                return biddingTimeLimit.after(startDate) && biddingTimeLimit.before(endDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (minBid != null && maxBid != null) {
            products = products.stream()
                    .filter(product -> product.getCurrentBid() != null &&
                            product.getCurrentBid() >= minBid && product.getCurrentBid() <= maxBid)
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "Most Active":
                products = products.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getNumberOfBids(), p1.getNumberOfBids()))
                        .collect(Collectors.toList());
                break;
            case "Recently Started":
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                        .collect(Collectors.toList());
                break;
            case "Current Bid: Low to High":
                products = products.stream()
                        .sorted(Comparator.comparingLong(p -> p.getCurrentBid() != null ? p.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Current Bid: High to Low":
                products = products.stream()
                        .sorted((p1, p2) -> Long.compare(p2.getCurrentBid() != null ? p2.getCurrentBid() : 0,
                                p1.getCurrentBid() != null ? p1.getCurrentBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Minimum Bid: Low to High":
                products = products.stream()
                        .sorted(Comparator.comparingLong(p -> p.getMinimumBid() != null ? p.getMinimumBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Minimum Bid: High to Low":
                products = products.stream()
                        .sorted((p1, p2) -> Long.compare(p2.getMinimumBid() != null ? p2.getMinimumBid() : 0,
                                p1.getMinimumBid() != null ? p1.getMinimumBid() : 0))
                        .collect(Collectors.toList());
                break;
            case "Ending Soonest":
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getBiddingTimeLimit))
                        .collect(Collectors.toList());
                break;
        }

        return products.stream()
                .map(product -> modelMapper.map(product, AdminProductDto.class))
                .collect(Collectors.toList());
    }
    @Override
    public ProductDto getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) throw new ResourceNotFoundException("Product not found");
        return modelMapper.map(product.get(), ProductDto.class);
    }
    @Override
    public String deleteProduct(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        User user = userAuthenticationHelper.getLoggedInUser();

        if(user != product.getVendor() && user.getRole() != Role.ADMIN){
            throw new BiddingUnauthorizedException("This product does not belong to you, you cannot delete it");
        }

        List<User> bidders = biddingRepository.findDistinctBiddersByProductId(product.getId());
        if (bidders.size() > 0) {
            bidders.forEach(bidder -> notificationService.sendNotification(
                    product.getName() + "has been deleted ", bidder.getId()));
        }


        auditTrialService.addToAuditTrial(user, "Product Deletion", "Deleted product: " + product.getName());
        productRepository.delete(product);
        return "Product deleted succesfully";
    }
    @Scheduled(fixedRate = 1000)
    public void checkBiddingTimeLimits(){
        List<Product> products = productRepository.findAll();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Product product : products) {
            try{
                Date biddingTimeLimit = dateFormat.parse(product.getBiddingTimeLimit());

                if (!product.isApproachingNotificationSent() && !product.isBiddingClosed() && isApproachingBiddingTimeLimit(biddingTimeLimit) && !product.getBidList().isEmpty()) {
                    sendBiddingEndingNotification(product);
                    product.setApproachingNotificationSent(true);
                }

                if (product.getBiddingTimeLimit() != null && new Date().after(biddingTimeLimit) && !product.isBiddingClosed()) {
                    if (!product.isEndingNotificationSent()) {
                        product.setEndingNotificationSent(true);
                        if (product.getBidList().isEmpty()) {
                            notificationService.sendNotification("Bidding time limit for " + product.getName() + " has reached and there was no bid on this product. Please update the bidding time limit.", product.getVendor().getId());
                        } else {
                            notificationService.sendNotification("Bidding time limit for " + product.getName() + " has reached. Please award your winner so they can proceed to pay.", product.getVendor().getId());
                        }
                    }
                    product.setBiddingClosed(true);
                    productRepository.save(product);
                }

            } catch (ParseException e) {
                System.err.println("Error getting bidding time limit for product: " + product.getName());
                e.printStackTrace();
            }
        }
    }
    private boolean isApproachingBiddingTimeLimit(Date productDate) throws ParseException {
        Date currentTime = new Date();

        long timeDifferenceInMillis = productDate.getTime() - currentTime.getTime();

        long timeDifferenceInHours = timeDifferenceInMillis / (60 * 60 * 1000);

        return timeDifferenceInHours <= 24;
    }
    private void sendBiddingEndingNotification(Product product) {
        List<Bid> bidList = product.getBidList();

        for (Bid bid : bidList) {
            User bidder = bid.getBidder();
            boolean isWinningBidder = product.getWinningBidder() != null && product.getWinningBidder().equals(bidder);

            String notificationMessage;
            if (isWinningBidder) {
                notificationMessage = "Bidding for " + product.getName() + "will soon end and you are winning the bid.";
            } else {
                notificationMessage = "Bidding for " + product.getName() + "will soon end and you are losing the bid.";
            }

            notificationService.sendNotification(notificationMessage, bidder.getId());
            notificationService.sendAdminNotification(notificationMessage);
        }
    }

    public String createProductWithUsername(CreateProductDto createProductDto) {
        validateCondition(createProductDto.getCondition());

        String uploadedImageUrl = uploadImage(createProductDto.getProductImage());
        String uploadedBackViewImageUrl = uploadImage(createProductDto.getBackViewImage());
        String uploadedSideViewImageUrl = uploadImage(createProductDto.getSideViewImage());
        String uploadedInnerViewImageUrl = uploadImage(createProductDto.getInnerViewImage());

        Optional<User> vendor = userRepository.findByUsername(createProductDto.getUsername());
        if (vendor.isEmpty()) {
            throw new ResourceNotFoundException("Vendor not found");
        }

        if(vendor.get().getRole() != Role.SELLER){
            throw new BiddingUnauthorizedException("The specified user is not a seller");
        }

        Optional<Category> category = categoryRepository.findById(createProductDto.getCategoryId());
        if (category.isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }

        Product newProduct = new Product();
        newProduct.setVendor(vendor.get());
        newProduct.setName(createProductDto.getName());
        newProduct.setShortDescription(createProductDto.getShortDescription());
        newProduct.setDescription(createProductDto.getDescription());
        newProduct.setCondition(Condition.valueOf(createProductDto.getCondition()));
        newProduct.setMinimumBid(createProductDto.getMinimumBid());
        newProduct.setCurrentBid(createProductDto.getMinimumBid());
        newProduct.setCategory(category.get());
        newProduct.setBiddingTimeLimit(createProductDto.getBiddingTimeLimit());
        newProduct.setProductImageUrl(uploadedImageUrl);
        newProduct.setImageUrls(Arrays.asList(uploadedBackViewImageUrl, uploadedSideViewImageUrl, uploadedInnerViewImageUrl));

        productRepository.save(newProduct);
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Product Creation", "Created product: " + newProduct.getName());
        return "Product created successfully";
    }

    public String blockProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(product.isBlocked()){
            throw new CustomException("Auction is already blocked");
        }
        product.setBlocked(true);
        productRepository.save(product);
        notificationService.sendNotification("Your product " + product.getName() + " has been blocked", product.getVendor().getId());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Product Block", "Blocked product: " + product.getName());
        return "Product blocked successfully";
    }

    public String unblockProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(!product.isBlocked()){
            throw new CustomException("Auction is not blocked");
        }
        product.setBlocked(false);
        productRepository.save(product);
        notificationService.sendNotification("Your product " + product.getName() + " has been unblocked", product.getVendor().getId());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Product Unblock", "Unblocked product: " + product.getName());
        return "Product unblocked successfully";
    }

    public String endProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(product.isBiddingClosed()){
            throw new CustomException("Auction is already closed down");
        }
        product.setBiddingClosed(true);
        productRepository.save(product);
        notificationService.sendNotification("Your product " + product.getName() + " bidding has been stopped", product.getVendor().getId());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Product End", "Ended product: " + product.getName());
        return "Product ended successfully";
    }

    public String reopenProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        if(!product.isBiddingClosed()){
            throw new CustomException("Auction is already open");
        }
        product.setBiddingClosed(false);
        productRepository.save(product);
        notificationService.sendNotification("Your product " + product.getName() + " bidding has been reopened", product.getVendor().getId());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Product Reopen", "Reopened product: " + product.getName());
        return "Product reopened successfully";
    }

    public String resetWinningBidder(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        product.setWinningBidder(null);
        productRepository.save(product);
        notificationService.sendNotification("The winning bidder for your product " + product.getName() + " has been reset", product.getVendor().getId());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Reset Winning Bidder", "Reset winning bidder for product: " + product.getName());
        return "Winning bidder reset successfully";
    }

    public void winningBidderTrophy(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOptional.get();
        product.setWinningBidderTrophy(true);
        productRepository.save(product);
    }
}
