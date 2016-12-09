DROP TABLE IF EXISTS `users_similarities`;

CREATE TABLE `users_similarities` (
	`user1_id` INT NOT NULL,
	`user2_id` INT NOT NULL,
	`correlation` FLOAT DEFAULT 0,
	PRIMARY KEY (`user1_id`, `user2_id`)
);
