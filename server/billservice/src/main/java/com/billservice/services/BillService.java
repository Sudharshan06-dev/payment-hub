
package com.billservice.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billservice.dto.BillStatsDto;
import com.billservice.dto.Billdto;
import com.billservice.models.Bills;
import com.billservice.models.Bills.BillStatus;
import com.billservice.repositories.BillsRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BillService {

    @Autowired
    private BillsRepository billsRepository;

    /**
     * Get all bills for a user (excluding deleted)
     */
    public ArrayList<Bills> getAllBills(Long userId) {

        try {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            
            List<Bills> bills = billsRepository.findAllByUserIdNotDeleted(userId);
            return new ArrayList<>(bills);

        } catch (Exception e) {
            log.error("Error fetching bills for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve bills for user: " + userId);
        }
    }

    /**
     * Get bills by status for a user
     */
    public ArrayList<Bills> getBillsByStatus(Long userId, BillStatus status) {

        try {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            if (status == null) {
                throw new IllegalArgumentException("Bill status cannot be null");
            }
            
            List<Bills> bills = billsRepository.findByUserIdAndStatus(userId, status);
            return new ArrayList<>(bills);
        } catch (Exception e) {
            log.error("Error fetching bills by status for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve bills with status: " + status);
        }
    }

    /**
     * Get overdue bills for a user
     */
    public ArrayList<Bills> getOverdueBills(Long userId) {

        try {
            List<Bills> bills = billsRepository.findOverdueBills(userId, BillStatus.OVERDUE);
            return new ArrayList<>(bills);
        } catch (Exception e) {
            log.error("Error fetching overdue bills for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve overdue bills");
        }
    }

    /**
     * Get bills due within X days
     */
    public ArrayList<Bills> getBillsDueWithinDays(Long userId, int days) {

        try {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(days);
            List<Bills> bills = billsRepository.findBillsDueWithin(userId, dueDate);
            return new ArrayList<>(bills);
        } catch (Exception e) {
            log.error("Error fetching bills due within days for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve bills due within days");
        }
    }

    /**
     * Get bill statistics for dashboard
     */
    public BillStatsDto getBillStatistics(Long userId) {

        try {

            ArrayList<Bills> allBills = getAllBills(userId);

            long totalBills = allBills.size();
            long pendingCount = billsRepository.countByUserIdAndStatus(userId, BillStatus.PENDING);
            long paidCount = billsRepository.countByUserIdAndStatus(userId, BillStatus.PAID);
            long overdueCount = billsRepository.countByUserIdAndStatus(userId, BillStatus.OVERDUE);

            long dueCount = allBills.stream().filter(b -> b.getBillStatus() == BillStatus.PENDING).filter(b -> b.getDueDate().isBefore(LocalDateTime.now().plusDays(30))).count();

            Double totalAmount = billsRepository.getTotalAmountOwed(userId);

            return BillStatsDto.builder()
            .total(totalBills)
            .pending(pendingCount)
            .paid(paidCount)
            .overdue(overdueCount)
            .due(dueCount)
            .totalAmount(totalAmount)
            .bills(allBills)
            .build();
            
        } catch(Exception e) {
            log.error("Error fetching bill statistics for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve bill statistics");
        }
    }

    /**
     * Get a specific bill by ID
     */
    public Bills getBillById(Long userId, Long billId) {
        try {
            Optional<Bills> bill = billsRepository.findByIdAndUserId(billId, userId);
            if (bill.isEmpty()) {
                throw new RuntimeException("Bill not found with ID: " + billId);
            }
            return bill.get();
        }catch (Exception e) {
            log.error("Error fetching bill {} for user {}: {}", billId, userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve bill");
        }
    }

    /**
     * Create a new bill
     */
    public Bills createBill(Long userId, Bills bill) {

        try {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            
            // Validate bill data
            if (bill.getBillerName() == null || bill.getBillerName().isEmpty()) {
                throw new IllegalArgumentException("Biller name is required");
            }
            if (bill.getAmount() == null || bill.getAmount() <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }
            if (bill.getDueDate() == null) {
                throw new IllegalArgumentException("Due date is required");
            }
            if (bill.getBillStatus() == null) {
                bill.setBillStatus(BillStatus.PENDING);
            }
            
            bill.setUserId(userId);
            bill.setIsDeleted(false);
            
            Bills savedBill = billsRepository.save(bill);
            log.info("Bill created successfully with ID: {}", savedBill.getBillId());
            return savedBill;
        } catch (Exception e) {
            log.error("Error creating bill for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to create bill: " + e.getMessage());
        }
    }

    /**
     * Update an existing bill
     */
    public Bills updateBill(Long userId, Long billId, Bills billDetails) {

        try {
            Bills bill = getBillById(userId, billId);
            
            // Update fields only if provided
            if (billDetails.getBillerName() != null && !billDetails.getBillerName().isEmpty()) {
                bill.setBillerName(billDetails.getBillerName());
            }
            if (billDetails.getAmount() != null && billDetails.getAmount() > 0) {
                bill.setAmount(billDetails.getAmount());
            }
            if (billDetails.getDueDate() != null) {
                bill.setDueDate(billDetails.getDueDate());
            }
            if (billDetails.getBillStatus() != null) {
                bill.setBillStatus(billDetails.getBillStatus());
            }
            if (billDetails.getBillFrequency() != null) {
                bill.setBillFrequency(billDetails.getBillFrequency());
            }
            if (billDetails.getAccountNumber() != null && !billDetails.getAccountNumber().isEmpty()) {
                bill.setAccountNumber(billDetails.getAccountNumber());
            }
            
            Bills updatedBill = billsRepository.save(bill);
            log.info("Bill {} updated successfully", billId);
            return updatedBill;
        } catch (Exception e) {
            log.error("Error updating bill {} for user {}: {}", billId, userId, e.getMessage());
            throw new RuntimeException("Failed to update bill: " + e.getMessage());
        }
    }

    /**
     * Mark bill as paid
     */
    public Bills markBillAsPaid(Long userId, Long billId) {
        log.info("Marking bill {} as paid for user: {}", billId, userId);
        try {
            Bills bill = getBillById(userId, billId);
            bill.setBillStatus(BillStatus.PAID);
            Bills paidBill = billsRepository.save(bill);
            log.info("Bill {} marked as paid successfully", billId);
            return paidBill;
        } catch (Exception e) {
            log.error("Error marking bill {} as paid for user {}: {}", billId, userId, e.getMessage());
            throw new RuntimeException("Failed to mark bill as paid: " + e.getMessage());
        }
    }

    /**
     * Delete bill (soft delete)
     */
    public void deleteBill(Long userId, Long billId) {
        log.info("Deleting bill {} for user: {}", billId, userId);
        try {
            // Verify bill exists and belongs to user
            Bills bill = getBillById(userId, billId);
            
            bill.setIsDeleted(true);
            billsRepository.save(bill);
            log.info("Bill {} deleted successfully", billId);
        } catch (Exception e) {
            log.error("Error deleting bill {} for user {}: {}", billId, userId, e.getMessage());
            throw new RuntimeException("Failed to delete bill: " + e.getMessage());
        }
    }

    /**
     * Search bills by biller name
     */
    public ArrayList<Bills> searchBills(Long userId, String searchTerm) {
        log.info("Searching bills for user: {} with term: {}", userId, searchTerm);
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllBills(userId);
            }
            
            List<Bills> bills = billsRepository.findByUserIdAndBillerNameContaining(userId, searchTerm);
            return new ArrayList<>(bills);
        } catch (Exception e) {
            log.error("Error searching bills for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to search bills");
        }
    }

    /**
     * Get total amount owed by user
     */
    public Double getTotalAmountOwed(Long userId) {
        try {
            Double total = billsRepository.getTotalAmountOwed(userId);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            log.error("Error fetching total amount owed for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve total amount owed");
        }
    }

    /**
     * Convert Bill to BillDTO (for API responses)
     */
    public Billdto convertToDTO(Bills bill) {
        return Billdto.builder()
            .billId(bill.getBillId())
            .userId(bill.getUserId())
            .billerName(bill.getBillerName())
            .accountNumber(bill.getAccountNumber())
            .amount(bill.getAmount())
            .currency(bill.getCurrency())
            .dueDate(bill.getDueDate())
            .billStatus(bill.getBillStatus())
            .billFrequency(bill.getBillFrequency())
            .createdAt(bill.getCreatedAt())
            .updatedAt(bill.getUpdatedAt())
            .build();
    }

    /**
     * Convert list of Bills to DTOs
     */
    public ArrayList<Billdto> convertToDTOList(ArrayList<Bills> bills) {
        return bills.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}