create table if not exists `marketplace`.`item` (
  `item_id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  `cost` INT NULL,
  `supply` INT NULL,
  `duration` VARCHAR(45) NULL,
  `type` VARCHAR(45) NULL,
  `effectmagnitude` VARCHAR(45) NULL,
  `attaffected` VARCHAR(45) NULL,
  PRIMARY KEY (`item_id`));
