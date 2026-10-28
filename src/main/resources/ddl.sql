-- DROP SCHEMA demo;

CREATE SCHEMA demo AUTHORIZATION postgres;

-- DROP SEQUENCE demo.buying_party_id_seq;

CREATE SEQUENCE demo.buying_party_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.delievery_id_seq;

CREATE SEQUENCE demo.delievery_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.employee_id_seq;

CREATE SEQUENCE demo.employee_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.finance_order_settlement_id_seq;

CREATE SEQUENCE demo.finance_order_settlement_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.finance_payment_allocation_id_seq;

CREATE SEQUENCE demo.finance_payment_allocation_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.mortality_id_seq;

CREATE SEQUENCE demo.mortality_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.order_id_seq;

CREATE SEQUENCE demo.order_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.order_type_id_seq;

CREATE SEQUENCE demo.order_type_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.party_order_relation_relation_id_seq;

CREATE SEQUENCE demo.party_order_relation_relation_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.payment_id_seq;

CREATE SEQUENCE demo.payment_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.selling_party_id_seq;

CREATE SEQUENCE demo.selling_party_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.supplier_pickup_id_seq;

CREATE SEQUENCE demo.supplier_pickup_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.trip_party_relation_relation_id_seq;

CREATE SEQUENCE demo.trip_party_relation_relation_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.trip_trip_id_seq;

CREATE SEQUENCE demo.trip_trip_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.user_account_id_seq;

CREATE SEQUENCE demo.user_account_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.vehicle_id_seq;

CREATE SEQUENCE demo.vehicle_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE demo.vendor_delievery_id_seq;

CREATE SEQUENCE demo.vendor_delievery_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;-- demo.delievery definition

-- Drop table

-- DROP TABLE demo.delievery;

CREATE TABLE demo.delievery ( id serial4 NOT NULL, trip_id int4 NULL, supplier_pickup_id int4 NULL, vendor_delievery_id int4 NULL, mortality_id int4 NULL, payment_id int4 NULL, expense_id int4 NULL, status int4 NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT delievery_pkey PRIMARY KEY (id));


-- demo.delievery_party_relation definition

-- Drop table

-- DROP TABLE demo.delievery_party_relation;

CREATE TABLE demo.delievery_party_relation ( id int4 NOT NULL, delivery_id int4 NULL, party_id int4 NULL, party_type varchar(50) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, create_date timestamp DEFAULT CURRENT_TIMESTAMP NULL, updated_date timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT delievery_party_relation_pkey PRIMARY KEY (id));


-- demo.employee definition

-- Drop table

-- DROP TABLE demo.employee;

CREATE TABLE demo.employee ( id serial4 NOT NULL, "name" varchar(250) NULL, address varchar(250) NULL, "number" varchar(250) NULL, "type" varchar(250) NULL, liecnse varchar(250) NULL, status varchar(250) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT employee_pkey PRIMARY KEY (id));


-- demo.expenses definition

-- Drop table

-- DROP TABLE demo.expenses;

CREATE TABLE demo.expenses ( id int4 NOT NULL, selling_party_id int4 NULL, actual_order_id int4 NULL, note varchar(500) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, create_date timestamp DEFAULT CURRENT_TIMESTAMP NULL, updated_date timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT expenses_pkey PRIMARY KEY (id));


-- demo.mortality definition

-- Drop table

-- DROP TABLE demo.mortality;

CREATE TABLE demo.mortality ( id serial4 NOT NULL, trip_id int4 NULL, combined_mortality int4 NULL, reason varchar(255) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, create_date timestamp DEFAULT CURRENT_TIMESTAMP NULL, updated_date timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT mortality_pkey PRIMARY KEY (id));


-- demo.order_type definition

-- Drop table

-- DROP TABLE demo.order_type;

CREATE TABLE demo.order_type ( id serial4 NOT NULL, "name" varchar(100) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, CONSTRAINT order_type_pkey PRIMARY KEY (id));


-- demo.party_order_relation definition

-- Drop table

-- DROP TABLE demo.party_order_relation;

CREATE TABLE demo.party_order_relation ( relation_id serial4 NOT NULL, party_id int4 NOT NULL, order_id int4 NOT NULL, is_active bool NULL, is_deleted bool NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL, party_type varchar(250) NULL);


-- demo.supplier_pickup definition

-- Drop table

-- DROP TABLE demo.supplier_pickup;

CREATE TABLE demo.supplier_pickup ( id int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL, buying_party_id int4 NULL, actual_order_id int4 NULL, note varchar(500) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, created_ate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updated_date timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT supplier_pickup_pkey PRIMARY KEY (id));


-- demo.trip_party_relation definition

-- Drop table

-- DROP TABLE demo.trip_party_relation;

CREATE TABLE demo.trip_party_relation ( relation_id serial4 NOT NULL, trip_id int4 NOT NULL, party_id int4 NOT NULL, party_type varchar(15) NOT NULL, is_active bool NULL, is_deleted bool NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL);


-- demo.vehicle definition

-- Drop table

-- DROP TABLE demo.vehicle;

CREATE TABLE demo.vehicle ( id serial4 NOT NULL, "name" varchar(100) NULL, veh_number varchar(50) NULL, registration_no varchar(100) NULL, reg_validity date NULL, insurance_no varchar(100) NULL, insurance_validity date NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT vehicle_pkey PRIMARY KEY (id));


-- demo.vendor_delievery definition

-- Drop table

-- DROP TABLE demo.vendor_delievery;

CREATE TABLE demo.vendor_delievery ( id serial4 NOT NULL, selling_party_id int4 NULL, actual_order_id int4 NULL, note varchar(500) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, create_date timestamp DEFAULT CURRENT_TIMESTAMP NULL, updated_date timestamp NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT vendor_delievery_pkey PRIMARY KEY (id));


-- demo."order" definition

-- Drop table

-- DROP TABLE demo."order";

CREATE TABLE demo."order" ( id serial4 NOT NULL, order_type_id int4 NULL, qty int4 NULL, weight numeric(10, 2) NULL, rate numeric(10, 2) NULL, total numeric(12, 2) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT order_pkey PRIMARY KEY (id), CONSTRAINT order_order_type_id_fkey FOREIGN KEY (order_type_id) REFERENCES demo.order_type(id));


-- demo.selling_party definition

-- Drop table

-- DROP TABLE demo.selling_party;

CREATE TABLE demo.selling_party ( id serial4 NOT NULL, "name" varchar(150) NULL, contact varchar(50) NULL, "location" text NULL, order_id int4 NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT selling_party_pkey PRIMARY KEY (id), CONSTRAINT selling_party_order_id_fkey FOREIGN KEY (order_id) REFERENCES demo."order"(id));


-- demo.trip definition

-- Drop table

-- DROP TABLE demo.trip;

CREATE TABLE demo.trip ( trip_id serial4 NOT NULL, vehicle_id int4 NULL, driver_id int4 NULL, supervisor_id int4 NULL, route text NULL, "date" date NULL, pickup_id int4 NULL, drop_id int4 NULL, status varchar(50) NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL, buying_party_id int8 NULL, selling_party_id int8 NULL, CONSTRAINT trip_pkey PRIMARY KEY (trip_id), CONSTRAINT trip_vehicle_id_fkey FOREIGN KEY (vehicle_id) REFERENCES demo.vehicle(id));


-- demo.user_account definition

-- Drop table

-- DROP TABLE demo.user_account;

CREATE TABLE demo.user_account ( id int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL, username varchar(100) NOT NULL, password_hash varchar(100) NOT NULL, "role" varchar(20) NOT NULL, employee_id int4 NULL, enabled bool DEFAULT true NOT NULL, CONSTRAINT user_account_employee_id_key UNIQUE (employee_id), CONSTRAINT user_account_pkey PRIMARY KEY (id), CONSTRAINT user_account_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'EMPLOYEE'::character varying])::text[]))), CONSTRAINT user_account_role_employee_check CHECK (((((role)::text = 'ADMIN'::text) AND (employee_id IS NULL)) OR (((role)::text = 'EMPLOYEE'::text) AND (employee_id IS NOT NULL)))), CONSTRAINT user_account_username_key UNIQUE (username), CONSTRAINT user_account_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES demo.employee(id));
CREATE UNIQUE INDEX user_account_single_admin ON demo.user_account USING btree (role) WHERE ((role)::text = 'ADMIN'::text);


-- demo.buying_party definition

-- Drop table

-- DROP TABLE demo.buying_party;

CREATE TABLE demo.buying_party ( id serial4 NOT NULL, "name" varchar(150) NULL, contact varchar(50) NULL, "location" text NULL, order_id int4 NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, createdate timestamp DEFAULT CURRENT_TIMESTAMP NULL, updateddate timestamp DEFAULT CURRENT_TIMESTAMP NULL, created_by int4 NULL, updated_by int4 NULL, CONSTRAINT buying_party_pkey PRIMARY KEY (id), CONSTRAINT buying_party_order_id_fkey FOREIGN KEY (order_id) REFERENCES demo."order"(id));


-- demo.finance_order_settlement definition

-- Drop table

-- DROP TABLE demo.finance_order_settlement;

CREATE TABLE demo.finance_order_settlement ( id int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL, trip_id int4 NOT NULL, order_id int4 NOT NULL, buying_party_id int4 NOT NULL, selling_party_id int4 NOT NULL, settlement_date date DEFAULT CURRENT_DATE NOT NULL, due_date date NULL, currency_code bpchar(3) DEFAULT 'INR'::bpchar NOT NULL, order_amount numeric(14, 2) NOT NULL, brokerage_amount numeric(14, 2) DEFAULT 0 NOT NULL, brokerage_payer varchar(10) NOT NULL, buyer_due_amount numeric(14, 2) GENERATED ALWAYS AS ((order_amount +
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 WHEN brokerage_payer::text = 'BUYER'::text THEN brokerage_amount
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 ELSE 0::numeric
END)) STORED NULL, seller_due_amount numeric(14, 2) GENERATED ALWAYS AS ((order_amount -
                                                                          CASE
                                                                              WHEN brokerage_payer::text = 'SELLER'::text THEN brokerage_amount
                                                                              ELSE 0::numeric
END)) STORED NULL, status varchar(20) DEFAULT 'OPEN'::character varying NOT NULL, notes text NULL, created_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL, CONSTRAINT chk_finance_seller_brokerage CHECK ((((brokerage_payer)::text = 'BUYER'::text) OR (brokerage_amount <= order_amount))), CONSTRAINT chk_finance_settlement_due_date CHECK (((due_date IS NULL) OR (due_date >= settlement_date))), CONSTRAINT finance_order_settlement_brokerage_amount_check CHECK ((brokerage_amount >= (0)::numeric)), CONSTRAINT finance_order_settlement_brokerage_payer_check CHECK (((brokerage_payer)::text = ANY ((ARRAY['BUYER'::character varying, 'SELLER'::character varying])::text[]))), CONSTRAINT finance_order_settlement_order_amount_check CHECK ((order_amount >= (0)::numeric)), CONSTRAINT finance_order_settlement_pkey PRIMARY KEY (id), CONSTRAINT finance_order_settlement_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'OPEN'::character varying, 'PARTIAL'::character varying, 'SETTLED'::character varying, 'VOID'::character varying])::text[]))), CONSTRAINT uq_finance_settlement_party_order UNIQUE (trip_id, order_id, buying_party_id, selling_party_id), CONSTRAINT finance_order_settlement_buying_party_id_fkey FOREIGN KEY (buying_party_id) REFERENCES demo.buying_party(id) ON DELETE RESTRICT, CONSTRAINT finance_order_settlement_order_id_fkey FOREIGN KEY (order_id) REFERENCES demo."order"(id) ON DELETE RESTRICT, CONSTRAINT finance_order_settlement_selling_party_id_fkey FOREIGN KEY (selling_party_id) REFERENCES demo.selling_party(id) ON DELETE RESTRICT, CONSTRAINT finance_order_settlement_trip_id_fkey FOREIGN KEY (trip_id) REFERENCES demo.trip(trip_id) ON DELETE RESTRICT);
CREATE INDEX idx_fin_settlement_buyer ON demo.finance_order_settlement USING btree (buying_party_id);
CREATE INDEX idx_fin_settlement_order ON demo.finance_order_settlement USING btree (order_id);
CREATE INDEX idx_fin_settlement_seller ON demo.finance_order_settlement USING btree (selling_party_id);
CREATE INDEX idx_fin_settlement_status_due ON demo.finance_order_settlement USING btree (status, due_date);
CREATE INDEX idx_fin_settlement_trip ON demo.finance_order_settlement USING btree (trip_id);


-- demo.payment definition

-- Drop table

-- DROP TABLE demo.payment;

CREATE TABLE demo.payment ( id int4 NOT NULL, buying_party_id int4 NULL, actual_order_id int4 NULL, note varchar(500) NULL, isactive bool DEFAULT true NULL, isdeleted bool DEFAULT false NULL, create_date timestamp DEFAULT CURRENT_TIMESTAMP NULL, updated_date timestamp NULL, created_by int4 NULL, updated_by int4 NULL, payment_type varchar(20) NULL, amount numeric(14, 2) NULL, payment_date date NULL, payment_method varchar(30) NULL, external_reference varchar(100) NULL, selling_party_id int4 NULL, status varchar(20) DEFAULT 'PENDING'::character varying NOT NULL, CONSTRAINT payment_amount_chk CHECK (((amount IS NULL) OR (amount > (0)::numeric))), CONSTRAINT payment_method_chk CHECK (((payment_method IS NULL) OR ((payment_method)::text = ANY ((ARRAY['BANK_TRANSFER'::character varying, 'CASH'::character varying, 'CHEQUE'::character varying, 'UPI'::character varying, 'CARD'::character varying, 'OTHER'::character varying])::text[])))), CONSTRAINT payment_party_by_type_chk CHECK (((payment_type IS NULL) OR (((payment_type)::text = 'BUYER_RECEIPT'::text) AND (buying_party_id IS NOT NULL) AND (selling_party_id IS NULL)) OR (((payment_type)::text = 'SELLER_PAYOUT'::text) AND (selling_party_id IS NOT NULL) AND (buying_party_id IS NULL)))), CONSTRAINT payment_payment_type_chk CHECK (((payment_type IS NULL) OR ((payment_type)::text = ANY ((ARRAY['BUYER_RECEIPT'::character varying, 'SELLER_PAYOUT'::character varying])::text[])))), CONSTRAINT payment_pkey PRIMARY KEY (id), CONSTRAINT payment_status_chk CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'VOID'::character varying])::text[]))), CONSTRAINT payment_selling_party_fk FOREIGN KEY (selling_party_id) REFERENCES demo.selling_party(id) ON DELETE RESTRICT);
CREATE INDEX idx_payment_actual_order ON demo.payment USING btree (actual_order_id);
CREATE INDEX idx_payment_buying_party ON demo.payment USING btree (buying_party_id);
CREATE INDEX idx_payment_selling_party ON demo.payment USING btree (selling_party_id);
CREATE INDEX idx_payment_type_status_date ON demo.payment USING btree (payment_type, status, payment_date);


-- demo.finance_payment_allocation definition

-- Drop table

-- DROP TABLE demo.finance_payment_allocation;

CREATE TABLE demo.finance_payment_allocation ( id int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL, payment_id int4 NOT NULL, settlement_id int4 NOT NULL, allocation_type varchar(20) NOT NULL, allocated_amount numeric(14, 2) NOT NULL, allocation_date date DEFAULT CURRENT_DATE NOT NULL, note varchar(500) NULL, created_at timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL, CONSTRAINT finance_payment_allocation_allocated_amount_check CHECK ((allocated_amount > (0)::numeric)), CONSTRAINT finance_payment_allocation_allocation_type_check CHECK (((allocation_type)::text = ANY ((ARRAY['BUYER_RECEIPT'::character varying, 'SELLER_PAYOUT'::character varying])::text[]))), CONSTRAINT finance_payment_allocation_pkey PRIMARY KEY (id), CONSTRAINT finance_payment_allocation_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES demo.payment(id) ON DELETE RESTRICT, CONSTRAINT finance_payment_allocation_settlement_id_fkey FOREIGN KEY (settlement_id) REFERENCES demo.finance_order_settlement(id) ON DELETE RESTRICT);
CREATE INDEX idx_fin_payment_allocation_payment ON demo.finance_payment_allocation USING btree (payment_id);
CREATE INDEX idx_fin_payment_allocation_settlement_type ON demo.finance_payment_allocation USING btree (settlement_id, allocation_type);


-- demo.finance_buyer_receivables_v source

CREATE OR REPLACE VIEW demo.finance_buyer_receivables_v
AS SELECT s.id AS settlement_id,
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


-- demo.finance_seller_payables_v source

CREATE OR REPLACE VIEW demo.finance_seller_payables_v
AS SELECT s.id AS settlement_id,
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


-- demo.finance_trip_payment_summary_v source

CREATE OR REPLACE VIEW demo.finance_trip_payment_summary_v
AS SELECT s.trip_id,
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
   GROUP BY s.trip_id;