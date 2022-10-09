create table departamento
(
    id                  varchar(32) not null,
    fecha_actualizacion timestamp,
    fecha_creacion      timestamp   not null,
    codigo              varchar(2)  not null,
    localidad           varchar(16) not null,
    nombre              varchar(16) not null,
    primary key (id)
);

create table empleado
(
    id                  varchar(32)    not null,
    fecha_actualizacion timestamp,
    fecha_creacion      timestamp      not null,
    cargo               varchar(16)    not null,
    codigo              varchar(4)     not null,
    comision            numeric(19, 2) not null,
    fecha_contratacion  date           not null,
    genero              varchar(1)     not null,
    nombre              varchar(16)    not null,
    salario             numeric(19, 2) not null,
    id_departamento     varchar(32),
    id_supervisor       varchar(32),
    primary key (id)
);

alter table departamento
    add constraint dept_uk_codigo unique (codigo);

alter table empleado
    add constraint empl_uk_codigo unique (codigo);

alter table empleado
    add constraint empl_fk_departamento
        foreign key (id_departamento)
            references departamento
            on delete cascade;

alter table empleado
    add constraint empl_fk_supervisor
        foreign key (id_supervisor)
            references empleado
            on delete cascade;
