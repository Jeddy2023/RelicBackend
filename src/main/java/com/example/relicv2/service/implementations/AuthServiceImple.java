    package com.example.relicv2.service.implementations;

    import com.example.relicv2.dto.*;
    import com.example.relicv2.enums.OtpType;
    import com.example.relicv2.enums.Role;
    import com.example.relicv2.exception.AlreadyExistsException;
    import com.example.relicv2.exception.BiddingUnauthorizedException;
    import com.example.relicv2.exception.CustomException;
    import com.example.relicv2.model.ShippingAddress;
    import com.example.relicv2.model.ShopDetails;
    import com.example.relicv2.model.User;
    import com.example.relicv2.repository.ShippingAddressRepository;
    import com.example.relicv2.repository.ShopDetailsRepository;
    import com.example.relicv2.repository.UserRepository;
    import com.example.relicv2.security.JwtTokenProvider;
    import com.example.relicv2.service.AuthService;
    import com.example.relicv2.service.MailingService;
    import com.example.relicv2.service.NotificationService;
    import com.example.relicv2.service.OtpService;
    import com.example.relicv2.util.UserAuthenticationHelper;
    import org.modelmapper.ModelMapper;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.AuthenticationException;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import java.util.NoSuchElementException;
    import java.util.Objects;
    import java.util.Optional;
    import java.util.Random;

    @Service
    public class AuthServiceImple implements AuthService {
        private final UserRepository userRepository;
        private final ShippingAddressRepository shippingAddressRepository;
        private final ShopDetailsRepository shopDetailsRepository;
        private final JwtTokenProvider jwtTokenProvider;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final ModelMapper modelMapper;
        private final MailingService mailingService;
        private final OtpService otpService;
        private final NotificationService notificationService;
        private final UserAuthenticationHelper userAuthenticationHelper;

        public AuthServiceImple(UserRepository userRepository, ShippingAddressRepository shippingAddressRepository, ShopDetailsRepository shopDetailsRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, ModelMapper modelMapper, MailingServiceImpl mailingService, OtpServiceImpl otpService, NotificationService notificationService, UserAuthenticationHelper userAuthenticationHelper) {
            this.userRepository = userRepository;
            this.shippingAddressRepository = shippingAddressRepository;
            this.shopDetailsRepository = shopDetailsRepository;
            this.jwtTokenProvider = jwtTokenProvider;
            this.passwordEncoder = passwordEncoder;
            this.authenticationManager = authenticationManager;
            this.modelMapper = modelMapper;
            this.mailingService = mailingService;
            this.otpService = otpService;
            this.notificationService = notificationService;
            this.userAuthenticationHelper = userAuthenticationHelper;
        }

        @Override
        public RegisterResponseDto register(RegisterCustomerDto registerCustomerDto) {
            if(userRepository.existsByEmail(registerCustomerDto.getEmail())){
                throw new AlreadyExistsException("Email already exists.");
            }

            validateRole(registerCustomerDto.getRole());

            String username = generateUniqueUsername(registerCustomerDto.getEmail());

            User user = createUser(registerCustomerDto, username);
            ShippingAddress shippingAddress = createShippingAddress(registerCustomerDto, user);
            userRepository.save(user);
            shippingAddressRepository.save(shippingAddress);

            String otp = otpService.create(user);
            mailingService.sendEmail(user.getEmail(), "OTP Verification", "Your OTP code is: " + otp);
            String verificationToken = jwtTokenProvider.generateVerificationToken(user);
            notificationService.sendAdminNotification("New customer registered with email: " + user.getEmail());
            return new RegisterResponseDto("You have been registered successfully, Verify your email then login to continue", verificationToken);
        }
        @Override
        public RegisterResponseDto register(RegisterVendorDto registerVendorDto) {
            if(userRepository.existsByEmail(registerVendorDto.getEmail())){
                throw new AlreadyExistsException("Email already exists.");
            }
            if(userRepository.existsByUsername(registerVendorDto.getUsername())){
                throw new AlreadyExistsException("Username already exists");
            }
            if(userRepository.existsByShopname(registerVendorDto.getShopname())){
                throw new AlreadyExistsException("Shop Name already exists");
            }

            validateRole(registerVendorDto.getRole());

            User user = createUser(registerVendorDto);
            ShippingAddress shippingAddress = createShippingAddress(registerVendorDto, user);
            ShopDetails shopDetails = createShopDetails(registerVendorDto, user);

            userRepository.save(user);
            shippingAddressRepository.save(shippingAddress);
            shopDetailsRepository.save(shopDetails);


            String otp = otpService.create(user);
            mailingService.sendEmail(user.getEmail(), "OTP Verification", "Your OTP code is: " + otp);
            String verificationToken = jwtTokenProvider.generateVerificationToken(user);
            notificationService.sendAdminNotification("New vendor registered with email: " + user.getEmail());
            return new RegisterResponseDto("You have been registered successfully, Verify your email then login to continue", verificationToken);
        }
        @Override
        public LoginResponseDto login(LoginUserDto loginUserDto) {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                User loggedUser = userAuthenticationHelper.getLoggedInUser();
                UserDto userDto = modelMapper.map(loggedUser, UserDto.class);
                long expiresIn = jwtTokenProvider.getJwtExpirationDate();
                return new LoginResponseDto(userDto,accessToken,"Login Succesful", expiresIn);
            } catch (AuthenticationException e) {
                throw new BiddingUnauthorizedException("Invalid email or password");
            } catch (NoSuchElementException e) {
                throw new BiddingUnauthorizedException("User not found");
            }
        }
        @Override
        public UserDetailsDTO getUserDetails() {
            User loggedUser = userAuthenticationHelper.getLoggedInUser();
            return modelMapper.map(loggedUser, UserDetailsDTO.class);
        }
        @Override
        public String updateUserDetails(UpdateUserDetailsDto updateUserDetailsDto) {
            User loggedUser = userAuthenticationHelper.getLoggedInUser();
            updateUserIfNeeded(loggedUser, updateUserDetailsDto);

            handleShippingAddressIfNeeded(loggedUser, updateUserDetailsDto);
            handleShopDetailsIfNeeded(loggedUser, updateUserDetailsDto);

            userRepository.save(loggedUser);
            notificationService.sendAdminNotification(loggedUser.getUsername() + " Just updated his account details.");
            return "User details updated successfully";
        }
        @Override
        public String verifyEmail(VerifyEmailDto verifyEmailDto) {
            String token = verifyEmailDto.getVerificationToken();
            User user = jwtTokenProvider.getUserFromToken(token);

            if (user.isVerified()) {
                return "User already verified";
            }

            Boolean verified = otpService.verify(user, verifyEmailDto.getOtp(), OtpType.REGISTER);
            System.out.println(verified);
            if (verified) {
                user.setVerified(true);
                userRepository.save(user);
                otpService.clearOtps(user, OtpType.REGISTER);
                return "User Successfully verified";
            }
            throw new CustomException("Incorrect Otp");
        }
        @Override
        public RegisterResponseDto resendOtp(String verificationToken) {
            User user = jwtTokenProvider.getUserFromToken(verificationToken);

            if (user.isVerified()) {
                throw new CustomException("User is already verified");
            }

            otpService.clearOtps(user, OtpType.REGISTER);
            String otp = otpService.create(user);
            String newVerificationToken = jwtTokenProvider.generateVerificationToken(user);
            mailingService.sendEmail(user.getEmail(), "OTP Verification", "Your OTP code is: " + otp);

            return new RegisterResponseDto("A new OTP has been sent to your email", newVerificationToken);
        }


        private User createUser(RegisterCustomerDto dto, String username) {
            User user = new User();
            user.setLastname(dto.getLastname());
            user.setFirstname(dto.getFirstname());
            user.setUsername(username);
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPhonenumber(dto.getPhonenumber());
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            return user;
        }
        private User createUser(RegisterVendorDto dto) {
            User user = new User();
            user.setLastname(dto.getLastname());
            user.setFirstname(dto.getFirstname());
            user.setShopname(dto.getShopname());
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPhonenumber(dto.getPhonenumber());
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            return user;
        }
        private void updateUserIfNeeded(User loggedUser, UpdateUserDetailsDto dto) {
            if (userRepository.existsByUsername(dto.getUsername()) &&
                    !Objects.equals(loggedUser.getUsername(), dto.getUsername())) {
                throw new AlreadyExistsException("Username already exists");
            }

            if (!dto.getPassword().isEmpty()) {
                validateAndUpdatePassword(loggedUser, dto.getPassword(), dto.getNewpassword());
            }

            loggedUser.setUsername(dto.getUsername());
            loggedUser.setLastname(dto.getLastname());
            loggedUser.setFirstname(dto.getFirstname());
            loggedUser.setPhonenumber(dto.getPhonenumber());
            loggedUser.setEmail(dto.getEmail());
        }
        private void validateAndUpdatePassword(User loggedUser, String currentPassword, String newPassword) {
            if (newPassword.isEmpty()) {
                throw new CustomException("Please provide a new password");
            }

            if (!passwordEncoder.matches(currentPassword, loggedUser.getPassword())) {
                throw new CustomException("Current Password is Incorrect");
            }

            loggedUser.setPassword(passwordEncoder.encode(newPassword));
        }
        private void handleShippingAddressIfNeeded(User loggedUser, UpdateUserDetailsDto dto) {
            Optional<ShippingAddress> optionalShippingAddress = shippingAddressRepository.findByUser(loggedUser);
            if (optionalShippingAddress.isEmpty()) {
                ShippingAddress shippingAddress = new ShippingAddress();
                shippingAddress.setFirstname(dto.getFirstname());
                shippingAddress.setLastname(dto.getLastname());
                shippingAddress.setUser(loggedUser);
                shippingAddressRepository.save(shippingAddress);
            }
        }
        private void handleShopDetailsIfNeeded(User loggedUser, UpdateUserDetailsDto dto) {
            if (loggedUser.getRole() == Role.SELLER) {
                Optional<ShopDetails> optionalShopDetails = shopDetailsRepository.findByUser(loggedUser);
                if (optionalShopDetails.isEmpty()) {
                    ShopDetails shopDetails = new ShopDetails();
                    shopDetails.setPhonenumber(dto.getPhonenumber());
                    shopDetails.setEmail(dto.getEmail());
                    shopDetails.setUser(loggedUser);
                    shopDetailsRepository.save(shopDetails);
                }
            }
        }
        private ShippingAddress createShippingAddress(RegisterVendorDto dto, User user) {
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setFirstname(dto.getFirstname());
            shippingAddress.setLastname(dto.getLastname());
            shippingAddress.setUser(user);
            return shippingAddress;
        }
        private ShippingAddress createShippingAddress(RegisterCustomerDto dto, User user) {
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setFirstname(dto.getFirstname());
            shippingAddress.setLastname(dto.getLastname());
            shippingAddress.setUser(user);
            return shippingAddress;
        }
        private ShopDetails createShopDetails(RegisterVendorDto dto, User user) {
            ShopDetails shopDetails = new ShopDetails();
            shopDetails.setEmail(dto.getEmail());
            shopDetails.setShopname(dto.getShopname());
            shopDetails.setUser(user);
            return shopDetails;
        }
        private void validateRole(String role) {
            try {
                Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException("Invalid role provided");
            }
        }
        private String generateUniqueUsername(String email) {
            String baseUsername = email.split("@")[0];
            String uniqueUsername = baseUsername;

            Random random = new Random();
            int suffix;

            do {
                suffix = 1000 + random.nextInt(9000);
                uniqueUsername = baseUsername + suffix;
            } while (userRepository.existsByUsername(uniqueUsername));

            return uniqueUsername;
        }

    }
