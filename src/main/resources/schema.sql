-- Schema for food delivery system (PostgreSQL).
-- Enum-like columns use VARCHAR + CHECK (PostgreSQL has no inline ENUM syntax like MySQL).

CREATE TABLE IF NOT EXISTS users (
    id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    phone VARCHAR(20)  NOT NULL,
    role  VARCHAR(30)  NOT NULL CHECK (role IN ('ADMIN', 'RESTAURANT_OWNER', 'CUSTOMER', 'DELIVERY_PARTNER')),
    city  VARCHAR(20)           CHECK (city IN ('BANGALORE', 'MUMBAI', 'DELHI', 'HYDERABAD', 'CHENNAI', 'KOLKATA', 'PUNE'))
);

CREATE TABLE IF NOT EXISTS restaurants (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    city         VARCHAR(20)  NOT NULL CHECK (city IN ('BANGALORE', 'MUMBAI', 'DELHI', 'HYDERABAD', 'CHENNAI', 'KOLKATA', 'PUNE')),
    owner_id     INTEGER      NOT NULL REFERENCES users(id),
    name         VARCHAR(255) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    rating_sum   BIGINT       NOT NULL DEFAULT 0,
    review_count INTEGER      NOT NULL DEFAULT 0,
    version      INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS restaurant_cuisines (
    restaurant_id INTEGER     NOT NULL REFERENCES restaurants(id),
    cuisine       VARCHAR(20) NOT NULL CHECK (cuisine IN ('INDIAN', 'ITALIAN', 'CHINESE', 'MEXICAN', 'THAI', 'JAPANESE', 'AMERICAN')),
    PRIMARY KEY (restaurant_id, cuisine)
);

CREATE TABLE IF NOT EXISTS menu_items (
    id               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id    INTEGER         NOT NULL REFERENCES restaurants(id),
    name             VARCHAR(255)    NOT NULL,
    description      TEXT,
    price            NUMERIC(10, 2)  NOT NULL,
    available_stock  INTEGER         NOT NULL DEFAULT 0,
    available        BOOLEAN         NOT NULL DEFAULT TRUE,
    version          INTEGER         NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS delivery_partners (
    id               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          INTEGER      NOT NULL UNIQUE REFERENCES users(id),
    city             VARCHAR(20)  NOT NULL CHECK (city IN ('BANGALORE', 'MUMBAI', 'DELHI', 'HYDERABAD', 'CHENNAI', 'KOLKATA', 'PUNE')),
    status           VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE')),
    current_order_id INTEGER,
    rating_sum       BIGINT       NOT NULL DEFAULT 0,
    review_count     INTEGER      NOT NULL DEFAULT 0,
    version          INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
    id                   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id          INTEGER         NOT NULL REFERENCES users(id),
    restaurant_id        INTEGER         NOT NULL REFERENCES restaurants(id),
    city                 VARCHAR(20)     NOT NULL CHECK (city IN ('BANGALORE', 'MUMBAI', 'DELHI', 'HYDERABAD', 'CHENNAI', 'KOLKATA', 'PUNE')),
    status               VARCHAR(30)     NOT NULL CHECK (status IN (
        'PENDING_PAYMENT', 'PLACED', 'ACCEPTED', 'PREPARING',
        'OUT_FOR_DELIVERY', 'DELIVERED', 'REJECTED', 'CANCELLED', 'PAYMENT_FAILED'
    )),
    total_amount         NUMERIC(10, 2)  NOT NULL,
    assigned_partner_id  INTEGER         REFERENCES delivery_partners(id),
    placed_at            TIMESTAMP       NOT NULL,
    delivered_at         TIMESTAMP,
    version              INTEGER         NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_items (
    id                   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id             INTEGER         NOT NULL REFERENCES orders(id),
    menu_item_id         INTEGER         NOT NULL REFERENCES menu_items(id),
    name_snapshot        VARCHAR(255)    NOT NULL,
    unit_price_snapshot  NUMERIC(10, 2)  NOT NULL,
    quantity             INTEGER         NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    INTEGER         NOT NULL UNIQUE REFERENCES orders(id),
    amount      NUMERIC(10, 2)  NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL
);

CREATE TABLE IF NOT EXISTS ratings (
    id                INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id          INTEGER     NOT NULL UNIQUE REFERENCES orders(id),
    customer_id       INTEGER     NOT NULL REFERENCES users(id),
    restaurant_id     INTEGER     NOT NULL REFERENCES restaurants(id),
    partner_id        INTEGER     REFERENCES delivery_partners(id),
    restaurant_stars  INTEGER     NOT NULL CHECK (restaurant_stars BETWEEN 1 AND 5),
    partner_stars     INTEGER              CHECK (partner_stars BETWEEN 1 AND 5),
    review            TEXT,
    created_at        TIMESTAMP   NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_city ON users(city);
CREATE INDEX IF NOT EXISTS idx_restaurant_city ON restaurants(city);
CREATE INDEX IF NOT EXISTS idx_restaurant_name ON restaurants(LOWER(name));
CREATE INDEX IF NOT EXISTS idx_menu_item_restaurant ON menu_items(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_name ON menu_items(LOWER(name));
CREATE INDEX IF NOT EXISTS idx_order_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_restaurant ON orders(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_city ON orders(city);
CREATE INDEX IF NOT EXISTS idx_order_assigned_partner ON orders(assigned_partner_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_delivery_partner_user ON delivery_partners(user_id);
CREATE INDEX IF NOT EXISTS idx_delivery_partner_city ON delivery_partners(city);
CREATE INDEX IF NOT EXISTS idx_delivery_partner_status ON delivery_partners(status);
