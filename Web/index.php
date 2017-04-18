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
	if(!loggedIn()) { // not logged in
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
		if(isset($_GET['new'])) {
			$user = userDetails($_SESSION['key']);
			echo "<h1>Welcome {$user->firstName} {$user->surname}</h1>";
		}

		$memes = memeFeed($_SESSION['key']);

		if(isset($memes['error'])) // something went wrong
			echo "<p class='error'>{$memes['error']}</p>";
		else {
			foreach ($memes as $meme) {
				echo "
				<h4>".(($meme['original']) ? 'Reposted by' : '')."
					<a href='{$web}{$meme['poster']['username']}' title='{$meme['poster']['name']}'>
						{$meme['poster']['username']}
					</a>
				</h4>
				<p>".($meme['original'] ? "Original by {$meme['original']['username']}" : "Posted by {$meme['poster']['name']}")."</p>
				<p><i>".date('r',$meme['time']['epoch'])."</i></p>
				<img src='{$meme['images']['full']}'>
				<p>{$meme['caption']}</p>";
			}
		}

		print_r($memes);
?>
<?php
	} // end of being logged in
?>

		</div>

		<?php if(loggedIn()) include 'site/footer.php'; ?>
	</body>
</html>