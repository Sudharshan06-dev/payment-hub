package com.paymentservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;

import com.paymentservice.dto.UserRequest;
import com.paymentservice.models.Accounts;
import com.paymentservice.repositories.AccountsRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
@Transactional
public class AccountsService {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private WebClient gatewayWebClient;

    private UserRequest userRequest;

    private UserRequest getUserData(Long userId, String token) {
        if (this.userRequest == null) {
            this.userRequest = gatewayWebClient.get()
                    .uri("/api/v1/users/{id}", userId)
                    //.header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            resp -> Mono.error(new RuntimeException("User not found: " + userId)))
                    .bodyToMono(UserRequest.class)
                    .block();
        }
        return this.userRequest;
    }

    public List<Accounts> getAllAccountsByUserId(Long userId, HttpServletRequest request) {
        log.info("Fetching all accounts for user: {}", userId);

        String token = extractToken(request);
        this.getUserData(userId, token);

        if (this.userRequest == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        return accountsRepository.findAllByUserId(userId);
    }

    public Accounts getAccountById(Long userId, Long accountId) {
        log.info("Fetching account {} for user: {}", accountId, userId);

        return accountsRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));
    }

    public List<Accounts> getActiveAccounts(Long userId) {
        log.info("Fetching active accounts for user: {}", userId);
        return accountsRepository.findActiveAccountsByUserId(userId);
    }

    public Accounts createAccount(Long userId, Accounts account, HttpServletRequest request) {
        log.info("Creating account for user: {}", userId);

        String token = extractToken(request);
        this.getUserData(userId, token);

        if (this.userRequest == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        // Validation: Account Holder Name
        if (account.getAccountHolderName() == null || account.getAccountHolderName().trim().isEmpty()) {
            throw new RuntimeException("Account holder name is required");
        }
        if (account.getAccountHolderName().length() < 2) {
            throw new RuntimeException("Account holder name must be at least 2 characters");
        }

        // Validation: Bank Name
        if (account.getBankName() == null || account.getBankName().trim().isEmpty()) {
            throw new RuntimeException("Bank name is required");
        }

        // Validation: Account Number
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (!account.getAccountNumber().matches("^\\d{10,16}$")) {
            throw new RuntimeException("Account number must be 10-16 digits");
        }

        // Validation: Routing Number
        if (account.getRoutingNumber() == null || account.getRoutingNumber().trim().isEmpty()) {
            throw new RuntimeException("Routing number is required");
        }
        if (!account.getRoutingNumber().matches("^\\d{9}$")) {
            throw new RuntimeException("Routing number must be exactly 9 digits");
        }

        // Validation: Unique Account Number
        if (accountsRepository.existsByAccountNumber(account.getAccountNumber())) {
            throw new RuntimeException("Account number already exists: " + account.getAccountNumber());
        }

        // Validation: Balance
        if (account.getBalance() == null || account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial balance cannot be negative");
        }

        // Validation: Max 10 accounts per user
        long accountCount = accountsRepository.countByUserId(userId);
        if (accountCount >= 10) {
            throw new RuntimeException("User cannot have more than 10 accounts");
        }

        // Set user relationship
        account.setUserId(userId);

        // Set defaults
        if (account.getStatus() == null) {
            account.setStatus(Accounts.AccountStatus.ACTIVE);
        }
        if (account.getCurrency() == null || account.getCurrency().trim().isEmpty()) {
            account.setCurrency("USD");
        }
        if (account.getIsActive() == null) {
            account.setIsActive(true);
        }
        if (account.getIsDeleted() == null) {
            account.setIsDeleted(false);
        }

        Accounts savedAccount = accountsRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getAccountId());
        return savedAccount;
    }

    public Accounts updateAccount(Long userId, Long accountId, Accounts accountDetails) {
        log.info("Updating account {} for user: {}", accountId, userId);

        Accounts account = getAccountById(userId, accountId);

        // Update Account Holder Name
        if (accountDetails.getAccountHolderName() != null && !accountDetails.getAccountHolderName().trim().isEmpty()) {
            if (accountDetails.getAccountHolderName().length() < 2) {
                throw new RuntimeException("Account holder name must be at least 2 characters");
            }
            account.setAccountHolderName(accountDetails.getAccountHolderName());
        }

        // Update Bank Name
        if (accountDetails.getBankName() != null && !accountDetails.getBankName().trim().isEmpty()) {
            account.setBankName(accountDetails.getBankName());
        }

        // Update Account Number
        if (accountDetails.getAccountNumber() != null && 
            !accountDetails.getAccountNumber().equals(account.getAccountNumber())) {
            
            if (!accountDetails.getAccountNumber().matches("^\\d{10,16}$")) {
                throw new RuntimeException("Account number must be 10-16 digits");
            }
            if (accountsRepository.existsByAccountNumber(accountDetails.getAccountNumber())) {
                throw new RuntimeException("Account number already exists: " + accountDetails.getAccountNumber());
            }
            account.setAccountNumber(accountDetails.getAccountNumber());
        }

        // Update Routing Number
        if (accountDetails.getRoutingNumber() != null && !accountDetails.getRoutingNumber().trim().isEmpty()) {
            if (!accountDetails.getRoutingNumber().matches("^\\d{9}$")) {
                throw new RuntimeException("Routing number must be exactly 9 digits");
            }
            account.setRoutingNumber(accountDetails.getRoutingNumber());
        }

        // Update Account Type
        if (accountDetails.getAccountType() != null) {
            account.setAccountType(accountDetails.getAccountType());
        }

        // Update Balance
        if (accountDetails.getBalance() != null) {
            if (accountDetails.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Balance cannot be negative");
            }
            account.setBalance(accountDetails.getBalance());
        }

        // Update Currency
        if (accountDetails.getCurrency() != null && !accountDetails.getCurrency().trim().isEmpty()) {
            account.setCurrency(accountDetails.getCurrency());
        }

        // Update Status
        if (accountDetails.getStatus() != null) {
            account.setStatus(accountDetails.getStatus());
        }

        // Update Active Flag
        if (accountDetails.getIsActive() != null) {
            account.setIsActive(accountDetails.getIsActive());
        }

        Accounts updatedAccount = accountsRepository.save(account);
        log.info("Account updated successfully: {}", accountId);
        return updatedAccount;
    }

    public void deleteAccount(Long userId, Long accountId) {
        log.info("Deleting account {} for user: {}", accountId, userId);

        Accounts account = getAccountById(userId, accountId);
        account.setIsDeleted(true);
        account.setIsActive(false);

        accountsRepository.save(account);
        log.info("Account deleted successfully: {}", accountId);
    }

    public BigDecimal getTotalBalance(Long userId) {
        log.info("Calculating total balance for user: {}", userId);
        BigDecimal totalBalance = accountsRepository.getTotalBalanceByUserId(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        BigDecimal totalBalance = getTotalBalance(userId);
        return totalBalance.compareTo(requiredAmount) >= 0;
    }

    public long countUserAccounts(Long userId) {
        return accountsRepository.countByUserId(userId);
    }

    public void validateUserCanMakePayment(Long userId, Long accountId, BigDecimal amount, HttpServletRequest request) {
        log.info("Validating payment for user: {} from account: {}", userId, accountId);

        String token = extractToken(request);
        this.getUserData(userId, token);

        if (this.userRequest == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        Accounts account = getAccountById(userId, accountId);

        if (!account.getIsActive() || account.getIsDeleted()) {
            throw new RuntimeException("Account is not active");
        }

        if (!account.getStatus().equals(Accounts.AccountStatus.ACTIVE)) {
            throw new RuntimeException("Account status is not ACTIVE");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account");
        }

        log.info("Payment validation successful for user: {}", userId);
    }

    public boolean accountBelongsToUser(Long userId, Long accountId) {
        return accountsRepository.existsByAccountIdAndUserId(accountId, userId);
    }

    public Optional<Accounts> getAccountByNumber(Long userId, String accountNumber) {
        return accountsRepository.findByAccountNumberAndUserId(accountNumber, userId);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Authorization token not found");
    }
}