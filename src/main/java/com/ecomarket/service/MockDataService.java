package com.ecomarket.service;

import com.ecomarket.model.AppNotification;
import com.ecomarket.model.Category;
import com.ecomarket.model.Conversation;
import com.ecomarket.model.Listing;
import com.ecomarket.model.Message;
import com.ecomarket.model.ProductCondition;
import com.ecomarket.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Single in-memory data source for the whole prototype.
 *
 * <p>Seed data mirrors the tables and seed rows in {@code supabase/schema.sql}:
 * users, categories and products are schema-backed, including eco points,
 * eco badges, stock, condition enums and carbon-saved values. Meetup location,
 * placeholder colors and saved/status flags remain prototype-only UI state.</p>
 */
public final class MockDataService {

    private static final MockDataService INSTANCE = new MockDataService();

    private final List<User> users = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final List<Listing> listings = new ArrayList<>();
    private final List<Conversation> conversations = new ArrayList<>();
    private final List<AppNotification> notifications = new ArrayList<>();
    private final Set<String> savedIds = new LinkedHashSet<>();

    private User currentUser;
    private User demoBuyer;
    private User demoSeller;

    private MockDataService() {
        seed();
    }

    public static MockDataService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------
    // Session
    // ------------------------------------------------------------------

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User demoBuyer() {
        return demoBuyer;
    }

    public User demoSeller() {
        return demoSeller;
    }

    /** Used by the Landing/Browse role selection. */
    public User enterAsBuyer() {
        currentUser = demoBuyer;
        return currentUser;
    }

    /** Used by the Landing/Browse role selection. */
    public User enterAsSeller() {
        currentUser = demoSeller;
        return currentUser;
    }

    /**
     * Mock authentication: known demo accounts sign in directly; any other
     * email with a password of at least 6 characters is registered on the fly.
     * The old PH demo emails remain accepted aliases for the schema personas.
     */
    public Optional<User> authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.length() < 6) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase();
        if ("juan@ecomarket.ph".equals(normalized)) {
            currentUser = demoBuyer;
            return Optional.of(demoBuyer);
        }
        if ("elena@ecomarket.ph".equals(normalized)) {
            currentUser = demoSeller;
            return Optional.of(demoSeller);
        }

        User existing = users.stream()
                .filter(u -> normalized.equals(u.getEmail().toLowerCase()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            currentUser = existing;
            return Optional.of(existing);
        }
        currentUser = register("New", "Member", normalized, password);
        return Optional.of(currentUser);
    }

    public User register(String firstName, String lastName, String email, String password) {
        User user = new User(
                "u-" + UUID.randomUUID().toString().substring(0, 8),
                firstName, lastName, email, password,
                "Metro City", "November 2026", User.Role.BUYER
        );
        user.setAddress("Green Avenue 12, Metro City");
        user.setEcoPoints(0);
        user.setVerified(false);
        user.setCreatedAt("2026-11-23");
        user.setUpdatedAt("2026-11-23");
        users.add(user);
        currentUser = user;
        return user;
    }

    // ------------------------------------------------------------------
    // Categories & listings
    // ------------------------------------------------------------------

    public List<Category> getCategories() {
        return categories;
    }

    public Optional<Category> getCategoryByName(String name) {
        return categories.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst();
    }

    public List<Listing> getListings() {
        return listings;
    }

    public Optional<Listing> getListingById(String id) {
        return listings.stream().filter(l -> l.getId().equals(id)).findFirst();
    }

    public List<Listing> getListingsBySeller(User seller) {
        if (seller == null) return List.of();
        return listings.stream()
                .filter(l -> l.getSeller() != null && seller.getId().equals(l.getSeller().getId()))
                .toList();
    }

    public List<Listing> getSavedListings() {
        return listings.stream().filter(l -> savedIds.contains(l.getId())).toList();
    }

    public void toggleSaved(Listing listing) {
        if (listing == null) return;
        if (savedIds.contains(listing.getId())) {
            savedIds.remove(listing.getId());
            listing.setSaved(false);
        } else {
            savedIds.add(listing.getId());
            listing.setSaved(true);
        }
    }

    public void addListing(Listing listing) {
        listings.add(0, listing);
    }

    public void updateListing(Listing listing) {
        // Objects are shared with the views, so edits are already applied.
        // This method exists as the stable "save" hook for a future backend.
    }

    public void deleteListing(Listing listing) {
        listings.remove(listing);
        savedIds.remove(listing.getId());
        conversations.removeIf(c -> c.getListing() == listing);
    }

    public void markSold(Listing listing) {
        if (listing != null) listing.setStatus(Listing.Status.SOLD);
    }

    /** Backwards-compatible overload for callers still passing a string condition. */
    public Listing createListing(String title, Category category, String condition,
                                 double price, String location, String description) {
        return createListing(title, category, ProductCondition.from(condition), price,
                location, description, 1, null, 0, null);
    }

    public Listing createListing(String title, Category category, ProductCondition condition,
                                 double price, String location, String description,
                                 int stockQuantity, String ecoBadge, double carbonSavedKg,
                                 String imageUrl) {
        Listing listing = new Listing(
                "p-" + UUID.randomUUID().toString().substring(0, 8),
                currentUser, category, title, price, condition, location, description,
                "#E4E0D3", "Just now", stockQuantity, ecoBadge, carbonSavedKg,
                imageUrl, "2026-11-23"
        );
        addListing(listing);
        return listing;
    }

    // ------------------------------------------------------------------
    // Chats
    // ------------------------------------------------------------------

    public List<Conversation> getConversations() {
        return conversations;
    }

    public Optional<Conversation> getConversationById(String id) {
        return conversations.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Optional<Conversation> findConversationForListing(Listing listing) {
        if (listing == null) return Optional.empty();
        return conversations.stream()
                .filter(c -> c.getListing() != null && listing.getId().equals(c.getListing().getId()))
                .findFirst();
    }

    /** Message Seller always lands in a real chat, creating a mock thread if needed. */
    public Conversation getOrCreateConversation(Listing listing) {
        if (listing == null) {
            throw new IllegalArgumentException("A listing is required to start a chat.");
        }
        return findConversationForListing(listing).orElseGet(() -> {
            User seller = listing.getSeller() == null ? demoSeller : listing.getSeller();
            Conversation conversation = new Conversation(
                    "c-" + UUID.randomUUID().toString().substring(0, 8), seller, listing);
            conversations.add(0, conversation);
            return conversation;
        });
    }

    public void addMessage(Conversation conversation, String text) {
        if (conversation == null || text == null || text.isBlank()) return;
        String senderId = currentUser == null ? demoBuyer.getId() : currentUser.getId();
        conversation.addMessage(new Message(
                "m-" + UUID.randomUUID().toString().substring(0, 8),
                senderId, text.trim(), LocalDateTime.now()
        ));
    }

    /** Message-only meetup suggestion; there is no scheduling/payment feature. */
    public void addMeetupMessage(Conversation conversation) {
        addMessage(conversation,
                "Meetup suggestion: Saturday, 10:00 AM at the Cavite city plaza entrance.");
    }

    public void markConversationRead(Conversation conversation) {
        if (conversation != null) conversation.setUnreadCount(0);
    }

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    public List<AppNotification> getNotifications() {
        return notifications;
    }

    public void markNotificationRead(AppNotification notification) {
        if (notification != null) notification.setRead(true);
    }

    public long unreadNotificationCount() {
        return notifications.stream().filter(n -> !n.isRead()).count();
    }

    // ------------------------------------------------------------------
    // Seed data — mirrors supabase/schema.sql
    // ------------------------------------------------------------------

    private void seed() {
        demoBuyer = schemaUser("u0000001-0000-0000-0000-000000000001",
                "Eco Demo User", "buyer@ecomarket.io", "password123",
                "Metro City", "Green Avenue 12, Metro City",
                "January 2025", User.Role.BUYER, 150, true);
        demoSeller = schemaUser("u0000002-0000-0000-0000-000000000002",
                "Eco Artisan Studio", "artisan@ecomarket.io", "password123",
                "Forest Hill", "Sustainable Lane 44, Forest Hill",
                "January 2024", User.Role.SELLER, 520, true);
        User admin = schemaUser("u0000003-0000-0000-0000-000000000003",
                "Admin Supervisor", "admin@ecomarket.io", "admin123",
                "Metro City", "EcoHQ Tower 1",
                "January 2023", User.Role.ADMIN, 1000, true);
        currentUser = demoBuyer;
        users.add(demoBuyer);
        users.add(demoSeller);
        users.add(admin);

        Category kitchen = category(1, "Zero-Waste Kitchen", "zero-waste-kitchen",
                "Sustainable kitchenware, beeswax wraps, and compostable tools",
                "kitchen", "🍃");
        Category fashion = category(2, "Eco Fashion & Apparel", "eco-fashion",
                "Upcycled organic cotton apparel and recycled accessories",
                "shirt", "👕");
        Category tech = category(3, "Refurbished Tech", "refurbished-tech",
                "Certified energy-efficient refurbished gadgets",
                "devices", "📱");
        Category care = category(4, "Personal Care", "personal-care",
                "Solid shampoo bars, bamboo toothbrushes, and cruelty-free care",
                "spa", "🧼");
        categories.add(kitchen);
        categories.add(fashion);
        categories.add(tech);
        categories.add(care);

        Listing wraps = product("p0000001-0000-0000-0000-000000000001", kitchen,
                "Organic Beeswax Food Wrap Set (Pack of 3)",
                "Reusable, washable, and biodegradable alternative to plastic cling wrap. "
                        + "Handcrafted with organic jojoba oil and damar resin.",
                18.50, ProductCondition.NEW, "Plastic-Free", 2.40, 45,
                "https://images.unsplash.com/photo-1605371924599-2d0365da1ae0?w=600",
                "Cavite", "#DCE9CF", "2 hours ago");
        Listing apron = product("p0000002-0000-0000-0000-000000000002", fashion,
                "Upcycled Denim Apron with Utility Pockets",
                "Hand-stitched kitchen and gardening apron created from rescued surplus denim "
                        + "jeans. Durable and unique.",
                32.00, ProductCondition.UPCYCLED, "Circular Economy", 7.80, 14,
                "https://images.unsplash.com/photo-1598532163257-ae3c6b2524b6?w=600",
                "Taguig", "#D6DEE9", "Yesterday");
        Listing reader = product("p0000003-0000-0000-0000-000000000003", tech,
                "Refurbished E-Reader (6-inch Display, 8GB)",
                "Factory refurbished digital paper reader with warm light and long battery "
                        + "life. Keeps electronics out of landfills.",
                69.99, ProductCondition.REFURBISHED, "E-Waste Saver", 14.50, 8,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
                "Batangas", "#DDE2E4", "2 days ago");
        Listing shampooBar = product("p0000004-0000-0000-0000-000000000004", care,
                "Handmade Coconut & Lavender Solid Shampoo Bar",
                "Zero-plastic hair cleansing bar that replaces up to three bottles of standard "
                        + "liquid shampoo. Vegan and sulfate-free.",
                9.75, ProductCondition.NEW, "Zero-Waste", 1.80, 60,
                "https://images.unsplash.com/photo-1608248597359-0744c84918e6?w=600",
                "Cavite", "#E8E2D0", "3 days ago");
        listings.add(wraps);
        listings.add(apron);
        listings.add(reader);
        listings.add(shampooBar);

        savedIds.add(shampooBar.getId());
        shampooBar.setSaved(true);

        Conversation wrapsChat = new Conversation("c-1", demoSeller, wraps);
        wrapsChat.addMessage(new Message("m-1", demoBuyer.getId(),
                "Hi! Are the beeswax wraps still available?", LocalDateTime.now().minusMinutes(32)));
        wrapsChat.addMessage(new Message("m-2", demoSeller.getId(),
                "Yes! They are handmade in small batches and ready to ship.",
                LocalDateTime.now().minusMinutes(28)));
        wrapsChat.addMessage(new Message("m-3", demoBuyer.getId(),
                "Great. Do they work on glass containers?", LocalDateTime.now().minusMinutes(18)));
        wrapsChat.addMessage(new Message("m-4", demoSeller.getId(),
                "Absolutely — the heat from your hands seals them snugly.",
                LocalDateTime.now().minusMinutes(5)));
        wrapsChat.setUnreadCount(1);

        Conversation apronChat = new Conversation("c-2", demoSeller, apron);
        apronChat.addMessage(new Message("m-5", demoBuyer.getId(),
                "Is the apron one of a kind?", LocalDateTime.now().minusHours(3)));
        apronChat.addMessage(new Message("m-6", demoSeller.getId(),
                "Yes, each one is cut from a different pair of rescued jeans.",
                LocalDateTime.now().minusHours(2)));
        apronChat.setUnreadCount(0);

        Conversation readerChat = new Conversation("c-3", demoSeller, reader);
        readerChat.addMessage(new Message("m-7", demoBuyer.getId(),
                "Does the e-reader include a charger?", LocalDateTime.now().minusDays(2)));
        readerChat.addMessage(new Message("m-8", demoSeller.getId(),
                "Yes, it includes a USB-C cable and a 90-day warranty.",
                LocalDateTime.now().minusDays(2)));
        readerChat.setUnreadCount(0);
        conversations.add(wrapsChat);
        conversations.add(apronChat);
        conversations.add(readerChat);

        notifications.add(new AppNotification("n-1", "Eco Artisan Studio replied to your message.",
                "Absolutely — the heat from your hands seals them snugly.",
                "5m", "Today", true, "c-1", null));
        notifications.add(new AppNotification("n-2", "New eco find: Organic Beeswax Food Wrap Set",
                "Plastic-Free   •   2.40 kg CO₂ saved", "1hr", "Today", true, null, wraps.getId()));
        notifications.add(new AppNotification("n-3", "Welcome to EcoMarket!",
                "Reuse. Resell. Rehome.", "Yesterday", "Yesterday", false, null, null));
    }

    private User schemaUser(String id, String fullName, String email, String password,
                            String location, String address, String memberSince,
                            User.Role role, int ecoPoints, boolean verified) {
        User user = new User(id, "", "", email, password, location, memberSince, role);
        user.setFullName(fullName);
        user.setAddress(address);
        user.setEcoPoints(ecoPoints);
        user.setVerified(verified);
        user.setCreatedAt(memberSince);
        user.setUpdatedAt(memberSince);
        return user;
    }

    private Category category(int id, String name, String slug, String description,
                              String iconName, String icon) {
        return new Category(id, name, slug, description, iconName, icon);
    }

    private Listing product(String id, Category category, String title, String description,
                            double price, ProductCondition condition, String ecoBadge,
                            double carbonSavedKg, int stockQuantity, String imageUrl,
                            String location, String tint, String postedAt) {
        return new Listing(id, demoSeller, category, title, price, condition, location,
                description, tint, postedAt, stockQuantity, ecoBadge, carbonSavedKg,
                imageUrl, "2026-11-23");
    }
}
