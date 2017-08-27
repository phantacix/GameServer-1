-- 1. ADD HP AND GENDER TO PLAYER TABLE
ALTER TABLE `players`
  ADD COLUMN `hp` INT NOT NULL DEFAULT 1,
  ADD COLUMN `gender` ENUM('m', 'k') NOT NULL;

-- 2. SET VERSION
UPDATE `s_version`
SET `version` = 4;