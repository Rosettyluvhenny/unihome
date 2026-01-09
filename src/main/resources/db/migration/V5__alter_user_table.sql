alter table users
	add column phone varchar(10);
alter table users
	add column image text;
update users
set phone='0000000000';
