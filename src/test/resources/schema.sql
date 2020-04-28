drop table if exists incoming_orders;

create table incoming_orders ( part_name varchar(20), quantity int, customer varchar(20));