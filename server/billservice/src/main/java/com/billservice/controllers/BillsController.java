package com.billservice.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billservice.dto.BillStatsDto;
import com.billservice.dto.Billdto;
import com.billservice.models.Bills;
import com.billservice.models.Bills.BillStatus;
import com.billservice.services.BillService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bills")
@Slf4j
public class BillsController {

    @Autowired
    private BillService billService;

    /**
     * GET /api/v1/bills/{userId}
     * Get all bills for a user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getAllBills(@PathVariable Long userId) {
        try {
            
            ArrayList<Bills> bills = billService.getAllBills(userId);
            ArrayList<Billdto> billDTOs = billService.convertToDTOList(bills);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bills retrieved successfully");
            response.put("data", billDTOs);
            response.put("count", billDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving bills for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/statistics
     * Get bill statistics (for dashboard)
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<?> getBillStatistics(@PathVariable Long userId) {
        try {
            
            BillStatsDto stats = billService.getBillStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bill statistics retrieved successfully");
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving bill statistics for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/status/{status}
     * Get bills by status
     */
    @GetMapping("/{userId}/status/{status}")
    public ResponseEntity<?> getBillsByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        try {
            
            BillStatus billStatus = BillStatus.valueOf(status.toUpperCase());
            ArrayList<Bills> bills = billService.getBillsByStatus(userId, billStatus);
            ArrayList<Billdto> billDTOs = billService.convertToDTOList(bills);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bills with status " + status + " retrieved successfully");
            response.put("data", billDTOs);
            response.put("count", billDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid bill status: {}", status);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid bill status: " + status);
        } catch (Exception e) {
            log.error("Error retrieving bills with status {} for user {}: {}", status, userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/overdue
     * Get overdue bills
     */
    @GetMapping("/{userId}/overdue")
    public ResponseEntity<?> getOverdueBills(@PathVariable Long userId) {
        try {
            
            ArrayList<Bills> bills = billService.getOverdueBills(userId);
            ArrayList<Billdto> billDTOs = billService.convertToDTOList(bills);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Overdue bills retrieved successfully");
            response.put("data", billDTOs);
            response.put("count", billDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving overdue bills for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/due?days=30
     * Get bills due within X days
     */
    @GetMapping("/{userId}/due")
    public ResponseEntity<?> getBillsDueWithin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            
            ArrayList<Bills> bills = billService.getBillsDueWithinDays(userId, days);
            ArrayList<Billdto> billDTOs = billService.convertToDTOList(bills);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bills due within " + days + " days retrieved successfully");
            response.put("data", billDTOs);
            response.put("count", billDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving bills due within {} days for user {}: {}", days, userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/bill/{billId}
     * Get a specific bill
     */
    @GetMapping("/{userId}/bill/{billId}")
    public ResponseEntity<?> getBillById(
            @PathVariable Long userId,
            @PathVariable Long billId) {
        try {
            log.info("Received request to get bill {} for user: {}", billId, userId);
            
            Bills bill = billService.getBillById(userId, billId);
            Billdto billDTO = billService.convertToDTO(bill);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bill retrieved successfully");
            response.put("data", billDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving bill {} for user {}: {}", billId, userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/search?term=Electric
     * Search bills by biller name
     */
    @GetMapping("/{userId}/search")
    public ResponseEntity<?> searchBills(
            @PathVariable Long userId,
            @RequestParam String term) {
        try {
            log.info("Received request to search bills for user: {} with term: {}", userId, term);
            
            ArrayList<Bills> bills = billService.searchBills(userId, term);
            ArrayList<Billdto> billDTOs = billService.convertToDTOList(bills);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Search results retrieved successfully");
            response.put("data", billDTOs);
            response.put("count", billDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching bills for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * GET /api/v1/bills/{userId}/total-owed
     * Get total amount owed
     */
    @GetMapping("/{userId}/total-owed")
    public ResponseEntity<?> getTotalAmountOwed(@PathVariable Long userId) {
        try {
            log.info("Received request to get total amount owed for user: {}", userId);
            
            Double totalAmountOwed = billService.getTotalAmountOwed(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Total amount owed retrieved successfully");
            response.put("data", totalAmountOwed);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving total amount owed for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * POST /api/v1/bills/{userId}
     * Create a new bill
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> createBill(
            @PathVariable Long userId,
            @RequestBody Bills bill) {
        try {
            log.info("Received request to create bill for user: {}", userId);
            
            Bills createdBill = billService.createBill(userId, bill);
            Billdto billDTO = billService.convertToDTO(createdBill);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bill created successfully");
            response.put("data", billDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating bill for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating bill for user {}: {}", userId, e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * PUT /api/v1/bills/{userId}/bill/{billId}
     * Update a bill
     */
    @PutMapping("/{userId}/bill/{billId}")
    public ResponseEntity<?> updateBill(
            @PathVariable Long userId,
            @PathVariable Long billId,
            @RequestBody Bills billDetails) {
        try {
            log.info("Received request to update bill {} for user: {}", billId, userId);
            
            Bills updatedBill = billService.updateBill(userId, billId, billDetails);
            Billdto billDTO = billService.convertToDTO(updatedBill);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bill updated successfully");
            response.put("data", billDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating bill {} for user {}: {}", billId, userId, e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * PUT /api/v1/bills/{userId}/bill/{billId}/pay
     * Mark bill as paid
     */
    @PutMapping("/{userId}/bill/{billId}/pay")
    public ResponseEntity<?> markBillAsPaid(
            @PathVariable Long userId,
            @PathVariable Long billId) {
        try {
            log.info("Received request to mark bill {} as paid for user: {}", billId, userId);
            
            Bills paidBill = billService.markBillAsPaid(userId, billId);
            Billdto billDTO = billService.convertToDTO(paidBill);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bill marked as paid successfully");
            response.put("data", billDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error marking bill {} as paid for user {}: {}", billId, userId, e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/bills/{userId}/bill/{billId}
     * Delete a bill (soft delete)
     */
    @DeleteMapping("/{userId}/bill/{billId}")
    public ResponseEntity<?> deleteBill(
            @PathVariable Long userId,
            @PathVariable Long billId) {
        try {
            log.info("Received request to delete bill {} for user: {}", billId, userId);
            
            billService.deleteBill(userId, billId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bill deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting bill {} for user {}: {}", billId, userId, e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Build error response
     */
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("status", status.value());
        return ResponseEntity.status(status).body(response);
    }
}