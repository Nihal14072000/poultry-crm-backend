-- Idempotent migration to add tenant_id (BIGINT NOT NULL DEFAULT 1) to all demo tables and views.

ALTER TABLE demo.user_account ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.employee ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.vehicle ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.buying_party ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.selling_party ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.trip ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.delievery ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.trip_party_relation ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.delievery_party_relation ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.supplier_pickup ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.vendor_delievery ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.payment ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.expenses ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo."order" ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.order_type ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.party_order_relation ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.mortality ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.finance_order_settlement ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE demo.finance_payment_allocation ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;
ALTER TABLE IF EXISTS demo.generated_report ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1;

-- Allow one ADMIN per tenant instead of one ADMIN globally.
DROP INDEX IF EXISTS demo.user_account_single_admin;
CREATE UNIQUE INDEX IF NOT EXISTS user_account_single_admin_per_tenant
    ON demo.user_account USING btree (tenant_id)
    WHERE ((role)::text = 'ADMIN'::text);

CREATE INDEX IF NOT EXISTS idx_user_account_tenant ON demo.user_account (tenant_id);
CREATE INDEX IF NOT EXISTS idx_employee_tenant ON demo.employee (tenant_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_tenant ON demo.vehicle (tenant_id);
CREATE INDEX IF NOT EXISTS idx_buying_party_tenant ON demo.buying_party (tenant_id);
CREATE INDEX IF NOT EXISTS idx_selling_party_tenant ON demo.selling_party (tenant_id);
CREATE INDEX IF NOT EXISTS idx_trip_tenant ON demo.trip (tenant_id);
CREATE INDEX IF NOT EXISTS idx_order_tenant ON demo."order" (tenant_id);
CREATE INDEX IF NOT EXISTS idx_payment_tenant ON demo.payment (tenant_id);
CREATE INDEX IF NOT EXISTS idx_expenses_tenant ON demo.expenses (tenant_id);
CREATE INDEX IF NOT EXISTS idx_fin_settlement_tenant ON demo.finance_order_settlement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_generated_report_tenant ON demo.generated_report (tenant_id, requested_by, created_at DESC);

DROP VIEW IF EXISTS demo.finance_buyer_receivables_v;
CREATE VIEW demo.finance_buyer_receivables_v
AS SELECT s.id AS settlement_id,
          s.tenant_id,
          s.trip_id,
          s.order_id,
          s.buying_party_id,
          bp.name AS buying_party_name,
          s.selling_party_id,
          sp.name AS selling_party_name,
          ot.name AS order_type,
          o.qty,
          o.weight,
          o.rate,
          s.currency_code,
          s.order_amount,
          s.brokerage_amount,
          s.brokerage_payer,
          s.buyer_due_amount,
          COALESCE(paid.received_amount, 0::numeric)::numeric(14,2) AS received_amount,
              (s.buyer_due_amount - COALESCE(paid.received_amount, 0::numeric))::numeric(14,2) AS outstanding_amount,
              s.due_date,
          CASE
              WHEN s.status::text = 'VOID'::text THEN 'VOID'::text
              WHEN s.buyer_due_amount <= COALESCE(paid.received_amount, 0::numeric) THEN 'SETTLED'::text
              WHEN COALESCE(paid.received_amount, 0::numeric) > 0::numeric THEN 'PARTIAL'::text
            WHEN s.due_date < CURRENT_DATE THEN 'OVERDUE'::text
            ELSE 'OPEN'::text
END AS balance_status
   FROM demo.finance_order_settlement s
     JOIN demo.buying_party bp ON bp.id = s.buying_party_id
     JOIN demo.selling_party sp ON sp.id = s.selling_party_id
     JOIN demo."order" o ON o.id = s.order_id
     LEFT JOIN demo.order_type ot ON ot.id = o.order_type_id
     LEFT JOIN LATERAL ( SELECT sum(a.allocated_amount) AS received_amount
           FROM demo.finance_payment_allocation a
             JOIN demo.payment p ON p.id = a.payment_id
          WHERE a.settlement_id = s.id AND a.allocation_type::text = 'BUYER_RECEIPT'::text AND p.payment_type::text = 'BUYER_RECEIPT'::text AND p.status::text = 'COMPLETED'::text) paid ON true;

DROP VIEW IF EXISTS demo.finance_seller_payables_v;
CREATE VIEW demo.finance_seller_payables_v
AS SELECT s.id AS settlement_id,
          s.tenant_id,
          s.trip_id,
          s.order_id,
          s.selling_party_id,
          sp.name AS selling_party_name,
          s.buying_party_id,
          bp.name AS buying_party_name,
          ot.name AS order_type,
          s.currency_code,
          s.order_amount,
          s.brokerage_amount,
          s.brokerage_payer,
          s.seller_due_amount,
          COALESCE(paid.paid_amount, 0::numeric)::numeric(14,2) AS paid_amount,
                  (s.seller_due_amount - COALESCE(paid.paid_amount, 0::numeric))::numeric(14,2) AS outstanding_amount,
                  s.due_date,
          CASE
              WHEN s.status::text = 'VOID'::text THEN 'VOID'::text
              WHEN s.seller_due_amount <= COALESCE(paid.paid_amount, 0::numeric) THEN 'SETTLED'::text
              WHEN COALESCE(paid.paid_amount, 0::numeric) > 0::numeric THEN 'PARTIAL'::text
            WHEN s.due_date < CURRENT_DATE THEN 'OVERDUE'::text
            ELSE 'OPEN'::text
END AS balance_status
   FROM demo.finance_order_settlement s
     JOIN demo.selling_party sp ON sp.id = s.selling_party_id
     JOIN demo.buying_party bp ON bp.id = s.buying_party_id
     JOIN demo."order" o ON o.id = s.order_id
     LEFT JOIN demo.order_type ot ON ot.id = o.order_type_id
     LEFT JOIN LATERAL ( SELECT sum(a.allocated_amount) AS paid_amount
           FROM demo.finance_payment_allocation a
             JOIN demo.payment p ON p.id = a.payment_id
          WHERE a.settlement_id = s.id AND a.allocation_type::text = 'SELLER_PAYOUT'::text AND p.payment_type::text = 'SELLER_PAYOUT'::text AND p.status::text = 'COMPLETED'::text) paid ON true;

DROP VIEW IF EXISTS demo.finance_trip_payment_summary_v;
CREATE VIEW demo.finance_trip_payment_summary_v
AS SELECT s.trip_id,
          s.tenant_id,
          count(*) AS settlement_count,
          sum(s.buyer_due_amount)::numeric(14,2) AS total_buyer_due,
                  sum(COALESCE(br.received_amount, 0::numeric))::numeric(14,2) AS total_received,
                  sum(s.buyer_due_amount - COALESCE(br.received_amount, 0::numeric))::numeric(14,2) AS buyer_outstanding,
                  sum(s.seller_due_amount)::numeric(14,2) AS total_seller_due,
                  sum(COALESCE(spay.paid_amount, 0::numeric))::numeric(14,2) AS total_paid_to_sellers,
                  sum(s.seller_due_amount - COALESCE(spay.paid_amount, 0::numeric))::numeric(14,2) AS seller_outstanding,
                  sum(s.brokerage_amount)::numeric(14,2) AS brokerage_assessed
   FROM demo.finance_order_settlement s
            LEFT JOIN LATERAL ( SELECT sum(a.allocated_amount) AS received_amount
                                FROM demo.finance_payment_allocation a
                                         JOIN demo.payment p ON p.id = a.payment_id
                                WHERE a.settlement_id = s.id AND a.allocation_type::text = 'BUYER_RECEIPT'::text AND p.payment_type::text = 'BUYER_RECEIPT'::text AND p.status::text = 'COMPLETED'::text) br ON true
                  LEFT JOIN LATERAL ( SELECT sum(a.allocated_amount) AS paid_amount
                  FROM demo.finance_payment_allocation a
                  JOIN demo.payment p ON p.id = a.payment_id
                  WHERE a.settlement_id = s.id AND a.allocation_type::text = 'SELLER_PAYOUT'::text AND p.payment_type::text = 'SELLER_PAYOUT'::text AND p.status::text = 'COMPLETED'::text) spay ON true
   WHERE s.status::text <> 'VOID'::text
   GROUP BY s.trip_id, s.tenant_id;
