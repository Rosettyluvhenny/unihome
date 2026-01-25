create table post
(
	id          varchar(36) primary key,
	user_id     varchar(36)    not null,
	title       varchar(100)   not null,
	price       decimal(12, 2) not null,
	status      varchar        not null,
	created_at  timestamp default current_timestamp,
	updated_at  timestamp,
	category_id UUID           not null,
	constraint fk_user_post foreign key (user_id) references users (id),
	constraint fk_category_post foreign key (category_id) references categories (category_id)
);
create table post_user_boost
(
	id            varchar(36) primary key,
	user_boost_id varchar(36) not null,
	post_id       varchar(36) not null,
	start_time    timestamp   not null,
	end_time      timestamp   not null,
	status        varchar     not null,
	created_at    timestamp default current_timestamp,
	updated_at    timestamp,
	constraint fk_post_user_boost_user_boost foreign key (user_boost_id) references user_boost (id),
	constraint fk_post_user_boost_post foreign key (post_id) references post (id)
);

create table post_detail
(
	id          varchar(36) primary key,
	post_id     varchar(36) not null,
	created_at  timestamp default current_timestamp,
	updated_at  timestamp,
	description varchar,
	image       varchar,
	constraint fk_post_post_detail foreign key (post_id) references post (id)
);

create table comment
(
	id         varchar(36) primary key,
	post_id    varchar(36) not null,
	reply_id   varchar(36) null,
	content    text,
	user_id    varchar(36) not null,
	created_at timestamp default current_timestamp,
	updated_at timestamp,
	constraint fk_user_comment foreign key (user_id) references users (id),
	constraint fk_post_comment foreign key (post_id) references post (id),
	constraint fk_comment_reply foreign key (reply_id) references comment (id)
)
