--
-- PostgreSQL database dump
--

\restrict nzGZq0ps781otwKEeRHC4cJbmAyVsPKmSk6Rj1pU749KH9jw3GI5iHdHk0bHH8N

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
-- Name: asset_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asset_categories (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255),
    icon character varying(50),
    name character varying(100) NOT NULL
);


--
-- Name: asset_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.asset_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: asset_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.asset_categories_id_seq OWNED BY public.asset_categories.id;


--
-- Name: asset_usages; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: asset_usages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.asset_usages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: asset_usages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.asset_usages_id_seq OWNED BY public.asset_usages.id;


--
-- Name: assets; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: assets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.assets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: assets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.assets_id_seq OWNED BY public.assets.id;


--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_logs_id_seq OWNED BY public.audit_logs.id;


--
-- Name: chat_messages; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: chat_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_messages_id_seq OWNED BY public.chat_messages.id;


--
-- Name: maintenance_requests; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: maintenance_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.maintenance_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: maintenance_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.maintenance_requests_id_seq OWNED BY public.maintenance_requests.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    description character varying(255),
    name character varying(50) NOT NULL
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: rooms; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: rooms_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rooms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rooms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rooms_id_seq OWNED BY public.rooms.id;


--
-- Name: technician_coverage_rules; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: technician_coverage_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.technician_coverage_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: technician_coverage_rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.technician_coverage_rules_id_seq OWNED BY public.technician_coverage_rules.id;


--
-- Name: ticket_attachments; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: ticket_attachments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ticket_attachments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ticket_attachments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ticket_attachments_id_seq OWNED BY public.ticket_attachments.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
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


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: asset_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_categories ALTER COLUMN id SET DEFAULT nextval('public.asset_categories_id_seq'::regclass);


--
-- Name: asset_usages id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_usages ALTER COLUMN id SET DEFAULT nextval('public.asset_usages_id_seq'::regclass);


--
-- Name: assets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets ALTER COLUMN id SET DEFAULT nextval('public.assets_id_seq'::regclass);


--
-- Name: audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq'::regclass);


--
-- Name: chat_messages id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages ALTER COLUMN id SET DEFAULT nextval('public.chat_messages_id_seq'::regclass);


--
-- Name: maintenance_requests id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_requests ALTER COLUMN id SET DEFAULT nextval('public.maintenance_requests_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: rooms id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms ALTER COLUMN id SET DEFAULT nextval('public.rooms_id_seq'::regclass);


--
-- Name: technician_coverage_rules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technician_coverage_rules ALTER COLUMN id SET DEFAULT nextval('public.technician_coverage_rules_id_seq'::regclass);


--
-- Name: ticket_attachments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_attachments ALTER COLUMN id SET DEFAULT nextval('public.ticket_attachments_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: asset_categories; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.asset_categories (id, created_at, description, icon, name) FROM stdin;
1	2026-03-28 07:54:51.27	Máy tính bàn và laptop	bi-pc-display	Máy tính
2	2026-03-28 07:54:51.27	Projector và màn chiếu	bi-projector	Máy chiếu
3	2026-03-28 07:54:51.27	Máy lạnh và điều hòa không khí	bi-thermometer	Điều hòa
4	2026-03-28 07:54:51.27	Bàn học, ghế ngồi	bi-easel	Bàn ghế
5	2026-03-28 07:54:51.27	Router, Switch, Access Point	bi-router	Thiết bị mạng
6	2026-03-28 07:54:51.27	Monitor và TV	bi-display	Màn hình
7	2026-03-28 07:54:51.27	Printer và Scanner	bi-printer	Máy in
8	2026-03-28 07:54:51.27	Loa, microphone	bi-speaker	Thiết bị âm thanh
\.


--
-- Data for Name: asset_usages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.asset_usages (id, check_in_time, check_out_time, created_at, note, purpose, status, asset_id, room_from_id, room_to_id, user_id) FROM stdin;
1	2026-03-31 15:16:09.760931	2026-03-31 15:16:17.875922	2026-03-31 15:16:09.774883	Hoàn trả thiết bị		COMPLETED	3	1	5	1
2	2026-03-31 15:21:07.70103	2026-03-31 15:21:34.43805	2026-03-31 15:21:07.704321	Hoàn trả thiết bị		COMPLETED	3	5	5	1
3	2026-03-31 15:24:29.778087	2026-03-31 15:24:36.967518	2026-03-31 15:24:29.779717	Hoàn trả thiết bị		COMPLETED	1	3	4	1
4	2026-03-31 15:25:42.026996	2026-03-31 15:53:12.556982	2026-03-31 15:25:42.026996	Hoàn trả thiết bị		COMPLETED	3	\N	\N	1
5	2026-03-31 15:54:34.765516	2026-03-31 16:15:10.87756	2026-03-31 15:54:34.766637	Hoàn trả thiết bị		COMPLETED	1	\N	1	1
6	2026-03-31 17:22:50.810859	2026-03-31 17:22:58.314044	2026-03-31 17:22:50.830393	Hoàn trả thiết bị	Test	COMPLETED	1	2	1	1
7	2026-03-31 17:52:31.222778	2026-03-31 17:52:34.911811	2026-03-31 17:52:31.226313	Hoàn trả thiết bị		COMPLETED	1	10002	2	1
8	2026-03-31 17:57:48.658876	2026-03-31 21:33:05.903555	2026-03-31 17:57:48.662303	Phiếu sử dụng được đóng do thiết bị được đánh dấu thất lạc.		CANCELLED	1	10002	\N	1
9	2026-04-01 20:37:15.998935	2026-04-01 20:37:18.804606	2026-04-01 20:37:16.002614	Checked out from React console		COMPLETED	2	3	1	1
10	2026-04-01 20:37:35.335757	2026-04-01 20:37:36.998795	2026-04-01 20:37:35.336309	Checked out from React console		COMPLETED	3	\N	7	1
11	2026-04-01 20:37:52.956678	2026-04-01 20:37:54.99903	2026-04-01 20:37:52.957182	Checked out from React console		COMPLETED	4	1	\N	1
12	2026-04-01 20:38:34.031343	2026-04-01 20:38:36.671672	2026-04-01 20:38:34.031754	Checked out from React console		COMPLETED	5	5	\N	1
13	2026-04-01 20:38:54.50055	2026-04-01 20:39:09.068894	2026-04-01 20:38:54.501455	Checked out from React console		COMPLETED	6	4	\N	1
14	2026-04-01 22:37:42.879712	2026-04-01 22:37:57.429131	2026-04-01 22:37:42.887672	Checked out from React console		COMPLETED	1	10002	\N	1
15	2026-04-01 22:38:58.077505	2026-04-01 22:39:16.438347	2026-04-01 22:38:58.09167	Checked out from React console		COMPLETED	1	10002	\N	1
16	2026-04-01 22:42:28.957248	2026-04-02 14:05:36.252705	2026-04-01 22:42:28.958327	Checked out from React console		COMPLETED	1	10002	\N	1
10009	2026-04-02 14:09:20.590158	2026-04-02 14:09:27.898624	2026-04-02 14:09:20.592396	Checked out from React console		COMPLETED	1	10002	\N	1
\.


--
-- Data for Name: assets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.assets (id, brand, created_at, description, is_active, model, name, purchase_date, purchase_price, qa_code, qr_code_path, serial_number, status, updated_at, warranty_expiry, category_id, created_by, room_id) FROM stdin;
1	Dell	2026-03-28 07:54:51.36		t	Optiplex 7090	Máy tính DELL Optiplex 001	2023-01-15	\N	FPT-PC-001	\N	SN-DELL-001	AVAILABLE	2026-04-02 14:09:27.905503	\N	1	\N	10002
2	Dell	2026-03-28 07:54:51.36		t	Optiplex 7090	Máy tính DELL Optiplex 002	2023-01-15	\N	FPT-PC-002	\N	SN-DELL-002	BROKEN	2026-04-01 22:45:33.551134	\N	1	\N	10002
3	Epson	2026-03-28 07:54:51.36		t	EB-X49	Máy chiếu Epson EB-X49	2022-06-01	\N	FPT-PROJ-001	\N	SN-EPS-001	AVAILABLE	2026-04-01 20:37:37.003027	\N	2	\N	10002
4	Daikin	2026-03-28 07:54:51.36	\N	t	FTKF35XVMV	Điều hòa Daikin 1.5HP phòng 101	2023-03-10	\N	FPT-AC-001	\N	SN-DAI-001	AVAILABLE	2026-04-01 20:37:55.011636	\N	3	\N	10002
5	Dell	2026-03-28 07:54:51.36		t	Inspiron 15	Laptop Dell Inspiron 15	2021-08-20	\N	FPT-PC-003	\N	SN-DELL-003	AVAILABLE	2026-04-01 20:38:36.685941	\N	1	\N	10002
6	ViewSonic	2026-03-28 07:54:51.36		t	PA503S	Máy chiếu ViewSonic PA503S	2022-09-15	\N	FPT-PROJ-002	\N	SN-VS-001	AVAILABLE	2026-04-01 20:39:09.072158	\N	2	\N	10002
\.


--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.audit_logs (id, action_type, created_at, summary, target_id, target_type, actor_user_id) FROM stdin;
1	USER_ROLE_CHANGED	2026-03-31 17:44:49.392025	Qu?n tr? viên da doi quyen cua Phú Qu?c Lê Phan tu STAFF sang MAINTENANCE.	10005	USER	1
2	MAINTENANCE_ASSIGNED	2026-03-31 17:44:58.466185	Qu?n tr? viên da phan cong bao tri cho Phú Qu?c Lê Phan.	1	ASSET	1
3	MAINTENANCE_RESOLVED	2026-03-31 17:45:35.860534	Qu?n tr? viên da xac nhan sua xong cho Máy tính DELL Optiplex 001.	1	ASSET	1
4	ASSET_CHECKIN	2026-03-31 17:52:31.309657	Qu?n tr? viên dã check-in Máy tính DELL Optiplex 001 t?i Phòng h?c 102.	1	ASSET	1
5	ASSET_CHECKOUT	2026-03-31 17:52:34.957777	Qu?n tr? viên dã check-out Máy tính DELL Optiplex 001 v? kho.	1	ASSET	1
6	ASSET_CHECKIN	2026-03-31 17:57:48.691101	Quản trị viên đã check-in Máy tính DELL Optiplex 001 tới giữ nguyên vị trí.	1	ASSET	1
7	ASSET_MARKED_LOST	2026-03-31 21:33:05.939709	Quản trị viên đã đánh dấu thất lạc thiết bị Máy tính DELL Optiplex 001. Vị trí ghi nhận cuối: Store Room.	1	ASSET	1
8	ASSET_MARKED_FOUND	2026-03-31 21:33:13.401303	Quản trị viên đã đánh dấu tìm thấy thiết bị Máy tính DELL Optiplex 001 và chuyển về Store Room.	1	ASSET	1
\.


--
-- Data for Name: chat_messages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.chat_messages (id, created_at, is_system, message, message_type, sender_id, ticket_id) FROM stdin;
1	2026-04-01 17:29:07.632741	f	12	TEXT	1	2
2	2026-04-01 17:30:40.708912	f	12	TEXT	1	2
3	2026-04-01 17:40:48.408015	f	12	TEXT	1	2
4	2026-04-01 17:40:49.957112	f	12	TEXT	1	2
5	2026-04-01 17:41:22.854671	f	12	TEXT	1	2
6	2026-04-01 19:01:44.10024	t	Qu?n tr? viên dã t?o ticket TKT-1775044904026.	SYSTEM	1	3
7	2026-04-01 19:08:08.97561	f	Quốc	TEXT	1	3
8	2026-04-01 19:08:46.37535	f	Em đang kiểm rta	TEXT	10005	3
9	2026-04-01 19:08:58.974888	f	Ok	TEXT	1	3
10	2026-04-01 19:11:53.325683	f	Sản phẩm đang được sửa	TEXT	10005	3
11	2026-04-01 19:12:18.923202	f	123	TEXT	10005	3
12	2026-04-01 19:12:41.780578	f	123	TEXT	10005	3
13	2026-04-01 19:12:45.816177	f	111	TEXT	10005	3
14	2026-04-01 19:17:37.727268	f	12	TEXT	10005	3
15	2026-04-01 19:17:47.910975	t	Ticket được giao cho Phú Quốc Lê Phan.	SYSTEM	1	3
16	2026-04-01 19:17:54.767306	t	Ticket được giao cho Phú Quốc Lê Phan.	SYSTEM	1	3
17	2026-04-01 19:25:36.463004	t	Quản trị viên đã tạo ticket TKT-1775046336420.	SYSTEM	1	4
18	2026-04-01 19:35:40.926515	t	Quản trị viên đã cập nhật trạng thái sang Đang xử lý.	SYSTEM	1	4
19	2026-04-01 19:35:44.323987	t	Quản trị viên đã đánh dấu ticket là đã giải quyết.	SYSTEM	1	4
20	2026-04-01 19:35:50.971917	t	Ticket được giao cho Phú Quốc Lê Phan.	SYSTEM	1	3
21	2026-04-01 19:36:02.657046	t	Quản trị viên đã cập nhật trạng thái sang Đã giải quyết.	SYSTEM	1	3
22	2026-04-01 19:36:03.591477	t	Quản trị viên đã đánh dấu ticket là đã giải quyết.	SYSTEM	1	3
23	2026-04-01 19:36:19.582172	t	Quản trị viên đã tạo ticket TKT-1775046979544.	SYSTEM	1	5
24	2026-04-01 19:36:30.286076	t	Ticket được giao cho Phú Quốc Lê Phan.	SYSTEM	1	5
25	2026-04-01 20:36:01.649073	t	Quản trị viên đã cập nhật trạng thái sang Đã hủy.	SYSTEM	1	5
26	2026-04-01 22:45:33.568985	t	Quản trị viên đã tạoticket TKT-1775058333506.	SYSTEM	1	6
10007	2026-04-02 14:05:21.859634	t	Quản trị viên đã cập nhật trạng thái sang Đã hủy.	SYSTEM	1	6
10008	2026-04-02 15:32:26.472614	f	a	TEXT	1	6
\.


--
-- Data for Name: maintenance_requests; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.maintenance_requests (id, actual_cost, assignment_source, description, estimated_cost, issue_type, last_activity_at, priority, reported_at, reported_room_snapshot, resolution_note, resolved_at, sla_breached_at, sla_due_at, status, ticket_code, asset_id, assigned_to, reported_by) FROM stdin;
1	\N	\N	bảo trì	\N	MAINTENANCE	\N	HIGH	2026-03-15 15:05:10.100295	\N	a	2026-03-31 15:21:29.690811	\N	\N	RESOLVED	\N	1	1	1
2	\N	\N	OK	\N	MAINTENANCE	2026-04-01 17:41:22.856354	NORMAL	2026-03-31 17:24:08.426186	\N	Ok	2026-03-31 17:45:35.786942	\N	\N	RESOLVED	\N	1	1	1
3	\N	MANUAL	123	\N	BROKEN	2026-04-01 19:36:03.593327	NORMAL	2026-04-01 19:01:44.025992	Store Room	Resolved from React workspace	2026-04-01 19:36:03.564461	\N	2026-04-02 03:01:44.025992	RESOLVED	TKT-1775044904026	1	1	1
4	\N	AUTO	FPT-PC-002	\N	BROKEN	2026-04-01 19:35:44.326725	NORMAL	2026-04-01 19:25:36.420938	Store Room	Resolved from React workspace	2026-04-01 19:35:44.29647	\N	2026-04-02 03:25:36.420938	RESOLVED	TKT-1775046336420	1	1	1
5	\N	MANUAL	12	\N	BROKEN	2026-04-01 20:36:01.652552	NORMAL	2026-04-01 19:36:19.544499	Phòng th?c hành 201	\N	\N	\N	2026-04-02 03:36:19.544499	CANCELLED	TKT-1775046979544	2	10005	1
6	\N	AUTO	12	\N	BROKEN	2026-04-02 15:32:26.485103	NORMAL	2026-04-01 22:45:33.506947	Store Room	\N	\N	2026-04-02 14:04:56.086444	2026-04-02 06:45:33.506947	CANCELLED	TKT-1775058333506	2	10005	1
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notifications (id, count, created_at, href, icon, is_archived, is_read, message, notification_key, title, tone, updated_at, user_id) FROM stdin;
1	1	2026-03-31 15:33:16.557567	/assets?status=MAINTENANCE	bi-wrench-adjustable-circle	t	f	1 thi?t b? hi?n ? tr?ng thái b?o trì.	assets-maintenance	Thi?t b? dang b?o trì	info	2026-03-31 15:43:02.658501	1
2	1	2026-03-31 15:33:16.562517	/usages?status=ACTIVE	bi-arrow-left-right	t	f	1 lu?t mu?n dang ho?t d?ng.	usages-active	Thi?t b? dang du?c mu?n	primary	2026-03-31 15:43:02.659692	1
3	1	2026-03-31 15:43:02.643966	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy chi?u Epson EB-X49 (FPT-PROJ-001) t?i không d?i phòng.	usage-checkin-4	Check-in m?i	primary	2026-03-31 15:53:04.633468	1
4	1	2026-03-31 15:43:02.646601	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	usage-checkout-3	Check-out m?i	success	2026-03-31 15:53:04.63786	1
5	1	2026-03-31 15:43:02.648513	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001) t?i Phòng Lab 202.	usage-checkin-3	Check-in m?i	primary	2026-03-31 15:53:04.641205	1
6	1	2026-03-31 15:43:02.650538	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy chi?u Epson EB-X49 (FPT-PROJ-001).	usage-checkout-2	Check-out m?i	success	2026-03-31 15:53:04.643889	1
7	1	2026-03-31 15:43:02.651634	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy chi?u Epson EB-X49 (FPT-PROJ-001) t?i Phòng h?i th?o.	usage-checkin-2	Check-in m?i	primary	2026-03-31 15:53:04.648094	1
8	1	2026-03-31 15:43:02.653242	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy chi?u Epson EB-X49 (FPT-PROJ-001).	usage-checkout-1	Check-out m?i	success	2026-03-31 15:53:04.652906	1
9	1	2026-03-31 15:43:02.654278	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy chi?u Epson EB-X49 (FPT-PROJ-001) t?i Phòng h?i th?o.	usage-checkin-1	Check-in m?i	primary	2026-03-31 15:53:04.656566	1
10	1	2026-03-31 15:43:02.655839	/maintenance	bi-tools	t	t	Qu?n tr? viên v?a t?o yêu c?u b?o trì d?nh k? cho thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	maintenance-created-1	Yêu c?u b?o trì m?i	warning	2026-03-31 15:53:04.659834	1
11	1	2026-03-31 15:53:12.625292	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy chi?u Epson EB-X49 (FPT-PROJ-001).	usage-checkout-4	Check-out m?i	success	2026-03-31 15:53:15.558133	1
12	1	2026-03-31 15:54:34.812353	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001) t?i Phòng h?c 101.	usage-checkin-5	Check-in m?i	primary	2026-03-31 15:55:06.180913	1
13	1	2026-03-31 16:15:10.967893	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	usage-checkout-5	Check-out m?i	success	2026-03-31 17:07:23.528323	1
14	1	2026-03-31 17:22:51.014669	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001) t?i Phòng h?c 101.	usage-checkin-6	Check-in m?i	primary	2026-03-31 17:24:35.502336	1
15	1	2026-03-31 17:22:58.470795	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	usage-checkout-6	Check-out m?i	success	2026-03-31 17:24:35.509568	1
16	1	2026-03-31 17:24:08.547659	/maintenance	bi-tools	t	t	Qu?n tr? viên v?a t?o yêu c?u b?o trì d?nh k? cho thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	maintenance-created-2	Yêu c?u b?o trì m?i	warning	2026-03-31 17:24:35.518737	1
17	1	2026-03-31 17:52:31.432734	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001) t?i Phòng h?c 102.	usage-checkin-7	Check-in m?i	primary	2026-03-31 21:30:49.798215	1
18	1	2026-03-31 17:52:35.05688	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	usage-checkout-7	Check-out m?i	success	2026-03-31 21:30:49.801951	1
19	1	2026-03-31 17:57:48.795023	/usages	bi-box-arrow-in-right	t	t	Qu?n tr? viên v?a check-in thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001) t?i không d?i phòng.	usage-checkin-8	Check-in m?i	primary	2026-03-31 21:30:49.804056	1
20	1	2026-03-31 21:33:05.981729	/usages	bi-box-arrow-left	t	t	Qu?n tr? viên v?a check-out thi?t b? Máy tính DELL Optiplex 001 (FPT-PC-001).	usage-checkout-8	Check-out m?i	success	2026-04-01 17:29:20.931871	1
21	1	2026-04-01 17:29:07.660225	/tickets/2	bi-chat-dots	t	t	Qu?n tr? viên: 12	chat-ticket-2-1	Tin nh?n m?i trong null	primary	2026-04-01 17:40:05.069405	1
26	1	2026-04-01 19:01:44.066859	/tickets/3	bi-tools	t	t	Qu?n tr? viên dã t?o ticket TKT-1775044904026 cho Máy tính DELL Optiplex 001.	ticket_created-ticket-3-1	Ticket TKT-1775044904026	info	2026-04-01 19:01:49.372085	1
27	1	2026-04-01 19:01:44.072202	/tickets/3	bi-tools	t	t	Qu?n tr? viên dã t?o ticket TKT-1775044904026 cho Máy tính DELL Optiplex 001.	ticket_created-ticket-3-10005	Ticket TKT-1775044904026	info	2026-04-01 19:14:19.488974	10005
28	1	2026-04-01 19:01:44.157033	/tickets	bi-tools	t	t	Quản trị viên vừa tạo yêu cầu báo hỏng cho thiết bị Máy tính DELL Optiplex 001 (FPT-PC-001).	maintenance-created-3	Yêu cầu bảo trì mới	warning	2026-04-01 19:39:50.159189	1
30	1	2026-04-01 19:08:08.991533	/tickets/3	bi-chat-dots	t	t	Phú Quốc Lê Phan: 12	chat-ticket-3-1	Tin nhắn mới trong TKT-1775044904026	primary	2026-04-01 19:39:50.163894	1
31	7	2026-04-01 19:08:08.997497	/tickets/3	bi-chat-dots	t	t	Phú Quốc Lê Phan: 111	chat-ticket-3-10005	Tin nhắn mới trong TKT-1775044904026	primary	2026-04-01 19:14:18.780647	10005
39	3	2026-04-01 19:17:47.892467	/tickets/3	bi-tools	t	t	Ticket TKT-1775044904026 đã được giao cho Phú Quốc Lê Phan.	ticket_assigned-ticket-3-1	Ticket TKT-1775044904026	warning	2026-04-01 19:39:50.168242	1
40	2	2026-04-01 19:17:47.896146	/tickets/3	bi-tools	t	t	Ticket TKT-1775044904026 đã được giao cho Phú Quốc Lê Phan.	ticket_assigned-ticket-3-10005	Ticket TKT-1775044904026	warning	2026-04-01 19:24:30.289111	10005
41	1	2026-04-01 19:25:36.444287	/tickets/4	bi-tools	t	t	Quản trị viên đã tạo ticket TKT-1775046336420 cho Máy tính DELL Optiplex 001.	ticket_created-ticket-4-1	Ticket TKT-1775046336420	info	2026-04-01 19:39:50.171541	1
42	1	2026-04-01 19:25:36.448538	/tickets/4	bi-tools	f	f	Quản trị viên đã tạo ticket TKT-1775046336420 cho Máy tính DELL Optiplex 001.	ticket_created-ticket-4-10005	Ticket TKT-1775046336420	info	2026-04-01 19:25:36.448538	10005
44	1	2026-04-01 19:25:36.513802	/tickets	bi-tools	t	t	Quản trị viên vừa tạo yêu cầu báo hỏng cho thiết bị Máy tính DELL Optiplex 001 (FPT-PC-001).	maintenance-created-4	Yêu cầu bảo trì mới	warning	2026-04-01 19:39:50.173597	1
45	1	2026-04-01 19:35:40.897137	/tickets/4	bi-tools	t	t	Ticket TKT-1775046336420 đã chuyển sang Đang xử lý.	ticket_status_changed-ticket-4-1	Ticket TKT-1775046336420	info	2026-04-01 19:39:50.17723	1
46	1	2026-04-01 19:35:40.90401	/tickets/4	bi-tools	f	f	Ticket TKT-1775046336420 đã chuyển sang Đang xử lý.	ticket_status_changed-ticket-4-10005	Ticket TKT-1775046336420	info	2026-04-01 19:35:40.90401	10005
47	1	2026-04-01 19:35:44.302242	/tickets/4	bi-tools	t	t	Quản trị viên đã giải quyết ticket TKT-1775046336420.	ticket_resolved-ticket-4-1	Ticket TKT-1775046336420	success	2026-04-01 19:39:50.18043	1
48	1	2026-04-01 19:35:50.952293	/tickets/3	bi-tools	f	f	Ticket TKT-1775044904026 đã được giao cho Phú Quốc Lê Phan.	ticket_assigned-ticket-3-10005	Ticket TKT-1775044904026	warning	2026-04-01 19:35:50.952293	10005
49	1	2026-04-01 19:36:02.633254	/tickets/3	bi-tools	t	t	Ticket TKT-1775044904026 đã chuyển sang Đã giải quyết.	ticket_status_changed-ticket-3-1	Ticket TKT-1775044904026	info	2026-04-01 19:39:50.183791	1
50	1	2026-04-01 19:36:02.636884	/tickets/3	bi-tools	f	f	Ticket TKT-1775044904026 đã chuyển sang Đã giải quyết.	ticket_status_changed-ticket-3-10005	Ticket TKT-1775044904026	info	2026-04-01 19:36:02.636884	10005
51	1	2026-04-01 19:36:03.5699	/tickets/3	bi-tools	t	t	Quản trị viên đã giải quyết ticket TKT-1775044904026.	ticket_resolved-ticket-3-1	Ticket TKT-1775044904026	success	2026-04-01 19:39:50.186909	1
52	1	2026-04-01 19:36:19.561679	/tickets/5	bi-tools	t	t	Quản trị viên đã tạo ticket TKT-1775046979544 cho Máy tính DELL Optiplex 002.	ticket_created-ticket-5-1	Ticket TKT-1775046979544	info	2026-04-01 19:39:50.189506	1
53	1	2026-04-01 19:36:19.56668	/tickets/5	bi-tools	f	f	Quản trị viên đã tạo ticket TKT-1775046979544 cho Máy tính DELL Optiplex 002.	ticket_created-ticket-5-10005	Ticket TKT-1775046979544	info	2026-04-01 19:36:19.56668	10005
54	1	2026-04-01 19:36:29.186582	/tickets	bi-tools	t	t	Quản trị viên vừa tạo yêu cầu báo hỏng cho thiết bị Máy tính DELL Optiplex 002 (FPT-PC-002).	maintenance-created-5	Yêu cầu bảo trì mới	warning	2026-04-01 19:39:50.19271	1
55	1	2026-04-01 19:36:30.265496	/tickets/5	bi-tools	t	t	Ticket TKT-1775046979544 đã được giao cho Phú Quốc Lê Phan.	ticket_assigned-ticket-5-1	Ticket TKT-1775046979544	warning	2026-04-01 19:39:50.195296	1
56	1	2026-04-01 19:36:30.270623	/tickets/5	bi-tools	f	f	Ticket TKT-1775046979544 đã được giao cho Phú Quốc Lê Phan.	ticket_assigned-ticket-5-10005	Ticket TKT-1775046979544	warning	2026-04-01 19:36:30.270623	10005
57	1	2026-04-01 20:36:01.556282	/tickets/5	bi-tools	t	t	Ticket TKT-1775046979544 đã chuyển sang Đã hủy.	ticket_status_changed-ticket-5-1	Ticket TKT-1775046979544	info	2026-04-01 20:38:28.577767	1
58	1	2026-04-01 20:36:01.580645	/tickets/5	bi-tools	f	f	Ticket TKT-1775046979544 đã chuyển sang Đã hủy.	ticket_status_changed-ticket-5-10005	Ticket TKT-1775046979544	info	2026-04-01 20:36:01.581273	10005
59	1	2026-04-01 20:37:22.907451	/usages	bi-box-arrow-left	t	t	Quản trị viên vừa check-out thiết bị Máy tính DELL Optiplex 002 (FPT-PC-002).	usage-checkout-9	Check-out mới	success	2026-04-01 20:38:28.589015	1
61	1	2026-04-01 20:37:24.095138	/usages	bi-box-arrow-in-right	t	t	Quản trị viên vừa check-in thiết bị Máy tính DELL Optiplex 002 (FPT-PC-002) tới Phòng học 101.	usage-checkin-9	Check-in mới	primary	2026-04-01 20:38:28.596688	1
62	1	2026-04-01 20:37:37.681753	/usages	bi-box-arrow-left	t	t	Quản trị viên vừa check-out thiết bị Máy chiếu Epson EB-X49 (FPT-PROJ-001).	usage-checkout-10	Check-out mới	success	2026-04-01 20:38:28.605507	1
64	1	2026-04-01 20:37:41.610592	/usages	bi-box-arrow-in-right	t	t	Quản trị viên vừa check-in thiết bị Máy chiếu Epson EB-X49 (FPT-PROJ-001) tới Phòng họp.	usage-checkin-10	Check-in mới	primary	2026-04-01 20:38:28.613095	1
65	1	2026-04-01 20:37:56.317638	/usages	bi-box-arrow-left	t	t	Quản trị viên vừa check-out thiết bị Điều hòa Daikin 1.5HP phòng 101 (FPT-AC-001).	usage-checkout-11	Check-out mới	success	2026-04-01 20:38:28.624222	1
67	1	2026-04-01 20:37:59.12747	/usages	bi-box-arrow-in-right	t	t	Quản trị viên vừa check-in thiết bị Điều hòa Daikin 1.5HP phòng 101 (FPT-AC-001) tới không đổi phòng.	usage-checkin-11	Check-in mới	primary	2026-04-01 20:38:28.632011	1
68	1	2026-04-01 20:38:39.212797	/usages	bi-box-arrow-left	t	t	Quản trị viên vừa check-out thiết bị Laptop Dell Inspiron 15 (FPT-PC-003).	usage-checkout-12	Check-out mới	success	2026-04-01 22:42:27.281195	1
70	1	2026-04-01 20:38:39.221336	/usages	bi-box-arrow-in-right	t	t	Quản trị viên vừa check-in thiết bị Laptop Dell Inspiron 15 (FPT-PC-003) tới không đổi phòng.	usage-checkin-12	Check-in mới	primary	2026-04-01 22:42:27.286607	1
71	1	2026-04-01 20:39:01.568457	/usages	bi-box-arrow-in-right	t	t	Quản trị viên vừa check-in thiết bị Máy chiếu ViewSonic PA503S (FPT-PROJ-002) tới không đổi phòng.	usage-checkin-13	Check-in mới	primary	2026-04-01 22:42:27.291367	1
73	1	2026-04-01 20:39:10.023278	/usages	bi-box-arrow-left	t	t	Quản trị viên vừa check-out thiết bị Máy chiếu ViewSonic PA503S (FPT-PROJ-002).	usage-checkout-13	Check-out mới	success	2026-04-01 22:42:27.29357	1
75	1	2026-04-01 22:45:33.537137	/tickets/6	bi-tools	t	t	Quản trị viên đã tạo ticket TKT-1775058333506 cho Máy tính DELL Optiplex 002.	ticket_created-ticket-6-1	Ticket TKT-1775058333506	info	2026-04-02 14:09:43.457075	1
76	1	2026-04-01 22:45:33.54789	/tickets/6	bi-tools	f	f	Quản trị viên đã tạo ticket TKT-1775058333506 cho Máy tính DELL Optiplex 002.	ticket_created-ticket-6-10005	Ticket TKT-1775058333506	info	2026-04-01 22:45:33.54789	10005
10034	1	2026-04-02 14:04:56.24082	/tickets/6	bi-tools	f	f	Ticket TKT-1775058333506 đã quá hạn SLA.	ticket_overdue-ticket-6-10005	Ticket TKT-1775058333506	danger	2026-04-02 14:04:56.24082	10005
10035	1	2026-04-02 14:04:56.274212	/tickets/6	bi-tools	t	t	Ticket TKT-1775058333506 đã quá hạn SLA.	ticket_overdue-ticket-6-1	Ticket TKT-1775058333506	danger	2026-04-02 14:09:43.46871	1
10036	1	2026-04-02 14:05:21.817792	/tickets/6	bi-tools	f	f	Ticket TKT-1775058333506 đã chuyển sang Đã hủy.	ticket_status_changed-ticket-6-10005	Ticket TKT-1775058333506	info	2026-04-02 14:05:21.817792	10005
10037	1	2026-04-02 14:05:21.831629	/tickets/6	bi-tools	t	t	Ticket TKT-1775058333506 đã chuyển sang Đã hủy.	ticket_status_changed-ticket-6-1	Ticket TKT-1775058333506	info	2026-04-02 14:09:43.476138	1
10038	1	2026-04-02 14:09:20.669657	/usages	bi-box-arrow-in-right	t	t	Quản trị viên vừa check-in thiết bị Máy tính DELL Optiplex 001 (FPT-PC-001) tới không đổi phòng.	usage-checkin-10009-1	Check-in mới	primary	2026-04-02 14:09:43.484821	1
10039	1	2026-04-02 14:09:27.93944	/usages	bi-box-arrow-left	t	t	Quản trị viên vừa check-out thiết bị Máy tính DELL Optiplex 001 (FPT-PC-001).	usage-checkout-10009-1	Check-out mới	success	2026-04-02 14:09:41.435007	1
10040	1	2026-04-02 15:32:26.502877	/tickets/6	bi-chat-dots	f	f	Quản trị viên: a	chat-ticket-6-10005	Tin nhắn mới trong TKT-1775058333506	primary	2026-04-02 15:32:26.502877	10005
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.roles (id, description, name) FROM stdin;
1	Quản trị viên hệ thống	ADMIN
2	Nhân viên	STAFF
10002	Nhan vien bao tri	MAINTENANCE
\.


--
-- Data for Name: rooms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.rooms (id, building, capacity, code, created_at, description, floor, is_active, name) FROM stdin;
1	Tòa A	40	P101	2026-03-28 07:54:51.233333	\N	1	t	Phòng học 101
2	Tòa A	40	P102	2026-03-28 07:54:51.233333	\N	1	t	Phòng học 102
3	Tòa A	30	P201	2026-03-28 07:54:51.233333	\N	2	t	Phòng thực hành 201
4	Tòa A	30	P202	2026-03-28 07:54:51.233333	\N	2	t	Phòng Lab 202
5	Tòa B	60	P301	2026-03-28 07:54:51.233333	\N	3	t	Phòng hội thảo
6	Tòa B	20	P401	2026-03-28 07:54:51.233333	\N	4	t	Phòng giáo viên
7	Tòa B	15	P501	2026-03-28 07:54:51.233333	\N	5	t	Phòng họp
8	Tòa C	100	TT	2026-03-28 07:54:51.233333	\N	1	t	Thư viện
10002	\N	\N	STORE	2026-03-31 17:12:02.208877	Kho mac dinh cho thiet bi moi tao va thiet bi da tra.	\N	t	Store Room
\.


--
-- Data for Name: technician_coverage_rules; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.technician_coverage_rules (id, created_at, is_active, issue_type, sort_order, category_id, room_id, technician_id) FROM stdin;
\.


--
-- Data for Name: ticket_attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.ticket_attachments (id, content_type, created_at, original_name, relative_path, size, stored_name, ticket_id, uploaded_by) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, created_at, email, email_verification_expiry, email_verification_token, email_verified, full_name, is_active, password, password_reset_expiry, password_reset_token, phone, updated_at, username, role_id) FROM stdin;
1	2026-03-28 07:54:51.073333	admin@fptpoly.edu.vn	\N	\N	\N	Quản trị viên	t	$2a$12$LqlPKVNgTHuCrXk2FczoF.j7axQC2JgzjH45CTzMijBUIryzZeif6	\N	\N	0236123456	2026-03-28 07:54:51.073333	admin	1
2	2026-03-28 07:54:51.126667	nhanvien@fptpoly.edu.vn	\N	\N	\N	Nguyễn Văn A	t	$2a$12$LqlPKVNgTHuCrXk2FczoF.j7axQC2JgzjH45CTzMijBUIryzZeif6	\N	\N	0236654321	2026-03-31 15:09:17.10377	nhanvien	2
10005	2026-03-31 16:16:56.972128	lephanphuquoc@gmail.com	\N	\N	t	Phú Quốc Lê Phan	t	$2a$12$HJPXAUNEGEsxPqbmM8GVG.RJhD6vK6e4m0Kvi2XEYD5JQ5UdDyTN.	2026-03-31 16:52:44.494782	2340076f-bf6b-4f7a-ad0e-094903138a62	\N	2026-03-31 17:44:49.320889	admin1	10002
\.


--
-- Name: asset_categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.asset_categories_id_seq', 8, true);


--
-- Name: asset_usages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.asset_usages_id_seq', 10009, true);


--
-- Name: assets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.assets_id_seq', 6, true);


--
-- Name: audit_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.audit_logs_id_seq', 8, true);


--
-- Name: chat_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.chat_messages_id_seq', 10008, true);


--
-- Name: maintenance_requests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.maintenance_requests_id_seq', 6, true);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.notifications_id_seq', 10040, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.roles_id_seq', 10002, true);


--
-- Name: rooms_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rooms_id_seq', 10002, true);


--
-- Name: technician_coverage_rules_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.technician_coverage_rules_id_seq', 1, false);


--
-- Name: ticket_attachments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ticket_attachments_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 10005, true);


--
-- Name: asset_categories asset_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_categories
    ADD CONSTRAINT asset_categories_pkey PRIMARY KEY (id);


--
-- Name: asset_usages asset_usages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT asset_usages_pkey PRIMARY KEY (id);


--
-- Name: assets assets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: chat_messages chat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT chat_messages_pkey PRIMARY KEY (id);


--
-- Name: maintenance_requests maintenance_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT maintenance_requests_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: rooms rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_pkey PRIMARY KEY (id);


--
-- Name: technician_coverage_rules technician_coverage_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT technician_coverage_rules_pkey PRIMARY KEY (id);


--
-- Name: ticket_attachments ticket_attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_attachments
    ADD CONSTRAINT ticket_attachments_pkey PRIMARY KEY (id);


--
-- Name: users uk_2rjv6idry5h6maepsmyvxqbli; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_2rjv6idry5h6maepsmyvxqbli UNIQUE (password_reset_token);


--
-- Name: users uk_6312a15kar8gic56s3csfavqx; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6312a15kar8gic56s3csfavqx UNIQUE (email_verification_token);


--
-- Name: maintenance_requests uk_6amng8yousnihtq93r33goacl; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT uk_6amng8yousnihtq93r33goacl UNIQUE (ticket_code);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: assets uk_bb4tc5u9fc1e4dm4nv1gki017; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT uk_bb4tc5u9fc1e4dm4nv1gki017 UNIQUE (qa_code);


--
-- Name: asset_categories uk_hnhql4xih4qxqlvlub5vfu6rp; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_categories
    ADD CONSTRAINT uk_hnhql4xih4qxqlvlub5vfu6rp UNIQUE (name);


--
-- Name: notifications uk_notification_user_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT uk_notification_user_key UNIQUE (user_id, notification_key, is_archived);


--
-- Name: roles uk_ofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_ofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: rooms uk_pwsjifwofg0y1ux7gtd8sveqq; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT uk_pwsjifwofg0y1ux7gtd8sveqq UNIQUE (code);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: audit_logs fk17vn8rhj6qver0naebk935vkk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT fk17vn8rhj6qver0naebk935vkk FOREIGN KEY (actor_user_id) REFERENCES public.users(id);


--
-- Name: maintenance_requests fk36gtbymrxk1glfp8at3vsg19y; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT fk36gtbymrxk1glfp8at3vsg19y FOREIGN KEY (assigned_to) REFERENCES public.users(id);


--
-- Name: ticket_attachments fk3bvc6hcwg85wf2r857yq5l0sw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_attachments
    ADD CONSTRAINT fk3bvc6hcwg85wf2r857yq5l0sw FOREIGN KEY (uploaded_by) REFERENCES public.users(id);


--
-- Name: asset_usages fk5r44w6ybp2ahhcytt3613f4b1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fk5r44w6ybp2ahhcytt3613f4b1 FOREIGN KEY (room_from_id) REFERENCES public.rooms(id);


--
-- Name: technician_coverage_rules fk6vrdbbsoissxoubtp2ag95t0i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT fk6vrdbbsoissxoubtp2ag95t0i FOREIGN KEY (category_id) REFERENCES public.asset_categories(id);


--
-- Name: asset_usages fk8k4fft4nbp1c2n9hjvitmfdqp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fk8k4fft4nbp1c2n9hjvitmfdqp FOREIGN KEY (room_to_id) REFERENCES public.rooms(id);


--
-- Name: maintenance_requests fk8wguf4cbovsckuqodcqirrfeh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT fk8wguf4cbovsckuqodcqirrfeh FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: notifications fk9y21adhxn0ayjhfocscqox7bh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: maintenance_requests fkceb5jp4rqwukelhuckeixr7bh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_requests
    ADD CONSTRAINT fkceb5jp4rqwukelhuckeixr7bh FOREIGN KEY (reported_by) REFERENCES public.users(id);


--
-- Name: assets fkcwxkksxvxtrvv0sjtu5cgflep; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fkcwxkksxvxtrvv0sjtu5cgflep FOREIGN KEY (category_id) REFERENCES public.asset_categories(id);


--
-- Name: ticket_attachments fkcyhughg435brf6okjoct05x8k; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_attachments
    ADD CONSTRAINT fkcyhughg435brf6okjoct05x8k FOREIGN KEY (ticket_id) REFERENCES public.maintenance_requests(id);


--
-- Name: chat_messages fke2trs6fwy4p65m42fxojyh2ns; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fke2trs6fwy4p65m42fxojyh2ns FOREIGN KEY (ticket_id) REFERENCES public.maintenance_requests(id);


--
-- Name: asset_usages fkf4dsg43xjmgo4hefkjiyqir1x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fkf4dsg43xjmgo4hefkjiyqir1x FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- Name: asset_usages fkgfdqtum44u9qesue9d3god8o5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asset_usages
    ADD CONSTRAINT fkgfdqtum44u9qesue9d3god8o5 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: chat_messages fkgiqeap8ays4lf684x7m0r2729; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fkgiqeap8ays4lf684x7m0r2729 FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: technician_coverage_rules fkgskngglssg66vfx5tibhqj82h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT fkgskngglssg66vfx5tibhqj82h FOREIGN KEY (technician_id) REFERENCES public.users(id);


--
-- Name: assets fknetab8erpxdgdle27ywjpblp1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fknetab8erpxdgdle27ywjpblp1 FOREIGN KEY (room_id) REFERENCES public.rooms(id);


--
-- Name: assets fkomivswycaxmal9hy9ou1xlj99; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT fkomivswycaxmal9hy9ou1xlj99 FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: users fkp56c1712k691lhsyewcssf40f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkp56c1712k691lhsyewcssf40f FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: technician_coverage_rules fkpev0mbn1jk1qoep7ln8nkkqui; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technician_coverage_rules
    ADD CONSTRAINT fkpev0mbn1jk1qoep7ln8nkkqui FOREIGN KEY (room_id) REFERENCES public.rooms(id);


--
-- PostgreSQL database dump complete
--

\unrestrict nzGZq0ps781otwKEeRHC4cJbmAyVsPKmSk6Rj1pU749KH9jw3GI5iHdHk0bHH8N

