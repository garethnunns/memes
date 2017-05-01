<?php
	require_once 'site/web.php';

	$found = true;

	$meme = meme($_SESSION['key'],$_GET['meme'],400,null,false);

	$found = $meme['success'];

	if($found) $title = "Stars on post by {$meme['meme']['poster']['username']}";
	else $title = "Post not found";
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
		<div class='listHeadingContainer'>
			<a href='{$meme['meme']['link']}' class='meme'>
				<img src='{$meme['meme']['images']['thumb']}' alt='Meme ".($meme['meme']['original'] ? 'reposted' : 'posted')." by {$meme['meme']['poster']['username']}'>
			</a>
			<div class='details'>
				<h2>{$meme['meme']['stars-num']} {$meme['meme']['stars-str']} on this <a href='{$meme['meme']['link']}'>".($meme['meme']['original'] ? 'repost' : 'post')."</a> by <a href='{$meme['meme']['poster']['link']}'>{$meme['meme']['poster']['username']}</a></h2>
			</div>
		</div>";

		$stars = stars($_SESSION['key'],0);

		if(!$stars['success'])
			echo "<p class='error'>" . ($stars['error'] ?: "There was an error getting the list of people") . "</p>";
		elseif(!$stars['num']) 
			echo "<p>There are currently no stars on this post 😭</p>";
		else {
			echo "<div class='userListContainer'>";
			foreach ($stars['stars'] as $star)
				displayUserList($star);
			echo '</div>';
		}
	}
	else
		echo $pageError;
?>
		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>