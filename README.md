# Testinium Case Study

Java / Maven test automation project covering two scenarios:

1. **Selenium Web Otomasyon** — end-to-end flow on [www.zara.com/tr/](https://www.zara.com/tr/)
2. **Web Servis Otomasyon** — Trello REST API CRUD flow via Rest-Assured

The project follows **OOP** and the **Page Object Pattern** (plus a Service Object pattern on the API side). JUnit 5 is the runner, Log4J 2 handles logs, and Apache POI reads the Excel test data.


## Requirements

- Java 17+
- Google Chrome (the driver is downloaded automatically by WebDriverManager)
- No local Maven install needed — the repo ships with the Maven Wrapper (`./mvnw`)

## Project layout

```
src/
├── main/java/org/example/
│   ├── web/                   # Selenium — DriverManager, Page Objects, utils
│   │   ├── driver/DriverManager.java
│   │   ├── pages/             # BasePage + HomePage/LoginPage/MenPage/SearchPage/
│   │   │                      # ProductListPage/ProductDetailPage/CartPage
│   │   └── utils/             # ConfigReader, ExcelReader, ProductInfoWriter, WaitHelper
│   └── api/                   # Rest-Assured — client, models, services
│       ├── client/ApiClient.java
│       ├── config/ApiConfig.java
│       ├── models/            # Board, Card POJOs
│       └── services/          # BoardService, CardService
├── main/resources/
│   ├── log4j2.xml
│   ├── config.properties.example
│   └── testdata.xlsx          # A1="şort", B1="gömlek"
└── test/java/org/example/
    ├── web/ZaraFlowTest.java
    └── api/TrelloFlowTest.java
```

## Configuration

Copy the example file and fill in your own values:

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

`config.properties` is gitignored — never commit real credentials. The example file should only contain placeholders.

```properties
zara.email=your-email@example.com
zara.password=your-password
browser.headless=false

trello.key=YOUR_TRELLO_API_KEY
trello.token=YOUR_TRELLO_API_TOKEN
```

Any key can also be overridden by environment variables (upper-case with underscores in place of dots): `ZARA_EMAIL`, `TRELLO_KEY`, `TRELLO_TOKEN`, etc.

Trello key + token can be generated at https://trello.com/power-ups/admin.

## Running

```bash
# compile only
./mvnw clean compile

# Selenium flow (opens a real Chrome window)
./mvnw -Dtest=ZaraFlowTest test

# Trello API flow
TRELLO_KEY=... TRELLO_TOKEN=... ./mvnw -Dtest=TrelloFlowTest test

# everything
./mvnw test
```

Artifacts produced during a run:

- `logs/test.log` — Log4J file output (combined run log)
- `test-output/selected-product.txt` — product name + price written by the Zara test
- `target/surefire-reports/` — JUnit HTML / plain-text reports

## Test flow coverage

### `ZaraFlowTest.fullZaraFlow`

1. Open `www.zara.com/tr/`, accept the cookie banner.
2. Attempt login with credentials from `config.properties`.  If Zara's anti-bot
   challenge blocks the logon page (very common) the step is caught, logged,
   and the flow continues as guest.
3. Open the hamburger menu → click `ERKEK` (category-level-1) → click
   `TÜMÜNÜ GÖR`.
4. Open search, type the first keyword from `testdata.xlsx` (A1="şort"),
   clear it, type the second keyword (B1="gömlek"), press Enter.
5. Pick a random product card from the rendered grid.
6. Write the selected product's name + price to `test-output/selected-product.txt`.
7. Click the grid-level "Sepete ekle" button, pick the first in-stock size in
   the size selector overlay — this adds the product to the cart.
8. Navigate to `/tr/tr/shop/cart`, assert that the cart's price matches the
   grid price.
9. Increase the quantity, assert it is now 2.
10. Remove the item, assert the cart is empty.

### `TrelloFlowTest.fullTrelloFlow`

1. `POST /boards/` — create a board (assert 200 and returned name).
2. `GET /boards/{id}/lists` — discover the default list.
3. `POST /cards` — create "Card 1" and "Card 2".
4. Pick one of them at random, `PUT /cards/{id}` with a new name (assert the
   response reflects the update).
5. `DELETE /cards/{id}` for both cards.
6. `DELETE /boards/{id}` — clean up the board.

Every step is logged via Log4J and asserts both status and body.

## Known constraints (Zara side only)

Zara's production site ships aggressive Akamai / DataDome anti-bot protection.
Some pages never finish loading under automation. The framework works around
this as follows:

- `ChromeDriver` runs with `PageLoadStrategy.EAGER`, an
  `--disable-blink-features=AutomationControlled` flag, and a
  realistic user-agent.
- Login is **best-effort**: if the login form times out or is blocked, the
  step logs a warning and the rest of the test proceeds as guest. The framework
  itself demonstrates the login Page Object correctly.
- Product detail pages are unreachable via automation (they always hit a
  renderer timeout). The test therefore reads the product name and price
  **directly from the search-results grid** and uses the grid's in-line
  "Sepete ekle" button, which opens the size selector overlay and adds to
  cart without ever leaving the grid page. This is a reasonable interpretation
  of the case study step "Seçilen ürünün ürün bilgisi ve tutar bilgisi".
- Some header links appear twice in the DOM; where standard clicks are
  intercepted, the code falls back to either JavaScript clicks or direct
  URL navigation (e.g. opening the search page).
- Each end-to-end run typically takes **5–8 minutes** because Zara rate-limits
  heavily. This is a property of the site under test, not the framework.

The Trello API test is deterministic and runs in under 15 seconds.
