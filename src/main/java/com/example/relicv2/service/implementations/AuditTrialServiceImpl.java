package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.AuditDto;
import com.example.relicv2.model.AuditTrial;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.AuditTrialRepository;
import com.example.relicv2.service.AuditTrialService;
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
public class AuditTrialServiceImpl implements AuditTrialService {
    private final AuditTrialRepository auditTrialRepository;
    private final ModelMapper modelMapper;

    public AuditTrialServiceImpl(AuditTrialRepository auditTrialRepository, ModelMapper modelMapper) {
        this.auditTrialRepository = auditTrialRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<AuditDto> getAllAuditTrials(
            Long userId,
            String action,
            String audit,
            String startDate,
            String endDate,
            String sortBy
    ) {
        List<AuditTrial> auditTrials = auditTrialRepository.findAll();

        if (userId != null) {
            auditTrials = auditTrials.stream()
                    .filter(auditTrial -> auditTrial.getUser().getId().equals(userId))
                    .collect(Collectors.toList());
        }

        if (action != null) {
            action = action.toLowerCase();
            String finalAction = action;
            auditTrials = auditTrials.stream()
                    .filter(auditTrial -> auditTrial.getAction().toLowerCase().contains(finalAction))
                    .collect(Collectors.toList());
        }

        if (audit != null) {
            audit = audit.toLowerCase();
            String finalAudit = audit;
            auditTrials = auditTrials.stream()
                    .filter(auditTrial -> auditTrial.getAudit().toLowerCase().contains(finalAudit))
                    .collect(Collectors.toList());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            if (startDate != null && endDate != null) {
                Date startDateNew = dateFormat.parse(startDate);
                Date endDateNew = dateFormat.parse(endDate);
                auditTrials = auditTrials.stream()
                        .filter(auditTrial -> {
                            return auditTrial.getTimestamp().after(startDateNew) && auditTrial.getTimestamp().before(endDateNew);
                        })
                        .collect(Collectors.toList());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (sortBy) {
            case "Newest":
                auditTrials.sort(Comparator.comparing(AuditTrial::getTimestamp).reversed());
                break;
            case "Oldest":
                auditTrials.sort(Comparator.comparing(AuditTrial::getTimestamp));
                break;
            default:
                auditTrials.sort(Comparator.comparing(AuditTrial::getTimestamp).reversed());
                break;
        }

        return auditTrials.stream()
                .map(auditTrial -> modelMapper.map(auditTrial, AuditDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void addToAuditTrial(User user, String action, String audit) {
        AuditTrial auditTrial = new AuditTrial();
        auditTrial.setUser(user);
        auditTrial.setAction(action);
        auditTrial.setAudit(audit);
        auditTrialRepository.save(auditTrial);
    }
}
