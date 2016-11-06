package bakha.ms.outlook.data;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private Account currentAccount;
    private List<Account> accounts;

    private static AccountManager instance;

    private AccountManager() {
        // initialization
    }

    public static AccountManager getInstance() {
        if(instance == null){
            instance = new AccountManager();
        }

        return instance;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    public void addAccount(Account account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);

        // if there is only one currentAccount, set it as a current currentAccount
        if (accounts.size() == 1) {
            setCurrentAccount(account);
        }
    }

    public void removeAccount(Account account) {
        for (Account acc : accounts) {
            if (acc.getAccountId() == account.getAccountId()) {
                accounts.remove(account);
            }
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void clearAccounts() {
        accounts.clear();
    }

    public Account getAccount(String email) {
        for (Account account : accounts) {
            if (account.getEmail() != null && account.getEmail().equals(email)) {
                return account;
            }
        }

        return null;
    }
}
