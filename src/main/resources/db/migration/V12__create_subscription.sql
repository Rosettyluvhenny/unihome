create table boost
(
	id         varchar(36) primary key,
	name       varchar(100),
	price      decimal(12, 2),
	duration   int,
	created_at timestamp default current_timestamp,
	updated_at timestamp,
	status     varchar
);

create table user_boost
(
	id         varchar(36) primary key,
	user_id    varchar(36) not null,
	price      decimal(12, 2),
	created_at timestamp default current_timestamp,
	updated_at timestamp,
	status     varchar,
	constraint fk_user_user_boost foreign key (user_id) references users (id)
);





