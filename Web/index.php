<?php
	require_once 'site/functions.php';
?><!DOCTYPE html>
<html>
	<head>
		<title>Memes</title>

		<?php include 'site/head.php'; ?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>
		<div class="wrapper">
<?php
	if(!$_SESSION['user']) { // not logged in
?>
			<h1>Welcome to Meme Me, we're currently working on the site&hellip;</h1>

			<form id="login" method="POST" action="login.php">
				<table>
					<tr>
						<td colspan="2">
							<h2>Please login</h2>

<?php
	if(isset($_GET['loginerror'])) echo "<p class='error'>The username or password was incorrect</p>";
	if(isset($_GET['goingto'])) echo '<input type="hidden" name="goingto" value="'.htmlspecialchars($_GET['goingto']).'">';
?>

						</td>
					</tr>
					<tr>
						<td>Username</td>
						<td>
							<input type="text" name="username" placeholder="Username" minlength="3" maxlength="20">
						</td>
					</tr>
					<tr>
						<td>Password</td>
						<td>
							<input type="password" name="password" placeholder="Password">
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<input type="submit" name="login" value="Login">
							<p><a href="signup">Or signup</a></p>
						</td>
					</tr>
				</table>
			</form>
<?php
	}
	else {
		$sql = "SELECT user.username, user.firstName, user.surname  
				FROM user
				WHERE iduser = ?";

		$sth = $dbh->prepare($sql);

		$sth->execute(array($_SESSION['user']));

		if($sth->rowCount()!=1) die("<p>User not found</p>");

		$user = $sth->fetch(PDO::FETCH_OBJ);

		echo "<h1>Hello {$user->firstName} {$user->surname}</h1>";
?>
		<p><i>some memes...</i></p>
<?php
	} // end of being logged in
?>

		</div>

		<?php if($_SESSION['user']) include 'site/footer.php'; ?>
	</body>
</html>