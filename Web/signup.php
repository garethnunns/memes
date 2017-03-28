<?php
	require_once 'site/functions.php';

	if(isset($_POST['signup'])) { // adding a user

		// initalise an empty errors array that could crop up
		$errors = array();

		$fields = array( // simple array of all of the inputs and what fields they are for
			'user.firstName' => $_POST['firstName'],
			'user.surname' => $_POST['surname'],
			'user.email' => $_POST['email'],
			'user.username' => $_POST['username'],
			'user.password' => $_POST['password1']
		);

		// check they are valid string inputs in terms of length
		foreach ($fields as $field => $text)
			if(valid($field,$text) !== true)
				$errors[$field] .= " ".valid($field,$text);

		// check the username is valid in terms of what it contains
		preg_match('/^([_|-]?[[:alnum:]][_|-]?){0,20}/', $_POST['username'], $usermatches, PREG_OFFSET_CAPTURE);
		if(($usermatches[0][0]!=$_POST['username']) || ($usermatches[0][1]!=0))
			$errors['user.username'] .= " The username can only contain alphanumeric characters (letters and numbers), as well as underscores and dashes (_ or -). <i>You can't have two underscores or dashes next to each other</i>.";


		// check the username hasn't been taken by another user
		try {
			$sql = "SELECT COUNT(iduser) FROM user WHERE username = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array($_POST['username']));

			if($sth->fetchColumn()>0) 
				$errors['user.username'] .= " The username '".htmlspecialchars($_POST['username'])."' has already been taken.";
		}
		catch (PDOException $e) {
			echo $e->getMessage();
		}

		// check passwords are the same
		if($_POST['password1'] != $_POST['password2'])
			$errors['user.password'] .= " The passwords don't match.";

		// check the password is strong enough
		// must have a lowercase, uppercase and number in
		preg_match('/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]{8,50}$/', $_POST['password1'], $passmatches, PREG_OFFSET_CAPTURE);
		if(($passmatches[0][0]!=$_POST['password1']) || ($passmatches[0][1]!=0))
			$errors['user.password'] .= " The password must contain a lowercase letter, an uppercase letter and a number.";


		if(!$errors) { // we've made it through all the checks and we want to add to the database

			// generate unique key that isn't in the db yet
			do {
				$notUnique = true;

				$key = bin2hex(openssl_random_pseudo_bytes(125));

				try {
					$sql = "SELECT COUNT(iduser) FROM user WHERE ukey = ?";

					$sth = $dbh->prepare($sql);

					$sth->execute(array($key));

					if($sth->fetchColumn()==0) 
						$notUnique = false;
				}
				catch (PDOException $e) {
					echo $e->getMessage();
				}
			} while ($notUnique);

			// we've got everything we need so we'll add them to the database
			$sth = $dbh->prepare("INSERT INTO user (ukey, username, password, email, firstName, surname) 
				VALUES (?, ?, ?, ?, ?, ?)");

			$sth->execute(array(
				$key,
				$_POST['username'],
				password_hash($_POST['password1'],PASSWORD_DEFAULT),
				$_POST['email'],
				$_POST['firstName'],
				$_POST['surname']
			));

			// yay, all done so send them off to the home page - could set some session variables here so they're logged in
			header("Location: /?created");
		}
	}

?><!DOCTYPE html>
<html>
	<head>
		<title>Memes · Sign up</title>

		<link rel="stylesheet" type="text/css" href="/site/memes.css">
	</head>

	<body>
		<header>
			<div class="wrapper">
				<h1>Meme Me</h1>
			</div>
		</header>

		<div class="wrapper">
			<form id="signup" method="POST">
				<h2>Welcome to Meme Me</h2>

				<p>We only collect the essential information here</p>

				<table>
					<tr>
						<td>First Name</td>
						<td>
							<input type="text" name="firstName" placeholder="First name" maxlength="60" value="<?php echo $_POST['firstName'] ?>">
							<?php if(isset($errors['user.firstName'])) echo "<p class='error'>{$errors['user.firstName']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Surname</td>
						<td>
							<input type="text" name="surname" placeholder="Surname" maxlength="60" value="<?php echo $_POST['surname'] ?>">
							<?php if(isset($errors['user.surname'])) echo "<p class='error'>{$errors['user.surname']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Email</td>
						<td>
							<input type="email" name="email" placeholder="Email address" maxlength="100" value="<?php echo $_POST['email'] ?>">
							<?php if(isset($errors['user.email'])) echo "<p class='error'>{$errors['user.email']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Username</td>
						<td>
							<input type="text" name="username" placeholder="Pick a username" minlength="3" maxlength="20" value="<?php echo $_POST['username'] ?>">
							<?php if(isset($errors['user.username'])) echo "<p class='error'>{$errors['user.username']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Password</td>
						<td>
							<input type="password" name="password1" placeholder="Password">
							<?php if(isset($errors['user.password'])) echo "<p class='error'>{$errors['user.password']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Confirm Password</td>
						<td><input type="password" name="password2" placeholder="Confirm password"></td>
					</tr>
				</table>
				<input type="submit" value="Sign up" name="signup">
			</form>
		</div>
	</body>
</html>