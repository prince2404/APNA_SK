package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.healthcard.HealthCardMemberRequest;
import com.ask.dto.request.healthcard.HealthCardRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.healthcard.HealthCardMemberResponse;
import com.ask.dto.response.healthcard.HealthCardResponse;
import com.ask.entity.*;
import com.ask.enums.EntityStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.HealthCardMapper;
import com.ask.repository.*;
import com.ask.service.HealthCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCardServiceImpl implements HealthCardService {

    private final HealthCardRepository healthCardRepository;
    private final HealthCardMemberRepository healthCardMemberRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final HealthCardMapper healthCardMapper;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureGeographicScope(User user, HealthCard card) {
        String roleName = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName)) {
            return;
        }
        if (user.getState() != null && !card.getStore().getBlock().getDistrict().getState().getId().equals(user.getState().getId())) {
            throw new GeographicScopeException("Health card is outside user's state scope");
        }
        if (user.getDistrict() != null && !card.getStore().getBlock().getDistrict().getId().equals(user.getDistrict().getId())) {
            throw new GeographicScopeException("Health card is outside user's district scope");
        }
        if (user.getBlock() != null && !card.getStore().getBlock().getId().equals(user.getBlock().getId())) {
            throw new GeographicScopeException("Health card is outside user's block scope");
        }
        if (user.getStore() != null && !card.getStore().getId().equals(user.getStore().getId())) {
            throw new GeographicScopeException("Health card is outside user's store scope");
        }
    }

    private void ensurePatientScope(User user, Patient patient) {
        String roleName = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName)) {
            return;
        }
        if (user.getState() != null && !patient.getState().getId().equals(user.getState().getId())) {
            throw new GeographicScopeException("Patient is outside user's state scope");
        }
        if (user.getDistrict() != null && !patient.getDistrict().getId().equals(user.getDistrict().getId())) {
            throw new GeographicScopeException("Patient is outside user's district scope");
        }
        if (user.getBlock() != null && !patient.getBlock().getId().equals(user.getBlock().getId())) {
            throw new GeographicScopeException("Patient is outside user's block scope");
        }
        if (user.getStore() != null && (patient.getStore() == null || !patient.getStore().getId().equals(user.getStore().getId()))) {
            throw new GeographicScopeException("Patient is outside user's store scope");
        }
    }

    @Override
    @Transactional
    public HealthCardResponse issueHealthCard(HealthCardRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        ensurePatientScope(currentUser, patient);

        if (healthCardRepository.findByPatientId(request.getPatientId()).isPresent()) {
            throw new BusinessRuleException("A health card has already been issued for this patient");
        }

        Store store = currentUser.getStore();
        if (store == null) {
            store = patient.getStore();
        }
        if (store == null) {
            throw new BusinessRuleException("A store context is required to issue a health card.");
        }

        String cardNumber = String.format("HC-%s-%d", store.getCode(), System.currentTimeMillis());

        HealthCard card = HealthCard.builder()
                .cardNumber(cardNumber)
                .patient(patient)
                .store(store)
                .issuedBy(currentUser)
                .status(EntityStatus.ACTIVE)
                .build();

        HealthCard saved = healthCardRepository.save(card);
        HealthCardResponse response = healthCardMapper.toHealthCardResponse(saved);
        response.setMembers(new ArrayList<>());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthCardResponse getHealthCardByNumber(String cardNumber, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        HealthCard card = healthCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("HealthCard", "cardNumber", cardNumber));

        ensureGeographicScope(currentUser, card);

        HealthCardResponse response = healthCardMapper.toHealthCardResponse(card);
        List<HealthCardMember> members = healthCardMemberRepository.findByHealthCardId(card.getId());
        response.setMembers(members.stream().map(healthCardMapper::toMemberResponse).toList());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthCardResponse getHealthCardByPatientId(Long patientId, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        HealthCard card = healthCardRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthCard", "patientId", patientId));

        ensureGeographicScope(currentUser, card);

        HealthCardResponse response = healthCardMapper.toHealthCardResponse(card);
        List<HealthCardMember> members = healthCardMemberRepository.findByHealthCardId(card.getId());
        response.setMembers(members.stream().map(healthCardMapper::toMemberResponse).toList());
        return response;
    }

    @Override
    @Transactional
    public HealthCardResponse addFamilyMember(Long cardId, HealthCardMemberRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        HealthCard card = healthCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthCard", "id", cardId));

        ensureGeographicScope(currentUser, card);

        long currentCount = healthCardMemberRepository.countByHealthCardId(cardId);
        if (currentCount >= 5) {
            throw new BusinessRuleException("A health card can have at most 5 family members");
        }

        HealthCardMember member = HealthCardMember.builder()
                .healthCard(card)
                .name(request.getName())
                .relation(request.getRelation())
                .age(request.getAge())
                .gender(request.getGender())
                .build();

        healthCardMemberRepository.save(member);

        HealthCardResponse response = healthCardMapper.toHealthCardResponse(card);
        List<HealthCardMember> members = healthCardMemberRepository.findByHealthCardId(card.getId());
        response.setMembers(members.stream().map(healthCardMapper::toMemberResponse).toList());
        return response;
    }

    @Override
    @Transactional
    public void removeFamilyMember(Long cardId, Long memberId, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        HealthCard card = healthCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthCard", "id", cardId));

        ensureGeographicScope(currentUser, card);

        HealthCardMember member = healthCardMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthCardMember", "id", memberId));

        if (!member.getHealthCard().getId().equals(cardId)) {
            throw new BusinessRuleException("Member does not belong to the selected health card");
        }

        healthCardMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthCardResponse> getHealthCards(int page, int size, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        String role = currentUser.getRole().getName();
        
        Long sId = null;
        Long dId = null;
        Long bId = null;
        Long stId = null;

        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            // no filters
        } else if (RoleConstants.STATE_ADMIN.equals(role)) {
            sId = currentUser.getState().getId();
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            dId = currentUser.getDistrict().getId();
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            bId = currentUser.getBlock().getId();
        } else {
            stId = currentUser.getStore().getId();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("issuedAt").descending());
        
        final Long finalStateId = sId;
        final Long finalDistrictId = dId;
        final Long finalBlockId = bId;
        final Long finalStoreId = stId;

        Specification<HealthCard> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalStoreId != null) {
                predicates.add(cb.equal(root.get("store").get("id"), finalStoreId));
            }
            if (finalBlockId != null) {
                predicates.add(cb.equal(root.get("store").get("block").get("id"), finalBlockId));
            }
            if (finalDistrictId != null) {
                predicates.add(cb.equal(root.get("store").get("block").get("district").get("id"), finalDistrictId));
            }
            if (finalStateId != null) {
                predicates.add(cb.equal(root.get("store").get("block").get("district").get("state").get("id"), finalStateId));
            }
            predicates.add(cb.equal(root.get("status"), EntityStatus.ACTIVE));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<HealthCard> cardPage = healthCardRepository.findAll(spec, pageable);
        List<HealthCardResponse> content = cardPage.getContent().stream()
                .map(card -> {
                    HealthCardResponse res = healthCardMapper.toHealthCardResponse(card);
                    List<HealthCardMember> members = healthCardMemberRepository.findByHealthCardId(card.getId());
                    res.setMembers(members.stream().map(healthCardMapper::toMemberResponse).toList());
                    return res;
                })
                .toList();

        return PageResponse.of(cardPage, content);
    }
}
