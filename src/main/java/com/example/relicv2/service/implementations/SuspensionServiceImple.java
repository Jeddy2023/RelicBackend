package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.GetSuspensionDto;
import com.example.relicv2.model.Suspension;
import com.example.relicv2.repository.SuspensionRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.SuspensionService;
import com.example.relicv2.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SuspensionServiceImple implements SuspensionService {
    private ModelMapper modelMapper;
    private UserAuthenticationHelper userAuthenticationHelper;
    private final UserRepository userRepository;
    private final SuspensionRepository suspensionRepository;

    @Override
    public List<GetSuspensionDto> getSuspensionsByUser(Long userId) {
        List<Suspension> suspensions = suspensionRepository.findByUserId(userId);
        return suspensions.stream()
                .map(suspension -> modelMapper.map(suspension, GetSuspensionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<GetSuspensionDto> getAllSuspensions(String startDate, String endDate, Boolean suspended, String sortBy) {
        List<Suspension> suspensions = suspensionRepository.findAll();

        try {
            if (startDate != null && endDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
                Date startDateNew = dateFormat.parse(startDate);
                Date endDateNew = dateFormat.parse(endDate);
                suspensions = suspensions.stream()
                        .filter(suspension ->
                                !suspension.getCreatedAt().before(startDateNew) &&
                                        !suspension.getCreatedAt().after(endDateNew))
                        .collect(Collectors.toList());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (suspended != null) {
            suspensions = suspensions.stream()
                    .filter(suspension -> suspension.isSuspended() == suspended)
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "dateAsc":
                suspensions.sort(Comparator.comparing(Suspension::getCreatedAt));
                break;
            case "dateDesc":
                suspensions.sort(Comparator.comparing(Suspension::getCreatedAt).reversed());
                break;
        }

        return suspensions.stream()
                .map(suspension -> modelMapper.map(suspension, GetSuspensionDto.class))
                .collect(Collectors.toList());
    }
}
