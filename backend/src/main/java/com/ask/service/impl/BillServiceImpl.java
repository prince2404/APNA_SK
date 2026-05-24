package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.billing.BillItemRequest;
import com.ask.dto.request.billing.BillRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.billing.BillResponse;
import com.ask.entity.*;
import com.ask.enums.*;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.BillMapper;
import com.ask.repository.*;
import com.ask.service.BillService;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PatientRepository patientRepository;
    private final HealthCardRepository healthCardRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockStoreRepository stockStoreRepository;
    private final SchemeRepository schemeRepository;
    private final RoleRepository roleRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionEntryRepository commissionEntryRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final BillMapper billMapper;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureGeographicScope(User user, Bill bill) {
        String roleName = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName)) {
            return;
        }
        Store store = bill.getStore();
        if (user.getState() != null && !store.getBlock().getDistrict().getState().getId().equals(user.getState().getId())) {
            throw new GeographicScopeException("Bill is outside user's state scope");
        }
        if (user.getDistrict() != null && !store.getBlock().getDistrict().getId().equals(user.getDistrict().getId())) {
            throw new GeographicScopeException("Bill is outside user's district scope");
        }
        if (user.getBlock() != null && !store.getBlock().getId().equals(user.getBlock().getId())) {
            throw new GeographicScopeException("Bill is outside user's block scope");
        }
        if (user.getStore() != null && !store.getId().equals(user.getStore().getId())) {
            throw new GeographicScopeException("Bill is outside user's store scope");
        }
    }

    private void validateStoreScope(User user, Long requestedStoreId) {
        if (requestedStoreId == null) return;
        Store store = storeRepository.findById(requestedStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", requestedStoreId));
        String role = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            return;
        }
        if (user.getState() != null && !store.getBlock().getDistrict().getState().getId().equals(user.getState().getId())) {
            throw new GeographicScopeException("Store is outside your state scope");
        }
        if (user.getDistrict() != null && !store.getBlock().getDistrict().getId().equals(user.getDistrict().getId())) {
            throw new GeographicScopeException("Store is outside your district scope");
        }
        if (user.getBlock() != null && !store.getBlock().getId().equals(user.getBlock().getId())) {
            throw new GeographicScopeException("Store is outside your block scope");
        }
        if (user.getStore() != null && !store.getId().equals(user.getStore().getId())) {
            throw new GeographicScopeException("Store is outside your store scope");
        }
    }

    private int getReturnWindowDays() {
        return systemConfigRepository.findByConfigKey("RETURN_WINDOW_DAYS")
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return 7;
                    }
                })
                .orElse(7);
    }

    @Override
    @Transactional
    public synchronized BillResponse createBill(BillRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Store store = currentUser.getStore();
        if (store == null) {
            throw new BusinessRuleException("You must be assigned to a store to create bills.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        Optional<HealthCard> healthCardOpt = healthCardRepository.findByPatientId(patient.getId());

        // Daily bill sequence counting
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayBillCount = billRepository.countByStoreIdAndCreatedAtAfter(store.getId(), startOfDay);
        long nextSeq = todayBillCount + 1;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String billNumber = String.format("ASK-%s-%s-%04d", store.getCode(), dateStr, nextSeq);

        BigDecimal totalMrp = BigDecimal.ZERO;
        BigDecimal totalAskPrice = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal netAmount = BigDecimal.ZERO;

        List<Scheme> activeSchemes = schemeRepository.findActiveSchemesAtDate(EntityStatus.ACTIVE, LocalDate.now());
        List<BillItem> billItems = new ArrayList<>();

        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .store(store)
                .patient(patient)
                .healthCard(healthCardOpt.orElse(null))
                .paymentMode(request.getPaymentMode())
                .status(BillStatus.ACTIVE)
                .createdBy(currentUser)
                .billDate(LocalDateTime.now())
                .build();

        for (BillItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            StockStore stockStore = stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(
                    store.getId(), product.getId(), itemReq.getBatchNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("StockStore", "product/batch", 
                            product.getId() + "/" + itemReq.getBatchNumber()));

            if (stockStore.getQuantity() < itemReq.getQuantity()) {
                throw new BusinessRuleException("Insufficient stock for product: " + product.getName() 
                        + " in batch: " + itemReq.getBatchNumber() + ". Available: " + stockStore.getQuantity());
            }

            // Deduct stock
            stockStore.setQuantity(stockStore.getQuantity() - itemReq.getQuantity());
            stockStoreRepository.save(stockStore);

            // Apply best scheme
            BigDecimal maxUnitDiscount = BigDecimal.ZERO;
            for (Scheme scheme : activeSchemes) {
                boolean categoryMatch = scheme.getCategory() == null || scheme.getCategory().getId().equals(product.getCategory().getId());
                boolean stateMatch = scheme.getState() == null || scheme.getState().getId().equals(patient.getState().getId());

                if (categoryMatch && stateMatch) {
                    BigDecimal disc = BigDecimal.ZERO;
                    if (scheme.getDiscountType() == DiscountType.PERCENTAGE) {
                        disc = product.getAskPrice().multiply(scheme.getDiscountValue())
                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    } else if (scheme.getDiscountType() == DiscountType.FLAT) {
                        disc = scheme.getDiscountValue();
                    }
                    if (disc.compareTo(product.getAskPrice()) > 0) {
                        disc = product.getAskPrice();
                    }
                    if (disc.compareTo(maxUnitDiscount) > 0) {
                        maxUnitDiscount = disc;
                    }
                }
            }

            BigDecimal mrp = product.getMrp();
            BigDecimal askPrice = product.getAskPrice();
            BigDecimal discountAmount = maxUnitDiscount.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            BigDecimal subtotal = askPrice.subtract(maxUnitDiscount).multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            // Extract GST (Inclusive GST)
            BigDecimal gstPercentage = product.getGstPercentage();
            BigDecimal gstAmount = subtotal.multiply(gstPercentage)
                    .divide(BigDecimal.valueOf(100).add(gstPercentage), 2, java.math.RoundingMode.HALF_UP);

            totalMrp = totalMrp.add(mrp.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            totalAskPrice = totalAskPrice.add(askPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            totalDiscount = totalDiscount.add(discountAmount);
            totalGst = totalGst.add(gstAmount);
            netAmount = netAmount.add(subtotal);

            BillItem billItem = BillItem.builder()
                    .bill(bill)
                    .product(product)
                    .batchNumber(itemReq.getBatchNumber())
                    .quantity(itemReq.getQuantity())
                    .mrp(mrp)
                    .askPrice(askPrice)
                    .gstAmount(gstAmount)
                    .discountAmount(discountAmount)
                    .subtotal(subtotal)
                    .returnStatus(ReturnStatus.NONE)
                    .returnQuantity(0)
                    .build();

            billItems.add(billItem);
        }

        bill.setItems(billItems);
        bill.setTotalMrp(totalMrp);
        bill.setTotalAskPrice(totalAskPrice);
        bill.setTotalGst(totalGst);
        bill.setTotalDiscount(totalDiscount);
        bill.setNetAmount(netAmount);
        bill.setTotalSavings(totalMrp.subtract(netAmount));

        Bill saved = billRepository.save(bill);

        // Generate Upward Commission Logs
        calculateAndSaveCommissions(saved, netAmount);

        return billMapper.toBillResponse(saved);
    }

    private void calculateAndSaveCommissions(Bill bill, BigDecimal netAmount) {
        Store store = bill.getStore();
        Block block = store.getBlock();
        District district = block.getDistrict();
        State state = district.getState();

        createLevelCommissions(bill, RoleConstants.BLOCK_ADMIN, block.getId(), "block", netAmount);
        createLevelCommissions(bill, RoleConstants.DISTRICT_ADMIN, district.getId(), "district", netAmount);
        createLevelCommissions(bill, RoleConstants.STATE_ADMIN, state.getId(), "state", netAmount);
        createLevelCommissions(bill, RoleConstants.SUPER_ADMIN, null, "super", netAmount);
    }

    private void createLevelCommissions(Bill bill, String roleName, Long scopeId, String scopeType, BigDecimal netAmount) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        Optional<CommissionConfig> configOpt = commissionConfigRepository.findByRoleId(role.getId());
        if (configOpt.isEmpty()) {
            return;
        }
        CommissionConfig config = configOpt.get();
        BigDecimal percentage = config.getPercentage();
        if (percentage.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Find active users matching the role and scope
        List<User> eligibleUsers = userRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("role").get("id"), role.getId()));
            predicates.add(cb.equal(root.get("status"), UserStatus.ACTIVE));

            if ("block".equals(scopeType)) {
                predicates.add(cb.equal(root.get("block").get("id"), scopeId));
            } else if ("district".equals(scopeType)) {
                predicates.add(cb.equal(root.get("district").get("id"), scopeId));
            } else if ("state".equals(scopeType)) {
                predicates.add(cb.equal(root.get("state").get("id"), scopeId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        if (eligibleUsers.isEmpty()) {
            return;
        }

        BigDecimal totalLevelCommission = netAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal individualCommission = totalLevelCommission.divide(BigDecimal.valueOf(eligibleUsers.size()), 2, java.math.RoundingMode.HALF_UP);

        String month = bill.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (User u : eligibleUsers) {
            CommissionEntry entry = CommissionEntry.builder()
                    .bill(bill)
                    .user(u)
                    .role(role)
                    .amount(individualCommission)
                    .month(month)
                    .status(CommissionStatus.CALCULATED)
                    .build();
            commissionEntryRepository.save(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByNumber(String billNumber, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "billNumber", billNumber));

        ensureGeographicScope(currentUser, bill);
        return billMapper.toBillResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        ensureGeographicScope(currentUser, bill);
        return billMapper.toBillResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BillResponse> getBills(Long storeId, int page, int size, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        validateStoreScope(currentUser, storeId);

        String role = currentUser.getRole().getName();
        Long sId = null;
        Long dId = null;
        Long bId = null;
        Long stId = null;

        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            stId = storeId;
        } else if (RoleConstants.STATE_ADMIN.equals(role)) {
            sId = currentUser.getState().getId();
            stId = storeId;
        } else if (RoleConstants.DISTRICT_ADMIN.equals(role)) {
            dId = currentUser.getDistrict().getId();
            stId = storeId;
        } else if (RoleConstants.BLOCK_ADMIN.equals(role)) {
            bId = currentUser.getBlock().getId();
            stId = storeId;
        } else {
            stId = currentUser.getStore().getId();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("billDate").descending());

        final Long finalStateId = sId;
        final Long finalDistrictId = dId;
        final Long finalBlockId = bId;
        final Long finalStoreId = stId;

        Specification<Bill> spec = (root, query, cb) -> {
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Bill> billPage = billRepository.findAll(spec, pageable);
        List<BillResponse> content = billPage.getContent().stream()
                .map(billMapper::toBillResponse)
                .toList();

        return PageResponse.of(billPage, content);
    }

    @Override
    @Transactional
    public BillResponse cancelBill(Long id, String cancelReason, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        ensureGeographicScope(currentUser, bill);

        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessRuleException("This bill is already cancelled");
        }

        int returnWindow = getReturnWindowDays();
        if (bill.getBillDate().plusDays(returnWindow).isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Return window of " + returnWindow + " days has expired for this bill.");
        }

        // Restore stock
        for (BillItem item : bill.getItems()) {
            StockStore stockStore = stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(
                    bill.getStore().getId(), item.getProduct().getId(), item.getBatchNumber())
                    .orElseGet(() -> StockStore.builder()
                            .store(bill.getStore())
                            .product(item.getProduct())
                            .batchNumber(item.getBatchNumber())
                            .quantity(0)
                            .expiryDate(LocalDate.now().plusYears(1))
                            .build());

            stockStore.setQuantity(stockStore.getQuantity() + item.getQuantity());
            stockStoreRepository.save(stockStore);
        }

        // Cancel commissions
        List<CommissionEntry> commissions = commissionEntryRepository.findByBillId(bill.getId());
        for (CommissionEntry entry : commissions) {
            entry.setStatus(CommissionStatus.CANCELLED);
            commissionEntryRepository.save(entry);
        }

        bill.setStatus(BillStatus.CANCELLED);
        bill.setCancelReason(cancelReason);
        bill.setCancelledBy(currentUser);

        Bill saved = billRepository.save(bill);
        return billMapper.toBillResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateBillPdf(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        ensureGeographicScope(currentUser, bill);

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         APNA SWASTHYA KENDRA            \n");
        sb.append("=========================================\n");
        sb.append("Bill Number : ").append(bill.getBillNumber()).append("\n");
        sb.append("Date        : ").append(bill.getBillDate().toString()).append("\n");
        sb.append("Store       : ").append(bill.getStore().getName()).append("\n");
        sb.append("Store Phone : ").append(bill.getStore().getPhone() != null ? bill.getStore().getPhone() : "N/A").append("\n");
        sb.append("Patient     : ").append(bill.getPatient().getFullName()).append("\n");
        if (bill.getHealthCard() != null) {
            sb.append("Health Card : ").append(bill.getHealthCard().getCardNumber()).append("\n");
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-20s %3s %8s %8s\n", "Item Name", "Qty", "Price", "Subtotal"));
        sb.append("-----------------------------------------\n");
        for (BillItem item : bill.getItems()) {
            String name = item.getProduct().getName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            BigDecimal unitPrice = item.getSubtotal().divide(BigDecimal.valueOf(item.getQuantity()), 2, java.math.RoundingMode.HALF_UP);
            sb.append(String.format("%-20s %3d %8.2f %8.2f\n", 
                    name, 
                    item.getQuantity(), 
                    unitPrice, 
                    item.getSubtotal()));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-32s: %8.2f\n", "Total MRP", bill.getTotalMrp()));
        sb.append(String.format("%-32s: %8.2f\n", "Total Ask Price", bill.getTotalAskPrice()));
        sb.append(String.format("%-32s: %8.2f\n", "Total Discount", bill.getTotalDiscount()));
        sb.append(String.format("%-32s: %8.2f\n", "GST Amount (Included)", bill.getTotalGst()));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-32s: %8.2f\n", "NET AMOUNT", bill.getNetAmount()));
        sb.append(String.format("%-32s: %8.2f\n", "TOTAL SAVINGS", bill.getTotalSavings()));
        sb.append("-----------------------------------------\n");
        sb.append("Payment Mode: ").append(bill.getPaymentMode()).append("\n");
        sb.append("Status      : ").append(bill.getStatus()).append("\n");
        sb.append("=========================================\n");
        sb.append("        Thank you for visiting!          \n");
        sb.append("=========================================\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
