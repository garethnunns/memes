<?php
	require_once '../site/web.php';
	check();
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo 'Notifications · ' . $sitename; ?></title>

		<?php include '../site/head.php'; ?>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<h1>Notifications</h1>
<?php
	$nots = notifications($_SESSION['key'],0,150);

	echo '<div class="notifications-containter">';
	foreach ($nots['notifications'] as $not) {
		echo '<div class="notification '.($not['unread'] ? 'unread' : 'read').'">';

		$user = "<a href='{$not['user']['link']}' class='pp'>
			<img src='{$not['user']['pic']}' alt='{$not['user']['username']} profile picture' class='pp'/></a> ";

		switch ($not['type']) {
			case 'follow':
				echo "<div class='follow'>".($not['user']['you'] ? '(you)' : 
					("<button onClick='follow(this,{$meme['user']['iduser']})'>". 
					(($not['user']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>")).
				"</div>
				$user
				<a href='{$not['user']['link']}'>{$not['user']['name']}</a> "." followed you";
				break;
			
			default:
				echo "
				<a href='{$not['meme']['link']}' class='meme'>
					<img src='{$not['meme']['images']['thumb']}' alt='Your post'>
				</a>
				$user
				<a href='{$not['user']['link']}'>{$not['user']['name']}</a> ".
				($not['type']=='star' ? 'starred' : 
					($not['type']=='comment' ? 'commented' : 
						($not['type']=='repost' ? 'reposted' : $not['type'].'ed'))).
				" your <a href='{$not['meme']['link']}' title='Go to meme'>".
				($not['meme']['original'] ? 'repost' : 'post')."</a>";
				break;
		}
		echo '</div>';
	}
	echo '</div>';

	print_r($nots);

	/*
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
	} */
?>
		</div>

		<?php include '../site/footer.php'; ?>
	</body>
</html>