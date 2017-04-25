<?php
	require_once '../site/web.php';
	check();
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo 'Starred · ' . $sitename; ?></title>

		<?php include '../site/head.php'; ?>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<h1>Your starred memes</h1>
<?php
	$starred = memeStarredFeed($_SESSION['key'],0,400);

	if(!$starred['success'])
		echo "<p class='error'>There was an error displaying your starred memes&hellip;<br>{$starred['error']}</p>";
	elseif(!count($starred['memes']))
		echo "<p><i>You haven't starred any memes yet&hellip; but when you do, you can always come back and look at them here</p>";
	else {
		echo "<p>These are all the memes you've starred <span class='icon-star-full'></span></p>
		<div class='memeGrid'>";
		foreach ($starred['memes'] as $meme) 
			displayMemeGrid($meme);
		echo "</div>";
	}
?>
		</div>

		<?php include '../site/footer.php'; ?>
	</body>
</html>