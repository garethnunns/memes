<?php
	require_once '../site/web.php';

	if(loggedIn()) // user already logged in
		header("Location: /");

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

		// check passwords are the same
		if($_POST['password1'] != $_POST['password2'])
			$errors['user.password'] .= " The passwords don't match.";


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
					$dbError = $e;
				}
			} while ($notUnique);

			// we've got everything we need so we'll add them to the database
			try {
				$sth = $dbh->prepare("INSERT INTO user (ukey, username, password, email, firstName, surname, picUri) 
					VALUES (?, ?, ?, ?, ?, ?, ?)");

				$sth->execute(array(
					$key,
					$_POST['username'],
					password_hash($_POST['password1'],PASSWORD_DEFAULT),
					$_POST['email'],
					$_POST['firstName'],
					$_POST['surname'],
					$defaultPics[mt_rand(0, count($$defaultPicture) - 1)]
				));

				// yay, all done so send them off to the home page - could set some session variables here so they're logged in
				$_SESSION['user'] = $dbh->lastInsertId();
				$_SESSION['key'] = $key;

				follow($key,1); // follow the first account made on the server so that their feed isn't empty
				
				header("Location: /?new");
			}
			catch (PDOException $e) {
				$dbError = $e;
			}
		}
	}

?><!DOCTYPE html>
<html>
	<head>
		<title>Signup · <?php echo $sitename; ?></title>

		<?php include '../site/head.php'; ?>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<form id="signup" method="POST">
				<table>
					<tr>
						<td colspan="2">
							<h1>Welcome to <?php echo $sitename; ?></h1>

							<p>We only collect the essential information here</p>

							<?php if(!empty($dbError)) echo '<p class="error">There was an error adding you to the database :( have another go</p>'; ?>
						</td>
					</tr>
					<tr>
						<td>First Name</td>
						<td>
							<input type="text" name="firstName" placeholder="First name" minlength="1" maxlength="60" value="<?php if(isset($_POST['firstName'])) echo $_POST['firstName'] ?>" title="The first name can only contain letters, spaces, hyphens and full stops." required>
							<?php if(isset($errors['user.firstName'])) echo "<p class='error'>{$errors['user.firstName']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Surname</td>
						<td>
							<input type="text" name="surname" placeholder="Surname" minlength="1" maxlength="60" value="<?php if(isset($_POST['surname'])) echo $_POST['surname'] ?>" title="The surname can only contain letters, spaces, hyphens and full stops." required>
							<?php if(isset($errors['user.surname'])) echo "<p class='error'>{$errors['user.surname']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Email</td>
						<td>
							<input type="email" name="email" placeholder="Email address" maxlength="100" value="<?php if(isset($_POST['email'])) echo $_POST['email'] ?>" required>
							<?php if(isset($errors['user.email'])) echo "<p class='error'>{$errors['user.email']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Username</td>
						<td>
							<input type="text" name="username" placeholder="Pick a username" minlength="3" maxlength="20" value="<?php if(isset($_POST['username'])) echo $_POST['username'] ?>" pattern="^([_|-]?[a-zA-Z0-9][_|-]?){0,20}" title="The username can only contain alphanumeric characters (letters and numbers), as well as underscores and dashes (_ or -). You can't have two underscores or dashes next to each other." required>
							<?php if(isset($errors['user.username'])) echo "<p class='error'>{$errors['user.username']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Password</td>
						<td>
							<input type="password" name="password1" placeholder="Password" minlength="8" maxlength="50" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]{8,50}$" title="The password must contain a lowercase letter, an uppercase letter and a number" required>
							<?php if(isset($errors['user.password'])) echo "<p class='error'>{$errors['user.password']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Confirm Password</td>
						<td>
							<input type="password" name="password2" placeholder="Confirm password" minlength="8" maxlength="50" required>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<input type="submit" value="Sign up" name="signup">
						</td>
					</tr>
				</table>
			</form>
		</div>
	</body>
</html>