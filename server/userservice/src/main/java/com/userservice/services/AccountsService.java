package com.userservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.userservice.models.Accounts;
import com.userservice.models.Users;
import com.userservice.repositories.AccountsRepository;
import com.userservice.repositories.UsersRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class AccountsService {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersService usersService;

    /**
     * Get all accounts for a user
     * Security: Only returns accounts belonging to the specified user
     */
    public List<Accounts> getAllAccountsByUserId(Long userId) {
        log.info("Fetching all accounts for user: {}", userId);
        
        // Verify user exists
        Users user = usersService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        return accountsRepository.findByUser_UserId(userId);
    }

    /**
     * Get a specific account by ID
     * Security: Verifies account belongs to the user
     */
    public Accounts getAccountById(Long userId, Long accountId) {
        log.info("Fetching account {} for user: {}", accountId, userId);
        
        return accountsRepository.findByAccountIdAndUser_UserId(accountId, userId)
            .orElseThrow(() -> new RuntimeException(
                "Account not found or access denied"
            ));
    }

    /**
     * Get only ACTIVE accounts for a user
     * Used for payment processing and dashboard
     */
    public List<Accounts> getActiveAccounts(Long userId) {
        log.info("Fetching active accounts for user: {}", userId);
        
        return accountsRepository.findActiveAccountsByUserId(userId);
    }

    /**
     * Create a new account for a user
     * Business Logic:
     *   1. User must exist
     *   2. Account number must be unique
     *   3. Account number format validation
     *   4. Balance validation
     *   5. Max accounts per user check (10 accounts limit)
     *   6. Set user relationship
     *   7. Save to database
     */
    public Accounts createAccount(Long userId, Accounts account) {
        log.info("Creating account for user: {}", userId);

        // Validation 1: User must exist
        Users user = usersRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Validation 2: Account number must not be empty
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }

        // Validation 3: Account number format (8-20 characters)
        if (account.getAccountNumber().length() < 8 || account.getAccountNumber().length() > 20) {
            throw new RuntimeException("Account number must be 8-20 characters");
        }

        // Validation 4: Account number must be unique
        if (accountsRepository.existsByAccountNumber(account.getAccountNumber())) {
            throw new RuntimeException("Account number already exists: " + account.getAccountNumber());
        }

        // Validation 5: Initial balance must be valid (>= 0)
        if (account.getBalance() == null || account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial balance cannot be negative");
        }

        // Validation 6: Check max accounts per user (max 10 accounts)
        long accountCount = accountsRepository.countByUser_UserId(userId);
        if (accountCount >= 10) {
            throw new RuntimeException("User cannot have more than 10 accounts");
        }

        // Set user relationship
        account.setUser(user);
        
        // Set defaults
        if (account.getStatus() == null) {
            account.setStatus(Accounts.AccountStatus.ACTIVE);
        }
        if (account.getCurrency() == null) {
            account.setCurrency("USD");
        }
        if (account.getIsActive() == null) {
            account.setIsActive(true);
        }
        if (account.getIsDeleted() == null) {
            account.setIsDeleted(false);
        }

        // Save and return
        Accounts savedAccount = accountsRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getAccountId());
        return savedAccount;
    }

    /**
     * Update an existing account
     * Business Logic:
     *   1. Account must exist and belong to user
     *   2. If updating account number, it must be unique
     *   3. Balance and status updates
     *   4. Save to database
     */
    public Accounts updateAccount(Long userId, Long accountId, Accounts accountDetails) {
        log.info("Updating account {} for user: {}", accountId, userId);

        // Get existing account
        Accounts account = getAccountById(userId, accountId);

        // Validation: If account number is being changed, ensure it's unique
        if (accountDetails.getAccountNumber() != null && 
            !accountDetails.getAccountNumber().equals(account.getAccountNumber())) {
            
            if (accountsRepository.existsByAccountNumber(accountDetails.getAccountNumber())) {
                throw new RuntimeException("Account number already exists: " + accountDetails.getAccountNumber());
            }
            
            account.setAccountNumber(accountDetails.getAccountNumber());
        }

        // Update fields
        if (accountDetails.getAccountType() != null) {
            account.setAccountType(accountDetails.getAccountType());
        }

        if (accountDetails.getBalance() != null) {
            if (accountDetails.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Balance cannot be negative");
            }
            account.setBalance(accountDetails.getBalance());
        }

        if (accountDetails.getCurrency() != null) {
            account.setCurrency(accountDetails.getCurrency());
        }

        if (accountDetails.getStatus() != null) {
            account.setStatus(accountDetails.getStatus());
        }

        if (accountDetails.getIsActive() != null) {
            account.setIsActive(accountDetails.getIsActive());
        }

        // Save and return
        Accounts updatedAccount = accountsRepository.save(account);
        log.info("Account updated successfully: {}", accountId);
        return updatedAccount;
    }

    /**
     * Soft delete an account
     * Sets isDeleted = true and isActive = false
     */
    public void deleteAccount(Long userId, Long accountId) {
        log.info("Deleting account {} for user: {}", accountId, userId);

        Accounts account = getAccountById(userId, accountId);

        // Soft delete
        account.setIsDeleted(true);
        account.setIsActive(false);

        accountsRepository.save(account);
        log.info("Account deleted successfully: {}", accountId);
    }

    /**
     * Get total balance across all ACTIVE accounts
     * Useful for dashboard showing net worth
     */
    public BigDecimal getTotalBalance(Long userId) {
        log.info("Calculating total balance for user: {}", userId);
        
        BigDecimal totalBalance = accountsRepository.getTotalBalanceByUserId(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    /**
     * Check if user has sufficient balance for a payment
     * Used for payment validation before processing
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        BigDecimal totalBalance = getTotalBalance(userId);
        return totalBalance.compareTo(requiredAmount) >= 0;
    }

    /**
     * Get user's primary account (first active account)
     * Useful for setting default payment account
     */
    public Optional<Accounts> getPrimaryAccount(Long userId) {
        log.info("Fetching primary account for user: {}", userId);
        
        List<Accounts> activeAccounts = getActiveAccounts(userId);
        return activeAccounts.isEmpty() ? Optional.empty() : Optional.of(activeAccounts.get(0));
    }

    /**
     * Count total accounts for a user
     * Used for validation (e.g., max 10 accounts per user)
     */
    public long countUserAccounts(Long userId) {
        return accountsRepository.countByUser_UserId(userId);
    }

    /**
     * Validate if user can make a payment
     * Combines multiple checks:
     *   1. User exists and is active
     *   2. Account exists and belongs to user
     *   3. Account is active
     *   4. Account has sufficient balance
     *   5. Payment amount is valid
     */
    public void validateUserCanMakePayment(Long userId, Long accountId, BigDecimal amount) {
        log.info("Validating payment for user: {} from account: {}", userId, accountId);

        // Check 1: User exists and is active
        Users user = usersService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        if (user.getIsDeleted()) {
            throw new RuntimeException("User account is deleted");
        }

        // Check 2: Account exists and belongs to user
        Accounts account = getAccountById(userId, accountId);

        // Check 3: Account is active
        if (!account.getIsActive() || account.getIsDeleted()) {
            throw new RuntimeException("Account is not active");
        }

        if (!account.getStatus().equals(Accounts.AccountStatus.ACTIVE)) {
            throw new RuntimeException("Account status is not ACTIVE");
        }

        // Check 4: Amount is valid
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        // Check 5: Has sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account");
        }

        log.info("Payment validation successful for user: {}", userId);
    }

    /**
     * Check if account belongs to user
     */
    public boolean accountBelongsToUser(Long userId, Long accountId) {
        return accountsRepository.existsByAccountIdAndUser_UserId(accountId, userId);
    }

    /**
     * Get account by account number
     * Security: Returns only if it belongs to the user
     */
    public Optional<Accounts> getAccountByNumber(Long userId, String accountNumber) {
        return accountsRepository.findByAccountNumberAndUser_UserId(accountNumber, userId);
    }
}