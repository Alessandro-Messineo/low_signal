-- =========================================================
-- 1) Estensione per gestire caratteri accentati
-- =========================================================
CREATE EXTENSION IF NOT EXISTS unaccent;

-- =========================================================
-- 2) Funzione base slugify
-- =========================================================
CREATE OR REPLACE FUNCTION public.slugify(value TEXT)
RETURNS TEXT
LANGUAGE SQL
IMMUTABLE
STRICT
AS $$
  SELECT trim(BOTH '-' FROM regexp_replace(
    lower(unaccent(trim(value))),
    '[^a-z0-9]+',
    '-',
    'g'
  ));
$$;

-- =========================================================
-- 3) COLLECTIONS: genera/aggiorna slug
-- =========================================================
CREATE OR REPLACE FUNCTION public.set_collection_slug()
RETURNS trigger
LANGUAGE plpgsql
AS $$
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
      SELECT 1
      FROM collections c
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
$$;

DROP TRIGGER IF EXISTS trg_collection_slug ON collections;

CREATE TRIGGER trg_collection_slug
BEFORE INSERT OR UPDATE ON collections
FOR EACH ROW
EXECUTE FUNCTION public.set_collection_slug();

-- =========================================================
-- 4) PRODUCTS: genera/aggiorna slug
-- =========================================================
CREATE OR REPLACE FUNCTION public.set_product_slug()
RETURNS trigger
LANGUAGE plpgsql
AS $$
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
      SELECT 1
      FROM products p
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
$$;

DROP TRIGGER IF EXISTS trg_product_slug ON products;

CREATE TRIGGER trg_product_slug
BEFORE INSERT OR UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION public.set_product_slug();