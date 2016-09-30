package za.org.grassroot.core.domain;

import za.org.grassroot.core.util.UIDGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by luke on 2015/10/18.
 * note: For naming this entity, there could be confusion with a 'user account', but since we rarely use that terminology,
 * better that than 'institution', which seems like it would set us up for trouble (the term is loaded) down the road.
 */

@Entity
@Table(name="paid_account")
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private Long id;

    @Column(name = "uid", nullable = false, unique = true)
    private String uid;

    @ManyToOne
    @JoinColumn(name = "created_by_user", nullable = false, updatable = false)
    private User createdByUser;

    @Basic
    @Column(name="created_date_time", insertable = true, updatable = false)
    private Instant createdDateTime;

    @ManyToOne
    @JoinColumn(name = "disabled_by_user")
    private User disabledByUser;

    @Column(name = "disabled_date_time")
    private Instant disabledDateTime;

    /*
    Doing this as one-to-many from account to users, rather than the inverse, because we are (much) more likely to have
    an account with 2-3 administrators than to have a user administering two accounts. The latter is not a non-zero
    possibility, but until/unless we have (very) strong user demand, catering to it is not worth many-to-many overheads
     */
    @OneToMany(mappedBy = "accountAdministered")
    private Set<User> administrators = new HashSet<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<PaidGroup> paidGroups = new HashSet<>();

    @Basic
    @Column(name = "account_name")
    private String accountName;

    @Basic
    @Column(name = "primary_email")
    private String primaryEmail;

    @Basic
    @Column
    private boolean enabled; // for future, in case we want to toggle a non-paying account on/off

    /*
    Range of account features
     */

    // how many groups the account can set as paid
    @Basic
    @Column(name = "max_group_number")
    private int numberOfGroups;

    @Basic
    @Column(name = "max_group_size")
    private int maxSizePerGroup;

    @Basic
    @Column(name = "max_sub_group_depth")
    private int maxSubGroupDepth;

    @Basic
    @Column(name="free_form")
    private boolean freeFormMessages;

    @Basic
    @Column(name="additional_reminders")
    private int extraReminders;

    @Version
    private Integer version;

    /*
    Constructors
     */

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        if (createdDateTime == null) {
            createdDateTime = Instant.now();
        }
    }

    private Account() {
        // For JPA
    }

    public Account(User createdByUser, String accountName) {
        Objects.requireNonNull(createdByUser);
        Objects.requireNonNull(accountName);

        this.uid = UIDGenerator.generateId();

        this.accountName = accountName;
        this.createdByUser = createdByUser;

        this.enabled = true;
        this.freeFormMessages = true;
    }

    /*
    Getters and setters
     */

    public Long getId() {
        return id;
    }

    public String getUid() { return uid; }

    public Instant getCreatedDateTime() {
        return createdDateTime;
    }

    public User getCreatedByUser() { return createdByUser; }

    public void setDisabledByUser(User disabledByUser) { this.disabledByUser = disabledByUser; }

    public void setDisabledDateTime(Instant disabledDateTime) { this.disabledDateTime = disabledDateTime; }

    public User getDisabledByUser() { return disabledByUser; }

    public Instant getDisabledDateTime() { return disabledDateTime; }

    public Set<User> getAdministrators() {
        return administrators;
    }

    public void setAdministrators(Set<User> administrators) {
        this.administrators = administrators;
    }

    public Set<PaidGroup> getPaidGroups() {
        return paidGroups;
    }

    public void setPaidGroups(Set<PaidGroup> paidGroups) {
        this.paidGroups = paidGroups;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFreeFormMessages() {
        return freeFormMessages;
    }

    public void setFreeFormMessages(boolean freeFormMessages) {
        this.freeFormMessages = freeFormMessages;
    }

    /*
    Helper methods for adding and removing administrators and groups
     */

    public void addAdministrator(User administrator) {
        administrators.add(administrator);
    }

    public void removeAdministrator(User administrator) {
        administrators.remove(administrator);
    }

    public void addPaidGroup(PaidGroup paidGroup) {
        paidGroups.add(paidGroup);
    }

    public void removePaidGroup(PaidGroup paidGroup) {
        paidGroups.remove(paidGroup);
    }

    public int getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(int numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public int getMaxSizePerGroup() {
        return maxSizePerGroup;
    }

    public void setMaxSizePerGroup(int maxSizePerGroup) {
        this.maxSizePerGroup = maxSizePerGroup;
    }

    public int getMaxSubGroupDepth() {
        return maxSubGroupDepth;
    }

    public void setMaxSubGroupDepth(int maxSubGroupDepth) {
        this.maxSubGroupDepth = maxSubGroupDepth;
    }

    public int getExtraReminders() {
        return extraReminders;
    }

    public void setExtraReminders(int extraReminders) {
        this.extraReminders = extraReminders;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }

        Account account = (Account) o;

        return getUid() != null ? getUid().equals(account.getUid()) : account.getUid() == null;
    }

    @Override
    public int hashCode() {
        return getUid() != null ? getUid().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", createdDateTime=" + createdDateTime +
                ", accountName=" + accountName +
                ", primaryEmail=" + primaryEmail +
                ", enabled=" + enabled +
                ", free form messages='" + freeFormMessages + '\'' +
                '}';
    }

}
