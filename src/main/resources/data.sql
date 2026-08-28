-- =============================================================
-- Seed data for low_signal
-- Usa ON CONFLICT DO NOTHING per evitare errori al riavvio
-- =============================================================

SET session_replication_role = replica^^

-- ─── Collections ──────────────────────────────────────────────
INSERT INTO public.collections (id, collection_status, created_at, description, featured, hero_image, name, season, slug) VALUES
(1, 'DISPONIBILE', '2025-03-01 10:00:00', 'Six pieces built for friction. Functional silhouettes, industrial materials, zero compromise. The collection that defines the season.', false, 'https://images.unsplash.com/photo-1756276900419-868625adff43?w=1400&h=900&fit=crop&auto=format', 'SPRING / SUMMER 25', 'SS25', 'spring-summer-25'),
(2, 'SOLD_OUT',    '2024-09-01 10:00:00', 'Heavy textiles. Muted tones. A collection built for cold cities and darker hours. Three silhouettes, maximum impact.',                false, 'https://images.unsplash.com/photo-1771580425850-3dbcdfd97b54?w=1400&h=900&fit=crop&auto=format', 'FALL / WINTER 24',   'FW24', 'fall-winter-24'),
(3, 'SOLD_OUT',    '2024-03-01 10:00:00', 'Lightweight construction, maximum movement. Pigment-dyed canvas and technical mesh built for the streets in summer.',             false, 'https://images.unsplash.com/photo-1772997657431-6c3272ccaf15?w=1400&h=900&fit=crop&auto=format', 'SPRING / SUMMER 24', 'SS24', 'spring-summer-24')
ON CONFLICT DO NOTHING^^

-- ─── Roles ────────────────────────────────────────────────────
INSERT INTO public.roles (id, name) VALUES
(1, 'USER'),
(2, 'ADMIN')
ON CONFLICT DO NOTHING^^

-- ─── Users ────────────────────────────────────────────────────
INSERT INTO public.users (id, email, name, password, phone, surname) VALUES
(1, 'mario.rossi@email.com', 'Mario', '{bcrypt}$2a$10$R34/vvODmsPUs6brVBthCuK.iG.DW3mdgjNMY840lli3I2rYZvH4u', '1234567890', 'Rossi'),
(2, 'suca.rossi@email.com',  'ales',  '{bcrypt}$2a$10$uvWgkyFO6AQ6X7K9VrSYTOpnmihFGRDCEdHyVQXputEyxvAJEt7am', '1234567890', 'mess'),
(3, 'lucone@email.com',      'Luca',  '{bcrypt}$2a$10$JJuOC1Y3QPdCr1gZLr9B9emoeQkEjJoVxYb000Ww8ix6fwf9bynHi', '1234567890', 'Ruggeri'),
(4, 'gay@email.com',         'Augu',  '{bcrypt}$2a$10$jB4bLLTznZiaaCQlSHwaluthkPzSp.KEhsDnoKZwCq2BWtD9QZcfK',  '1234567890', 'Ricchione'),
(5, 'teonegro@email.com',    'Teo',   '{bcrypt}$2a$10$NneB/vVm42m0adtY7NOwCOjUyrSTz9odGkTzvamotk4cYPZhCdUi2', '1234567890', 'Simenza')
ON CONFLICT DO NOTHING^^

-- ─── Role - User ──────────────────────────────────────────────
INSERT INTO public.role_user (user_id, role_id) VALUES
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1)
ON CONFLICT DO NOTHING^^

-- ─── Products ─────────────────────────────────────────────────
INSERT INTO public.products (id, compare_price, created_at, description, featured, name, price, slug, tag, updated_at, collection_id) VALUES
(1,  192.40, '2025-03-01 10:00:00', '950gsm French terry. Oversized silhouette. Raw edge seams.',                       false, 'WRAITH HOODIE',        148.00, 'wraith-hoodie',        'NEW DROP', '2025-03-01 10:00:00', 1),
(2,  275.60, '2025-03-01 10:00:00', '16oz selvedge denim. Articulated knee. 8 functional pockets.',                    false, 'EXOSKELETON CARGO',    212.00, 'exoskeleton-cargo',    'LIMITED',  '2025-03-01 10:00:00', 1),
(3,  387.40, '2025-03-01 10:00:00', 'Reflective nylon shell. Quilted liner. Internal media pocket.',                   false, 'SIGNAL BOMBER',        298.00, 'signal-bomber',        'NEW DROP', '2025-03-01 10:00:00', 1),
(4,   88.40, '2025-03-01 10:00:00', 'Washed cotton twill. Embroidered logo. Unstructured fit.',                        false, 'VOID CAMP CAP',         68.00, 'void-camp-cap',        'RESTOCKED','2025-03-01 10:00:00', 1),
(5,  114.40, '2024-09-01 10:00:00', '240gsm supima cotton. Garment dyed. Oversized boxy cut.',                         false, 'FRACTURE TEE',          88.00, 'fracture-tee',         'SOLD OUT', '2024-09-01 10:00:00', 2),
(6,  231.40, '2025-03-01 10:00:00', 'Technical jersey. Tapered fit. Zip ankle and side pockets.',                      false, 'BLACKOUT TRACK',       178.00, 'blackout-track',       'NEW DROP', '2025-03-01 10:00:00', 1),
(7,  426.40, '2024-09-01 10:00:00', 'Waxed cotton ripstop. Storm flap. Removable liner.',                              false, 'STATIC FIELD JACKET',  328.00, 'static-field-jacket',  'SOLD OUT', '2024-09-01 10:00:00', 2),
(8,  254.80, '2024-09-01 10:00:00', 'Japanese merino wool. Dropped shoulder. Ribbed hem and cuffs.',                   false, 'GRID KNIT SWEATER',    196.00, 'grid-knit-sweater',    'SOLD OUT', '2024-09-01 10:00:00', 2),
(9,  244.40, '2024-03-01 10:00:00', 'Stonewashed canvas. Six-pocket utility. Cinch ankle.',                            false, 'SHADOW CARGO',         188.00, 'shadow-cargo',         'SOLD OUT', '2024-03-01 10:00:00', 3),
(10, 179.40, '2024-03-01 10:00:00', 'Heavyweight fleece. Exposed seam construction. Kangaroo pocket.',                 false, 'RAW SEAM HOODIE',      138.00, 'raw-seam-hoodie',      'SOLD OUT', '2024-03-01 10:00:00', 3)
ON CONFLICT DO NOTHING^^

-- ─── Product Images ───────────────────────────────────────────
INSERT INTO public.product_images (id, alt_text, image_url, is_primary, product_id) VALUES
(1,  'WRAITH HOODIE',        'https://images.unsplash.com/photo-1768084356884-22bb77e76931?w=600&h=750&fit=crop&auto=format', true, 1),
(2,  'EXOSKELETON CARGO',    'https://images.unsplash.com/photo-1762666167421-ff983e35ba98?w=600&h=750&fit=crop&auto=format', true, 2),
(3,  'SIGNAL BOMBER',        'https://images.unsplash.com/photo-1767036840968-91dc63654cd7?w=600&h=750&fit=crop&auto=format', true, 3),
(4,  'VOID CAMP CAP',        'https://images.unsplash.com/photo-1767036841733-cf7f5c40c18d?w=600&h=750&fit=crop&auto=format', true, 4),
(5,  'FRACTURE TEE',         'https://images.unsplash.com/photo-1763750581767-b367bcd6c117?w=600&h=750&fit=crop&auto=format', true, 5),
(6,  'BLACKOUT TRACK',       'https://images.unsplash.com/photo-1762666168682-8229f2a62305?w=600&h=750&fit=crop&auto=format', true, 6),
(7,  'STATIC FIELD JACKET',  'https://images.unsplash.com/photo-1759188863379-ae70c6f84bac?w=600&h=750&fit=crop&auto=format', true, 7),
(8,  'GRID KNIT SWEATER',    'https://images.unsplash.com/photo-1604689010734-72dc5075512e?w=600&h=750&fit=crop&auto=format', true, 8),
(9,  'SHADOW CARGO',         'https://images.unsplash.com/photo-1646633632840-1c95d61c909e?w=600&h=750&fit=crop&auto=format', true, 9),
(10, 'RAW SEAM HOODIE',      'https://images.unsplash.com/photo-1752674316405-549b77559eb3?w=600&h=750&fit=crop&auto=format', true, 10)
ON CONFLICT DO NOTHING^^

-- ─── Product Variants ─────────────────────────────────────────
INSERT INTO public.product_variants (id, created_at, size, sku, stock_quantity, updated_at, product_id) VALUES
(1,  '2025-03-01 10:00:00', 'XS',       'WRAITH-HOODIE-XS',          10, '2025-03-01 10:00:00', 1),
(2,  '2025-03-01 10:00:00', 'S',        'WRAITH-HOODIE-S',           10, '2025-03-01 10:00:00', 1),
(3,  '2025-03-01 10:00:00', 'M',        'WRAITH-HOODIE-M',           10, '2025-03-01 10:00:00', 1),
(4,  '2025-03-01 10:00:00', 'L',        'WRAITH-HOODIE-L',           10, '2025-03-01 10:00:00', 1),
(5,  '2025-03-01 10:00:00', 'XL',       'WRAITH-HOODIE-XL',          10, '2025-03-01 10:00:00', 1),
(6,  '2025-03-01 10:00:00', '28',       'EXOSKELETON-CARGO-28',      10, '2025-03-01 10:00:00', 2),
(7,  '2025-03-01 10:00:00', '30',       'EXOSKELETON-CARGO-30',      10, '2025-03-01 10:00:00', 2),
(8,  '2025-03-01 10:00:00', '32',       'EXOSKELETON-CARGO-32',      10, '2025-03-01 10:00:00', 2),
(9,  '2025-03-01 10:00:00', '34',       'EXOSKELETON-CARGO-34',      10, '2025-03-01 10:00:00', 2),
(10, '2025-03-01 10:00:00', '36',       'EXOSKELETON-CARGO-36',      10, '2025-03-01 10:00:00', 2),
(11, '2025-03-01 10:00:00', 'XS',       'SIGNAL-BOMBER-XS',          10, '2025-03-01 10:00:00', 3),
(12, '2025-03-01 10:00:00', 'S',        'SIGNAL-BOMBER-S',           10, '2025-03-01 10:00:00', 3),
(13, '2025-03-01 10:00:00', 'M',        'SIGNAL-BOMBER-M',           10, '2025-03-01 10:00:00', 3),
(14, '2025-03-01 10:00:00', 'L',        'SIGNAL-BOMBER-L',           10, '2025-03-01 10:00:00', 3),
(15, '2025-03-01 10:00:00', 'XL',       'SIGNAL-BOMBER-XL',          10, '2025-03-01 10:00:00', 3),
(16, '2025-03-01 10:00:00', 'ONE SIZE', 'VOID-CAMP-CAP-ONESIZE',     10, '2025-03-01 10:00:00', 4),
(17, '2024-09-01 10:00:00', 'S',        'FRACTURE-TEE-S',            10, '2024-09-01 10:00:00', 5),
(18, '2024-09-01 10:00:00', 'M',        'FRACTURE-TEE-M',            10, '2024-09-01 10:00:00', 5),
(19, '2024-09-01 10:00:00', 'L',        'FRACTURE-TEE-L',            10, '2024-09-01 10:00:00', 5),
(20, '2025-03-01 10:00:00', 'XS',       'BLACKOUT-TRACK-XS',         10, '2025-03-01 10:00:00', 6),
(21, '2025-03-01 10:00:00', 'S',        'BLACKOUT-TRACK-S',          10, '2025-03-01 10:00:00', 6),
(22, '2025-03-01 10:00:00', 'M',        'BLACKOUT-TRACK-M',          10, '2025-03-01 10:00:00', 6),
(23, '2025-03-01 10:00:00', 'L',        'BLACKOUT-TRACK-L',          10, '2025-03-01 10:00:00', 6),
(24, '2025-03-01 10:00:00', 'XL',       'BLACKOUT-TRACK-XL',         10, '2025-03-01 10:00:00', 6),
(25, '2024-09-01 10:00:00', 'S',        'STATIC-FIELD-JACKET-S',     10, '2024-09-01 10:00:00', 7),
(26, '2024-09-01 10:00:00', 'M',        'STATIC-FIELD-JACKET-M',     10, '2024-09-01 10:00:00', 7),
(27, '2024-09-01 10:00:00', 'L',        'STATIC-FIELD-JACKET-L',     10, '2024-09-01 10:00:00', 7),
(28, '2024-09-01 10:00:00', 'XL',       'STATIC-FIELD-JACKET-XL',    10, '2024-09-01 10:00:00', 7),
(29, '2024-09-01 10:00:00', 'XS',       'GRID-KNIT-SWEATER-XS',      10, '2024-09-01 10:00:00', 8),
(30, '2024-09-01 10:00:00', 'S',        'GRID-KNIT-SWEATER-S',       10, '2024-09-01 10:00:00', 8),
(31, '2024-09-01 10:00:00', 'M',        'GRID-KNIT-SWEATER-M',       10, '2024-09-01 10:00:00', 8),
(32, '2024-09-01 10:00:00', 'L',        'GRID-KNIT-SWEATER-L',       10, '2024-09-01 10:00:00', 8),
(33, '2024-03-01 10:00:00', '28',       'SHADOW-CARGO-28',           10, '2024-03-01 10:00:00', 9),
(34, '2024-03-01 10:00:00', '30',       'SHADOW-CARGO-30',           10, '2024-03-01 10:00:00', 9),
(35, '2024-03-01 10:00:00', '32',       'SHADOW-CARGO-32',           10, '2024-03-01 10:00:00', 9),
(36, '2024-03-01 10:00:00', '34',       'SHADOW-CARGO-34',           10, '2024-03-01 10:00:00', 9),
(37, '2024-03-01 10:00:00', 'S',        'RAW-SEAM-HOODIE-S',         10, '2024-03-01 10:00:00', 10),
(38, '2024-03-01 10:00:00', 'M',        'RAW-SEAM-HOODIE-M',         10, '2024-03-01 10:00:00', 10),
(39, '2024-03-01 10:00:00', 'L',        'RAW-SEAM-HOODIE-L',         10, '2024-03-01 10:00:00', 10),
(40, '2024-03-01 10:00:00', 'XL',       'RAW-SEAM-HOODIE-XL',        10, '2024-03-01 10:00:00', 10)
ON CONFLICT DO NOTHING^^

-- ─── Addresses ───────────────────────────────────────────────
INSERT INTO public.addresses (id, city, country, full_name, postal_code, province, street) VALUES
(1, 'Milano',      'IT', 'Mario Rossi',   '20100', 'Milano',     'Via Roma 10'),
(2, 'Roma',        'IT', 'Luca Ruggeri',  '00100', 'Roma',       'Via del Corso 25')
ON CONFLICT DO NOTHING^^

-- ─── User Addresses ─────────────────────────────────────────
INSERT INTO public.user_addresses (id, full_name, street, city, postal_code, province, country, is_default, user_id) VALUES
(1, 'Mario Rossi',  'Via Roma 10',      'Milano', '20100', 'Milano', 'IT', true,  1),
(2, 'Luca Ruggeri', 'Via del Corso 25', 'Roma',   '00100', 'Roma',   'IT', true,  3)
ON CONFLICT DO NOTHING^^

-- ─── Orders ───────────────────────────────────────────────────
INSERT INTO public.orders (id, created_at, order_number, payment_status, shipping_price, stripe_payment_intent, subtotal, total_price, billing_address_id, shipping_address_id, user_id) VALUES
(1, '2025-03-15 14:30:00', 'ORD-20250315-001', 'PAID',      5.99,  NULL, 148.00, 153.99, 1, 1, 1),
(2, '2025-03-20 09:15:00', 'ORD-20250320-002', 'PAID',      8.99,  NULL, 578.00, 586.99, 2, 2, 3)
ON CONFLICT DO NOTHING^^

-- ─── Order Items ──────────────────────────────────────────────
INSERT INTO public.order_items (id, product_name, product_price, quantity, size, order_id, product_id, product_variant_id) VALUES
(1, 'WRAITH HOODIE',         148.00, 1, 'L',        1, 1, 4),
(2, 'EXOSKELETON CARGO',     212.00, 1, '32',       2, 2, 8),
(3, 'SIGNAL BOMBER',         298.00, 1, 'M',        2, 3, 13),
(4, 'VOID CAMP CAP',          68.00, 1, 'ONE SIZE', 2, 4, 16)
ON CONFLICT DO NOTHING^^

-- ─── Sequences ────────────────────────────────────────────────
SELECT setval('public.collections_id_seq',      (SELECT MAX(id) FROM public.collections))^^
SELECT setval('public.roles_id_seq',             (SELECT MAX(id) FROM public.roles))^^
SELECT setval('public.users_id_seq',             (SELECT MAX(id) FROM public.users))^^
SELECT setval('public.products_id_seq',          (SELECT MAX(id) FROM public.products))^^
SELECT setval('public.product_images_id_seq',    (SELECT MAX(id) FROM public.product_images))^^
SELECT setval('public.product_variants_id_seq',  (SELECT MAX(id) FROM public.product_variants))^^
SELECT setval('public.addresses_id_seq',         (SELECT MAX(id) FROM public.addresses))^^
SELECT setval('public.user_addresses_id_seq',    (SELECT COALESCE(MAX(id), 0) FROM public.user_addresses))^^
SELECT setval('public.orders_id_seq',            (SELECT MAX(id) FROM public.orders))^^
SELECT setval('public.order_items_id_seq',       (SELECT MAX(id) FROM public.order_items))^^

SET session_replication_role = DEFAULT^^



-- ─── Funzione slugify ─────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.slugify(value TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN lower(
        regexp_replace(
            regexp_replace(
                trim(value),
                '[^a-z0-9\s-]', '', 'gi'
            ),
            '\s+', '-', 'g'
        )
    );
END;
$$ LANGUAGE plpgsql^^

-- ─── Funzione trigger collections ────────────────────────────
CREATE OR REPLACE FUNCTION generate_collection_slug()
RETURNS TRIGGER AS $$
DECLARE
  base_slug TEXT;
  final_slug TEXT;
  counter INTEGER := 2;
BEGIN
  IF TG_OP = 'INSERT'
     OR NEW.slug IS NULL
     OR NEW.slug = ''
     OR NEW.name IS DISTINCT FROM OLD.name THEN
    base_slug := public.slugify(NEW.name);
    final_slug := base_slug;
    IF final_slug IS NULL OR final_slug = '' THEN
      RAISE EXCEPTION 'Cannot generate slug from empty collection name';
    END IF;
    WHILE EXISTS (
      SELECT 1 FROM collections c
      WHERE c.slug = final_slug
        AND (TG_OP = 'INSERT' OR c.id <> NEW.id)
    ) LOOP
      final_slug := base_slug || '-' || counter;
      counter := counter + 1;
    END LOOP;
    NEW.slug := final_slug;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql^^

-- ─── Funzione trigger products ────────────────────────────────
CREATE OR REPLACE FUNCTION generate_product_slug()
RETURNS TRIGGER AS $$
DECLARE
  base_slug TEXT;
  final_slug TEXT;
  counter INTEGER := 2;
BEGIN
  IF TG_OP = 'INSERT'
     OR NEW.slug IS NULL
     OR NEW.slug = ''
     OR NEW.name IS DISTINCT FROM OLD.name THEN
    base_slug := public.slugify(NEW.name);
    final_slug := base_slug;
    IF final_slug IS NULL OR final_slug = '' THEN
      RAISE EXCEPTION 'Cannot generate slug from empty product name';
    END IF;
    WHILE EXISTS (
      SELECT 1 FROM products p
      WHERE p.slug = final_slug
        AND (TG_OP = 'INSERT' OR p.id <> NEW.id)
    ) LOOP
      final_slug := base_slug || '-' || counter;
      counter := counter + 1;
    END LOOP;
    NEW.slug := final_slug;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql^^

-- ─── Trigger collections ──────────────────────────────────────
DROP TRIGGER IF EXISTS trigger_generate_collection_slug ON collections^^
CREATE TRIGGER trigger_generate_collection_slug
    BEFORE INSERT OR UPDATE ON collections
    FOR EACH ROW
    EXECUTE FUNCTION generate_collection_slug()^^

-- ─── Trigger products ─────────────────────────────────────────
DROP TRIGGER IF EXISTS trigger_generate_product_slug ON products^^
CREATE TRIGGER trigger_generate_product_slug
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION generate_product_slug()^^