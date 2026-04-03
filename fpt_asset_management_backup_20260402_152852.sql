--
-- PostgreSQL database dump
--

\restrict CZ3aWoVfBxiEPc7CwzZ6UjF3ZPaEdMRUDfNnO5XYVeBRerqE6Z2OdKo2mXsYVYv

-- Dumped from database version 16.13 (Debian 16.13-1.pgdg13+1)
-- Dumped by pg_dump version 16.13 (Debian 16.13-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: asset_categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asset_categories (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255),
    icon character varying(50),
    name character varying(100) NOT NULL
);


ALTER TABLE public.asset_categories OWNER TO postgres;

--
-- Name: asset_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.asset_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.asset_categories_id_seq OWNER TO postgres;

--
-- Name: asset_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asset_categories_id_seq OWNED BY public.asset_categories.id;


--
-- Name: asset_usages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asset_usages (
    id bigint NOT NULL,
    check_in_time timestamp(6) without time zone NOT NULL,
    check_out_time timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    note character varying(1000),
    purpose character varying(500),
    status character varying(20),
    asset_id bigint NOT NULL,
    room_from_id bigint,
    room_to_id bigint,
    user_id bigint NOT NULL,
    CONSTRAINT asset_usages_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.asset_usages OWNER TO postgres;

--
-- Name: asset_usages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.asset_usages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.asset_usages_id_seq OWNER TO postgres;

--
-- Name: asset_usages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asset_usages_id_seq OWNED BY public.asset_usages.id;


--
-- Name: assets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.assets (
    id bigint NOT NULL,
    brand character varying(100),
    created_at timestamp(6) without time zone,
    description character varying(1000),
    is_active boolean,
    model character varying(100),
    name character varying(200) NOT NULL,
    purchase_date date,
    purchase_price numeric(18,2),
    qa_code character varying(50) NOT NULL,
    qr_code_path character varying(500),
    serial_number character varying(100),
    status character varying(30) NOT NULL,
    updated_at timestamp(6) without time zone,
    warranty_expiry date,
    category_id bigint NOT NULL,
    created_by bigint,
    room_id bigint,
    CONSTRAINT assets_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'IN_USE'::character varying, 'BROKEN'::character varying, 'MAINTENANCE'::character varying, 'LOST'::character varying])::text[])))
);


ALTER TABLE public.assets OWNER TO postgres;

--
-- Name: assets_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.assets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.assets_id_seq OWNER TO postgres;

--
-- Name: assets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.assets_id_seq OWNED BY public.assets.id;


--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_logs (
    id bigint NOT NULL,
    action_type character varying(80) NOT NULL,
    created_at timestamp(6) without time zone,
    summary character varying(500) NOT NULL,
    target_id bigint,
    target_type character varying(80) NOT NULL,
    actor_user_id bigint
);


ALTER TABLE public.audit_logs OWNER TO postgres;

--
-- Name: audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_logs_id_seq OWNER TO postgres;

--
-- Name: audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.audit_logs_id_seq OWNED BY public.audit_logs.id;


--
-- Name: chat_messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chat_messages (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    is_system boolean NOT NULL,
    message character varying(2000) NOT NULL,
    message_type character varying(30) NOT NULL,
    sender_id bigint NOT NULL,
    ticket_id bigint NOT NULL
);


ALTER TABLE public.chat_messages OWNER TO postgres;

--
-- Name: chat_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.chat_messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.chat_messages_id_seq OWNER TO postgres;

--
-- Name: chat_messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.chat_messages_id_seq OWNED BY public.chat_messages.id;


--
-- Name: maintenance_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.maintenance_requests (
    id bigint NOT NULL,
    actual_cost numeric(18,2),
    assignment_source character varying(30),
    description character varying(2000) NOT NULL,
    estimated_cost numeric(18,2),
    issue_type character varying(50) NOT NULL,
    last_activity_at timestamp(6) without time zone,
    priority character varying(20),
    reported_at timestamp(6) without time zone,
    reported_room_snapshot character varying(200),
    resolution_note character varying(2000),
    resolved_at timestamp(6) without time zone,
    sla_breached_at timestamp(6) without time zone,
    sla_due_at timestamp(6) without time zone,
    status character varying(30),
    ticket_code character varying(50),
    asset_id bigint NOT NULL,
    assigned_to bigint,
    reported_by bigint NOT NULL,
    CONSTRAINT maintenance_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'RESOLVED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.maintenance_requests OWNER TO postgres;

--
-- Name: maintenance_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.maintenance_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.maintenance_requests_id_seq OWNER TO postgres;

--
-- Name: maintenance_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.maintenance_requests_id_seq OWNED BY public.maintenance_requests.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    count bigint NOT NULL,
    created_at timestamp(6) without time zone,
    href character varying(255),
    icon character varying(50),
    is_archived boolean NOT NULL,
    is_read boolean NOT NULL,
    message character varying(500) NOT NULL,
    notification_key character varying(100) NOT NULL,
    title character varying(150) NOT NULL,
    tone character varying(30),
    updated_at timestamp(6) without time zone,
    user_id bigint NOT NULL
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notifications_id_seq OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    description character varying(255),
    name character varying(50) NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_id_seq OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: rooms; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rooms (
    id bigint NOT NULL,
    building character varying(100),
    capacity integer,
    code character varying(20) NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(500),
    floor integer,
    is_active boolean,
    name character varying(150) NOT NULL
);


ALTER TABLE public.rooms OWNER TO postgres;

--
-- Name: rooms_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rooms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rooms_id_seq OWNER TO postgres;

--
-- Name: rooms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rooms_id_seq OWNED BY public.rooms.id;


--
-- Name: technician_coverage_rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.technician_coverage_rules (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    is_active boolean NOT NULL,
    issue_type character varying(50),
    sort_order integer NOT NULL,
    category_id bigint,
    room_id bigint,
    technician_id bigint NOT NULL
);


ALTER TABLE public.technician_coverage_rules OWNER TO postgres;

--
-- Name: technician_coverage_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.technician_coverage_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.technician_coverage_rules_id_seq OWNER TO postgres;

--
-- Name: technician_coverage_rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.technician_coverage_rules_id_seq OWNED BY public.technician_coverage_rules.id;


--
-- Name: ticket_attachments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ticket_attachments (
    id bigint NOT NULL,
    content_type character varying(100) NOT NULL,
    created_at timestamp(6) without time zone,
    original_name character varying(255) NOT NULL,
    relative_path character varying(500) NOT NULL,
    size bigint NOT NULL,
    stored_name character varying(255) NOT NULL,
    ticket_id bigint NOT NULL,
    uploaded_by bigint NOT NULL
);


ALTER TABLE public.ticket_attachments OWNER TO postgres;

--
-- Name: ticket_attachments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ticket_attachments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ticket_attachments_id_seq OWNER TO postgres;

--
-- Name: ticket_attachments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ticket_attachments_id_seq OWNED BY public.ticket_attachments.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    email character varying(150),
    email_verification_expiry timestamp(6) without time zone,
    email_verification_token character varying(120),
    email_verified boolean,
    full_name character varying(150) NOT NULL,
    is_active boolean,
    password character varying(255) NOT NULL,
    password_reset_expiry timestamp(6) without time zone,
    password_reset_token character varying(120),
    phone character varying(20),
    updated_at timestamp(6) without time zone,
    username character varying(50) NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: asset_categories id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_categories ALTER COLUMN id SET DEFAULT nextval('public.asset_categories_id_seq'::regclass);


--
-- Name: asset_usages id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_usages ALTER COLUMN id SET DEFAULT nextval('public.asset_usages_id_seq'::regclass);


--
-- Name: assets id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets ALTER COLUMN id SET DEFAULT nextval('public.assets_id_seq'::regclass);


--
-- Name: audit_logs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq'::regclass);


--
-- Name: chat_messages id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages ALTER COLUMN id SET DEFAULT nextval('public.chat_messages_id_seq'::regclass);


--
-- Name: maintenance_requests id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maintenance_requests ALTER COLUMN id SET DEFAULT nextval('public.maintenance_requests_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: rooms id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms ALTER COLUMN id SET DEFAULT nextval('public.rooms_id_seq'::regclass);


--
-- Name: technician_coverage_rules id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.technician_coverage_rules ALTER COLUMN id SET DEFAULT nextval('public.technician_coverage_rules_id_seq'::regclass);


--
-- Name: ticket_attachments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ticket_attachments ALTER COLUMN id SET DEFAULT nextval('public.ticket_attachments_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: asset_categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asset_categories (id, created_at, description, icon, name) FROM stdin;
\.


--
-- Data for Name: asset_usages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asset_usages (id, check_in_time, check_out_time, created_at, note, purpose, status, asset_id, room_from_id, room_to_id, user_id) FROM stdin;
\.


--
-- Data for Name: assets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.assets (id, brand, created_at, description, is_active, model, name, purchase_date, purchase_price, qa_code, qr_code_path, serial_number, status, updated_at, warranty_expiry, category_id, created_by, room_id) FROM stdin;
\.


--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.audit_logs (id, action_type, created_at, summary, target_id, target_type, actor_user_id) FROM stdin;
\.


--
-- Data for Name: chat_messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chat_messages (id, created_at, is_system, message, message_type, sender_id, ticket_id) FROM stdin;
\.


--
-- Data for Name: maintenance_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.maintenance_requests (id, actual_cost, assignment_source, description, estimated_cost, issue_type, last_activity_at, priority, reported_at, reported_room_snapshot, resolution_note, resolved_at, sla_breached_at, sla_due_at, status, ticket_code, asset_id, assigned_to, reported_by) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, count, created_at, href, icon, is_archived, is_read, message, notification_key, title, tone, updated_at, user_id) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, description, name) FROM stdin;
1	Quan tri he thong	ADMIN
2	Nhan vien	STAFF
3	Nhan vien bao tri	MAINTENANCE
\.


--
-- Data for Name: rooms; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rooms (id, building, capacity, code, created_at, description, floor, is_active, name) FROM stdin;
\.


--
-- Data for Name: technician_coverage_rules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.technician_coverage_rules (id, created_at, is_active, issue_type, sort_order, category_id, room_id, technician_id) FROM stdin;
\.


--
-- Data for Name: ticket_attachments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ticket_attachments (id, content_type, created_at, original_name, relative_path, size, stored_name, ticket_id, uploaded_by) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, created_at, email, email_verification_expiry, email_verification_token, email_verified, full_name, is_active, password, password_reset_expiry, password_reset_token, phone, updated_at, username, role_id) FROM stdin;
1	2026-04-02 08:16:39.467749	admin@local	\N	\N	t	Administrator	t	$2a$12$wUr4y27YDJ.RDuFzHImowO8gCRcjtbTTbVwpce8q2YxCMlvx8zhAW	\N	\N	\N	2026-04-02 08:16:39.467749	admin	1
\.


--
-- Name: asset_categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asset_categories_id_seq', 1, false);


--
-- Name: asset_usages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asset_usages_id_seq', 1, false);


--
-- Name: assets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.assets_id_seq', 1, false);


--
-- Name: audit_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.audit_logs_id_seq', 1, false);


--
-- Name: chat_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.chat_messages_id_seq', 1, false);


--
-- Name: maintenance_requests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.maintenance_requests_id_seq', 1, false);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notifications_id_seq', 1, false);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_seq', 3, true);


--
-- Name: rooms_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rooms_id_seq', 1, false);


--
-- Name: technician_coverage_rules_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.technician_coverage_rules_id_seq', 1, false);


--
-- Name: ticket_attachments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ticket_attachments_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 1, true);


--
-- Name: asset_categories asset_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_categories
    ADD CONSTRAINT asset_categories_pkey PRIMARY KEY (id);


--
-- Name: asset_usages asset_usages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT asset_usages_pkey PRIMARY KEY (id);


--
-- Name: assets assets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: chat_messages chat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT chat_messages_pkey PRIMARY KEY (id);


--
-- Name: maintenance_requests maintenance_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT maintenance_requests_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: rooms rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_pkey PRIMARY KEY (id);


--
-- Name: technician_coverage_rules technician_coverage_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT technician_coverage_rules_pkey PRIMARY KEY (id);


--
-- Name: ticket_attachments ticket_attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ticket_attachments
    ADD CONSTRAINT ticket_attachments_pkey PRIMARY KEY (id);


--
-- Name: users uk_2rjv6idry5h6maepsmyvxqbli; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_2rjv6idry5h6maepsmyvxqbli UNIQUE (password_reset_token);


--
-- Name: users uk_6312a15kar8gic56s3csfavqx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6312a15kar8gic56s3csfavqx UNIQUE (email_verification_token);


--
-- Name: maintenance_requests uk_6amng8yousnihtq93r33goacl; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT uk_6amng8yousnihtq93r33goacl UNIQUE (ticket_code);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: assets uk_bb4tc5u9fc1e4dm4nv1gki017; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT uk_bb4tc5u9fc1e4dm4nv1gki017 UNIQUE (qa_code);


--
-- Name: asset_categories uk_hnhql4xih4qxqlvlub5vfu6rp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_categories
    ADD CONSTRAINT uk_hnhql4xih4qxqlvlub5vfu6rp UNIQUE (name);


--
-- Name: notifications uk_notification_user_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT uk_notification_user_key UNIQUE (user_id, notification_key, is_archived);


--
-- Name: roles uk_ofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_ofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: rooms uk_pwsjifwofg0y1ux7gtd8sveqq; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT uk_pwsjifwofg0y1ux7gtd8sveqq UNIQUE (code);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: audit_logs fk17vn8rhj6qver0naebk935vkk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT fk17vn8rhj6qver0naebk935vkk FOREIGN KEY (actor_user_id) REFERENCES public.users(id);


--
-- Name: maintenance_requests fk36gtbymrxk1glfp8at3vsg19y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT fk36gtbymrxk1glfp8at3vsg19y FOREIGN KEY (assigned_to) REFERENCES public.users(id);


--
-- Name: ticket_attachments fk3bvc6hcwg85wf2r857yq5l0sw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ticket_attachments
    ADD CONSTRAINT fk3bvc6hcwg85wf2r857yq5l0sw FOREIGN KEY (uploaded_by) REFERENCES public.users(id);


--
-- Name: asset_usages fk5r44w6ybp2ahhcytt3613f4b1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fk5r44w6ybp2ahhcytt3613f4b1 FOREIGN KEY (room_from_id) REFERENCES public.rooms(id);


--
-- Name: technician_coverage_rules fk6vrdbbsoissxoubtp2ag95t0i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT fk6vrdbbsoissxoubtp2ag95t0i FOREIGN KEY (category_id) REFERENCES public.asset_categories(id);


--
-- Name: asset_usages fk8k4fft4nbp1c2n9hjvitmfdqp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fk8k4fft4nbp1c2n9hjvitmfdqp FOREIGN KEY (room_to_id) REFERENCES public.rooms(id);


--
-- Name: maintenance_requests fk8wguf4cbovsckuqodcqirrfeh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT fk8wguf4cbovsckuqodcqirrfeh FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: notifications fk9y21adhxn0ayjhfocscqox7bh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: maintenance_requests fkceb5jp4rqwukelhuckeixr7bh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT fkceb5jp4rqwukelhuckeixr7bh FOREIGN KEY (reported_by) REFERENCES public.users(id);


--
-- Name: assets fkcwxkksxvxtrvv0sjtu5cgflep; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fkcwxkksxvxtrvv0sjtu5cgflep FOREIGN KEY (category_id) REFERENCES public.asset_categories(id);


--
-- Name: ticket_attachments fkcyhughg435brf6okjoct05x8k; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ticket_attachments
    ADD CONSTRAINT fkcyhughg435brf6okjoct05x8k FOREIGN KEY (ticket_id) REFERENCES public.maintenance_requests(id);


--
-- Name: chat_messages fke2trs6fwy4p65m42fxojyh2ns; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fke2trs6fwy4p65m42fxojyh2ns FOREIGN KEY (ticket_id) REFERENCES public.maintenance_requests(id);


--
-- Name: asset_usages fkf4dsg43xjmgo4hefkjiyqir1x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fkf4dsg43xjmgo4hefkjiyqir1x FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: asset_usages fkgfdqtum44u9qesue9d3god8o5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fkgfdqtum44u9qesue9d3god8o5 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: chat_messages fkgiqeap8ays4lf684x7m0r2729; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fkgiqeap8ays4lf684x7m0r2729 FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: technician_coverage_rules fkgskngglssg66vfx5tibhqj82h; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT fkgskngglssg66vfx5tibhqj82h FOREIGN KEY (technician_id) REFERENCES public.users(id);


--
-- Name: assets fknetab8erpxdgdle27ywjpblp1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fknetab8erpxdgdle27ywjpblp1 FOREIGN KEY (room_id) REFERENCES public.rooms(id);


--
-- Name: assets fkomivswycaxmal9hy9ou1xlj99; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fkomivswycaxmal9hy9ou1xlj99 FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: users fkp56c1712k691lhsyewcssf40f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkp56c1712k691lhsyewcssf40f FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: technician_coverage_rules fkpev0mbn1jk1qoep7ln8nkkqui; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT fkpev0mbn1jk1qoep7ln8nkkqui FOREIGN KEY (room_id) REFERENCES public.rooms(id);


--
-- PostgreSQL database dump complete
--

\unrestrict CZ3aWoVfBxiEPc7CwzZ6UjF3ZPaEdMRUDfNnO5XYVeBRerqE6Z2OdKo2mXsYVYv

