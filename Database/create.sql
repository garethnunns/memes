-- MySQL Script generated by MySQL Workbench
-- Mon Apr 17 16:42:45 2017
-- Model: Meme Me    Version: 1.5
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema garethnu_memes
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema garethnu_memes
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `garethnu_memes` DEFAULT CHARACTER SET utf8 ;
USE `garethnu_memes` ;

-- -----------------------------------------------------
-- Table `garethnu_memes`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `garethnu_memes`.`user` (
  `iduser` INT NOT NULL AUTO_INCREMENT,
  `ukey` VARCHAR(255) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `firstName` VARCHAR(60) NOT NULL,
  `surname` VARCHAR(60) NOT NULL,
  `picUri` VARCHAR(150) NULL DEFAULT NULL,
  `lat` DECIMAL(10,6) NULL DEFAULT NULL,
  `long` DECIMAL(10,6) NULL DEFAULT NULL,
  `joined` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `notifications` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`iduser`, `ukey`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `garethnu_memes`.`meme`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `garethnu_memes`.`meme` (
  `idmeme` INT NOT NULL AUTO_INCREMENT,
  `iduser` INT NOT NULL,
  `sizes` VARCHAR(150) NOT NULL,
  `caption` VARCHAR(140) NULL DEFAULT NULL,
  `posted` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `latitude` DECIMAL(10,6) NULL DEFAULT NULL,
  `longitude` DECIMAL(10,6) NULL DEFAULT NULL,
  PRIMARY KEY (`idmeme`),
  INDEX `userMeme_idx` (`iduser` ASC),
  CONSTRAINT `userMeme`
    FOREIGN KEY (`iduser`)
    REFERENCES `garethnu_memes`.`user` (`iduser`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `garethnu_memes`.`star`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `garethnu_memes`.`star` (
  `idmeme` INT NOT NULL,
  `iduser` INT NOT NULL,
  `starred` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idmeme`, `iduser`),
  INDEX `likeMeme_idx` (`idmeme` ASC),
  INDEX `likeUser` (`iduser` ASC),
  CONSTRAINT `likeMeme`
    FOREIGN KEY (`idmeme`)
    REFERENCES `garethnu_memes`.`meme` (`idmeme`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `likeUser`
    FOREIGN KEY (`iduser`)
    REFERENCES `garethnu_memes`.`user` (`iduser`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `garethnu_memes`.`comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `garethnu_memes`.`comment` (
  `idcomment` INT NOT NULL AUTO_INCREMENT,
  `idmeme` INT NOT NULL,
  `iduser` INT NOT NULL,
  `comment` VARCHAR(140) NOT NULL,
  `commented` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idcomment`),
  INDEX `commentMeme_idx` (`idmeme` ASC),
  INDEX `commentUser_idx` (`iduser` ASC),
  CONSTRAINT `commentMeme`
    FOREIGN KEY (`idmeme`)
    REFERENCES `garethnu_memes`.`meme` (`idmeme`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `commentUser`
    FOREIGN KEY (`iduser`)
    REFERENCES `garethnu_memes`.`user` (`iduser`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `garethnu_memes`.`share`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `garethnu_memes`.`share` (
  `idshare` INT NOT NULL AUTO_INCREMENT,
  `idmeme` INT NOT NULL,
  `iduser` INT NOT NULL,
  `caption` VARCHAR(140) NULL DEFAULT NULL,
  `shared` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idshare`),
  INDEX `shareMeme_idx` (`idmeme` ASC),
  INDEX `shareUser_idx` (`iduser` ASC),
  CONSTRAINT `shareMeme`
    FOREIGN KEY (`idmeme`)
    REFERENCES `garethnu_memes`.`meme` (`idmeme`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `shareUser`
    FOREIGN KEY (`iduser`)
    REFERENCES `garethnu_memes`.`user` (`iduser`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
