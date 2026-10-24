-- Optional reference schema. Hibernate ddl-auto=update also manages these tables.
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  category VARCHAR(120) NOT NULL,
  brand VARCHAR(120) NOT NULL,
  price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  rating DOUBLE PRECISION,
  review_count INTEGER,
  description VARCHAR(1200),
  image_url VARCHAR(800),
  color VARCHAR(80),
  featured BOOLEAN DEFAULT FALSE,
  tags VARCHAR(800),
  available_sizes VARCHAR(200)
);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE IF NOT EXISTS addresses (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  recipient_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  address_line1 VARCHAR(255) NOT NULL,
  address_line2 VARCHAR(255),
  locality VARCHAR(120),
  city VARCHAR(100) NOT NULL,
  state VARCHAR(100) NOT NULL,
  postal_code VARCHAR(20) NOT NULL,
  country VARCHAR(100) NOT NULL DEFAULT 'India',
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);

CREATE TABLE IF NOT EXISTS orders (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  total_amount NUMERIC(12,2) NOT NULL,
  status VARCHAR(255) NOT NULL DEFAULT 'PENDING_PAYMENT',
  recipient_name VARCHAR(100) NOT NULL,
  recipient_phone VARCHAR(30) NOT NULL,
  delivery_address VARCHAR(1000) NOT NULL,
  contact_email VARCHAR(254),
  items_json TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);

CREATE TABLE IF NOT EXISTS payments (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
  razorpay_order_id VARCHAR(255) NOT NULL UNIQUE,
  razorpay_payment_id VARCHAR(255),
  status VARCHAR(255) NOT NULL DEFAULT 'CREATED',
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS background_jobs (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  job_type VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
  message VARCHAR(1000) NOT NULL DEFAULT 'Waiting for processing',
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uq_background_job_order_type UNIQUE(order_id, job_type)
);
-- ProductSeeder inserts 1,000 demo products on first application startup.
