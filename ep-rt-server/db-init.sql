--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.5
-- Dumped by pg_dump version 9.3.5
-- Started on 2015-11-13 14:59:32

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 265 (class 3079 OID 11750)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2571 (class 0 OID 0)
-- Dependencies: 265
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 170 (class 1259 OID 116266)
-- Name: cs_col_img_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_col_img_paths (
    ip_id bigint NOT NULL,
    ip_img_id bigint NOT NULL,
    ip_path character varying(500) NOT NULL
);


--
-- TOC entry 171 (class 1259 OID 116272)
-- Name: cs_col_img_paths_ip_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_col_img_paths_ip_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 172 (class 1259 OID 116274)
-- Name: cs_col_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_col_paths (
    cp_id bigint NOT NULL,
    cp_col_id bigint NOT NULL,
    cp_path character varying(500) NOT NULL
);


--
-- TOC entry 173 (class 1259 OID 116280)
-- Name: cs_col_paths_cp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_col_paths_cp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 174 (class 1259 OID 116282)
-- Name: cs_dir_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_dir_paths (
    dp_id bigint NOT NULL,
    dp_dir_id bigint NOT NULL,
    dp_path character varying(500) NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 175 (class 1259 OID 116288)
-- Name: cs_dir_paths_dp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_dir_paths_dp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 176 (class 1259 OID 116290)
-- Name: cs_event_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_event_details (
    evd_eve_id bigint NOT NULL,
    evd_key character varying(20) NOT NULL,
    evd_value character varying(1000)
);


--
-- TOC entry 177 (class 1259 OID 116296)
-- Name: cs_events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_events (
    eve_id bigint NOT NULL,
    eve_date timestamp without time zone NOT NULL,
    eve_event_class character varying(100) NOT NULL,
    eve_service_id bigint NOT NULL
);


--
-- TOC entry 178 (class 1259 OID 116299)
-- Name: cs_events_eve_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_events_eve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 179 (class 1259 OID 116301)
-- Name: cs_fil_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_fil_paths (
    fp_id bigint NOT NULL,
    fp_fil_id bigint NOT NULL,
    fp_path character varying(500) NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 180 (class 1259 OID 116307)
-- Name: cs_fil_paths_fp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_fil_paths_fp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 181 (class 1259 OID 116309)
-- Name: cs_img_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_img_paths (
    ip_id bigint NOT NULL,
    ip_img_id bigint NOT NULL,
    ip_path character varying(500) NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 182 (class 1259 OID 116315)
-- Name: cs_img_paths_ip_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_img_paths_ip_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 183 (class 1259 OID 116317)
-- Name: cs_pub_img_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_pub_img_paths (
    ip_id bigint NOT NULL,
    ip_img_id bigint NOT NULL,
    ip_path character varying(500) NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 184 (class 1259 OID 116323)
-- Name: cs_pub_img_paths_ip_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_pub_img_paths_ip_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 185 (class 1259 OID 116325)
-- Name: cs_pub_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_pub_paths (
    pp_id bigint NOT NULL,
    pp_pub_id bigint NOT NULL,
    pp_path character varying(500) NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 186 (class 1259 OID 116331)
-- Name: cs_pub_paths_pp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_pub_paths_pp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 187 (class 1259 OID 116333)
-- Name: cs_ver_content_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_ver_content_types (
    vc_ver_id bigint NOT NULL,
    vc_content_type smallint NOT NULL
);


--
-- TOC entry 188 (class 1259 OID 116336)
-- Name: cs_ver_digests; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_ver_digests (
    vd_id bigint NOT NULL,
    vd_ver_id bigint NOT NULL,
    vp_digest bytea NOT NULL,
    vd_check_date timestamp without time zone NOT NULL
);


--
-- TOC entry 189 (class 1259 OID 116342)
-- Name: cs_ver_digests_vd_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_ver_digests_vd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 190 (class 1259 OID 116344)
-- Name: cs_ver_paths; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cs_ver_paths (
    vp_id bigint NOT NULL,
    vp_ver_id bigint NOT NULL,
    vp_path character varying(500) NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 191 (class 1259 OID 116350)
-- Name: cs_ver_paths_vp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cs_ver_paths_vp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 192 (class 1259 OID 116352)
-- Name: eve_event_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE eve_event_details (
    evd_eve_id bigint NOT NULL,
    evd_key character varying(20) NOT NULL,
    evd_value character varying(1000)
);


--
-- TOC entry 193 (class 1259 OID 116358)
-- Name: eve_events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE eve_events (
    eve_id bigint NOT NULL,
    eve_date timestamp without time zone NOT NULL,
    eve_event_class character varying(100) NOT NULL,
    eve_service_id bigint NOT NULL,
    eve_target_service_id bigint NOT NULL,
    eve_channel smallint NOT NULL
);


--
-- TOC entry 194 (class 1259 OID 116361)
-- Name: eve_events_eve_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE eve_events_eve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 195 (class 1259 OID 116363)
-- Name: met_attr_values_av_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_attr_values_av_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 196 (class 1259 OID 116365)
-- Name: met_attribute_datas; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_attribute_datas (
    ad_att_id bigint NOT NULL,
    ad_lan_id bigint NOT NULL,
    ad_name character varying(50),
    ad_description character varying(1000)
);


--
-- TOC entry 197 (class 1259 OID 116371)
-- Name: met_attribute_parents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_attribute_parents (
    ap_att_id bigint NOT NULL,
    ap_parent_id bigint NOT NULL,
    ap_dist bigint NOT NULL
);


--
-- TOC entry 198 (class 1259 OID 116374)
-- Name: met_attribute_values; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_attribute_values (
    av_id bigint NOT NULL,
    av_ext_id character varying(20),
    av_value character varying(16000),
    av_lan_id bigint NOT NULL,
    av_bas_id bigint,
    av_att_id bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 199 (class 1259 OID 116380)
-- Name: met_attributes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_attributes (
    att_id bigint NOT NULL,
    att_number integer,
    att_rdfname character varying(50) NOT NULL,
    att_role_id character varying(50),
    att_controlled boolean NOT NULL,
    att_parent_id bigint
);


--
-- TOC entry 200 (class 1259 OID 116383)
-- Name: met_attributes_att_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_attributes_att_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 201 (class 1259 OID 116385)
-- Name: met_col_datas; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_col_datas (
    cd_col_id bigint NOT NULL,
    cd_lan_id bigint NOT NULL,
    cd_name character varying(300),
    cd_description text
);


--
-- TOC entry 202 (class 1259 OID 116391)
-- Name: met_col_images; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_col_images (
    ci_col_id bigint NOT NULL,
    ci_img_content_type character varying(30)
);


--
-- TOC entry 203 (class 1259 OID 116394)
-- Name: met_col_order; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_col_order (
    co_col_id bigint NOT NULL,
    co_pub_id bigint NOT NULL,
    co_position integer NOT NULL
);


--
-- TOC entry 204 (class 1259 OID 116397)
-- Name: met_collection_parents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_collection_parents (
    cp_col_id bigint NOT NULL,
    cp_parent_id bigint NOT NULL,
    cp_dist bigint NOT NULL
);


--
-- TOC entry 205 (class 1259 OID 116400)
-- Name: met_collections; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_collections (
    col_id bigint NOT NULL,
    col_notes character varying(1000),
    col_short_name character varying(50),
    col_number integer NOT NULL,
    col_parent_id bigint,
    col_ordered boolean NOT NULL
);


--
-- TOC entry 206 (class 1259 OID 116406)
-- Name: met_collections_col_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_collections_col_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 207 (class 1259 OID 116408)
-- Name: met_del_pub_data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_del_pub_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 208 (class 1259 OID 116410)
-- Name: met_del_pub_datas; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_del_pub_datas (
    dpd_id bigint NOT NULL,
    dpd_reason character varying(500),
    dpd_date timestamp without time zone NOT NULL,
    dpd_pub_id bigint NOT NULL
);


--
-- TOC entry 209 (class 1259 OID 116416)
-- Name: met_dir_act; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_dir_act (
    da_dir_id bigint NOT NULL,
    da_act_id bigint NOT NULL
);


--
-- TOC entry 210 (class 1259 OID 116419)
-- Name: met_dir_atts; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_dir_atts (
    da_dir_id bigint NOT NULL,
    da_av_id bigint NOT NULL,
    da_number integer NOT NULL
);


--
-- TOC entry 211 (class 1259 OID 116422)
-- Name: met_directories; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_directories (
    dir_id bigint NOT NULL,
    dir_name character varying(300),
    dir_notes character varying(1000),
    dir_parent_id bigint
);


--
-- TOC entry 212 (class 1259 OID 116428)
-- Name: met_directories_dir_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_directories_dir_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 213 (class 1259 OID 116430)
-- Name: met_directory_parents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_directory_parents (
    dp_dir_id bigint NOT NULL,
    dp_parent_id bigint NOT NULL,
    dp_dist bigint NOT NULL
);


--
-- TOC entry 214 (class 1259 OID 116433)
-- Name: met_edi_atts; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_edi_atts (
    ea_edi_id bigint NOT NULL,
    ea_av_id bigint NOT NULL,
    ea_assoc_type smallint NOT NULL,
    ea_number integer NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 215 (class 1259 OID 116436)
-- Name: met_edi_datas; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_edi_datas (
    ed_edi_id bigint NOT NULL,
    ed_lan_id bigint NOT NULL,
    ed_description character varying(1000),
    ed_comment character varying(4000)
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 216 (class 1259 OID 116442)
-- Name: met_edi_images; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_edi_images (
    ei_edi_id bigint NOT NULL,
    ei_img_content_type character varying(30)
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 217 (class 1259 OID 116445)
-- Name: met_edi_ver; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_edi_ver (
    ev_edi_id bigint NOT NULL,
    ev_ver_id bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 218 (class 1259 OID 116448)
-- Name: met_editions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_editions (
    edi_id bigint NOT NULL,
    edi_name character varying(300),
    edi_state integer,
    edi_creation_date timestamp without time zone NOT NULL,
    edi_modification_date timestamp without time zone,
    edi_expiration_date timestamp without time zone,
    edi_publication_date timestamp without time zone,
    edi_notes character varying(1000),
    edi_cre_id bigint NOT NULL,
    edi_act_id bigint NOT NULL,
    edi_www_stats bigint NOT NULL,
    edi_vote_for bigint NOT NULL,
    edi_vote_against bigint NOT NULL,
    edi_pub_id bigint NOT NULL,
    edi_external_id character varying(100)
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 219 (class 1259 OID 116454)
-- Name: met_editions_edi_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_editions_edi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 220 (class 1259 OID 116456)
-- Name: met_event_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_event_details (
    evd_eve_id bigint NOT NULL,
    evd_key character varying(20) NOT NULL,
    evd_value character varying(1000)
);


--
-- TOC entry 221 (class 1259 OID 116462)
-- Name: met_events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_events (
    eve_id bigint NOT NULL,
    eve_date timestamp without time zone NOT NULL,
    eve_event_class character varying(100) NOT NULL,
    eve_service_id bigint NOT NULL
);


--
-- TOC entry 222 (class 1259 OID 116465)
-- Name: met_events_eve_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_events_eve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 223 (class 1259 OID 116467)
-- Name: met_files; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_files (
    fil_id bigint NOT NULL,
    fil_path character varying(2000) NOT NULL,
    fil_mime character varying(30),
    fil_pub_id bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 224 (class 1259 OID 116473)
-- Name: met_files_fil_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_files_fil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 225 (class 1259 OID 116475)
-- Name: met_languages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_languages (
    lan_id bigint NOT NULL,
    lan_name character varying(30),
    lan_type smallint NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 116478)
-- Name: met_languages_lan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_languages_lan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 227 (class 1259 OID 116480)
-- Name: met_pub_atts; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_pub_atts (
    pa_pub_id bigint NOT NULL,
    pa_av_id bigint NOT NULL,
    pa_assoc_type smallint NOT NULL,
    pa_number integer NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 228 (class 1259 OID 116483)
-- Name: met_pub_col; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_pub_col (
    pc_pub_id bigint NOT NULL,
    pc_col_id bigint NOT NULL,
    pc_assoc_type smallint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 229 (class 1259 OID 116486)
-- Name: met_pub_datas; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_pub_datas (
    pd_pub_id bigint NOT NULL,
    pd_lan_id bigint NOT NULL,
    pd_description character varying(1000),
    pd_comment character varying(4000)
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 230 (class 1259 OID 116492)
-- Name: met_pub_images; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_pub_images (
    pi_pub_id bigint NOT NULL,
    pi_img_content_type character varying(30)
);


--
-- TOC entry 231 (class 1259 OID 116495)
-- Name: met_publication_parents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_publication_parents (
    pp_pub_id bigint NOT NULL,
    pp_parent_id bigint NOT NULL,
    pp_dist bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 232 (class 1259 OID 116498)
-- Name: met_publications; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_publications (
    pub_id bigint NOT NULL,
    pub_name character varying(300),
    pub_secured integer,
    pub_notes character varying(1000),
    pub_position integer,
    pub_group_status smallint NOT NULL,
    pub_state smallint NOT NULL,
    pub_modification_date timestamp without time zone,
    pub_vote_for bigint NOT NULL,
    pub_vote_against bigint NOT NULL,
    pub_publishing_date timestamp without time zone,
    pub_parent_pub_id bigint,
    pub_fil_id bigint,
    pub_parent_id bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 233 (class 1259 OID 116504)
-- Name: met_publications_pub_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_publications_pub_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 234 (class 1259 OID 116506)
-- Name: met_statistics; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_statistics (
    sta_id bigint NOT NULL,
    sta_stat_date timestamp without time zone NOT NULL,
    sta_object_type character varying(100),
    sta_object_id bigint NOT NULL,
    sta_stat_type smallint NOT NULL,
    sta_stat_value bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 235 (class 1259 OID 116509)
-- Name: met_statistics_sta_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_statistics_sta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 236 (class 1259 OID 116511)
-- Name: met_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_tags (
    tag_id bigint NOT NULL,
    tag_login character varying(30) NOT NULL,
    tag_value character varying(4000) NOT NULL,
    tag_state smallint NOT NULL,
    tag_comment character varying(1000),
    tag_act_id bigint NOT NULL,
    tag_creation_date timestamp without time zone NOT NULL,
    tag_lan_id bigint NOT NULL,
    tag_edi_id bigint NOT NULL,
    tag_av_id bigint
);


--
-- TOC entry 237 (class 1259 OID 116517)
-- Name: met_tags_tag_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_tags_tag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 238 (class 1259 OID 116519)
-- Name: met_versions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE met_versions (
    ver_id bigint NOT NULL,
    ver_size bigint NOT NULL,
    ver_mod_date timestamp without time zone NOT NULL,
    ver_description character varying(200),
    ver_fil_id bigint NOT NULL
)
WITH (autovacuum_vacuum_threshold=5000, autovacuum_vacuum_scale_factor=0.0, autovacuum_analyze_threshold=5000, autovacuum_analyze_scale_factor=0.0);


--
-- TOC entry 239 (class 1259 OID 116522)
-- Name: met_versions_ver_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE met_versions_ver_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 240 (class 1259 OID 116524)
-- Name: ms_message_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ms_message_details (
    msd_msg_id bigint NOT NULL,
    msd_key character varying(50) NOT NULL,
    msd_value character varying(1000)
);


--
-- TOC entry 241 (class 1259 OID 116530)
-- Name: ms_messages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ms_messages (
    msg_id bigint NOT NULL,
    msg_date timestamp without time zone NOT NULL,
    msg_message_class character varying(100) NOT NULL,
    msg_sender_id bigint NOT NULL
);


--
-- TOC entry 242 (class 1259 OID 116533)
-- Name: ms_messages_msg_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE ms_messages_msg_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 243 (class 1259 OID 116535)
-- Name: ms_receivers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ms_receivers (
    msr_msg_id bigint NOT NULL,
    msr_act_id bigint NOT NULL
);


--
-- TOC entry 244 (class 1259 OID 116538)
-- Name: pp_user_profiles; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE pp_user_profiles (
    up_id bigint NOT NULL,
    up_section_id character varying(100) NOT NULL,
    up_content text NOT NULL,
    up_login character varying(30) NOT NULL,
    up_provider character varying(100) NOT NULL,
    up_version bigint NOT NULL,
    up_last_modification timestamp without time zone NOT NULL
);


--
-- TOC entry 245 (class 1259 OID 116544)
-- Name: pp_user_profiles_up_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE pp_user_profiles_up_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 246 (class 1259 OID 116546)
-- Name: sys_event_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sys_event_details (
    evd_eve_id bigint NOT NULL,
    evd_key character varying(20) NOT NULL,
    evd_value character varying(1000)
);


--
-- TOC entry 247 (class 1259 OID 116552)
-- Name: sys_events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sys_events (
    eve_id bigint NOT NULL,
    eve_date timestamp without time zone NOT NULL,
    eve_event_class character varying(100) NOT NULL,
    eve_service_id bigint NOT NULL
);


--
-- TOC entry 248 (class 1259 OID 116555)
-- Name: sys_events_eve_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sys_events_eve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 249 (class 1259 OID 116557)
-- Name: sys_services; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sys_services (
    ser_id bigint NOT NULL,
    ser_type character varying(200) NOT NULL,
    ser_description character varying(1000),
    ser_version character varying(100) NOT NULL,
    ser_connected integer,
    ser_password character varying(30) NOT NULL,
    ser_host character varying(1024) NOT NULL,
    ser_port integer NOT NULL
);


--
-- TOC entry 250 (class 1259 OID 116563)
-- Name: sys_services_ser_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sys_services_ser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 251 (class 1259 OID 116565)
-- Name: us_actors; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_actors (
    act_id bigint NOT NULL,
    act_name character varying(100) NOT NULL,
    act_login character varying(30) NOT NULL,
    act_locked integer,
    act_expiration timestamp without time zone,
    act_password character varying(64) NOT NULL,
    act_email character varying(100) NOT NULL,
    act_institution character varying(100) NOT NULL,
    act_type integer NOT NULL,
    act_description character varying(200),
    act_homedir bigint
);


--
-- TOC entry 252 (class 1259 OID 116571)
-- Name: us_actors_act_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE us_actors_act_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 253 (class 1259 OID 116573)
-- Name: us_admin_rights; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_admin_rights (
    ari_act_id bigint NOT NULL,
    ari_right_id character varying(20) NOT NULL
);


--
-- TOC entry 254 (class 1259 OID 116576)
-- Name: us_collection_rights; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_collection_rights (
    cri_act_id bigint NOT NULL,
    cri_collection_id bigint NOT NULL,
    cri_right_id character varying(20) NOT NULL
);


--
-- TOC entry 255 (class 1259 OID 116579)
-- Name: us_directory_rights; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_directory_rights (
    dri_act_id bigint NOT NULL,
    dri_directory_id bigint NOT NULL,
    dri_right_id character varying(20) NOT NULL
);


--
-- TOC entry 256 (class 1259 OID 116582)
-- Name: us_domains; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_domains (
    dom_id bigint NOT NULL,
    dom_ip_bottom bigint,
    dom_ip_top bigint,
    dom_domain character varying(1000),
    dom_address character varying(1000),
    dom_allow integer,
    dom_act_id bigint NOT NULL
);


--
-- TOC entry 257 (class 1259 OID 116588)
-- Name: us_domains_dom_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE us_domains_dom_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 258 (class 1259 OID 116590)
-- Name: us_event_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_event_details (
    evd_eve_id bigint NOT NULL,
    evd_key character varying(20) NOT NULL,
    evd_value character varying(1000)
);


--
-- TOC entry 259 (class 1259 OID 116596)
-- Name: us_events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_events (
    eve_id bigint NOT NULL,
    eve_date timestamp without time zone NOT NULL,
    eve_event_class character varying(100) NOT NULL,
    eve_service_id bigint NOT NULL
);


--
-- TOC entry 260 (class 1259 OID 116599)
-- Name: us_events_eve_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE us_events_eve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 261 (class 1259 OID 116601)
-- Name: us_gro_use; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_gro_use (
    gru_gro_id bigint NOT NULL,
    gru_act_id bigint NOT NULL,
    gru_supervisor boolean NOT NULL
);


--
-- TOC entry 262 (class 1259 OID 116604)
-- Name: us_ldap_gr_details_ldg_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE us_ldap_gr_details_ldg_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 263 (class 1259 OID 116606)
-- Name: us_ldap_group_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_ldap_group_details (
    lgd_id bigint NOT NULL,
    lgd_key character varying(100) NOT NULL,
    lgd_value character varying(1000) NOT NULL,
    lgd_act_id bigint NOT NULL
);


--
-- TOC entry 264 (class 1259 OID 116612)
-- Name: us_publication_rights; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE us_publication_rights (
    pri_act_id bigint NOT NULL,
    pri_publication_id bigint NOT NULL,
    pri_right_id character varying(20) NOT NULL
);


--
-- TOC entry 2470 (class 0 OID 116266)
-- Dependencies: 170
-- Data for Name: cs_col_img_paths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2572 (class 0 OID 0)
-- Dependencies: 171
-- Name: cs_col_img_paths_ip_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_col_img_paths_ip_id_seq', 1, false);


--
-- TOC entry 2472 (class 0 OID 116274)
-- Dependencies: 172
-- Data for Name: cs_col_paths; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO cs_col_paths VALUES (1, 1, '1.col');
INSERT INTO cs_col_paths VALUES (15, 19, '1.col/19.col');
INSERT INTO cs_col_paths VALUES (16, 20, '1.col/20.col');
INSERT INTO cs_col_paths VALUES (17, 21, '1.col/21.col');


--
-- TOC entry 2573 (class 0 OID 0)
-- Dependencies: 173
-- Name: cs_col_paths_cp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_col_paths_cp_id_seq', 20, true);


--
-- TOC entry 2474 (class 0 OID 116282)
-- Dependencies: 174
-- Data for Name: cs_dir_paths; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO cs_dir_paths VALUES (1, 1, '1.dir');
INSERT INTO cs_dir_paths VALUES (2056, 2057, '1.dir/2057.dir');
INSERT INTO cs_dir_paths VALUES (2414, 2415, '1.dir/2415.dir');


--
-- TOC entry 2574 (class 0 OID 0)
-- Dependencies: 175
-- Name: cs_dir_paths_dp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_dir_paths_dp_id_seq', 2500, true);


--
-- TOC entry 2476 (class 0 OID 116290)
-- Dependencies: 176
-- Data for Name: cs_event_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2477 (class 0 OID 116296)
-- Dependencies: 177
-- Data for Name: cs_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2575 (class 0 OID 0)
-- Dependencies: 178
-- Name: cs_events_eve_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_events_eve_id_seq', 1, false);


--
-- TOC entry 2479 (class 0 OID 116301)
-- Dependencies: 179
-- Data for Name: cs_fil_paths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2576 (class 0 OID 0)
-- Dependencies: 180
-- Name: cs_fil_paths_fp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_fil_paths_fp_id_seq', 1, false);


--
-- TOC entry 2481 (class 0 OID 116309)
-- Dependencies: 181
-- Data for Name: cs_img_paths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2577 (class 0 OID 0)
-- Dependencies: 182
-- Name: cs_img_paths_ip_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_img_paths_ip_id_seq', 1, false);


--
-- TOC entry 2483 (class 0 OID 116317)
-- Dependencies: 183
-- Data for Name: cs_pub_img_paths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2578 (class 0 OID 0)
-- Dependencies: 184
-- Name: cs_pub_img_paths_ip_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_pub_img_paths_ip_id_seq', 1, false);


--
-- TOC entry 2485 (class 0 OID 116325)
-- Dependencies: 185
-- Data for Name: cs_pub_paths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2579 (class 0 OID 0)
-- Dependencies: 186
-- Name: cs_pub_paths_pp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_pub_paths_pp_id_seq', 1, false);


--
-- TOC entry 2487 (class 0 OID 116333)
-- Dependencies: 187
-- Data for Name: cs_ver_content_types; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2488 (class 0 OID 116336)
-- Dependencies: 188
-- Data for Name: cs_ver_digests; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2580 (class 0 OID 0)
-- Dependencies: 189
-- Name: cs_ver_digests_vd_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_ver_digests_vd_id_seq', 1, false);


--
-- TOC entry 2490 (class 0 OID 116344)
-- Dependencies: 190
-- Data for Name: cs_ver_paths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2581 (class 0 OID 0)
-- Dependencies: 191
-- Name: cs_ver_paths_vp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cs_ver_paths_vp_id_seq', 1, false);


--
-- TOC entry 2492 (class 0 OID 116352)
-- Dependencies: 192
-- Data for Name: eve_event_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2493 (class 0 OID 116358)
-- Dependencies: 193
-- Data for Name: eve_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2582 (class 0 OID 0)
-- Dependencies: 194
-- Name: eve_events_eve_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('eve_events_eve_id_seq', 1, false);


--
-- TOC entry 2583 (class 0 OID 0)
-- Dependencies: 195
-- Name: met_attr_values_av_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_attr_values_av_id_seq', 200000, true);


--
-- TOC entry 2496 (class 0 OID 116365)
-- Dependencies: 196
-- Data for Name: met_attribute_datas; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_attribute_datas VALUES (69, 2, 'Autor', '');
INSERT INTO met_attribute_datas VALUES (85, 2, 'Sygnatura', 'sygnatura e-podręcznika, czyli numer dopuszczenia przez MEN do rejestru podręczników');
INSERT INTO met_attribute_datas VALUES (87, 2, 'Tekst alternatywny', 'pełnotekstowy opis zasobu, zgodny z wymaganiami WCAG');
INSERT INTO met_attribute_datas VALUES (88, 2, 'Licencja', 'prawa rozpowszechniania zasobu');
INSERT INTO met_attribute_datas VALUES (90, 2, 'Wymagania', 'określa wymagania odnośnie urządzeń końcowych, które muszą być spełnione, żeby konkretny alternatywny format obiektu WOMI mógł być na takim urządzeniu prezentowany');
INSERT INTO met_attribute_datas VALUES (91, 2, 'Środowisko uruchomieniowe', 'określa minimalną wersję środowiska programowego ("silnika") niezbędnego do uruchomienia danej aplikacji interaktywnej');
INSERT INTO met_attribute_datas VALUES (68, 2, 'Tytuł', 'Tytuł podręcznika, modułu treści lub zasobu WOMI');
INSERT INTO met_attribute_datas VALUES (80, 2, 'Etap edukacyjny', 'określa docelowy etap szkolny');
INSERT INTO met_attribute_datas VALUES (82, 2, 'Klasa', 'konkretne oznaczenie klasy w ramach etapu edukacyjnego');
INSERT INTO met_attribute_datas VALUES (83, 2, 'Odbiorca', 'oznaczenie czy treść jest przeznaczona dla nauczycieli');
INSERT INTO met_attribute_datas VALUES (84, 2, 'Status treści', 'określa, czy treść musi pojawić się w ramach tematu, czy jest uzupełnieniem bądź rozszerzeniem');
INSERT INTO met_attribute_datas VALUES (92, 2, 'Słowa kluczowe', 'Słowa kluczowe pozwalające na łatwiejsze wyszukanie obiektu');
INSERT INTO met_attribute_datas VALUES (94, 2, 'Identyfikator własny', 'Jeśli instytucja dostarczająca WOMI stosuje wewnętrzny system identyfikacji obiektów, tutaj można wpisać taki własny identyfikator w celu ułatwienia wyszukiwania.');
INSERT INTO met_attribute_datas VALUES (95, 2, 'Abstrakt', 'Kilkuzdaniowy opis zawartości podręcznika.');
INSERT INTO met_attribute_datas VALUES (96, 2, 'Podtytuł', 'Podtytuł podręcznika');
INSERT INTO met_attribute_datas VALUES (97, 2, 'Pierwsza wersja', 'Jeśli ten podręcznik jest kolejną wersją wcześniej opublikowanego podręcznika, tutaj należy umieścić jego identyfikator.
Wszystkie kolejne wersje podręcznika muszą mieć w tym polu ten sam identyfikator.');
INSERT INTO met_attribute_datas VALUES (98, 2, 'Typ', 'opcjonalne określenie specyficznego typu obiektu');
INSERT INTO met_attribute_datas VALUES (89, 2, 'Typ WOMI interaktywnego', 'Informacja o tym, przy pomocy jakiej techniki został stworzony obiekt interaktywny.');
INSERT INTO met_attribute_datas VALUES (86, 2, 'Okładka', 'Identyfikator WOMI zawierającego okładkę e-podręcznika (uwaga: chodzi o identyfikator widoczny w referencji kopiowanej do Worda, a nie widoczny na górnej liście)
Okładka musi spełniać następujące warunki:
- WOMI graficzne
- proporcja obrazu ok. 0,707
- alternatywny plik źródłowy przeznaczony dla EPUB');
INSERT INTO met_attribute_datas VALUES (99, 2, 'Przedmiot', 'Przedmiot, którego dotyczy podręcznik. Jest wykorzystywany do wizualnego różnicowania formatów emisyjnych podręcznika.
Szczegóły muszą być ustalone z partnerem technicznym dla każdego przedmiotu osobno.');
INSERT INTO met_attribute_datas VALUES (100, 2, 'Arkusz stylów', 'Podręczniki dodatkowo zdefiniowany arkusz stylów wpływający na jego wygląd.
Ma to podobne znaczenie, jak atrybut Przedmiot, ale daje dodatkowe możliwości różnicowania podręczników w ramach przedmiotu.
Możliwe arkusze stylów muszą być ustalone z partnerem technicznym.');
INSERT INTO met_attribute_datas VALUES (101, 2, 'Stan weryfikacji', 'Ten atrybut pozwala kontrolować cykl weryfikacji WOMI.
Dla stanów "Do weryfikacji", "Do poprawy" i "Poprawione", w podglądzie
podręcznika obok WOMI zostaną wyświetlone dodatkowe informacje.
Opublikowany obiekt musi mieć ustawiony stan na "Gotowe".');
INSERT INTO met_attribute_datas VALUES (102, 2, 'Panorama w tle', 'Ten atrybut można wypełnić tylko dla podrozdziału.
Wartość musi być identyfikatorem WOMI, które ma być zastosowane
jako panorama tła dla tego podrozdziału.');
INSERT INTO met_attribute_datas VALUES (116, 2, 'Referencje', 'Atrybut dla podręcznika - kod xml z informacjami o referencjach.');
INSERT INTO met_attribute_datas VALUES (117, 2, 'Tryb edytora', 'Informacja dla Edytora Online, jak ma traktować daną wersję podręcznika. Możliwe wartości:
- external-in-progress - wersja wrzucona z zewnętrznego edytora, będą kolejne wersje wrzucane z zewnętrznego edytora
- external-final - ostatnia wersja wrzucona z zewnętrznego edytora, nie będzie takich kolejnych
- edition-online - wersja kolekcji wrzucona (zapieczętowana) z EO
Aktualizacja danej wersji podręcznika nie może zmieniać tej wartości');
INSERT INTO met_attribute_datas VALUES (104, 2, 'Tymczasowy', 'Jeśli podręcznik ma być tymczasowy to wartość tego atrybutu należy ustawić na true. Domyślnie wartość nie jest ustawiana. Podręczniki z ustawioną wartością true są cyklicznie usuwane (co 24h).');
INSERT INTO met_attribute_datas VALUES (118, 2, 'Licencja - dodatkowe info', '');
INSERT INTO met_attribute_datas VALUES (103, 2, 'Środowisko Podręcznika', 'Jeśli treść podręcznika ma być wyświetlana w specjalnym czytniku,
należy tutaj przypisać wartość uzgodnioną z partnerem technicznym.
');
INSERT INTO met_attribute_datas VALUES (105, 2, 'Ikona w spisie treści', 'Ten atrybut można wypełnić tylko dla podrozdziału.
Wartość musi być identyfikatorem WOMI, które ma być zastosowane
jako ikona tego podrozdziału w spisie treści.');
INSERT INTO met_attribute_datas VALUES (106, 2, 'Data publikacji', 'Data w formacie: RRRR-MM-DD
Dzień, w którym podręcznik został(-anie) opublikowany.');
INSERT INTO met_attribute_datas VALUES (107, 2, 'Pokazuj uwagi techniczne', 'Należy ustawić na "Nie", jeśli uwagi techniczne dotyczące podręcznika mają być ukryte w podglądzie.
Domyślnie uwagi są pokazywane.');
INSERT INTO met_attribute_datas VALUES (108, 2, 'Język', '');
INSERT INTO met_attribute_datas VALUES (109, 2, 'Cele kształcenia - wymagania ogólne', '');
INSERT INTO met_attribute_datas VALUES (110, 2, 'Przeznaczenie', 'Atrybut wewnętrzny służący odróżnieniu zwykłych WOMI
od Katalogu Zasobów Dodatkowych');
INSERT INTO met_attribute_datas VALUES (111, 2, 'Pochodzenie', 'Atrybut wewnętrzny na potrzeby Katalogu Zasobów Dodatkowych');
INSERT INTO met_attribute_datas VALUES (112, 2, 'Kategoria', 'Atrybut wewnętrzny na potrzeby Katalogu Zasobów Dodatkowych');
INSERT INTO met_attribute_datas VALUES (113, 2, 'Podstawa programowa', 'Atrybut wewnętrzny na potrzeby Katalogu Zasobów Dodatkowych');
INSERT INTO met_attribute_datas VALUES (114, 2, 'System operacyjny', 'Atrybut wewnętrzny na potrzeby Katalogu Zasobów Dodatkowych');
INSERT INTO met_attribute_datas VALUES (115, 2, 'Opis', 'Atrybut wewnętrzny na potrzeby Katalogu Zasobów Dodatkowych');
INSERT INTO met_attribute_datas VALUES (119, 2, 'Tom', 'Atrybut używany w bardzo dużych podręcznikach, które warto rozdzielić
na kilka osobnych kolekcji. Takie kolekcje należące do jednego dużego
podręcznika muszą mieć identyczne Etap edukacyjny, Klasę i Przedmiot,
natomiast Tom to kolejne numery od 1.');


--
-- TOC entry 2497 (class 0 OID 116371)
-- Dependencies: 197
-- Data for Name: met_attribute_parents; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_attribute_parents VALUES (91, 90, 1);
INSERT INTO met_attribute_parents VALUES (96, 68, 1);
INSERT INTO met_attribute_parents VALUES (118, 88, 1);


--
-- TOC entry 2498 (class 0 OID 116374)
-- Dependencies: 198
-- Data for Name: met_attribute_values; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_attribute_values VALUES (54, NULL, 'Uczeń', 3, NULL, 83);
INSERT INTO met_attribute_values VALUES (55, NULL, 'Nauczyciel', 3, NULL, 83);
INSERT INTO met_attribute_values VALUES (56, NULL, 'Kanon', 3, NULL, 84);
INSERT INTO met_attribute_values VALUES (57, NULL, 'Uzupełniająca', 3, NULL, 84);
INSERT INTO met_attribute_values VALUES (58, NULL, 'Rozszerzająca', 3, NULL, 84);
INSERT INTO met_attribute_values VALUES (60, NULL, 'I', 3, NULL, 80);
INSERT INTO met_attribute_values VALUES (61, NULL, 'II', 3, NULL, 80);
INSERT INTO met_attribute_values VALUES (62, NULL, 'III', 3, NULL, 80);
INSERT INTO met_attribute_values VALUES (63, NULL, 'IV', 3, NULL, 80);
INSERT INTO met_attribute_values VALUES (64, NULL, '1', 3, NULL, 82);
INSERT INTO met_attribute_values VALUES (65, NULL, '2', 3, NULL, 82);
INSERT INTO met_attribute_values VALUES (66, NULL, '3', 3, NULL, 82);
INSERT INTO met_attribute_values VALUES (67, NULL, '4', 3, NULL, 82);
INSERT INTO met_attribute_values VALUES (68, NULL, '5', 3, NULL, 82);
INSERT INTO met_attribute_values VALUES (69, NULL, '6', 3, NULL, 82);
INSERT INTO met_attribute_values VALUES (71, NULL, 'CC0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (73, NULL, 'GE S.A.', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (75, NULL, 'ORE', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (77, NULL, 'shutterstock', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (80, NULL, 'Studio nagrań', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (82, NULL, 'własność prywatna', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (93, NULL, 'PŁ', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (102, NULL, 'PCSS', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (120, NULL, 'CC BY 3.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (170, NULL, 'nieznana', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (174, NULL, 'public domain', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (202, NULL, 'Free for Commercial Use', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (2400, NULL, 'przyroda', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (59, NULL, 'geogebra', 3, NULL, 89);
INSERT INTO met_attribute_values VALUES (318, NULL, 'swiffy', 3, NULL, 89);
INSERT INTO met_attribute_values VALUES (317, NULL, 'voiceover', 3, NULL, 89);
INSERT INTO met_attribute_values VALUES (2396, NULL, 'fizyka', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (1529, NULL, 'vxv', 3, NULL, 90);
INSERT INTO met_attribute_values VALUES (1531, NULL, 'vdv', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (1739, NULL, 'mapa', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (1740, NULL, 'rysunek', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (1741, NULL, 'schemat', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (1742, NULL, 'fotografia', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (1743, NULL, 'animacja', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (1744, NULL, 'film', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (2397, NULL, 'chemia', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2398, NULL, 'biologia', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (1951, NULL, 'CC BY 1.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1952, NULL, 'CC BY 2.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1953, NULL, 'CC BY 2.5', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1954, NULL, 'CC BY 4.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1955, NULL, 'CC BY SA 1.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1956, NULL, 'CC BY SA 2.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1957, NULL, 'CC BY SA 2.5', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1958, NULL, 'CC BY SA 3.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1959, NULL, 'CC BY SA 4.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (1975, NULL, 'w', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (2394, NULL, 'matematyka', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2399, NULL, 'geografia', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2395, NULL, 'język polski', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2401, NULL, 'edukacja wczesnoszkolna', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2402, NULL, 'historia i społeczeństwo', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2403, NULL, 'historia', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2404, NULL, 'informatyka', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2405, NULL, 'zajęcia komputerowe', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2406, NULL, 'wiedza o społeczeństwie', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (2407, NULL, 'edukacja dla bezpieczeństwa', 3, NULL, 99);
INSERT INTO met_attribute_values VALUES (4848, NULL, 'test', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (5307, NULL, 'biology', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (5467, NULL, 'geography', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (5498, NULL, 'reprodukcja', 3, NULL, 98);
INSERT INTO met_attribute_values VALUES (5499, NULL, 'GNU FDL', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (5500, NULL, 'GNU FDL 1.1', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (5501, NULL, 'GNU FDL 1.2', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (5502, NULL, 'GNU FDL 1.3', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (7834, NULL, 'polish', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (7835, NULL, 'history', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (12429, NULL, 'mathematics', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (12594, NULL, '3. Do poprawy', 3, NULL, 101);
INSERT INTO met_attribute_values VALUES (12592, NULL, '1. Nowe', 3, NULL, 101);
INSERT INTO met_attribute_values VALUES (12593, NULL, '2. Do weryfikacji', 3, NULL, 101);
INSERT INTO met_attribute_values VALUES (12595, NULL, '4. Poprawione', 3, NULL, 101);
INSERT INTO met_attribute_values VALUES (13462, NULL, 'wv', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (16026, NULL, 'tiles', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (17713, NULL, 'ge', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (18237, NULL, 'informatics', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (18270, NULL, 'edubezp', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (19339, NULL, 'true', 3, NULL, 104);
INSERT INTO met_attribute_values VALUES (19341, NULL, 'false', 3, NULL, 104);
INSERT INTO met_attribute_values VALUES (19706, NULL, 'uwr', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (19718, NULL, 'biologia', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (19749, NULL, 'przyroda', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (19818, NULL, 'wos', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (19916, NULL, 'historia', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (20184, NULL, 'informatyka', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (20189, NULL, 'uwr', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (20387, NULL, 'CC BY NC 3.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (20839, NULL, 'ge', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (20907, NULL, 'normal', 3, NULL, 103);
INSERT INTO met_attribute_values VALUES (20908, NULL, 'uwr', 3, NULL, 103);
INSERT INTO met_attribute_values VALUES (13611, NULL, 'ee', 3, NULL, 103);
INSERT INTO met_attribute_values VALUES (12596, NULL, '6. Gotowe', 3, NULL, 101);
INSERT INTO met_attribute_values VALUES (23654, NULL, '5. Ustalanie tekstu alternatywnego', 3, NULL, 101);
INSERT INTO met_attribute_values VALUES (23863, NULL, 'chemistry', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (24085, NULL, 'GE', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (24270, NULL, 'physics', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (24408, NULL, 'mat', 3, NULL, 103);
INSERT INTO met_attribute_values VALUES (29823, NULL, 'Nie', 3, NULL, 107);
INSERT INTO met_attribute_values VALUES (30008, NULL, 'Tak', 3, NULL, 107);
INSERT INTO met_attribute_values VALUES (34982, NULL, 'pl-PL', 3, NULL, 108);
INSERT INTO met_attribute_values VALUES (34983, NULL, 'en-US', 3, NULL, 108);
INSERT INTO met_attribute_values VALUES (34984, NULL, 'fr-FR', 3, NULL, 108);
INSERT INTO met_attribute_values VALUES (39150, NULL, 'Przyroda (Szkoła podstawowa)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39151, NULL, 'Biologia (Gimnazjum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39152, NULL, 'Chemia (Gimnazjum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39153, NULL, 'Edukacja dla bezpieczeństwa (Gimnazjum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39154, NULL, 'Fizyka (Gimnazjum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39155, NULL, 'Geografia (Gimnazjum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39156, NULL, 'Biologia (Liceum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39157, NULL, 'Chemia (Liceum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39158, NULL, 'Edukacja dla bezpieczeństwa (Liceum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39159, NULL, 'Fizyka (Liceum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (39160, NULL, 'Geografia (Liceum)', 3, NULL, 109);
INSERT INTO met_attribute_values VALUES (41667, NULL, 'dd', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (42725, NULL, 'uy', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (42736, NULL, 'ddd', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (42737, NULL, 'gg', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (42855, NULL, 'aa', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (45461, NULL, 'fizyka', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (45462, NULL, 'chemia', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (45464, NULL, 'geografia', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (52515, NULL, 'mapy', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52505, NULL, 'kzd', 3, NULL, 110);
INSERT INTO met_attribute_values VALUES (52506, NULL, 'kzd_embedded', 3, NULL, 110);
INSERT INTO met_attribute_values VALUES (52507, NULL, 'IP2', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (52508, NULL, 'POKL', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (52509, NULL, 'Scholaris', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (52510, NULL, 'pozostałe', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (52511, NULL, 'e-podreczniki', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52512, NULL, 'podręczniki', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52514, NULL, 'poradniki dla nauczycieli', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52516, NULL, 'programy nauczania', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52517, NULL, 'karty pracy', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52518, NULL, 'scenariusze lekcji', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52521, NULL, 'czcionka pisanka szkolna', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52523, NULL, 'generator kart pracy', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52519, NULL, 'lektury szkolne', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52522, NULL, 'nagrania edukacyjne', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52520, NULL, 'zdjęcia i ilustracje', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52513, NULL, 'multimedia edukacyjne', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (52607, NULL, 'windows|linux', 3, NULL, 114);
INSERT INTO met_attribute_values VALUES (53288, NULL, 'ZZZ|TTT', 3, NULL, 114);
INSERT INTO met_attribute_values VALUES (53289, NULL, 'TTT|ZZZ', 3, NULL, 114);
INSERT INTO met_attribute_values VALUES (53347, NULL, 'epo', 3, NULL, 110);
INSERT INTO met_attribute_values VALUES (54119, NULL, 'Windows|Linux', 3, NULL, 114);
INSERT INTO met_attribute_values VALUES (56935, NULL, 'matematics', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (57272, NULL, 'the-one', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (57948, NULL, 'standard-2', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (58177, NULL, 'sta', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (59430, NULL, 'gf', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (59559, NULL, 'karta do druku, 21, jesień', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (60406, NULL, 'synology', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (62361, NULL, 'galeria, fotografia, lalka, pacynka', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (69834, NULL, 'ga', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (69906, NULL, 'gry edukacyjne', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (70197, NULL, 'e-learning', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (70337, NULL, 'external-in-progress', 3, NULL, 117);
INSERT INTO met_attribute_values VALUES (70338, NULL, 'external-final', 3, NULL, 117);
INSERT INTO met_attribute_values VALUES (70339, NULL, 'edition-online', 3, NULL, 117);
INSERT INTO met_attribute_values VALUES (81406, NULL, 'testy i sprawdziany', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (81759, NULL, 'E-podręczniki 1.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (86014, NULL, 'Zanieczyszczenie Polski metalami ciężkimi', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (86160, NULL, 'standard-2-matematyka', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (98554, NULL, 'standard-2-uwr', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (99479, NULL, 'rozpuszczanie', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (102750, NULL, '14', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102737, NULL, '1', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102738, NULL, '2', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102739, NULL, '3', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102740, NULL, '4', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102741, NULL, '5', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102742, NULL, '6', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102743, NULL, '7', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102744, NULL, '8', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102745, NULL, '9', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102746, NULL, '10', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102747, NULL, '11', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102748, NULL, '12', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102749, NULL, '13', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102751, NULL, '15', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102752, NULL, '16', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102753, NULL, '17', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102754, NULL, '18', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102755, NULL, '19', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102756, NULL, '20', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102757, NULL, '21', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102758, NULL, '22', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102759, NULL, '23', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102760, NULL, '24', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102761, NULL, '25', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102762, NULL, '26', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102763, NULL, '27', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102764, NULL, '28', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102765, NULL, '29', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102766, NULL, '30', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102767, NULL, '31', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102772, NULL, '32', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102779, NULL, '39', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102773, NULL, '33', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102774, NULL, '34', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102775, NULL, '35', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102778, NULL, '38', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102776, NULL, '36', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102777, NULL, '37', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102780, NULL, '40', 3, NULL, 119);
INSERT INTO met_attribute_values VALUES (102811, NULL, 'qq', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (103575, NULL, 'standard-2-przyroda', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (103884, NULL, 'geAA', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (103952, NULL, 'geaa', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (104259, NULL, 'CC BY NC 1.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104260, NULL, 'CC BY NC 2.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104261, NULL, 'CC BY NC 2.5', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104262, NULL, 'CC BY NC 4.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104267, NULL, 'CC BY NC SA 1.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104268, NULL, 'CC BY NC SA 2.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104269, NULL, 'CC BY NC SA 2.5', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104270, NULL, 'CC BY NC SA 3.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104271, NULL, 'CC BY NC SA 4.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104275, NULL, 'CC BY NC ND 2.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104276, NULL, 'CC BY NC ND 2.5', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104277, NULL, 'CC BY NC ND 3.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104278, NULL, 'CC BY NC ND 4.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104288, NULL, 'Gwiazda podwójna', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (104308, NULL, 'rondo', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (104313, NULL, 'Rzut młotem', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (104693, NULL, 'CC BY ND 1.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104694, NULL, 'CC BY ND 2.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104695, NULL, 'CC BY ND 2.5', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104696, NULL, 'CC BY ND 3.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (104697, NULL, 'CC BY ND 4.0', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (105415, NULL, 'wirtualne wycieczki', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (108064, NULL, 'ORE', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (108065, NULL, 'Wolne Lektury', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (110523, NULL, 'reprodukcje', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (110694, NULL, 'galeria, fotografia, barometr, ciśnienie atmosferyczne', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (113251, NULL, 'tylko do użytku edukacyjnego', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (113252, NULL, 'tylko do użytku edukacyjnego na epodreczniki.pl', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (113253, NULL, 'tylko do użytku niekomercyjnego', 3, NULL, 88);
INSERT INTO met_attribute_values VALUES (121469, NULL, 'prezentacje', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (133979, NULL, 'olimpiady', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (137617, NULL, 'panoram,Anima,Eron,Figo,pogoda,park,wiosna,śnieg,ilustracje,bocian,dzieci,nauczycielka', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (142414, NULL, 'ged', 3, NULL, 100);
INSERT INTO met_attribute_values VALUES (143788, NULL, 'fotografia,57, dziecko,dziewczynka, ,mama, sprzątanie,stół,ścieranie', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (144069, NULL, 'galeria, ilustracja, mama, prasowanie, pokój', 3, NULL, 91);
INSERT INTO met_attribute_values VALUES (153167, NULL, 'nasz elementarz', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (158641, NULL, 'układ okresowy pierwiastków', 3, NULL, 112);
INSERT INTO met_attribute_values VALUES (161347, NULL, 'MEN', 3, NULL, 111);
INSERT INTO met_attribute_values VALUES (169234, NULL, 'podstawa programowa', 3, NULL, 112);


--
-- TOC entry 2499 (class 0 OID 116380)
-- Dependencies: 199
-- Data for Name: met_attributes; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_attributes VALUES (91, 1, 'SrodowiskoUruchomieniowe', NULL, false, 90);
INSERT INTO met_attributes VALUES (68, 0, 'Tytul', 'title', false, NULL);
INSERT INTO met_attributes VALUES (69, 1, 'Autor', 'creator', false, NULL);
INSERT INTO met_attributes VALUES (80, 2, 'EtapEdukacyjny', NULL, true, NULL);
INSERT INTO met_attributes VALUES (82, 3, 'Klasa', NULL, true, NULL);
INSERT INTO met_attributes VALUES (96, 1, 'Podtytul', NULL, false, 68);
INSERT INTO met_attributes VALUES (99, 4, 'Przedmiot', NULL, true, NULL);
INSERT INTO met_attributes VALUES (112, 33, 'Kategoria', NULL, false, NULL);
INSERT INTO met_attributes VALUES (118, 1, 'LicencjaDodatkoweInfo', NULL, false, 88);
INSERT INTO met_attributes VALUES (119, 5, 'Tom', NULL, true, NULL);
INSERT INTO met_attributes VALUES (100, 6, 'ArkuszStylow', NULL, false, NULL);
INSERT INTO met_attributes VALUES (83, 7, 'Odbiorca', NULL, true, NULL);
INSERT INTO met_attributes VALUES (84, 8, 'StatusTresci', NULL, true, NULL);
INSERT INTO met_attributes VALUES (85, 9, 'Sygnatura', NULL, false, NULL);
INSERT INTO met_attributes VALUES (95, 10, 'Abstrakt', NULL, false, NULL);
INSERT INTO met_attributes VALUES (86, 11, 'Okladka', NULL, false, NULL);
INSERT INTO met_attributes VALUES (102, 12, 'PanoramaWTle', NULL, false, NULL);
INSERT INTO met_attributes VALUES (105, 13, 'IkonaWSpisieTresci', NULL, false, NULL);
INSERT INTO met_attributes VALUES (103, 14, 'SrodowiskoPodrecznika', NULL, true, NULL);
INSERT INTO met_attributes VALUES (87, 15, 'TekstAlternatywny', NULL, false, NULL);
INSERT INTO met_attributes VALUES (88, 16, 'Licencja', NULL, true, NULL);
INSERT INTO met_attributes VALUES (89, 17, 'TypWOMI', NULL, true, NULL);
INSERT INTO met_attributes VALUES (90, 18, 'Wymagania', NULL, false, NULL);
INSERT INTO met_attributes VALUES (92, 19, 'SlowaKluczowe', NULL, false, NULL);
INSERT INTO met_attributes VALUES (98, 20, 'TypSemantyczny', NULL, true, NULL);
INSERT INTO met_attributes VALUES (94, 21, 'IdentyfikatorWlasny', NULL, false, NULL);
INSERT INTO met_attributes VALUES (106, 22, 'DataPublikacji', NULL, false, NULL);
INSERT INTO met_attributes VALUES (97, 23, 'RootID', NULL, false, NULL);
INSERT INTO met_attributes VALUES (116, 24, 'Referencje', NULL, false, NULL);
INSERT INTO met_attributes VALUES (101, 26, 'StanWeryfikacji', NULL, true, NULL);
INSERT INTO met_attributes VALUES (104, 27, 'temp', NULL, true, NULL);
INSERT INTO met_attributes VALUES (107, 28, 'PokazujUwagiTechniczne', NULL, true, NULL);
INSERT INTO met_attributes VALUES (108, 29, 'Jezyk', NULL, true, NULL);
INSERT INTO met_attributes VALUES (109, 30, 'CeleKsztalcenia', NULL, true, NULL);
INSERT INTO met_attributes VALUES (110, 31, 'Przeznaczenie', NULL, true, NULL);
INSERT INTO met_attributes VALUES (111, 32, 'Pochodzenie', NULL, true, NULL);
INSERT INTO met_attributes VALUES (113, 34, 'PodstawaProgramowa', NULL, false, NULL);
INSERT INTO met_attributes VALUES (114, 35, 'SystemOperacyjny', NULL, false, NULL);
INSERT INTO met_attributes VALUES (115, 36, 'Opis', NULL, false, NULL);
INSERT INTO met_attributes VALUES (117, 37, 'TrybEdytora', NULL, true, NULL);


--
-- TOC entry 2584 (class 0 OID 0)
-- Dependencies: 200
-- Name: met_attributes_att_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_attributes_att_id_seq', 150, true);


--
-- TOC entry 2501 (class 0 OID 116385)
-- Dependencies: 201
-- Data for Name: met_col_datas; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_col_datas VALUES (1, 2, 'Repozytorium e-podręczników', 'To jest główna kolekcja repozytorium e-podręczników.');
INSERT INTO met_col_datas VALUES (19, 2, 'WOMI', 'Specjalny projekt dla wieloformatowych obiektów multimedialnych i interaktywnych');
INSERT INTO met_col_datas VALUES (20, 2, 'Moduły treści', '');
INSERT INTO met_col_datas VALUES (21, 2, 'Samodzielne kolekcje', '');


--
-- TOC entry 2502 (class 0 OID 116391)
-- Dependencies: 202
-- Data for Name: met_col_images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2503 (class 0 OID 116394)
-- Dependencies: 203
-- Data for Name: met_col_order; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2504 (class 0 OID 116397)
-- Dependencies: 204
-- Data for Name: met_collection_parents; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_collection_parents VALUES (19, 1, 1);
INSERT INTO met_collection_parents VALUES (20, 1, 1);
INSERT INTO met_collection_parents VALUES (21, 1, 1);


--
-- TOC entry 2505 (class 0 OID 116400)
-- Dependencies: 205
-- Data for Name: met_collections; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_collections VALUES (1, '', 'dLibraDigitalLibrary', 0, NULL, false);
INSERT INTO met_collections VALUES (19, '', 'womi', 4, 1, false);
INSERT INTO met_collections VALUES (20, '', 'mt', 5, 1, false);
INSERT INTO met_collections VALUES (21, '', 'col', 6, 1, false);


--
-- TOC entry 2585 (class 0 OID 0)
-- Dependencies: 206
-- Name: met_collections_col_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_collections_col_id_seq', 25, true);


--
-- TOC entry 2586 (class 0 OID 0)
-- Dependencies: 207
-- Name: met_del_pub_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_del_pub_data_id_seq', 1, false);


--
-- TOC entry 2508 (class 0 OID 116410)
-- Dependencies: 208
-- Data for Name: met_del_pub_datas; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2509 (class 0 OID 116416)
-- Dependencies: 209
-- Data for Name: met_dir_act; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2510 (class 0 OID 116419)
-- Dependencies: 210
-- Data for Name: met_dir_atts; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2511 (class 0 OID 116422)
-- Dependencies: 211
-- Data for Name: met_directories; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_directories VALUES (1, 'Repozytorium e-podręczników', 'Katalog główny biblioteki cyfrowej.', NULL);
INSERT INTO met_directories VALUES (2057, 'Testy zewnętrzne', '', 1);
INSERT INTO met_directories VALUES (2415, 'załadowane z EZ', 'zasoby załadowane poprzez opcje "uploadu" w edytorze zasobów', 1);


--
-- TOC entry 2587 (class 0 OID 0)
-- Dependencies: 212
-- Name: met_directories_dir_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_directories_dir_id_seq', 2500, true);


--
-- TOC entry 2513 (class 0 OID 116430)
-- Dependencies: 213
-- Data for Name: met_directory_parents; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_directory_parents VALUES (2057, 1, 1);
INSERT INTO met_directory_parents VALUES (2415, 1, 1);


--
-- TOC entry 2514 (class 0 OID 116433)
-- Dependencies: 214
-- Data for Name: met_edi_atts; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2515 (class 0 OID 116436)
-- Dependencies: 215
-- Data for Name: met_edi_datas; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2516 (class 0 OID 116442)
-- Dependencies: 216
-- Data for Name: met_edi_images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2517 (class 0 OID 116445)
-- Dependencies: 217
-- Data for Name: met_edi_ver; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2518 (class 0 OID 116448)
-- Dependencies: 218
-- Data for Name: met_editions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2588 (class 0 OID 0)
-- Dependencies: 219
-- Name: met_editions_edi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_editions_edi_id_seq', 1, false);


--
-- TOC entry 2520 (class 0 OID 116456)
-- Dependencies: 220
-- Data for Name: met_event_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2521 (class 0 OID 116462)
-- Dependencies: 221
-- Data for Name: met_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2589 (class 0 OID 0)
-- Dependencies: 222
-- Name: met_events_eve_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_events_eve_id_seq', 4, true);


--
-- TOC entry 2523 (class 0 OID 116467)
-- Dependencies: 223
-- Data for Name: met_files; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2590 (class 0 OID 0)
-- Dependencies: 224
-- Name: met_files_fil_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_files_fil_id_seq', 1, false);


--
-- TOC entry 2525 (class 0 OID 116475)
-- Dependencies: 225
-- Data for Name: met_languages; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO met_languages VALUES (2, 'pl', 1);
INSERT INTO met_languages VALUES (3, 'pl', 16);
INSERT INTO met_languages VALUES (4, 'universal', 32);


--
-- TOC entry 2591 (class 0 OID 0)
-- Dependencies: 226
-- Name: met_languages_lan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_languages_lan_id_seq', 5, true);


--
-- TOC entry 2527 (class 0 OID 116480)
-- Dependencies: 227
-- Data for Name: met_pub_atts; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2528 (class 0 OID 116483)
-- Dependencies: 228
-- Data for Name: met_pub_col; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2529 (class 0 OID 116486)
-- Dependencies: 229
-- Data for Name: met_pub_datas; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2530 (class 0 OID 116492)
-- Dependencies: 230
-- Data for Name: met_pub_images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2531 (class 0 OID 116495)
-- Dependencies: 231
-- Data for Name: met_publication_parents; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2532 (class 0 OID 116498)
-- Dependencies: 232
-- Data for Name: met_publications; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2592 (class 0 OID 0)
-- Dependencies: 233
-- Name: met_publications_pub_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_publications_pub_id_seq', 1, false);


--
-- TOC entry 2534 (class 0 OID 116506)
-- Dependencies: 234
-- Data for Name: met_statistics; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2593 (class 0 OID 0)
-- Dependencies: 235
-- Name: met_statistics_sta_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_statistics_sta_id_seq', 1, false);


--
-- TOC entry 2536 (class 0 OID 116511)
-- Dependencies: 236
-- Data for Name: met_tags; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2594 (class 0 OID 0)
-- Dependencies: 237
-- Name: met_tags_tag_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_tags_tag_id_seq', 1, false);


--
-- TOC entry 2538 (class 0 OID 116519)
-- Dependencies: 238
-- Data for Name: met_versions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2595 (class 0 OID 0)
-- Dependencies: 239
-- Name: met_versions_ver_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('met_versions_ver_id_seq', 1, false);


--
-- TOC entry 2540 (class 0 OID 116524)
-- Dependencies: 240
-- Data for Name: ms_message_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2541 (class 0 OID 116530)
-- Dependencies: 241
-- Data for Name: ms_messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2596 (class 0 OID 0)
-- Dependencies: 242
-- Name: ms_messages_msg_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('ms_messages_msg_id_seq', 1, false);


--
-- TOC entry 2543 (class 0 OID 116535)
-- Dependencies: 243
-- Data for Name: ms_receivers; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2544 (class 0 OID 116538)
-- Dependencies: 244
-- Data for Name: pp_user_profiles; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2597 (class 0 OID 0)
-- Dependencies: 245
-- Name: pp_user_profiles_up_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('pp_user_profiles_up_id_seq', 1, false);


--
-- TOC entry 2546 (class 0 OID 116546)
-- Dependencies: 246
-- Data for Name: sys_event_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2547 (class 0 OID 116552)
-- Dependencies: 247
-- Data for Name: sys_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2598 (class 0 OID 0)
-- Dependencies: 248
-- Name: sys_events_eve_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sys_events_eve_id_seq', 24, true);


--
-- TOC entry 2549 (class 0 OID 116557)
-- Dependencies: 249
-- Data for Name: sys_services; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO sys_services VALUES (15, 'ep', 'Epodreczniki', '3.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (11, 'mx', 'dLibra JMX Management Service', '1.0.0', 0, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (14, 'ws', 'Web server', '1.0.0', 0, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (8, 'is', 'Index server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (2, 'ss', 'System services', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (4, 'ui', 'User interface', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (3, 'us', 'User server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (9, 'em', 'Event server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (6, 'cs', 'Content server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (5, 'ms', 'Metadata server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (12, 'pp', 'Profile Provider', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (13, 'me', 'Message server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (10, 'ps', 'Presentation servlet', '1.0.0', 0, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (7, 'se', 'Search server', '1.0.0', 1, 'aaa', 'rt.epo.pl', 10051);
INSERT INTO sys_services VALUES (1, 'ss', 'System services', '1.0.0', 1, 'aaa', '127.0.0.1', 10051);


--
-- TOC entry 2599 (class 0 OID 0)
-- Dependencies: 250
-- Name: sys_services_ser_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sys_services_ser_id_seq', 200, true);


--
-- TOC entry 2551 (class 0 OID 116565)
-- Dependencies: 251
-- Data for Name: us_actors; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO us_actors VALUES (2, 'Użytkownik publiczny', 'public', 0, NULL, 'public', 'public@digitallibrary.com', 'Digital Library Institution', 16, 'Domyślny użytkownik dla gości stron WWW', NULL);
INSERT INTO us_actors VALUES (1, 'Administrator', 'admin', 0, NULL, 'admin', 'admin@digitallibrary.com', 'Digital Library Institution', 4, 'Administrator biblioteki', NULL);
INSERT INTO us_actors VALUES (9, 'PCSS', 'PCSS', 0, NULL, 'PCSS', 'dlibra@man.poznan.pl', 'PCSS', 4, NULL, NULL);
INSERT INTO us_actors VALUES (11, 'Autor', 'autor', 0, NULL, 'autor', 'autor@epodreczniki.pl', 'epodreczniki.pl', 4, NULL, NULL);
INSERT INTO us_actors VALUES (8, 'Autorzy e-podręczników', 'Autorzy e-podręczników', NULL, NULL, '$pass$', '$email$Autorzy e-podręczników', '$Inst$', 1, 'Grupa ogólna dla wszystkich osób dodających obiekty informacyjne', NULL);
INSERT INTO us_actors VALUES (3, 'Użytkownicy publiczni', 'Użytkownicy publiczni', 0, NULL, 'publicGroup', '$email$Użytkownicy publiczni', 'Digital Library Institution', 1, 'Grupa użytkowników publicznych', NULL);


--
-- TOC entry 2600 (class 0 OID 0)
-- Dependencies: 252
-- Name: us_actors_act_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('us_actors_act_id_seq', 11, true);


--
-- TOC entry 2553 (class 0 OID 116573)
-- Dependencies: 253
-- Data for Name: us_admin_rights; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO us_admin_rights VALUES (1, 'am');
INSERT INTO us_admin_rights VALUES (1, 'gm');
INSERT INTO us_admin_rights VALUES (1, 'tm');
INSERT INTO us_admin_rights VALUES (1, 'at');
INSERT INTO us_admin_rights VALUES (1, 'cm');
INSERT INTO us_admin_rights VALUES (1, 'wm');
INSERT INTO us_admin_rights VALUES (1, 'av');
INSERT INTO us_admin_rights VALUES (1, 'lm');
INSERT INTO us_admin_rights VALUES (1, 'mo');
INSERT INTO us_admin_rights VALUES (1, 'cv');
INSERT INTO us_admin_rights VALUES (8, 'av');
INSERT INTO us_admin_rights VALUES (8, 'cm');


--
-- TOC entry 2554 (class 0 OID 116576)
-- Dependencies: 254
-- Data for Name: us_collection_rights; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO us_collection_rights VALUES (8, 1, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 4, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 8, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 9, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 13, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 10, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 11, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 12, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 14, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 15, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 16, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 5, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 6, 'ccm');
INSERT INTO us_collection_rights VALUES (8, 7, 'ccm');
INSERT INTO us_collection_rights VALUES (1, 19, 'ccm');
INSERT INTO us_collection_rights VALUES (1, 20, 'ccm');
INSERT INTO us_collection_rights VALUES (1, 21, 'ccm');


--
-- TOC entry 2555 (class 0 OID 116579)
-- Dependencies: 255
-- Data for Name: us_directory_rights; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO us_directory_rights VALUES (3, 1, 'dl');
INSERT INTO us_directory_rights VALUES (9, 1, 'de');
INSERT INTO us_directory_rights VALUES (9, 1, 'pc');
INSERT INTO us_directory_rights VALUES (8, 2057, 'de');
INSERT INTO us_directory_rights VALUES (8, 2057, 'pm');
INSERT INTO us_directory_rights VALUES (8, 1, 'da');
INSERT INTO us_directory_rights VALUES (8, 2415, 'da');
INSERT INTO us_directory_rights VALUES (8, 2415, 'dl');


--
-- TOC entry 2556 (class 0 OID 116582)
-- Dependencies: 256
-- Data for Name: us_domains; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2601 (class 0 OID 0)
-- Dependencies: 257
-- Name: us_domains_dom_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('us_domains_dom_id_seq', 1, false);


--
-- TOC entry 2558 (class 0 OID 116590)
-- Dependencies: 258
-- Data for Name: us_event_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2559 (class 0 OID 116596)
-- Dependencies: 259
-- Data for Name: us_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2602 (class 0 OID 0)
-- Dependencies: 260
-- Name: us_events_eve_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('us_events_eve_id_seq', 9, true);


--
-- TOC entry 2561 (class 0 OID 116601)
-- Dependencies: 261
-- Data for Name: us_gro_use; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO us_gro_use VALUES (8, 11, false);
INSERT INTO us_gro_use VALUES (3, 2, false);
INSERT INTO us_gro_use VALUES (3, 9, false);
INSERT INTO us_gro_use VALUES (8, 9, false);


--
-- TOC entry 2603 (class 0 OID 0)
-- Dependencies: 262
-- Name: us_ldap_gr_details_ldg_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('us_ldap_gr_details_ldg_id_seq', 1, false);


--
-- TOC entry 2563 (class 0 OID 116606)
-- Dependencies: 263
-- Data for Name: us_ldap_group_details; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2564 (class 0 OID 116612)
-- Dependencies: 264
-- Data for Name: us_publication_rights; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2166 (class 2606 OID 116738)
-- Name: cs_col_img_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_col_img_paths
    ADD CONSTRAINT cs_col_img_paths_pkey PRIMARY KEY (ip_id);


--
-- TOC entry 2168 (class 2606 OID 116740)
-- Name: cs_col_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_col_paths
    ADD CONSTRAINT cs_col_paths_pkey PRIMARY KEY (cp_id);


--
-- TOC entry 2170 (class 2606 OID 116742)
-- Name: cs_dir_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_dir_paths
    ADD CONSTRAINT cs_dir_paths_pkey PRIMARY KEY (dp_id);


--
-- TOC entry 2172 (class 2606 OID 116744)
-- Name: cs_event_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_event_details
    ADD CONSTRAINT cs_event_details_pkey PRIMARY KEY (evd_eve_id, evd_key);


--
-- TOC entry 2174 (class 2606 OID 116746)
-- Name: cs_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_events
    ADD CONSTRAINT cs_events_pkey PRIMARY KEY (eve_id);


--
-- TOC entry 2176 (class 2606 OID 116748)
-- Name: cs_fil_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_fil_paths
    ADD CONSTRAINT cs_fil_paths_pkey PRIMARY KEY (fp_id);


--
-- TOC entry 2178 (class 2606 OID 116750)
-- Name: cs_img_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_img_paths
    ADD CONSTRAINT cs_img_paths_pkey PRIMARY KEY (ip_id);


--
-- TOC entry 2180 (class 2606 OID 116752)
-- Name: cs_pub_img_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_pub_img_paths
    ADD CONSTRAINT cs_pub_img_paths_pkey PRIMARY KEY (ip_id);


--
-- TOC entry 2182 (class 2606 OID 116754)
-- Name: cs_pub_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_pub_paths
    ADD CONSTRAINT cs_pub_paths_pkey PRIMARY KEY (pp_id);


--
-- TOC entry 2184 (class 2606 OID 116756)
-- Name: cs_ver_content_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_ver_content_types
    ADD CONSTRAINT cs_ver_content_types_pkey PRIMARY KEY (vc_ver_id);


--
-- TOC entry 2186 (class 2606 OID 116758)
-- Name: cs_ver_digests_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_ver_digests
    ADD CONSTRAINT cs_ver_digests_pkey PRIMARY KEY (vd_id);


--
-- TOC entry 2188 (class 2606 OID 116760)
-- Name: cs_ver_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cs_ver_paths
    ADD CONSTRAINT cs_ver_paths_pkey PRIMARY KEY (vp_id);


--
-- TOC entry 2190 (class 2606 OID 116762)
-- Name: eve_event_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY eve_event_details
    ADD CONSTRAINT eve_event_details_pkey PRIMARY KEY (evd_eve_id, evd_key);


--
-- TOC entry 2192 (class 2606 OID 116764)
-- Name: eve_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY eve_events
    ADD CONSTRAINT eve_events_pkey PRIMARY KEY (eve_id);


--
-- TOC entry 2194 (class 2606 OID 116766)
-- Name: met_attribute_datas_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attribute_datas
    ADD CONSTRAINT met_attribute_datas_pkey PRIMARY KEY (ad_att_id, ad_lan_id);


--
-- TOC entry 2196 (class 2606 OID 116768)
-- Name: met_attribute_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attribute_parents
    ADD CONSTRAINT met_attribute_parents_pkey PRIMARY KEY (ap_att_id, ap_parent_id);


--
-- TOC entry 2198 (class 2606 OID 116770)
-- Name: met_attribute_values_av_ext_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attribute_values
    ADD CONSTRAINT met_attribute_values_av_ext_id_key UNIQUE (av_ext_id);


--
-- TOC entry 2200 (class 2606 OID 116772)
-- Name: met_attribute_values_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attribute_values
    ADD CONSTRAINT met_attribute_values_pkey PRIMARY KEY (av_id);


--
-- TOC entry 2202 (class 2606 OID 116774)
-- Name: met_attributes_att_rdfname_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attributes
    ADD CONSTRAINT met_attributes_att_rdfname_key UNIQUE (att_rdfname);


--
-- TOC entry 2204 (class 2606 OID 116776)
-- Name: met_attributes_att_role_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attributes
    ADD CONSTRAINT met_attributes_att_role_id_key UNIQUE (att_role_id);


--
-- TOC entry 2206 (class 2606 OID 116778)
-- Name: met_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_attributes
    ADD CONSTRAINT met_attributes_pkey PRIMARY KEY (att_id);


--
-- TOC entry 2208 (class 2606 OID 116780)
-- Name: met_col_datas_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_col_datas
    ADD CONSTRAINT met_col_datas_pkey PRIMARY KEY (cd_col_id, cd_lan_id);


--
-- TOC entry 2210 (class 2606 OID 116782)
-- Name: met_col_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_col_images
    ADD CONSTRAINT met_col_images_pkey PRIMARY KEY (ci_col_id);


--
-- TOC entry 2212 (class 2606 OID 116784)
-- Name: met_col_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_col_order
    ADD CONSTRAINT met_col_order_pkey PRIMARY KEY (co_col_id, co_pub_id);


--
-- TOC entry 2214 (class 2606 OID 116786)
-- Name: met_collection_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_collection_parents
    ADD CONSTRAINT met_collection_parents_pkey PRIMARY KEY (cp_col_id, cp_parent_id);


--
-- TOC entry 2216 (class 2606 OID 116788)
-- Name: met_collections_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_collections
    ADD CONSTRAINT met_collections_pkey PRIMARY KEY (col_id);


--
-- TOC entry 2218 (class 2606 OID 116790)
-- Name: met_del_pub_datas_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_del_pub_datas
    ADD CONSTRAINT met_del_pub_datas_pkey PRIMARY KEY (dpd_id);


--
-- TOC entry 2220 (class 2606 OID 116792)
-- Name: met_dir_act_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_dir_act
    ADD CONSTRAINT met_dir_act_pkey PRIMARY KEY (da_dir_id, da_act_id);


--
-- TOC entry 2222 (class 2606 OID 116794)
-- Name: met_dir_atts_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_dir_atts
    ADD CONSTRAINT met_dir_atts_pkey PRIMARY KEY (da_dir_id, da_av_id);


--
-- TOC entry 2224 (class 2606 OID 116796)
-- Name: met_directories_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_directories
    ADD CONSTRAINT met_directories_pkey PRIMARY KEY (dir_id);


--
-- TOC entry 2226 (class 2606 OID 116798)
-- Name: met_directory_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_directory_parents
    ADD CONSTRAINT met_directory_parents_pkey PRIMARY KEY (dp_dir_id, dp_parent_id);


--
-- TOC entry 2228 (class 2606 OID 116800)
-- Name: met_edi_atts_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_edi_atts
    ADD CONSTRAINT met_edi_atts_pkey PRIMARY KEY (ea_edi_id, ea_av_id);


--
-- TOC entry 2230 (class 2606 OID 116802)
-- Name: met_edi_datas_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_edi_datas
    ADD CONSTRAINT met_edi_datas_pkey PRIMARY KEY (ed_edi_id, ed_lan_id);


--
-- TOC entry 2232 (class 2606 OID 116804)
-- Name: met_edi_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_edi_images
    ADD CONSTRAINT met_edi_images_pkey PRIMARY KEY (ei_edi_id);


--
-- TOC entry 2234 (class 2606 OID 116806)
-- Name: met_edi_ver_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_edi_ver
    ADD CONSTRAINT met_edi_ver_pkey PRIMARY KEY (ev_ver_id, ev_edi_id);


--
-- TOC entry 2236 (class 2606 OID 116808)
-- Name: met_editions_edi_external_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_editions
    ADD CONSTRAINT met_editions_edi_external_id_key UNIQUE (edi_external_id);


--
-- TOC entry 2238 (class 2606 OID 116810)
-- Name: met_editions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_editions
    ADD CONSTRAINT met_editions_pkey PRIMARY KEY (edi_id);


--
-- TOC entry 2240 (class 2606 OID 116812)
-- Name: met_event_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_event_details
    ADD CONSTRAINT met_event_details_pkey PRIMARY KEY (evd_eve_id, evd_key);


--
-- TOC entry 2242 (class 2606 OID 116814)
-- Name: met_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_events
    ADD CONSTRAINT met_events_pkey PRIMARY KEY (eve_id);


--
-- TOC entry 2245 (class 2606 OID 116816)
-- Name: met_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_files
    ADD CONSTRAINT met_files_pkey PRIMARY KEY (fil_id);


--
-- TOC entry 2247 (class 2606 OID 116818)
-- Name: met_languages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_languages
    ADD CONSTRAINT met_languages_pkey PRIMARY KEY (lan_id);


--
-- TOC entry 2249 (class 2606 OID 116820)
-- Name: met_pub_atts_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_pub_atts
    ADD CONSTRAINT met_pub_atts_pkey PRIMARY KEY (pa_pub_id, pa_av_id);


--
-- TOC entry 2251 (class 2606 OID 116822)
-- Name: met_pub_col_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_pub_col
    ADD CONSTRAINT met_pub_col_pkey PRIMARY KEY (pc_pub_id, pc_col_id);


--
-- TOC entry 2253 (class 2606 OID 116824)
-- Name: met_pub_datas_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_pub_datas
    ADD CONSTRAINT met_pub_datas_pkey PRIMARY KEY (pd_pub_id, pd_lan_id);


--
-- TOC entry 2255 (class 2606 OID 116826)
-- Name: met_pub_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_pub_images
    ADD CONSTRAINT met_pub_images_pkey PRIMARY KEY (pi_pub_id);


--
-- TOC entry 2257 (class 2606 OID 116828)
-- Name: met_publication_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_publication_parents
    ADD CONSTRAINT met_publication_parents_pkey PRIMARY KEY (pp_pub_id, pp_parent_id);


--
-- TOC entry 2259 (class 2606 OID 116830)
-- Name: met_publications_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_publications
    ADD CONSTRAINT met_publications_pkey PRIMARY KEY (pub_id);


--
-- TOC entry 2261 (class 2606 OID 116832)
-- Name: met_statistics_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_statistics
    ADD CONSTRAINT met_statistics_pkey PRIMARY KEY (sta_id);


--
-- TOC entry 2263 (class 2606 OID 116834)
-- Name: met_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_tags
    ADD CONSTRAINT met_tags_pkey PRIMARY KEY (tag_id);


--
-- TOC entry 2265 (class 2606 OID 116836)
-- Name: met_versions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY met_versions
    ADD CONSTRAINT met_versions_pkey PRIMARY KEY (ver_id);


--
-- TOC entry 2267 (class 2606 OID 116838)
-- Name: ms_message_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ms_message_details
    ADD CONSTRAINT ms_message_details_pkey PRIMARY KEY (msd_msg_id, msd_key);


--
-- TOC entry 2269 (class 2606 OID 116840)
-- Name: ms_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ms_messages
    ADD CONSTRAINT ms_messages_pkey PRIMARY KEY (msg_id);


--
-- TOC entry 2271 (class 2606 OID 116842)
-- Name: ms_receivers_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ms_receivers
    ADD CONSTRAINT ms_receivers_pkey PRIMARY KEY (msr_msg_id, msr_act_id);


--
-- TOC entry 2273 (class 2606 OID 116844)
-- Name: pp_user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY pp_user_profiles
    ADD CONSTRAINT pp_user_profiles_pkey PRIMARY KEY (up_id);


--
-- TOC entry 2275 (class 2606 OID 116846)
-- Name: sys_event_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sys_event_details
    ADD CONSTRAINT sys_event_details_pkey PRIMARY KEY (evd_eve_id, evd_key);


--
-- TOC entry 2277 (class 2606 OID 116848)
-- Name: sys_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sys_events
    ADD CONSTRAINT sys_events_pkey PRIMARY KEY (eve_id);


--
-- TOC entry 2279 (class 2606 OID 116850)
-- Name: sys_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sys_services
    ADD CONSTRAINT sys_services_pkey PRIMARY KEY (ser_id);


--
-- TOC entry 2281 (class 2606 OID 116852)
-- Name: us_actors_act_email_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_actors
    ADD CONSTRAINT us_actors_act_email_key UNIQUE (act_email);


--
-- TOC entry 2283 (class 2606 OID 116854)
-- Name: us_actors_act_login_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_actors
    ADD CONSTRAINT us_actors_act_login_key UNIQUE (act_login);


--
-- TOC entry 2285 (class 2606 OID 116856)
-- Name: us_actors_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_actors
    ADD CONSTRAINT us_actors_pkey PRIMARY KEY (act_id);


--
-- TOC entry 2287 (class 2606 OID 116858)
-- Name: us_admin_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_admin_rights
    ADD CONSTRAINT us_admin_rights_pkey PRIMARY KEY (ari_act_id, ari_right_id);


--
-- TOC entry 2289 (class 2606 OID 116860)
-- Name: us_collection_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_collection_rights
    ADD CONSTRAINT us_collection_rights_pkey PRIMARY KEY (cri_act_id, cri_collection_id, cri_right_id);


--
-- TOC entry 2291 (class 2606 OID 116862)
-- Name: us_directory_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_directory_rights
    ADD CONSTRAINT us_directory_rights_pkey PRIMARY KEY (dri_act_id, dri_directory_id, dri_right_id);


--
-- TOC entry 2293 (class 2606 OID 116864)
-- Name: us_domains_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_domains
    ADD CONSTRAINT us_domains_pkey PRIMARY KEY (dom_id);


--
-- TOC entry 2295 (class 2606 OID 116866)
-- Name: us_event_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_event_details
    ADD CONSTRAINT us_event_details_pkey PRIMARY KEY (evd_eve_id, evd_key);


--
-- TOC entry 2297 (class 2606 OID 116868)
-- Name: us_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_events
    ADD CONSTRAINT us_events_pkey PRIMARY KEY (eve_id);


--
-- TOC entry 2299 (class 2606 OID 116870)
-- Name: us_gro_use_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_gro_use
    ADD CONSTRAINT us_gro_use_pkey PRIMARY KEY (gru_act_id, gru_gro_id);


--
-- TOC entry 2301 (class 2606 OID 116872)
-- Name: us_ldap_group_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_ldap_group_details
    ADD CONSTRAINT us_ldap_group_details_pkey PRIMARY KEY (lgd_id);


--
-- TOC entry 2303 (class 2606 OID 116874)
-- Name: us_publication_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY us_publication_rights
    ADD CONSTRAINT us_publication_rights_pkey PRIMARY KEY (pri_act_id, pri_publication_id, pri_right_id);


--
-- TOC entry 2243 (class 1259 OID 116875)
-- Name: files_paths_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX files_paths_index ON met_files USING btree (fil_path);


--
-- TOC entry 2308 (class 2606 OID 116876)
-- Name: fk10685a63ab914426; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attribute_parents
    ADD CONSTRAINT fk10685a63ab914426 FOREIGN KEY (ap_att_id) REFERENCES met_attributes(att_id);


--
-- TOC entry 2347 (class 2606 OID 116881)
-- Name: fk158170bc2bdca4fd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_tags
    ADD CONSTRAINT fk158170bc2bdca4fd FOREIGN KEY (tag_lan_id) REFERENCES met_languages(lan_id);


--
-- TOC entry 2348 (class 2606 OID 116886)
-- Name: fk158170bcb41f1154; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_tags
    ADD CONSTRAINT fk158170bcb41f1154 FOREIGN KEY (tag_av_id) REFERENCES met_attribute_values(av_id);


--
-- TOC entry 2349 (class 2606 OID 116891)
-- Name: fk158170bcfd4e3414; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_tags
    ADD CONSTRAINT fk158170bcfd4e3414 FOREIGN KEY (tag_edi_id) REFERENCES met_editions(edi_id);


--
-- TOC entry 2336 (class 2606 OID 116896)
-- Name: fk1c5a51375de9026b; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_atts
    ADD CONSTRAINT fk1c5a51375de9026b FOREIGN KEY (pa_av_id) REFERENCES met_attribute_values(av_id);


--
-- TOC entry 2337 (class 2606 OID 116901)
-- Name: fk1c5a5137fc3b998; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_atts
    ADD CONSTRAINT fk1c5a5137fc3b998 FOREIGN KEY (pa_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2324 (class 2606 OID 116906)
-- Name: fk251669a846f3c60c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_directories
    ADD CONSTRAINT fk251669a846f3c60c FOREIGN KEY (dir_parent_id) REFERENCES met_directories(dir_id);


--
-- TOC entry 2304 (class 2606 OID 116911)
-- Name: fk2649dbce7b9a6bf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cs_event_details
    ADD CONSTRAINT fk2649dbce7b9a6bf FOREIGN KEY (evd_eve_id) REFERENCES cs_events(eve_id);


--
-- TOC entry 2358 (class 2606 OID 116916)
-- Name: fk28b757cbfe9d151; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_event_details
    ADD CONSTRAINT fk28b757cbfe9d151 FOREIGN KEY (evd_eve_id) REFERENCES us_events(eve_id);


--
-- TOC entry 2333 (class 2606 OID 116921)
-- Name: fk2dbded98bc918dbf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_editions
    ADD CONSTRAINT fk2dbded98bc918dbf FOREIGN KEY (edi_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2326 (class 2606 OID 116926)
-- Name: fk2e5560aa2dd1a0d2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_atts
    ADD CONSTRAINT fk2e5560aa2dd1a0d2 FOREIGN KEY (ea_edi_id) REFERENCES met_editions(edi_id);


--
-- TOC entry 2327 (class 2606 OID 116931)
-- Name: fk2e5560aae73c14d6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_atts
    ADD CONSTRAINT fk2e5560aae73c14d6 FOREIGN KEY (ea_av_id) REFERENCES met_attribute_values(av_id);


--
-- TOC entry 2356 (class 2606 OID 116936)
-- Name: fk2e7e2b8a79db6c66; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_directory_rights
    ADD CONSTRAINT fk2e7e2b8a79db6c66 FOREIGN KEY (dri_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2312 (class 2606 OID 116941)
-- Name: fk354693ba33fdbf4f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attributes
    ADD CONSTRAINT fk354693ba33fdbf4f FOREIGN KEY (att_parent_id) REFERENCES met_attributes(att_id);


--
-- TOC entry 2325 (class 2606 OID 116946)
-- Name: fk47fc6d3432b9a38e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_directory_parents
    ADD CONSTRAINT fk47fc6d3432b9a38e FOREIGN KEY (dp_dir_id) REFERENCES met_directories(dir_id);


--
-- TOC entry 2351 (class 2606 OID 116951)
-- Name: fk50052d3111b3757d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ms_message_details
    ADD CONSTRAINT fk50052d3111b3757d FOREIGN KEY (msd_msg_id) REFERENCES ms_messages(msg_id);


--
-- TOC entry 2357 (class 2606 OID 116956)
-- Name: fk5374836e5c92d2df; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_domains
    ADD CONSTRAINT fk5374836e5c92d2df FOREIGN KEY (dom_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2306 (class 2606 OID 116961)
-- Name: fk5499e5e342f1dc54; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attribute_datas
    ADD CONSTRAINT fk5499e5e342f1dc54 FOREIGN KEY (ad_lan_id) REFERENCES met_languages(lan_id);


--
-- TOC entry 2307 (class 2606 OID 116966)
-- Name: fk5499e5e3cd0329b2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attribute_datas
    ADD CONSTRAINT fk5499e5e3cd0329b2 FOREIGN KEY (ad_att_id) REFERENCES met_attributes(att_id);


--
-- TOC entry 2343 (class 2606 OID 116971)
-- Name: fk55f52ab325f55aa9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_publication_parents
    ADD CONSTRAINT fk55f52ab325f55aa9 FOREIGN KEY (pp_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2350 (class 2606 OID 116976)
-- Name: fk57e5a9be12fb1a08; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_versions
    ADD CONSTRAINT fk57e5a9be12fb1a08 FOREIGN KEY (ver_fil_id) REFERENCES met_files(fil_id);


--
-- TOC entry 2309 (class 2606 OID 116981)
-- Name: fk5d56b8681ad85160; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attribute_values
    ADD CONSTRAINT fk5d56b8681ad85160 FOREIGN KEY (av_att_id) REFERENCES met_attributes(att_id);


--
-- TOC entry 2310 (class 2606 OID 116986)
-- Name: fk5d56b86890c70402; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attribute_values
    ADD CONSTRAINT fk5d56b86890c70402 FOREIGN KEY (av_lan_id) REFERENCES met_languages(lan_id);


--
-- TOC entry 2311 (class 2606 OID 116991)
-- Name: fk5d56b868966ed544; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_attribute_values
    ADD CONSTRAINT fk5d56b868966ed544 FOREIGN KEY (av_bas_id) REFERENCES met_attribute_values(av_id);


--
-- TOC entry 2321 (class 2606 OID 116996)
-- Name: fk62028a1d1c88027d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_dir_act
    ADD CONSTRAINT fk62028a1d1c88027d FOREIGN KEY (da_dir_id) REFERENCES met_directories(dir_id);


--
-- TOC entry 2355 (class 2606 OID 117001)
-- Name: fk669913378591fb47; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_collection_rights
    ADD CONSTRAINT fk669913378591fb47 FOREIGN KEY (cri_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2340 (class 2606 OID 117006)
-- Name: fk6f11774447674035; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_datas
    ADD CONSTRAINT fk6f11774447674035 FOREIGN KEY (pd_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2341 (class 2606 OID 117011)
-- Name: fk6f117744f2f45d63; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_datas
    ADD CONSTRAINT fk6f117744f2f45d63 FOREIGN KEY (pd_lan_id) REFERENCES met_languages(lan_id);


--
-- TOC entry 2344 (class 2606 OID 117016)
-- Name: fk72407d6a1f76956e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_publications
    ADD CONSTRAINT fk72407d6a1f76956e FOREIGN KEY (pub_fil_id) REFERENCES met_files(fil_id);


--
-- TOC entry 2345 (class 2606 OID 117021)
-- Name: fk72407d6a28acf2fd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_publications
    ADD CONSTRAINT fk72407d6a28acf2fd FOREIGN KEY (pub_parent_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2346 (class 2606 OID 117026)
-- Name: fk72407d6a8320a87c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_publications
    ADD CONSTRAINT fk72407d6a8320a87c FOREIGN KEY (pub_parent_id) REFERENCES met_directories(dir_id);


--
-- TOC entry 2342 (class 2606 OID 117031)
-- Name: fk7c46387d4ecd2090; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_images
    ADD CONSTRAINT fk7c46387d4ecd2090 FOREIGN KEY (pi_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2334 (class 2606 OID 117036)
-- Name: fk8660c81a8add966d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_event_details
    ADD CONSTRAINT fk8660c81a8add966d FOREIGN KEY (evd_eve_id) REFERENCES met_events(eve_id);


--
-- TOC entry 2313 (class 2606 OID 117041)
-- Name: fk894f17876b7aba56; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_col_datas
    ADD CONSTRAINT fk894f17876b7aba56 FOREIGN KEY (cd_lan_id) REFERENCES met_languages(lan_id);


--
-- TOC entry 2314 (class 2606 OID 117046)
-- Name: fk894f1787afa81c95; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_col_datas
    ADD CONSTRAINT fk894f1787afa81c95 FOREIGN KEY (cd_col_id) REFERENCES met_collections(col_id);


--
-- TOC entry 2316 (class 2606 OID 117051)
-- Name: fk89f198cc26550a2a; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_col_order
    ADD CONSTRAINT fk89f198cc26550a2a FOREIGN KEY (co_col_id) REFERENCES met_collections(col_id);


--
-- TOC entry 2317 (class 2606 OID 117056)
-- Name: fk89f198cc369a8abd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_col_order
    ADD CONSTRAINT fk89f198cc369a8abd FOREIGN KEY (co_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2318 (class 2606 OID 117061)
-- Name: fk89fb47ab8e363709; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_collection_parents
    ADD CONSTRAINT fk89fb47ab8e363709 FOREIGN KEY (cp_col_id) REFERENCES met_collections(col_id);


--
-- TOC entry 2362 (class 2606 OID 117066)
-- Name: fk8b15e2abed4cb9da; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_publication_rights
    ADD CONSTRAINT fk8b15e2abed4cb9da FOREIGN KEY (pri_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2331 (class 2606 OID 117071)
-- Name: fk8de205cb55dc079e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_ver
    ADD CONSTRAINT fk8de205cb55dc079e FOREIGN KEY (ev_ver_id) REFERENCES met_versions(ver_id);


--
-- TOC entry 2332 (class 2606 OID 117076)
-- Name: fk8de205cbb34a4f1d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_ver
    ADD CONSTRAINT fk8de205cbb34a4f1d FOREIGN KEY (ev_edi_id) REFERENCES met_editions(edi_id);


--
-- TOC entry 2335 (class 2606 OID 117081)
-- Name: fk99eb1214cdd4b080; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_files
    ADD CONSTRAINT fk99eb1214cdd4b080 FOREIGN KEY (fil_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2328 (class 2606 OID 117086)
-- Name: fk9c7856316575276f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_datas
    ADD CONSTRAINT fk9c7856316575276f FOREIGN KEY (ed_edi_id) REFERENCES met_editions(edi_id);


--
-- TOC entry 2329 (class 2606 OID 117091)
-- Name: fk9c78563194039858; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_datas
    ADD CONSTRAINT fk9c78563194039858 FOREIGN KEY (ed_lan_id) REFERENCES met_languages(lan_id);


--
-- TOC entry 2361 (class 2606 OID 117096)
-- Name: fka49d14ebb5d04ff8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_ldap_group_details
    ADD CONSTRAINT fka49d14ebb5d04ff8 FOREIGN KEY (lgd_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2315 (class 2606 OID 117101)
-- Name: fka9bca09ab70dfcf0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_col_images
    ADD CONSTRAINT fka9bca09ab70dfcf0 FOREIGN KEY (ci_col_id) REFERENCES met_collections(col_id);


--
-- TOC entry 2354 (class 2606 OID 117106)
-- Name: fkadf654e89cff1909; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_admin_rights
    ADD CONSTRAINT fkadf654e89cff1909 FOREIGN KEY (ari_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2353 (class 2606 OID 117111)
-- Name: fkafe5e74b1a6c6c1c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sys_event_details
    ADD CONSTRAINT fkafe5e74b1a6c6c1c FOREIGN KEY (evd_eve_id) REFERENCES sys_events(eve_id);


--
-- TOC entry 2305 (class 2606 OID 117116)
-- Name: fkb7e65a12b5856d75; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eve_event_details
    ADD CONSTRAINT fkb7e65a12b5856d75 FOREIGN KEY (evd_eve_id) REFERENCES eve_events(eve_id);


--
-- TOC entry 2320 (class 2606 OID 117121)
-- Name: fkc205e970b4177051; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_del_pub_datas
    ADD CONSTRAINT fkc205e970b4177051 FOREIGN KEY (dpd_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2352 (class 2606 OID 117126)
-- Name: fkd9f10ebc003e9af; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ms_receivers
    ADD CONSTRAINT fkd9f10ebc003e9af FOREIGN KEY (msr_msg_id) REFERENCES ms_messages(msg_id);


--
-- TOC entry 2322 (class 2606 OID 117131)
-- Name: fkde4ef9a71c88027d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_dir_atts
    ADD CONSTRAINT fkde4ef9a71c88027d FOREIGN KEY (da_dir_id) REFERENCES met_directories(dir_id);


--
-- TOC entry 2323 (class 2606 OID 117136)
-- Name: fkde4ef9a77f5ae7f7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_dir_atts
    ADD CONSTRAINT fkde4ef9a77f5ae7f7 FOREIGN KEY (da_av_id) REFERENCES met_attribute_values(av_id);


--
-- TOC entry 2338 (class 2606 OID 117141)
-- Name: fkf066097bcf4092c3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_col
    ADD CONSTRAINT fkf066097bcf4092c3 FOREIGN KEY (pc_col_id) REFERENCES met_collections(col_id);


--
-- TOC entry 2339 (class 2606 OID 117146)
-- Name: fkf066097bdf861356; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_pub_col
    ADD CONSTRAINT fkf066097bdf861356 FOREIGN KEY (pc_pub_id) REFERENCES met_publications(pub_id);


--
-- TOC entry 2319 (class 2606 OID 117151)
-- Name: fkf4848772fa62778e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_collections
    ADD CONSTRAINT fkf4848772fa62778e FOREIGN KEY (col_parent_id) REFERENCES met_collections(col_id);


--
-- TOC entry 2359 (class 2606 OID 117156)
-- Name: fkf76fbecb3545da37; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_gro_use
    ADD CONSTRAINT fkf76fbecb3545da37 FOREIGN KEY (gru_act_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2360 (class 2606 OID 117161)
-- Name: fkf76fbecb405408a5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY us_gro_use
    ADD CONSTRAINT fkf76fbecb405408a5 FOREIGN KEY (gru_gro_id) REFERENCES us_actors(act_id);


--
-- TOC entry 2330 (class 2606 OID 117166)
-- Name: fkfbbb37306cdb07ca; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY met_edi_images
    ADD CONSTRAINT fkfbbb37306cdb07ca FOREIGN KEY (ei_edi_id) REFERENCES met_editions(edi_id);


-- Completed on 2015-11-13 14:59:34

--
-- PostgreSQL database dump complete
--

