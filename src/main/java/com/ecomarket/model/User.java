package com.ecomarket.model;

/**
 * A marketplace user mapped to the {@code users} table in schema.sql.
 *
 * <p>The prototype still uses firstName/lastName and a display location for
 * its forms; those are derived from (or kept alongside) the schema's
 * {@code full_name}, {@code address}, {@code eco_points} and
 * {@code is_verified} columns.</p>
 */
public class User {

    public enum Role { BUYER, SELLER, ADMIN }

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String location;
    private String memberSince;
    private Role role;

    // Schema-backed fields
    private String address;
    private String avatarUrl;
    private int ecoPoints;
    private boolean verified;
    private String createdAt;
    private String updatedAt;

    public User() {
    }

    public User(String id, String firstName, String lastName, String email,
                String password, String location, String memberSince, Role role) {
        this.id = id;
        setFirstName(firstName);
        setLastName(lastName);
        this.email = email;
        this.password = password;
        this.location = location;
        this.memberSince = memberSince;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? "" : firstName.trim();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        this.lastName = lastName == null ? "" : lastName.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /** Prototype-only meetup area; the schema's address is kept separately. */
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMemberSince() { return memberSince; }
    public void setMemberSince(String memberSince) { this.memberSince = memberSince; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getEcoPoints() { return ecoPoints; }
    public void setEcoPoints(int ecoPoints) { this.ecoPoints = ecoPoints; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /** Maps directly to the schema's {@code full_name} column. */
    public String getFullName() {
        return fullName();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            setFirstName("");
            setLastName("");
            return;
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        setFirstName(parts[0]);
        setLastName(parts.length > 1 ? parts[1] : "");
    }

    /** Convenience used by labels, chat headers and cells. */
    public String fullName() {
        return (firstName + " " + lastName).trim();
    }

    /** Two-letter avatar fallback (no image assets required). */
    public String initials() {
        String a = firstName == null || firstName.isBlank() ? "?" : firstName.substring(0, 1);
        String b = lastName == null || lastName.isBlank() ? "" : lastName.substring(0, 1);
        return (a + b).toUpperCase();
    }

    @Override
    public String toString() {
        return fullName();
    }
}
