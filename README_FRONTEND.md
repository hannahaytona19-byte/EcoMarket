# EcoMarket — Frontend-only JavaFX prototype

A wireframe-styled **JavaFX 21 / Java 17+** desktop frontend for the EcoMarket
peer-to-peer pre-loved marketplace. Everything runs from hardcoded mock data in
`com.ecomarket.service.MockDataService`; there is **no database, no networking,
and no cart/checkout/order/payment/shipping code** in this app.

Tagline: **Reuse. Resell. Rehome.**

## Run

```powershell
mvn javafx:run
```

If Maven is not on your PATH, use the bundled wrapper:

```powershell
.\mvnw.cmd javafx:run
```

Run the checks (including an FXML smoke test that loads every view):

```powershell
mvn test
```

The original backend-oriented sources under `io.ecomarket.desktop` are left
untouched. `pom.xml` now points `javafx:run` at `com.ecomarket.app.Main`.

## Structure

```
src/main/java/com/ecomarket/
├── app/        Main.java, SceneNavigator.java   (single Stage / shared shell)
├── controller/ 12 small FXML controllers
├── model/      User, Listing, Category, ProductCondition, Conversation, Message, AppNotification
├── service/    MockDataService (all sample data + in-memory actions)
├── ui/         BrandAssets (paths/sizes/colors) + LogoView (clickable badge)
└── util/       UiUtil (cards, placeholders, currency, modal dialogs, About)
src/main/resources/
├── view/       12 FXML files
├── css/        ecomarket.css (logo-matched green/cream tokens)
├── img/        Legacy eco-market-logo.svg (no longer used by the UI)
└── assets/
    ├── images/ Transparent circular badge: source, 16–512 px, @2x for HiDPI
    └── icons/  ecomarket.ico (Windows) and ecomarket.icns (macOS)
```

## Brand assets

The supplied JPG-style badge was converted to a transparent PNG by flood-filling
only the near-white area outside the circle, then cropped to the badge. Every
placement loads through `com.ecomarket.ui.BrandAssets`, so swapping the artwork
only means replacing the files in `src/main/resources/assets/`.

- Splash: 256 px, shown for 700 ms before the landing page.
- Login / Register card: 160 px, centred above the form.
- Navbar / sidebar / footer: 40 px, clickable to home (dashboard when signed in).
- About dialog: 128 px, with app name and `Implementation-Version` from the POM.
- Window, taskbar and installer: `.ico` on Windows, `.icns` on macOS, PNG elsewhere.
- High-DPI displays automatically select the `@2x` (1024 px) master.

Tagline text is only rendered at large sizes; the sub-64 px placements use the
whole badge as a mark rather than relying on wordmark legibility.

Native packaging is opt-in (it needs the JDK `jpackage` tool plus platform tools
such as WiX on Windows):

```powershell
.\mvnw.cmd -DdesktopInstaller verify
```

## Screen map

| Route | FXML | Purpose |
|---|---|---|
| Landing | `landing.fxml` | Hero, 2×2 photo placeholders, categories, how it works, why, footer |
| Role select | `role-select.fxml` | Buyer / Seller persona cards |
| Login / Register | `login.fxml`, `register.fxml` | Client-side validation only |
| Browse | `browse.fxml` | Filter chips + reflowing product grid; cards open listing details |
| Listing details | `listing-details.fxml` | Modal; Save / Share / **Message Seller** only |
| Messages | `messages.fxml` | Two-panel chats, pinned listing, send, message-only meetup suggestion |
| Notifications | `notifications.fxml` | Today / Yesterday groups, All / Unread tabs |
| Profile | `profile.fxml` | Avatar/details, My Listings / Saved Items tabs |
| Seller Dashboard | `seller-dashboard.fxml` | Summary cards + listing management + Post New Item dialog |

Logged-in pages are hosted inside `shell.fxml`, so the sidebar and top bar stay
fixed while only the center content is swapped by `SceneNavigator`.

## Schema alignment

The in-memory domain mirrors `supabase/schema.sql` while staying fully offline:

| Schema table | Prototype type | Notes |
|---|---|---|
| `users` | `com.ecomarket.model.User` | id, full name, email, role (+`ADMIN`), address, avatar URL, eco points, verified flag and timestamps |
| `categories` | `com.ecomarket.model.Category` | id, name, slug, description and icon name from the seed rows |
| `products` | `com.ecomarket.model.Listing` | id, seller, category, title, description, price, stock, condition enum, eco badge, carbon saved, image URL, active flag and created date |
| `cart_items`, `orders`, `order_items`, `reviews` | — | Present in the SQL schema and legacy `io.ecomarket.desktop` sources; deliberately outside this frontend-only prototype |
| — | `Conversation`, `Message`, `AppNotification` | Prototype-only messaging UI; no schema tables |

`Listing.location`, `tint`, `photoLabels`, `saved`, `status` and `postedAt` are
prototype-only presentation fields (meetup area, placeholder photo colors,
favorites and the local ACTIVE/SOLD listing lifecycle). They are **not** the
same as `orders.status`.

## Demo data

Seed rows come from `supabase/schema.sql`:

- Buyer: `buyer@ecomarket.io` / `password123` (legacy alias `juan@ecomarket.ph` / `demo123`)
- Seller: `artisan@ecomarket.io` / `password123` (legacy alias `elena@ecomarket.ph` / `demo123`)
- Admin: `admin@ecomarket.io` / `admin123` (schema seed only)
- Any other email with a password of 6+ characters registers a new in-memory buyer.
- 4 schema products, 4 schema categories, 3 conversations, 3 notifications and
  one pre-saved item. Prices use the schema's numeric values with 2 decimals.

## Assumptions (blank areas in the wireframes)

- **Profile:** the avatar is initials-only (no image assets); "Member since" is
  mock text. `Edit Profile` edits first name, last name and location only.
  `My Listings` shows items owned by the active persona; `Saved Items` starts
  with the rattan side table already saved.
- **Seller Dashboard:** driven by the demo seller Elena Rivera. "Unread Messages"
  is derived from unread in-app notifications. Rows support Edit, Mark as Sold
  and Delete. "Post New Item" opens a form whose four photo slots are clickable
  mock placeholders, not real file uploads.
- **Categories & conditions:** browse chips come from the four schema categories
  (Zero-Waste Kitchen, Eco Fashion & Apparel, Refurbished Tech, Personal Care).
  Conditions use the schema CHECK values `NEW`, `UPCYCLED`, `REFURBISHED` and
  `PRELOVED`, shown as friendly labels. Meetup location remains a prototype-only
  filter because the schema has no location column.
- **Meetup:** "Suggest meetup" only appends a message with a suggested place,
  date and time — it does not create a calendar event or any transaction.
- **Navbar links:** Categories and How It Works scroll to their landing sections;
  Browse leads to role selection when signed out.
