package com.example.relicv2.service.implementations;

import com.cloudinary.utils.StringUtils;
import com.example.relicv2.dto.CreateShopDetailsDto;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.model.ShopDetails;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.ShopDetailsRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.ShopDetailsService;
import com.example.relicv2.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ShopDetailsServiceImple implements ShopDetailsService {
    private ModelMapper modelMapper;
    private UserRepository userRepository;
    private UserAuthenticationHelper userAuthenticationHelper;
    private ShopDetailsRepository shopDetailsRepository;

    @Override
    public String saveDetails(CreateShopDetailsDto createShopDetailsDto) {
        if (createShopDetailsDto == null ||
                StringUtils.isBlank(createShopDetailsDto.getStreetAddress()) ||
                StringUtils.isBlank(createShopDetailsDto.getCity()) ||
                StringUtils.isBlank(createShopDetailsDto.getState()) ||
                StringUtils.isBlank(createShopDetailsDto.getPhonenumber()) ||
                StringUtils.isBlank(createShopDetailsDto.getShopname()) ||
                StringUtils.isBlank(createShopDetailsDto.getEmail()) ||
                StringUtils.isBlank(createShopDetailsDto.getPostalCode()) ||
                StringUtils.isBlank(createShopDetailsDto.getCountry()) )
        {
            throw new CustomException("All fields are required");
        }

        if (userRepository.existsByEmail(createShopDetailsDto.getEmail()) || shopDetailsRepository.existsByEmail(createShopDetailsDto.getEmail())) {
            throw new CustomException("Email already exists");
        }

        if(shopDetailsRepository.existsByShopname(createShopDetailsDto.getShopname())){
            throw new CustomException("Shop name already exists");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        Optional<ShopDetails> optionalShopDetail = shopDetailsRepository.findByUser(user);

        ShopDetails newDetail;

        newDetail = optionalShopDetail.orElseGet(ShopDetails::new);

        newDetail.setStreetAddress(createShopDetailsDto.getStreetAddress());
        newDetail.setCity(createShopDetailsDto.getCity());
        newDetail.setCountry(createShopDetailsDto.getCountry());
        newDetail.setState(createShopDetailsDto.getState());
        newDetail.setEmail(createShopDetailsDto.getEmail());
        newDetail.setPhonenumber(createShopDetailsDto.getPhonenumber());
        newDetail.setPostalCode(createShopDetailsDto.getPostalCode());
        newDetail.setShopname(createShopDetailsDto.getShopname());
        user.setShopname(createShopDetailsDto.getShopname());
        if(!optionalShopDetail.isPresent()){
            newDetail.setUser(user);
        }
        shopDetailsRepository.save(newDetail);
        userRepository.save(user);

        return "Shop details saved succesfully";
    }

    @Override
    public CreateShopDetailsDto getUserShopDetails() {
        User user = userAuthenticationHelper.getLoggedInUser();
        Optional<ShopDetails> shopDetails = shopDetailsRepository.findByUser(user);

        return modelMapper.map(shopDetails.get(), CreateShopDetailsDto.class);
    }
}
