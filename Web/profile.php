<?php
	require_once 'site/web.php';

	$found = true;

	if(($username = userDetailsFromUsername($_GET['username']))===false) $found = false;

	$profile = profile($_SESSION['key'],$username->iduser,0,400);

	$found = $profile['success'];

	if($found) $title = "{$profile['user']['name']} ({$profile['user']['username']})";
	else $title = "User not found";
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $title . ' · ' . $sitename; ?></title>

		<?php include 'site/head.php'; ?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>

		<div class="wrapper">
<?php
	if($found) {
		echo "
		<div class='profile'>
			<img src='{$profile['user']['pic']}' class='pp' alt='{$meme['poster']['username']} profile picture'>
			<h1>{$profile['user']['username']}</h1>
			<p>{$profile['user']['name']}</p>
			
		</div>";

		if(!count($profile['memes']))
			echo "<p><i>There are no memes to show at the moment, see the best posts in the <a href='/hot'>hot feed</a></i></p>";
		else {
			echo "<div class='memeGrid'>";
			foreach ($profile['memes'] as $meme) 
				displayMemeGrid($meme);
			echo "</div>";
		}
	}
	else {
		echo $pageError;
		if(isset($profile['error'])) echo "<p class='error'>{$profile['error']}</p>";
	}
?>
		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>