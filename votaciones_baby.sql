--
-- PostgreSQL database dump
--

-- Dumped from database version 14.13 (Ubuntu 14.13-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 17.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ciudadano; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ciudadano (
    id integer NOT NULL,
    documento character varying,
    nombre character varying,
    apellido character varying,
    mesa_id integer
);


ALTER TABLE public.ciudadano OWNER TO postgres;

--
-- Name: ciudadano_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ciudadano_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ciudadano_seq OWNER TO postgres;

--
-- Name: departamento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.departamento (
    id integer NOT NULL,
    nombre character varying
);


ALTER TABLE public.departamento OWNER TO postgres;

--
-- Name: departamento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.departamento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.departamento_seq OWNER TO postgres;

--
-- Name: mesa_votacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mesa_votacion (
    id integer NOT NULL,
    consecutive integer,
    puesto_id integer
);


ALTER TABLE public.mesa_votacion OWNER TO postgres;

--
-- Name: mesa_votacion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mesa_votacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mesa_votacion_seq OWNER TO postgres;

--
-- Name: municipio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.municipio (
    id integer NOT NULL,
    nombre character varying,
    departamento_id integer
);


ALTER TABLE public.municipio OWNER TO postgres;

--
-- Name: municipio_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.municipio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.municipio_seq OWNER TO postgres;

--
-- Name: puesto_votacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.puesto_votacion (
    id integer NOT NULL,
    nombre character varying,
    consecutive integer,
    direccion character varying,
    municipio_id integer
);


ALTER TABLE public.puesto_votacion OWNER TO postgres;

--
-- Name: puesto_votacion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.puesto_votacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.puesto_votacion_seq OWNER TO postgres;

--
-- Name: ciudadano ciudadano_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ciudadano
    ADD CONSTRAINT ciudadano_pkey PRIMARY KEY (id);


--
-- Name: departamento departamento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departamento
    ADD CONSTRAINT departamento_pkey PRIMARY KEY (id);


--
-- Name: mesa_votacion mesa_votacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mesa_votacion
    ADD CONSTRAINT mesa_votacion_pkey PRIMARY KEY (id);


--
-- Name: municipio municipio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.municipio
    ADD CONSTRAINT municipio_pkey PRIMARY KEY (id);


--
-- Name: puesto_votacion puesto_votacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.puesto_votacion
    ADD CONSTRAINT puesto_votacion_pkey PRIMARY KEY (id);


--
-- Name: idx_ciudadano_documento; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ciudadano_documento ON public.ciudadano USING btree (documento);


--
-- Name: idx_ciudadano_mesa_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ciudadano_mesa_id ON public.ciudadano USING btree (mesa_id);


--
-- Name: idx_mesa_votacion_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mesa_votacion_id ON public.mesa_votacion USING btree (id);


--
-- Name: idx_mesa_votacion_puesto_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mesa_votacion_puesto_id ON public.mesa_votacion USING btree (puesto_id);


--
-- Name: idx_municipio_departamento_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_municipio_departamento_id ON public.municipio USING btree (departamento_id);


--
-- Name: idx_puesto_votacion_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_puesto_votacion_id ON public.puesto_votacion USING btree (id);


--
-- Name: idx_puesto_votacion_municipio_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_puesto_votacion_municipio_id ON public.puesto_votacion USING btree (municipio_id);


--
-- Name: ciudadano ciudadano_mesa_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ciudadano
    ADD CONSTRAINT ciudadano_mesa_id_fkey FOREIGN KEY (mesa_id) REFERENCES public.mesa_votacion(id);


--
-- Name: mesa_votacion mesa_votacion_puesto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mesa_votacion
    ADD CONSTRAINT mesa_votacion_puesto_id_fkey FOREIGN KEY (puesto_id) REFERENCES public.puesto_votacion(id);


--
-- Name: municipio municipio_departamento_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.municipio
    ADD CONSTRAINT municipio_departamento_id_fkey FOREIGN KEY (departamento_id) REFERENCES public.departamento(id);


--
-- Name: puesto_votacion puesto_votacion_municipio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.puesto_votacion
    ADD CONSTRAINT puesto_votacion_municipio_id_fkey FOREIGN KEY (municipio_id) REFERENCES public.municipio(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

-- Insertar datos en la tabla departamento
INSERT INTO public.departamento (id, nombre) VALUES
(1, 'Antioquia'),
(2, 'Cundinamarca'),
(3, 'Valle del Cauca');

-- Insertar datos en la tabla municipio
INSERT INTO public.municipio (id, nombre, departamento_id) VALUES
(1, 'Medellín', 1),
(2, 'Envigado', 1),
(3, 'Bogotá', 2),
(4, 'Cali', 3);

-- Insertar datos en la tabla puesto_votacion
INSERT INTO public.puesto_votacion (id, nombre, consecutive, direccion, municipio_id) VALUES
(1, 'Puesto 1 Medellín', 101, 'Calle 10 #20-30', 1),
(2, 'Puesto 2 Medellín', 102, 'Carrera 15 #25-50', 1),
(3, 'Puesto 1 Bogotá', 201, 'Calle 1 #2-3', 3),
(4, 'Puesto 1 Cali', 301, 'Carrera 40 #50-60', 4);

-- Insertar datos en la tabla mesa_votacion
INSERT INTO public.mesa_votacion (id, consecutive, puesto_id) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 1, 3),
(4, 1, 4);

-- Insertar datos en la tabla ciudadano
INSERT INTO public.ciudadano (id, documento, nombre, apellido, mesa_id) VALUES
(1, '12345678', 'Juan', 'Pérez', 1),
(2, '87654321', 'María', 'Gómez', 2),
(3, '11223344', 'Carlos', 'López', 3),
(4, '44332211', 'Ana', 'Martínez', 4);

DO $$
BEGIN
    FOR i IN 1000001..100000000 LOOP
        INSERT INTO public.ciudadano (id, documento, nombre, apellido, mesa_id) VALUES
        (i, 'doc' || i, 'Nombre' || i, 'Apellido' || i, 1);
    END LOOP;
END $$;
