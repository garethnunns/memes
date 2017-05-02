<?php
	require_once 'site/web.php';

	$found = true;

	$meme = meme($_SESSION['key'],$_GET['meme'],400,null,false);

	if($meme['meme']['original'])
		$meme = meme($_SESSION['key'],$meme['meme']['idmeme'],400,null,false);

	$found = $meme['success'];

	if($found) $title = "Reposts on post by {$meme['meme']['poster']['username']}";
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
		displayListHeading($meme['meme'],'repost');

		$reposts = reposts($_SESSION['key'],$meme['meme']['idmeme'],0);

		if(!$reposts['success'])
			echo "<p class='error'>" . ($reposts['error'] ?: "There was an error getting the list of people") . "</p>";
		elseif(!$reposts['num']) 
			echo "<p>There are currently no reposts on this post 😭</p>";
		else {
			echo "<div class='userListContainer'>";
			foreach ($reposts['reposts'] as $repost)
				displayUserList($repost);
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