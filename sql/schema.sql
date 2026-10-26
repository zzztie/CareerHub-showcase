create table user(
                     id bigint primary key auto_increment,
                     username varchar(50) not null unique ,
                     email varchar(100) not null unique ,
                     password_hash varchar(255) not null ,
                     status varchar(20) not null ,
                     create_time datetime not null ,
                     update_time datetime not null
);

create table resume(
                       id bigint primary key  auto_increment,
                       user_id bigint not null ,
                       file_name varchar(255) not null ,
                       file_path varchar(500) not null ,
                       create_time datetime not null ,
                       update_time datetime not null,

                       constraint fk_resume_user
                           foreign key (user_id)
                               references user(id)

);

create table job(
                    id bigint primary key auto_increment,
                    title varchar(100) not null,
                    company varchar(100) not null,
                    description text,
                    status varchar(20) not null default 'OPEN',
                    create_time datetime not null,
                    update_time datetime not null
);
