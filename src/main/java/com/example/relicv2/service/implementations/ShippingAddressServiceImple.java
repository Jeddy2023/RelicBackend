package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.CreateShippingAddressDTO;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.model.ShippingAddress;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.ShippingAddressRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.ShippingAddressService;
import com.example.relicv2.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ShippingAddressServiceImple implements ShippingAddressService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final UserAuthenticationHelper userAuthenticationHelper;

    @Override
    public String saveAddress(CreateShippingAddressDTO createShippingAddressDTO) {
        User user = userAuthenticationHelper.getLoggedInUser();
        Optional<ShippingAddress> optionalShippingAddress = shippingAddressRepository.findByUser(user);

        ShippingAddress newAddress = optionalShippingAddress.orElseGet(ShippingAddress::new);

        newAddress.setStreetAddress(createShippingAddressDTO.getStreetAddress());
        newAddress.setCity(createShippingAddressDTO.getCity());
        newAddress.setPostalCode(createShippingAddressDTO.getPostalCode());
        newAddress.setState(createShippingAddressDTO.getState());
        newAddress.setFirstname(createShippingAddressDTO.getFirstname());
        newAddress.setLastname(createShippingAddressDTO.getLastname());

        if(optionalShippingAddress.isEmpty()){
            newAddress.setUser(user);
        }

        shippingAddressRepository.save(newAddress);

        return "Shipping address saved successfully";
    }

    @Override
    public CreateShippingAddressDTO getUserShippingAddress() {
        User user = userAuthenticationHelper.getLoggedInUser();
        Optional<ShippingAddress> shippingAddress = shippingAddressRepository.findByUser(user);

        return shippingAddress.map(address -> modelMapper.map(address, CreateShippingAddressDTO.class))
                .orElseThrow(() -> new CustomException("Shipping address not found"));
    }

    @Override
    public CreateShippingAddressDTO getUserShippingAddressById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        ShippingAddress shippingAddress = shippingAddressRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Shipping address not found"));

        return modelMapper.map(shippingAddress, CreateShippingAddressDTO.class);
    }
}
