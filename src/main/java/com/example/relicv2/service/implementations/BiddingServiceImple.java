package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.*;
import com.example.relicv2.enums.Role;
import com.example.relicv2.exception.BiddingUnauthorizedException;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.Bid;
import com.example.relicv2.model.Product;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.BiddingRepository;
import com.example.relicv2.repository.OrderRepository;
import com.example.relicv2.repository.ProductRepository;
import com.example.relicv2.service.AuditTrialService;
import com.example.relicv2.service.BiddingService;
import com.example.relicv2.service.NotificationService;
import com.example.relicv2.service.OrderService;
import com.example.relicv2.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BiddingServiceImple implements BiddingService {
    private final ProductRepository productRepository;
    private final BiddingRepository biddingRepository;
    private final UserAuthenticationHelper userAuthenticationHelper;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    @Autowired
    private OrderService orderService;
    private final AuditTrialService auditTrialService;

    public BiddingServiceImple(ProductRepository productRepository, BiddingRepository biddingRepository, OrderRepository orderRepository, UserAuthenticationHelper userAuthenticationHelper, ModelMapper modelMapper, NotificationService notificationService, AuditTrialService auditTrialService) {
        this.productRepository = productRepository;
        this.biddingRepository = biddingRepository;
        this.userAuthenticationHelper = userAuthenticationHelper;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
        this.auditTrialService = auditTrialService;
    }

    @Override
    public String placeBid(Long productId, BidDto bidDto) {
        validateBidRequest(bidDto);
        User user = userAuthenticationHelper.getLoggedInUser();
        validateUser(user);
        Product product = getProductById(productId);
        validateBid(product, bidDto.getBidAmount(), user);

        Bid newBid = createNewBid(user, product, bidDto.getBidAmount());

        product.setCurrentBid(bidDto.getBidAmount());
        productRepository.save(product);

        notifyBidPlacement(product, user);
        handlePreviousHighestBid(productId, product);

        return "Bid has been placed successfully";
    }

    private void validateBidRequest(BidDto bidDto) {
        if (bidDto == null || bidDto.getBidAmount() == null) {
            throw new CustomException("All fields are required");
        }
    }
    private void validateUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            throw new CustomException("Admin users cannot place bids");
        }

        if (user.isBlocked()) {
            throw new CustomException("You are not authorized to access this resource. You are blocked.");
        }

        if (!user.isVerified()) {
            throw new CustomException("Please verify your email to access this");
        }
    }
    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
    private void validateBid(Product product, Long bidAmount, User user) {
        if (product.isBiddingClosed()) {
            throw new CustomException("Bidding is closed for this product");
        }

        if (userOwnsProduct(user, product)) {
            throw new BiddingUnauthorizedException("You cannot place a bid on your own product");
        }

        if (bidAmount.compareTo(product.getMinimumBid()) < 0) {
            throw new CustomException("Bid amount must be greater than or equal to the minimum bid");
        }

        Long currentBid = getCurrentBid(product);
        if (bidAmount.compareTo(currentBid) <= 0) {
            throw new CustomException("Bid amount must be greater than ₦" + currentBid);
        }
    }
    private boolean userOwnsProduct(User user, Product product) {
        return Objects.equals(user.getId(), product.getVendor().getId());
    }
    private Long getCurrentBid(Product product) {
        return product.getCurrentBid() != null ? product.getCurrentBid() : 0L;
    }
    private Bid createNewBid(User user, Product product, Long bidAmount) {
        Bid bid = new Bid();
        bid.setBidder(user);
        bid.setProduct(product);
        bid.setBidAmount(bidAmount);
        bid.setIsWinningBid(true);
        biddingRepository.save(bid);
        return bid;
    }
    private void notifyBidPlacement(Product product, User user) {
        String notificationMessage = "New bid has been placed on " + product.getName() + " by " + user.getUsername();
        notificationService.sendNotification(notificationMessage, product.getVendor().getId());
        notificationService.sendAdminNotification(notificationMessage);
    }
    private void handlePreviousHighestBid(Long productId, Product product) {
        List<Bid> previousBids = biddingRepository.findBidsByProductIdOrderByBidAmountDesc(productId);
        if (previousBids.size() > 1) {
            Bid previousBid = previousBids.get(1); // Get the second highest bid
            previousBid.setIsWinningBid(false);
            biddingRepository.save(previousBid);
            notifyOutbidUser(previousBid, product);
        }
    }
    private void notifyOutbidUser(Bid previousBid, Product product) {
        User previousBidder = previousBid.getBidder();
        String outbidMessage = "You have been outbid on " + product.getName();
        notificationService.sendNotification(outbidMessage, previousBidder.getId());
    }

    @Override
    public List<BidListDto> getBiddingList(Long productId) {
        List<Bid> biddingList = biddingRepository.findBidsByProductIdOrderByBidAmountDesc(productId);
        if (biddingList.isEmpty()) {
            return Collections.emptyList();
        }
        return biddingList.stream().map(bid -> modelMapper.map(bid, BidListDto.class)).collect(Collectors.toList());
    }

    @Override
    public BidListDto getWinningBid(Long productId) {
        Bid winningBid = biddingRepository.findByProductIdAndIsWinningBidTrue(productId);
        if (winningBid == null) {
            return null;
        }

        return modelMapper.map(winningBid, BidListDto.class);
    }

    @Override
    public String closeBidding(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        User user = userAuthenticationHelper.getLoggedInUser();
        Product product = productOptional.get();
        if(product.isBiddingClosed()){
            throw new CustomException("Auction is already closed");
        }
        if(product.getVendor() != user){
            throw new BiddingUnauthorizedException("You cannot close this bid because the product does not belong to you");
        }
        product.setBiddingClosed(true);
        productRepository.save(product);

        List<User> bidders = biddingRepository.findDistinctBiddersByProductId(productId);
        if (bidders.size() > 0) {
            bidders.forEach(bidder -> notificationService.sendNotification(
                    "Bidding has closed for " + product.getName(), bidder.getId()));
        }

        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Close Bidding", "Closed bidding for product " + product.getName());


        return "Bidding has been closed for product " + product.getName();
    }

    @Override
    public String reopenBidding(Long productId) throws ParseException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        Product product = productOptional.get();
        if(!product.isBiddingClosed()){
            throw new CustomException("Auction is already opened");
        }
        if(product.getVendor() != user){
            throw new BiddingUnauthorizedException("You cannot re open this bid because the product does not belong to you");
        }

        if (product.getWinningBidder() != null) {
            throw new CustomException("Product already has a winner, bidding is not allowed");
        }

        product.setBiddingClosed(false);
        productRepository.save(product);

        List<User> biddingList = biddingRepository.findDistinctBiddersByProductId(productId);
        if (biddingList.size() > 0) {
            biddingList.forEach(bidder -> notificationService.sendNotification(
                    "Bidding has reopened for " + product.getName(), bidder.getId()));
        }

        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Reopen Bidding", "Reopened bidding for product " + product.getName());

        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return currentDate.after(formatter.parse(product.getBiddingTimeLimit())) ? "Successfully reopened bid but please update the Bidding Time Limit " : "Successfully reopened bid ";
    }

    @Override
    public String declareWinner(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        Product product = productOptional.get();
        if(product.getVendor() != user){
            throw new BiddingUnauthorizedException("You cannot declare winner for this bid because the product does not belong to you");
        }

        List<Bid> biddingList = biddingRepository.findBidsByProductIdOrderByBidAmountDesc(productId);

        Bid winningBid = biddingList.get(0);
        User winner = winningBid.getBidder();

        finalizeProductBidding(product, winner, winningBid);
        createOrderForWinner(product, winningBid);

        notificationService.sendNotification("🎉 Congratulations! You have won " + product.getName() + ", Do well to pay for your order and it will be shipped to you", winner.getId());
        notificationService.sendAdminNotification("🎉 Winner has been awarded for " + product.getName());

        List<Bid> losingBids = biddingRepository.findByProductIdAndIsWinningBidFalseAndBidderNot(productId, winner);

        for (Bid bid : losingBids) {
            notificationService.sendNotification(
                    "You lost the bid on " + product.getName() + " to " + winner.getUsername(),
                    bid.getBidder().getId()
            );
        }

        return winner.getUsername() + " has be declared the winner of " + product.getName();
    }

    private void finalizeProductBidding(Product product, User winner, Bid winningBid) {
        winningBid.setIsWinningBid(true);
        product.setWinningBidder(winner);
        product.setBiddingClosed(true);
        product.setBiddingTimeLimit(getCurrentTimeStamp());
        productRepository.save(product);
        biddingRepository.save(winningBid);
    }
    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
        return dateFormat.format(new Date());
    }
    private void createOrderForWinner(Product product, Bid winningBid) {
        CreateOrderDto orderDto = buildOrderDto(product, winningBid);
        orderService.createOrder(orderDto);
    }
    private CreateOrderDto buildOrderDto(Product product, Bid winningBid) {
        CreateOrderDto orderDto = new CreateOrderDto();
        orderDto.setProductId(product.getId());
        Long totalPrice = winningBid.getBidAmount();
        orderDto.setTotalPrice(totalPrice);
        orderDto.setCompanyPercentage(3.0);
        orderDto.setVendorId(product.getVendor().getId());
        orderDto.setCustomerId(winningBid.getBidder().getId());
        orderDto.setCompanyCommission(0.03 * totalPrice);
        return orderDto;
    }

    @Override
    public List<UserBiddingListDto> getUserBids(Long userId, String keyword, String createdAtStart, String createdAtEnd, String sortBy) {
        List<Bid> userBids = biddingRepository.findByBidderId(userId);

        if (keyword != null && !keyword.isEmpty()) {
            userBids = userBids.stream()
                    .filter(bid -> bid.getProduct().getName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        if (createdAtStart != null && createdAtEnd != null) {
            try {
                Date startDate = dateFormat.parse(createdAtStart);
                Date endDate = dateFormat.parse(createdAtEnd);
                userBids = userBids.stream()
                        .filter(bid -> bid.getCreatedAt().after(startDate) && bid.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        switch (sortBy) {
            case "Date Ascending":
                userBids.sort(Comparator.comparing(Bid::getCreatedAt));
                break;
            case "Date Descending":
                userBids.sort(Comparator.comparing(Bid::getCreatedAt).reversed());
                break;
            case "Bid Amount Ascending":
                userBids.sort(Comparator.comparing(Bid::getBidAmount));
                break;
            case "Bid Amount Descending":
                userBids.sort(Comparator.comparing(Bid::getBidAmount).reversed());
                break;
        }

        return userBids.stream()
                .map(bid -> modelMapper.map(bid, UserBiddingListDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminBidListDto> getAllBidsForAdmin(String keyword, String createdAtStart, String createdAtEnd, String sortBy) {
        List<Bid> allBids = biddingRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            allBids = allBids.stream()
                    .filter(bid -> bid.getProduct().getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            bid.getBidder().getUsername().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        if (createdAtStart != null && createdAtEnd != null) {
            try {
                Date startDate = dateFormat.parse(createdAtStart);
                Date endDate = dateFormat.parse(createdAtEnd);
                allBids = allBids.stream()
                        .filter(bid -> bid.getCreatedAt().after(startDate) && bid.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        switch (sortBy) {
            case "Date Ascending":
                allBids.sort(Comparator.comparing(Bid::getCreatedAt));
                break;
            case "Date Descending":
                allBids.sort(Comparator.comparing(Bid::getCreatedAt).reversed());
                break;
            case "Bid Amount Ascending":
                allBids.sort(Comparator.comparing(Bid::getBidAmount));
                break;
            case "Bid Amount Descending":
                allBids.sort(Comparator.comparing(Bid::getBidAmount).reversed());
                break;
        }

        return allBids.stream()
                .map(bid -> modelMapper.map(bid, AdminBidListDto.class))
                .collect(Collectors.toList());
    }
}
