<?php
	require_once 'site/web.php';
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $sitename; ?></title>

		<?php 
			include 'site/head.php'; 

			$link = $web;
			$image = "http://memes-store.garethnunns.com/full/1000/22.jpg";
			$desc = $tagline;

			echo "
			<meta property=\"og:url\" content=\"{$link}\">
			<meta property=\"og:title\" content=\"{$title}\">
			<meta property=\"og:description\" content=\"{$desc}\">
			<meta property=\"og:image\" content=\"{$image}\">

			<meta property=\"twitter:title\" content=\"{$title}\">
			<meta property=\"twitter:description\" content=\"{$desc}\">
			<meta property=\"twitter:image\" content=\"{$image}\">

			<link rel=\"canonical\" href=\"{$link}\"/>
			<meta name=\"description\" content=\"{$desc}\">";
		?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>
		<div class="wrapper">
<?php
	if(!loggedIn()) { // not logged in
?>
			<form id="login" method="POST" action="login.php">
				<table>
					<tr>
						<td colspan="2">
							<h1>Welcome to <?php echo $sitename; ?></h1>
							<h3><?php echo $tagline; ?></h3>
							<h2>Please login</h2>

<?php
	if(isset($_GET['loginerror'])) echo "<p class='error'>The username or password was incorrect</p>";
	if(isset($_GET['emailerror'])) echo "<p class='error'>We're waiting on you to confirm your email address - check your emails</p>";
	if(isset($_GET['goingto'])) echo '<input type="hidden" name="goingto" value="'.htmlspecialchars($_GET['goingto']).'">';
?>

						</td>
					</tr>
					<tr>
						<td>Username</td>
						<td>
							<input type="text" name="username" placeholder="Username">
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
							<p><input type="submit" name="login" value="Login"></p>
							<p><a href="signup">or signup now&hellip;</a></p>
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

		if(!$memes['success']) // something went wrong
			echo "<p class='error'>".(isset($memes['error']) ? $memes['error'] : "There was an error fetching the memes")."</p>";
		elseif(!count($memes['memes']))
			echo "<p><i>There are no memes to show at the moment, see the best posts in the <a href='/hot'>hot feed</a></i></p>";
		else
			foreach ($memes['memes'] as $meme) 
				displayMeme($meme);
?>
<?php
	} // end of being logged in
?>

		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>