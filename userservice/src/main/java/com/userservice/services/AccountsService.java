package com.userservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import com.userservice.models.Accounts;
import com.userservice.models.Users;
import com.userservice.repositories.AccountsRepository;
import com.userservice.repositories.UsersRepository;

public class AccountsService {

    @Autowired
    private AccountsRepository accountsRepository;
    private UsersRepository usersRepository;

    private UsersService usersService;

    public AccountsService(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * Get all accounts for a user
     *  Validates user exists first
     */
    public List<Accounts> getUserAccounts(Long userId) {
        return accountsRepository.findByUser_UserId(userId);
    }

    /**
     * Get only ACTIVE accounts (exclude closed/frozen)
     *  For dashboard showing available accounts for payments
     */
    public List<Accounts> getActiveAccounts(Long userId) {
        return accountsRepository.findActiveAccountsByUserId(userId);
    }

    /**
     * Get a specific account by account number
     *  Security: Verifies account belongs to the user
     * Prevents user A from accessing user B's accounts
     */
    public Accounts getAccount(Long userId, String accountNumber) {

        //  Verify account exists AND belongs to this user
        return accountsRepository.findByUserIdAndAccountNumber(userId, accountNumber)
            .orElseThrow(() -> new RuntimeException(
                "Accounts not found or access denied"
            ));
    }

    /**
     * Create a new account for user
     *  Business Logic:
     *    1. User must exist
     *    2. Accounts number must be unique
     *    3. Set user relationship
     *    4. Save account
     */
    public Accounts createAccount(Long userId, Accounts account) {
        //  Validation 1: User must exist
        Optional<Users> user = usersRepository.findById(userId);

        if (user.isEmpty()) {
             throw new RuntimeException(
                "User does not exists: " + userId
            );
        }

        //  Validation 2: Accounts number must be unique
        if (accountsRepository.existsByAccountNumber(account.getAccountNumber())) {
            throw new RuntimeException(
                "Account number already exists: " + account.getAccountNumber()
            );
        }

        //  Validation 3: Account number format (not empty, reasonable length)
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }

        if (account.getAccountNumber().length() < 8 || account.getAccountNumber().length() > 20) {
            throw new RuntimeException("Account number must be 8-20 characters");
        }

        //  Validation 4: Initial balance must be valid
        if (account.getBalance() == null || account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial balance cannot be negative");
        }

        //  Validation 5: Check max accounts per user (e.g., max 10 accounts)
        long accountCount = accountsRepository.countAccountsByUserId(userId);
        if (accountCount >= 10) {
            throw new RuntimeException("User cannot have more than 10 accounts");
        }

        //  Save and return
        return accountsRepository.save(account);
    }

    /**
     * Get total balance across ALL accounts
     *  Useful for dashboard showing net worth
     */
    public BigDecimal getTotalBalance(Long userId) {
        
        //  Get total balance
        BigDecimal totalBalance = accountsRepository.getTotalBalanceByUserId(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    /**
     * Check if user has sufficient balance
     *  Used for payment validation
     * Example: Payment Service calls this before processing payment
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        BigDecimal totalBalance = getTotalBalance(userId);
        return totalBalance.compareTo(requiredAmount) >= 0;
    }

    /**
     * Get user's primary account (first active account)
     *  Useful for setting default payment account
     */
    public Optional<Accounts> getPrimaryAccount(Long userId) {

        
        //  Get first active account (if exists)
        List<Accounts> accounts = getActiveAccounts(userId);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }

    /**
     * Count total accounts for a user
     *  Used for validation (e.g., max 10 accounts per user)
     */
    public long countUserAccounts(Long userId) {

        return accountsRepository.countAccountsByUserId(userId);
    }

    /**
     * Validate if user can make a payment
     *  Combines multiple checks:
     *    1. User exists and is active
     *    2. Has sufficient balance
     *    3. Account is active
     */
    public void validateUserCanMakePayment(Long userId, Long accountId, BigDecimal amount) {
        //  Check 1: User exists and is active
        Users user = usersService.getUserById(userId);

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        if (user.getIsDeleted()) {
            throw new RuntimeException("User account is deleted");
        }

        //  Check 2: Accounts exists and belongs to user
        Optional<Accounts> account = accountsRepository.findById(accountId);
        if (account.isEmpty()) {
            throw new RuntimeException("Account not found");
        }

        if (!account.get().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Account does not belong to user");
        }

        //  Check 3: Account is active
        if (!account.get().getStatus().toString().equals("ACTIVE")) {
            throw new RuntimeException("Account is not active");
        }

        //  Check 4: Has sufficient balance
        if (account.get().getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account");
        }

        //  Check 5: Amount is valid
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
    }
}
